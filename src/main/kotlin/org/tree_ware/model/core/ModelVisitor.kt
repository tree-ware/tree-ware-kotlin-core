package org.tree_ware.model.core

import org.tree_ware.schema.core.*

/** Model visitor (Visitor Pattern).
 * This is an enhanced visitor with `visit()` and `leave()` methods for each
 * element instead of just a `visit()` method for each element.
 */
interface ModelVisitor<T> {
    fun visit(element: ElementModel): T
    fun leave(element: ElementModel)

    fun visit(model: Model): T
    fun leave(model: Model)

    fun visit(baseEntity: BaseEntityModel): T
    fun leave(baseEntity: BaseEntityModel)

    fun visit(root: RootModel): T
    fun leave(root: RootModel)

    fun visit(entity: EntityModel): T
    fun leave(entity: EntityModel)

    fun visit(field: FieldModel): T
    fun leave(field: FieldModel)

    // Scalar fields

    fun visit(field: ScalarFieldModel): T
    fun leave(field: ScalarFieldModel)

    fun visit(field: PrimitiveFieldModel): T
    fun leave(field: PrimitiveFieldModel)

    fun visit(field: AliasFieldModel): T
    fun leave(field: AliasFieldModel)

    fun visit(field: EnumerationFieldModel): T
    fun leave(field: EnumerationFieldModel)

    fun visit(field: AssociationFieldModel): T
    fun leave(field: AssociationFieldModel)

    fun visit(field: CompositionFieldModel): T
    fun leave(field: CompositionFieldModel)

    // List fields

    fun visit(field: ListFieldModel): T
    fun leave(field: ListFieldModel)

    fun visit(field: PrimitiveListFieldModel): T
    fun leave(field: PrimitiveListFieldModel)

    fun visit(field: AliasListFieldModel): T
    fun leave(field: AliasListFieldModel)

    fun visit(field: EnumerationListFieldModel): T
    fun leave(field: EnumerationListFieldModel)

    fun visit(field: AssociationListFieldModel): T
    fun leave(field: AssociationListFieldModel)

    fun visit(field: CompositionListFieldModel): T
    fun leave(field: CompositionListFieldModel)

    // Field values

    fun visit(value: Any?, fieldSchema: PrimitiveFieldSchema): T
    fun leave(value: Any?, fieldSchema: PrimitiveFieldSchema)

    fun visit(value: Any?, fieldSchema: AliasFieldSchema): T
    fun leave(value: Any?, fieldSchema: AliasFieldSchema)

    fun visit(value: EnumerationValueSchema?, fieldSchema: EnumerationFieldSchema): T
    fun leave(value: EnumerationValueSchema?, fieldSchema: EnumerationFieldSchema)

    fun visit(value: AssociationValueModel, fieldSchema: AssociationFieldSchema): T
    fun leave(value: AssociationValueModel, fieldSchema: AssociationFieldSchema)

    fun visit(entityKeys: EntityKeysModel): T
    fun leave(entityKeys: EntityKeysModel)

    // NOTE: composition-values are EntityModel instances and so they don't have separate visit/leave methods.
}
