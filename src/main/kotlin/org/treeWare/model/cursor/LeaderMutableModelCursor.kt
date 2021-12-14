package org.treeWare.model.cursor

import org.treeWare.model.core.*
import org.treeWare.model.traversal.AbstractLeader1MutableModelVisitor
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.model.traversal.dispatchVisit
import java.util.*

class LeaderMutableModelCursor(private val initial: MutableElementModel) {
    private val stateStack = LeaderMutableModelStateStack()
    private val stateFactoryVisitor = LeaderMutableModelStateFactoryVisitor(stateStack)
    private var isAtStart = true

    val element: MutableElementModel? get() = if (stateStack.isEmpty()) null else stateStack.peekFirst().element

    fun next(previousAction: TraversalAction): LeaderMutableModelCursorMove? = when {
        previousAction == TraversalAction.ABORT_SUB_TREE -> {
            // Remove current state from the stack to abort its sub-tree.
            val currentState = stateStack.pollFirst()
            currentState.leaveCursorMove
        }
        isAtStart -> {
            isAtStart = false
            val initialState =
                dispatchVisit(initial, stateFactoryVisitor) ?: throw IllegalStateException("null initial state")
            stateStack.addFirst(initialState)
            initialState.visitCursorMove
        }
        stateStack.isNotEmpty() -> {
            val state = stateStack.peekFirst()
            state.next()
        }
        else -> {
            // The stack is empty, and we are not at the start. This means the model has been traversed.
            // So there are no more moves.
            null
        }
    }
}

private typealias LeaderMutableModelStateStack = ArrayDeque<LeaderMutableModelState>
private typealias LeaderMutableModelStateAction = () -> LeaderMutableModelCursorMove?

private abstract class LeaderMutableModelState(
    val element: MutableElementModel,
    val stateStack: LeaderMutableModelStateStack
) {
    abstract val visitCursorMove: LeaderMutableModelCursorMove
    abstract val leaveCursorMove: LeaderMutableModelCursorMove
    protected abstract val actionIterator: Iterator<LeaderMutableModelStateAction>

    fun next(): LeaderMutableModelCursorMove? = if (actionIterator.hasNext()) {
        val action = actionIterator.next()
        action()
    } else {
        // Remove self from stack
        stateStack.pollFirst()
        leaveCursorMove
    }
}

private class MainLeaderMutableModelState(
    main: MutableMainModel,
    stateStack: LeaderMutableModelStateStack,
    stateFactoryVisitor: LeaderMutableModelStateFactoryVisitor
) : LeaderMutableModelState(main, stateStack) {
    override val visitCursorMove = LeaderMutableModelCursorMove(CursorMoveDirection.VISIT, main)
    override val leaveCursorMove = LeaderMutableModelCursorMove(CursorMoveDirection.LEAVE, main)
    override val actionIterator: Iterator<LeaderMutableModelStateAction>

    init {
        val actionList = listOf<LeaderMutableModelStateAction> {
            val rootState =
                dispatchVisit(main.root, stateFactoryVisitor) ?: throw IllegalStateException("null root state")
            stateStack.addFirst(rootState)
            rootState.visitCursorMove
        }
        actionIterator = actionList.iterator()
    }
}

private abstract class BaseEntityLeaderMutableModelState(
    baseEntity: MutableBaseEntityModel,
    stack: LeaderMutableModelStateStack,
    stateFactoryVisitor: LeaderMutableModelStateFactoryVisitor
) : LeaderMutableModelState(baseEntity, stack) {
    final override val actionIterator: Iterator<LeaderMutableModelStateAction>

    init {
        actionIterator = IteratorAdapter({ baseEntity.fields.values.iterator() }) { field ->
            {
                val fieldState =
                    dispatchVisit(field, stateFactoryVisitor) ?: throw IllegalStateException("null field state")
                stateStack.addFirst(fieldState)
                fieldState.visitCursorMove
            }
        }
    }
}

private class RootLeaderMutableModelState(
    root: MutableRootModel,
    stack: LeaderMutableModelStateStack,
    stateFactoryVisitor: LeaderMutableModelStateFactoryVisitor
) : BaseEntityLeaderMutableModelState(root, stack, stateFactoryVisitor) {
    override val visitCursorMove = LeaderMutableModelCursorMove(CursorMoveDirection.VISIT, root)
    override val leaveCursorMove = LeaderMutableModelCursorMove(CursorMoveDirection.LEAVE, root)
}

private class EntityLeaderMutableModelState(
    entity: MutableEntityModel,
    stack: LeaderMutableModelStateStack,
    stateFactoryVisitor: LeaderMutableModelStateFactoryVisitor
) : BaseEntityLeaderMutableModelState(entity, stack, stateFactoryVisitor) {
    override val visitCursorMove = LeaderMutableModelCursorMove(CursorMoveDirection.VISIT, entity)
    override val leaveCursorMove = LeaderMutableModelCursorMove(CursorMoveDirection.LEAVE, entity)
}

// Fields

private class SingleFieldLeaderMutableModelState(
    field: MutableSingleFieldModel,
    stack: LeaderMutableModelStateStack,
    stateFactoryVisitor: LeaderMutableModelStateFactoryVisitor
) : LeaderMutableModelState(field, stack) {
    override val visitCursorMove = LeaderMutableModelCursorMove(CursorMoveDirection.VISIT, field)
    override val leaveCursorMove = LeaderMutableModelCursorMove(CursorMoveDirection.LEAVE, field)
    override val actionIterator: Iterator<LeaderMutableModelStateAction>

    init {
        val value = field.value
        val actionList =
            if (value == null) listOf()
            else listOf<LeaderMutableModelStateAction> {
                val valueState =
                    dispatchVisit(value, stateFactoryVisitor) ?: throw IllegalStateException("null root state")
                stateStack.addFirst(valueState)
                valueState.visitCursorMove
            }
        actionIterator = actionList.iterator()
    }
}

