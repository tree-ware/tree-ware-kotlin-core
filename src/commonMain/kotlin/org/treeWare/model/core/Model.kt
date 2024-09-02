package org.treeWare.model.core

interface ElementModel {
    val elementType: ModelElementType
    val meta: ElementModel?
    val parent: ElementModel?
    val auxs: Map<String, Any>?

    fun setAux(auxName: String, aux: Any) // TODO(#77): remove when #77 is implemented
    fun unsetAux(auxName: String)
    fun matches(that: ElementModel): Boolean
    fun isEmpty(): Boolean = false
}

inline fun <reified Aux> ElementModel.getAux(auxName: String): Aux? = this.auxs?.let { it[auxName] as? Aux? }

fun ElementModel.getMetaResolved(): Resolved? = this.meta?.getAux<Resolved>(RESOLVED_AUX)

data class Keys(val available: List<SingleFieldModel>, val missing: List<String>)

val EmptyKeys = Keys(emptyList(), emptyList())

interface BaseEntityModel : ElementModel {
    override val meta: EntityModel?

    val fields: Map<String, FieldModel>

    override fun isEmpty(): Boolean = fields.isEmpty()

    fun getField(fieldName: String): FieldModel?

    fun hasOnlyKeyFields(): Boolean = fields.all { (_, field) -> isKeyField(field) }
    fun getKeyFields(flatten: Boolean = false): Keys

    /** Returns key values. Keys are in sorted meta-model order and composite keys are flattened.
     */
    fun getKeyValues(): List<Any?>
}

interface EntityModel : BaseEntityModel {
    override val elementType: ModelElementType
        get() = ModelElementType.ENTITY

    override val parent: FieldModel?
}

// Fields

interface FieldModel : ElementModel {
    override val meta: EntityModel?
    override val parent: BaseEntityModel?
}

interface SingleFieldModel : FieldModel {
    override val elementType: ModelElementType
        get() = ModelElementType.SINGLE_FIELD

    val value: ElementModel?

    override fun isEmpty(): Boolean = value?.isEmpty() ?: (value == null)
}

interface CollectionFieldModel : FieldModel {
    val values: Collection<ElementModel>

    override fun isEmpty(): Boolean = values.isEmpty()
    fun firstValue(): ElementModel?
    fun getValueMatching(that: ElementModel): ElementModel?
}

interface SetFieldModel : CollectionFieldModel {
    override val elementType: ModelElementType
        get() = ModelElementType.SET_FIELD
}

// Values

interface PrimitiveModel : ElementModel {
    override val elementType: ModelElementType
        get() = ModelElementType.PRIMITIVE

    override val parent: FieldModel
    val value: Any
}

interface AliasModel : ElementModel {
    override val elementType: ModelElementType
        get() = ModelElementType.ALIAS

    override val parent: FieldModel
    val value: Any
}

interface Password1wayModel : ElementModel {
    override val elementType: ModelElementType
        get() = ModelElementType.PASSWORD1WAY

    override val parent: FieldModel

    val unhashed: String?
    val hashed: String?
    val hashVersion: Int
}

interface Password2wayModel : ElementModel {
    override val elementType: ModelElementType
        get() = ModelElementType.PASSWORD2WAY

    override val parent: FieldModel

    val unencrypted: String?
    val encrypted: String?
    val cipherVersion: Int
}

interface EnumerationModel : ElementModel {
    override val elementType: ModelElementType
        get() = ModelElementType.ENUMERATION

    override val meta: EntityModel?
    override val parent: FieldModel
    val value: String
    val number: UInt
}

interface AssociationModel : ElementModel {
    override val elementType: ModelElementType
        get() = ModelElementType.ASSOCIATION

    override val parent: FieldModel
    val value: EntityModel
}