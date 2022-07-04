package org.treeWare.model.operator

import org.treeWare.model.core.EntityModel
import org.treeWare.model.core.MainModel
import org.treeWare.model.operator.set.SetDelegate
import org.treeWare.model.operator.set.SetDelegateVisitor
import org.treeWare.model.operator.set.SetResponse
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
): SetResponse {
    val beginErrors = setDelegate.begin()
    if (beginErrors.isNotEmpty()) return SetResponse.ErrorList(ErrorCode.SERVER_ERROR, beginErrors)

    val setVisitor = SetDelegateVisitor(setDelegate, entityDelegates)
    forEach(main, setVisitor, false)
    if (setVisitor.errors.isNotEmpty()) return SetResponse.ErrorList(ErrorCode.CLIENT_ERROR, setVisitor.errors)

    val endErrors = setDelegate.end()
    if (endErrors.isNotEmpty()) return SetResponse.ErrorList(ErrorCode.CLIENT_ERROR, endErrors)

    return SetResponse.Success
}