private class ListFieldLeaderMutableModelState(
    field: MutableListFieldModel,
    stack: LeaderMutableModelStateStack,
    stateFactoryVisitor: LeaderMutableModelStateFactoryVisitor
) : LeaderMutableModelState(field, stack) {
    override val visitCursorMove = LeaderMutableModelCursorMove(CursorMoveDirection.VISIT, field)
    override val leaveCursorMove = LeaderMutableModelCursorMove(CursorMoveDirection.LEAVE, field)
    override val actionIterator: Iterator<LeaderMutableModelStateAction>

    init {
        actionIterator = IteratorAdapter({ field.values.iterator() }) { value ->
            {
                val valueState =
                    dispatchVisit(value, stateFactoryVisitor) ?: throw IllegalStateException("null field state")
                stateStack.addFirst(valueState)
                valueState.visitCursorMove
            }
        }
    }
}

private class SetFieldLeaderMutableModelState(
    field: MutableSetFieldModel,
    stack: LeaderMutableModelStateStack,
    stateFactoryVisitor: LeaderMutableModelStateFactoryVisitor
) : LeaderMutableModelState(field, stack) {
    override val visitCursorMove = LeaderMutableModelCursorMove(CursorMoveDirection.VISIT, field)
    override val leaveCursorMove = LeaderMutableModelCursorMove(CursorMoveDirection.LEAVE, field)
    override val actionIterator: Iterator<LeaderMutableModelStateAction>

    init {
        actionIterator = IteratorAdapter({ field.values.iterator() }) { value ->
            {
                val valueState =
                    dispatchVisit(value, stateFactoryVisitor) ?: throw IllegalStateException("null field state")
                stateStack.addFirst(valueState)
                valueState.visitCursorMove
            }
        }
    }
}

// Values

private class ScalarValueLeaderMutableModelState(
    value: MutableElementModel,
    stack: LeaderMutableModelStateStack
) : LeaderMutableModelState(value, stack) {
    override val visitCursorMove = LeaderMutableModelCursorMove(CursorMoveDirection.VISIT, value)
    override val leaveCursorMove = LeaderMutableModelCursorMove(CursorMoveDirection.LEAVE, value)
    override val actionIterator: Iterator<LeaderMutableModelStateAction>

    init {
        val actionList = listOf<LeaderMutableModelStateAction>()
        actionIterator = actionList.iterator()
    }
}

// Sub-values

private class EntityKeysLeaderMutableModelState(
    entityKeys: MutableEntityKeysModel,
    stack: LeaderMutableModelStateStack,
    stateFactoryVisitor: LeaderMutableModelStateFactoryVisitor
) : BaseEntityLeaderMutableModelState(entityKeys, stack, stateFactoryVisitor) {
    override val visitCursorMove = LeaderMutableModelCursorMove(CursorMoveDirection.VISIT, entityKeys)
    override val leaveCursorMove = LeaderMutableModelCursorMove(CursorMoveDirection.LEAVE, entityKeys)
}

// State factory visitor

private class LeaderMutableModelStateFactoryVisitor(
    private val stateStack: LeaderMutableModelStateStack
) : AbstractLeader1MutableModelVisitor<LeaderMutableModelState?>(null) {
    override fun visit(leaderMain1: MutableMainModel) = MainLeaderMutableModelState(leaderMain1, stateStack, this)
    override fun visit(leaderRoot1: MutableRootModel) = RootLeaderMutableModelState(leaderRoot1, stateStack, this)
    override fun visit(leaderEntity1: MutableEntityModel) =
        EntityLeaderMutableModelState(leaderEntity1, stateStack, this)

    // Fields

    override fun visit(leaderField1: MutableSingleFieldModel) =
        SingleFieldLeaderMutableModelState(leaderField1, stateStack, this)

    override fun visit(leaderField1: MutableListFieldModel) =
        ListFieldLeaderMutableModelState(leaderField1, stateStack, this)

    override fun visit(leaderField1: MutableSetFieldModel) =
        SetFieldLeaderMutableModelState(leaderField1, stateStack, this)

    // Values

    override fun visit(leaderValue1: MutablePrimitiveModel) =
        ScalarValueLeaderMutableModelState(leaderValue1, stateStack)

    override fun visit(leaderValue1: MutableAliasModel) = ScalarValueLeaderMutableModelState(leaderValue1, stateStack)
    override fun visit(leaderValue1: MutablePassword1wayModel) =
        ScalarValueLeaderMutableModelState(leaderValue1, stateStack)

    override fun visit(leaderValue1: MutablePassword2wayModel) =
        ScalarValueLeaderMutableModelState(leaderValue1, stateStack)

    override fun visit(leaderValue1: MutableEnumerationModel) =
        ScalarValueLeaderMutableModelState(leaderValue1, stateStack)

    override fun visit(leaderValue1: MutableAssociationModel) =
        ScalarValueLeaderMutableModelState(leaderValue1, stateStack)

    // Sub-values

    override fun visit(leaderEntityKeys1: MutableEntityKeysModel) =
        EntityKeysLeaderMutableModelState(leaderEntityKeys1, stateStack, this)
}
