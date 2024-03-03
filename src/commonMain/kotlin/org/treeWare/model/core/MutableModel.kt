package org.treeWare.model.core

import org.treeWare.metaModel.*
import org.treeWare.util.decodeBase64
import org.treeWare.util.hash
import org.treeWare.util.toBigDecimal
import org.treeWare.util.toBigInteger

abstract class MutableElementModel : ElementModel {
    override val meta: ElementModel? = null
    private var auxsInternal: LinkedHashMap<String, Any>? = null

    override val auxs: Map<String, Any>?
        get() = auxsInternal

    override fun setAux(auxName: String, aux: Any) {
        if (auxsInternal == null) auxsInternal = LinkedHashMap()
        auxsInternal?.also { it[auxName] = aux }
    }

    override fun unsetAux(auxName: String) {
        auxsInternal?.also { it.remove(auxName) }
    }

    open fun getNewValue(): MutableElementModel = throw UnsupportedOperationException()
}

open class MutableMainModel(
    override val mainMeta: MainModel?,
    rootFactory: FieldValueFactory = MutableEntityModel.fieldValueFactory
) :
    MutableSingleFieldModel(mainMeta?.let { getRootMeta(it) }, null, rootFactory), MainModel {
    override val parent: MutableBaseEntityModel? = null

    override var value: MutableElementModel? = null
    override var root: MutableEntityModel?
        get() = value as MutableEntityModel?
        set(value) {
            this.value = value
        }

    override fun matches(that: ElementModel): Boolean = false // Not yet needed, so not yet supported.

    fun getOrNewRoot(): MutableEntityModel = getNewValue() as MutableEntityModel
}

abstract class MutableBaseEntityModel(
    override val meta: EntityModel?
) : MutableElementModel(), BaseEntityModel {
    override var fields = LinkedHashMap<String, MutableFieldModel>()
        internal set

    override fun matches(that: ElementModel): Boolean {
        if (that !is BaseEntityModel) return false
        val (thisKeyFields, missingKeys) = getKeyFields()
        if (missingKeys.isNotEmpty()) {
            val entityMetaName = getMetaModelResolved(meta)?.fullName ?: getMetaName(meta)
            throw MissingKeysException("Missing key fields $missingKeys in instance of $entityMetaName")
        }
        return thisKeyFields.all { thisKeyField ->
            val thatKeyField = that.getField(getMetaName(thisKeyField.meta)) ?: return false
            thisKeyField.matches(thatKeyField)
        }
    }

    // NOTE: key values cannot be changed after an entity has been added to its parent set.
    override fun getMatchingHashCode(): Int = hash(getKeyValues())

    override fun getField(fieldName: String): MutableFieldModel? = fields[fieldName]

    fun getOrNewField(fieldName: String): MutableFieldModel {
        val existing = getField(fieldName)
        if (existing != null) return existing
        val fieldMeta = meta?.let { getFieldMeta(it, fieldName) }
            ?: throw IllegalStateException("fieldMeta is null when creating mutable field model")
        val valueFactory = getFieldValueFactory(fieldName, fieldMeta)
        val newField = when (getMultiplicityMeta(fieldMeta)) {
            Multiplicity.LIST -> MutableListFieldModel(fieldMeta, this, valueFactory)
            Multiplicity.SET -> MutableSetFieldModel(fieldMeta, this, valueFactory)
            else -> MutableSingleFieldModel(fieldMeta, this, valueFactory)
        }
        fields[fieldName] = newField
        return newField
    }

    private fun getFieldValueFactory(fieldName: String, fieldMeta: EntityModel): FieldValueFactory =
        when (getFieldTypeMeta(fieldMeta)) {
            FieldType.PASSWORD1WAY -> MutablePassword1wayModel.fieldValueFactory
            FieldType.PASSWORD2WAY -> MutablePassword2wayModel.fieldValueFactory
            FieldType.ENUMERATION -> MutableEnumerationModel.fieldValueFactory
            FieldType.ASSOCIATION -> MutableAssociationModel.fieldValueFactory
            FieldType.COMPOSITION -> getCompositionFactory(fieldName, fieldMeta)
            else -> MutablePrimitiveModel.fieldValueFactory
        }

    // To be overridden by generated subclasses to return a factory that returns an instance of a generated class.
    protected open fun getCompositionFactory(fieldName: String, fieldMeta: EntityModel): FieldValueFactory =
        MutableEntityModel.fieldValueFactory

    fun detachField(field: MutableFieldModel) {
        val fieldName = getMetaName(field.meta)
        val existingField = fields[fieldName] ?: return
        if (field != existingField) throw IllegalArgumentException("Field does not match existing field")
        fields.remove(fieldName)
    }

    fun detachAllFields() {
        fields.clear()
    }

    override fun getKeyFields(flatten: Boolean): Keys {
        val availableKeys = mutableListOf<SingleFieldModel>()
        val missingKeys = mutableListOf<String>()
        getKeyFields(flatten, null, availableKeys, missingKeys)
        return Keys(availableKeys, missingKeys)
    }

    private fun getKeyFields(
        flatten: Boolean,
        prefix: String?,
        availableKeys: MutableList<SingleFieldModel>,
        missingKeys: MutableList<String>
    ) {
        val keyFieldsMeta = meta?.let { getKeyFieldsMeta(it) } ?: return
        keyFieldsMeta.forEach { fieldMeta ->
            val keyFieldName = getMetaName(fieldMeta)
            val keyFieldFullName = if (prefix == null) keyFieldName else "$prefix/$keyFieldName"
            val keyField = this.fields[keyFieldName] as SingleFieldModel?
            if (keyField == null) missingKeys.add(keyFieldFullName)
            else {
                if (flatten && isCompositionField(keyField)) {
                    val childEntity = keyField.value as MutableBaseEntityModel?
                    if (childEntity == null) missingKeys.add(keyFieldFullName)
                    else childEntity.getKeyFields(true, keyFieldFullName, availableKeys, missingKeys)
                } else availableKeys.add(keyField)
            }
        }
    }

    override fun getKeyValues(): List<Any?> {
        val (availableKeys, missingKeys) = getKeyFields()
        if (missingKeys.isNotEmpty()) {
            val entityMetaName = getMetaModelResolved(meta)?.fullName ?: getMetaName(meta)
            throw MissingKeysException("Missing key fields $missingKeys in instance of $entityMetaName")
        }
        return availableKeys.flatMap { keyField ->
            when (getFieldTypeMeta(keyField.meta)) {
                FieldType.COMPOSITION -> (keyField.value as BaseEntityModel).getKeyValues()
                else -> listOf(keyField.value?.let { (it as PrimitiveModel).value })
            }
        }
    }
}

