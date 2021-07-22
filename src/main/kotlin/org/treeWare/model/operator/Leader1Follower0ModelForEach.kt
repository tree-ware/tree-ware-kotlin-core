package org.treeWare.model.operator

import org.treeWare.model.cursor.CursorMoveDirection
import org.treeWare.model.cursor.LeaderModelCursor
import org.treeWare.common.traversal.TraversalAction
import org.treeWare.model.core.*

fun <LeaderAux> forEach(
    leader: ElementModel<LeaderAux>,
    visitor: Leader1Follower0ModelVisitor<LeaderAux, TraversalAction>
): TraversalAction {
    val leaderCursor = LeaderModelCursor(leader)
    var action = TraversalAction.CONTINUE
    while (action != TraversalAction.ABORT_TREE) {
        val leaderMove = leaderCursor.next(action) ?: break
        action = when (leaderMove.direction) {
            CursorMoveDirection.Visit -> dispatchVisit(leaderMove.element, visitor)
            CursorMoveDirection.Leave -> {
                dispatchLeave(leaderMove.element, visitor)
                TraversalAction.CONTINUE
            }
        }
    }
    return action
}

fun <LeaderAux> dispatchVisit(
    leader: ElementModel<LeaderAux>,
    visitor: Leader1Follower0ModelVisitor<LeaderAux, TraversalAction>
): TraversalAction = when (leader) {
    is Model<LeaderAux> -> {
        visitor.visit(leader)
    }
    is RootModel<LeaderAux> -> {
        visitor.visit(leader)
    }
    is EntityModel<LeaderAux> -> {
        visitor.visit(leader)
    }
    is PrimitiveFieldModel<LeaderAux> -> {
        visitor.visit(leader)
    }
    is AliasFieldModel<LeaderAux> -> {
        visitor.visit(leader)
    }
    is EnumerationFieldModel<LeaderAux> -> {
        visitor.visit(leader)
    }
    is AssociationFieldModel<LeaderAux> -> {
        visitor.visit(leader)
    }
    is CompositionFieldModel<LeaderAux> -> {
        visitor.visit(leader)
    }
    is PrimitiveListFieldModel<LeaderAux> -> {
        visitor.visit(leader)
    }
    is AliasListFieldModel<LeaderAux> -> {
        visitor.visit(leader)
    }
    is EnumerationListFieldModel<LeaderAux> -> {
        visitor.visit(leader)
    }
    is AssociationListFieldModel<LeaderAux> -> {
        visitor.visit(leader)
    }
    is CompositionListFieldModel<LeaderAux> -> {
        visitor.visit(leader)
    }
    is EntityKeysModel<LeaderAux> -> {
        visitor.visit(leader)
    }
    else -> {
        assert(false) { "Unknown element type: $leader" }
        TraversalAction.ABORT_TREE
    }
}

fun <LeaderAux> dispatchLeave(
    leader: ElementModel<LeaderAux>,
    visitor: Leader1Follower0ModelVisitor<LeaderAux, TraversalAction>
) {
    when (leader) {
        is Model<LeaderAux> -> {
            visitor.leave(leader)
        }
        is RootModel<LeaderAux> -> {
            visitor.leave(leader)
        }
        is EntityModel<LeaderAux> -> {
            visitor.leave(leader)
        }
        is PrimitiveFieldModel<LeaderAux> -> {
            visitor.leave(leader)
        }
        is AliasFieldModel<LeaderAux> -> {
            visitor.leave(leader)
        }
        is EnumerationFieldModel<LeaderAux> -> {
            visitor.leave(leader)
        }
        is AssociationFieldModel<LeaderAux> -> {
            visitor.leave(leader)
        }
        is CompositionFieldModel<LeaderAux> -> {
            visitor.leave(leader)
        }
        is PrimitiveListFieldModel<LeaderAux> -> {
            visitor.leave(leader)
        }
        is AliasListFieldModel<LeaderAux> -> {
            visitor.leave(leader)
        }
        is EnumerationListFieldModel<LeaderAux> -> {
            visitor.leave(leader)
        }
        is AssociationListFieldModel<LeaderAux> -> {
            visitor.leave(leader)
        }
        is CompositionListFieldModel<LeaderAux> -> {
            visitor.leave(leader)
        }
        is EntityKeysModel<LeaderAux> -> {
            visitor.leave(leader)
        }
        else -> {
            assert(false) { "Unknown element type: $leader" }
            TraversalAction.ABORT_TREE
        }
    }
}
