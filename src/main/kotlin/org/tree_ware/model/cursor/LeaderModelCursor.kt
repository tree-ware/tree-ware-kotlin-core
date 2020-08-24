package org.tree_ware.model.cursor

import org.tree_ware.model.core.*
import org.tree_ware.model.visitor.AbstractModelVisitor
import org.tree_ware.schema.core.SchemaTraversalAction
import java.util.*

class LeaderModelCursor<Aux>(private val initial: ElementModel<Aux>) {
    private val stateStack = LeaderStateStack<Aux>()
    private val stateFactoryVisitor = LeaderStateFactoryVisitor(stateStack)
    private var isAtStart = true

    val element: ElementModel<Aux>? get() = if (stateStack.isEmpty()) null else stateStack.peekFirst().element

    fun next(previousAction: SchemaTraversalAction): LeaderModelCursorMove<Aux>? = when {
        previousAction == SchemaTraversalAction.ABORT_SUB_TREE -> {
            // Remove current state from the stack to abort its sub-tree.
            val currentState = stateStack.pollFirst()
            currentState.leaveCursorMove
        }
        isAtStart -> {
            isAtStart = false
            val initialState =
                initial.dispatch(stateFactoryVisitor) ?: throw IllegalStateException("null initial state")
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
            val rootState = model.root.dispatch(stateFactoryVisitor) ?: throw IllegalStateException("null root state")
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
                val fieldState = field.dispatch(stateFactoryVisitor) ?: throw IllegalStateException("null field state")
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
                field.value.dispatch(stateFactoryVisitor) ?: throw IllegalStateException("null entity state")
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
                    primitive.dispatch(stateFactoryVisitor) ?: throw IllegalStateException("null primitive state")
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
                val aliasState = alias.dispatch(stateFactoryVisitor) ?: throw IllegalStateException("null alias state")
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
                    enumeration.dispatch(stateFactoryVisitor) ?: throw IllegalStateException("null enumeration state")
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
                    association.dispatch(stateFactoryVisitor) ?: throw IllegalStateException("null association state")
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
                    entity.dispatch(stateFactoryVisitor) ?: throw IllegalStateException("null entity state")
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
) : AbstractModelVisitor<Aux, LeaderState<Aux>?>(null) {
    override fun visit(model: Model<Aux>) = ModelLeaderState(model, stateStack, this)
    override fun visit(root: RootModel<Aux>) = RootLeaderState(root, stateStack, this)
    override fun visit(entity: EntityModel<Aux>) = EntityLeaderState(entity, stateStack, this)

    // Scalar fields

    override fun visit(field: PrimitiveFieldModel<Aux>) = ScalarFieldLeaderState(field, stateStack)
    override fun visit(field: AliasFieldModel<Aux>) = ScalarFieldLeaderState(field, stateStack)
    override fun visit(field: EnumerationFieldModel<Aux>) = ScalarFieldLeaderState(field, stateStack)
    override fun visit(field: AssociationFieldModel<Aux>) = ScalarFieldLeaderState(field, stateStack)
    override fun visit(field: CompositionFieldModel<Aux>) = CompositionFieldLeaderState(field, stateStack, this)

    // List fields

    override fun visit(field: PrimitiveListFieldModel<Aux>) = PrimitiveListFieldLeaderState(field, stateStack, this)
    override fun visit(field: AliasListFieldModel<Aux>) = AliasListFieldLeaderState(field, stateStack, this)
    override fun visit(field: EnumerationListFieldModel<Aux>) = EnumerationListFieldLeaderState(field, stateStack, this)
    override fun visit(field: AssociationListFieldModel<Aux>) = AssociationListFieldLeaderState(field, stateStack, this)
    override fun visit(field: CompositionListFieldModel<Aux>) = CompositionListFieldLeaderState(field, stateStack, this)

    // Field values

    override fun visit(entityKeys: EntityKeysModel<Aux>) = EntityKeysLeaderState(entityKeys, stateStack, this)
}