open class MutableEntityModel(
    meta: EntityModel?,
    override val parent: MutableFieldModel
) : MutableBaseEntityModel(meta), EntityModel {
    companion object {
        val fieldValueFactory: FieldValueFactory =
            { fieldMeta, parent -> MutableEntityModel(getMetaModelResolved(fieldMeta)?.compositionMeta, parent) }
    }
}

// Fields

abstract class MutableFieldModel(
    override val meta: EntityModel?,
    override val parent: MutableBaseEntityModel?
) : MutableElementModel(), FieldModel {
    fun detachFromParent() {
        parent?.detachField(this)
    }
}

open class MutableSingleFieldModel(
    meta: EntityModel?,
    parent: MutableBaseEntityModel?,
    val valueFactory: FieldValueFactory,
) : MutableFieldModel(meta, parent), SingleFieldModel {
    override var value: MutableElementModel? = null
        internal set

    override fun matches(that: ElementModel): Boolean {
        if (that !is SingleFieldModel) return false
        val thisValue = this.value
        val thatValue = that.value
        if (thisValue == null) return thatValue == null
        if (thatValue == null) return false
        return thisValue.matches(thatValue)
    }

    /** Returns existing value if not null, else creates, sets and returns a new value. */
    override fun getNewValue(): MutableElementModel {
        val existing = value
        if (existing != null) return existing
        val fieldMeta = meta ?: throw IllegalStateException("Field meta is null when creating mutable value model")
        val newValue = valueFactory(fieldMeta, this)
        value = newValue
        return newValue
    }

    fun setValue(value: MutableElementModel?) {
        this.value = value
    }
}

abstract class MutableCollectionFieldModel(
    meta: EntityModel?,
    parent: MutableBaseEntityModel
) : MutableFieldModel(meta, parent), CollectionFieldModel {
    abstract override val values: MutableCollection<MutableElementModel>

    abstract fun addValue(value: MutableElementModel)
}

