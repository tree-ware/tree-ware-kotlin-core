package org.treeWare.model.cursor

import org.treeWare.metaModel.getFieldNames
import org.treeWare.model.core.*
import org.treeWare.model.traversal.TraversalAction

class LeaderManyModelCursor(private val initialList: List<ElementModel>) {
    private val stateStack = LeaderManyStateStack()
    private var isAtStart = true

    fun next(previousAction: TraversalAction): LeaderManyModelCursorMove? = when {
        previousAction == TraversalAction.ABORT_SUB_TREE -> {
            // Remove current state from the stack to abort its subtree.
            val currentState = stateStack.removeFirst()
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

private typealias LeaderManyStateStack = ArrayDeque<LeaderManyState>
private typealias LeaderManyStateAction = () -> LeaderManyModelCursorMove?

private abstract class LeaderManyState(leaders: Leaders, val stateStack: LeaderManyStateStack) {
    val visitCursorMove = LeaderManyModelCursorMove(CursorMoveDirection.VISIT, leaders)
    val leaveCursorMove = LeaderManyModelCursorMove(CursorMoveDirection.LEAVE, leaders)
    protected abstract val actionIterator: Iterator<LeaderManyStateAction>

    fun next(): LeaderManyModelCursorMove? = if (actionIterator.hasNext()) {
        val action = actionIterator.next()
        action()
    } else {
        // Remove self from stack
        stateStack.removeFirst()
        leaveCursorMove
    }
}

private class MainLeaderManyState(
    leaders: Leaders,
    stateStack: LeaderManyStateStack
) : LeaderManyState(leaders, stateStack) {
    override val actionIterator: Iterator<LeaderManyStateAction>

    init {
        val actionList = listOf({
            val rootList = (leaders.elements).map { (it as MainModel?)?.value }
            val rootLeaders = Leaders(rootList)
            val rootState = newLeaderManyState(rootLeaders, stateStack)
            stateStack.addFirst(rootState)
            rootState.visitCursorMove
        })
        actionIterator = actionList.iterator()
    }
}

private abstract class BaseEntityLeaderManyState(
    leaders: Leaders,
    stateStack: LeaderManyStateStack
) : LeaderManyState(leaders, stateStack) {
    final override val actionIterator: Iterator<LeaderManyStateAction>

    init {
        // Traverse entity fields in the order in which they are defined in
        // the meta-model.
        val baseEntityMeta = (leaders.nonNullElement as BaseEntityModel?)?.meta
        val fieldNames = baseEntityMeta?.let { getFieldNames(it) } ?: emptyList()
        val fieldLeadersList = fieldNames.mapNotNull { fieldName ->
            val fields = leaders.elements.map { baseEntityElement ->
                val baseEntity = baseEntityElement as BaseEntityModel?
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

private class EntityLeaderManyState(leaders: Leaders, stateStack: LeaderManyStateStack) :
    BaseEntityLeaderManyState(leaders, stateStack)

// Fields

private class SingleFieldLeaderManyState(
    leaders: Leaders,
    stateStack: LeaderManyStateStack
) : LeaderManyState(leaders, stateStack) {
    override val actionIterator: Iterator<LeaderManyStateAction>

    init {
        val actionList = listOf({
            val valueList = (leaders.elements).map { (it as SingleFieldModel?)?.value }
            val valueLeaders = Leaders(valueList)
            val valueState = newLeaderManyState(valueLeaders, stateStack)
            stateStack.addFirst(valueState)
            valueState.visitCursorMove
        })
        actionIterator = actionList.iterator()
    }
}

private class ListFieldLeaderManyState(
    leaders: Leaders,
    stateStack: LeaderManyStateStack
) : LeaderManyState(leaders, stateStack) {
    override val actionIterator: Iterator<LeaderManyStateAction>

    init {
        // Traverse list elements at each index together.
        val lists = leaders.elements
        val maxSize = lists.maxOf { (it as ListFieldModel?)?.values?.size ?: 0 }
        actionIterator = IteratorAdapter({ (0 until maxSize).iterator() }) { index ->
            {
                val indexElements = lists.map { (it as ListFieldModel?)?.values?.getOrNull(index) }
                val indexState = newLeaderManyState(Leaders(indexElements), stateStack)
                stateStack.addFirst(indexState)
                indexState.visitCursorMove
            }
        }
    }
}

private class SetFieldLeaderManyState(
    leaders: Leaders,
    stateStack: LeaderManyStateStack
) : LeaderManyState(leaders, stateStack) {
    override val actionIterator: Iterator<LeaderManyStateAction>

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

fun getSetElementLeadersList(leaders: Leaders): List<Leaders> =
    leaders.elements.flatMapIndexed { leaderIndex: Int, leaderElementModel: ElementModel? ->
        val leaderSetField = leaderElementModel as SetFieldModel?
        leaderSetField?.values?.mapNotNull { getSetElementLeaders(it as EntityModel, leaderIndex, leaders) }
            ?: emptyList()
    }

fun getSetElementLeaders(
    matching: EntityModel,
    inSetLeaderIndex: Int,
    setLeaders: Leaders
): Leaders? {
    val elementList = mutableListOf<EntityModel?>()
    setLeaders.elements.forEachIndexed { leaderIndex, leaderElementModel ->
        if (leaderIndex == inSetLeaderIndex) elementList.add(matching)
        else {
            val leaderSetField = leaderElementModel as SetFieldModel?
            val matched = leaderSetField?.getValueMatching(matching) as EntityModel?
            // Skip elements that have already been traversed.
            if (matched != null && leaderIndex < inSetLeaderIndex) return null
            elementList.add(matched)
        }
    }
    return Leaders(elementList)
}

// Values

private class ScalarValueLeaderManyState(
    leaders: Leaders,
    stateStack: LeaderManyStateStack
) : LeaderManyState(leaders, stateStack) {
    override val actionIterator = emptyList<LeaderManyStateAction>().iterator()
}

// State factory

private fun newLeaderManyState(
    leaders: Leaders,
    stateStack: LeaderManyStateStack
): LeaderManyState = when (leaders.elementType) {
    ModelElementType.MAIN -> MainLeaderManyState(leaders, stateStack)
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