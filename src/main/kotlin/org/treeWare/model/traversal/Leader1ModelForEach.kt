package org.treeWare.model.traversal

import org.treeWare.model.core.*
import org.treeWare.model.cursor.CursorMoveDirection
import org.treeWare.model.cursor.LeaderModelCursor

fun forEach(
    leader: ElementModel,
    visitor: Leader1ModelVisitor<TraversalAction>
): TraversalAction {
    val leaderCursor = LeaderModelCursor(leader)
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
        visitor.visit(leader as MainModel)
    }
    ModelElementType.ROOT -> {
        visitor.visit(leader as RootModel)
    }
    ModelElementType.ENTITY -> {
        visitor.visit(leader as EntityModel)
    }
    ModelElementType.SINGLE_FIELD -> {
        visitor.visit(leader as SingleFieldModel)
    }
    ModelElementType.LIST_FIELD -> {
        visitor.visit(leader as ListFieldModel)
    }
    ModelElementType.SET_FIELD -> {
        visitor.visit(leader as SetFieldModel)
    }
    ModelElementType.PRIMITIVE -> {
        visitor.visit(leader as PrimitiveModel)
    }
    ModelElementType.ALIAS -> {
        visitor.visit(leader as AliasModel)
    }
    ModelElementType.PASSWORD1WAY -> {
        visitor.visit(leader as Password1wayModel)
    }
    ModelElementType.PASSWORD2WAY -> {
        visitor.visit(leader as Password2wayModel)
    }
    ModelElementType.ENUMERATION -> {
        visitor.visit(leader as EnumerationModel)
    }
    ModelElementType.ASSOCIATION -> {
        visitor.visit(leader as AssociationModel)
    }
    ModelElementType.ENTITY_KEYS -> {
        visitor.visit(leader as EntityKeysModel)
    }
}

fun <Return> dispatchLeave(
    leader: ElementModel,
    visitor: Leader1ModelVisitor<Return>
) {
    when (leader.elementType) {
        ModelElementType.MAIN -> {
            visitor.leave(leader as MainModel)
        }
        ModelElementType.ROOT -> {
            visitor.leave(leader as RootModel)
        }
        ModelElementType.ENTITY -> {
            visitor.leave(leader as EntityModel)
        }
        ModelElementType.SINGLE_FIELD -> {
            visitor.leave(leader as SingleFieldModel)
        }
        ModelElementType.LIST_FIELD -> {
            visitor.leave(leader as ListFieldModel)
        }
        ModelElementType.SET_FIELD -> {
            visitor.leave(leader as SetFieldModel)
        }
        ModelElementType.PRIMITIVE -> {
            visitor.leave(leader as PrimitiveModel)
        }
        ModelElementType.ALIAS -> {
            visitor.leave(leader as AliasModel)
        }
        ModelElementType.PASSWORD1WAY -> {
            visitor.leave(leader as Password1wayModel)
        }
        ModelElementType.PASSWORD2WAY -> {
            visitor.leave(leader as Password2wayModel)
        }
        ModelElementType.ENUMERATION -> {
            visitor.leave(leader as EnumerationModel)
        }
        ModelElementType.ASSOCIATION -> {
            visitor.leave(leader as AssociationModel)
        }
        ModelElementType.ENTITY_KEYS -> {
            visitor.leave(leader as EntityKeysModel)
        }
    }
}
