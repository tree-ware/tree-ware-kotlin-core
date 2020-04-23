package org.tree_ware.model.core

import org.tree_ware.core.schema.*

/** MutableModel visitor (Visitor Pattern).
 *
 * `ModelVisitor` cannot mutate the model elements it visits,
 * but this interface can mutate the model elements it visits.
 *
 * This is an enhanced visitor with `mutableVisit()` and `mutableLeave()` methods for
 * each element instead of just a `mutableVisit()` method for each element.
 *
 * `mutableVisit()` methods should return `true` to proceed with model traversal
 * and `false` to stop model traversal.
 */
interface MutableModelVisitor {
    fun mutableVisit(element: MutableElementModel): Boolean
    fun mutableLeave(element: MutableElementModel)

    fun mutableVisit(model: MutableModel): Boolean
    fun mutableLeave(model: MutableModel)

    fun mutableVisit(baseEntity: MutableBaseEntityModel): Boolean
    fun mutableLeave(baseEntity: MutableBaseEntityModel)

    fun mutableVisit(root: MutableRootModel): Boolean
    fun mutableLeave(root: MutableRootModel)

    fun mutableVisit(entity: MutableEntityModel): Boolean
    fun mutableLeave(entity: MutableEntityModel)

    fun mutableVisit(field: MutableFieldModel): Boolean
    fun mutableLeave(field: MutableFieldModel)

    // Scalar fields

    fun mutableVisit(field: MutableScalarFieldModel): Boolean
    fun mutableLeave(field: MutableScalarFieldModel)

    fun mutableVisit(field: MutablePrimitiveFieldModel): Boolean
    fun mutableLeave(field: MutablePrimitiveFieldModel)

    fun mutableVisit(field: MutableAliasFieldModel): Boolean
    fun mutableLeave(field: MutableAliasFieldModel)

    fun mutableVisit(field: MutableEnumerationFieldModel): Boolean
    fun mutableLeave(field: MutableEnumerationFieldModel)

    fun mutableVisit(field: MutableAssociationFieldModel): Boolean
    fun mutableLeave(field: MutableAssociationFieldModel)

    fun mutableVisit(field: MutableCompositionFieldModel): Boolean
    fun mutableLeave(field: MutableCompositionFieldModel)

    // List fields

    fun mutableVisit(field: MutableListFieldModel): Boolean
    fun mutableLeave(field: MutableListFieldModel)

    fun mutableVisit(field: MutablePrimitiveListFieldModel): Boolean
    fun mutableLeave(field: MutablePrimitiveListFieldModel)

    fun mutableVisit(field: MutableAliasListFieldModel): Boolean
    fun mutableLeave(field: MutableAliasListFieldModel)

    fun mutableVisit(field: MutableEnumerationListFieldModel): Boolean
    fun mutableLeave(field: MutableEnumerationListFieldModel)

    fun mutableVisit(field: MutableAssociationListFieldModel): Boolean
    fun mutableLeave(field: MutableAssociationListFieldModel)

    fun mutableVisit(field: MutableCompositionListFieldModel): Boolean
    fun mutableLeave(field: MutableCompositionListFieldModel)

    // Field values

    fun mutableVisit(value: Any, fieldSchema: PrimitiveFieldSchema): Boolean
    fun mutableLeave(value: Any, fieldSchema: PrimitiveFieldSchema)

    fun mutableVisit(value: Any, fieldSchema: AliasFieldSchema): Boolean
    fun mutableLeave(value: Any, fieldSchema: AliasFieldSchema)

    fun mutableVisit(value: EnumerationValueSchema, fieldSchema: EnumerationFieldSchema): Boolean
    fun mutableLeave(value: EnumerationValueSchema, fieldSchema: EnumerationFieldSchema)

    fun mutableVisit(value: MutableAssociationValueModel, fieldSchema: AssociationFieldSchema): Boolean
    fun mutableLeave(value: MutableAssociationValueModel, fieldSchema: AssociationFieldSchema)

    fun mutableVisit(entityKeys: MutableEntityKeysModel): Boolean
    fun mutableLeave(entityKeys: MutableEntityKeysModel)

    // NOTE: composition-values are EntityModel instances and so they don't have separate visit/leave methods.
}
