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
}

class MutableMainModel(override val mainMeta: MainModel?) :
    MutableSingleFieldModel(mainMeta?.let { getRootMeta(it) }, null), MainModel {
    override val parent: MutableBaseEntityModel? = null

    override var value: MutableElementModel? = null
    override var root: MutableEntityModel
        get() = value as? MutableEntityModel ?: throw IllegalStateException("Root has not been set")
        set(value) {
            this.value = value
        }

    override fun matches(that: ElementModel): Boolean = false // Not yet needed, so not yet supported.

    fun getOrNewRoot(): MutableEntityModel = getOrNewValue() as MutableEntityModel
}

abstract class MutableBaseEntityModel(
    override val meta: EntityModel?
) : MutableElementModel(), BaseEntityModel {
    override var fields = LinkedHashMap<String, MutableFieldModel>()
        internal set

    override fun matches(that: ElementModel): Boolean {
        if (that !is BaseEntityModel) return false
        val thisKeyFields = getKeyFields()
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
        val newField = when (getMultiplicityMeta(fieldMeta)) {
            Multiplicity.LIST -> MutableListFieldModel(fieldMeta, this)
            Multiplicity.SET -> MutableSetFieldModel(fieldMeta, this)
            else -> MutableSingleFieldModel(fieldMeta, this)
        }
        fields[fieldName] = newField
        return newField
    }

    override fun getKeyFields(): List<FieldModel> {
        val fieldsMeta = meta?.let { getFieldsMeta(it) } ?: return emptyList()
        val keyFieldsMeta = filterKeyFields(fieldsMeta.values)
        val missingKeys = mutableListOf<String>()
        val keyFields = mutableListOf<FieldModel>()
        keyFieldsMeta.forEach { fieldMeta ->
            val keyFieldName = getMetaName(fieldMeta)
            val keyField = this.fields[keyFieldName]
            if (keyField == null) missingKeys.add(keyFieldName) else keyFields.add(keyField)
        }
        if (missingKeys.isNotEmpty()) {
            val entityMetaName = getMetaModelResolved(this.meta)?.fullName ?: getMetaName(this.meta)
            throw MissingKeysException(
                "Missing key fields $missingKeys in instance of $entityMetaName"
            )
        }
        return keyFields
    }

    override fun getKeyValues(): List<Any?> = getKeyFields().flatMap { field ->
        if (field.elementType == ModelElementType.SINGLE_FIELD) {
            val singleField = field as SingleFieldModel
            when (getFieldTypeMeta(singleField.meta)) {
                FieldType.COMPOSITION -> (singleField.value as BaseEntityModel).getKeyValues()
                else -> listOf((singleField.value as PrimitiveModel).value)
            }
        } else throw IllegalStateException("Unexpected element type ${field.elementType} for key ${field.getMetaResolved()?.fullName}")
    }
}

class MutableEntityModel(
    meta: EntityModel?,
    override val parent: MutableFieldModel
) : MutableBaseEntityModel(meta), EntityModel

// Fields

abstract class MutableFieldModel(
    override val meta: EntityModel?,
    override val parent: MutableBaseEntityModel?
) : MutableElementModel(), FieldModel

