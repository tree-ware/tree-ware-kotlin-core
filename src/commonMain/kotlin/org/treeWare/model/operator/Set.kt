package org.treeWare.model.operator

import org.treeWare.model.core.EntityModel
import org.treeWare.model.core.MainModel
import org.treeWare.model.operator.set.SetDelegate
import org.treeWare.model.operator.set.SetDelegateVisitor
import org.treeWare.model.traversal.forEach

interface SetEntityDelegate {
    fun isSingleValue(): Boolean

    // TODO(cleanup): getSingleValue() is not used in core code, only in delegate code. So drop from here.
    fun getSingleValue(entity: EntityModel): Any?
}

object SetOperatorId : OperatorId<SetEntityDelegate>

fun set(
    main: MainModel,
    setDelegate: SetDelegate,
    entityDelegates: EntityDelegateRegistry<SetEntityDelegate>?
): List<ElementModelError> {
    val beginErrors = setDelegate.begin()
    if (beginErrors.isNotEmpty()) return beginErrors

    val setVisitor = SetDelegateVisitor(setDelegate, entityDelegates)
    forEach(main, setVisitor, false)
    if (setVisitor.errors.isNotEmpty()) return setVisitor.errors

    return setDelegate.end()
}