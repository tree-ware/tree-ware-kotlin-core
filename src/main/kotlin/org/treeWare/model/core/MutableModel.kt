package org.treeWare.model.core

import org.treeWare.metaModel.*
import java.math.BigDecimal
import java.util.*

abstract class MutableElementModel<Aux> : ElementModel<Aux> {
    override val meta: ElementModel<Resolved>? = null
    override var aux: Aux? = null
        internal set
}

class MutableMainModel<Aux>(override val meta: MainModel<Resolved>?) :
    MutableElementModel<Aux>(), MainModel<Aux> {
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
        val thisKeyFields = getKeyFields()
        if (thisKeyFields.isEmpty()) throw MissingKeysException("No key fields")
        return thisKeyFields.all { thisKeyField ->
            val thatKeyField = that.getField(getMetaName(thisKeyField.meta)) ?: return false
            thisKeyField.matches(thatKeyField)
        }
    }

    // NOTE: key values cannot be changed after an entity has been added to its parent set.
    override fun getMatchingHashCode(): Int {
        val keyValues = getKeyValues().toTypedArray()
        return Objects.hash(*keyValues)
    }

    override fun getField(fieldName: String): MutableFieldModel<Aux>? = fields[fieldName]

    fun getOrNewField(fieldName: String): MutableFieldModel<Aux> {
        val existing = getField(fieldName)
        if (existing != null) return existing
        val fieldMeta = meta?.let { getFieldMeta(it, fieldName) }
            ?: throw IllegalStateException("fieldMeta is null when creating mutable field model")
        val newField = when (getMultiplicityMeta(fieldMeta)) {
            Multiplicity.LIST -> MutableListFieldModel(fieldMeta, this)
            Multiplicity.SET -> MutableSetFieldModel(fieldMeta, this)
            else -> MutableSingleFieldModel(fieldMeta, this)
        }
        fields[fieldName] = newField
        return newField
    }

    private fun getKeyFields(): List<MutableFieldModel<Aux>> {
        val fieldsMeta = meta?.let { getFieldsMeta(it) } ?: return listOf()
        val keyFieldsMeta = filterKeyFields(fieldsMeta.values)
        val missingKeys = mutableListOf<String>()
        val keyFields = mutableListOf<MutableFieldModel<Aux>>()
        keyFieldsMeta.forEach { fieldMeta ->
            val keyFieldName = getMetaName(fieldMeta)
            val keyField = this.fields[keyFieldName]
            if (keyField == null) missingKeys.add(keyFieldName) else keyFields.add(keyField)
        }
        if (missingKeys.isNotEmpty()) throw MissingKeysException("Missing key fields: $missingKeys")
        return keyFields
    }

    fun getKeyValues(): List<Any?> = getKeyFields().flatMap { field ->
        if (field.elementType == ModelElementType.SINGLE_FIELD) {
            val singleField = field as MutableSingleFieldModel
            when (getFieldTypeMeta(singleField.meta)) {
                FieldType.COMPOSITION -> (singleField.value as MutableBaseEntityModel<Aux>).getKeyValues()
                else -> listOf((singleField.value as MutablePrimitiveModel<Aux>).value)
            }
        } else throw IllegalStateException("Unexpected element type ${field.elementType} for key ${field.meta?.aux?.fullName}")
    }
}

