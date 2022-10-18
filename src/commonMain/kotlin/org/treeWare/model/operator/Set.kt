package org.treeWare.model.operator

import org.treeWare.model.core.MainModel
import org.treeWare.model.operator.set.SetDelegate
import org.treeWare.model.operator.set.SetDelegateVisitor
import org.treeWare.model.operator.set.SetResponse
import org.treeWare.model.traversal.forEach

interface SetEntityDelegate {
    fun isSingleValue(): Boolean
}

object SetOperatorId : OperatorId<SetEntityDelegate>

fun set(
    main: MainModel,
    setDelegate: SetDelegate,
    entityDelegates: EntityDelegateRegistry<SetEntityDelegate>?
): SetResponse {
    val beginResponse = setDelegate.begin()
    if (!beginResponse.isOk()) return beginResponse

    val setVisitor = SetDelegateVisitor(setDelegate, entityDelegates)
    forEach(main, setVisitor, false)
    if (setVisitor.errors.isNotEmpty()) return SetResponse.ErrorList(ErrorCode.CLIENT_ERROR, setVisitor.errors)

    val endResponse = setDelegate.end()
    if (!endResponse.isOk()) return endResponse

    return SetResponse.Success
}