package org.treeWare.model.cursor

import org.treeWare.metaModel.getMetaName
import org.treeWare.model.core.*
import org.treeWare.model.traversal.AbstractLeader1Follower0ModelVisitor
import org.treeWare.model.traversal.dispatchVisit
import java.util.*

class FollowerModelCursor<LeaderAux, FollowerAux>(private val initial: ElementModel<FollowerAux>) {
    private val stateStack = FollowerStateStack<LeaderAux, FollowerAux>()
    private val stateFactoryVisitor = FollowerStateFactoryVisitor(stateStack)
    private var isAtStart = true

    val element: ElementModel<FollowerAux>? get() = stateStack.peekFirst()?.element

    fun follow(move: LeaderModelCursorMove<LeaderAux>): FollowerModelCursorMove<FollowerAux>? = when {
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

private typealias FollowerStateStack<LeaderAux, FollowerAux> = ArrayDeque<FollowerState<LeaderAux, FollowerAux>>

private abstract class FollowerState<LeaderAux, FollowerAux>(
    val element: ElementModel<FollowerAux>?,
    protected val stateStack: FollowerStateStack<LeaderAux, FollowerAux>
) {
    abstract val visitCursorMove: FollowerModelCursorMove<FollowerAux>

    open fun follow(move: LeaderModelCursorMove<LeaderAux>): FollowerModelCursorMove<FollowerAux>? {
        throw IllegalStateException("Unknown move $move in state $this")
    }
}

private class NullFollowerState<LeaderAux, FollowerAux>(
    override val visitCursorMove: FollowerModelCursorMove<FollowerAux>,
    stateStack: FollowerStateStack<LeaderAux, FollowerAux>
) : FollowerState<LeaderAux, FollowerAux>(null, stateStack) {
    override fun follow(move: LeaderModelCursorMove<LeaderAux>): FollowerModelCursorMove<FollowerAux> {
        if (move.direction == CursorMoveDirection.VISIT) stateStack.addFirst(this) else stateStack.pollFirst()
        return FollowerModelCursorMove(move.direction, null)
    }
}

private class MainFollowerState<LeaderAux, FollowerAux>(
    private val main: MainModel<FollowerAux>,
    stateStack: FollowerStateStack<LeaderAux, FollowerAux>,
    private val stateFactoryVisitor: FollowerStateFactoryVisitor<LeaderAux, FollowerAux>
) : FollowerState<LeaderAux, FollowerAux>(main, stateStack) {
    override val visitCursorMove = FollowerModelCursorMove(CursorMoveDirection.VISIT, main)

    override fun follow(move: LeaderModelCursorMove<LeaderAux>) = when {
        move.direction == CursorMoveDirection.LEAVE && move.element.elementType == ModelElementType.MAIN -> {
            stateStack.pollFirst()
            FollowerModelCursorMove(CursorMoveDirection.LEAVE, main)
        }
        move.direction == CursorMoveDirection.VISIT && move.element.elementType == ModelElementType.ROOT -> {
            val rootState =
                dispatchVisit(main.root, stateFactoryVisitor) ?: throw IllegalStateException("null root state")
            stateStack.addFirst(rootState)
            rootState.visitCursorMove
        }
        else -> super.follow(move)
    }
}

private abstract class BaseEntityFollowerState<LeaderAux, FollowerAux>(
    private val baseEntity: BaseEntityModel<FollowerAux>,
    stack: FollowerStateStack<LeaderAux, FollowerAux>,
    private val stateFactoryVisitor: FollowerStateFactoryVisitor<LeaderAux, FollowerAux>
) : FollowerState<LeaderAux, FollowerAux>(baseEntity, stack) {
    override fun follow(move: LeaderModelCursorMove<LeaderAux>) = when (move.direction) {
        CursorMoveDirection.VISIT -> when (move.element.elementType) {
            ModelElementType.SINGLE_FIELD -> visitField(
                move.element as SingleFieldModel<LeaderAux>,
                FollowerModelCursorMove(CursorMoveDirection.VISIT, null)
            )
            ModelElementType.LIST_FIELD -> visitField(
                move.element as ListFieldModel<LeaderAux>,
                FollowerModelCursorMove(CursorMoveDirection.VISIT, null)
            )
            ModelElementType.SET_FIELD -> visitField(
                move.element as SetFieldModel<LeaderAux>,
                FollowerModelCursorMove(CursorMoveDirection.VISIT, null)
            )
            else -> super.follow(move)
        }
        else -> super.follow(move)
    }

    private fun visitField(
        leaderField: FieldModel<LeaderAux>,
        nullVisitCursorMove: FollowerModelCursorMove<FollowerAux>
    ): FollowerModelCursorMove<FollowerAux> {
        val leaderMeta = leaderField.meta ?: throw IllegalStateException("Meta is missing for leader field")
        val followerField = baseEntity.getField(getMetaName(leaderMeta))
        val fieldState = if (followerField == null) NullFollowerState(nullVisitCursorMove, stateStack)
        else dispatchVisit(followerField, stateFactoryVisitor) ?: throw IllegalStateException("null field state")
        stateStack.addFirst(fieldState)
        return fieldState.visitCursorMove
    }
}

private class RootFollowerState<LeaderAux, FollowerAux>(
    private val root: RootModel<FollowerAux>,
    stateStack: FollowerStateStack<LeaderAux, FollowerAux>,
    stateFactoryVisitor: FollowerStateFactoryVisitor<LeaderAux, FollowerAux>
) : BaseEntityFollowerState<LeaderAux, FollowerAux>(root, stateStack, stateFactoryVisitor) {
    override val visitCursorMove = FollowerModelCursorMove(CursorMoveDirection.VISIT, root)

    override fun follow(move: LeaderModelCursorMove<LeaderAux>) = when {
        move.direction == CursorMoveDirection.LEAVE && move.element.elementType == ModelElementType.ROOT -> {
            stateStack.pollFirst()
            FollowerModelCursorMove(CursorMoveDirection.LEAVE, root)
        }
        else -> super.follow(move)
    }
}

private class EntityFollowerState<LeaderAux, FollowerAux>(
    private val entity: EntityModel<FollowerAux>,
    stateStack: FollowerStateStack<LeaderAux, FollowerAux>,
    stateFactoryVisitor: FollowerStateFactoryVisitor<LeaderAux, FollowerAux>
) : BaseEntityFollowerState<LeaderAux, FollowerAux>(entity, stateStack, stateFactoryVisitor) {
    override val visitCursorMove = FollowerModelCursorMove(CursorMoveDirection.VISIT, entity)

    override fun follow(move: LeaderModelCursorMove<LeaderAux>) = when {
        move.direction == CursorMoveDirection.LEAVE && move.element.elementType == ModelElementType.ENTITY -> {
            stateStack.pollFirst()
            FollowerModelCursorMove(CursorMoveDirection.LEAVE, entity)
        }
        else -> super.follow(move)
    }
}

// Fields

private class SingleFieldFollowerState<LeaderAux, FollowerAux>(
    private val field: SingleFieldModel<FollowerAux>,
    stack: FollowerStateStack<LeaderAux, FollowerAux>,
    private val stateFactoryVisitor: FollowerStateFactoryVisitor<LeaderAux, FollowerAux>
) : FollowerState<LeaderAux, FollowerAux>(field, stack) {
    override val visitCursorMove = FollowerModelCursorMove(CursorMoveDirection.VISIT, field)

    override fun follow(move: LeaderModelCursorMove<LeaderAux>) = when (move.direction) {
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

private class ListFieldFollowerState<LeaderAux, FollowerAux>(
    private val field: ListFieldModel<FollowerAux>,
    stack: FollowerStateStack<LeaderAux, FollowerAux>,
    private val stateFactoryVisitor: FollowerStateFactoryVisitor<LeaderAux, FollowerAux>
) : FollowerState<LeaderAux, FollowerAux>(field, stack) {
    override val visitCursorMove = FollowerModelCursorMove(CursorMoveDirection.VISIT, field)

    override fun follow(move: LeaderModelCursorMove<LeaderAux>) = when (move.direction) {
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
                    move.element as? ElementModel<LeaderAux> ?: throw IllegalStateException("expected list field value")
                val followerValue: ElementModel<FollowerAux>? = field.getValueMatching(leaderValue)
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

private class SetFieldFollowerState<LeaderAux, FollowerAux>(
    private val field: SetFieldModel<FollowerAux>,
    stack: FollowerStateStack<LeaderAux, FollowerAux>,
    private val stateFactoryVisitor: FollowerStateFactoryVisitor<LeaderAux, FollowerAux>
) : FollowerState<LeaderAux, FollowerAux>(field, stack) {
    override val visitCursorMove = FollowerModelCursorMove(CursorMoveDirection.VISIT, field)

    override fun follow(move: LeaderModelCursorMove<LeaderAux>) = when (move.direction) {
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
                    move.element as? ElementModel<LeaderAux> ?: throw IllegalStateException("expected set field value")
                val followerValue: ElementModel<FollowerAux>? = field.getValueMatching(leaderValue)
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

private class ScalarValueFollowerState<LeaderAux, FollowerAux>(
    private val value: ElementModel<FollowerAux>,
    stack: FollowerStateStack<LeaderAux, FollowerAux>
) : FollowerState<LeaderAux, FollowerAux>(value, stack) {
    override val visitCursorMove = FollowerModelCursorMove(CursorMoveDirection.VISIT, value)

    override fun follow(move: LeaderModelCursorMove<LeaderAux>) = when (move.direction) {
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

private class EntityKeysFollowerState<LeaderAux, FollowerAux>(
    private val entityKeys: EntityKeysModel<FollowerAux>,
    stateStack: FollowerStateStack<LeaderAux, FollowerAux>,
    stateFactoryVisitor: FollowerStateFactoryVisitor<LeaderAux, FollowerAux>
) : BaseEntityFollowerState<LeaderAux, FollowerAux>(entityKeys, stateStack, stateFactoryVisitor) {
    override val visitCursorMove = FollowerModelCursorMove(CursorMoveDirection.VISIT, entityKeys)

    override fun follow(move: LeaderModelCursorMove<LeaderAux>) = when {
        move.direction == CursorMoveDirection.LEAVE && move.element.elementType == ModelElementType.ENTITY_KEYS -> {
            stateStack.pollFirst()
            FollowerModelCursorMove(CursorMoveDirection.LEAVE, entityKeys)
        }
        else -> super.follow(move)
    }
}

// State factory visitor

private class FollowerStateFactoryVisitor<LeaderAux, FollowerAux>(
    private val stateStack: FollowerStateStack<LeaderAux, FollowerAux>
) : AbstractLeader1Follower0ModelVisitor<FollowerAux, FollowerState<LeaderAux, FollowerAux>?>(null) {
    override fun visit(leaderMain1: MainModel<FollowerAux>) = MainFollowerState(leaderMain1, stateStack, this)
    override fun visit(leaderRoot1: RootModel<FollowerAux>) = RootFollowerState(leaderRoot1, stateStack, this)
    override fun visit(leaderEntity1: EntityModel<FollowerAux>) = EntityFollowerState(leaderEntity1, stateStack, this)

    // Fields

    override fun visit(leaderField1: SingleFieldModel<FollowerAux>) =
        SingleFieldFollowerState(leaderField1, stateStack, this)

    override fun visit(leaderField1: ListFieldModel<FollowerAux>) =
        ListFieldFollowerState(leaderField1, stateStack, this)

    override fun visit(leaderField1: SetFieldModel<FollowerAux>) =
        SetFieldFollowerState(leaderField1, stateStack, this)

    // Values

    override fun visit(leaderValue1: PrimitiveModel<FollowerAux>) = ScalarValueFollowerState(leaderValue1, stateStack)
    override fun visit(leaderValue1: AliasModel<FollowerAux>) = ScalarValueFollowerState(leaderValue1, stateStack)
    override fun visit(leaderValue1: Password1wayModel<FollowerAux>) =
        ScalarValueFollowerState(leaderValue1, stateStack)

    override fun visit(leaderValue1: Password2wayModel<FollowerAux>) =
        ScalarValueFollowerState(leaderValue1, stateStack)

    override fun visit(leaderValue1: EnumerationModel<FollowerAux>) = ScalarValueFollowerState(leaderValue1, stateStack)
    override fun visit(leaderValue1: AssociationModel<FollowerAux>) = ScalarValueFollowerState(leaderValue1, stateStack)

    // Sub-values

    override fun visit(leaderEntityKeys1: EntityKeysModel<FollowerAux>) =
        EntityKeysFollowerState(leaderEntityKeys1, stateStack, this)
}
