package org.treeWare.model.cursor

import org.treeWare.model.core.*
import org.treeWare.model.traversal.AbstractLeader1Follower0ModelVisitor
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.model.traversal.dispatchVisit
import java.util.*

class LeaderModelCursor(private val initial: ElementModel) {
    private val stateStack = LeaderStateStack()
    private val stateFactoryVisitor = LeaderStateFactoryVisitor(stateStack)
    private var isAtStart = true

    val element: ElementModel? get() = if (stateStack.isEmpty()) null else stateStack.peekFirst().element

    fun next(previousAction: TraversalAction): LeaderModelCursorMove? = when {
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

private typealias LeaderStateStack = ArrayDeque<LeaderState>
private typealias LeaderStateAction = () -> LeaderModelCursorMove?

private abstract class LeaderState(val element: ElementModel, val stateStack: LeaderStateStack) {
    abstract val visitCursorMove: LeaderModelCursorMove
    abstract val leaveCursorMove: LeaderModelCursorMove
    protected abstract val actionIterator: Iterator<LeaderStateAction>

    fun next(): LeaderModelCursorMove? = if (actionIterator.hasNext()) {
        val action = actionIterator.next()
        action()
    } else {
        // Remove self from stack
        stateStack.pollFirst()
        leaveCursorMove
    }
}

private class MainLeaderState(
    main: MainModel,
    stateStack: LeaderStateStack,
    stateFactoryVisitor: LeaderStateFactoryVisitor
) : LeaderState(main, stateStack) {
    override val visitCursorMove = LeaderModelCursorMove(CursorMoveDirection.VISIT, main)
    override val leaveCursorMove = LeaderModelCursorMove(CursorMoveDirection.LEAVE, main)
    override val actionIterator: Iterator<LeaderStateAction>

    init {
        val actionList = listOf<LeaderStateAction> {
            val rootState =
                dispatchVisit(main.root, stateFactoryVisitor) ?: throw IllegalStateException("null root state")
            stateStack.addFirst(rootState)
            rootState.visitCursorMove
        }
        actionIterator = actionList.iterator()
    }
}

private abstract class BaseEntityLeaderState(
    baseEntity: BaseEntityModel,
    stack: LeaderStateStack,
    stateFactoryVisitor: LeaderStateFactoryVisitor
) : LeaderState(baseEntity, stack) {
    final override val actionIterator: Iterator<LeaderStateAction>

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

private class RootLeaderState(
    root: RootModel,
    stack: LeaderStateStack,
    stateFactoryVisitor: LeaderStateFactoryVisitor
) : BaseEntityLeaderState(root, stack, stateFactoryVisitor) {
    override val visitCursorMove = LeaderModelCursorMove(CursorMoveDirection.VISIT, root)
    override val leaveCursorMove = LeaderModelCursorMove(CursorMoveDirection.LEAVE, root)
}

private class EntityLeaderState(
    entity: EntityModel,
    stack: LeaderStateStack,
    stateFactoryVisitor: LeaderStateFactoryVisitor
) : BaseEntityLeaderState(entity, stack, stateFactoryVisitor) {
    override val visitCursorMove = LeaderModelCursorMove(CursorMoveDirection.VISIT, entity)
    override val leaveCursorMove = LeaderModelCursorMove(CursorMoveDirection.LEAVE, entity)
}

// Fields

private class SingleFieldLeaderState(
    field: SingleFieldModel,
    stack: LeaderStateStack,
    stateFactoryVisitor: LeaderStateFactoryVisitor
) : LeaderState(field, stack) {
    override val visitCursorMove = LeaderModelCursorMove(CursorMoveDirection.VISIT, field)
    override val leaveCursorMove = LeaderModelCursorMove(CursorMoveDirection.LEAVE, field)
    override val actionIterator: Iterator<LeaderStateAction>

    init {
        val value = field.value
        val actionList =
            if (value == null) listOf()
            else listOf<LeaderStateAction> {
                val valueState =
                    dispatchVisit(value, stateFactoryVisitor) ?: throw IllegalStateException("null root state")
                stateStack.addFirst(valueState)
                valueState.visitCursorMove
            }
        actionIterator = actionList.iterator()
    }
}

private class ListFieldLeaderState(
    field: ListFieldModel,
    stack: LeaderStateStack,
    stateFactoryVisitor: LeaderStateFactoryVisitor
) : LeaderState(field, stack) {
    override val visitCursorMove = LeaderModelCursorMove(CursorMoveDirection.VISIT, field)
    override val leaveCursorMove = LeaderModelCursorMove(CursorMoveDirection.LEAVE, field)
    override val actionIterator: Iterator<LeaderStateAction>

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

private class SetFieldLeaderState(
    field: SetFieldModel,
    stack: LeaderStateStack,
    stateFactoryVisitor: LeaderStateFactoryVisitor
) : LeaderState(field, stack) {
    override val visitCursorMove = LeaderModelCursorMove(CursorMoveDirection.VISIT, field)
    override val leaveCursorMove = LeaderModelCursorMove(CursorMoveDirection.LEAVE, field)
    override val actionIterator: Iterator<LeaderStateAction>

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

private class ScalarValueLeaderState(
    value: ElementModel,
    stack: LeaderStateStack
) : LeaderState(value, stack) {
    override val visitCursorMove = LeaderModelCursorMove(CursorMoveDirection.VISIT, value)
    override val leaveCursorMove = LeaderModelCursorMove(CursorMoveDirection.LEAVE, value)
    override val actionIterator: Iterator<LeaderStateAction>

    init {
        val actionList = listOf<LeaderStateAction>()
        actionIterator = actionList.iterator()
    }
}

// Sub-values

private class EntityKeysLeaderState(
    entityKeys: EntityKeysModel,
    stack: LeaderStateStack,
    stateFactoryVisitor: LeaderStateFactoryVisitor
) : BaseEntityLeaderState(entityKeys, stack, stateFactoryVisitor) {
    override val visitCursorMove = LeaderModelCursorMove(CursorMoveDirection.VISIT, entityKeys)
    override val leaveCursorMove = LeaderModelCursorMove(CursorMoveDirection.LEAVE, entityKeys)
}

// State factory visitor

private class LeaderStateFactoryVisitor(
    private val stateStack: LeaderStateStack
) : AbstractLeader1Follower0ModelVisitor<LeaderState?>(null) {
    override fun visit(leaderMain1: MainModel) = MainLeaderState(leaderMain1, stateStack, this)
    override fun visit(leaderRoot1: RootModel) = RootLeaderState(leaderRoot1, stateStack, this)
    override fun visit(leaderEntity1: EntityModel) = EntityLeaderState(leaderEntity1, stateStack, this)

    // Fields

    override fun visit(leaderField1: SingleFieldModel) = SingleFieldLeaderState(leaderField1, stateStack, this)
    override fun visit(leaderField1: ListFieldModel) = ListFieldLeaderState(leaderField1, stateStack, this)
    override fun visit(leaderField1: SetFieldModel) = SetFieldLeaderState(leaderField1, stateStack, this)

    // Values

    override fun visit(leaderValue1: PrimitiveModel) = ScalarValueLeaderState(leaderValue1, stateStack)
    override fun visit(leaderValue1: AliasModel) = ScalarValueLeaderState(leaderValue1, stateStack)
    override fun visit(leaderValue1: Password1wayModel) = ScalarValueLeaderState(leaderValue1, stateStack)
    override fun visit(leaderValue1: Password2wayModel) = ScalarValueLeaderState(leaderValue1, stateStack)
    override fun visit(leaderValue1: EnumerationModel) = ScalarValueLeaderState(leaderValue1, stateStack)
    override fun visit(leaderValue1: AssociationModel) = ScalarValueLeaderState(leaderValue1, stateStack)

    // Sub-values

    override fun visit(leaderEntityKeys1: EntityKeysModel) =
        EntityKeysLeaderState(leaderEntityKeys1, stateStack, this)
}
