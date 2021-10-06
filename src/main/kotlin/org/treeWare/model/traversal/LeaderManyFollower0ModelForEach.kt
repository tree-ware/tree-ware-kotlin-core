package org.treeWare.model.traversal

import org.treeWare.model.core.*
import org.treeWare.model.cursor.CursorMoveDirection
import org.treeWare.model.cursor.LeaderManyModelCursor

suspend fun <LeaderAux> forEach(
    leaders: List<ElementModel<LeaderAux>>,
    visitor: LeaderManyFollower0ModelVisitor<LeaderAux, TraversalAction>
): TraversalAction {
    val leaderCursor = LeaderManyModelCursor(leaders)
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

suspend fun <LeaderAux, Return> dispatchVisit(
    elementType: ModelElementType,
    leaders: List<ElementModel<LeaderAux>?>,
    visitor: LeaderManyFollower0ModelVisitor<LeaderAux, Return>
): Return? = when (elementType) {
    ModelElementType.MAIN -> visitor.visitMain(leaders as List<MainModel<LeaderAux>?>)
    ModelElementType.ROOT -> visitor.visitRoot(leaders as List<RootModel<LeaderAux>?>)
    ModelElementType.ENTITY -> visitor.visitEntity(leaders as List<EntityModel<LeaderAux>?>)
    ModelElementType.SINGLE_FIELD -> visitor.visitSingleField(leaders as List<SingleFieldModel<LeaderAux>?>)
    ModelElementType.LIST_FIELD -> visitor.visitListField(leaders as List<ListFieldModel<LeaderAux>?>)
    ModelElementType.SET_FIELD -> visitor.visitSetField(leaders as List<SetFieldModel<LeaderAux>?>)
    ModelElementType.PRIMITIVE -> visitor.visitPrimitive(leaders as List<PrimitiveModel<LeaderAux>?>)
    ModelElementType.ALIAS -> visitor.visitAlias(leaders as List<AliasModel<LeaderAux>?>)
    ModelElementType.PASSWORD1WAY -> visitor.visitPassword1way(leaders as List<Password1wayModel<LeaderAux>?>)
    ModelElementType.PASSWORD2WAY -> visitor.visitPassword2way(leaders as List<Password2wayModel<LeaderAux>?>)
    ModelElementType.ENUMERATION -> visitor.visitEnumeration(leaders as List<EnumerationModel<LeaderAux>?>)
    ModelElementType.ASSOCIATION -> visitor.visitAssociation(leaders as List<AssociationModel<LeaderAux>?>)
    else -> throw UnsupportedOperationException("Dispatching to unsupported model element type: $elementType")
}

suspend fun <LeaderAux, Return> dispatchLeave(
    elementType: ModelElementType,
    leaders: List<ElementModel<LeaderAux>?>,
    visitor: LeaderManyFollower0ModelVisitor<LeaderAux, Return>
) = when (elementType) {
    ModelElementType.MAIN -> visitor.leaveMain(leaders as List<MainModel<LeaderAux>?>)
    ModelElementType.ROOT -> visitor.leaveRoot(leaders as List<RootModel<LeaderAux>?>)
    ModelElementType.ENTITY -> visitor.leaveEntity(leaders as List<EntityModel<LeaderAux>?>)
    ModelElementType.SINGLE_FIELD -> visitor.leaveSingleField(leaders as List<SingleFieldModel<LeaderAux>?>)
    ModelElementType.LIST_FIELD -> visitor.leaveListField(leaders as List<ListFieldModel<LeaderAux>?>)
    ModelElementType.SET_FIELD -> visitor.leaveSetField(leaders as List<SetFieldModel<LeaderAux>?>)
    ModelElementType.PRIMITIVE -> visitor.leavePrimitive(leaders as List<PrimitiveModel<LeaderAux>?>)
    ModelElementType.ALIAS -> visitor.leaveAlias(leaders as List<AliasModel<LeaderAux>?>)
    ModelElementType.PASSWORD1WAY -> visitor.leavePassword1way(leaders as List<Password1wayModel<LeaderAux>?>)
    ModelElementType.PASSWORD2WAY -> visitor.leavePassword2way(leaders as List<Password2wayModel<LeaderAux>?>)
    ModelElementType.ENUMERATION -> visitor.leaveEnumeration(leaders as List<EnumerationModel<LeaderAux>?>)
    ModelElementType.ASSOCIATION -> visitor.leaveAssociation(leaders as List<AssociationModel<LeaderAux>?>)
    else -> throw UnsupportedOperationException("Dispatching to unsupported model element type: $elementType")
}
