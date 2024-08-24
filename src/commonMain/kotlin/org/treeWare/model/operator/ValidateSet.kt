package org.treeWare.model.operator

import org.treeWare.model.core.EntityModel
import org.treeWare.model.operator.set.SetRequestValidationVisitor
import org.treeWare.model.traversal.forEach

fun validateSet(meta: EntityModel): List<ElementModelError> {
    val validationVisitor = SetRequestValidationVisitor()
    forEach(meta, validationVisitor, false)
    return validationVisitor.errors
}