package org.treeWare.model.core

interface ElementModel {
    val elementType: ModelElementType
    val meta: ElementModel?
    val parent: ElementModel?
    val auxs: Map<String, Any>?

    fun setAux(auxName: String, aux: Any) // TODO(#77): remove when #77 is implemented
    fun matches(that: ElementModel): Boolean
}

inline fun <reified Aux> ElementModel.getAux(auxName: String): Aux? = this.auxs?.let { it[auxName] as? Aux? }

fun ElementModel.getMetaResolved(): Resolved? = this.meta?.getAux<Resolved>(RESOLVED_AUX)

/** The entire model (from the root entity). */
interface MainModel : SingleFieldModel {
    override val elementType: ModelElementType
        get() = ModelElementType.MAIN

    val mainMeta: MainModel?
    val root: EntityModel // Same as SingleFieldModel.value but different type
}

data class Keys(val available: List<SingleFieldModel>, val missing: List<String>)

val EmptyKeys = Keys(emptyList(), emptyList())

interface BaseEntityModel : ElementModel {
    override val meta: EntityModel?

    val fields: Map<String, FieldModel>

    fun isEmpty(): Boolean = fields.isEmpty()

    fun getField(fieldName: String): FieldModel?
    fun getMatchingHashCode(): Int

    fun hasOnlyKeyFields(): Boolean = fields.all { (_, field) -> isKeyField(field) }
    fun getKeyFields(flatten: Boolean = false): Keys
    fun getKeyValues(): List<Any?>
}

interface EntityModel : BaseEntityModel {
    override val elementType: ModelElementType
        get() = ModelElementType.ENTITY

    override val parent: FieldModel
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
}

interface CollectionFieldModel : FieldModel {
    val values: Collection<ElementModel>

    fun isEmpty(): Boolean = values.isEmpty()
    fun firstValue(): ElementModel?
    fun getValueMatching(that: ElementModel): ElementModel?
}

interface ListFieldModel : CollectionFieldModel {
    override val elementType: ModelElementType
        get() = ModelElementType.LIST_FIELD

    override val values: List<ElementModel>
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