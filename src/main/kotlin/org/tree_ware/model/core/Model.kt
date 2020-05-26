package org.tree_ware.model.core

import org.tree_ware.schema.core.*

interface VisitableModel<Aux> {
    /**
     * Traverses the model element and visits it and its sub-elements (Visitor Pattern).
     * Traversal continues or aborts (partially or fully) based on the value returned by the visitor.
     */
    fun traverse(visitor: ModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction

    /**
     * Visits the model element without traversing its sub-elements.
     * Leave methods are NOT called.
     * Returns what the visitor returns.
     */
    fun <Return> dispatch(visitor: ModelVisitor<Aux, Return>): Return
}

interface ElementModel<Aux> : VisitableModel<Aux> {
    val schema: ElementSchema
    val parent: ElementModel<Aux>?
    val aux: Aux?
}

enum class ModelType {
    data
    // TODO(deepak-nulu): error
    // TODO(deepak-nulu): filter
    // TODO(deepak-nulu): delta
}

/** The entire model (from the root entity). */
interface Model<Aux> : ElementModel<Aux> {
    val type: ModelType
    val root: RootModel<Aux>

    override val schema: Schema
}

interface BaseEntityModel<Aux> : ElementModel<Aux> {
    val fields: List<FieldModel<Aux>>
}

interface RootModel<Aux> : BaseEntityModel<Aux> {
    override val schema: RootSchema
    override val parent: Model<Aux>
}

interface EntityModel<Aux> : BaseEntityModel<Aux> {
    override val schema: EntitySchema
    override val parent: FieldModel<Aux>
}

interface FieldModel<Aux> : ElementModel<Aux> {
    override val schema: FieldSchema
    override val parent: BaseEntityModel<Aux>
}

// Scalar fields

interface ScalarFieldModel<Aux> : FieldModel<Aux>

interface PrimitiveFieldModel<Aux> : ScalarFieldModel<Aux> {
    override val schema: PrimitiveFieldSchema
    val value: Any?
}

interface AliasFieldModel<Aux> : ScalarFieldModel<Aux> {
    override val schema: AliasFieldSchema
    val value: Any?
}

interface EnumerationFieldModel<Aux> : ScalarFieldModel<Aux> {
    override val schema: EnumerationFieldSchema
    val value: EnumerationValueSchema?
}

interface AssociationFieldModel<Aux> : ScalarFieldModel<Aux> {
    override val schema: AssociationFieldSchema
    val value: AssociationValueModel<Aux>?
}

interface CompositionFieldModel<Aux> : ScalarFieldModel<Aux> {
    override val schema: CompositionFieldSchema
    val value: EntityModel<Aux>
}

// List fields

interface ListFieldModel<Aux> : FieldModel<Aux>

interface PrimitiveListFieldModel<Aux> : ListFieldModel<Aux> {
    override val schema: PrimitiveFieldSchema
    val value: List<Any>
}

interface AliasListFieldModel<Aux> : ListFieldModel<Aux> {
    override val schema: AliasFieldSchema
    val value: List<Any>
}

interface EnumerationListFieldModel<Aux> : ListFieldModel<Aux> {
    override val schema: EnumerationFieldSchema
    val value: List<EnumerationValueSchema>
}

interface AssociationListFieldModel<Aux> : ListFieldModel<Aux> {
    override val schema: AssociationFieldSchema
    val value: List<AssociationValueModel<Aux>>
}

interface CompositionListFieldModel<Aux> : ListFieldModel<Aux> {
    override val schema: CompositionFieldSchema
    val value: List<EntityModel<Aux>>
}

// Field values

interface AssociationValueModel<Aux> : ElementModel<Aux> {
    val pathKeys: List<EntityKeysModel<Aux>>
}

interface EntityKeysModel<Aux> : BaseEntityModel<Aux> {
    override val schema: EntitySchema
}
