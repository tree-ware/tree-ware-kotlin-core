package org.treeWare.model.cursor

import org.treeWare.model.core.*
import org.treeWare.model.traversal.AbstractLeader1ModelVisitor
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.model.traversal.dispatchVisit

class Leader1ModelCursor(private val initial: ElementModel, traverseAssociations: Boolean) {
    private val stateStack = LeaderStateStack()
    private val stateFactoryVisitor = LeaderStateFactoryVisitor(traverseAssociations, stateStack)
    private var isAtStart = true

    val element: ElementModel? get() = if (stateStack.isEmpty()) null else stateStack.first().element

    fun next(previousAction: TraversalAction): Leader1ModelCursorMove? = when {
        previousAction == TraversalAction.ABORT_SUB_TREE -> {
            // Remove current state from the stack to abort its sub-tree.
            val currentState = stateStack.removeFirst()
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
            val state = stateStack.first()
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
private typealias LeaderStateAction = () -> Leader1ModelCursorMove?

private abstract class LeaderState(val element: ElementModel, val stateStack: LeaderStateStack) {
    abstract val visitCursorMove: Leader1ModelCursorMove
    abstract val leaveCursorMove: Leader1ModelCursorMove
    protected abstract val actionIterator: Iterator<LeaderStateAction>

    fun next(): Leader1ModelCursorMove? = if (actionIterator.hasNext()) {
        val action = actionIterator.next()
        action()
    } else {
        // Remove self from stack
        stateStack.removeFirst()
        leaveCursorMove
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

private class EntityLeaderState(
    entity: EntityModel,
    stack: LeaderStateStack,
    stateFactoryVisitor: LeaderStateFactoryVisitor
) : BaseEntityLeaderState(entity, stack, stateFactoryVisitor) {
    override val visitCursorMove = Leader1ModelCursorMove(CursorMoveDirection.VISIT, entity)
    override val leaveCursorMove = Leader1ModelCursorMove(CursorMoveDirection.LEAVE, entity)
}

// Fields

private class SingleFieldLeaderState(
    field: SingleFieldModel,
    stack: LeaderStateStack,
    stateFactoryVisitor: LeaderStateFactoryVisitor
) : LeaderState(field, stack) {
    override val visitCursorMove = Leader1ModelCursorMove(CursorMoveDirection.VISIT, field)
    override val leaveCursorMove = Leader1ModelCursorMove(CursorMoveDirection.LEAVE, field)
    override val actionIterator: Iterator<LeaderStateAction>

    init {
        actionIterator = SingleValueIteratorAdapter({ field.value }) { value ->
            {
                val valueState =
                    dispatchVisit(value, stateFactoryVisitor) ?: throw IllegalStateException("null single-field state")
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
    override val visitCursorMove = Leader1ModelCursorMove(CursorMoveDirection.VISIT, field)
    override val leaveCursorMove = Leader1ModelCursorMove(CursorMoveDirection.LEAVE, field)
    override val actionIterator: Iterator<LeaderStateAction>

    init {
        actionIterator = IteratorAdapter({ field.values.iterator() }) { value ->
            {
                val valueState =
                    dispatchVisit(value, stateFactoryVisitor) ?: throw IllegalStateException("null set-field state")
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
    override val visitCursorMove = Leader1ModelCursorMove(CursorMoveDirection.VISIT, value)
    override val leaveCursorMove = Leader1ModelCursorMove(CursorMoveDirection.LEAVE, value)
    override val actionIterator: Iterator<LeaderStateAction>

    init {
        val actionList = listOf<LeaderStateAction>()
        actionIterator = actionList.iterator()
    }
}

private class AssociationLeaderState(
    association: AssociationModel,
    traverseAssociations: Boolean,
    stack: LeaderStateStack,
    stateFactoryVisitor: LeaderStateFactoryVisitor
) : LeaderState(association, stack) {
    override val visitCursorMove = Leader1ModelCursorMove(CursorMoveDirection.VISIT, association)
    override val leaveCursorMove = Leader1ModelCursorMove(CursorMoveDirection.LEAVE, association)
    override val actionIterator: Iterator<LeaderStateAction>

    init {
        val actionList = if (traverseAssociations) listOf<LeaderStateAction>({
            val valueState = dispatchVisit(association.value, stateFactoryVisitor)
                ?: throw IllegalStateException("null association state")
            stateStack.addFirst(valueState)
            valueState.visitCursorMove
        }) else emptyList()
        actionIterator = actionList.iterator()
    }
}

// State factory visitor

private class LeaderStateFactoryVisitor(
    private val traverseAssociations: Boolean,
    private val stateStack: LeaderStateStack
) : AbstractLeader1ModelVisitor<LeaderState?>(null) {
    override fun visitEntity(leaderEntity1: EntityModel) = EntityLeaderState(leaderEntity1, stateStack, this)

    // Fields

    override fun visitSingleField(leaderField1: SingleFieldModel) =
        SingleFieldLeaderState(leaderField1, stateStack, this)

    override fun visitSetField(leaderField1: SetFieldModel) = SetFieldLeaderState(leaderField1, stateStack, this)

    // Values

    override fun visitPrimitive(leaderValue1: PrimitiveModel) = ScalarValueLeaderState(leaderValue1, stateStack)
    override fun visitAlias(leaderValue1: AliasModel) = ScalarValueLeaderState(leaderValue1, stateStack)
    override fun visitPassword1way(leaderValue1: Password1wayModel) = ScalarValueLeaderState(leaderValue1, stateStack)
    override fun visitPassword2way(leaderValue1: Password2wayModel) = ScalarValueLeaderState(leaderValue1, stateStack)
    override fun visitEnumeration(leaderValue1: EnumerationModel) = ScalarValueLeaderState(leaderValue1, stateStack)
    override fun visitAssociation(leaderValue1: AssociationModel) =
        AssociationLeaderState(leaderValue1, traverseAssociations, stateStack, this)
}