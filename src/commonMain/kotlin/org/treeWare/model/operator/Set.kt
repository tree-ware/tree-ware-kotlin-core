package org.treeWare.model.operator

import org.treeWare.model.core.EntityModel
import org.treeWare.model.core.MainModel
import org.treeWare.model.operator.set.SetDelegate
import org.treeWare.model.operator.set.SetDelegateVisitor
import org.treeWare.model.operator.set.SetRequestValidationVisitor
import org.treeWare.model.traversal.forEach

interface SetEntityDelegate {
    fun isSingleValue(): Boolean
    fun getSingleValue(entity: EntityModel): Any?
}

object SetOperatorId : OperatorId<SetEntityDelegate>

fun set(
    main: MainModel,
    setDelegate: SetDelegate,
    entityDelegates: EntityDelegateRegistry<SetEntityDelegate>?
): List<ElementModelError> {
    // TODO(performance): traverse all visitors together.

    val validationVisitor = SetRequestValidationVisitor()
    forEach(main, validationVisitor, false)
    if (validationVisitor.errors.isNotEmpty()) return validationVisitor.errors

    val beginErrors = setDelegate.begin()
    if (beginErrors.isNotEmpty()) return beginErrors

    val setVisitor = SetDelegateVisitor(setDelegate, entityDelegates)
    forEach(main, setVisitor, false)
    if (setVisitor.errors.isNotEmpty()) return setVisitor.errors

    return setDelegate.end()
}