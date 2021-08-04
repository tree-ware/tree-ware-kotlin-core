package org.treeWare.model.core

import org.treeWare.schema.core.*
import java.math.BigDecimal

abstract class MutableElementModel<Aux> : ElementModel<Aux> {
    var objectId = ""

    override var aux: Aux? = null
        internal set
}

class MutableModel<Aux>(override val schema: Schema) : MutableElementModel<Aux>(), Model<Aux> {
    override val parent: ElementModel<Aux>? = null

    override var type = "data"
        internal set(value) {
            field = value
            _root?.also { it.objectId = value }
        }

    override var root: MutableRootModel<Aux>
        get() = _root ?: throw IllegalStateException("Root has not been set")
        internal set(value) {
            value.objectId = type
            _root = value
        }
    private var _root: MutableRootModel<Aux>? = null

    override fun matches(that: ElementModel<*>): Boolean = false // Not yet needed, so not yet supported.

    fun getOrNewRoot(): MutableRootModel<Aux> {
        if (_root == null) _root = newMutableModel(schema.root, this) as MutableRootModel<Aux>
        return root
    }
}

abstract class MutableBaseEntityModel<Aux>(
    internal val entitySchema: EntitySchema
) : MutableElementModel<Aux>(), BaseEntityModel<Aux> {
    override var fields: MutableList<MutableFieldModel<Aux>> = mutableListOf()
        internal set

    override fun matches(that: ElementModel<*>): Boolean {
        if (that !is BaseEntityModel<*>) return false
        val thisKeyFields = this.fields.filter { it.schema.isKey }
        return thisKeyFields.all { thisKeyField ->
            val thatKeyField = that.getField(thisKeyField.schema.name) ?: return false
            thisKeyField.matches(thatKeyField)
        }
    }

    // TODO(deepak-nulu): optimize
    override fun getField(fieldName: String): MutableFieldModel<Aux>? = fields.find { it.schema.name == fieldName }

    fun getOrNewField(fieldName: String): MutableFieldModel<Aux>? {
        val existing = getField(fieldName)
        if (existing != null) return existing
        val fieldSchema = entitySchema.getField(fieldName) ?: return null
        val newField = newMutableModel(fieldSchema, this) as? MutableFieldModel<Aux> ?: return null
        fields.add(newField)
        return newField
    }
}

class MutableRootModel<Aux>(
    override val schema: RootSchema,
    override val parent: MutableModel<Aux>
) : MutableBaseEntityModel<Aux>(schema.resolvedEntity), RootModel<Aux>

class MutableEntityModel<Aux>(
    override val schema: EntitySchema,
    override val parent: MutableFieldModel<Aux>
) : MutableBaseEntityModel<Aux>(schema), EntityModel<Aux>

// Fields

abstract class MutableFieldModel<Aux>(
    override val schema: FieldSchema,
    override val parent: MutableBaseEntityModel<Aux>
) : MutableElementModel<Aux>(), FieldModel<Aux>

class MutableSingleFieldModel<Aux>(
    schema: FieldSchema,
    parent: MutableBaseEntityModel<Aux>
) : MutableFieldModel<Aux>(schema, parent), SingleFieldModel<Aux> {
    override var value: MutableElementModel<Aux>? = null
        internal set

    override fun matches(that: ElementModel<*>): Boolean {
        if (that !is SingleFieldModel<*>) return false
        val thisValue = this.value
        val thatValue = that.value
        if (thisValue == null) return thatValue == null
        if (thatValue == null) return false
        return thisValue.matches(thatValue)
    }

    fun getOrNewValue(): MutableElementModel<Aux> {
        val existing = value
        if (existing != null) return existing
        val newValue = newMutableValueModel(schema, this)
        value = newValue
        return newValue
    }

    fun setValue(value: MutableElementModel<Aux>?) {
        this.value = value
    }
}

class MutableListFieldModel<Aux>(
    schema: FieldSchema,
    parent: MutableBaseEntityModel<Aux>
) : MutableFieldModel<Aux>(schema, parent), ListFieldModel<Aux> {
    override val values = mutableListOf<MutableElementModel<Aux>>()

    override fun matches(that: ElementModel<*>): Boolean = false // Not yet needed, so not yet supported.
    override fun firstValue(): ElementModel<Aux>? = values.firstOrNull()
    override fun getValue(index: Int): ElementModel<Aux>? = values.getOrNull(index)
    override fun getValueMatching(that: ElementModel<*>): ElementModel<Aux>? = values.find { it.matches(that) }

    /** Adds a new value to the list and returns the new value. */
    fun getNewValue(): MutableElementModel<Aux> {
        val newValue = newMutableValueModel(schema, this)
        addValue(newValue)
        return newValue
    }

    fun addValue(value: MutableElementModel<Aux>) {
        values.add(value)
    }
}

// Values

abstract class MutableScalarValueModel<Aux>(
    override val parent: MutableFieldModel<Aux>
) : MutableElementModel<Aux>() {
    open fun setNullValue(): Boolean = false
    open fun setValue(value: String): Boolean = false
    open fun setValue(value: BigDecimal): Boolean = false
    open fun setValue(value: Boolean): Boolean = false
}

