package org.treeWare.model.operator

import org.treeWare.model.core.*
import org.treeWare.model.cursor.CursorMoveDirection
import org.treeWare.model.cursor.FollowerModelCursor
import org.treeWare.model.cursor.LeaderModelCursor

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
            CursorMoveDirection.VISIT -> dispatchVisit(
                leaderMove.element,
                follower1Move.element,
                follower2Move.element,
                visitor
            ) ?: TraversalAction.ABORT_TREE
            CursorMoveDirection.LEAVE -> {
                dispatchLeave(leaderMove.element, follower1Move.element, follower2Move.element, visitor)
                TraversalAction.CONTINUE
            }
        }
    }
    return action
}

suspend fun <LeaderAux, Follower1Aux, Follower2Aux, Return> dispatchVisit(
    leader: ElementModel<LeaderAux>,
    follower1: ElementModel<Follower1Aux>?,
    follower2: ElementModel<Follower2Aux>?,
    visitor: Leader1Follower2ModelVisitor<LeaderAux, Follower1Aux, Follower2Aux, Return>
): Return? = when (leader.elementType) {
    ModelElementType.MAIN -> {
        if (follower1 != null) assert(follower1.elementType == ModelElementType.MAIN)
        if (follower2 != null) assert(follower2.elementType == ModelElementType.MAIN)
        if (
            (follower1 == null || follower1.elementType == ModelElementType.MAIN) &&
            (follower2 == null || follower2.elementType == ModelElementType.MAIN)
        ) visitor.visit(
            leader as MainModel<LeaderAux>,
            follower1 as MainModel<Follower1Aux>?,
            follower2 as MainModel<Follower2Aux>?,
        )
        else null
    }
    ModelElementType.ROOT -> {
        if (follower1 != null) assert(follower1.elementType == ModelElementType.ROOT)
        if (follower2 != null) assert(follower2.elementType == ModelElementType.ROOT)
        if (
            (follower1 == null || follower1.elementType == ModelElementType.ROOT) &&
            (follower2 == null || follower2.elementType == ModelElementType.ROOT)
        ) visitor.visit(
            leader as RootModel<LeaderAux>,
            follower1 as RootModel<Follower1Aux>?,
            follower2 as RootModel<Follower2Aux>?,
        )
        else null
    }
    ModelElementType.ENTITY -> {
        if (follower1 != null) assert(follower1.elementType == ModelElementType.ENTITY)
        if (follower2 != null) assert(follower2.elementType == ModelElementType.ENTITY)
        if (
            (follower1 == null || follower1.elementType == ModelElementType.ENTITY) &&
            (follower2 == null || follower2.elementType == ModelElementType.ENTITY)
        ) visitor.visit(
            leader as EntityModel<LeaderAux>,
            follower1 as EntityModel<Follower1Aux>?,
            follower2 as EntityModel<Follower2Aux>?,
        )
        else null
    }
    ModelElementType.SINGLE_FIELD -> {
        if (follower1 != null) assert(follower1.elementType == ModelElementType.SINGLE_FIELD)
        if (follower2 != null) assert(follower2.elementType == ModelElementType.SINGLE_FIELD)
        if (
            (follower1 == null || follower1.elementType == ModelElementType.SINGLE_FIELD) &&
            (follower2 == null || follower2.elementType == ModelElementType.SINGLE_FIELD)
        ) visitor.visit(
            leader as SingleFieldModel<LeaderAux>,
            follower1 as SingleFieldModel<Follower1Aux>?,
            follower2 as SingleFieldModel<Follower2Aux>?,
        )
        else null
    }
    ModelElementType.LIST_FIELD -> {
        if (follower1 != null) assert(follower1.elementType == ModelElementType.LIST_FIELD)
        if (follower2 != null) assert(follower2.elementType == ModelElementType.LIST_FIELD)
        if (
            (follower1 == null || follower1.elementType == ModelElementType.LIST_FIELD) &&
            (follower2 == null || follower2.elementType == ModelElementType.LIST_FIELD)
        ) visitor.visit(
            leader as ListFieldModel<LeaderAux>,
            follower1 as ListFieldModel<Follower1Aux>?,
            follower2 as ListFieldModel<Follower2Aux>?,
        )
        else null
    }
    ModelElementType.PRIMITIVE -> {
        if (follower1 != null) assert(follower1.elementType == ModelElementType.PRIMITIVE)
        if (follower2 != null) assert(follower2.elementType == ModelElementType.PRIMITIVE)
        if (
            (follower1 == null || follower1.elementType == ModelElementType.PRIMITIVE) &&
            (follower2 == null || follower2.elementType == ModelElementType.PRIMITIVE)
        ) visitor.visit(
            leader as PrimitiveModel<LeaderAux>,
            follower1 as PrimitiveModel<Follower1Aux>?,
            follower2 as PrimitiveModel<Follower2Aux>?,
        )
        else null
    }
    ModelElementType.ALIAS -> {
        if (follower1 != null) assert(follower1.elementType == ModelElementType.ALIAS)
        if (follower2 != null) assert(follower2.elementType == ModelElementType.ALIAS)
        if (
            (follower1 == null || follower1.elementType == ModelElementType.ALIAS) &&
            (follower2 == null || follower2.elementType == ModelElementType.ALIAS)
        ) visitor.visit(
            leader as AliasModel<LeaderAux>,
            follower1 as AliasModel<Follower1Aux>?,
            follower2 as AliasModel<Follower2Aux>?,
        )
        else null
    }
    ModelElementType.PASSWORD1WAY -> {
        if (follower1 != null) assert(follower1.elementType == ModelElementType.PASSWORD1WAY)
        if (follower2 != null) assert(follower2.elementType == ModelElementType.PASSWORD1WAY)
        if (
            (follower1 == null || follower1.elementType == ModelElementType.PASSWORD1WAY) &&
            (follower2 == null || follower2.elementType == ModelElementType.PASSWORD1WAY)
        ) visitor.visit(
            leader as Password1wayModel<LeaderAux>,
            follower1 as Password1wayModel<Follower1Aux>?,
            follower2 as Password1wayModel<Follower2Aux>?,
        )
        else null
    }
    ModelElementType.PASSWORD2WAY -> {
        if (follower1 != null) assert(follower1.elementType == ModelElementType.PASSWORD2WAY)
        if (follower2 != null) assert(follower2.elementType == ModelElementType.PASSWORD2WAY)
        if (
            (follower1 == null || follower1.elementType == ModelElementType.PASSWORD2WAY) &&
            (follower2 == null || follower2.elementType == ModelElementType.PASSWORD2WAY)
        ) visitor.visit(
            leader as Password2wayModel<LeaderAux>,
            follower1 as Password2wayModel<Follower1Aux>?,
            follower2 as Password2wayModel<Follower2Aux>?,
        )
        else null
    }
    ModelElementType.ENUMERATION -> {
        if (follower1 != null) assert(follower1.elementType == ModelElementType.ENUMERATION)
        if (follower2 != null) assert(follower2.elementType == ModelElementType.ENUMERATION)
        if (
            (follower1 == null || follower1.elementType == ModelElementType.ENUMERATION) &&
            (follower2 == null || follower2.elementType == ModelElementType.ENUMERATION)
        ) visitor.visit(
            leader as EnumerationModel<LeaderAux>,
            follower1 as EnumerationModel<Follower1Aux>?,
            follower2 as EnumerationModel<Follower2Aux>?,
        )
        else null
    }
    ModelElementType.ASSOCIATION -> {
        if (follower1 != null) assert(follower1.elementType == ModelElementType.ASSOCIATION)
        if (follower2 != null) assert(follower2.elementType == ModelElementType.ASSOCIATION)
        if (
            (follower1 == null || follower1.elementType == ModelElementType.ASSOCIATION) &&
            (follower2 == null || follower2.elementType == ModelElementType.ASSOCIATION)
        ) visitor.visit(
            leader as AssociationModel<LeaderAux>,
            follower1 as AssociationModel<Follower1Aux>?,
            follower2 as AssociationModel<Follower2Aux>?,
        )
        else null
    }
    ModelElementType.ENTITY_KEYS -> {
        if (follower1 != null) assert(follower1.elementType == ModelElementType.ENTITY_KEYS)
        if (follower2 != null) assert(follower2.elementType == ModelElementType.ENTITY_KEYS)
        if (
            (follower1 == null || follower1.elementType == ModelElementType.ENTITY_KEYS) &&
            (follower2 == null || follower2.elementType == ModelElementType.ENTITY_KEYS)
        ) visitor.visit(
            leader as EntityKeysModel<LeaderAux>,
            follower1 as EntityKeysModel<Follower1Aux>?,
            follower2 as EntityKeysModel<Follower2Aux>?,
        )
        else null
    }
}

