package org.treeWare.model.operator

import org.treeWare.model.core.*
import org.treeWare.model.cursor.CursorMoveDirection
import org.treeWare.model.cursor.LeaderModelCursor

fun <LeaderAux> forEach(
    leader: ElementModel<LeaderAux>,
    visitor: Leader1Follower0ModelVisitor<LeaderAux, TraversalAction>
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

fun <LeaderAux, Return> dispatchVisit(
    leader: ElementModel<LeaderAux>,
    visitor: Leader1Follower0ModelVisitor<LeaderAux, Return>
): Return? = when (leader.elementType) {
    ModelElementType.MAIN -> {
        visitor.visit(leader as MainModel<LeaderAux>)
    }
    ModelElementType.ROOT -> {
        visitor.visit(leader as RootModel<LeaderAux>)
    }
    ModelElementType.ENTITY -> {
        visitor.visit(leader as EntityModel<LeaderAux>)
    }
    ModelElementType.SINGLE_FIELD -> {
        visitor.visit(leader as SingleFieldModel<LeaderAux>)
    }
    ModelElementType.LIST_FIELD -> {
        visitor.visit(leader as ListFieldModel<LeaderAux>)
    }
    ModelElementType.PRIMITIVE -> {
        visitor.visit(leader as PrimitiveModel<LeaderAux>)
    }
    ModelElementType.ALIAS -> {
        visitor.visit(leader as AliasModel<LeaderAux>)
    }
    ModelElementType.PASSWORD1WAY -> {
        visitor.visit(leader as Password1wayModel<LeaderAux>)
    }
    ModelElementType.PASSWORD2WAY -> {
        visitor.visit(leader as Password2wayModel<LeaderAux>)
    }
    ModelElementType.ENUMERATION -> {
        visitor.visit(leader as EnumerationModel<LeaderAux>)
    }
    ModelElementType.ASSOCIATION -> {
        visitor.visit(leader as AssociationModel<LeaderAux>)
    }
    ModelElementType.ENTITY_KEYS -> {
        visitor.visit(leader as EntityKeysModel<LeaderAux>)
    }
}

fun <LeaderAux, Return> dispatchLeave(
    leader: ElementModel<LeaderAux>,
    visitor: Leader1Follower0ModelVisitor<LeaderAux, Return>
) {
    when (leader.elementType) {
        ModelElementType.MAIN -> {
            visitor.leave(leader as MainModel<LeaderAux>)
        }
        ModelElementType.ROOT -> {
            visitor.leave(leader as RootModel<LeaderAux>)
        }
        ModelElementType.ENTITY -> {
            visitor.leave(leader as EntityModel<LeaderAux>)
        }
        ModelElementType.SINGLE_FIELD -> {
            visitor.leave(leader as SingleFieldModel<LeaderAux>)
        }
        ModelElementType.LIST_FIELD -> {
            visitor.leave(leader as ListFieldModel<LeaderAux>)
        }
        ModelElementType.PRIMITIVE -> {
            visitor.leave(leader as PrimitiveModel<LeaderAux>)
        }
        ModelElementType.ALIAS -> {
            visitor.leave(leader as AliasModel<LeaderAux>)
        }
        ModelElementType.PASSWORD1WAY -> {
            visitor.leave(leader as Password1wayModel<LeaderAux>)
        }
        ModelElementType.PASSWORD2WAY -> {
            visitor.leave(leader as Password2wayModel<LeaderAux>)
        }
        ModelElementType.ENUMERATION -> {
            visitor.leave(leader as EnumerationModel<LeaderAux>)
        }
        ModelElementType.ASSOCIATION -> {
            visitor.leave(leader as AssociationModel<LeaderAux>)
        }
        ModelElementType.ENTITY_KEYS -> {
            visitor.leave(leader as EntityKeysModel<LeaderAux>)
        }
    }
}
