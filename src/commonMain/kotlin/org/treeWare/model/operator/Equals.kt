package org.treeWare.model.operator

import org.treeWare.model.core.*

/**
 * Returns `true` if the two model elements are equal, `false` otherwise.
 *
 * This is a lighter-weight alternative to using the `difference()` operator
 * for detecting if model elements are equal. It aborts the traversal and
 * returns `false` as soon as inequality is detected instead of traversing
 * the entire tree.
 */
fun equals(first: ElementModel, second: ElementModel): Boolean {
    if (first === second) return true
    if (first.elementType != second.elementType) return false
    return when (first) {
        is EntityModel -> entitiesEqual(first, second as EntityModel)
        is SingleFieldModel -> singleFieldsEqual(first, second as SingleFieldModel)
        is SetFieldModel -> setFieldsEqual(first, second as SetFieldModel)
        is PrimitiveModel -> first.matches(second)
        is AliasModel -> first.matches(second)
        is Password1wayModel -> first.matches(second)
        is Password2wayModel -> first.matches(second)
        is EnumerationModel -> first.matches(second)
        is AssociationModel -> associationsEqual(first, second as AssociationModel)
        else -> false
    }
}

private fun entitiesEqual(first: EntityModel, second: EntityModel): Boolean {
    if (first.fields.size != second.fields.size) return false
    return first.fields.all { (fieldName, firstField) ->
        val secondField = second.getField(fieldName) ?: return false
        equals(firstField, secondField)
    }
}

private fun singleFieldsEqual(first: SingleFieldModel, second: SingleFieldModel): Boolean {
    val firstValue = first.value
    val secondValue = second.value
    if (firstValue == null) return secondValue == null
    if (secondValue == null) return false
    return equals(firstValue, secondValue)
}

private fun setFieldsEqual(first: SetFieldModel, second: SetFieldModel): Boolean {
    if (first.values.size != second.values.size) return false
    return first.values.all { firstValue ->
        val secondValue = second.getValueMatching(firstValue) ?: return false
        equals(firstValue, secondValue)
    }
}

// Associations are compared directly instead of using the `difference()`
// operator (which copies the association path trees into new entity models
// before comparing them) so that the comparison stays light-weight and
// aborts as soon as inequality is detected.
private fun associationsEqual(first: AssociationModel, second: AssociationModel): Boolean =
    equals(first.value, second.value)
