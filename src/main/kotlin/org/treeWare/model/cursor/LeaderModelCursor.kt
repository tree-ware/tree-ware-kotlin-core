package org.treeWare.model.cursor

import org.treeWare.common.traversal.TraversalAction
import org.treeWare.model.core.*
import org.treeWare.model.operator.AbstractLeader1Follower0ModelVisitor
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
            // The stack is empty and we are not at the start. This means the model has been traversed.
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

private class ModelLeaderState<Aux>(
    model: Model<Aux>,
    stateStack: LeaderStateStack<Aux>,
    stateFactoryVisitor: LeaderStateFactoryVisitor<Aux>
) : LeaderState<Aux>(model, stateStack) {
    override val visitCursorMove = VisitLeaderModel(model)
    override val leaveCursorMove = LeaveLeaderModel(model)
    override val actionIterator: Iterator<LeaderStateAction<Aux>>

    init {
        val actionList = listOf<LeaderStateAction<Aux>> {
            val rootState =
                dispatchVisit(model.root, stateFactoryVisitor) ?: throw IllegalStateException("null root state")
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
        actionIterator = IteratorAdapter({ baseEntity.fields.iterator() }) { field ->
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

// Scalar fields

private abstract class FieldModelLeaderState<Aux>(
    field: FieldModel<Aux>,
    stack: LeaderStateStack<Aux>
) : LeaderState<Aux>(field, stack) {
    override val visitCursorMove = VisitLeaderFieldModel(field)
    override val leaveCursorMove = LeaveLeaderFieldModel(field)
}

private class ScalarFieldLeaderState<Aux>(
    field: FieldModel<Aux>,
    stack: LeaderStateStack<Aux>
) : FieldModelLeaderState<Aux>(field, stack) {
    override val actionIterator: Iterator<LeaderStateAction<Aux>>

    init {
        val actionList = listOf<LeaderStateAction<Aux>>()
        actionIterator = actionList.iterator()
    }
}

private class CompositionFieldLeaderState<Aux>(
    field: CompositionFieldModel<Aux>,
    stack: LeaderStateStack<Aux>,
    stateFactoryVisitor: LeaderStateFactoryVisitor<Aux>
) : FieldModelLeaderState<Aux>(field, stack) {
    override val actionIterator: Iterator<LeaderStateAction<Aux>>

    init {
        val actionList = listOf<LeaderStateAction<Aux>> {
            val entityState =
                dispatchVisit(field.value, stateFactoryVisitor) ?: throw IllegalStateException("null entity state")
            stateStack.addFirst(entityState)
            entityState.visitCursorMove
        }
        actionIterator = actionList.iterator()
    }
}

// List fields

private abstract class ListFieldModelLeaderState<Aux>(
    field: ListFieldModel<Aux>,
    stack: LeaderStateStack<Aux>
) : LeaderState<Aux>(field, stack) {
    override val visitCursorMove = VisitLeaderListFieldModel(field)
    override val leaveCursorMove = LeaveLeaderListFieldModel(field)
}

private class PrimitiveListFieldLeaderState<Aux>(
    field: PrimitiveListFieldModel<Aux>,
    stack: LeaderStateStack<Aux>,
    stateFactoryVisitor: LeaderStateFactoryVisitor<Aux>
) : ListFieldModelLeaderState<Aux>(field, stack) {
    override val actionIterator: Iterator<LeaderStateAction<Aux>>

    init {
        actionIterator = IteratorAdapter({ field.primitives.iterator() }) { primitive ->
            {
                val primitiveState =
                    dispatchVisit(primitive, stateFactoryVisitor) ?: throw IllegalStateException("null primitive state")
                stateStack.addFirst(primitiveState)
                primitiveState.visitCursorMove
            }
        }
    }
}

private class AliasListFieldLeaderState<Aux>(
    field: AliasListFieldModel<Aux>,
    stack: LeaderStateStack<Aux>,
    stateFactoryVisitor: LeaderStateFactoryVisitor<Aux>
) : ListFieldModelLeaderState<Aux>(field, stack) {
    override val actionIterator: Iterator<LeaderStateAction<Aux>>

    init {
        actionIterator = IteratorAdapter({ field.aliases.iterator() }) { alias ->
            {
                val aliasState =
                    dispatchVisit(alias, stateFactoryVisitor) ?: throw IllegalStateException("null alias state")
                stateStack.addFirst(aliasState)
                aliasState.visitCursorMove
            }
        }
    }
}

private class EnumerationListFieldLeaderState<Aux>(
    field: EnumerationListFieldModel<Aux>,
    stack: LeaderStateStack<Aux>,
    stateFactoryVisitor: LeaderStateFactoryVisitor<Aux>
) : ListFieldModelLeaderState<Aux>(field, stack) {
    override val actionIterator: Iterator<LeaderStateAction<Aux>>

    init {
        actionIterator = IteratorAdapter({ field.enumerations.iterator() }) { enumeration ->
            {
                val enumerationState =
                    dispatchVisit(enumeration, stateFactoryVisitor)
                        ?: throw IllegalStateException("null enumeration state")
                stateStack.addFirst(enumerationState)
                enumerationState.visitCursorMove
            }
        }
    }
}

private class AssociationListFieldLeaderState<Aux>(
    field: AssociationListFieldModel<Aux>,
    stack: LeaderStateStack<Aux>,
    stateFactoryVisitor: LeaderStateFactoryVisitor<Aux>
) : ListFieldModelLeaderState<Aux>(field, stack) {
    override val actionIterator: Iterator<LeaderStateAction<Aux>>

    init {
        actionIterator = IteratorAdapter({ field.associations.iterator() }) { association ->
            {
                val associationState =
                    dispatchVisit(association, stateFactoryVisitor)
                        ?: throw IllegalStateException("null association state")
                stateStack.addFirst(associationState)
                associationState.visitCursorMove
            }
        }
    }
}

private class CompositionListFieldLeaderState<Aux>(
    field: CompositionListFieldModel<Aux>,
    stack: LeaderStateStack<Aux>,
    stateFactoryVisitor: LeaderStateFactoryVisitor<Aux>
) : ListFieldModelLeaderState<Aux>(field, stack) {
    override val actionIterator: Iterator<LeaderStateAction<Aux>>

    init {
        actionIterator = IteratorAdapter({ field.entities.iterator() }) { entity ->
            {
                val entityState =
                    dispatchVisit(entity, stateFactoryVisitor) ?: throw IllegalStateException("null entity state")
                stateStack.addFirst(entityState)
                entityState.visitCursorMove
            }
        }
    }
}

// Field values

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
    override fun visit(leaderModel1: Model<Aux>) = ModelLeaderState(leaderModel1, stateStack, this)
    override fun visit(leaderRoot1: RootModel<Aux>) = RootLeaderState(leaderRoot1, stateStack, this)
    override fun visit(leaderEntity1: EntityModel<Aux>) = EntityLeaderState(leaderEntity1, stateStack, this)

    // Scalar fields

    override fun visit(leaderField1: PrimitiveFieldModel<Aux>) = ScalarFieldLeaderState(leaderField1, stateStack)
    override fun visit(leaderField1: AliasFieldModel<Aux>) = ScalarFieldLeaderState(leaderField1, stateStack)
    override fun visit(leaderField1: EnumerationFieldModel<Aux>) = ScalarFieldLeaderState(leaderField1, stateStack)
    override fun visit(leaderField1: AssociationFieldModel<Aux>) = ScalarFieldLeaderState(leaderField1, stateStack)
    override fun visit(leaderField1: CompositionFieldModel<Aux>) =
        CompositionFieldLeaderState(leaderField1, stateStack, this)

    // List fields

    override fun visit(leaderField1: PrimitiveListFieldModel<Aux>) =
        PrimitiveListFieldLeaderState(leaderField1, stateStack, this)

    override fun visit(leaderField1: AliasListFieldModel<Aux>) =
        AliasListFieldLeaderState(leaderField1, stateStack, this)

    override fun visit(leaderField1: EnumerationListFieldModel<Aux>) =
        EnumerationListFieldLeaderState(leaderField1, stateStack, this)

    override fun visit(leaderField1: AssociationListFieldModel<Aux>) =
        AssociationListFieldLeaderState(leaderField1, stateStack, this)

    override fun visit(leaderField1: CompositionListFieldModel<Aux>) =
        CompositionListFieldLeaderState(leaderField1, stateStack, this)

    // Field values

    override fun visit(leaderEntityKeys1: EntityKeysModel<Aux>) =
        EntityKeysLeaderState(leaderEntityKeys1, stateStack, this)
}
