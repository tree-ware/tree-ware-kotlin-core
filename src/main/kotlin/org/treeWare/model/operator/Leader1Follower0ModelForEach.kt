package org.treeWare.model.operator

import org.treeWare.common.traversal.TraversalAction
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
            CursorMoveDirection.Visit -> dispatchVisit(leaderMove.element, visitor) ?: TraversalAction.ABORT_TREE
            CursorMoveDirection.Leave -> {
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
    ModelElementType.MODEL -> {
        visitor.visit(leader as Model<LeaderAux>)
    }
    ModelElementType.ROOT -> {
        visitor.visit(leader as RootModel<LeaderAux>)
    }
    ModelElementType.ENTITY -> {
        visitor.visit(leader as EntityModel<LeaderAux>)
    }
    ModelElementType.PRIMITIVE_FIELD -> {
        visitor.visit(leader as PrimitiveFieldModel<LeaderAux>)
    }
    ModelElementType.ALIAS_FIELD -> {
        visitor.visit(leader as AliasFieldModel<LeaderAux>)
    }
    ModelElementType.ENUMERATION_FIELD -> {
        visitor.visit(leader as EnumerationFieldModel<LeaderAux>)
    }
    ModelElementType.ASSOCIATION_FIELD -> {
        visitor.visit(leader as AssociationFieldModel<LeaderAux>)
    }
    ModelElementType.COMPOSITION_FIELD -> {
        visitor.visit(leader as CompositionFieldModel<LeaderAux>)
    }
    ModelElementType.PRIMITIVE_LIST_FIELD -> {
        visitor.visit(leader as PrimitiveListFieldModel<LeaderAux>)
    }
    ModelElementType.ALIAS_LIST_FIELD -> {
        visitor.visit(leader as AliasListFieldModel<LeaderAux>)
    }
    ModelElementType.ENUMERATION_LIST_FIELD -> {
        visitor.visit(leader as EnumerationListFieldModel<LeaderAux>)
    }
    ModelElementType.ASSOCIATION_LIST_FIELD -> {
        visitor.visit(leader as AssociationListFieldModel<LeaderAux>)
    }
    ModelElementType.COMPOSITION_LIST_FIELD -> {
        visitor.visit(leader as CompositionListFieldModel<LeaderAux>)
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
        ModelElementType.MODEL -> {
            visitor.leave(leader as Model<LeaderAux>)
        }
        ModelElementType.ROOT -> {
            visitor.leave(leader as RootModel<LeaderAux>)
        }
        ModelElementType.ENTITY -> {
            visitor.leave(leader as EntityModel<LeaderAux>)
        }
        ModelElementType.PRIMITIVE_FIELD -> {
            visitor.leave(leader as PrimitiveFieldModel<LeaderAux>)
        }
        ModelElementType.ALIAS_FIELD -> {
            visitor.leave(leader as AliasFieldModel<LeaderAux>)
        }
        ModelElementType.ENUMERATION_FIELD -> {
            visitor.leave(leader as EnumerationFieldModel<LeaderAux>)
        }
        ModelElementType.ASSOCIATION_FIELD -> {
            visitor.leave(leader as AssociationFieldModel<LeaderAux>)
        }
        ModelElementType.COMPOSITION_FIELD -> {
            visitor.leave(leader as CompositionFieldModel<LeaderAux>)
        }
        ModelElementType.PRIMITIVE_LIST_FIELD -> {
            visitor.leave(leader as PrimitiveListFieldModel<LeaderAux>)
        }
        ModelElementType.ALIAS_LIST_FIELD -> {
            visitor.leave(leader as AliasListFieldModel<LeaderAux>)
        }
        ModelElementType.ENUMERATION_LIST_FIELD -> {
            visitor.leave(leader as EnumerationListFieldModel<LeaderAux>)
        }
        ModelElementType.ASSOCIATION_LIST_FIELD -> {
            visitor.leave(leader as AssociationListFieldModel<LeaderAux>)
        }
        ModelElementType.COMPOSITION_LIST_FIELD -> {
            visitor.leave(leader as CompositionListFieldModel<LeaderAux>)
        }
        ModelElementType.ENTITY_KEYS -> {
            visitor.leave(leader as EntityKeysModel<LeaderAux>)
        }
    }
}
