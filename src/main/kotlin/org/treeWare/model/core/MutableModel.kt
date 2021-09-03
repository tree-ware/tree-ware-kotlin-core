package org.treeWare.model.core

import org.treeWare.metaModel.*
import java.math.BigDecimal

abstract class MutableElementModel<Aux> : ElementModel<Aux> {
    override val meta: ElementModel<Resolved>? = null
    override var aux: Aux? = null
        internal set
}

class MutableModel<Aux>(override val meta: Model<Resolved>?) :
    MutableElementModel<Aux>(), Model<Aux> {
    override val parent: ElementModel<Aux>? = null

    override var type = "data"
        internal set

    override var root: MutableRootModel<Aux>
        get() = _root ?: throw IllegalStateException("Root has not been set")
        internal set(value) {
            _root = value
        }
    private var _root: MutableRootModel<Aux>? = null

    override fun matches(that: ElementModel<*>): Boolean = false // Not yet needed, so not yet supported.

    fun getOrNewRoot(): MutableRootModel<Aux> {
        if (_root == null) {
            val rootMeta = meta?.let { getRootMeta(it) }
            val resolvedRootMeta = rootMeta?.aux?.compositionMeta
            _root = MutableRootModel(resolvedRootMeta, this)
        }
        return root
    }
}

abstract class MutableBaseEntityModel<Aux>(
    override val meta: EntityModel<Resolved>?
) : MutableElementModel<Aux>(), BaseEntityModel<Aux> {
    override var fields = LinkedHashMap<String, MutableFieldModel<Aux>>()
        internal set

    override fun matches(that: ElementModel<*>): Boolean {
        if (that !is BaseEntityModel<*>) return false
        val thisKeyFields = this.fields.values.filter { isKeyFieldMeta(it.meta) }
        return thisKeyFields.all { thisKeyField ->
            val thatKeyField = that.getField(getMetaName(thisKeyField.meta)) ?: return false
            thisKeyField.matches(thatKeyField)
        }
    }

    override fun getField(fieldName: String): MutableFieldModel<Aux>? = fields[fieldName]

    fun getOrNewField(fieldName: String): MutableFieldModel<Aux> {
        val existing = getField(fieldName)
        if (existing != null) return existing
        val fieldMeta = meta?.let { getFieldMeta(it, fieldName) }
            ?: throw IllegalStateException("fieldMeta is null when creating mutable field model")
        val newField = if (isListFieldMeta(fieldMeta)) MutableListFieldModel(fieldMeta, this)
        else MutableSingleFieldModel(fieldMeta, this)
        fields[fieldName] = newField
        return newField
    }
}

class MutableRootModel<Aux>(
    meta: EntityModel<Resolved>?,
    override val parent: MutableModel<Aux>
) : MutableBaseEntityModel<Aux>(meta), RootModel<Aux>

class MutableEntityModel<Aux>(
    meta: EntityModel<Resolved>?,
    override val parent: MutableFieldModel<Aux>
) : MutableBaseEntityModel<Aux>(meta), EntityModel<Aux>

// Fields

abstract class MutableFieldModel<Aux>(
    override val meta: EntityModel<Resolved>?,
    override val parent: MutableBaseEntityModel<Aux>
) : MutableElementModel<Aux>(), FieldModel<Aux>

class MutableSingleFieldModel<Aux>(
    meta: EntityModel<Resolved>?,
    parent: MutableBaseEntityModel<Aux>
) : MutableFieldModel<Aux>(meta, parent), SingleFieldModel<Aux> {
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
        val newValue = newMutableValueModel(meta, this)
        value = newValue
        return newValue
    }

    fun setValue(value: MutableElementModel<Aux>?) {
        this.value = value
    }
}

