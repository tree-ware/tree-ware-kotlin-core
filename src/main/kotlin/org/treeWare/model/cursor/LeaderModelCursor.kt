package org.treeWare.model.cursor

import org.treeWare.model.core.*
import org.treeWare.model.operator.AbstractLeader1Follower0ModelVisitor
import org.treeWare.model.operator.TraversalAction
import org.treeWare.model.operator.dispatchVisit
import java.util.*

class LeaderModelCursor<Aux>(private val initial: ElementModel<Aux>) {
    private val stateStack = LeaderStateStack<Aux>()
    private val stateFactoryVisitor = LeaderStateFactoryVisitor(stateStack)
    private var isAtStart = true

    val element: ElementModel<Aux>? get() = if (stateStack.isEmpty()) null else stateStack.peekFirst().element

    fun next(previousAction: TraversalAction): LeaderModelCursorMove<Aux>? = when {
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

private typealias LeaderStateStack<Aux> = ArrayDeque<LeaderState<Aux>>
private typealias LeaderStateAction<Aux> = () -> LeaderModelCursorMove<Aux>?

private class IteratorAdapter<T, R>(
    private val adapteeFactory: () -> Iterator<T>,
    private val transform: (T) -> R
) : Iterator<R> {
    private var _adaptee: Iterator<T>? = null
    private val adaptee: Iterator<T> get() = _adaptee ?: adapteeFactory().also { _adaptee = it }
    override fun hasNext(): Boolean = adaptee.hasNext()
    override fun next(): R = transform(adaptee.next())
}

private abstract class LeaderState<Aux>(val element: ElementModel<Aux>, val stateStack: LeaderStateStack<Aux>) {
    abstract val visitCursorMove: LeaderModelCursorMove<Aux>
    abstract val leaveCursorMove: LeaderModelCursorMove<Aux>
    protected abstract val actionIterator: Iterator<LeaderStateAction<Aux>>

    fun next(): LeaderModelCursorMove<Aux>? = if (actionIterator.hasNext()) {
        val action = actionIterator.next()
        action()
    } else {
        // Remove self from stack
        stateStack.pollFirst()
        leaveCursorMove
    }
}

private class MainLeaderState<Aux>(
    main: MainModel<Aux>,
    stateStack: LeaderStateStack<Aux>,
    stateFactoryVisitor: LeaderStateFactoryVisitor<Aux>
) : LeaderState<Aux>(main, stateStack) {
    override val visitCursorMove = VisitLeaderMainModel(main)
    override val leaveCursorMove = LeaveLeaderMainModel(main)
    override val actionIterator: Iterator<LeaderStateAction<Aux>>

    init {
        val actionList = listOf<LeaderStateAction<Aux>> {
            val rootState =
                dispatchVisit(main.root, stateFactoryVisitor) ?: throw IllegalStateException("null root state")
            stateStack.addFirst(rootState)
            rootState.visitCursorMove
        }
        actionIterator = actionList.iterator()
    }
}

private abstract class BaseEntityLeaderState<Aux>(
    baseEntity: BaseEntityModel<Aux>,
    stack: LeaderStateStack<Aux>,
    stateFactoryVisitor: LeaderStateFactoryVisitor<Aux>
) : LeaderState<Aux>(baseEntity, stack) {
    final override val actionIterator: Iterator<LeaderStateAction<Aux>>

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

private class RootLeaderState<Aux>(
    root: RootModel<Aux>,
    stack: LeaderStateStack<Aux>,
    stateFactoryVisitor: LeaderStateFactoryVisitor<Aux>
) : BaseEntityLeaderState<Aux>(root, stack, stateFactoryVisitor) {
    override val visitCursorMove = VisitLeaderRootModel(root)
    override val leaveCursorMove = LeaveLeaderRootModel(root)
}

private class EntityLeaderState<Aux>(
    entity: EntityModel<Aux>,
    stack: LeaderStateStack<Aux>,
    stateFactoryVisitor: LeaderStateFactoryVisitor<Aux>
) : BaseEntityLeaderState<Aux>(entity, stack, stateFactoryVisitor) {
    override val visitCursorMove = VisitLeaderEntityModel(entity)
    override val leaveCursorMove = LeaveLeaderEntityModel(entity)
}

// Fields

private class SingleFieldLeaderState<Aux>(
    field: SingleFieldModel<Aux>,
    stack: LeaderStateStack<Aux>,
    stateFactoryVisitor: LeaderStateFactoryVisitor<Aux>
) : LeaderState<Aux>(field, stack) {
    override val visitCursorMove = VisitLeaderSingleFieldModel(field)
    override val leaveCursorMove = LeaveLeaderSingleFieldModel(field)
    override val actionIterator: Iterator<LeaderStateAction<Aux>>

    init {
        val value = field.value
        val actionList =
            if (value == null) listOf()
            else listOf<LeaderStateAction<Aux>> {
                val valueState =
                    dispatchVisit(value, stateFactoryVisitor) ?: throw IllegalStateException("null root state")
                stateStack.addFirst(valueState)
                valueState.visitCursorMove
            }
        actionIterator = actionList.iterator()
    }
}

private class ListFieldLeaderState<Aux>(
    field: ListFieldModel<Aux>,
    stack: LeaderStateStack<Aux>,
    stateFactoryVisitor: LeaderStateFactoryVisitor<Aux>
) : LeaderState<Aux>(field, stack) {
    override val visitCursorMove = VisitLeaderListFieldModel(field)
    override val leaveCursorMove = LeaveLeaderListFieldModel(field)
    override val actionIterator: Iterator<LeaderStateAction<Aux>>

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

private class ScalarValueLeaderState<Aux>(
    value: ElementModel<Aux>,
    stack: LeaderStateStack<Aux>
) : LeaderState<Aux>(value, stack) {
    override val visitCursorMove = VisitLeaderValueModel(value)
    override val leaveCursorMove = LeaveLeaderValueModel(value)
    override val actionIterator: Iterator<LeaderStateAction<Aux>>

    init {
        val actionList = listOf<LeaderStateAction<Aux>>()
        actionIterator = actionList.iterator()
    }
}

// Sub-values

private class EntityKeysLeaderState<Aux>(
    entityKeys: EntityKeysModel<Aux>,
    stack: LeaderStateStack<Aux>,
    stateFactoryVisitor: LeaderStateFactoryVisitor<Aux>
) : BaseEntityLeaderState<Aux>(entityKeys, stack, stateFactoryVisitor) {
    override val visitCursorMove = VisitLeaderEntityKeysModel(entityKeys)
    override val leaveCursorMove = LeaveLeaderEntityKeysModel(entityKeys)
}

// State factory visitor

private class LeaderStateFactoryVisitor<Aux>(
    private val stateStack: LeaderStateStack<Aux>
) : AbstractLeader1Follower0ModelVisitor<Aux, LeaderState<Aux>?>(null) {
    override fun visit(leaderMain1: MainModel<Aux>) = MainLeaderState(leaderMain1, stateStack, this)
    override fun visit(leaderRoot1: RootModel<Aux>) = RootLeaderState(leaderRoot1, stateStack, this)
    override fun visit(leaderEntity1: EntityModel<Aux>) = EntityLeaderState(leaderEntity1, stateStack, this)

    // Fields

    override fun visit(leaderField1: SingleFieldModel<Aux>) = SingleFieldLeaderState(leaderField1, stateStack, this)
    override fun visit(leaderField1: ListFieldModel<Aux>) = ListFieldLeaderState(leaderField1, stateStack, this)

    // Values

    override fun visit(leaderValue1: PrimitiveModel<Aux>) = ScalarValueLeaderState(leaderValue1, stateStack)
    override fun visit(leaderValue1: AliasModel<Aux>) = ScalarValueLeaderState(leaderValue1, stateStack)
    override fun visit(leaderValue1: Password1wayModel<Aux>) = ScalarValueLeaderState(leaderValue1, stateStack)
    override fun visit(leaderValue1: Password2wayModel<Aux>) = ScalarValueLeaderState(leaderValue1, stateStack)
    override fun visit(leaderValue1: EnumerationModel<Aux>) = ScalarValueLeaderState(leaderValue1, stateStack)
    override fun visit(leaderValue1: AssociationModel<Aux>) = ScalarValueLeaderState(leaderValue1, stateStack)

    // Sub-values

    override fun visit(leaderEntityKeys1: EntityKeysModel<Aux>) =
        EntityKeysLeaderState(leaderEntityKeys1, stateStack, this)
}
