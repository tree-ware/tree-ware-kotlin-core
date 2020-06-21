package org.tree_ware.model.core

import org.tree_ware.schema.core.*

interface VisitableModel<Aux> {
    /**
     * Visits the model element and its superclasses.
     * The superclasses are visited first and the model element itself is visited last.
     */
    fun visitSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction

    /**
     * Leaves the model element and its superclasses.
     * The model element itself is left first and the superclasses are left last.
     */
    fun leaveSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>)

    /**
     * Visits the model element without traversing its sub-elements.
     * Leave methods are NOT called.
     * Returns what the visitor returns.
     */
    fun <Return> dispatch(visitor: ModelVisitor<Aux, Return>): Return
}

interface ElementModel<Aux> : VisitableModel<Aux> {
    val schema: VisitableSchema
    val parent: ElementModel<Aux>?
    val aux: Aux?
}

enum class ModelType {
    data,
    error
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
}

// Scalar fields

interface ScalarFieldModel<Aux> : FieldModel<Aux> {
    override val parent: ElementModel<Aux>
}

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

interface AssociationFieldModel<Aux> : FieldModel<Aux> {
    override val schema: AssociationFieldSchema
    val pathKeys: List<EntityKeysModel<Aux>>
}

interface CompositionFieldModel<Aux> : FieldModel<Aux> {
    override val schema: CompositionFieldSchema
    val value: EntityModel<Aux>
}

// List fields

interface ListFieldModel<Aux> : FieldModel<Aux> {
    override val parent: BaseEntityModel<Aux>
}

interface ScalarListFieldModel<Aux> : ListFieldModel<Aux>

interface PrimitiveListFieldModel<Aux> : ScalarListFieldModel<Aux> {
    override val schema: PrimitiveFieldSchema
    val primitives: List<PrimitiveFieldModel<Aux>>
}

interface AliasListFieldModel<Aux> : ScalarListFieldModel<Aux> {
    override val schema: AliasFieldSchema
    val aliases: List<AliasFieldModel<Aux>>
}

interface EnumerationListFieldModel<Aux> : ScalarListFieldModel<Aux> {
    override val schema: EnumerationFieldSchema
    val enumerations: List<EnumerationFieldModel<Aux>>
}

interface AssociationListFieldModel<Aux> : ListFieldModel<Aux> {
    override val schema: AssociationFieldSchema
    val value: List<AssociationFieldModel<Aux>>
}

interface CompositionListFieldModel<Aux> : ListFieldModel<Aux> {
    override val schema: CompositionFieldSchema
    val value: List<EntityModel<Aux>>
}

// Field values

interface EntityKeysModel<Aux> : BaseEntityModel<Aux> {
    override val schema: EntitySchema
}