class MutableListFieldModel<Aux>(
    meta: EntityModel<Resolved>?,
    parent: MutableBaseEntityModel<Aux>
) : MutableFieldModel<Aux>(meta, parent), ListFieldModel<Aux> {
    override val values = mutableListOf<MutableElementModel<Aux>>()

    override fun matches(that: ElementModel<*>): Boolean = false // Not yet needed, so not yet supported.
    override fun firstValue(): ElementModel<Aux>? = values.firstOrNull()
    override fun getValue(index: Int): ElementModel<Aux>? = values.getOrNull(index)
    override fun getValueMatching(that: ElementModel<*>): ElementModel<Aux>? = values.find { it.matches(that) }

    /** Adds a new value to the list and returns the new value. */
    fun getNewValue(): MutableElementModel<Aux> {
        val newValue = newMutableValueModel(meta, this)
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

    override fun setValue(value: String): Boolean = setValue(parent.meta, value) { this.value = it }
    override fun setValue(value: BigDecimal): Boolean = setValue(parent.meta, value) { this.value = it }
    override fun setValue(value: Boolean): Boolean = setValue(parent.meta, value) { this.value = it }
}

class MutableAliasModel<Aux>(
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

    override fun setValue(value: String): Boolean = setValue(parent.meta, value) { this.value = it }
    override fun setValue(value: BigDecimal): Boolean = setValue(parent.meta, value) { this.value = it }
    override fun setValue(value: Boolean): Boolean = setValue(parent.meta, value) { this.value = it }
}

class MutableEnumerationModel<Aux>(
    parent: MutableFieldModel<Aux>
) : MutableScalarValueModel<Aux>(parent), EnumerationModel<Aux> {
    override var value: String? = null
        internal set

    override fun matches(that: ElementModel<*>): Boolean {
        if (that !is EnumerationModel<*>) return false
        return this.value == that.value
    }

    override fun setNullValue(): Boolean {
        this.value = null
        return true
    }

    override fun setValue(value: String): Boolean = setEnumerationValue(parent.meta, value) { this.value = it }
}

class MutableAssociationModel<Aux>(
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
        val keyEntityMetaList = parent.meta?.aux?.associationMeta?.keyEntityMetaList ?: listOf()
        value = keyEntityMetaList.map { MutableEntityKeysModel(it, this) }
        return value
    }
}

// Sub-values

class MutableEntityKeysModel<Aux>(
    meta: EntityModel<Resolved>?,
    override val parent: AssociationModel<Aux>
) : MutableBaseEntityModel<Aux>(meta), EntityKeysModel<Aux>

// Helpers

typealias ValueSetter = (Any) -> Unit

fun setValue(fieldMeta: EntityModel<Resolved>?, value: String, setter: ValueSetter): Boolean {
    return when (getFieldTypeMeta(fieldMeta)) {
        FieldType.STRING,
        FieldType.UUID -> {
            setter(value)
            true
        }
        FieldType.PASSWORD1WAY,
        FieldType.PASSWORD2WAY,
        FieldType.BLOB -> {
            // TODO(deepak-nulu): special handling for Password1WaySchema, Password2WaySchema, BlobSchema
            setter(value)
            true
        }
        // 64-bit integers are encoded as strings because JavaScript integers are only 53-bits
        FieldType.LONG,
        FieldType.TIMESTAMP ->
            try {
                setter(value.toLong())
                true
            } catch (e: NumberFormatException) {
                false
            }
        else -> false
    }
}

fun setValue(fieldMeta: EntityModel<Resolved>?, value: BigDecimal, setter: ValueSetter): Boolean {
    return when (getFieldTypeMeta(fieldMeta)) {
        FieldType.BYTE ->
            try {
                setter(value.toByte())
                true
            } catch (e: java.lang.NumberFormatException) {
                false
            }
        FieldType.SHORT ->
            try {
                setter(value.toShort())
                true
            } catch (e: java.lang.NumberFormatException) {
                false
            }
        FieldType.INT ->
            try {
                setter(value.toInt())
                true
            } catch (e: java.lang.NumberFormatException) {
                false
            }
        FieldType.FLOAT ->
            try {
                setter(value.toFloat())
                true
            } catch (e: java.lang.NumberFormatException) {
                false
            }
        FieldType.DOUBLE ->
            try {
                setter(value.toDouble())
                true
            } catch (e: java.lang.NumberFormatException) {
                false
            }
        else -> false
    }
}

fun setValue(fieldMeta: EntityModel<Resolved>?, value: Boolean, setter: ValueSetter): Boolean {
    return when (getFieldTypeMeta(fieldMeta)) {
        FieldType.BOOLEAN -> {
            setter(value)
            true
        }
        else -> false
    }
}

typealias StringSetter = (String) -> Unit

fun setEnumerationValue(
    fieldMeta: EntityModel<Resolved>?,
    value: String,
    setter: StringSetter
): Boolean {
    val enumerationValue = if (fieldMeta == null) value else {
        val resolvedEnumeration = fieldMeta.aux?.enumerationMeta
            ?: throw IllegalStateException("Enumeration has not been resolved")
        val enumerationValues = getEnumerationValues(resolvedEnumeration)
        if (enumerationValues.contains(value)) value else return false
    }
    setter(enumerationValue)
    return true
}
