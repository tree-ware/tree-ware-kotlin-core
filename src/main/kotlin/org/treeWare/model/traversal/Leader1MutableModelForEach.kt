package org.treeWare.model.traversal

import org.treeWare.model.core.*
import org.treeWare.model.cursor.CursorMoveDirection
import org.treeWare.model.cursor.Leader1MutableModelCursor

fun mutableForEach(
    leader: MutableElementModel,
    visitor: Leader1MutableModelVisitor<TraversalAction>
): TraversalAction {
    val leaderCursor = Leader1MutableModelCursor(leader)
    var action = TraversalAction.CONTINUE
    while (action != TraversalAction.ABORT_TREE) {
        val leaderMove = leaderCursor.next(action) ?: break
        action = when (leaderMove.direction) {
            CursorMoveDirection.VISIT -> dispatchVisit(leaderMove.element, visitor) ?: TraversalAction.ABORT_TREE
            CursorMoveDirection.LEAVE -> {
                dispatchLeave(leaderMove.element, visitor)
                TraversalAction.CONTINUE
            }
        }
    }
    return action
}

fun <Return> dispatchVisit(
    leader: MutableElementModel,
    visitor: Leader1MutableModelVisitor<Return>
): Return? = when (leader.elementType) {
    ModelElementType.MAIN -> visitor.visitMutableMain(leader as MutableMainModel)
    ModelElementType.ROOT -> visitor.visitMutableRoot(leader as MutableRootModel)
    ModelElementType.ENTITY -> visitor.visitMutableEntity(leader as MutableEntityModel)
    ModelElementType.SINGLE_FIELD -> visitor.visitMutableSingleField(leader as MutableSingleFieldModel)
    ModelElementType.LIST_FIELD -> visitor.visitMutableListField(leader as MutableListFieldModel)
    ModelElementType.SET_FIELD -> visitor.visitMutableSetField(leader as MutableSetFieldModel)
    ModelElementType.PRIMITIVE -> visitor.visitMutablePrimitive(leader as MutablePrimitiveModel)
    ModelElementType.ALIAS -> visitor.visitMutableAlias(leader as MutableAliasModel)
    ModelElementType.PASSWORD1WAY -> visitor.visitMutablePassword1way(leader as MutablePassword1wayModel)
    ModelElementType.PASSWORD2WAY -> visitor.visitMutablePassword2way(leader as MutablePassword2wayModel)
    ModelElementType.ENUMERATION -> visitor.visitMutableEnumeration(leader as MutableEnumerationModel)
    ModelElementType.ASSOCIATION -> visitor.visitMutableAssociation(leader as MutableAssociationModel)
    ModelElementType.ENTITY_KEYS -> visitor.visitMutableEntityKeys(leader as MutableEntityKeysModel)
}

fun <Return> dispatchLeave(
    leader: MutableElementModel,
    visitor: Leader1MutableModelVisitor<Return>
) = when (leader.elementType) {
    ModelElementType.MAIN -> visitor.leaveMutableMain(leader as MutableMainModel)
    ModelElementType.ROOT -> visitor.leaveMutableRoot(leader as MutableRootModel)
    ModelElementType.ENTITY -> visitor.leaveMutableEntity(leader as MutableEntityModel)
    ModelElementType.SINGLE_FIELD -> visitor.leaveMutableSingleField(leader as MutableSingleFieldModel)
    ModelElementType.LIST_FIELD -> visitor.leaveMutableListField(leader as MutableListFieldModel)
    ModelElementType.SET_FIELD -> visitor.leaveMutableSetField(leader as MutableSetFieldModel)
    ModelElementType.PRIMITIVE -> visitor.leaveMutablePrimitive(leader as MutablePrimitiveModel)
    ModelElementType.ALIAS -> visitor.leaveMutableAlias(leader as MutableAliasModel)
    ModelElementType.PASSWORD1WAY -> visitor.leaveMutablePassword1way(leader as MutablePassword1wayModel)
    ModelElementType.PASSWORD2WAY -> visitor.leaveMutablePassword2way(leader as MutablePassword2wayModel)
    ModelElementType.ENUMERATION -> visitor.leaveMutableEnumeration(leader as MutableEnumerationModel)
    ModelElementType.ASSOCIATION -> visitor.leaveMutableAssociation(leader as MutableAssociationModel)
    ModelElementType.ENTITY_KEYS -> visitor.leaveMutableEntityKeys(leader as MutableEntityKeysModel)
}