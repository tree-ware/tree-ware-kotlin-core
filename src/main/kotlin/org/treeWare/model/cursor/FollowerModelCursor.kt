package org.treeWare.model.cursor

import org.treeWare.metaModel.getMetaName
import org.treeWare.model.core.*
import org.treeWare.model.traversal.AbstractLeader1ModelVisitor
import org.treeWare.model.traversal.dispatchVisit
import java.util.*

class FollowerModelCursor(private val initial: ElementModel) {
    private val stateStack = FollowerStateStack()
    private val stateFactoryVisitor = FollowerStateFactoryVisitor(stateStack)
    private var isAtStart = true

    val element: ElementModel? get() = stateStack.peekFirst()?.element

    fun follow(move: Leader1ModelCursorMove): FollowerModelCursorMove? = when {
        isAtStart -> {
            isAtStart = false
            val initialState =
                dispatchVisit(initial, stateFactoryVisitor) ?: throw IllegalStateException("null initial state")
            stateStack.addFirst(initialState)
            initialState.visitCursorMove
        }
        stateStack.isNotEmpty() -> {
            val state = stateStack.peekFirst()
            state.follow(move)
        }
        else -> {
            // The stack is empty, and we are not at the start. This means the model has been traversed.
            // So there are no more moves.
            null
        }
    }
}

private typealias FollowerStateStack = ArrayDeque<FollowerState>

private abstract class FollowerState(
    val element: ElementModel?,
    protected val stateStack: FollowerStateStack
) {
    abstract val visitCursorMove: FollowerModelCursorMove

    open fun follow(move: Leader1ModelCursorMove): FollowerModelCursorMove? {
        throw IllegalStateException("Unknown move $move in state $this")
    }
}

private class NullFollowerState(
    override val visitCursorMove: FollowerModelCursorMove,
    stateStack: FollowerStateStack
) : FollowerState(null, stateStack) {
    override fun follow(move: Leader1ModelCursorMove): FollowerModelCursorMove {
        if (move.direction == CursorMoveDirection.VISIT) stateStack.addFirst(this) else stateStack.pollFirst()
        return FollowerModelCursorMove(move.direction, null)
    }
}

private class MainFollowerState(
    private val main: MainModel,
    stateStack: FollowerStateStack,
    private val stateFactoryVisitor: FollowerStateFactoryVisitor
) : FollowerState(main, stateStack) {
    override val visitCursorMove = FollowerModelCursorMove(CursorMoveDirection.VISIT, main)

    override fun follow(move: Leader1ModelCursorMove) = when {
        move.direction == CursorMoveDirection.LEAVE && move.element.elementType == ModelElementType.MAIN -> {
            stateStack.pollFirst()
            FollowerModelCursorMove(CursorMoveDirection.LEAVE, main)
        }
        move.direction == CursorMoveDirection.VISIT && move.element.elementType == ModelElementType.ENTITY -> {
            val rootState = main.value?.let { dispatchVisit(it, stateFactoryVisitor) }
                ?: throw IllegalStateException("null root state")
            stateStack.addFirst(rootState)
            rootState.visitCursorMove
        }
        else -> super.follow(move)
    }
}

private abstract class BaseEntityFollowerState(
    private val baseEntity: BaseEntityModel,
    stack: FollowerStateStack,
    private val stateFactoryVisitor: FollowerStateFactoryVisitor
) : FollowerState(baseEntity, stack) {
    override fun follow(move: Leader1ModelCursorMove) = when (move.direction) {
        CursorMoveDirection.VISIT -> when (move.element.elementType) {
            ModelElementType.SINGLE_FIELD -> visitField(
                move.element as SingleFieldModel,
                FollowerModelCursorMove(CursorMoveDirection.VISIT, null)
            )
            ModelElementType.LIST_FIELD -> visitField(
                move.element as ListFieldModel,
                FollowerModelCursorMove(CursorMoveDirection.VISIT, null)
            )
            ModelElementType.SET_FIELD -> visitField(
                move.element as SetFieldModel,
                FollowerModelCursorMove(CursorMoveDirection.VISIT, null)
            )
            else -> super.follow(move)
        }
        else -> super.follow(move)
    }

    private fun visitField(
        leaderField: FieldModel,
        nullVisitCursorMove: FollowerModelCursorMove
    ): FollowerModelCursorMove {
        val leaderMeta = leaderField.meta ?: throw IllegalStateException("Meta is missing for leader field")
        val followerField = baseEntity.getField(getMetaName(leaderMeta))
        val fieldState = if (followerField == null) NullFollowerState(nullVisitCursorMove, stateStack)
        else dispatchVisit(followerField, stateFactoryVisitor) ?: throw IllegalStateException("null field state")
        stateStack.addFirst(fieldState)
        return fieldState.visitCursorMove
    }
}

