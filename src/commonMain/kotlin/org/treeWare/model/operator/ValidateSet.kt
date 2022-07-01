package org.treeWare.model.operator

import org.treeWare.model.core.MainModel
import org.treeWare.model.operator.set.SetRequestValidationVisitor
import org.treeWare.model.traversal.forEach

fun validateSet(main: MainModel): List<ElementModelError> {
    val validationVisitor = SetRequestValidationVisitor()
    forEach(main, validationVisitor, false)
    return validationVisitor.errors
}