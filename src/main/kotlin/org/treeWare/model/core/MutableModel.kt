package org.treeWare.model.core

import org.treeWare.metaModel.*
import java.math.BigDecimal
import java.util.*

abstract class MutableElementModel : ElementModel {
    override val meta: ElementModel? = null
    override var auxs: LinkedHashMap<String, Any>? = null

    override fun setAux(auxName: String, aux: Any) {
        if (auxs == null) auxs = LinkedHashMap()
        auxs?.also { it[auxName] = aux }
    }
}

class MutableMainModel(override val meta: MainModel?) :
    MutableElementModel(), MainModel {
    override val parent: ElementModel? = null

    override var type = "data"
        internal set

    override val auxTypes = mutableListOf<String>()

    override var root: MutableRootModel
        get() = _root ?: throw IllegalStateException("Root has not been set")
        internal set(value) {
            _root = value
        }
    private var _root: MutableRootModel? = null

    override fun matches(that: ElementModel): Boolean = false // Not yet needed, so not yet supported.

    fun getOrNewRoot(): MutableRootModel {
        if (_root == null) {
            val rootMeta = meta?.let { getRootMeta(it) }
            val resolvedRootMeta = getMetaModelResolved(rootMeta)?.compositionMeta
            _root = MutableRootModel(resolvedRootMeta, this)
        }
        return root
    }
}

abstract class MutableBaseEntityModel(
    override val meta: EntityModel?
) : MutableElementModel(), BaseEntityModel {
    override var fields = LinkedHashMap<String, MutableFieldModel>()
        internal set

    override fun matches(that: ElementModel): Boolean {
        if (that !is BaseEntityModel) return false
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
        val fieldsMeta = meta?.let { getFieldsMeta(it) } ?: return listOf()
        val keyFieldsMeta = filterKeyFields(fieldsMeta.values)
        val missingKeys = mutableListOf<String>()
        val keyFields = mutableListOf<FieldModel>()
        keyFieldsMeta.forEach { fieldMeta ->
            val keyFieldName = getMetaName(fieldMeta)
            val keyField = this.fields[keyFieldName]
            if (keyField == null) missingKeys.add(keyFieldName) else keyFields.add(keyField)
        }
        if (missingKeys.isNotEmpty()) throw MissingKeysException("Missing key fields: $missingKeys")
        return keyFields
    }

    override fun getKeyValues(): List<Any?> = getKeyFields().flatMap { field ->
        if (field.elementType == ModelElementType.SINGLE_FIELD) {
            val singleField = field as SingleFieldModel
            when (getFieldTypeMeta(singleField.meta)) {
                FieldType.COMPOSITION -> (singleField.value as BaseEntityModel).getKeyValues()
                else -> listOf((singleField.value as PrimitiveModel).value)
            }
        } else throw IllegalStateException("Unexpected element type ${field.elementType} for key ${field.getMetaAux()?.fullName}")
    }
}

class MutableRootModel(
    meta: EntityModel?,
    override val parent: MutableMainModel
) : MutableBaseEntityModel(meta), RootModel

class MutableEntityModel(
    meta: EntityModel?,
    override val parent: MutableFieldModel
) : MutableBaseEntityModel(meta), EntityModel

// Fields

abstract class MutableFieldModel(
    override val meta: EntityModel?,
    override val parent: MutableBaseEntityModel
) : MutableElementModel(), FieldModel

class MutableSingleFieldModel(
    meta: EntityModel?,
    parent: MutableBaseEntityModel
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

    fun addValue(value: MutableElementModel) {
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

    fun addValue(value: MutableElementModel) {
        linkedHashMap[newElementModelId(value)] = value
    }
}

// Values

abstract class MutableScalarValueModel(
    override val parent: MutableFieldModel
) : MutableElementModel() {
    open fun setNullValue(): Boolean = false
    open fun setValue(value: String): Boolean = false
    open fun setValue(value: BigDecimal): Boolean = false
    open fun setValue(value: Boolean): Boolean = false
}

class MutablePrimitiveModel(
    parent: MutableFieldModel
) : MutableScalarValueModel(parent), PrimitiveModel {
    override var value: Any? = null
        internal set

    override fun matches(that: ElementModel): Boolean {
        if (that !is PrimitiveModel) return false
        return this.value == that.value
    }

    override fun setNullValue(): Boolean {
        this.value = null
        return true
    }

    override fun setValue(value: String): Boolean = setValue(parent.meta, value) { this.value = it }
    override fun setValue(value: BigDecimal): Boolean = setValue(parent.meta, value) { this.value = it }
    override fun setValue(value: Boolean): Boolean = setValue(parent.meta, value) { this.value = it }

    fun copyValueFrom(that: PrimitiveModel) {
        this.value = that.value
    }
}

class MutableAliasModel(
    parent: MutableFieldModel
) : MutableScalarValueModel(parent), AliasModel {
    override var value: Any? = null
        internal set

    override fun matches(that: ElementModel): Boolean {
        if (that !is AliasModel) return false
        return this.value == that.value
    }

    override fun setNullValue(): Boolean {
        this.value = null
        return true
    }

    override fun setValue(value: String): Boolean = setValue(parent.meta, value) { this.value = it }
    override fun setValue(value: BigDecimal): Boolean = setValue(parent.meta, value) { this.value = it }
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
        val hasher = parent.getMetaAux()?.password1wayHasher
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
        val hasher = parent.getMetaAux()?.password1wayHasher ?: return false
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
        val cipher = parent.getMetaAux()?.password2wayCipher
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
        val cipher = parent.getMetaAux()?.password2wayCipher ?: return unencrypted
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
    parent: MutableFieldModel
) : MutableScalarValueModel(parent), EnumerationModel {
    override var meta: EntityModel? = null
    override var value: String? = null
        internal set

    override fun matches(that: ElementModel): Boolean {
        if (that !is EnumerationModel) return false
        return this.value == that.value
    }

    override fun setNullValue(): Boolean {
        this.value = null
        return true
    }

    override fun setValue(value: String): Boolean = setEnumerationValue(this, value) { this.value = it }

    fun copyValueFrom(that: EnumerationModel) {
        this.value = that.value
    }
}

class MutableAssociationModel(
    override val parent: MutableFieldModel
) : MutableElementModel(), AssociationModel {
    override var value: List<MutableEntityKeysModel> = listOf()
        internal set

    override fun matches(that: ElementModel): Boolean {
        if (that !is AssociationModel) return false
        if (this.value.size != that.value.size) return false
        return this.value.zip(that.value).all { (a, b) -> a.matches(b) }
    }

    fun setNullValue(): Boolean {
        this.value = listOf()
        return true
    }

    fun newValue(): List<MutableEntityKeysModel> {
        val keyEntityMetaList = parent.getMetaAux()?.associationMeta?.keyEntityMetaList ?: listOf()
        value = keyEntityMetaList.map { MutableEntityKeysModel(it, this) }
        return value
    }
}

// Sub-values

class MutableEntityKeysModel(
    meta: EntityModel?,
    override val parent: AssociationModel?
) : MutableBaseEntityModel(meta), EntityKeysModel

// Helpers

typealias ValueSetter = (Any) -> Unit

fun setValue(fieldMeta: EntityModel?, value: String, setter: ValueSetter): Boolean {
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

fun setValue(fieldMeta: EntityModel?, value: BigDecimal, setter: ValueSetter): Boolean {
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