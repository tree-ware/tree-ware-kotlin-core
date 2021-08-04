package org.treeWare.model.operator

import org.treeWare.common.traversal.TraversalAction
import org.treeWare.model.core.*
import org.treeWare.model.cursor.CursorMoveDirection
import org.treeWare.model.cursor.FollowerModelCursor
import org.treeWare.model.cursor.LeaderModelCursor

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
            CursorMoveDirection.VISIT -> dispatchVisit(leaderMove.element, followerMove.element, visitor)
                ?: TraversalAction.ABORT_TREE
            CursorMoveDirection.LEAVE -> {
                dispatchLeave(leaderMove.element, followerMove.element, visitor)
                TraversalAction.CONTINUE
            }
        }
    }
    return action
}

fun <LeaderAux, FollowerAux, Return> dispatchVisit(
    leader: ElementModel<LeaderAux>,
    follower: ElementModel<FollowerAux>?,
    visitor: Leader1Follower1ModelVisitor<LeaderAux, FollowerAux, Return>
): Return? = when (leader.elementType) {
    ModelElementType.MODEL -> {
        if (follower != null) assert(follower.elementType == ModelElementType.MODEL)
        if (follower == null || follower.elementType == ModelElementType.MODEL) visitor.visit(
            leader as Model<LeaderAux>,
            follower as Model<FollowerAux>?
        )
        else null
    }
    ModelElementType.ROOT -> {
        if (follower != null) assert(follower.elementType == ModelElementType.ROOT)
        if (follower == null || follower.elementType == ModelElementType.ROOT) visitor.visit(
            leader as RootModel<LeaderAux>,
            follower as RootModel<FollowerAux>?
        )
        else null
    }
    ModelElementType.ENTITY -> {
        if (follower != null) assert(follower.elementType == ModelElementType.ENTITY)
        if (follower == null || follower.elementType == ModelElementType.ENTITY) visitor.visit(
            leader as EntityModel<LeaderAux>,
            follower as EntityModel<FollowerAux>?
        )
        else null
    }
    ModelElementType.SINGLE_FIELD -> {
        if (follower != null) assert(follower.elementType == ModelElementType.SINGLE_FIELD)
        if (follower == null || follower.elementType == ModelElementType.SINGLE_FIELD) visitor.visit(
            leader as SingleFieldModel<LeaderAux>,
            follower as SingleFieldModel<FollowerAux>?
        )
        else null
    }
    ModelElementType.LIST_FIELD -> {
        if (follower != null) assert(follower.elementType == ModelElementType.LIST_FIELD)
        if (follower == null || follower.elementType == ModelElementType.LIST_FIELD) visitor.visit(
            leader as ListFieldModel<LeaderAux>,
            follower as ListFieldModel<FollowerAux>?
        )
        else null
    }
    ModelElementType.PRIMITIVE -> {
        if (follower != null) assert(follower.elementType == ModelElementType.PRIMITIVE)
        if (follower == null || follower.elementType == ModelElementType.PRIMITIVE) visitor.visit(
            leader as PrimitiveModel<LeaderAux>,
            follower as PrimitiveModel<FollowerAux>?
        )
        else null
    }
    ModelElementType.ALIAS -> {
        if (follower != null) assert(follower.elementType == ModelElementType.ALIAS)
        if (follower == null || follower.elementType == ModelElementType.ALIAS) visitor.visit(
            leader as AliasModel<LeaderAux>,
            follower as AliasModel<FollowerAux>?
        )
        else null
    }
    ModelElementType.ENUMERATION -> {
        if (follower != null) assert(follower.elementType == ModelElementType.ENUMERATION)
        if (follower == null || follower.elementType == ModelElementType.ENUMERATION) visitor.visit(
            leader as EnumerationModel<LeaderAux>,
            follower as EnumerationModel<FollowerAux>?
        )
        else null
    }
    ModelElementType.ASSOCIATION -> {
        if (follower != null) assert(follower.elementType == ModelElementType.ASSOCIATION)
        if (follower == null || follower.elementType == ModelElementType.ASSOCIATION) visitor.visit(
            leader as AssociationModel<LeaderAux>,
            follower as AssociationModel<FollowerAux>?
        )
        else null
    }
    ModelElementType.ENTITY_KEYS -> {
        if (follower != null) assert(follower.elementType == ModelElementType.ENTITY_KEYS)
        if (follower == null || follower.elementType == ModelElementType.ENTITY_KEYS) visitor.visit(
            leader as EntityKeysModel<LeaderAux>,
            follower as EntityKeysModel<FollowerAux>?
        )
        else null
    }
}

