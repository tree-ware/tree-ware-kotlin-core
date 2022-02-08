package org.treeWare.model.traversal

import org.treeWare.model.core.*
import org.treeWare.model.cursor.CursorMoveDirection
import org.treeWare.model.cursor.FollowerModelCursor
import org.treeWare.model.cursor.Leader1ModelCursor
import org.treeWare.util.assertInDevMode

fun forEach(
    leader: ElementModel,
    follower1: ElementModel,
    follower2: ElementModel,
    visitor: Leader1Follower2ModelVisitor<TraversalAction>
): TraversalAction {
    val leaderCursor = Leader1ModelCursor(leader)
    val follower1Cursor = FollowerModelCursor(follower1)
    val follower2Cursor = FollowerModelCursor(follower2)
    var action = TraversalAction.CONTINUE
    while (action != TraversalAction.ABORT_TREE) {
        val leaderMove = leaderCursor.next(action) ?: break
        val follower1Move = follower1Cursor.follow(leaderMove)
        val follower2Move = follower2Cursor.follow(leaderMove)
        assertInDevMode(follower1Move != null)
        if (follower1Move == null) break
        assertInDevMode(follower2Move != null)
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

fun <Return> dispatchVisit(
    leader: ElementModel,
    follower1: ElementModel?,
    follower2: ElementModel?,
    visitor: Leader1Follower2ModelVisitor<Return>
): Return? = when (leader.elementType) {
    ModelElementType.MAIN -> {
        if (follower1 != null) assertInDevMode(follower1.elementType == ModelElementType.MAIN)
        if (follower2 != null) assertInDevMode(follower2.elementType == ModelElementType.MAIN)
        if (
            (follower1 == null || follower1.elementType == ModelElementType.MAIN) &&
            (follower2 == null || follower2.elementType == ModelElementType.MAIN)
        ) visitor.visitMain(
            leader as MainModel,
            follower1 as MainModel?,
            follower2 as MainModel?,
        )
        else null
    }
    ModelElementType.ENTITY -> {
        if (follower1 != null) assertInDevMode(follower1.elementType == ModelElementType.ENTITY)
        if (follower2 != null) assertInDevMode(follower2.elementType == ModelElementType.ENTITY)
        if (
            (follower1 == null || follower1.elementType == ModelElementType.ENTITY) &&
            (follower2 == null || follower2.elementType == ModelElementType.ENTITY)
        ) visitor.visitEntity(
            leader as EntityModel,
            follower1 as EntityModel?,
            follower2 as EntityModel?,
        )
        else null
    }
    ModelElementType.SINGLE_FIELD -> {
        if (follower1 != null) assertInDevMode(follower1.elementType == ModelElementType.SINGLE_FIELD)
        if (follower2 != null) assertInDevMode(follower2.elementType == ModelElementType.SINGLE_FIELD)
        if (
            (follower1 == null || follower1.elementType == ModelElementType.SINGLE_FIELD) &&
            (follower2 == null || follower2.elementType == ModelElementType.SINGLE_FIELD)
        ) visitor.visitSingleField(
            leader as SingleFieldModel,
            follower1 as SingleFieldModel?,
            follower2 as SingleFieldModel?,
        )
        else null
    }
    ModelElementType.LIST_FIELD -> {
        if (follower1 != null) assertInDevMode(follower1.elementType == ModelElementType.LIST_FIELD)
        if (follower2 != null) assertInDevMode(follower2.elementType == ModelElementType.LIST_FIELD)
        if (
            (follower1 == null || follower1.elementType == ModelElementType.LIST_FIELD) &&
            (follower2 == null || follower2.elementType == ModelElementType.LIST_FIELD)
        ) visitor.visitListField(
            leader as ListFieldModel,
            follower1 as ListFieldModel?,
            follower2 as ListFieldModel?,
        )
        else null
    }
    ModelElementType.SET_FIELD -> {
        if (follower1 != null) assertInDevMode(follower1.elementType == ModelElementType.SET_FIELD)
        if (follower2 != null) assertInDevMode(follower2.elementType == ModelElementType.SET_FIELD)
        if (
            (follower1 == null || follower1.elementType == ModelElementType.SET_FIELD) &&
            (follower2 == null || follower2.elementType == ModelElementType.SET_FIELD)
        ) visitor.visitSetField(
            leader as SetFieldModel,
            follower1 as SetFieldModel?,
            follower2 as SetFieldModel?,
        )
        else null
    }
    ModelElementType.PRIMITIVE -> {
        if (follower1 != null) assertInDevMode(follower1.elementType == ModelElementType.PRIMITIVE)
        if (follower2 != null) assertInDevMode(follower2.elementType == ModelElementType.PRIMITIVE)
        if (
            (follower1 == null || follower1.elementType == ModelElementType.PRIMITIVE) &&
            (follower2 == null || follower2.elementType == ModelElementType.PRIMITIVE)
        ) visitor.visitPrimitive(
            leader as PrimitiveModel,
            follower1 as PrimitiveModel?,
            follower2 as PrimitiveModel?,
        )
        else null
    }
    ModelElementType.ALIAS -> {
        if (follower1 != null) assertInDevMode(follower1.elementType == ModelElementType.ALIAS)
        if (follower2 != null) assertInDevMode(follower2.elementType == ModelElementType.ALIAS)
        if (
            (follower1 == null || follower1.elementType == ModelElementType.ALIAS) &&
            (follower2 == null || follower2.elementType == ModelElementType.ALIAS)
        ) visitor.visitAlias(
            leader as AliasModel,
            follower1 as AliasModel?,
            follower2 as AliasModel?,
        )
        else null
    }
    ModelElementType.PASSWORD1WAY -> {
        if (follower1 != null) assertInDevMode(follower1.elementType == ModelElementType.PASSWORD1WAY)
        if (follower2 != null) assertInDevMode(follower2.elementType == ModelElementType.PASSWORD1WAY)
        if (
            (follower1 == null || follower1.elementType == ModelElementType.PASSWORD1WAY) &&
            (follower2 == null || follower2.elementType == ModelElementType.PASSWORD1WAY)
        ) visitor.visitPassword1way(
            leader as Password1wayModel,
            follower1 as Password1wayModel?,
            follower2 as Password1wayModel?,
        )
        else null
    }
    ModelElementType.PASSWORD2WAY -> {
        if (follower1 != null) assertInDevMode(follower1.elementType == ModelElementType.PASSWORD2WAY)
        if (follower2 != null) assertInDevMode(follower2.elementType == ModelElementType.PASSWORD2WAY)
        if (
            (follower1 == null || follower1.elementType == ModelElementType.PASSWORD2WAY) &&
            (follower2 == null || follower2.elementType == ModelElementType.PASSWORD2WAY)
        ) visitor.visitPassword2way(
            leader as Password2wayModel,
            follower1 as Password2wayModel?,
            follower2 as Password2wayModel?,
        )
        else null
    }
    ModelElementType.ENUMERATION -> {
        if (follower1 != null) assertInDevMode(follower1.elementType == ModelElementType.ENUMERATION)
        if (follower2 != null) assertInDevMode(follower2.elementType == ModelElementType.ENUMERATION)
        if (
            (follower1 == null || follower1.elementType == ModelElementType.ENUMERATION) &&
            (follower2 == null || follower2.elementType == ModelElementType.ENUMERATION)
        ) visitor.visitEnumeration(
            leader as EnumerationModel,
            follower1 as EnumerationModel?,
            follower2 as EnumerationModel?,
        )
        else null
    }
    ModelElementType.ASSOCIATION -> {
        if (follower1 != null) assertInDevMode(follower1.elementType == ModelElementType.ASSOCIATION)
        if (follower2 != null) assertInDevMode(follower2.elementType == ModelElementType.ASSOCIATION)
        if (
            (follower1 == null || follower1.elementType == ModelElementType.ASSOCIATION) &&
            (follower2 == null || follower2.elementType == ModelElementType.ASSOCIATION)
        ) visitor.visitAssociation(
            leader as AssociationModel,
            follower1 as AssociationModel?,
            follower2 as AssociationModel?,
        )
        else null
    }
    ModelElementType.ENTITY_KEYS -> {
        if (follower1 != null) assertInDevMode(follower1.elementType == ModelElementType.ENTITY_KEYS)
        if (follower2 != null) assertInDevMode(follower2.elementType == ModelElementType.ENTITY_KEYS)
        if (
            (follower1 == null || follower1.elementType == ModelElementType.ENTITY_KEYS) &&
            (follower2 == null || follower2.elementType == ModelElementType.ENTITY_KEYS)
        ) visitor.visitEntityKeys(
            leader as EntityKeysModel,
            follower1 as EntityKeysModel?,
            follower2 as EntityKeysModel?,
        )
        else null
    }
}

fun <Return> dispatchLeave(
    leader: ElementModel,
    follower1: ElementModel?,
    follower2: ElementModel?,
    visitor: Leader1Follower2ModelVisitor<Return>
) {
    when (leader.elementType) {
        ModelElementType.MAIN -> {
            if (follower1 != null) assertInDevMode(follower1.elementType == ModelElementType.MAIN)
            if (follower2 != null) assertInDevMode(follower2.elementType == ModelElementType.MAIN)
            if (
                (follower1 == null || follower1.elementType == ModelElementType.MAIN) &&
                (follower2 == null || follower2.elementType == ModelElementType.MAIN)
            ) visitor.leaveMain(
                leader as MainModel,
                follower1 as MainModel?,
                follower2 as MainModel?,
            )
        }
        ModelElementType.ENTITY -> {
            if (follower1 != null) assertInDevMode(follower1.elementType == ModelElementType.ENTITY)
            if (follower2 != null) assertInDevMode(follower2.elementType == ModelElementType.ENTITY)
            if (
                (follower1 == null || follower1.elementType == ModelElementType.ENTITY) &&
                (follower2 == null || follower2.elementType == ModelElementType.ENTITY)
            ) visitor.leaveEntity(
                leader as EntityModel,
                follower1 as EntityModel?,
                follower2 as EntityModel?,
            )
        }
        ModelElementType.SINGLE_FIELD -> {
            if (follower1 != null) assertInDevMode(follower1.elementType == ModelElementType.SINGLE_FIELD)
            if (follower2 != null) assertInDevMode(follower2.elementType == ModelElementType.SINGLE_FIELD)
            if (
                (follower1 == null || follower1.elementType == ModelElementType.SINGLE_FIELD) &&
                (follower2 == null || follower2.elementType == ModelElementType.SINGLE_FIELD)
            ) visitor.leaveSingleField(
                leader as SingleFieldModel,
                follower1 as SingleFieldModel?,
                follower2 as SingleFieldModel?,
            )
        }
        ModelElementType.LIST_FIELD -> {
            if (follower1 != null) assertInDevMode(follower1.elementType == ModelElementType.LIST_FIELD)
            if (follower2 != null) assertInDevMode(follower2.elementType == ModelElementType.LIST_FIELD)
            if (
                (follower1 == null || follower1.elementType == ModelElementType.LIST_FIELD) &&
                (follower2 == null || follower2.elementType == ModelElementType.LIST_FIELD)
            ) visitor.leaveListField(
                leader as ListFieldModel,
                follower1 as ListFieldModel?,
                follower2 as ListFieldModel?,
            )
        }
        ModelElementType.SET_FIELD -> {
            if (follower1 != null) assertInDevMode(follower1.elementType == ModelElementType.SET_FIELD)
            if (follower2 != null) assertInDevMode(follower2.elementType == ModelElementType.SET_FIELD)
            if (
                (follower1 == null || follower1.elementType == ModelElementType.SET_FIELD) &&
                (follower2 == null || follower2.elementType == ModelElementType.SET_FIELD)
            ) visitor.leaveSetField(
                leader as SetFieldModel,
                follower1 as SetFieldModel?,
                follower2 as SetFieldModel?,
            )
        }
        ModelElementType.PRIMITIVE -> {
            if (follower1 != null) assertInDevMode(follower1.elementType == ModelElementType.PRIMITIVE)
            if (follower2 != null) assertInDevMode(follower2.elementType == ModelElementType.PRIMITIVE)
            if (
                (follower1 == null || follower1.elementType == ModelElementType.PRIMITIVE) &&
                (follower2 == null || follower2.elementType == ModelElementType.PRIMITIVE)
            ) visitor.leavePrimitive(
                leader as PrimitiveModel,
                follower1 as PrimitiveModel?,
                follower2 as PrimitiveModel?,
            )
        }
        ModelElementType.ALIAS -> {
            if (follower1 != null) assertInDevMode(follower1.elementType == ModelElementType.ALIAS)
            if (follower2 != null) assertInDevMode(follower2.elementType == ModelElementType.ALIAS)
            if (
                (follower1 == null || follower1.elementType == ModelElementType.ALIAS) &&
                (follower2 == null || follower2.elementType == ModelElementType.ALIAS)
            ) visitor.leaveAlias(
                leader as AliasModel,
                follower1 as AliasModel?,
                follower2 as AliasModel?,
            )
        }
        ModelElementType.PASSWORD1WAY -> {
            if (follower1 != null) assertInDevMode(follower1.elementType == ModelElementType.PASSWORD1WAY)
            if (follower2 != null) assertInDevMode(follower2.elementType == ModelElementType.PASSWORD1WAY)
            if (
                (follower1 == null || follower1.elementType == ModelElementType.PASSWORD1WAY) &&
                (follower2 == null || follower2.elementType == ModelElementType.PASSWORD1WAY)
            ) visitor.leavePassword1way(
                leader as Password1wayModel,
                follower1 as Password1wayModel?,
                follower2 as Password1wayModel?,
            )
        }
        ModelElementType.PASSWORD2WAY -> {
            if (follower1 != null) assertInDevMode(follower1.elementType == ModelElementType.PASSWORD2WAY)
            if (follower2 != null) assertInDevMode(follower2.elementType == ModelElementType.PASSWORD2WAY)
            if (
                (follower1 == null || follower1.elementType == ModelElementType.PASSWORD2WAY) &&
                (follower2 == null || follower2.elementType == ModelElementType.PASSWORD2WAY)
            ) visitor.leavePassword2way(
                leader as Password2wayModel,
                follower1 as Password2wayModel?,
                follower2 as Password2wayModel?,
            )
        }
        ModelElementType.ENUMERATION -> {
            if (follower1 != null) assertInDevMode(follower1.elementType == ModelElementType.ENUMERATION)
            if (follower2 != null) assertInDevMode(follower2.elementType == ModelElementType.ENUMERATION)
            if (
                (follower1 == null || follower1.elementType == ModelElementType.ENUMERATION) &&
                (follower2 == null || follower2.elementType == ModelElementType.ENUMERATION)
            ) visitor.leaveEnumeration(
                leader as EnumerationModel,
                follower1 as EnumerationModel?,
                follower2 as EnumerationModel?,
            )
        }
        ModelElementType.ASSOCIATION -> {
            if (follower1 != null) assertInDevMode(follower1.elementType == ModelElementType.ASSOCIATION)
            if (follower2 != null) assertInDevMode(follower2.elementType == ModelElementType.ASSOCIATION)
            if (
                (follower1 == null || follower1.elementType == ModelElementType.ASSOCIATION) &&
                (follower2 == null || follower2.elementType == ModelElementType.ASSOCIATION)
            ) visitor.leaveAssociation(
                leader as AssociationModel,
                follower1 as AssociationModel?,
                follower2 as AssociationModel?,
            )
        }
        ModelElementType.ENTITY_KEYS -> {
            if (follower1 != null) assertInDevMode(follower1.elementType == ModelElementType.ENTITY_KEYS)
            if (follower2 != null) assertInDevMode(follower2.elementType == ModelElementType.ENTITY_KEYS)
            if (
                (follower1 == null || follower1.elementType == ModelElementType.ENTITY_KEYS) &&
                (follower2 == null || follower2.elementType == ModelElementType.ENTITY_KEYS)
            ) visitor.leaveEntityKeys(
                leader as EntityKeysModel,
                follower1 as EntityKeysModel?,
                follower2 as EntityKeysModel?,
            )
        }
    }
}