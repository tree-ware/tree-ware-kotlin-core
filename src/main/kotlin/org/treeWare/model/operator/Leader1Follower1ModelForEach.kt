package org.treeWare.model.operator

import org.treeWare.model.core.*
import org.treeWare.model.cursor.CursorMoveDirection
import org.treeWare.model.cursor.FollowerModelCursor
import org.treeWare.model.cursor.LeaderModelCursor
import org.treeWare.common.traversal.TraversalAction

fun <LeaderAux, FollowerAux> forEach(
    leader: ElementModel<LeaderAux>,
    follower: ElementModel<FollowerAux>,
    visitor: Leader1Follower1ModelVisitor<LeaderAux, FollowerAux, TraversalAction>
): TraversalAction {
    val leaderCursor = LeaderModelCursor(leader)
    val followerCursor = FollowerModelCursor<LeaderAux, FollowerAux>(follower)
    var action = TraversalAction.CONTINUE
    while (action != TraversalAction.ABORT_TREE) {
        val leaderMove = leaderCursor.next(action) ?: break
        val followerMove = followerCursor.follow(leaderMove)
        assert(followerMove != null)
        if (followerMove == null) break
        action = when (leaderMove.direction) {
            CursorMoveDirection.Visit -> dispatchVisit(leaderMove.element, followerMove.element, visitor)
            CursorMoveDirection.Leave -> {
                dispatchLeave(leaderMove.element, followerMove.element, visitor)
                TraversalAction.CONTINUE
            }
        }
    }
    return action
}

fun <LeaderAux, FollowerAux> dispatchVisit(
    leader: ElementModel<LeaderAux>,
    follower: ElementModel<FollowerAux>?,
    visitor: Leader1Follower1ModelVisitor<LeaderAux, FollowerAux, TraversalAction>
): TraversalAction = when (leader) {
    is Model<LeaderAux> -> {
        assert(follower is Model<FollowerAux>?)
        if (follower is Model<FollowerAux>?) visitor.visit(leader, follower)
        else TraversalAction.ABORT_TREE
    }
    is RootModel<LeaderAux> -> {
        assert(follower is RootModel<FollowerAux>?)
        if (follower is RootModel<FollowerAux>?) visitor.visit(leader, follower)
        else TraversalAction.ABORT_TREE
    }
    is EntityModel<LeaderAux> -> {
        assert(follower is EntityModel<FollowerAux>?)
        if (follower is EntityModel<FollowerAux>?) visitor.visit(leader, follower)
        else TraversalAction.ABORT_TREE
    }
    is PrimitiveFieldModel<LeaderAux> -> {
        assert(follower is PrimitiveFieldModel<FollowerAux>?)
        if (follower is PrimitiveFieldModel<FollowerAux>?) visitor.visit(leader, follower)
        else TraversalAction.ABORT_TREE
    }
    is AliasFieldModel<LeaderAux> -> {
        assert(follower is AliasFieldModel<FollowerAux>?)
        if (follower is AliasFieldModel<FollowerAux>?) visitor.visit(leader, follower)
        else TraversalAction.ABORT_TREE
    }
    is EnumerationFieldModel<LeaderAux> -> {
        assert(follower is EnumerationFieldModel<FollowerAux>?)
        if (follower is EnumerationFieldModel<FollowerAux>?) visitor.visit(leader, follower)
        else TraversalAction.ABORT_TREE
    }
    is AssociationFieldModel<LeaderAux> -> {
        assert(follower is AssociationFieldModel<FollowerAux>?)
        if (follower is AssociationFieldModel<FollowerAux>?) visitor.visit(leader, follower)
        else TraversalAction.ABORT_TREE
    }
    is CompositionFieldModel<LeaderAux> -> {
        assert(follower is CompositionFieldModel<FollowerAux>?)
        if (follower is CompositionFieldModel<FollowerAux>?) visitor.visit(leader, follower)
        else TraversalAction.ABORT_TREE
    }
    is PrimitiveListFieldModel<LeaderAux> -> {
        assert(follower is PrimitiveListFieldModel<FollowerAux>?)
        if (follower is PrimitiveListFieldModel<FollowerAux>?) visitor.visit(leader, follower)
        else TraversalAction.ABORT_TREE
    }
    is AliasListFieldModel<LeaderAux> -> {
        assert(follower is AliasListFieldModel<FollowerAux>?)
        if (follower is AliasListFieldModel<FollowerAux>?) visitor.visit(leader, follower)
        else TraversalAction.ABORT_TREE
    }
    is EnumerationListFieldModel<LeaderAux> -> {
        assert(follower is EnumerationListFieldModel<FollowerAux>?)
        if (follower is EnumerationListFieldModel<FollowerAux>?) visitor.visit(leader, follower)
        else TraversalAction.ABORT_TREE
    }
    is AssociationListFieldModel<LeaderAux> -> {
        assert(follower is AssociationListFieldModel<FollowerAux>?)
        if (follower is AssociationListFieldModel<FollowerAux>?) visitor.visit(leader, follower)
        else TraversalAction.ABORT_TREE
    }
    is CompositionListFieldModel<LeaderAux> -> {
        assert(follower is CompositionListFieldModel<FollowerAux>?)
        if (follower is CompositionListFieldModel<FollowerAux>?) visitor.visit(leader, follower)
        else TraversalAction.ABORT_TREE
    }
    is EntityKeysModel<LeaderAux> -> {
        assert(follower is EntityKeysModel<FollowerAux>?)
        if (follower is EntityKeysModel<FollowerAux>?) visitor.visit(leader, follower)
        else TraversalAction.ABORT_TREE
    }
    else -> {
        assert(false) { "Unknown element type: $leader" }
        TraversalAction.ABORT_TREE
    }
}

