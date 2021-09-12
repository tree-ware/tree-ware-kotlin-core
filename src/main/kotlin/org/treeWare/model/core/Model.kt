package org.treeWare.model.core

interface ElementModel<Aux> {
    val elementType: ModelElementType
    val meta: ElementModel<Resolved>?
    val parent: ElementModel<Aux>?
    val aux: Aux?

    fun matches(that: ElementModel<*>): Boolean
}

/** The entire model (from the root entity). */
interface MainModel<Aux> : ElementModel<Aux> {
    override val elementType: ModelElementType
        get() = ModelElementType.MAIN

    override val meta: MainModel<Resolved>?

    val type: String
    val root: RootModel<Aux>
}

interface BaseEntityModel<Aux> : ElementModel<Aux> {
    override val meta: EntityModel<Resolved>?

    val fields: Map<String, FieldModel<Aux>>

    fun getField(fieldName: String): FieldModel<Aux>?
}

interface RootModel<Aux> : BaseEntityModel<Aux> {
    override val elementType: ModelElementType
        get() = ModelElementType.ROOT

    override val parent: MainModel<Aux>
}

interface EntityModel<Aux> : BaseEntityModel<Aux> {
    override val elementType: ModelElementType
        get() = ModelElementType.ENTITY

    override val parent: FieldModel<Aux>
}

// Fields

interface FieldModel<Aux> : ElementModel<Aux> {
    override val meta: EntityModel<Resolved>?
    override val parent: BaseEntityModel<Aux>
}

interface SingleFieldModel<Aux> : FieldModel<Aux> {
    override val elementType: ModelElementType
        get() = ModelElementType.SINGLE_FIELD

    val value: ElementModel<Aux>?
}

interface ListFieldModel<Aux> : FieldModel<Aux> {
    override val elementType: ModelElementType
        get() = ModelElementType.LIST_FIELD

    val values: List<ElementModel<Aux>>

    fun firstValue(): ElementModel<Aux>?
    fun getValue(index: Int): ElementModel<Aux>?
    fun getValueMatching(that: ElementModel<*>): ElementModel<Aux>?
}

// Values

interface PrimitiveModel<Aux> : ElementModel<Aux> {
    override val elementType: ModelElementType
        get() = ModelElementType.PRIMITIVE

    override val parent: FieldModel<Aux>
    val value: Any?
}

interface AliasModel<Aux> : ElementModel<Aux> {
    override val elementType: ModelElementType
        get() = ModelElementType.ALIAS

    override val parent: FieldModel<Aux>
    val value: Any?
}

interface Password1wayModel<Aux> : ElementModel<Aux> {
    override val elementType: ModelElementType
        get() = ModelElementType.PASSWORD1WAY

    override val parent: FieldModel<Aux>

    val unhashed: String?
    val hashed: String?
    val hashVersion: Int
}

interface Password2wayModel<Aux> : ElementModel<Aux> {
    override val elementType: ModelElementType
        get() = ModelElementType.PASSWORD2WAY

    override val parent: FieldModel<Aux>

    val unencrypted: String?
    val encrypted: String?
    val encryptionVersion: Int
}

interface EnumerationModel<Aux> : ElementModel<Aux> {
    override val elementType: ModelElementType
        get() = ModelElementType.ENUMERATION

    override val parent: FieldModel<Aux>
    val value: String?
}

interface AssociationModel<Aux> : ElementModel<Aux> {
    override val elementType: ModelElementType
        get() = ModelElementType.ASSOCIATION

    override val parent: FieldModel<Aux>
    val value: List<EntityKeysModel<Aux>>
}

// Sub-values

interface EntityKeysModel<Aux> : BaseEntityModel<Aux> {
    override val elementType: ModelElementType
        get() = ModelElementType.ENTITY_KEYS

    override val parent: AssociationModel<Aux>
}
