package org.treeWare.model.cursor

import org.treeWare.metaModel.getMetaName
import org.treeWare.model.core.*
import org.treeWare.model.operator.AbstractLeader1Follower0ModelVisitor
import org.treeWare.model.operator.dispatchVisit
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

// TODO(performance): when()s in this file should check enums instead classes.

private class NullFollowerState<LeaderAux, FollowerAux>(
    override val visitCursorMove: FollowerModelCursorMove<FollowerAux>,
    stateStack: FollowerStateStack<LeaderAux, FollowerAux>
) : FollowerState<LeaderAux, FollowerAux>(null, stateStack) {
    override fun follow(move: LeaderModelCursorMove<LeaderAux>): FollowerModelCursorMove<FollowerAux> = when (move) {
        is VisitLeaderMainModel -> {
            stateStack.addFirst(this)
            VisitFollowerMainModel(null)
        }
        is LeaveLeaderMainModel -> {
            stateStack.pollFirst()
            LeaveFollowerMainModel(null)
        }
        is VisitLeaderRootModel -> {
            stateStack.addFirst(this)
            VisitFollowerRootModel(null)
        }
        is LeaveLeaderRootModel -> {
            stateStack.pollFirst()
            LeaveFollowerRootModel(null)
        }
        is VisitLeaderEntityModel -> {
            stateStack.addFirst(this)
            VisitFollowerEntityModel(null)
        }
        is LeaveLeaderEntityModel -> {
            stateStack.pollFirst()
            LeaveFollowerEntityModel(null)
        }
        is VisitLeaderSingleFieldModel -> {
            stateStack.addFirst(this)
            VisitFollowerSingleFieldModel(null)
        }
        is LeaveLeaderSingleFieldModel -> {
            stateStack.pollFirst()
            LeaveFollowerSingleFieldModel(null)
        }
        is VisitLeaderListFieldModel -> {
            stateStack.addFirst(this)
            VisitFollowerListFieldModel(null)
        }
        is LeaveLeaderListFieldModel -> {
            stateStack.pollFirst()
            LeaveFollowerListFieldModel(null)
        }
        is VisitLeaderValueModel -> {
            stateStack.addFirst(this)
            VisitFollowerValueModel(null)
        }
        is LeaveLeaderValueModel -> {
            stateStack.pollFirst()
            LeaveFollowerValueModel(null)
        }
        is VisitLeaderEntityKeysModel -> {
            stateStack.addFirst(this)
            VisitFollowerEntityKeysModel(null)
        }
        is LeaveLeaderEntityKeysModel -> {
            stateStack.pollFirst()
            LeaveFollowerEntityKeysModel(null)
        }
    }
}

