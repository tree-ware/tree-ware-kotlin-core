package org.treeWare.model.traversal

import org.treeWare.model.core.*
import org.treeWare.model.cursor.CursorMoveDirection
import org.treeWare.model.cursor.FollowerModelCursor
import org.treeWare.model.cursor.Leader1ModelCursor

fun forEach(
    leader: ElementModel,
    follower: ElementModel,
    visitor: Leader1Follower1ModelVisitor<TraversalAction>
): TraversalAction {
    val leaderCursor = Leader1ModelCursor(leader)
    val followerCursor = FollowerModelCursor(follower)
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

fun <Return> dispatchVisit(
    leader: ElementModel,
    follower: ElementModel?,
    visitor: Leader1Follower1ModelVisitor<Return>
): Return? = when (leader.elementType) {
    ModelElementType.MAIN -> {
        if (follower != null) assert(follower.elementType == ModelElementType.MAIN)
        if (follower == null || follower.elementType == ModelElementType.MAIN) visitor.visitMain(
            leader as MainModel,
            follower as MainModel?
        )
        else null
    }
    ModelElementType.ROOT -> {
        if (follower != null) assert(follower.elementType == ModelElementType.ROOT)
        if (follower == null || follower.elementType == ModelElementType.ROOT) visitor.visitRoot(
            leader as RootModel,
            follower as RootModel?
        )
        else null
    }
    ModelElementType.ENTITY -> {
        if (follower != null) assert(follower.elementType == ModelElementType.ENTITY)
        if (follower == null || follower.elementType == ModelElementType.ENTITY) visitor.visitEntity(
            leader as EntityModel,
            follower as EntityModel?
        )
        else null
    }
    ModelElementType.SINGLE_FIELD -> {
        if (follower != null) assert(follower.elementType == ModelElementType.SINGLE_FIELD)
        if (follower == null || follower.elementType == ModelElementType.SINGLE_FIELD) visitor.visitSingleField(
            leader as SingleFieldModel,
            follower as SingleFieldModel?
        )
        else null
    }
    ModelElementType.LIST_FIELD -> {
        if (follower != null) assert(follower.elementType == ModelElementType.LIST_FIELD)
        if (follower == null || follower.elementType == ModelElementType.LIST_FIELD) visitor.visitListField(
            leader as ListFieldModel,
            follower as ListFieldModel?
        )
        else null
    }
    ModelElementType.SET_FIELD -> {
        if (follower != null) assert(follower.elementType == ModelElementType.SET_FIELD)
        if (follower == null || follower.elementType == ModelElementType.SET_FIELD) visitor.visitSetField(
            leader as SetFieldModel,
            follower as SetFieldModel?
        )
        else null
    }
    ModelElementType.PRIMITIVE -> {
        if (follower != null) assert(follower.elementType == ModelElementType.PRIMITIVE)
        if (follower == null || follower.elementType == ModelElementType.PRIMITIVE) visitor.visitPrimitive(
            leader as PrimitiveModel,
            follower as PrimitiveModel?
        )
        else null
    }
    ModelElementType.ALIAS -> {
        if (follower != null) assert(follower.elementType == ModelElementType.ALIAS)
        if (follower == null || follower.elementType == ModelElementType.ALIAS) visitor.visitAlias(
            leader as AliasModel,
            follower as AliasModel?
        )
        else null
    }
    ModelElementType.PASSWORD1WAY -> {
        if (follower != null) assert(follower.elementType == ModelElementType.PASSWORD1WAY)
        if (follower == null || follower.elementType == ModelElementType.PASSWORD1WAY) visitor.visitPassword1way(
            leader as Password1wayModel,
            follower as Password1wayModel?
        )
        else null
    }
    ModelElementType.PASSWORD2WAY -> {
        if (follower != null) assert(follower.elementType == ModelElementType.PASSWORD2WAY)
        if (follower == null || follower.elementType == ModelElementType.PASSWORD2WAY) visitor.visitPassword2way(
            leader as Password2wayModel,
            follower as Password2wayModel?
        )
        else null
    }
    ModelElementType.ENUMERATION -> {
        if (follower != null) assert(follower.elementType == ModelElementType.ENUMERATION)
        if (follower == null || follower.elementType == ModelElementType.ENUMERATION) visitor.visitEnumeration(
            leader as EnumerationModel,
            follower as EnumerationModel?
        )
        else null
    }
    ModelElementType.ASSOCIATION -> {
        if (follower != null) assert(follower.elementType == ModelElementType.ASSOCIATION)
        if (follower == null || follower.elementType == ModelElementType.ASSOCIATION) visitor.visitAssociation(
            leader as AssociationModel,
            follower as AssociationModel?
        )
        else null
    }
    ModelElementType.ENTITY_KEYS -> {
        if (follower != null) assert(follower.elementType == ModelElementType.ENTITY_KEYS)
        if (follower == null || follower.elementType == ModelElementType.ENTITY_KEYS) visitor.visitEntityKeys(
            leader as EntityKeysModel,
            follower as EntityKeysModel?
        )
        else null
    }
}

fun <Return> dispatchLeave(
    leader: ElementModel,
    follower: ElementModel?,
    visitor: Leader1Follower1ModelVisitor<Return>
) {
    when (leader.elementType) {
        ModelElementType.MAIN -> {
            if (follower != null) assert(follower.elementType == ModelElementType.MAIN)
            if (follower == null || follower.elementType == ModelElementType.MAIN) visitor.leaveMain(
                leader as MainModel,
                follower as MainModel?
            )
        }
        ModelElementType.ROOT -> {
            if (follower != null) assert(follower.elementType == ModelElementType.ROOT)
            if (follower == null || follower.elementType == ModelElementType.ROOT) visitor.leaveRoot(
                leader as RootModel,
                follower as RootModel?
            )
        }
        ModelElementType.ENTITY -> {
            if (follower != null) assert(follower.elementType == ModelElementType.ENTITY)
            if (follower == null || follower.elementType == ModelElementType.ENTITY) visitor.leaveEntity(
                leader as EntityModel,
                follower as EntityModel?
            )
        }
        ModelElementType.SINGLE_FIELD -> {
            if (follower != null) assert(follower.elementType == ModelElementType.SINGLE_FIELD)
            if (follower == null || follower.elementType == ModelElementType.SINGLE_FIELD) visitor.leaveSingleField(
                leader as SingleFieldModel,
                follower as SingleFieldModel?
            )
        }
        ModelElementType.LIST_FIELD -> {
            if (follower != null) assert(follower.elementType == ModelElementType.LIST_FIELD)
            if (follower == null || follower.elementType == ModelElementType.LIST_FIELD) visitor.leaveListField(
                leader as ListFieldModel,
                follower as ListFieldModel?
            )
        }
        ModelElementType.SET_FIELD -> {
            if (follower != null) assert(follower.elementType == ModelElementType.SET_FIELD)
            if (follower == null || follower.elementType == ModelElementType.SET_FIELD) visitor.leaveSetField(
                leader as SetFieldModel,
                follower as SetFieldModel?
            )
        }
        ModelElementType.PRIMITIVE -> {
            if (follower != null) assert(follower.elementType == ModelElementType.PRIMITIVE)
            if (follower == null || follower.elementType == ModelElementType.PRIMITIVE) visitor.leavePrimitive(
                leader as PrimitiveModel,
                follower as PrimitiveModel?
            )
        }
        ModelElementType.ALIAS -> {
            if (follower != null) assert(follower.elementType == ModelElementType.ALIAS)
            if (follower == null || follower.elementType == ModelElementType.ALIAS) visitor.leaveAlias(
                leader as AliasModel,
                follower as AliasModel?
            )
        }
        ModelElementType.PASSWORD1WAY -> {
            if (follower != null) assert(follower.elementType == ModelElementType.PASSWORD1WAY)
            if (follower == null || follower.elementType == ModelElementType.PASSWORD1WAY) visitor.leavePassword1way(
                leader as Password1wayModel,
                follower as Password1wayModel?
            )
        }
        ModelElementType.PASSWORD2WAY -> {
            if (follower != null) assert(follower.elementType == ModelElementType.PASSWORD2WAY)
            if (follower == null || follower.elementType == ModelElementType.PASSWORD2WAY) visitor.leavePassword2way(
                leader as Password2wayModel,
                follower as Password2wayModel?
            )
        }
        ModelElementType.ENUMERATION -> {
            if (follower != null) assert(follower.elementType == ModelElementType.ENUMERATION)
            if (follower == null || follower.elementType == ModelElementType.ENUMERATION) visitor.leaveEnumeration(
                leader as EnumerationModel,
                follower as EnumerationModel?
            )
        }
        ModelElementType.ASSOCIATION -> {
            if (follower != null) assert(follower.elementType == ModelElementType.ASSOCIATION)
            if (follower == null || follower.elementType == ModelElementType.ASSOCIATION) visitor.leaveAssociation(
                leader as AssociationModel,
                follower as AssociationModel?
            )
        }
        ModelElementType.ENTITY_KEYS -> {
            if (follower != null) assert(follower.elementType == ModelElementType.ENTITY_KEYS)
            if (follower == null || follower.elementType == ModelElementType.ENTITY_KEYS) visitor.leaveEntityKeys(
                leader as EntityKeysModel,
                follower as EntityKeysModel?
            )
        }
    }
}
