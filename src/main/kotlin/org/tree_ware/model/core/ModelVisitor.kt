package org.tree_ware.model.core

import org.tree_ware.schema.core.*

/** Model visitor (Visitor Pattern).
 * This is an enhanced visitor with `visit()` and `leave()` methods for each
 * element instead of just a `visit()` method for each element.
 */
interface ModelVisitor<Aux, Return> {
    fun visit(element: ElementModel<Aux>): Return
    fun leave(element: ElementModel<Aux>)

    fun visit(model: Model<Aux>): Return
    fun leave(model: Model<Aux>)

    fun visit(baseEntity: BaseEntityModel<Aux>): Return
    fun leave(baseEntity: BaseEntityModel<Aux>)

    fun visit(root: RootModel<Aux>): Return
    fun leave(root: RootModel<Aux>)

    fun visit(entity: EntityModel<Aux>): Return
    fun leave(entity: EntityModel<Aux>)

    fun visit(field: FieldModel<Aux>): Return
    fun leave(field: FieldModel<Aux>)

    // Scalar fields

    fun visit(field: ScalarFieldModel<Aux>): Return
    fun leave(field: ScalarFieldModel<Aux>)

    fun visit(field: PrimitiveFieldModel<Aux>): Return
    fun leave(field: PrimitiveFieldModel<Aux>)

    fun visit(field: AliasFieldModel<Aux>): Return
    fun leave(field: AliasFieldModel<Aux>)

    fun visit(field: EnumerationFieldModel<Aux>): Return
    fun leave(field: EnumerationFieldModel<Aux>)

    fun visit(field: AssociationFieldModel<Aux>): Return
    fun leave(field: AssociationFieldModel<Aux>)

    fun visit(field: CompositionFieldModel<Aux>): Return
    fun leave(field: CompositionFieldModel<Aux>)

    // List fields

    fun visit(field: ListFieldModel<Aux>): Return
    fun leave(field: ListFieldModel<Aux>)

    fun visit(field: PrimitiveListFieldModel<Aux>): Return
    fun leave(field: PrimitiveListFieldModel<Aux>)

    fun visit(field: AliasListFieldModel<Aux>): Return
    fun leave(field: AliasListFieldModel<Aux>)

    fun visit(field: EnumerationListFieldModel<Aux>): Return
    fun leave(field: EnumerationListFieldModel<Aux>)

    fun visit(field: AssociationListFieldModel<Aux>): Return
    fun leave(field: AssociationListFieldModel<Aux>)

    fun visit(field: CompositionListFieldModel<Aux>): Return
    fun leave(field: CompositionListFieldModel<Aux>)

    // Field values

    fun visit(value: Any?, fieldSchema: PrimitiveFieldSchema): Return
    fun leave(value: Any?, fieldSchema: PrimitiveFieldSchema)

    fun visit(value: Any?, fieldSchema: AliasFieldSchema): Return
    fun leave(value: Any?, fieldSchema: AliasFieldSchema)

    fun visit(value: EnumerationValueSchema?, fieldSchema: EnumerationFieldSchema): Return
    fun leave(value: EnumerationValueSchema?, fieldSchema: EnumerationFieldSchema)

    fun visit(value: AssociationValueModel<Aux>, fieldSchema: AssociationFieldSchema): Return
    fun leave(value: AssociationValueModel<Aux>, fieldSchema: AssociationFieldSchema)

    fun visit(entityKeys: EntityKeysModel<Aux>): Return
    fun leave(entityKeys: EntityKeysModel<Aux>)

    // NOTE: composition-values are EntityModel instances and so they don't have separate visit/leave methods.
}