class MutableListFieldModel(
    meta: EntityModel?,
    parent: MutableBaseEntityModel,
    val valueFactory: FieldValueFactory,
) : MutableCollectionFieldModel(meta, parent), ListFieldModel {
    override val values = mutableListOf<MutableElementModel>()

    override fun matches(that: ElementModel): Boolean {
        if (that !is ListFieldModel) return false
        if (this.values.size != that.values.size) return false
        this.values.forEachIndexed { index, thisValue ->
            val thatValue = that.values[index]
            if (!thisValue.matches(thatValue)) return false
        }
        return true
    }

    override fun firstValue(): ElementModel? = values.firstOrNull()
    override fun getValueMatching(that: ElementModel): ElementModel? = values.find { it.matches(that) }

    /** Adds a new value to the list and returns the new value. */
    override fun getNewValue(): MutableElementModel {
        val fieldMeta = meta ?: throw IllegalStateException("Field meta is null when creating mutable value model")
        val newValue = valueFactory(fieldMeta, this)
        addValue(newValue)
        return newValue
    }

    override fun addValue(value: MutableElementModel) {
        values.add(value)
    }

    fun clear() {
        values.clear()
    }
}

class MutableSetFieldModel(
    meta: EntityModel?,
    parent: MutableBaseEntityModel,
    val valueFactory: FieldValueFactory,
) : MutableCollectionFieldModel(meta, parent), SetFieldModel {
    private val linkedHashMap = LinkedHashMap<ElementModelId, MutableElementModel>()
    override val values get() = linkedHashMap.values

    override fun matches(that: ElementModel): Boolean = false // Not yet needed, so not yet supported.
    override fun firstValue(): ElementModel? = values.iterator().takeIf { it.hasNext() }?.next()
    override fun getValueMatching(that: ElementModel): ElementModel? = linkedHashMap[newElementModelId(that)]

    /**
     * Returns a new value.
     * WARNING: the new value needs to be added to the set after the key fields are set in it.
     */
    override fun getNewValue(): MutableElementModel {
        val fieldMeta = meta ?: throw IllegalStateException("Field meta is null when creating mutable value model")
        return valueFactory(fieldMeta, this)
    }

    override fun addValue(value: MutableElementModel) {
        linkedHashMap[newElementModelId(value)] = value
    }
}

// Values

abstract class MutableScalarValueModel(
    override val parent: MutableFieldModel
) : MutableElementModel() {
    open fun setValue(value: String): Boolean = false
    open fun setValue(value: Boolean): Boolean = false
}

class MutablePrimitiveModel(
    parent: MutableFieldModel,
    override var value: Any
) : MutableScalarValueModel(parent), PrimitiveModel {
    companion object {
        val fieldValueFactory: FieldValueFactory = { _, parent -> MutablePrimitiveModel(parent, 0) }
    }

    override fun matches(that: ElementModel): Boolean {
        if (that !is PrimitiveModel) return false
        if (getFieldTypeMeta(this.parent.meta) == FieldType.BLOB)
            return (this.value as ByteArray).contentEquals(that.value as ByteArray)
        return this.value == that.value
    }

    override fun setValue(value: String): Boolean = setValue(parent.meta, value) { this.value = it }
    override fun setValue(value: Boolean): Boolean = setValue(parent.meta, value) { this.value = it }

    fun copyValueFrom(that: PrimitiveModel) {
        this.value = that.value
    }
}

class MutableAliasModel(
    parent: MutableFieldModel,
    override var value: Any
) : MutableScalarValueModel(parent), AliasModel {
    override fun matches(that: ElementModel): Boolean {
        if (that !is AliasModel) return false
        return this.value == that.value
    }

    override fun setValue(value: String): Boolean = setValue(parent.meta, value) { this.value = it }
    override fun setValue(value: Boolean): Boolean = setValue(parent.meta, value) { this.value = it }

    fun copyValueFrom(that: AliasModel) {
        this.value = that.value
    }
}

