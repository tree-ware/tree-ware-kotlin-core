package org.treeWare.model.operator

import org.treeWare.model.core.*
import org.treeWare.model.cursor.CursorMoveDirection
import org.treeWare.model.cursor.FollowerModelCursor
import org.treeWare.model.cursor.LeaderModelCursor
import org.treeWare.common.traversal.TraversalAction

suspend fun <LeaderAux, Follower1Aux, Follower2Aux> forEach(
    leader: ElementModel<LeaderAux>,
    follower1: ElementModel<Follower1Aux>,
    follower2: ElementModel<Follower2Aux>,
    visitor: Leader1Follower2ModelVisitor<LeaderAux, Follower1Aux, Follower2Aux, TraversalAction>
): TraversalAction {
    val leaderCursor = LeaderModelCursor(leader)
    val follower1Cursor = FollowerModelCursor<LeaderAux, Follower1Aux>(follower1)
    val follower2Cursor = FollowerModelCursor<LeaderAux, Follower2Aux>(follower2)
    var action = TraversalAction.CONTINUE
    while (action != TraversalAction.ABORT_TREE) {
        val leaderMove = leaderCursor.next(action) ?: break
        val follower1Move = follower1Cursor.follow(leaderMove)
        val follower2Move = follower2Cursor.follow(leaderMove)
        assert(follower1Move != null)
        if (follower1Move == null) break
        assert(follower2Move != null)
        if (follower2Move == null) break
        action = when (leaderMove.direction) {
            CursorMoveDirection.Visit -> dispatchVisit(
                leaderMove.element,
                follower1Move.element,
                follower2Move.element,
                visitor
            )
            CursorMoveDirection.Leave -> {
                dispatchLeave(leaderMove.element, follower1Move.element, follower2Move.element, visitor)
                TraversalAction.CONTINUE
            }
        }
    }
    return action
}

