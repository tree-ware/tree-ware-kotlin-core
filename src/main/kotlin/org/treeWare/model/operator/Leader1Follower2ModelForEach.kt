package org.treeWare.model.operator

import org.treeWare.common.traversal.TraversalAction
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
    ModelElementType.MODEL -> {
        if (follower1 != null) assert(follower1.elementType == ModelElementType.MODEL)
        if (follower2 != null) assert(follower2.elementType == ModelElementType.MODEL)
        if (
            (follower1 == null || follower1.elementType == ModelElementType.MODEL) &&
            (follower2 == null || follower2.elementType == ModelElementType.MODEL)
        ) visitor.visit(
            leader as Model<LeaderAux>,
            follower1 as Model<Follower1Aux>?,
            follower2 as Model<Follower2Aux>?,
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
    ModelElementType.PRIMITIVE_FIELD -> {
        if (follower1 != null) assert(follower1.elementType == ModelElementType.PRIMITIVE_FIELD)
        if (follower2 != null) assert(follower2.elementType == ModelElementType.PRIMITIVE_FIELD)
        if (
            (follower1 == null || follower1.elementType == ModelElementType.PRIMITIVE_FIELD) &&
            (follower2 == null || follower2.elementType == ModelElementType.PRIMITIVE_FIELD)
        ) visitor.visit(
            leader as PrimitiveFieldModel<LeaderAux>,
            follower1 as PrimitiveFieldModel<Follower1Aux>?,
            follower2 as PrimitiveFieldModel<Follower2Aux>?,
        )
        else null
    }
    ModelElementType.ALIAS_FIELD -> {
        if (follower1 != null) assert(follower1.elementType == ModelElementType.ALIAS_FIELD)
        if (follower2 != null) assert(follower2.elementType == ModelElementType.ALIAS_FIELD)
        if (
            (follower1 == null || follower1.elementType == ModelElementType.ALIAS_FIELD) &&
            (follower2 == null || follower2.elementType == ModelElementType.ALIAS_FIELD)
        ) visitor.visit(
            leader as AliasFieldModel<LeaderAux>,
            follower1 as AliasFieldModel<Follower1Aux>?,
            follower2 as AliasFieldModel<Follower2Aux>?,
        )
        else null
    }
    ModelElementType.ENUMERATION_FIELD -> {
        if (follower1 != null) assert(follower1.elementType == ModelElementType.ENUMERATION_FIELD)
        if (follower2 != null) assert(follower2.elementType == ModelElementType.ENUMERATION_FIELD)
        if (
            (follower1 == null || follower1.elementType == ModelElementType.ENUMERATION_FIELD) &&
            (follower2 == null || follower2.elementType == ModelElementType.ENUMERATION_FIELD)
        ) visitor.visit(
            leader as EnumerationFieldModel<LeaderAux>,
            follower1 as EnumerationFieldModel<Follower1Aux>?,
            follower2 as EnumerationFieldModel<Follower2Aux>?,
        )
        else null
    }
    ModelElementType.ASSOCIATION_FIELD -> {
        if (follower1 != null) assert(follower1.elementType == ModelElementType.ASSOCIATION_FIELD)
        if (follower2 != null) assert(follower2.elementType == ModelElementType.ASSOCIATION_FIELD)
        if (
            (follower1 == null || follower1.elementType == ModelElementType.ASSOCIATION_FIELD) &&
            (follower2 == null || follower2.elementType == ModelElementType.ASSOCIATION_FIELD)
        ) visitor.visit(
            leader as AssociationFieldModel<LeaderAux>,
            follower1 as AssociationFieldModel<Follower1Aux>?,
            follower2 as AssociationFieldModel<Follower2Aux>?,
        )
        else null
    }
    ModelElementType.COMPOSITION_FIELD -> {
        if (follower1 != null) assert(follower1.elementType == ModelElementType.COMPOSITION_FIELD)
        if (follower2 != null) assert(follower2.elementType == ModelElementType.COMPOSITION_FIELD)
        if (
            (follower1 == null || follower1.elementType == ModelElementType.COMPOSITION_FIELD) &&
            (follower2 == null || follower2.elementType == ModelElementType.COMPOSITION_FIELD)
        ) visitor.visit(
            leader as CompositionFieldModel<LeaderAux>,
            follower1 as CompositionFieldModel<Follower1Aux>?,
            follower2 as CompositionFieldModel<Follower2Aux>?,
        )
        else null
    }
    ModelElementType.PRIMITIVE_LIST_FIELD -> {
        if (follower1 != null) assert(follower1.elementType == ModelElementType.PRIMITIVE_LIST_FIELD)
        if (follower2 != null) assert(follower2.elementType == ModelElementType.PRIMITIVE_LIST_FIELD)
        if (
            (follower1 == null || follower1.elementType == ModelElementType.PRIMITIVE_LIST_FIELD) &&
            (follower2 == null || follower2.elementType == ModelElementType.PRIMITIVE_LIST_FIELD)
        ) visitor.visit(
            leader as PrimitiveListFieldModel<LeaderAux>,
            follower1 as PrimitiveListFieldModel<Follower1Aux>?,
            follower2 as PrimitiveListFieldModel<Follower2Aux>?,
        )
        else null
    }
    ModelElementType.ALIAS_LIST_FIELD -> {
        if (follower1 != null) assert(follower1.elementType == ModelElementType.ALIAS_LIST_FIELD)
        if (follower2 != null) assert(follower2.elementType == ModelElementType.ALIAS_LIST_FIELD)
        if (
            (follower1 == null || follower1.elementType == ModelElementType.ALIAS_LIST_FIELD) &&
            (follower2 == null || follower2.elementType == ModelElementType.ALIAS_LIST_FIELD)
        ) visitor.visit(
            leader as AliasListFieldModel<LeaderAux>,
            follower1 as AliasListFieldModel<Follower1Aux>?,
            follower2 as AliasListFieldModel<Follower2Aux>?,
        )
        else null
    }
    ModelElementType.ENUMERATION_LIST_FIELD -> {
        if (follower1 != null) assert(follower1.elementType == ModelElementType.ENUMERATION_LIST_FIELD)
        if (follower2 != null) assert(follower2.elementType == ModelElementType.ENUMERATION_LIST_FIELD)
        if (
            (follower1 == null || follower1.elementType == ModelElementType.ENUMERATION_LIST_FIELD) &&
            (follower2 == null || follower2.elementType == ModelElementType.ENUMERATION_LIST_FIELD)
        ) visitor.visit(
            leader as EnumerationListFieldModel<LeaderAux>,
            follower1 as EnumerationListFieldModel<Follower1Aux>?,
            follower2 as EnumerationListFieldModel<Follower2Aux>?,
        )
        else null
    }
    ModelElementType.ASSOCIATION_LIST_FIELD -> {
        if (follower1 != null) assert(follower1.elementType == ModelElementType.ASSOCIATION_LIST_FIELD)
        if (follower2 != null) assert(follower2.elementType == ModelElementType.ASSOCIATION_LIST_FIELD)
        if (
            (follower1 == null || follower1.elementType == ModelElementType.ASSOCIATION_LIST_FIELD) &&
            (follower2 == null || follower2.elementType == ModelElementType.ASSOCIATION_LIST_FIELD)
        ) visitor.visit(
            leader as AssociationListFieldModel<LeaderAux>,
            follower1 as AssociationListFieldModel<Follower1Aux>?,
            follower2 as AssociationListFieldModel<Follower2Aux>?,
        )
        else null
    }
    ModelElementType.COMPOSITION_LIST_FIELD -> {
        if (follower1 != null) assert(follower1.elementType == ModelElementType.COMPOSITION_LIST_FIELD)
        if (follower2 != null) assert(follower2.elementType == ModelElementType.COMPOSITION_LIST_FIELD)
        if (
            (follower1 == null || follower1.elementType == ModelElementType.COMPOSITION_LIST_FIELD) &&
            (follower2 == null || follower2.elementType == ModelElementType.COMPOSITION_LIST_FIELD)
        ) visitor.visit(
            leader as CompositionListFieldModel<LeaderAux>,
            follower1 as CompositionListFieldModel<Follower1Aux>?,
            follower2 as CompositionListFieldModel<Follower2Aux>?,
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
        ModelElementType.MODEL -> {
            if (follower1 != null) assert(follower1.elementType == ModelElementType.MODEL)
            if (follower2 != null) assert(follower2.elementType == ModelElementType.MODEL)
            if (
                (follower1 == null || follower1.elementType == ModelElementType.MODEL) &&
                (follower2 == null || follower2.elementType == ModelElementType.MODEL)
            ) visitor.leave(
                leader as Model<LeaderAux>,
                follower1 as Model<Follower1Aux>?,
                follower2 as Model<Follower2Aux>?,
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
        ModelElementType.PRIMITIVE_FIELD -> {
            if (follower1 != null) assert(follower1.elementType == ModelElementType.PRIMITIVE_FIELD)
            if (follower2 != null) assert(follower2.elementType == ModelElementType.PRIMITIVE_FIELD)
            if (
                (follower1 == null || follower1.elementType == ModelElementType.PRIMITIVE_FIELD) &&
                (follower2 == null || follower2.elementType == ModelElementType.PRIMITIVE_FIELD)
            ) visitor.leave(
                leader as PrimitiveFieldModel<LeaderAux>,
                follower1 as PrimitiveFieldModel<Follower1Aux>?,
                follower2 as PrimitiveFieldModel<Follower2Aux>?,
            )

        }
        ModelElementType.ALIAS_FIELD -> {
            if (follower1 != null) assert(follower1.elementType == ModelElementType.ALIAS_FIELD)
            if (follower2 != null) assert(follower2.elementType == ModelElementType.ALIAS_FIELD)
            if (
                (follower1 == null || follower1.elementType == ModelElementType.ALIAS_FIELD) &&
                (follower2 == null || follower2.elementType == ModelElementType.ALIAS_FIELD)
            ) visitor.leave(
                leader as AliasFieldModel<LeaderAux>,
                follower1 as AliasFieldModel<Follower1Aux>?,
                follower2 as AliasFieldModel<Follower2Aux>?,
            )

        }
        ModelElementType.ENUMERATION_FIELD -> {
            if (follower1 != null) assert(follower1.elementType == ModelElementType.ENUMERATION_FIELD)
            if (follower2 != null) assert(follower2.elementType == ModelElementType.ENUMERATION_FIELD)
            if (
                (follower1 == null || follower1.elementType == ModelElementType.ENUMERATION_FIELD) &&
                (follower2 == null || follower2.elementType == ModelElementType.ENUMERATION_FIELD)
            ) visitor.leave(
                leader as EnumerationFieldModel<LeaderAux>,
                follower1 as EnumerationFieldModel<Follower1Aux>?,
                follower2 as EnumerationFieldModel<Follower2Aux>?,
            )

        }
        ModelElementType.ASSOCIATION_FIELD -> {
            if (follower1 != null) assert(follower1.elementType == ModelElementType.ASSOCIATION_FIELD)
            if (follower2 != null) assert(follower2.elementType == ModelElementType.ASSOCIATION_FIELD)
            if (
                (follower1 == null || follower1.elementType == ModelElementType.ASSOCIATION_FIELD) &&
                (follower2 == null || follower2.elementType == ModelElementType.ASSOCIATION_FIELD)
            ) visitor.leave(
                leader as AssociationFieldModel<LeaderAux>,
                follower1 as AssociationFieldModel<Follower1Aux>?,
                follower2 as AssociationFieldModel<Follower2Aux>?,
            )

        }
        ModelElementType.COMPOSITION_FIELD -> {
            if (follower1 != null) assert(follower1.elementType == ModelElementType.COMPOSITION_FIELD)
            if (follower2 != null) assert(follower2.elementType == ModelElementType.COMPOSITION_FIELD)
            if (
                (follower1 == null || follower1.elementType == ModelElementType.COMPOSITION_FIELD) &&
                (follower2 == null || follower2.elementType == ModelElementType.COMPOSITION_FIELD)
            ) visitor.leave(
                leader as CompositionFieldModel<LeaderAux>,
                follower1 as CompositionFieldModel<Follower1Aux>?,
                follower2 as CompositionFieldModel<Follower2Aux>?,
            )

        }
        ModelElementType.PRIMITIVE_LIST_FIELD -> {
            if (follower1 != null) assert(follower1.elementType == ModelElementType.PRIMITIVE_LIST_FIELD)
            if (follower2 != null) assert(follower2.elementType == ModelElementType.PRIMITIVE_LIST_FIELD)
            if (
                (follower1 == null || follower1.elementType == ModelElementType.PRIMITIVE_LIST_FIELD) &&
                (follower2 == null || follower2.elementType == ModelElementType.PRIMITIVE_LIST_FIELD)
            ) visitor.leave(
                leader as PrimitiveListFieldModel<LeaderAux>,
                follower1 as PrimitiveListFieldModel<Follower1Aux>?,
                follower2 as PrimitiveListFieldModel<Follower2Aux>?,
            )

        }
        ModelElementType.ALIAS_LIST_FIELD -> {
            if (follower1 != null) assert(follower1.elementType == ModelElementType.ALIAS_LIST_FIELD)
            if (follower2 != null) assert(follower2.elementType == ModelElementType.ALIAS_LIST_FIELD)
            if (
                (follower1 == null || follower1.elementType == ModelElementType.ALIAS_LIST_FIELD) &&
                (follower2 == null || follower2.elementType == ModelElementType.ALIAS_LIST_FIELD)
            ) visitor.leave(
                leader as AliasListFieldModel<LeaderAux>,
                follower1 as AliasListFieldModel<Follower1Aux>?,
                follower2 as AliasListFieldModel<Follower2Aux>?,
            )

        }
        ModelElementType.ENUMERATION_LIST_FIELD -> {
            if (follower1 != null) assert(follower1.elementType == ModelElementType.ENUMERATION_LIST_FIELD)
            if (follower2 != null) assert(follower2.elementType == ModelElementType.ENUMERATION_LIST_FIELD)
            if (
                (follower1 == null || follower1.elementType == ModelElementType.ENUMERATION_LIST_FIELD) &&
                (follower2 == null || follower2.elementType == ModelElementType.ENUMERATION_LIST_FIELD)
            ) visitor.leave(
                leader as EnumerationListFieldModel<LeaderAux>,
                follower1 as EnumerationListFieldModel<Follower1Aux>?,
                follower2 as EnumerationListFieldModel<Follower2Aux>?,
            )

        }
        ModelElementType.ASSOCIATION_LIST_FIELD -> {
            if (follower1 != null) assert(follower1.elementType == ModelElementType.ASSOCIATION_LIST_FIELD)
            if (follower2 != null) assert(follower2.elementType == ModelElementType.ASSOCIATION_LIST_FIELD)
            if (
                (follower1 == null || follower1.elementType == ModelElementType.ASSOCIATION_LIST_FIELD) &&
                (follower2 == null || follower2.elementType == ModelElementType.ASSOCIATION_LIST_FIELD)
            ) visitor.leave(
                leader as AssociationListFieldModel<LeaderAux>,
                follower1 as AssociationListFieldModel<Follower1Aux>?,
                follower2 as AssociationListFieldModel<Follower2Aux>?,
            )

        }
        ModelElementType.COMPOSITION_LIST_FIELD -> {
            if (follower1 != null) assert(follower1.elementType == ModelElementType.COMPOSITION_LIST_FIELD)
            if (follower2 != null) assert(follower2.elementType == ModelElementType.COMPOSITION_LIST_FIELD)
            if (
                (follower1 == null || follower1.elementType == ModelElementType.COMPOSITION_LIST_FIELD) &&
                (follower2 == null || follower2.elementType == ModelElementType.COMPOSITION_LIST_FIELD)
            ) visitor.leave(
                leader as CompositionListFieldModel<LeaderAux>,
                follower1 as CompositionListFieldModel<Follower1Aux>?,
                follower2 as CompositionListFieldModel<Follower2Aux>?,
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
