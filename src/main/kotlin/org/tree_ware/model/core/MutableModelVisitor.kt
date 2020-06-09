package org.tree_ware.model.core

/** MutableModel visitor (Visitor Pattern).
 * This is an enhanced visitor with `mutableVisit()` and `mutableLeave()` methods for
 * each element instead of just a `mutableVisit()` method for each element.
 *
 * `ModelVisitor` cannot mutate the model elements it visits,
 * but this interface can mutate the model elements it visits.
 */
interface MutableModelVisitor<Aux, Return> {
    fun mutableVisit(element: MutableElementModel<Aux>): Return
    fun mutableLeave(element: MutableElementModel<Aux>)

    fun mutableVisit(model: MutableModel<Aux>): Return
    fun mutableLeave(model: MutableModel<Aux>)

    fun mutableVisit(baseEntity: MutableBaseEntityModel<Aux>): Return
    fun mutableLeave(baseEntity: MutableBaseEntityModel<Aux>)

    fun mutableVisit(root: MutableRootModel<Aux>): Return
    fun mutableLeave(root: MutableRootModel<Aux>)

    fun mutableVisit(entity: MutableEntityModel<Aux>): Return
    fun mutableLeave(entity: MutableEntityModel<Aux>)

    fun mutableVisit(field: MutableFieldModel<Aux>): Return
    fun mutableLeave(field: MutableFieldModel<Aux>)

    // Scalar fields

    fun mutableVisit(field: MutableScalarFieldModel<Aux>): Return
    fun mutableLeave(field: MutableScalarFieldModel<Aux>)

    fun mutableVisit(field: MutablePrimitiveFieldModel<Aux>): Return
    fun mutableLeave(field: MutablePrimitiveFieldModel<Aux>)

    fun mutableVisit(field: MutableAliasFieldModel<Aux>): Return
    fun mutableLeave(field: MutableAliasFieldModel<Aux>)

    fun mutableVisit(field: MutableEnumerationFieldModel<Aux>): Return
    fun mutableLeave(field: MutableEnumerationFieldModel<Aux>)

    fun mutableVisit(field: MutableAssociationFieldModel<Aux>): Return
    fun mutableLeave(field: MutableAssociationFieldModel<Aux>)

    fun mutableVisit(field: MutableCompositionFieldModel<Aux>): Return
    fun mutableLeave(field: MutableCompositionFieldModel<Aux>)

    // List fields

    fun mutableVisit(field: MutableListFieldModel<Aux>): Return
    fun mutableLeave(field: MutableListFieldModel<Aux>)

    fun mutableVisit(field: MutableScalarListFieldModel<Aux>): Return
    fun mutableLeave(field: MutableScalarListFieldModel<Aux>)

    fun mutableVisit(field: MutablePrimitiveListFieldModel<Aux>): Return
    fun mutableLeave(field: MutablePrimitiveListFieldModel<Aux>)

    fun mutableVisit(field: MutableAliasListFieldModel<Aux>): Return
    fun mutableLeave(field: MutableAliasListFieldModel<Aux>)

    fun mutableVisit(field: MutableEnumerationListFieldModel<Aux>): Return
    fun mutableLeave(field: MutableEnumerationListFieldModel<Aux>)

    fun mutableVisit(field: MutableAssociationListFieldModel<Aux>): Return
    fun mutableLeave(field: MutableAssociationListFieldModel<Aux>)

    fun mutableVisit(field: MutableCompositionListFieldModel<Aux>): Return
    fun mutableLeave(field: MutableCompositionListFieldModel<Aux>)

    // Field values

    fun mutableVisit(entityKeys: MutableEntityKeysModel<Aux>): Return
    fun mutableLeave(entityKeys: MutableEntityKeysModel<Aux>)

    // NOTE: composition-values are EntityModel instances and so they don't have separate visit/leave methods.
}