class MutablePassword1wayModel(
    override val parent: MutableFieldModel
) : MutableElementModel(), Password1wayModel {
    companion object {
        val fieldValueFactory: FieldValueFactory = { _, parent -> MutablePassword1wayModel(parent) }
    }

    override var unhashed: String? = null
        internal set

    override var hashed: String? = null
        internal set

    override var hashVersion: Int = 0
        internal set

    override fun matches(that: ElementModel): Boolean {
        if (that !is Password1wayModel) return false
        if (this.unhashed != that.unhashed) return false
        if (this.hashed != that.hashed) return false
        return this.hashVersion == that.hashVersion
    }

    fun setUnhashed(unhashed: String): Boolean {
        val hasher = parent.getMetaResolved()?.password1wayHasher
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
        val hasher = parent.getMetaResolved()?.password1wayHasher ?: return false
        if (this.hashVersion != hasher.hashVersion) return false
        return this.hashed?.let { hasher.verify(thatUnhashed, it) } ?: false
    }

    fun copyValueFrom(that: Password1wayModel) {
        this.unhashed = that.unhashed
        this.hashed = that.hashed
        this.hashVersion = that.hashVersion
    }
}

class MutablePassword2wayModel(
    override val parent: MutableFieldModel
) : MutableElementModel(), Password2wayModel {
    companion object {
        val fieldValueFactory: FieldValueFactory = { _, parent -> MutablePassword2wayModel(parent) }
    }

    override var unencrypted: String? = null
        internal set

    override var encrypted: String? = null
        internal set

    override var cipherVersion: Int = 0
        internal set

    override fun matches(that: ElementModel): Boolean {
        if (that !is Password2wayModel) return false
        if (this.unencrypted != that.unencrypted) return false
        if (this.encrypted != that.encrypted) return false
        return this.cipherVersion == that.cipherVersion
    }

    fun setUnencrypted(unencrypted: String): Boolean {
        val cipher = parent.getMetaResolved()?.password2wayCipher
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
        val cipher = parent.getMetaResolved()?.password2wayCipher ?: return unencrypted
        if (this.cipherVersion != cipher.cipherVersion) return null
        return this.encrypted?.let { cipher.decrypt(it) }
    }

    fun copyValueFrom(that: Password2wayModel) {
        this.unencrypted = that.unencrypted
        this.encrypted = that.encrypted
        this.cipherVersion = that.cipherVersion
    }
}

class MutableEnumerationModel(
    parent: MutableFieldModel,
    value: String
) : MutableScalarValueModel(parent), EnumerationModel {
    companion object {
        val fieldValueFactory: FieldValueFactory = { _, parent -> MutableEnumerationModel(parent, "") }
    }

    override var meta: EntityModel? = null

    override var value: String = ""
        private set
    override var number: UInt = 0U
        private set

    init {
        setValue(value)
    }

    override fun matches(that: ElementModel): Boolean {
        if (that !is EnumerationModel) return false
        return this.value == that.value
    }

    override fun setValue(value: String): Boolean =
        setEnumerationValue(parent.meta, value) { stringValue, number, meta ->
            this.value = stringValue
            this.number = number
            this.meta = meta
        }

    fun setNumber(number: UInt): Boolean {
        val fieldMeta: EntityModel = requireNotNull(parent.meta) { "Enumeration meta is missing " }
        val resolvedEnumeration = getMetaModelResolved(fieldMeta)?.enumerationMeta
            ?: throw IllegalStateException("Enumeration has not been resolved")
        val enumerationValueMeta = getEnumerationValueMeta(resolvedEnumeration, number)
        this.meta = enumerationValueMeta
        if (enumerationValueMeta == null) return false
        this.value = getMetaName(enumerationValueMeta)
        this.number = number
        return true
    }

    fun copyValueFrom(that: EnumerationModel) {
        this.meta = that.meta
        this.value = that.value
        this.number = that.number
    }
}

class MutableAssociationModel(
    override val parent: MutableFieldModel,
    valueMeta: EntityModel?
) : MutableElementModel(), AssociationModel {
    companion object {
        val fieldValueFactory: FieldValueFactory = { fieldMeta, parent ->
            MutableAssociationModel(
                parent,
                getMetaModelResolved(fieldMeta)?.associationMeta?.rootEntityMeta
            )
        }
    }

    // NOTE: entities need to have a non-null parent that is a field. So this association cannot be used as the parent
    // of the `value` entity below. Instead, the parent field of this association instance is used as the parent. This
    // actually works out well since the `value` entity is equivalent to a direct child of the field (the only reason
    // it is not that way in the code is because the direct child needs to have ASSOCIATION as the `elementType`).
    override val value: MutableEntityModel = MutableEntityModel(valueMeta, parent)

    override fun getNewValue(): MutableElementModel = value

    override fun matches(that: ElementModel): Boolean {
        if (that !is AssociationModel) return false
        throw UnsupportedOperationException("Use difference() operator")
    }

    // NOTE: instead of wrapping the association value to store aux data, the aux data is stored in the root entity of
    // the association value. This reduces the amount of nesting in the encoding. The aux getter and setter are
    // overridden to make this possible.
    override val auxs: Map<String, Any>? get() = value.auxs
    override fun setAux(auxName: String, aux: Any) = value.setAux(auxName, aux)
    override fun unsetAux(auxName: String) = value.unsetAux(auxName)
}

