package org.treeWare.model.cursor

import org.treeWare.metaModel.getFieldNames
import org.treeWare.model.core.*
import org.treeWare.model.traversal.TraversalAction
import java.util.*

class LeaderManyModelCursor<Aux>(private val initialList: List<ElementModel<Aux>>) {
    private val stateStack = LeaderManyStateStack<Aux>()
    private var isAtStart = true

    fun next(previousAction: TraversalAction): LeaderManyModelCursorMove<Aux>? = when {
        previousAction == TraversalAction.ABORT_SUB_TREE -> {
            // Remove current state from the stack to abort its sub-tree.
            val currentState = stateStack.pollFirst()
            currentState.leaveCursorMove
        }
        isAtStart -> {
            isAtStart = false
            val initialLeaders = Leaders(initialList)
            val initialState = newLeaderManyState(initialLeaders, stateStack)
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

private typealias LeaderManyStateStack<Aux> = ArrayDeque<LeaderManyState<Aux>>
private typealias LeaderManyStateAction<Aux> = () -> LeaderManyModelCursorMove<Aux>?

private abstract class LeaderManyState<Aux>(leaders: Leaders<Aux>, val stateStack: LeaderManyStateStack<Aux>) {
    val visitCursorMove = LeaderManyModelCursorMove(CursorMoveDirection.VISIT, leaders)
    val leaveCursorMove = LeaderManyModelCursorMove(CursorMoveDirection.LEAVE, leaders)
    protected abstract val actionIterator: Iterator<LeaderManyStateAction<Aux>>

    fun next(): LeaderManyModelCursorMove<Aux>? = if (actionIterator.hasNext()) {
        val action = actionIterator.next()
        action()
    } else {
        // Remove self from stack
        stateStack.pollFirst()
        leaveCursorMove
    }
}

private class MainLeaderManyState<Aux>(
    leaders: Leaders<Aux>,
    stateStack: LeaderManyStateStack<Aux>
) : LeaderManyState<Aux>(leaders, stateStack) {
    override val actionIterator: Iterator<LeaderManyStateAction<Aux>>

    init {
        val actionList = listOf {
            val rootList = (leaders.elements as List<MainModel<Aux>?>).map { it?.root }
            val rootLeaders = Leaders(rootList)
            val rootState = newLeaderManyState(rootLeaders, stateStack)
            stateStack.addFirst(rootState)
            rootState.visitCursorMove
        }
        actionIterator = actionList.iterator()
    }
}

private abstract class BaseEntityLeaderManyState<Aux>(
    leaders: Leaders<Aux>,
    stateStack: LeaderManyStateStack<Aux>
) : LeaderManyState<Aux>(leaders, stateStack) {
    final override val actionIterator: Iterator<LeaderManyStateAction<Aux>>

    init {
        // Traverse entity fields in the order in which they are defined in
        // the meta-model.
        val baseEntityMeta = (leaders.nonNullElement as BaseEntityModel<Aux>?)?.meta
        val fieldNames = baseEntityMeta?.let { getFieldNames(it) } ?: emptyList()
        val fieldLeadersList = fieldNames.mapNotNull { fieldName ->
            val fields = leaders.elements.map { baseEntityElement ->
                val baseEntity = baseEntityElement as BaseEntityModel<Aux>?
                baseEntity?.getField(fieldName)
            }
            val fieldLeaders = Leaders(fields)
            fieldLeaders.takeIf { fieldLeaders.nonNullElement != null }
        }
        // TODO(performance): use a custom iterator to avoid above pre-computation (hasNext() is not trivial).
        actionIterator = IteratorAdapter({ fieldLeadersList.iterator() }) { fieldLeaders ->
            {
                val fieldState = newLeaderManyState(fieldLeaders, stateStack)
                stateStack.addFirst(fieldState)
                fieldState.visitCursorMove
            }
        }
    }
}

private class RootLeaderManyState<Aux>(leaders: Leaders<Aux>, stateStack: LeaderManyStateStack<Aux>) :
    BaseEntityLeaderManyState<Aux>(leaders, stateStack)

private class EntityLeaderManyState<Aux>(leaders: Leaders<Aux>, stateStack: LeaderManyStateStack<Aux>) :
    BaseEntityLeaderManyState<Aux>(leaders, stateStack)

// Fields

private class SingleFieldLeaderManyState<Aux>(
    leaders: Leaders<Aux>,
    stateStack: LeaderManyStateStack<Aux>
) : LeaderManyState<Aux>(leaders, stateStack) {
    override val actionIterator: Iterator<LeaderManyStateAction<Aux>>

    init {
        val actionList = listOf {
            val valueList = (leaders.elements as List<SingleFieldModel<Aux>?>).map { it?.value }
            val valueLeaders = Leaders(valueList)
            val valueState = newLeaderManyState(valueLeaders, stateStack)
            stateStack.addFirst(valueState)
            valueState.visitCursorMove
        }
        actionIterator = actionList.iterator()
    }
}

private class ListFieldLeaderManyState<Aux>(
    leaders: Leaders<Aux>,
    stateStack: LeaderManyStateStack<Aux>
) : LeaderManyState<Aux>(leaders, stateStack) {
    override val actionIterator: Iterator<LeaderManyStateAction<Aux>>

    init {
        // Traverse list elements at each index together.
        val lists = leaders.elements as List<ListFieldModel<Aux>?>
        val maxSize = lists.maxOf { it?.values?.size ?: 0 }
        actionIterator = IteratorAdapter({ (0 until maxSize).iterator() }) { index ->
            {
                val indexElements = lists.map { it?.values?.getOrNull(index) }
                val indexState = newLeaderManyState(Leaders(indexElements), stateStack)
                stateStack.addFirst(indexState)
                indexState.visitCursorMove
            }
        }
    }
}

private class SetFieldLeaderManyState<Aux>(
    leaders: Leaders<Aux>,
    stateStack: LeaderManyStateStack<Aux>
) : LeaderManyState<Aux>(leaders, stateStack) {
    override val actionIterator: Iterator<LeaderManyStateAction<Aux>>

    init {
        // Traverse set elements from first leader to the last. For each
        // element, find corresponding elements (matching keys) in other
        // leaders and traverse them together. Skip elements if they have
        // already been traversed.
        val elementLeadersList = getSetElementLeadersList(leaders)
        // TODO(performance): use a custom iterator to avoid above pre-computation (hasNext() is not trivial).
        actionIterator = IteratorAdapter({ elementLeadersList.iterator() }) { elementLeaders ->
            {
                val elementState = newLeaderManyState(elementLeaders, stateStack)
                stateStack.addFirst(elementState)
                elementState.visitCursorMove
            }
        }
    }
}

fun <Aux> getSetElementLeadersList(leaders: Leaders<Aux>): List<Leaders<Aux>> =
    leaders.elements.flatMapIndexed { leaderIndex: Int, leaderElementModel: ElementModel<Aux>? ->
        val leaderSetField = leaderElementModel as SetFieldModel<Aux>?
        leaderSetField?.values?.mapNotNull { getSetElementLeaders(it as EntityModel<Aux>, leaderIndex, leaders) }
            ?: emptyList()
    }

fun <Aux> getSetElementLeaders(
    matching: EntityModel<Aux>,
    inSetLeaderIndex: Int,
    setLeaders: Leaders<Aux>
): Leaders<Aux>? {
    val elementList = mutableListOf<EntityModel<Aux>?>()
    setLeaders.elements.forEachIndexed { leaderIndex, leaderElementModel ->
        if (leaderIndex == inSetLeaderIndex) elementList.add(matching)
        else {
            val leaderSetField = leaderElementModel as SetFieldModel<Aux>?
            val matched = leaderSetField?.getValueMatching(matching) as EntityModel<Aux>?
            // Skip elements that have already been traversed.
            if (matched != null && leaderIndex < inSetLeaderIndex) return null
            elementList.add(matched)
        }
    }
    return Leaders(elementList)
}

// Values

private class ScalarValueLeaderManyState<Aux>(
    leaders: Leaders<Aux>,
    stateStack: LeaderManyStateStack<Aux>
) : LeaderManyState<Aux>(leaders, stateStack) {
    override val actionIterator = emptyList<LeaderManyStateAction<Aux>>().iterator()
}

// State factory

private fun <Aux> newLeaderManyState(
    leaders: Leaders<Aux>,
    stateStack: LeaderManyStateStack<Aux>
): LeaderManyState<Aux> = when (leaders.elementType) {
    ModelElementType.MAIN -> MainLeaderManyState(leaders, stateStack)
    ModelElementType.ROOT -> RootLeaderManyState(leaders, stateStack)
    ModelElementType.ENTITY -> EntityLeaderManyState(leaders, stateStack)
    ModelElementType.SINGLE_FIELD -> SingleFieldLeaderManyState(leaders, stateStack)
    ModelElementType.LIST_FIELD -> ListFieldLeaderManyState(leaders, stateStack)
    ModelElementType.SET_FIELD -> SetFieldLeaderManyState(leaders, stateStack)
    ModelElementType.PRIMITIVE -> ScalarValueLeaderManyState(leaders, stateStack)
    ModelElementType.ALIAS -> ScalarValueLeaderManyState(leaders, stateStack)
    ModelElementType.PASSWORD1WAY -> ScalarValueLeaderManyState(leaders, stateStack)
    ModelElementType.PASSWORD2WAY -> ScalarValueLeaderManyState(leaders, stateStack)
    ModelElementType.ENUMERATION -> ScalarValueLeaderManyState(leaders, stateStack)
    ModelElementType.ASSOCIATION -> ScalarValueLeaderManyState(leaders, stateStack)
    else -> throw UnsupportedOperationException("Creating state for unsupported model element type: ${leaders.elementType}")
}