// TODO(deepak-nulu): how do we ensure these methods are updated when new element types are added?
// There have been community requests for sealed interfaces in Kotlin, but they may not be implemented
// Luckily new model elements are rare.
suspend fun <LeaderAux, Follower1Aux, Follower2Aux> dispatchVisit(
    leader: ElementModel<LeaderAux>,
    follower1: ElementModel<Follower1Aux>?,
    follower2: ElementModel<Follower2Aux>?,
    visitor: Leader1Follower2ModelVisitor<LeaderAux, Follower1Aux, Follower2Aux, TraversalAction>
): TraversalAction = when (leader) {
    is Model<LeaderAux> -> {
        assert(follower1 is Model<Follower1Aux>?)
        assert(follower2 is Model<Follower2Aux>?)
        if (follower1 is Model<Follower1Aux>? && follower2 is Model<Follower2Aux>) visitor.visit(
            leader,
            follower1,
            follower2
        ) else TraversalAction.ABORT_TREE
    }
    is RootModel<LeaderAux> -> {
        assert(follower1 is RootModel<Follower1Aux>?)
        assert(follower2 is RootModel<Follower2Aux>?)
        if (follower1 is RootModel<Follower1Aux>? && follower2 is RootModel<Follower2Aux>?) visitor.visit(
            leader,
            follower1,
            follower2
        ) else TraversalAction.ABORT_TREE
    }
    is EntityModel<LeaderAux> -> {
        assert(follower1 is EntityModel<Follower1Aux>?)
        assert(follower2 is EntityModel<Follower2Aux>?)
        if (follower1 is EntityModel<Follower1Aux>? && follower2 is EntityModel<Follower2Aux>?) visitor.visit(
            leader,
            follower1,
            follower2
        ) else TraversalAction.ABORT_TREE
    }
    is PrimitiveFieldModel<LeaderAux> -> {
        assert(follower1 is PrimitiveFieldModel<Follower1Aux>?)
        assert(follower2 is PrimitiveFieldModel<Follower2Aux>?)
        if (follower1 is PrimitiveFieldModel<Follower1Aux>? && follower2 is PrimitiveFieldModel<Follower2Aux>?) visitor.visit(
            leader,
            follower1,
            follower2
        ) else TraversalAction.ABORT_TREE
    }
    is AliasFieldModel<LeaderAux> -> {
        assert(follower1 is AliasFieldModel<Follower1Aux>?)
        assert(follower2 is AliasFieldModel<Follower2Aux>?)
        if (follower1 is AliasFieldModel<Follower1Aux>? && follower2 is AliasFieldModel<Follower2Aux>?) visitor.visit(
            leader,
            follower1,
            follower2
        ) else TraversalAction.ABORT_TREE
    }
    is EnumerationFieldModel<LeaderAux> -> {
        assert(follower1 is EnumerationFieldModel<Follower1Aux>?)
        assert(follower2 is EnumerationFieldModel<Follower2Aux>?)
        if (follower1 is EnumerationFieldModel<Follower1Aux>? && follower2 is EnumerationFieldModel<Follower2Aux>?) visitor.visit(
            leader,
            follower1,
            follower2
        ) else TraversalAction.ABORT_TREE
    }
    is AssociationFieldModel<LeaderAux> -> {
        assert(follower1 is AssociationFieldModel<Follower1Aux>?)
        assert(follower2 is AssociationFieldModel<Follower2Aux>?)
        if (follower1 is AssociationFieldModel<Follower1Aux>? && follower2 is AssociationFieldModel<Follower2Aux>?) visitor.visit(
            leader,
            follower1,
            follower2
        ) else TraversalAction.ABORT_TREE
    }
    is CompositionFieldModel<LeaderAux> -> {
        assert(follower1 is CompositionFieldModel<Follower1Aux>?)
        assert(follower2 is CompositionFieldModel<Follower2Aux>?)
        if (follower1 is CompositionFieldModel<Follower1Aux>? && follower2 is CompositionFieldModel<Follower2Aux>?) visitor.visit(
            leader,
            follower1,
            follower2
        ) else TraversalAction.ABORT_TREE
    }
    is PrimitiveListFieldModel<LeaderAux> -> {
        assert(follower1 is PrimitiveListFieldModel<Follower1Aux>?)
        assert(follower2 is PrimitiveListFieldModel<Follower2Aux>?)
        if (follower1 is PrimitiveListFieldModel<Follower1Aux>? && follower2 is PrimitiveListFieldModel<Follower2Aux>?) visitor.visit(
            leader,
            follower1,
            follower2
        ) else TraversalAction.ABORT_TREE
    }
    is AliasListFieldModel<LeaderAux> -> {
        assert(follower1 is AliasListFieldModel<Follower1Aux>?)
        assert(follower2 is AliasListFieldModel<Follower2Aux>?)
        if (follower1 is AliasListFieldModel<Follower1Aux>? && follower2 is AliasListFieldModel<Follower2Aux>?) visitor.visit(
            leader,
            follower1,
            follower2
        ) else TraversalAction.ABORT_TREE
    }
    is EnumerationListFieldModel<LeaderAux> -> {
        assert(follower1 is EnumerationListFieldModel<Follower1Aux>?)
        assert(follower2 is EnumerationListFieldModel<Follower2Aux>?)
        if (follower1 is EnumerationListFieldModel<Follower1Aux>? && follower2 is EnumerationListFieldModel<Follower2Aux>?) visitor.visit(
            leader,
            follower1,
            follower2
        ) else TraversalAction.ABORT_TREE
    }
    is AssociationListFieldModel<LeaderAux> -> {
        assert(follower1 is AssociationListFieldModel<Follower1Aux>?)
        assert(follower2 is AssociationListFieldModel<Follower2Aux>?)
        if (follower1 is AssociationListFieldModel<Follower1Aux>? && follower2 is AssociationListFieldModel<Follower2Aux>?) visitor.visit(
            leader,
            follower1,
            follower2
        ) else TraversalAction.ABORT_TREE
    }
    is CompositionListFieldModel<LeaderAux> -> {
        assert(follower1 is CompositionListFieldModel<Follower1Aux>?)
        assert(follower2 is CompositionListFieldModel<Follower2Aux>?)
        if (follower1 is CompositionListFieldModel<Follower1Aux>? && follower2 is CompositionListFieldModel<Follower2Aux>?) visitor.visit(
            leader,
            follower1,
            follower2
        ) else TraversalAction.ABORT_TREE
    }
    else -> {
        assert(false) { "Unknown element type: $leader" }
        TraversalAction.ABORT_TREE
    }
}

