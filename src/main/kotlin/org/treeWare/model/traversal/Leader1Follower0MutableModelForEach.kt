package org.treeWare.model.traversal

import org.treeWare.model.core.*
import org.treeWare.model.cursor.CursorMoveDirection
import org.treeWare.model.cursor.LeaderMutableModelCursor

fun mutableForEach(
    leader: MutableElementModel,
    visitor: Leader1Follower0MutableModelVisitor<TraversalAction>
): TraversalAction {
    val leaderCursor = LeaderMutableModelCursor(leader)
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
    visitor: Leader1Follower0MutableModelVisitor<Return>
): Return? = when (leader.elementType) {
    ModelElementType.MAIN -> visitor.visit(leader as MutableMainModel)
    ModelElementType.ROOT -> visitor.visit(leader as MutableRootModel)
    ModelElementType.ENTITY -> visitor.visit(leader as MutableEntityModel)
    ModelElementType.SINGLE_FIELD -> visitor.visit(leader as MutableSingleFieldModel)
    ModelElementType.LIST_FIELD -> visitor.visit(leader as MutableListFieldModel)
    ModelElementType.SET_FIELD -> visitor.visit(leader as MutableSetFieldModel)
    ModelElementType.PRIMITIVE -> visitor.visit(leader as MutablePrimitiveModel)
    ModelElementType.ALIAS -> visitor.visit(leader as MutableAliasModel)
    ModelElementType.PASSWORD1WAY -> visitor.visit(leader as MutablePassword1wayModel)
    ModelElementType.PASSWORD2WAY -> visitor.visit(leader as MutablePassword2wayModel)
    ModelElementType.ENUMERATION -> visitor.visit(leader as MutableEnumerationModel)
    ModelElementType.ASSOCIATION -> visitor.visit(leader as MutableAssociationModel)
    ModelElementType.ENTITY_KEYS -> visitor.visit(leader as MutableEntityKeysModel)
}

fun <Return> dispatchLeave(
    leader: MutableElementModel,
    visitor: Leader1Follower0MutableModelVisitor<Return>
) = when (leader.elementType) {
    ModelElementType.MAIN -> visitor.leave(leader as MutableMainModel)
    ModelElementType.ROOT -> visitor.leave(leader as MutableRootModel)
    ModelElementType.ENTITY -> visitor.leave(leader as MutableEntityModel)
    ModelElementType.SINGLE_FIELD -> visitor.leave(leader as MutableSingleFieldModel)
    ModelElementType.LIST_FIELD -> visitor.leave(leader as MutableListFieldModel)
    ModelElementType.SET_FIELD -> visitor.leave(leader as MutableSetFieldModel)
    ModelElementType.PRIMITIVE -> visitor.leave(leader as MutablePrimitiveModel)
    ModelElementType.ALIAS -> visitor.leave(leader as MutableAliasModel)
    ModelElementType.PASSWORD1WAY -> visitor.leave(leader as MutablePassword1wayModel)
    ModelElementType.PASSWORD2WAY -> visitor.leave(leader as MutablePassword2wayModel)
    ModelElementType.ENUMERATION -> visitor.leave(leader as MutableEnumerationModel)
    ModelElementType.ASSOCIATION -> visitor.leave(leader as MutableAssociationModel)
    ModelElementType.ENTITY_KEYS -> visitor.leave(leader as MutableEntityKeysModel)
}