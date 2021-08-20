package org.treeWare.model.core

import org.treeWare.schema.core.*

interface ElementModel<Aux> {
    val elementType: ModelElementType
    val schema: VisitableSchema
    val meta: ElementModel<Resolved>?
    val parent: ElementModel<Aux>?
    val aux: Aux?

    fun matches(that: ElementModel<*>): Boolean
}

/** The entire model (from the root entity). */
interface Model<Aux> : ElementModel<Aux> {
    override val elementType: ModelElementType
        get() = ModelElementType.MODEL

    override val schema: Schema
    override val meta: Model<Resolved>?

    val type: String
    val root: RootModel<Aux>
}

interface BaseEntityModel<Aux> : ElementModel<Aux> {
    override val meta: EntityModel<Resolved>?

    val fields: List<FieldModel<Aux>>

    fun getField(fieldName: String): FieldModel<Aux>?
}

interface RootModel<Aux> : BaseEntityModel<Aux> {
    override val elementType: ModelElementType
        get() = ModelElementType.ROOT

    override val schema: RootSchema
    override val parent: Model<Aux>
}

interface EntityModel<Aux> : BaseEntityModel<Aux> {
    override val elementType: ModelElementType
        get() = ModelElementType.ENTITY

    override val schema: EntitySchema
    override val parent: FieldModel<Aux>
}

// Fields

interface FieldModel<Aux> : ElementModel<Aux> {
    override val schema: FieldSchema
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

    override val schema: PrimitiveFieldSchema
    override val parent: FieldModel<Aux>
    val value: Any?
}

interface AliasModel<Aux> : ElementModel<Aux> {
    override val elementType: ModelElementType
        get() = ModelElementType.ALIAS

    override val schema: AliasFieldSchema
    override val parent: FieldModel<Aux>
    val value: Any?
}

interface EnumerationModel<Aux> : ElementModel<Aux> {
    override val elementType: ModelElementType
        get() = ModelElementType.ENUMERATION

    override val schema: EnumerationFieldSchema
    override val parent: FieldModel<Aux>
    val value: EnumerationValueSchema?
}

interface AssociationModel<Aux> : ElementModel<Aux> {
    override val elementType: ModelElementType
        get() = ModelElementType.ASSOCIATION

    override val schema: AssociationFieldSchema
    override val parent: FieldModel<Aux>
    val value: List<EntityKeysModel<Aux>>
}

// Sub-values

interface EntityKeysModel<Aux> : BaseEntityModel<Aux> {
    override val elementType: ModelElementType
        get() = ModelElementType.ENTITY_KEYS

    override val schema: EntitySchema
    override val parent: AssociationModel<Aux>
}
