package org.tree_ware.model.core

import org.tree_ware.schema.core.*

/** Model visitor (Visitor Pattern).
 * This is an enhanced visitor with `visit()` and `leave()` methods for each
 * element instead of just a `visit()` method for each element.
 *
 * `visit()` methods should return `true` to proceed with model traversal
 * and `false` to stop model traversal.
 */
interface ModelVisitor {
    fun visit(element: ElementModel): Boolean
    fun leave(element: ElementModel)

    fun visit(model: Model): Boolean
    fun leave(model: Model)

    fun visit(baseEntity: BaseEntityModel): Boolean
    fun leave(baseEntity: BaseEntityModel)

    fun visit(root: RootModel): Boolean
    fun leave(root: RootModel)

    fun visit(entity: EntityModel): Boolean
    fun leave(entity: EntityModel)

    fun visit(field: FieldModel): Boolean
    fun leave(field: FieldModel)

    // Scalar fields

    fun visit(field: ScalarFieldModel): Boolean
    fun leave(field: ScalarFieldModel)

    fun visit(field: PrimitiveFieldModel): Boolean
    fun leave(field: PrimitiveFieldModel)

    fun visit(field: AliasFieldModel): Boolean
    fun leave(field: AliasFieldModel)

    fun visit(field: EnumerationFieldModel): Boolean
    fun leave(field: EnumerationFieldModel)

    fun visit(field: AssociationFieldModel): Boolean
    fun leave(field: AssociationFieldModel)

    fun visit(field: CompositionFieldModel): Boolean
    fun leave(field: CompositionFieldModel)

    // List fields

    fun visit(field: ListFieldModel): Boolean
    fun leave(field: ListFieldModel)

    fun visit(field: PrimitiveListFieldModel): Boolean
    fun leave(field: PrimitiveListFieldModel)

    fun visit(field: AliasListFieldModel): Boolean
    fun leave(field: AliasListFieldModel)

    fun visit(field: EnumerationListFieldModel): Boolean
    fun leave(field: EnumerationListFieldModel)

    fun visit(field: AssociationListFieldModel): Boolean
    fun leave(field: AssociationListFieldModel)

    fun visit(field: CompositionListFieldModel): Boolean
    fun leave(field: CompositionListFieldModel)

    // Field values

    fun visit(value: Any, fieldSchema: PrimitiveFieldSchema): Boolean
    fun leave(value: Any, fieldSchema: PrimitiveFieldSchema)

    fun visit(value: Any, fieldSchema: AliasFieldSchema): Boolean
    fun leave(value: Any, fieldSchema: AliasFieldSchema)

    fun visit(value: EnumerationValueSchema, fieldSchema: EnumerationFieldSchema): Boolean
    fun leave(value: EnumerationValueSchema, fieldSchema: EnumerationFieldSchema)

    fun visit(value: AssociationValueModel, fieldSchema: AssociationFieldSchema): Boolean
    fun leave(value: AssociationValueModel, fieldSchema: AssociationFieldSchema)

    fun visit(entityKeys: EntityKeysModel): Boolean
    fun leave(entityKeys: EntityKeysModel)

    // NOTE: composition-values are EntityModel instances and so they don't have separate visit/leave methods.
}
