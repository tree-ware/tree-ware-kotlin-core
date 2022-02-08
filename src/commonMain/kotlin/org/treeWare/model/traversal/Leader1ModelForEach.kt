package org.treeWare.model.traversal

import org.treeWare.model.core.*
import org.treeWare.model.cursor.CursorMoveDirection
import org.treeWare.model.cursor.Leader1ModelCursor

fun forEach(
    leader: ElementModel,
    visitor: Leader1ModelVisitor<TraversalAction>
): TraversalAction {
    val leaderCursor = Leader1ModelCursor(leader)
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
    leader: ElementModel,
    visitor: Leader1ModelVisitor<Return>
): Return? = when (leader.elementType) {
    ModelElementType.MAIN -> {
        visitor.visitMain(leader as MainModel)
    }
    ModelElementType.ENTITY -> {
        visitor.visitEntity(leader as EntityModel)
    }
    ModelElementType.SINGLE_FIELD -> {
        visitor.visitSingleField(leader as SingleFieldModel)
    }
    ModelElementType.LIST_FIELD -> {
        visitor.visitListField(leader as ListFieldModel)
    }
    ModelElementType.SET_FIELD -> {
        visitor.visitSetField(leader as SetFieldModel)
    }
    ModelElementType.PRIMITIVE -> {
        visitor.visitPrimitive(leader as PrimitiveModel)
    }
    ModelElementType.ALIAS -> {
        visitor.visitAlias(leader as AliasModel)
    }
    ModelElementType.PASSWORD1WAY -> {
        visitor.visitPassword1way(leader as Password1wayModel)
    }
    ModelElementType.PASSWORD2WAY -> {
        visitor.visitPassword2way(leader as Password2wayModel)
    }
    ModelElementType.ENUMERATION -> {
        visitor.visitEnumeration(leader as EnumerationModel)
    }
    ModelElementType.ASSOCIATION -> {
        visitor.visitAssociation(leader as AssociationModel)
    }
    ModelElementType.ENTITY_KEYS -> {
        visitor.visitEntityKeys(leader as EntityKeysModel)
    }
}

fun <Return> dispatchLeave(
    leader: ElementModel,
    visitor: Leader1ModelVisitor<Return>
) {
    when (leader.elementType) {
        ModelElementType.MAIN -> {
            visitor.leaveMain(leader as MainModel)
        }
        ModelElementType.ENTITY -> {
            visitor.leaveEntity(leader as EntityModel)
        }
        ModelElementType.SINGLE_FIELD -> {
            visitor.leaveSingleField(leader as SingleFieldModel)
        }
        ModelElementType.LIST_FIELD -> {
            visitor.leaveListField(leader as ListFieldModel)
        }
        ModelElementType.SET_FIELD -> {
            visitor.leaveSetField(leader as SetFieldModel)
        }
        ModelElementType.PRIMITIVE -> {
            visitor.leavePrimitive(leader as PrimitiveModel)
        }
        ModelElementType.ALIAS -> {
            visitor.leaveAlias(leader as AliasModel)
        }
        ModelElementType.PASSWORD1WAY -> {
            visitor.leavePassword1way(leader as Password1wayModel)
        }
        ModelElementType.PASSWORD2WAY -> {
            visitor.leavePassword2way(leader as Password2wayModel)
        }
        ModelElementType.ENUMERATION -> {
            visitor.leaveEnumeration(leader as EnumerationModel)
        }
        ModelElementType.ASSOCIATION -> {
            visitor.leaveAssociation(leader as AssociationModel)
        }
        ModelElementType.ENTITY_KEYS -> {
            visitor.leaveEntityKeys(leader as EntityKeysModel)
        }
    }
}