suspend fun <LeaderAux, Follower1Aux, Follower2Aux, Return> dispatchLeave(
    leader: ElementModel<LeaderAux>,
    follower1: ElementModel<Follower1Aux>?,
    follower2: ElementModel<Follower2Aux>?,
    visitor: Leader1Follower2ModelVisitor<LeaderAux, Follower1Aux, Follower2Aux, Return>
) {
    when (leader.elementType) {
        ModelElementType.MAIN -> {
            if (follower1 != null) assert(follower1.elementType == ModelElementType.MAIN)
            if (follower2 != null) assert(follower2.elementType == ModelElementType.MAIN)
            if (
                (follower1 == null || follower1.elementType == ModelElementType.MAIN) &&
                (follower2 == null || follower2.elementType == ModelElementType.MAIN)
            ) visitor.leave(
                leader as MainModel<LeaderAux>,
                follower1 as MainModel<Follower1Aux>?,
                follower2 as MainModel<Follower2Aux>?,
            )
        }
        ModelElementType.ROOT -> {
            if (follower1 != null) assert(follower1.elementType == ModelElementType.ROOT)
            if (follower2 != null) assert(follower2.elementType == ModelElementType.ROOT)
            if (
                (follower1 == null || follower1.elementType == ModelElementType.ROOT) &&
                (follower2 == null || follower2.elementType == ModelElementType.ROOT)
            ) visitor.leave(
                leader as RootModel<LeaderAux>,
                follower1 as RootModel<Follower1Aux>?,
                follower2 as RootModel<Follower2Aux>?,
            )
        }
        ModelElementType.ENTITY -> {
            if (follower1 != null) assert(follower1.elementType == ModelElementType.ENTITY)
            if (follower2 != null) assert(follower2.elementType == ModelElementType.ENTITY)
            if (
                (follower1 == null || follower1.elementType == ModelElementType.ENTITY) &&
                (follower2 == null || follower2.elementType == ModelElementType.ENTITY)
            ) visitor.leave(
                leader as EntityModel<LeaderAux>,
                follower1 as EntityModel<Follower1Aux>?,
                follower2 as EntityModel<Follower2Aux>?,
            )
        }
        ModelElementType.SINGLE_FIELD -> {
            if (follower1 != null) assert(follower1.elementType == ModelElementType.SINGLE_FIELD)
            if (follower2 != null) assert(follower2.elementType == ModelElementType.SINGLE_FIELD)
            if (
                (follower1 == null || follower1.elementType == ModelElementType.SINGLE_FIELD) &&
                (follower2 == null || follower2.elementType == ModelElementType.SINGLE_FIELD)
            ) visitor.leave(
                leader as SingleFieldModel<LeaderAux>,
                follower1 as SingleFieldModel<Follower1Aux>?,
                follower2 as SingleFieldModel<Follower2Aux>?,
            )
        }
        ModelElementType.LIST_FIELD -> {
            if (follower1 != null) assert(follower1.elementType == ModelElementType.LIST_FIELD)
            if (follower2 != null) assert(follower2.elementType == ModelElementType.LIST_FIELD)
            if (
                (follower1 == null || follower1.elementType == ModelElementType.LIST_FIELD) &&
                (follower2 == null || follower2.elementType == ModelElementType.LIST_FIELD)
            ) visitor.leave(
                leader as ListFieldModel<LeaderAux>,
                follower1 as ListFieldModel<Follower1Aux>?,
                follower2 as ListFieldModel<Follower2Aux>?,
            )
        }
        ModelElementType.PRIMITIVE -> {
            if (follower1 != null) assert(follower1.elementType == ModelElementType.PRIMITIVE)
            if (follower2 != null) assert(follower2.elementType == ModelElementType.PRIMITIVE)
            if (
                (follower1 == null || follower1.elementType == ModelElementType.PRIMITIVE) &&
                (follower2 == null || follower2.elementType == ModelElementType.PRIMITIVE)
            ) visitor.leave(
                leader as PrimitiveModel<LeaderAux>,
                follower1 as PrimitiveModel<Follower1Aux>?,
                follower2 as PrimitiveModel<Follower2Aux>?,
            )
        }
        ModelElementType.ALIAS -> {
            if (follower1 != null) assert(follower1.elementType == ModelElementType.ALIAS)
            if (follower2 != null) assert(follower2.elementType == ModelElementType.ALIAS)
            if (
                (follower1 == null || follower1.elementType == ModelElementType.ALIAS) &&
                (follower2 == null || follower2.elementType == ModelElementType.ALIAS)
            ) visitor.leave(
                leader as AliasModel<LeaderAux>,
                follower1 as AliasModel<Follower1Aux>?,
                follower2 as AliasModel<Follower2Aux>?,
            )
        }
        ModelElementType.PASSWORD1WAY -> {
            if (follower1 != null) assert(follower1.elementType == ModelElementType.PASSWORD1WAY)
            if (follower2 != null) assert(follower2.elementType == ModelElementType.PASSWORD1WAY)
            if (
                (follower1 == null || follower1.elementType == ModelElementType.PASSWORD1WAY) &&
                (follower2 == null || follower2.elementType == ModelElementType.PASSWORD1WAY)
            ) visitor.leave(
                leader as Password1wayModel<LeaderAux>,
                follower1 as Password1wayModel<Follower1Aux>?,
                follower2 as Password1wayModel<Follower2Aux>?,
            )
        }
        ModelElementType.PASSWORD2WAY -> {
            if (follower1 != null) assert(follower1.elementType == ModelElementType.PASSWORD2WAY)
            if (follower2 != null) assert(follower2.elementType == ModelElementType.PASSWORD2WAY)
            if (
                (follower1 == null || follower1.elementType == ModelElementType.PASSWORD2WAY) &&
                (follower2 == null || follower2.elementType == ModelElementType.PASSWORD2WAY)
            ) visitor.leave(
                leader as Password2wayModel<LeaderAux>,
                follower1 as Password2wayModel<Follower1Aux>?,
                follower2 as Password2wayModel<Follower2Aux>?,
            )
        }
        ModelElementType.ENUMERATION -> {
            if (follower1 != null) assert(follower1.elementType == ModelElementType.ENUMERATION)
            if (follower2 != null) assert(follower2.elementType == ModelElementType.ENUMERATION)
            if (
                (follower1 == null || follower1.elementType == ModelElementType.ENUMERATION) &&
                (follower2 == null || follower2.elementType == ModelElementType.ENUMERATION)
            ) visitor.leave(
                leader as EnumerationModel<LeaderAux>,
                follower1 as EnumerationModel<Follower1Aux>?,
                follower2 as EnumerationModel<Follower2Aux>?,
            )
        }
        ModelElementType.ASSOCIATION -> {
            if (follower1 != null) assert(follower1.elementType == ModelElementType.ASSOCIATION)
            if (follower2 != null) assert(follower2.elementType == ModelElementType.ASSOCIATION)
            if (
                (follower1 == null || follower1.elementType == ModelElementType.ASSOCIATION) &&
                (follower2 == null || follower2.elementType == ModelElementType.ASSOCIATION)
            ) visitor.leave(
                leader as AssociationModel<LeaderAux>,
                follower1 as AssociationModel<Follower1Aux>?,
                follower2 as AssociationModel<Follower2Aux>?,
            )
        }
        ModelElementType.ENTITY_KEYS -> {
            if (follower1 != null) assert(follower1.elementType == ModelElementType.ENTITY_KEYS)
            if (follower2 != null) assert(follower2.elementType == ModelElementType.ENTITY_KEYS)
            if (
                (follower1 == null || follower1.elementType == ModelElementType.ENTITY_KEYS) &&
                (follower2 == null || follower2.elementType == ModelElementType.ENTITY_KEYS)
            ) visitor.leave(
                leader as EntityKeysModel<LeaderAux>,
                follower1 as EntityKeysModel<Follower1Aux>?,
                follower2 as EntityKeysModel<Follower2Aux>?,
            )
        }
    }
}
