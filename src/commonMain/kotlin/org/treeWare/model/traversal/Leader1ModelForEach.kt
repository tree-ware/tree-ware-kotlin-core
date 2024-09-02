package org.treeWare.model.traversal

import org.treeWare.metaModel.getMetaName
import org.treeWare.model.core.*
import org.treeWare.model.cursor.CursorMoveDirection
import org.treeWare.model.cursor.Leader1ModelCursor

fun forEach(
    leader: ElementModel,
    visitor: Leader1ModelVisitor<TraversalAction>,
    traverseAssociations: Boolean,
    logPrefix: String? = null // logs dispatchVisit() and dispatchLeave() if not null
): TraversalAction {
    val leaderCursor = Leader1ModelCursor(leader, traverseAssociations)
    var action = TraversalAction.CONTINUE
    while (action != TraversalAction.ABORT_TREE) {
        val leaderMove = leaderCursor.next(action) ?: break
        action = when (leaderMove.direction) {
            CursorMoveDirection.VISIT -> dispatchVisit(leaderMove.element, visitor, logPrefix)
                ?: TraversalAction.ABORT_TREE
            CursorMoveDirection.LEAVE -> {
                dispatchLeave(leaderMove.element, visitor, logPrefix)
                TraversalAction.CONTINUE
            }
        }
    }
    return action
}

fun <Return> dispatchVisit(
    leader: ElementModel,
    visitor: Leader1ModelVisitor<Return>,
    logPrefix: String? = null // logs if not null
): Return? = when (leader.elementType.also { if (logPrefix != null) logDispatch(logPrefix, "->", leader) }) {
    ModelElementType.ENTITY -> {
        visitor.visitEntity(leader as EntityModel)
    }
    ModelElementType.SINGLE_FIELD -> {
        visitor.visitSingleField(leader as SingleFieldModel)
    }
    ModelElementType.SET_FIELD -> {
        visitor.visitSetField(leader as SetFieldModel)
    }
    ModelElementType.PRIMITIVE -> {
        visitor.visitPrimitive(leader as PrimitiveModel)
    }
    ModelElementType.ALIAS -> {
        visitor.visitAlias(leader as AliasModel)
    }
    ModelElementType.PASSWORD1WAY -> {
        visitor.visitPassword1way(leader as Password1wayModel)
    }
    ModelElementType.PASSWORD2WAY -> {
        visitor.visitPassword2way(leader as Password2wayModel)
    }
    ModelElementType.ENUMERATION -> {
        visitor.visitEnumeration(leader as EnumerationModel)
    }
    ModelElementType.ASSOCIATION -> {
        visitor.visitAssociation(leader as AssociationModel)
    }
}

fun <Return> dispatchLeave(
    leader: ElementModel,
    visitor: Leader1ModelVisitor<Return>,
    logPrefix: String? = null // logs if not null
) {
    when (leader.elementType.also { if (logPrefix != null) logDispatch(logPrefix, "<-", leader) }) {
        ModelElementType.ENTITY -> {
            visitor.leaveEntity(leader as EntityModel)
        }
        ModelElementType.SINGLE_FIELD -> {
            visitor.leaveSingleField(leader as SingleFieldModel)
        }
        ModelElementType.SET_FIELD -> {
            visitor.leaveSetField(leader as SetFieldModel)
        }
        ModelElementType.PRIMITIVE -> {
            visitor.leavePrimitive(leader as PrimitiveModel)
        }
        ModelElementType.ALIAS -> {
            visitor.leaveAlias(leader as AliasModel)
        }
        ModelElementType.PASSWORD1WAY -> {
            visitor.leavePassword1way(leader as Password1wayModel)
        }
        ModelElementType.PASSWORD2WAY -> {
            visitor.leavePassword2way(leader as Password2wayModel)
        }
        ModelElementType.ENUMERATION -> {
            visitor.leaveEnumeration(leader as EnumerationModel)
        }
        ModelElementType.ASSOCIATION -> {
            visitor.leaveAssociation(leader as AssociationModel)
        }
    }
}

private fun logDispatch(logPrefix: String, direction: String, leader: ElementModel) {
    println("$logPrefix $direction ${leader.elementType} ${(leader.meta as? BaseEntityModel)?.let { getMetaName(it) }}")
    System.out.flush()
}