fun <LeaderAux, FollowerAux, Return> dispatchLeave(
    leader: ElementModel<LeaderAux>,
    follower: ElementModel<FollowerAux>?,
    visitor: Leader1Follower1ModelVisitor<LeaderAux, FollowerAux, Return>
) {
    when (leader.elementType) {
        ModelElementType.MODEL -> {
            if (follower != null) assert(follower.elementType == ModelElementType.MODEL)
            if (follower == null || follower.elementType == ModelElementType.MODEL) visitor.leave(
                leader as Model<LeaderAux>,
                follower as Model<FollowerAux>?
            )
        }
        ModelElementType.ROOT -> {
            if (follower != null) assert(follower.elementType == ModelElementType.ROOT)
            if (follower == null || follower.elementType == ModelElementType.ROOT) visitor.leave(
                leader as RootModel<LeaderAux>,
                follower as RootModel<FollowerAux>?
            )
        }
        ModelElementType.ENTITY -> {
            if (follower != null) assert(follower.elementType == ModelElementType.ENTITY)
            if (follower == null || follower.elementType == ModelElementType.ENTITY) visitor.leave(
                leader as EntityModel<LeaderAux>,
                follower as EntityModel<FollowerAux>?
            )
        }
        ModelElementType.SINGLE_FIELD -> {
            if (follower != null) assert(follower.elementType == ModelElementType.SINGLE_FIELD)
            if (follower == null || follower.elementType == ModelElementType.SINGLE_FIELD) visitor.leave(
                leader as SingleFieldModel<LeaderAux>,
                follower as SingleFieldModel<FollowerAux>?
            )
        }
        ModelElementType.LIST_FIELD -> {
            if (follower != null) assert(follower.elementType == ModelElementType.LIST_FIELD)
            if (follower == null || follower.elementType == ModelElementType.LIST_FIELD) visitor.leave(
                leader as ListFieldModel<LeaderAux>,
                follower as ListFieldModel<FollowerAux>?
            )
        }
        ModelElementType.PRIMITIVE -> {
            if (follower != null) assert(follower.elementType == ModelElementType.PRIMITIVE)
            if (follower == null || follower.elementType == ModelElementType.PRIMITIVE) visitor.leave(
                leader as PrimitiveModel<LeaderAux>,
                follower as PrimitiveModel<FollowerAux>?
            )
        }
        ModelElementType.ALIAS -> {
            if (follower != null) assert(follower.elementType == ModelElementType.ALIAS)
            if (follower == null || follower.elementType == ModelElementType.ALIAS) visitor.leave(
                leader as AliasModel<LeaderAux>,
                follower as AliasModel<FollowerAux>?
            )
        }
        ModelElementType.ENUMERATION -> {
            if (follower != null) assert(follower.elementType == ModelElementType.ENUMERATION)
            if (follower == null || follower.elementType == ModelElementType.ENUMERATION) visitor.leave(
                leader as EnumerationModel<LeaderAux>,
                follower as EnumerationModel<FollowerAux>?
            )
        }
        ModelElementType.ASSOCIATION -> {
            if (follower != null) assert(follower.elementType == ModelElementType.ASSOCIATION)
            if (follower == null || follower.elementType == ModelElementType.ASSOCIATION) visitor.leave(
                leader as AssociationModel<LeaderAux>,
                follower as AssociationModel<FollowerAux>?
            )
        }
        ModelElementType.ENTITY_KEYS -> {
            if (follower != null) assert(follower.elementType == ModelElementType.ENTITY_KEYS)
            if (follower == null || follower.elementType == ModelElementType.ENTITY_KEYS) visitor.leave(
                leader as EntityKeysModel<LeaderAux>,
                follower as EntityKeysModel<FollowerAux>?
            )
        }
    }
}
