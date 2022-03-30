package org.treeWare.metaModel.validation

import org.treeWare.metaModel.getMetaNumber
import org.treeWare.metaModel.traversal.AbstractLeader1MetaModelVisitor
import org.treeWare.metaModel.traversal.metaModelForEach
import org.treeWare.model.core.EntityModel
import org.treeWare.model.core.MainModel
import org.treeWare.model.core.getMetaModelResolved
import org.treeWare.model.traversal.TraversalAction

/**
 * Validates the numbers in the meta-model.
 * Returns a list of errors. Returns an empty list if there are no errors.
 *
 * Side effects: none
 */
fun validateNumbers(mainMeta: MainModel): List<String> {
    val visitor = ValidateNumbersVisitor()
    metaModelForEach(mainMeta, visitor)
    return visitor.errors
}

private interface NumberValidator {
    fun isWithinBounds(number: UInt): Boolean
    fun isFirstValid(number: UInt): Boolean
}

private object EnumerationNumberValidator : NumberValidator {
    override fun isWithinBounds(number: UInt): Boolean = true // all unsigned 32-bit numbers are supported
    override fun isFirstValid(number: UInt): Boolean = number == 0U
}

private object FieldNumberValidator : NumberValidator {
    override fun isWithinBounds(number: UInt): Boolean =
        // Tree-ware uses proto3 field number bounds
        (number > 0U && number < 19000U) || (number > 19999U && number < 536870912U)

    override fun isFirstValid(number: UInt): Boolean = true // any valid field number can be the first one
}

private class ValidateNumbersVisitor : AbstractLeader1MetaModelVisitor<TraversalAction>(TraversalAction.CONTINUE) {
    val errors = mutableListOf<String>()
    private val numbers = HashSet<UInt>()
    private var isFirstNumber = true

    private fun clearState(): TraversalAction {
        numbers.clear()
        isFirstNumber = true
        return TraversalAction.CONTINUE
    }

    private fun validateNumber(
        meta: EntityModel,
        metaType: String,
        validator: NumberValidator
    ): TraversalAction {
        val number = getMetaNumber(meta)
        val fullName = getMetaModelResolved(meta)?.fullName ?: ""
        if (number == null) errors.add("$metaType number is missing for $fullName")
        else if (!validator.isWithinBounds(number)) errors.add("$metaType number $number is out of bounds for $fullName")
        else if (isFirstNumber.also { isFirstNumber = false } && !validator.isFirstValid(number)) {
            errors.add("First $metaType number $number is invalid for $fullName")
        } else if (numbers.contains(number)) errors.add("$metaType number $number is a duplicate for $fullName")
        else numbers.add(number)
        return TraversalAction.CONTINUE
    }

    override fun visitEnumerationMeta(leaderEnumerationMeta1: EntityModel): TraversalAction = clearState()

    override fun visitEnumerationValueMeta(leaderEnumerationValueMeta1: EntityModel): TraversalAction =
        validateNumber(leaderEnumerationValueMeta1, "Enumeration", EnumerationNumberValidator)

    override fun visitEntityMeta(leaderEntityMeta1: EntityModel): TraversalAction = clearState()

    override fun visitFieldMeta(leaderFieldMeta1: EntityModel): TraversalAction =
        validateNumber(leaderFieldMeta1, "Field", FieldNumberValidator)
}