class MutableRootModel<Aux>(
    meta: EntityModel<Resolved>?,
    override val parent: MutableMainModel<Aux>
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

abstract class MutableCollectionFieldModel<Aux>(
    meta: EntityModel<Resolved>?,
    parent: MutableBaseEntityModel<Aux>
) : MutableFieldModel<Aux>(meta, parent), CollectionFieldModel<Aux> {
    abstract override val values: MutableCollection<MutableElementModel<Aux>>
}

class MutableListFieldModel<Aux>(
    meta: EntityModel<Resolved>?,
    parent: MutableBaseEntityModel<Aux>
) : MutableCollectionFieldModel<Aux>(meta, parent), ListFieldModel<Aux> {
    override val values = mutableListOf<MutableElementModel<Aux>>()

    override fun matches(that: ElementModel<*>): Boolean = false // Not yet needed, so not yet supported.
    override fun firstValue(): ElementModel<Aux>? = values.firstOrNull()
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

class MutableSetFieldModel<Aux>(
    meta: EntityModel<Resolved>?,
    parent: MutableBaseEntityModel<Aux>
) : MutableCollectionFieldModel<Aux>(meta, parent), SetFieldModel<Aux> {
    private val linkedHashMap = LinkedHashMap<ElementModelId, MutableElementModel<Aux>>()
    override val values get() = linkedHashMap.values

    override fun matches(that: ElementModel<*>): Boolean = false // Not yet needed, so not yet supported.
    override fun firstValue(): ElementModel<Aux>? = values.iterator().takeIf { it.hasNext() }?.next()
    override fun getValueMatching(that: ElementModel<*>): ElementModel<Aux>? = linkedHashMap[newElementModelId(that)]

    /**
     * Returns a new value.
     * WARNING: the new value needs to be added to the set after the key fields are set in it.
     */
    fun getNewValue(): MutableElementModel<Aux> = newMutableValueModel(meta, this)

    fun addValue(value: MutableElementModel<Aux>) {
        linkedHashMap[newElementModelId(value)] = value
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

class MutablePassword1wayModel<Aux>(
    override val parent: MutableFieldModel<Aux>
) : MutableElementModel<Aux>(), Password1wayModel<Aux> {
    override var unhashed: String? = null
        internal set

    override var hashed: String? = null
        internal set

    override var hashVersion: Int = 0
        internal set

    override fun matches(that: ElementModel<*>): Boolean {
        if (that !is Password1wayModel<*>) return false
        if (this.unhashed != that.unhashed) return false
        if (this.hashed != that.hashed) return false
        if (this.hashVersion != that.hashVersion) return false
        return true
    }

    fun setUnhashed(unhashed: String): Boolean {
        val hasher = parent.meta?.aux?.password1wayHasher
        if (hasher != null) {
            this.unhashed = null
            this.hashed = hasher.hash(unhashed)
            this.hashVersion = 1
        } else {
            this.unhashed = unhashed
            this.hashed = null
            this.hashVersion = 0
        }
        return true
    }

    fun setHashed(hashed: String, hashVersion: Int): Boolean {
        if (hashVersion <= 0) return false
        this.unhashed = null
        this.hashed = hashed
        this.hashVersion = hashVersion
        return true
    }

    fun verify(thatUnhashed: String): Boolean {
        val hasher = parent.meta?.aux?.password1wayHasher ?: return false
        if (this.hashVersion != hasher.hashVersion) return false
        return this.hashed?.let { hasher.verify(thatUnhashed, it) } ?: false
    }
}

class MutablePassword2wayModel<Aux>(
    override val parent: MutableFieldModel<Aux>
) : MutableElementModel<Aux>(), Password2wayModel<Aux> {
    override var unencrypted: String? = null
        internal set

    override var encrypted: String? = null
        internal set

    override var cipherVersion: Int = 0
        internal set

    override fun matches(that: ElementModel<*>): Boolean {
        if (that !is Password2wayModel<*>) return false
        if (this.unencrypted != that.unencrypted) return false
        if (this.encrypted != that.encrypted) return false
        if (this.cipherVersion != that.cipherVersion) return false
        return true
    }

    fun setUnencrypted(unencrypted: String): Boolean {
        val cipher = parent.meta?.aux?.password2wayCipher
        if (cipher != null) {
            this.unencrypted = null
            this.encrypted = cipher.encrypt(unencrypted)
            this.cipherVersion = 1
        } else {
            this.unencrypted = unencrypted
            this.encrypted = null
            this.cipherVersion = 0
        }
        return true
    }

    fun setEncrypted(encrypted: String, cipherVersion: Int): Boolean {
        if (cipherVersion <= 0) return false
        this.unencrypted = null
        this.encrypted = encrypted
        this.cipherVersion = cipherVersion
        return true
    }

    fun decrypt(): String? {
        val cipher = parent.meta?.aux?.password2wayCipher ?: return unencrypted
        if (this.cipherVersion != cipher.cipherVersion) return null
        return this.encrypted?.let { cipher.decrypt(it) }
    }
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
        FieldType.BLOB -> {
            val blob = Base64.getDecoder().decode(value)
            setter(blob)
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
        null, // fieldMeta is null when constructing the meta-meta-model.
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
    // fieldMeta is null when constructing the meta-meta-model.
    val enumerationValue = if (fieldMeta == null) value else {
        val resolvedEnumeration = fieldMeta.aux?.enumerationMeta
            ?: throw IllegalStateException("Enumeration has not been resolved")
        val enumerationValues = getEnumerationValues(resolvedEnumeration)
        if (enumerationValues.contains(value)) value else return false
    }
    setter(enumerationValue)
    return true
}

class MissingKeysException(message: String) : Exception(message)