private class RootFollowerState(
    private val root: RootModel,
    stateStack: FollowerStateStack,
    stateFactoryVisitor: FollowerStateFactoryVisitor
) : BaseEntityFollowerState(root, stateStack, stateFactoryVisitor) {
    override val visitCursorMove = FollowerModelCursorMove(CursorMoveDirection.VISIT, root)

    override fun follow(move: Leader1ModelCursorMove) = when {
        move.direction == CursorMoveDirection.LEAVE && move.element.elementType == ModelElementType.ROOT -> {
            stateStack.pollFirst()
            FollowerModelCursorMove(CursorMoveDirection.LEAVE, root)
        }
        else -> super.follow(move)
    }
}

private class EntityFollowerState(
    private val entity: EntityModel,
    stateStack: FollowerStateStack,
    stateFactoryVisitor: FollowerStateFactoryVisitor
) : BaseEntityFollowerState(entity, stateStack, stateFactoryVisitor) {
    override val visitCursorMove = FollowerModelCursorMove(CursorMoveDirection.VISIT, entity)

    override fun follow(move: Leader1ModelCursorMove) = when {
        move.direction == CursorMoveDirection.LEAVE && move.element.elementType == ModelElementType.ENTITY -> {
            stateStack.pollFirst()
            FollowerModelCursorMove(CursorMoveDirection.LEAVE, entity)
        }
        else -> super.follow(move)
    }
}

// Fields

private class SingleFieldFollowerState(
    private val field: SingleFieldModel,
    stack: FollowerStateStack,
    private val stateFactoryVisitor: FollowerStateFactoryVisitor
) : FollowerState(field, stack) {
    override val visitCursorMove = FollowerModelCursorMove(CursorMoveDirection.VISIT, field)

    override fun follow(move: Leader1ModelCursorMove) = when (move.direction) {
        CursorMoveDirection.LEAVE -> when (move.element.elementType) {
            ModelElementType.SINGLE_FIELD -> {
                stateStack.pollFirst()
                FollowerModelCursorMove(CursorMoveDirection.LEAVE, field)
            }
            else -> super.follow(move)
        }
        CursorMoveDirection.VISIT -> when (move.element.elementType) {
            ModelElementType.PRIMITIVE,
            ModelElementType.ALIAS,
            ModelElementType.PASSWORD1WAY,
            ModelElementType.PASSWORD2WAY,
            ModelElementType.ENUMERATION,
            ModelElementType.ASSOCIATION -> {
                val followerValue = field.value
                val elementState =
                    if (followerValue == null) NullFollowerState(
                        FollowerModelCursorMove(CursorMoveDirection.VISIT, null),
                        stateStack
                    )
                    else dispatchVisit(followerValue, stateFactoryVisitor)
                        ?: throw IllegalStateException("null element state")
                stateStack.addFirst(elementState)
                elementState.visitCursorMove
            }
            ModelElementType.ENTITY -> {
                val followerEntity = field.value
                val elementState =
                    if (followerEntity == null) NullFollowerState(
                        FollowerModelCursorMove(CursorMoveDirection.VISIT, null),
                        stateStack
                    )
                    else dispatchVisit(followerEntity, stateFactoryVisitor)
                        ?: throw IllegalStateException("null element state")
                stateStack.addFirst(elementState)
                elementState.visitCursorMove
            }
            else -> super.follow(move)
        }
    }
}