suspend fun <LeaderAux, Follower1Aux, Follower2Aux> dispatchLeave(
    leader: ElementModel<LeaderAux>,
    follower1: ElementModel<Follower1Aux>?,
    follower2: ElementModel<Follower2Aux>?,
    visitor: Leader1Follower2ModelVisitor<LeaderAux, Follower1Aux, Follower2Aux, TraversalAction>
) {
    when (leader) {
        is Model<LeaderAux> -> {
            assert(follower1 is Model<Follower1Aux>?)
            assert(follower2 is Model<Follower2Aux>?)
            if (follower1 is Model<Follower1Aux>? && follower2 is Model<Follower2Aux>) visitor.leave(
                leader,
                follower1,
                follower2
            )
        }
        is RootModel<LeaderAux> -> {
            assert(follower1 is RootModel<Follower1Aux>?)
            assert(follower2 is RootModel<Follower2Aux>?)
            if (follower1 is RootModel<Follower1Aux>? && follower2 is RootModel<Follower2Aux>?) visitor.leave(
                leader,
                follower1,
                follower2
            )
        }
        is EntityModel<LeaderAux> -> {
            assert(follower1 is EntityModel<Follower1Aux>?)
            assert(follower2 is EntityModel<Follower2Aux>?)
            if (follower1 is EntityModel<Follower1Aux>? && follower2 is EntityModel<Follower2Aux>?) visitor.leave(
                leader,
                follower1,
                follower2
            )
        }
        is PrimitiveFieldModel<LeaderAux> -> {
            assert(follower1 is PrimitiveFieldModel<Follower1Aux>?)
            assert(follower2 is PrimitiveFieldModel<Follower2Aux>?)
            if (follower1 is PrimitiveFieldModel<Follower1Aux>? && follower2 is PrimitiveFieldModel<Follower2Aux>?) visitor.leave(
                leader,
                follower1,
                follower2
            )
        }
        is AliasFieldModel<LeaderAux> -> {
            assert(follower1 is AliasFieldModel<Follower1Aux>?)
            assert(follower2 is AliasFieldModel<Follower2Aux>?)
            if (follower1 is AliasFieldModel<Follower1Aux>? && follower2 is AliasFieldModel<Follower2Aux>?) visitor.leave(
                leader,
                follower1,
                follower2
            )
        }
        is EnumerationFieldModel<LeaderAux> -> {
            assert(follower1 is EnumerationFieldModel<Follower1Aux>?)
            assert(follower2 is EnumerationFieldModel<Follower2Aux>?)
            if (follower1 is EnumerationFieldModel<Follower1Aux>? && follower2 is EnumerationFieldModel<Follower2Aux>?) visitor.leave(
                leader,
                follower1,
                follower2
            )
        }
        is AssociationFieldModel<LeaderAux> -> {
            assert(follower1 is AssociationFieldModel<Follower1Aux>?)
            assert(follower2 is AssociationFieldModel<Follower2Aux>?)
            if (follower1 is AssociationFieldModel<Follower1Aux>? && follower2 is AssociationFieldModel<Follower2Aux>?) visitor.leave(
                leader,
                follower1,
                follower2
            )
        }
        is CompositionFieldModel<LeaderAux> -> {
            assert(follower1 is CompositionFieldModel<Follower1Aux>?)
            assert(follower2 is CompositionFieldModel<Follower2Aux>?)
            if (follower1 is CompositionFieldModel<Follower1Aux>? && follower2 is CompositionFieldModel<Follower2Aux>?) visitor.leave(
                leader,
                follower1,
                follower2
            )
        }
        is PrimitiveListFieldModel<LeaderAux> -> {
            assert(follower1 is PrimitiveListFieldModel<Follower1Aux>?)
            assert(follower2 is PrimitiveListFieldModel<Follower2Aux>?)
            if (follower1 is PrimitiveListFieldModel<Follower1Aux>? && follower2 is PrimitiveListFieldModel<Follower2Aux>?) visitor.leave(
                leader,
                follower1,
                follower2
            )
        }
        is AliasListFieldModel<LeaderAux> -> {
            assert(follower1 is AliasListFieldModel<Follower1Aux>?)
            assert(follower2 is AliasListFieldModel<Follower2Aux>?)
            if (follower1 is AliasListFieldModel<Follower1Aux>? && follower2 is AliasListFieldModel<Follower2Aux>?) visitor.leave(
                leader,
                follower1,
                follower2
            )
        }
        is EnumerationListFieldModel<LeaderAux> -> {
            assert(follower1 is EnumerationListFieldModel<Follower1Aux>?)
            assert(follower2 is EnumerationListFieldModel<Follower2Aux>?)
            if (follower1 is EnumerationListFieldModel<Follower1Aux>? && follower2 is EnumerationListFieldModel<Follower2Aux>?) visitor.leave(
                leader,
                follower1,
                follower2
            )
        }
        is AssociationListFieldModel<LeaderAux> -> {
            assert(follower1 is AssociationListFieldModel<Follower1Aux>?)
            assert(follower2 is AssociationListFieldModel<Follower2Aux>?)
            if (follower1 is AssociationListFieldModel<Follower1Aux>? && follower2 is AssociationListFieldModel<Follower2Aux>?) visitor.leave(
                leader,
                follower1,
                follower2
            )
        }
        is CompositionListFieldModel<LeaderAux> -> {
            assert(follower1 is CompositionListFieldModel<Follower1Aux>?)
            assert(follower2 is CompositionListFieldModel<Follower2Aux>?)
            if (follower1 is CompositionListFieldModel<Follower1Aux>? && follower2 is CompositionListFieldModel<Follower2Aux>?) visitor.leave(
                leader,
                follower1,
                follower2
            )
        }
        else -> {
            assert(false) { "Unknown element type: $leader" }
            TraversalAction.ABORT_TREE
        }
    }
}