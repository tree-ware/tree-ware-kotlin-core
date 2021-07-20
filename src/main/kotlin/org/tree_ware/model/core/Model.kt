package org.tree_ware.model.core

import org.tree_ware.common.traversal.TraversalAction
import org.tree_ware.schema.core.*

interface VisitableModel<Aux> {
    /**
     * Visits the model element and its superclasses.
     * The superclasses are visited first and the model element itself is visited last.
     */
    fun visitSelf(visitor: ModelVisitor<Aux, TraversalAction>): TraversalAction

    /**
     * Leaves the model element and its superclasses.
     * The model element itself is left first and the superclasses are left last.
     */
    fun leaveSelf(visitor: ModelVisitor<Aux, TraversalAction>)

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

/** The entire model (from the root entity). */
interface Model<Aux> : ElementModel<Aux> {
    val type: String
    val root: RootModel<Aux>

    override val schema: Schema
}

interface BaseEntityModel<Aux> : ElementModel<Aux> {
    val fields: List<FieldModel<Aux>>

    fun getField(fieldName: String): FieldModel<Aux>?
    fun <ThatAux> keysMatch(that: BaseEntityModel<ThatAux>): Boolean
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

    fun <ThatAux> keysMatch(that: FieldModel<ThatAux>): Boolean
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
    val value: List<EntityKeysModel<Aux>>
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

    fun getPrimitiveField(matching: Any?): PrimitiveFieldModel<Aux>?
}

interface AliasListFieldModel<Aux> : ScalarListFieldModel<Aux> {
    override val schema: AliasFieldSchema
    val aliases: List<AliasFieldModel<Aux>>

    fun getAliasField(matching: Any?): AliasFieldModel<Aux>?
}

interface EnumerationListFieldModel<Aux> : ScalarListFieldModel<Aux> {
    override val schema: EnumerationFieldSchema
    val enumerations: List<EnumerationFieldModel<Aux>>

    fun getEnumerationField(matching: EnumerationValueSchema?): EnumerationFieldModel<Aux>?
}

interface AssociationListFieldModel<Aux> : ListFieldModel<Aux> {
    override val schema: AssociationFieldSchema
    val associations: List<AssociationFieldModel<Aux>>

    fun <MatchingAux> getAssociationField(matching: List<EntityKeysModel<MatchingAux>>): AssociationFieldModel<Aux>?
}

interface CompositionListFieldModel<Aux> : ListFieldModel<Aux> {
    override val schema: CompositionFieldSchema
    val entities: List<EntityModel<Aux>>

    fun <MatchingAux> getEntity(matching: EntityModel<MatchingAux>): EntityModel<Aux>?
}

// Field values

interface EntityKeysModel<Aux> : BaseEntityModel<Aux> {
    override val schema: EntitySchema
}