class MutablePrimitiveModel<Aux>(
    override val schema: PrimitiveFieldSchema,
    parent: MutableFieldModel<Aux>
) : MutableScalarValueModel<Aux>(parent), PrimitiveModel<Aux> {
    override var value: Any? = null
        internal set

    override fun matches(that: ElementModel<*>): Boolean {
        if (that !is PrimitiveModel<*>) return false
        return this.value == that.value
    }

    override fun setNullValue(): Boolean {
        this.value = null
        return true
    }

    override fun setValue(value: String): Boolean = setValue(schema.primitive, value) { this.value = it }
    override fun setValue(value: BigDecimal): Boolean = setValue(schema.primitive, value) { this.value = it }
    override fun setValue(value: Boolean): Boolean = setValue(schema.primitive, value) { this.value = it }
}

class MutableAliasModel<Aux>(
    override val schema: AliasFieldSchema,
    parent: MutableFieldModel<Aux>
) : MutableScalarValueModel<Aux>(parent), AliasModel<Aux> {
    override var value: Any? = null
        internal set

    override fun matches(that: ElementModel<*>): Boolean {
        if (that !is AliasModel<*>) return false
        return this.value == that.value
    }

    override fun setNullValue(): Boolean {
        this.value = null
        return true
    }

    override fun setValue(value: String): Boolean =
        setValue(schema.resolvedAlias.primitive, value) { this.value = it }

    override fun setValue(value: BigDecimal): Boolean =
        setValue(schema.resolvedAlias.primitive, value) { this.value = it }

    override fun setValue(value: Boolean): Boolean =
        setValue(schema.resolvedAlias.primitive, value) { this.value = it }
}

class MutableEnumerationModel<Aux>(
    override val schema: EnumerationFieldSchema,
    parent: MutableFieldModel<Aux>
) : MutableScalarValueModel<Aux>(parent), EnumerationModel<Aux> {
    override var value: EnumerationValueSchema? = null
        internal set

    override fun matches(that: ElementModel<*>): Boolean {
        if (that !is EnumerationModel<*>) return false
        return this.value == that.value
    }

    override fun setNullValue(): Boolean {
        this.value = null
        return true
    }

    override fun setValue(value: String): Boolean = setValue(schema.resolvedEnumeration, value) { this.value = it }
}

class MutableAssociationModel<Aux>(
    override val schema: AssociationFieldSchema,
    override val parent: MutableFieldModel<Aux>
) : MutableElementModel<Aux>(), AssociationModel<Aux> {
    override var value: List<MutableEntityKeysModel<Aux>> = listOf()
        internal set

    override fun matches(that: ElementModel<*>): Boolean {
        if (that !is AssociationModel<*>) return false
        if (this.value.size != that.value.size) return false
        return this.value.zip(that.value).all { (a, b) -> a.matches(b) }
    }

    fun setNullValue(): Boolean {
        this.value = listOf()
        return true
    }

    fun newValue(): List<MutableEntityKeysModel<Aux>> {
        value = schema.keyEntities.map { MutableEntityKeysModel(it, this) }
        return value
    }
}

// Sub-values

class MutableEntityKeysModel<Aux>(
    override val schema: EntitySchema,
    override val parent: AssociationModel<Aux>
) : MutableBaseEntityModel<Aux>(schema), EntityKeysModel<Aux>

// Helpers

typealias ValueSetter = (Any) -> Unit

fun setValue(primitive: PrimitiveSchema, value: String, setter: ValueSetter): Boolean {
    return when (primitive) {
        is StringSchema,
        is UuidSchema -> {
            setter(value)
            true
        }
        is Password1WaySchema,
        is Password2WaySchema,
        is BlobSchema -> {
            // TODO(deepak-nulu): special handling for Password1WaySchema, Password2WaySchema, BlobSchema
            setter(value)
            true
        }
        // 64-bit integers are encoded as strings because JavaScript integers are only 53-bits
        is LongSchema,
        is TimestampSchema ->
            try {
                setter(value.toLong())
                true
            } catch (e: NumberFormatException) {
                false
            }
        else -> false
    }
}

fun setValue(primitive: PrimitiveSchema, value: BigDecimal, setter: ValueSetter): Boolean {
    return when (primitive) {
        is ByteSchema ->
            try {
                setter(value.toByte())
                true
            } catch (e: java.lang.NumberFormatException) {
                false
            }
        is ShortSchema ->
            try {
                setter(value.toShort())
                true
            } catch (e: java.lang.NumberFormatException) {
                false
            }
        is IntSchema ->
            try {
                setter(value.toInt())
                true
            } catch (e: java.lang.NumberFormatException) {
                false
            }
        is FloatSchema ->
            try {
                setter(value.toFloat())
                true
            } catch (e: java.lang.NumberFormatException) {
                false
            }
        is DoubleSchema ->
            try {
                setter(value.toDouble())
                true
            } catch (e: java.lang.NumberFormatException) {
                false
            }
        else -> false
    }
}

fun setValue(primitive: PrimitiveSchema, value: Boolean, setter: ValueSetter): Boolean {
    return when (primitive) {
        is BooleanSchema -> {
            setter(value)
            true
        }
        else -> false
    }
}

typealias EnumerationSetter = (EnumerationValueSchema) -> Unit

fun setValue(enumeration: EnumerationSchema, value: String, setter: EnumerationSetter): Boolean {
    val enumerationValue = enumeration.valueFromString(value) ?: return false
    setter(enumerationValue)
    return true
}