private class MainFollowerState<LeaderAux, FollowerAux>(
    private val main: MainModel<FollowerAux>,
    stateStack: FollowerStateStack<LeaderAux, FollowerAux>,
    private val stateFactoryVisitor: FollowerStateFactoryVisitor<LeaderAux, FollowerAux>
) : FollowerState<LeaderAux, FollowerAux>(main, stateStack) {
    override val visitCursorMove = VisitFollowerMainModel(main)

    override fun follow(move: LeaderModelCursorMove<LeaderAux>) = when (move) {
        is LeaveLeaderMainModel -> {
            stateStack.pollFirst()
            LeaveFollowerMainModel(main)
        }
        is VisitLeaderRootModel -> {
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
    override fun follow(move: LeaderModelCursorMove<LeaderAux>) = when (move) {
        is VisitLeaderSingleFieldModel -> visitField(move.element, VisitFollowerSingleFieldModel(null))
        is VisitLeaderListFieldModel -> visitField(move.element, VisitFollowerListFieldModel(null))
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
    override val visitCursorMove = VisitFollowerRootModel(root)

    override fun follow(move: LeaderModelCursorMove<LeaderAux>) = when (move) {
        is LeaveLeaderRootModel -> {
            stateStack.pollFirst()
            LeaveFollowerRootModel(root)
        }
        else -> super.follow(move)
    }
}

private class EntityFollowerState<LeaderAux, FollowerAux>(
    private val entity: EntityModel<FollowerAux>,
    stateStack: FollowerStateStack<LeaderAux, FollowerAux>,
    stateFactoryVisitor: FollowerStateFactoryVisitor<LeaderAux, FollowerAux>
) : BaseEntityFollowerState<LeaderAux, FollowerAux>(entity, stateStack, stateFactoryVisitor) {
    override val visitCursorMove = VisitFollowerEntityModel(entity)

    override fun follow(move: LeaderModelCursorMove<LeaderAux>) = when (move) {
        is LeaveLeaderEntityModel -> {
            stateStack.pollFirst()
            LeaveFollowerEntityModel(entity)
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
    override val visitCursorMove = VisitFollowerSingleFieldModel(field)

    override fun follow(move: LeaderModelCursorMove<LeaderAux>) = when (move) {
        is LeaveLeaderSingleFieldModel -> {
            stateStack.pollFirst()
            LeaveFollowerSingleFieldModel(field)
        }
        is VisitLeaderValueModel -> {
            val followerValue = field.value
            val elementState =
                if (followerValue == null) NullFollowerState(VisitFollowerValueModel(null), stateStack)
                else dispatchVisit(followerValue, stateFactoryVisitor)
                    ?: throw IllegalStateException("null element state")
            stateStack.addFirst(elementState)
            elementState.visitCursorMove
        }
        is VisitLeaderEntityModel -> {
            val followerEntity = field.value
            val elementState =
                if (followerEntity == null) NullFollowerState(VisitFollowerEntityModel(null), stateStack)
                else dispatchVisit(followerEntity, stateFactoryVisitor)
                    ?: throw IllegalStateException("null element state")
            stateStack.addFirst(elementState)
            elementState.visitCursorMove
        }
        else -> super.follow(move)
    }
}

private class ListFieldFollowerState<LeaderAux, FollowerAux>(
    private val field: ListFieldModel<FollowerAux>,
    stack: FollowerStateStack<LeaderAux, FollowerAux>,
    private val stateFactoryVisitor: FollowerStateFactoryVisitor<LeaderAux, FollowerAux>
) : FollowerState<LeaderAux, FollowerAux>(field, stack) {
    override val visitCursorMove = VisitFollowerListFieldModel(field)

    override fun follow(move: LeaderModelCursorMove<LeaderAux>) = when (move) {
        is LeaveLeaderListFieldModel -> {
            stateStack.pollFirst()
            LeaveFollowerListFieldModel(field)
        }
        is VisitLeaderValueModel -> {
            val leaderValue =
                move.element as? ElementModel<LeaderAux> ?: throw IllegalStateException("expected list field value")
            val followerValue: ElementModel<FollowerAux>? = field.getValueMatching(leaderValue)
            val elementState =
                if (followerValue == null) NullFollowerState(VisitFollowerValueModel(null), stateStack)
                else dispatchVisit(followerValue, stateFactoryVisitor)
                    ?: throw IllegalStateException("null element state")
            stateStack.addFirst(elementState)
            elementState.visitCursorMove
        }
        is VisitLeaderEntityModel -> {
            val leaderEntity = move.element
            val followerEntity = field.getValueMatching(leaderEntity)
            val elementState =
                if (followerEntity == null) NullFollowerState(VisitFollowerEntityModel(null), stateStack)
                else dispatchVisit(followerEntity, stateFactoryVisitor)
                    ?: throw IllegalStateException("null element state")
            stateStack.addFirst(elementState)
            elementState.visitCursorMove
        }
        else -> super.follow(move)
    }
}

// Values

private class ScalarValueFollowerState<LeaderAux, FollowerAux>(
    private val value: ElementModel<FollowerAux>,
    stack: FollowerStateStack<LeaderAux, FollowerAux>
) : FollowerState<LeaderAux, FollowerAux>(value, stack) {
    override val visitCursorMove = VisitFollowerValueModel(value)

    override fun follow(move: LeaderModelCursorMove<LeaderAux>) = when (move) {
        is LeaveLeaderValueModel -> {
            stateStack.pollFirst()
            LeaveFollowerValueModel(value)
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
    override val visitCursorMove = VisitFollowerEntityKeysModel(entityKeys)

    override fun follow(move: LeaderModelCursorMove<LeaderAux>) = when (move) {
        is LeaveLeaderEntityKeysModel -> {
            stateStack.pollFirst()
            LeaveFollowerEntityKeysModel(entityKeys)
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

    // Values

    override fun visit(leaderValue1: PrimitiveModel<FollowerAux>) = ScalarValueFollowerState(leaderValue1, stateStack)
    override fun visit(leaderValue1: AliasModel<FollowerAux>) = ScalarValueFollowerState(leaderValue1, stateStack)
    override fun visit(leaderValue1: EnumerationModel<FollowerAux>) = ScalarValueFollowerState(leaderValue1, stateStack)
    override fun visit(leaderValue1: AssociationModel<FollowerAux>) = ScalarValueFollowerState(leaderValue1, stateStack)

    // Sub-values

    override fun visit(leaderEntityKeys1: EntityKeysModel<FollowerAux>) =
        EntityKeysFollowerState(leaderEntityKeys1, stateStack, this)
}
