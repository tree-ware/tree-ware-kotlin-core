package org.treeWare.model.traversal

import org.treeWare.model.core.*
import org.treeWare.model.cursor.CursorMoveDirection
import org.treeWare.model.cursor.LeaderManyModelCursor

fun forEach(
    leaders: List<ElementModel>,
    visitor: LeaderManyModelVisitor<TraversalAction>,
    traverseAssociations: Boolean
): TraversalAction {
    val mainMeta = leaders[0].meta ?: throw IllegalArgumentException("Leader does not have meta-model")
    if (!leaders.all { it.meta === mainMeta }) throw IllegalArgumentException("Leaders do not have identical meta-models")
    val leaderCursor = LeaderManyModelCursor(leaders, traverseAssociations)
    var action = TraversalAction.CONTINUE
    while (action != TraversalAction.ABORT_TREE) {
        val leaderMove = leaderCursor.next(action) ?: break
        val nextLeaders = leaderMove.leaders
        action = when (leaderMove.direction) {
            CursorMoveDirection.VISIT -> dispatchVisit(nextLeaders.elementType, nextLeaders.elements, visitor)
                ?: TraversalAction.ABORT_TREE
            CursorMoveDirection.LEAVE -> {
                dispatchLeave(nextLeaders.elementType, nextLeaders.elements, visitor)
                TraversalAction.CONTINUE
            }
        }
    }
    return action
}

fun <Return> dispatchVisit(
    elementType: ModelElementType,
    leaders: List<ElementModel?>,
    visitor: LeaderManyModelVisitor<Return>
): Return? = when (elementType) {
    ModelElementType.ENTITY -> visitor.visitEntity(leaders as List<EntityModel?>)
    ModelElementType.SINGLE_FIELD -> visitor.visitSingleField(leaders as List<SingleFieldModel?>)
    ModelElementType.SET_FIELD -> visitor.visitSetField(leaders as List<SetFieldModel?>)
    ModelElementType.PRIMITIVE -> visitor.visitPrimitive(leaders as List<PrimitiveModel?>)
    ModelElementType.ALIAS -> visitor.visitAlias(leaders as List<AliasModel?>)
    ModelElementType.PASSWORD1WAY -> visitor.visitPassword1way(leaders as List<Password1wayModel?>)
    ModelElementType.PASSWORD2WAY -> visitor.visitPassword2way(leaders as List<Password2wayModel?>)
    ModelElementType.ENUMERATION -> visitor.visitEnumeration(leaders as List<EnumerationModel?>)
    ModelElementType.ASSOCIATION -> visitor.visitAssociation(leaders as List<AssociationModel?>)
}

fun <Return> dispatchLeave(
    elementType: ModelElementType,
    leaders: List<ElementModel?>,
    visitor: LeaderManyModelVisitor<Return>
) = when (elementType) {
    ModelElementType.ENTITY -> visitor.leaveEntity(leaders as List<EntityModel?>)
    ModelElementType.SINGLE_FIELD -> visitor.leaveSingleField(leaders as List<SingleFieldModel?>)
    ModelElementType.SET_FIELD -> visitor.leaveSetField(leaders as List<SetFieldModel?>)
    ModelElementType.PRIMITIVE -> visitor.leavePrimitive(leaders as List<PrimitiveModel?>)
    ModelElementType.ALIAS -> visitor.leaveAlias(leaders as List<AliasModel?>)
    ModelElementType.PASSWORD1WAY -> visitor.leavePassword1way(leaders as List<Password1wayModel?>)
    ModelElementType.PASSWORD2WAY -> visitor.leavePassword2way(leaders as List<Password2wayModel?>)
    ModelElementType.ENUMERATION -> visitor.leaveEnumeration(leaders as List<EnumerationModel?>)
    ModelElementType.ASSOCIATION -> visitor.leaveAssociation(leaders as List<AssociationModel?>)
}