open class MutableSingleFieldModel(
    meta: EntityModel?,
    parent: MutableBaseEntityModel?
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

    fun getOrNewValue(): MutableElementModel {
        val existing = value
        if (existing != null) return existing
        val newValue = newMutableValueModel(meta, this)
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
    parent: MutableBaseEntityModel
) : MutableCollectionFieldModel(meta, parent), ListFieldModel {
    override val values = mutableListOf<MutableElementModel>()

    override fun matches(that: ElementModel): Boolean = false // Not yet needed, so not yet supported.
    override fun firstValue(): ElementModel? = values.firstOrNull()
    override fun getValueMatching(that: ElementModel): ElementModel? = values.find { it.matches(that) }

    /** Adds a new value to the list and returns the new value. */
    fun getNewValue(): MutableElementModel {
        val newValue = newMutableValueModel(meta, this)
        addValue(newValue)
        return newValue
    }

    override fun addValue(value: MutableElementModel) {
        values.add(value)
    }
}

class MutableSetFieldModel(
    meta: EntityModel?,
    parent: MutableBaseEntityModel
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
    fun getNewValue(): MutableElementModel = newMutableValueModel(meta, this)

    override fun addValue(value: MutableElementModel) {
        linkedHashMap[newElementModelId(value)] = value
    }
}

// Values

abstract class MutableScalarValueModel(
    override val parent: MutableFieldModel
) : MutableElementModel() {
    open fun setNullValue(): Boolean = false
    open fun setValue(value: String): Boolean = false
    open fun setValue(value: Boolean): Boolean = false
}

class MutablePrimitiveModel(
    parent: MutableFieldModel,
    override var value: Any
) : MutableScalarValueModel(parent), PrimitiveModel {
    override fun matches(that: ElementModel): Boolean {
        if (that !is PrimitiveModel) return false
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
        if (this.hashVersion != that.hashVersion) return false
        return true
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
        if (this.cipherVersion != that.cipherVersion) return false
        return true
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
    override var value: String
) : MutableScalarValueModel(parent), EnumerationModel {
    override var meta: EntityModel? = null

    override fun matches(that: ElementModel): Boolean {
        if (that !is EnumerationModel) return false
        return this.value == that.value
    }

    override fun setValue(value: String): Boolean = setEnumerationValue(this, value) { this.value = it }

    fun copyValueFrom(that: EnumerationModel) {
        this.value = that.value
    }
}

class MutableAssociationModel(
    override val parent: MutableFieldModel,
    valueMeta: EntityModel?
) : MutableElementModel(), AssociationModel {
    // NOTE: entities need to have a non-null parent that is a field. So this association cannot be used as the parent
    // of the `value` entity below. Instead, the parent field of this association instance is used as the parent. This
    // actually works out well since the `value` entity is equivalent to a direct child of the field (the only reason
    // it is not that way in the code is because the direct child needs to have ASSOCIATION as the `elementType`).
    override val value: MutableEntityModel = MutableEntityModel(valueMeta, parent)

    override fun matches(that: ElementModel): Boolean {
        if (that !is AssociationModel) return false
        TODO("Requires an equals() model operator")
    }

    // NOTE: instead of wrapping the association value to store aux data, the aux data is store in the root entity of
    // the association value. This reduces the amount of nesting in the encoding. The aux getter and setter are
    // overridden to make this possible.
    override val auxs: Map<String, Any>? get() = value.auxs
    override fun setAux(auxName: String, aux: Any) = value.setAux(auxName, aux)
}

// Helpers

typealias ValueSetter = (Any) -> Unit

fun setValue(fieldMeta: EntityModel?, value: String, setter: ValueSetter): Boolean {
    // Integers in JavaScript are limited to 53 bits. So 64-bit values ("long", "timestamp")
    // are encoded as strings.
    return when (getFieldTypeMeta(fieldMeta)) {
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

typealias StringSetter = (String) -> Unit

fun setEnumerationValue(
    enumerationModel: MutableEnumerationModel,
    value: String,
    setter: StringSetter
): Boolean {
    val fieldMeta: EntityModel? = enumerationModel.parent.meta
    // fieldMeta is null when constructing the meta-meta-model.
    val enumerationValue = if (fieldMeta == null) value else {
        val resolvedEnumeration = getMetaModelResolved(fieldMeta)?.enumerationMeta
            ?: throw IllegalStateException("Enumeration has not been resolved")
        val enumerationValueMeta = getEnumerationValueMeta(resolvedEnumeration, value)
        enumerationModel.meta = enumerationValueMeta
        if (enumerationValueMeta != null) value else return false
    }
    setter(enumerationValue)
    return true
}

class MissingKeysException(message: String) : Exception(message)