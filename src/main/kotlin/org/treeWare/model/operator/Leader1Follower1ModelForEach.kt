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
        println("#### followerMove.element: ${followerMove.element?.elementType}")
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
    ModelElementType.PRIMITIVE_FIELD -> {
        if (follower != null) assert(follower.elementType == ModelElementType.PRIMITIVE_FIELD)
        if (follower == null || follower.elementType == ModelElementType.PRIMITIVE_FIELD) visitor.visit(
            leader as PrimitiveFieldModel<LeaderAux>,
            follower as PrimitiveFieldModel<FollowerAux>?
        )
        else null
    }
    ModelElementType.ALIAS_FIELD -> {
        if (follower != null) assert(follower.elementType == ModelElementType.ALIAS_FIELD)
        if (follower == null || follower.elementType == ModelElementType.ALIAS_FIELD) visitor.visit(
            leader as AliasFieldModel<LeaderAux>,
            follower as AliasFieldModel<FollowerAux>?
        )
        else null
    }
    ModelElementType.ENUMERATION_FIELD -> {
        if (follower != null) assert(follower.elementType == ModelElementType.ENUMERATION_FIELD)
        if (follower == null || follower.elementType == ModelElementType.ENUMERATION_FIELD) visitor.visit(
            leader as EnumerationFieldModel<LeaderAux>,
            follower as EnumerationFieldModel<FollowerAux>?
        )
        else null
    }
    ModelElementType.ASSOCIATION_FIELD -> {
        if (follower != null) assert(follower.elementType == ModelElementType.ASSOCIATION_FIELD)
        if (follower == null || follower.elementType == ModelElementType.ASSOCIATION_FIELD) visitor.visit(
            leader as AssociationFieldModel<LeaderAux>,
            follower as AssociationFieldModel<FollowerAux>?
        )
        else null
    }
    ModelElementType.COMPOSITION_FIELD -> {
        if (follower != null) assert(follower.elementType == ModelElementType.COMPOSITION_FIELD)
        if (follower == null || follower.elementType == ModelElementType.COMPOSITION_FIELD) visitor.visit(
            leader as CompositionFieldModel<LeaderAux>,
            follower as CompositionFieldModel<FollowerAux>?
        )
        else null
    }
    ModelElementType.PRIMITIVE_LIST_FIELD -> {
        if (follower != null) assert(follower.elementType == ModelElementType.PRIMITIVE_LIST_FIELD)
        if (follower == null || follower.elementType == ModelElementType.PRIMITIVE_LIST_FIELD) visitor.visit(
            leader as PrimitiveListFieldModel<LeaderAux>,
            follower as PrimitiveListFieldModel<FollowerAux>?
        )
        else null
    }
    ModelElementType.ALIAS_LIST_FIELD -> {
        if (follower != null) assert(follower.elementType == ModelElementType.ALIAS_LIST_FIELD)
        if (follower == null || follower.elementType == ModelElementType.ALIAS_LIST_FIELD) visitor.visit(
            leader as AliasListFieldModel<LeaderAux>,
            follower as AliasListFieldModel<FollowerAux>?
        )
        else null
    }
    ModelElementType.ENUMERATION_LIST_FIELD -> {
        if (follower != null) assert(follower.elementType == ModelElementType.ENUMERATION_LIST_FIELD)
        if (follower == null || follower.elementType == ModelElementType.ENUMERATION_LIST_FIELD) visitor.visit(
            leader as EnumerationListFieldModel<LeaderAux>,
            follower as EnumerationListFieldModel<FollowerAux>?
        )
        else null
    }
    ModelElementType.ASSOCIATION_LIST_FIELD -> {
        if (follower != null) assert(follower.elementType == ModelElementType.ASSOCIATION_LIST_FIELD)
        if (follower == null || follower.elementType == ModelElementType.ASSOCIATION_LIST_FIELD) visitor.visit(
            leader as AssociationListFieldModel<LeaderAux>,
            follower as AssociationListFieldModel<FollowerAux>?
        )
        else null
    }
    ModelElementType.COMPOSITION_LIST_FIELD -> {
        if (follower != null) assert(follower.elementType == ModelElementType.COMPOSITION_LIST_FIELD)
        if (follower == null || follower.elementType == ModelElementType.COMPOSITION_LIST_FIELD) visitor.visit(
            leader as CompositionListFieldModel<LeaderAux>,
            follower as CompositionListFieldModel<FollowerAux>?
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
        ModelElementType.PRIMITIVE_FIELD -> {
            if (follower != null) assert(follower.elementType == ModelElementType.PRIMITIVE_FIELD)
            if (follower == null || follower.elementType == ModelElementType.PRIMITIVE_FIELD) visitor.leave(
                leader as PrimitiveFieldModel<LeaderAux>,
                follower as PrimitiveFieldModel<FollowerAux>?
            )
        }
        ModelElementType.ALIAS_FIELD -> {
            if (follower != null) assert(follower.elementType == ModelElementType.ALIAS_FIELD)
            if (follower == null || follower.elementType == ModelElementType.ALIAS_FIELD) visitor.leave(
                leader as AliasFieldModel<LeaderAux>,
                follower as AliasFieldModel<FollowerAux>?
            )
        }
        ModelElementType.ENUMERATION_FIELD -> {
            if (follower != null) assert(follower.elementType == ModelElementType.ENUMERATION_FIELD)
            if (follower == null || follower.elementType == ModelElementType.ENUMERATION_FIELD) visitor.leave(
                leader as EnumerationFieldModel<LeaderAux>,
                follower as EnumerationFieldModel<FollowerAux>?
            )
        }
        ModelElementType.ASSOCIATION_FIELD -> {
            if (follower != null) assert(follower.elementType == ModelElementType.ASSOCIATION_FIELD)
            if (follower == null || follower.elementType == ModelElementType.ASSOCIATION_FIELD) visitor.leave(
                leader as AssociationFieldModel<LeaderAux>,
                follower as AssociationFieldModel<FollowerAux>?
            )
        }
        ModelElementType.COMPOSITION_FIELD -> {
            if (follower != null) assert(follower.elementType == ModelElementType.COMPOSITION_FIELD)
            if (follower == null || follower.elementType == ModelElementType.COMPOSITION_FIELD) visitor.leave(
                leader as CompositionFieldModel<LeaderAux>,
                follower as CompositionFieldModel<FollowerAux>?
            )
        }
        ModelElementType.PRIMITIVE_LIST_FIELD -> {
            if (follower != null) assert(follower.elementType == ModelElementType.PRIMITIVE_LIST_FIELD)
            if (follower == null || follower.elementType == ModelElementType.PRIMITIVE_LIST_FIELD) visitor.leave(
                leader as PrimitiveListFieldModel<LeaderAux>,
                follower as PrimitiveListFieldModel<FollowerAux>?
            )
        }
        ModelElementType.ALIAS_LIST_FIELD -> {
            if (follower != null) assert(follower.elementType == ModelElementType.ALIAS_LIST_FIELD)
            if (follower == null || follower.elementType == ModelElementType.ALIAS_LIST_FIELD) visitor.leave(
                leader as AliasListFieldModel<LeaderAux>,
                follower as AliasListFieldModel<FollowerAux>?
            )
        }
        ModelElementType.ENUMERATION_LIST_FIELD -> {
            if (follower != null) assert(follower.elementType == ModelElementType.ENUMERATION_LIST_FIELD)
            if (follower == null || follower.elementType == ModelElementType.ENUMERATION_LIST_FIELD) visitor.leave(
                leader as EnumerationListFieldModel<LeaderAux>,
                follower as EnumerationListFieldModel<FollowerAux>?
            )
        }
        ModelElementType.ASSOCIATION_LIST_FIELD -> {
            if (follower != null) assert(follower.elementType == ModelElementType.ASSOCIATION_LIST_FIELD)
            if (follower == null || follower.elementType == ModelElementType.ASSOCIATION_LIST_FIELD) visitor.leave(
                leader as AssociationListFieldModel<LeaderAux>,
                follower as AssociationListFieldModel<FollowerAux>?
            )
        }
        ModelElementType.COMPOSITION_LIST_FIELD -> {
            if (follower != null) assert(follower.elementType == ModelElementType.COMPOSITION_LIST_FIELD)
            if (follower == null || follower.elementType == ModelElementType.COMPOSITION_LIST_FIELD) visitor.leave(
                leader as CompositionListFieldModel<LeaderAux>,
                follower as CompositionListFieldModel<FollowerAux>?
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