// region Helpers

typealias FieldValueFactory = (fieldMeta: EntityModel, parent: MutableFieldModel) -> MutableElementModel

typealias ValueSetter = (Any) -> Unit

fun setValue(fieldMeta: EntityModel?, value: String, setter: ValueSetter): Boolean {
    // Integers in JavaScript are limited to 53 bits. So 64-bit values ("long", "timestamp")
    // are encoded as strings.
    return when (getFieldTypeMeta(fieldMeta)) {
        FieldType.BOOLEAN ->
            try {
                setter(value.lowercase().toBooleanStrict())
                true
            } catch (e: Exception) {
                false
            }
        FieldType.UINT8 ->
            try {
                setter(value.toUByte())
                true
            } catch (e: NumberFormatException) {
                false
            }
        FieldType.UINT16 ->
            try {
                setter(value.toUShort())
                true
            } catch (e: NumberFormatException) {
                false
            }
        FieldType.UINT32 ->
            try {
                setter(value.toUInt())
                true
            } catch (e: NumberFormatException) {
                false
            }
        FieldType.UINT64 ->
            try {
                setter(value.toULong())
                true
            } catch (e: NumberFormatException) {
                false
            }
        FieldType.INT8 ->
            try {
                setter(value.toByte())
                true
            } catch (e: NumberFormatException) {
                false
            }
        FieldType.INT16 ->
            try {
                setter(value.toShort())
                true
            } catch (e: NumberFormatException) {
                false
            }
        FieldType.INT32 ->
            try {
                setter(value.toInt())
                true
            } catch (e: NumberFormatException) {
                false
            }
        FieldType.INT64 ->
            try {
                setter(value.toLong())
                true
            } catch (e: NumberFormatException) {
                false
            }
        FieldType.FLOAT ->
            try {
                setter(value.toFloat())
                true
            } catch (e: NumberFormatException) {
                false
            }
        FieldType.DOUBLE ->
            try {
                setter(value.toDouble())
                true
            } catch (e: NumberFormatException) {
                false
            }
        FieldType.BIG_INTEGER ->
            try {
                setter(toBigInteger(value))
                true
            } catch (e: NumberFormatException) {
                false
            }
        FieldType.BIG_DECIMAL ->
            try {
                setter(toBigDecimal(value))
                true
            } catch (e: NumberFormatException) {
                false
            }
        FieldType.TIMESTAMP ->
            try {
                setter(value.toLong())
                true
            } catch (e: NumberFormatException) {
                false
            }
        FieldType.STRING,
        FieldType.UUID -> {
            setter(value)
            true
        }
        FieldType.BLOB -> {
            val blob = decodeBase64(value)
            setter(blob)
            true
        }
        else -> false
    }
}

fun setValue(fieldMeta: EntityModel?, value: Boolean, setter: ValueSetter): Boolean {
    return when (getFieldTypeMeta(fieldMeta)) {
        null, // fieldMeta is null when constructing the meta-meta-model.
        FieldType.BOOLEAN -> {
            setter(value)
            true
        }
        else -> false
    }
}

typealias EnumerationValueSetter = (String, UInt, EntityModel?) -> Unit

fun setEnumerationValue(fieldMeta: EntityModel?, value: String, setter: EnumerationValueSetter): Boolean =
    // fieldMeta is null when constructing the meta-meta-model.
    if (fieldMeta == null) {
        setter(value, 0U, null)
        true
    } else {
        val resolvedEnumeration = getMetaModelResolved(fieldMeta)?.enumerationMeta
            ?: throw IllegalStateException("Enumeration has not been resolved")
        val enumerationValueMeta = getEnumerationValueMeta(resolvedEnumeration, value)
        if (enumerationValueMeta != null) {
            val number = getMetaNumber(enumerationValueMeta) ?: 0U
            setter(value, number, enumerationValueMeta)
            true
        } else false
    }

class MissingKeysException(message: String) : Exception(message)

// endregion