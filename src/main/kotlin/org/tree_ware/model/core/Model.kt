package org.tree_ware.model.core

import org.tree_ware.schema.core.*

interface VisitableModel {
    /**
     * Accepts a visitor and traverses the model with it (Visitor Pattern).
     *
     * @returns `true` to proceed with model traversal, `false` to stop model traversal.
     */
    fun accept(visitor: ModelVisitor): Boolean
}

interface ElementModel : VisitableModel {
    val schema: ElementSchema
    val parent: ElementModel?
}

enum class ModelType {
    data
    // TODO(deepak-nulu): error
    // TODO(deepak-nulu): filter
    // TODO(deepak-nulu): delta
}

/** The entire model (from the root entity). */
interface Model : ElementModel {
    val type: ModelType
    val root: RootModel

    override val schema: Schema
}

interface BaseEntityModel : ElementModel {
    val fields: List<FieldModel>
}

interface RootModel : BaseEntityModel {
    override val schema: RootSchema
    override val parent: Model
}

interface EntityModel : BaseEntityModel {
    override val schema: EntitySchema
    override val parent: FieldModel
}

interface FieldModel : ElementModel {
    override val schema: FieldSchema
    override val parent: BaseEntityModel
}

// Scalar fields

interface ScalarFieldModel : FieldModel

interface PrimitiveFieldModel : ScalarFieldModel {
    override val schema: PrimitiveFieldSchema
    val value: Any
}

interface AliasFieldModel : ScalarFieldModel {
    override val schema: AliasFieldSchema
    val value: Any
}

interface EnumerationFieldModel : ScalarFieldModel {
    override val schema: EnumerationFieldSchema
    val value: EnumerationValueSchema
}

interface AssociationFieldModel : ScalarFieldModel {
    override val schema: AssociationFieldSchema
    val value: AssociationValueModel
}

interface CompositionFieldModel : ScalarFieldModel {
    override val schema: CompositionFieldSchema
    val value: EntityModel
}

// List fields

interface ListFieldModel : FieldModel

interface PrimitiveListFieldModel : ListFieldModel {
    override val schema: PrimitiveFieldSchema
    val value: List<Any>
}

interface AliasListFieldModel : ListFieldModel {
    override val schema: AliasFieldSchema
    val value: List<Any>
}

interface EnumerationListFieldModel : ListFieldModel {
    override val schema: EnumerationFieldSchema
    val value: List<EnumerationValueSchema>
}

interface AssociationListFieldModel : ListFieldModel {
    override val schema: AssociationFieldSchema
    val value: List<AssociationValueModel>
}

interface CompositionListFieldModel : ListFieldModel {
    override val schema: CompositionFieldSchema
    val value: List<EntityModel>
}

// Field values

interface AssociationValueModel : ElementModel {
    val pathKeys: List<EntityKeysModel>
}

interface EntityKeysModel : BaseEntityModel {
    override val schema: EntitySchema
}
