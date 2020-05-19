package org.tree_ware.model.core

import org.tree_ware.schema.core.*

/** MutableModel visitor (Visitor Pattern).
 * This is an enhanced visitor with `mutableVisit()` and `mutableLeave()` methods for
 * each element instead of just a `mutableVisit()` method for each element.
 *
 * `ModelVisitor` cannot mutate the model elements it visits,
 * but this interface can mutate the model elements it visits.
 */
interface MutableModelVisitor<T> {
    fun mutableVisit(element: MutableElementModel): T
    fun mutableLeave(element: MutableElementModel)

    fun mutableVisit(model: MutableModel): T
    fun mutableLeave(model: MutableModel)

    fun mutableVisit(baseEntity: MutableBaseEntityModel): T
    fun mutableLeave(baseEntity: MutableBaseEntityModel)

    fun mutableVisit(root: MutableRootModel): T
    fun mutableLeave(root: MutableRootModel)

    fun mutableVisit(entity: MutableEntityModel): T
    fun mutableLeave(entity: MutableEntityModel)

    fun mutableVisit(field: MutableFieldModel): T
    fun mutableLeave(field: MutableFieldModel)

    // Scalar fields

    fun mutableVisit(field: MutableScalarFieldModel): T
    fun mutableLeave(field: MutableScalarFieldModel)

    fun mutableVisit(field: MutablePrimitiveFieldModel): T
    fun mutableLeave(field: MutablePrimitiveFieldModel)

    fun mutableVisit(field: MutableAliasFieldModel): T
    fun mutableLeave(field: MutableAliasFieldModel)

    fun mutableVisit(field: MutableEnumerationFieldModel): T
    fun mutableLeave(field: MutableEnumerationFieldModel)

    fun mutableVisit(field: MutableAssociationFieldModel): T
    fun mutableLeave(field: MutableAssociationFieldModel)

    fun mutableVisit(field: MutableCompositionFieldModel): T
    fun mutableLeave(field: MutableCompositionFieldModel)

    // List fields

    fun mutableVisit(field: MutableListFieldModel): T
    fun mutableLeave(field: MutableListFieldModel)

    fun mutableVisit(field: MutablePrimitiveListFieldModel): T
    fun mutableLeave(field: MutablePrimitiveListFieldModel)

    fun mutableVisit(field: MutableAliasListFieldModel): T
    fun mutableLeave(field: MutableAliasListFieldModel)

    fun mutableVisit(field: MutableEnumerationListFieldModel): T
    fun mutableLeave(field: MutableEnumerationListFieldModel)

    fun mutableVisit(field: MutableAssociationListFieldModel): T
    fun mutableLeave(field: MutableAssociationListFieldModel)

    fun mutableVisit(field: MutableCompositionListFieldModel): T
    fun mutableLeave(field: MutableCompositionListFieldModel)

    // Field values

    fun mutableVisit(value: Any, fieldSchema: PrimitiveFieldSchema): T
    fun mutableLeave(value: Any, fieldSchema: PrimitiveFieldSchema)

    fun mutableVisit(value: Any, fieldSchema: AliasFieldSchema): T
    fun mutableLeave(value: Any, fieldSchema: AliasFieldSchema)

    fun mutableVisit(value: EnumerationValueSchema, fieldSchema: EnumerationFieldSchema): T
    fun mutableLeave(value: EnumerationValueSchema, fieldSchema: EnumerationFieldSchema)

    fun mutableVisit(value: MutableAssociationValueModel, fieldSchema: AssociationFieldSchema): T
    fun mutableLeave(value: MutableAssociationValueModel, fieldSchema: AssociationFieldSchema)

    fun mutableVisit(entityKeys: MutableEntityKeysModel): T
    fun mutableLeave(entityKeys: MutableEntityKeysModel)

    // NOTE: composition-values are EntityModel instances and so they don't have separate visit/leave methods.
}