private class ListFieldFollowerState(
    private val field: ListFieldModel,
    stack: FollowerStateStack,
    private val stateFactoryVisitor: FollowerStateFactoryVisitor
) : FollowerState(field, stack) {
    override val visitCursorMove = FollowerModelCursorMove(CursorMoveDirection.VISIT, field)

    override fun follow(move: Leader1ModelCursorMove) = when (move.direction) {
        CursorMoveDirection.LEAVE -> when (move.element.elementType) {
            ModelElementType.LIST_FIELD -> {
                stateStack.pollFirst()
                FollowerModelCursorMove(CursorMoveDirection.LEAVE, field)
            }
            else -> super.follow(move)
        }
        CursorMoveDirection.VISIT -> when (move.element.elementType) {
            ModelElementType.PRIMITIVE,
            ModelElementType.ALIAS,
            ModelElementType.PASSWORD1WAY,
            ModelElementType.PASSWORD2WAY,
            ModelElementType.ENUMERATION,
            ModelElementType.ASSOCIATION -> {
                val leaderValue =
                    move.element as? ElementModel ?: throw IllegalStateException("expected list field value")
                val followerValue: ElementModel? = field.getValueMatching(leaderValue)
                val elementState =
                    if (followerValue == null) NullFollowerState(
                        FollowerModelCursorMove(CursorMoveDirection.VISIT, null),
                        stateStack
                    )
                    else dispatchVisit(followerValue, stateFactoryVisitor)
                        ?: throw IllegalStateException("null element state")
                stateStack.addFirst(elementState)
                elementState.visitCursorMove
            }
            ModelElementType.ENTITY -> {
                val leaderEntity = move.element
                val followerEntity = field.getValueMatching(leaderEntity)
                val elementState =
                    if (followerEntity == null) NullFollowerState(
                        FollowerModelCursorMove(CursorMoveDirection.VISIT, null),
                        stateStack
                    )
                    else dispatchVisit(followerEntity, stateFactoryVisitor)
                        ?: throw IllegalStateException("null element state")
                stateStack.addFirst(elementState)
                elementState.visitCursorMove
            }
            else -> super.follow(move)
        }
    }
}

private class SetFieldFollowerState(
    private val field: SetFieldModel,
    stack: FollowerStateStack,
    private val stateFactoryVisitor: FollowerStateFactoryVisitor
) : FollowerState(field, stack) {
    override val visitCursorMove = FollowerModelCursorMove(CursorMoveDirection.VISIT, field)

    override fun follow(move: Leader1ModelCursorMove) = when (move.direction) {
        CursorMoveDirection.LEAVE -> when (move.element.elementType) {
            ModelElementType.SET_FIELD -> {
                stateStack.pollFirst()
                FollowerModelCursorMove(CursorMoveDirection.LEAVE, field)
            }
            else -> super.follow(move)
        }
        CursorMoveDirection.VISIT -> when (move.element.elementType) {
            ModelElementType.PRIMITIVE,
            ModelElementType.ALIAS,
            ModelElementType.PASSWORD1WAY,
            ModelElementType.PASSWORD2WAY,
            ModelElementType.ENUMERATION,
            ModelElementType.ASSOCIATION -> {
                val leaderValue =
                    move.element as? ElementModel ?: throw IllegalStateException("expected set field value")
                val followerValue: ElementModel? = field.getValueMatching(leaderValue)
                val elementState =
                    if (followerValue == null) NullFollowerState(
                        FollowerModelCursorMove(CursorMoveDirection.VISIT, null),
                        stateStack
                    )
                    else dispatchVisit(followerValue, stateFactoryVisitor)
                        ?: throw IllegalStateException("null element state")
                stateStack.addFirst(elementState)
                elementState.visitCursorMove
            }
            ModelElementType.ENTITY -> {
                val leaderEntity = move.element
                val followerEntity = field.getValueMatching(leaderEntity)
                val elementState =
                    if (followerEntity == null) NullFollowerState(
                        FollowerModelCursorMove(CursorMoveDirection.VISIT, null),
                        stateStack
                    )
                    else dispatchVisit(followerEntity, stateFactoryVisitor)
                        ?: throw IllegalStateException("null element state")
                stateStack.addFirst(elementState)
                elementState.visitCursorMove
            }
            else -> super.follow(move)
        }
    }
}