fun <LeaderAux, FollowerAux> dispatchLeave(
    leader: ElementModel<LeaderAux>,
    follower: ElementModel<FollowerAux>?,
    visitor: Leader1Follower1ModelVisitor<LeaderAux, FollowerAux, TraversalAction>
) {
    when (leader) {
        is Model<LeaderAux> -> {
            assert(follower is Model<FollowerAux>?)
            if (follower is Model<FollowerAux>?) visitor.leave(leader, follower)
        }
        is RootModel<LeaderAux> -> {
            assert(follower is RootModel<FollowerAux>?)
            if (follower is RootModel<FollowerAux>?) visitor.leave(leader, follower)
        }
        is EntityModel<LeaderAux> -> {
            assert(follower is EntityModel<FollowerAux>?)
            if (follower is EntityModel<FollowerAux>?) visitor.leave(leader, follower)
        }
        is PrimitiveFieldModel<LeaderAux> -> {
            assert(follower is PrimitiveFieldModel<FollowerAux>?)
            if (follower is PrimitiveFieldModel<FollowerAux>?) visitor.leave(leader, follower)
        }
        is AliasFieldModel<LeaderAux> -> {
            assert(follower is AliasFieldModel<FollowerAux>?)
            if (follower is AliasFieldModel<FollowerAux>?) visitor.leave(leader, follower)
        }
        is EnumerationFieldModel<LeaderAux> -> {
            assert(follower is EnumerationFieldModel<FollowerAux>?)
            if (follower is EnumerationFieldModel<FollowerAux>?) visitor.leave(leader, follower)
        }
        is AssociationFieldModel<LeaderAux> -> {
            assert(follower is AssociationFieldModel<FollowerAux>?)
            if (follower is AssociationFieldModel<FollowerAux>?) visitor.leave(leader, follower)
        }
        is CompositionFieldModel<LeaderAux> -> {
            assert(follower is CompositionFieldModel<FollowerAux>?)
            if (follower is CompositionFieldModel<FollowerAux>?) visitor.leave(leader, follower)
        }
        is PrimitiveListFieldModel<LeaderAux> -> {
            assert(follower is PrimitiveListFieldModel<FollowerAux>?)
            if (follower is PrimitiveListFieldModel<FollowerAux>?) visitor.leave(leader, follower)
        }
        is AliasListFieldModel<LeaderAux> -> {
            assert(follower is AliasListFieldModel<FollowerAux>?)
            if (follower is AliasListFieldModel<FollowerAux>?) visitor.leave(leader, follower)
        }
        is EnumerationListFieldModel<LeaderAux> -> {
            assert(follower is EnumerationListFieldModel<FollowerAux>?)
            if (follower is EnumerationListFieldModel<FollowerAux>?) visitor.leave(leader, follower)
        }
        is AssociationListFieldModel<LeaderAux> -> {
            assert(follower is AssociationListFieldModel<FollowerAux>?)
            if (follower is AssociationListFieldModel<FollowerAux>?) visitor.leave(leader, follower)
        }
        is CompositionListFieldModel<LeaderAux> -> {
            assert(follower is CompositionListFieldModel<FollowerAux>?)
            if (follower is CompositionListFieldModel<FollowerAux>?) visitor.leave(leader, follower)
        }
        is EntityKeysModel<LeaderAux> -> {
            assert(follower is EntityKeysModel<FollowerAux>?)
            if (follower is EntityKeysModel<FollowerAux>?) visitor.leave(leader, follower)
        }
        else -> {
            assert(false) { "Unknown element type: $leader" }
            TraversalAction.ABORT_TREE
        }
    }
}