// Values

private class ScalarValueFollowerState(
    private val value: ElementModel,
    stack: FollowerStateStack
) : FollowerState(value, stack) {
    override val visitCursorMove = FollowerModelCursorMove(CursorMoveDirection.VISIT, value)

    override fun follow(move: Leader1ModelCursorMove) = when (move.direction) {
        CursorMoveDirection.LEAVE -> when (move.element.elementType) {
            ModelElementType.PRIMITIVE,
            ModelElementType.ALIAS,
            ModelElementType.PASSWORD1WAY,
            ModelElementType.PASSWORD2WAY,
            ModelElementType.ENUMERATION,
            ModelElementType.ASSOCIATION -> {
                stateStack.pollFirst()
                FollowerModelCursorMove(CursorMoveDirection.LEAVE, value)
            }
            else -> super.follow(move)
        }
        else -> super.follow(move)
    }
}

// Sub-values

private class EntityKeysFollowerState(
    private val entityKeys: EntityKeysModel,
    stateStack: FollowerStateStack,
    stateFactoryVisitor: FollowerStateFactoryVisitor
) : BaseEntityFollowerState(entityKeys, stateStack, stateFactoryVisitor) {
    override val visitCursorMove = FollowerModelCursorMove(CursorMoveDirection.VISIT, entityKeys)

    override fun follow(move: Leader1ModelCursorMove) = when {
        move.direction == CursorMoveDirection.LEAVE && move.element.elementType == ModelElementType.ENTITY_KEYS -> {
            stateStack.pollFirst()
            FollowerModelCursorMove(CursorMoveDirection.LEAVE, entityKeys)
        }
        else -> super.follow(move)
    }
}

// State factory visitor

private class FollowerStateFactoryVisitor(
    private val stateStack: FollowerStateStack
) : AbstractLeader1ModelVisitor<FollowerState?>(null) {
    override fun visitMain(leaderMain1: MainModel) = MainFollowerState(leaderMain1, stateStack, this)
    override fun visitRoot(leaderRoot1: RootModel) = RootFollowerState(leaderRoot1, stateStack, this)
    override fun visitEntity(leaderEntity1: EntityModel) = EntityFollowerState(leaderEntity1, stateStack, this)

    // Fields

    override fun visitSingleField(leaderField1: SingleFieldModel) =
        SingleFieldFollowerState(leaderField1, stateStack, this)

    override fun visitListField(leaderField1: ListFieldModel) =
        ListFieldFollowerState(leaderField1, stateStack, this)

    override fun visitSetField(leaderField1: SetFieldModel) =
        SetFieldFollowerState(leaderField1, stateStack, this)

    // Values

    override fun visitPrimitive(leaderValue1: PrimitiveModel) = ScalarValueFollowerState(leaderValue1, stateStack)
    override fun visitAlias(leaderValue1: AliasModel) = ScalarValueFollowerState(leaderValue1, stateStack)
    override fun visitPassword1way(leaderValue1: Password1wayModel) =
        ScalarValueFollowerState(leaderValue1, stateStack)

    override fun visitPassword2way(leaderValue1: Password2wayModel) =
        ScalarValueFollowerState(leaderValue1, stateStack)

    override fun visitEnumeration(leaderValue1: EnumerationModel) = ScalarValueFollowerState(leaderValue1, stateStack)
    override fun visitAssociation(leaderValue1: AssociationModel) = ScalarValueFollowerState(leaderValue1, stateStack)

    // Sub-values

    override fun visitEntityKeys(leaderEntityKeys1: EntityKeysModel) =
        EntityKeysFollowerState(leaderEntityKeys1, stateStack, this)
}
