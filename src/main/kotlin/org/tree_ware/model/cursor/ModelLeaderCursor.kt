package org.tree_ware.model.cursor

import org.tree_ware.model.core.*
import org.tree_ware.model.visitor.AbstractModelVisitor
import org.tree_ware.schema.core.SchemaTraversalAction
import java.util.*

class ModelLeaderCursor<Aux>(private val initial: ElementModel<Aux>) {
    private val stateStack = CursorStateStack<Aux>()
    private val stateFactoryVisitor = CursorStateFactoryVisitor(stateStack)
    private var isAtStart = true

    val element: ElementModel<Aux>? get() = stateStack.peekFirst().element

    fun next(previousAction: SchemaTraversalAction): ModelCursorMove<Aux>? {
        if (previousAction == SchemaTraversalAction.ABORT_SUB_TREE) {
            // Remove current state from the stack to abort its sub-tree.
            stateStack.pollFirst()
            // Proceed to the next node in the tree (next() must go to the next node).
        }
        return when {
            isAtStart -> {
                isAtStart = false
                val modelState =
                    initial.dispatch(stateFactoryVisitor) ?: throw IllegalStateException("null model state")
                stateStack.addFirst(modelState)
                modelState.visitCursorMove
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
}

private typealias CursorStateStack<Aux> = ArrayDeque<CursorState<Aux>>
private typealias CursorStateAction<Aux> = () -> ModelCursorMove<Aux>?

private class IteratorAdapter<T, R>(private val adaptee: Iterator<T>, private val transform: (T) -> R) : Iterator<R> {
    override fun hasNext(): Boolean = adaptee.hasNext()
    override fun next(): R = transform(adaptee.next())
}

private abstract class CursorState<Aux>(
    val element: ElementModel<Aux>,
    val stateStack: CursorStateStack<Aux>
) {
    abstract val visitCursorMove: ModelCursorMove<Aux>
    protected abstract val leaveCursorMove: ModelCursorMove<Aux>
    protected abstract val actionIterator: Iterator<CursorStateAction<Aux>>

    fun next(): ModelCursorMove<Aux>? = if (actionIterator.hasNext()) {
        val action = actionIterator.next()
        action()
    } else {
        // Remove self from stack
        stateStack.pollFirst()
        leaveCursorMove
    }
}

private class ModelState<Aux>(
    model: Model<Aux>,
    stateStack: CursorStateStack<Aux>,
    stateFactoryVisitor: CursorStateFactoryVisitor<Aux>
) : CursorState<Aux>(model, stateStack) {
    override val visitCursorMove = VisitModel(model)
    override val leaveCursorMove = LeaveModel(model)
    override val actionIterator: Iterator<CursorStateAction<Aux>>

    init {
        val actionList = listOf<CursorStateAction<Aux>> {
            val rootState = model.root.dispatch(stateFactoryVisitor) ?: throw IllegalStateException("null root state")
            stateStack.addFirst(rootState)
            rootState.visitCursorMove
        }
        actionIterator = actionList.iterator()
    }
}

private abstract class BaseEntityState<Aux>(
    baseEntity: BaseEntityModel<Aux>,
    stack: CursorStateStack<Aux>,
    stateFactoryVisitor: CursorStateFactoryVisitor<Aux>
) : CursorState<Aux>(baseEntity, stack) {
    final override val actionIterator: Iterator<CursorStateAction<Aux>>

    init {
        actionIterator = IteratorAdapter(baseEntity.fields.iterator()) { field ->
            {
                val fieldState = field.dispatch(stateFactoryVisitor) ?: throw IllegalStateException("null field state")
                stateStack.addFirst(fieldState)
                fieldState.visitCursorMove
            }
        }
    }
}

private class RootModelState<Aux>(
    root: RootModel<Aux>,
    stack: CursorStateStack<Aux>,
    stateFactoryVisitor: CursorStateFactoryVisitor<Aux>
) : BaseEntityState<Aux>(root, stack, stateFactoryVisitor) {
    override val visitCursorMove = VisitRootModel(root)
    override val leaveCursorMove = LeaveRootModel(root)
}

private class EntityModelState<Aux>(
    entity: EntityModel<Aux>,
    stack: CursorStateStack<Aux>,
    stateFactoryVisitor: CursorStateFactoryVisitor<Aux>
) : BaseEntityState<Aux>(entity, stack, stateFactoryVisitor) {
    override val visitCursorMove = VisitEntityModel(entity)
    override val leaveCursorMove = LeaveEntityModel(entity)
}

// Scalar fields

private abstract class FieldModelState<Aux>(
    field: FieldModel<Aux>,
    stack: CursorStateStack<Aux>
) : CursorState<Aux>(field, stack) {
    override val visitCursorMove = VisitFieldModel(field)
    override val leaveCursorMove = LeaveFieldModel(field)
}

private class ScalarFieldModelState<Aux>(
    field: FieldModel<Aux>,
    stack: CursorStateStack<Aux>
) : FieldModelState<Aux>(field, stack) {
    override val actionIterator: Iterator<CursorStateAction<Aux>>

    init {
        val actionList = listOf<CursorStateAction<Aux>>()
        actionIterator = actionList.iterator()
    }
}

private class AssociationFieldModelState<Aux>(
    field: AssociationFieldModel<Aux>,
    stack: CursorStateStack<Aux>,
    stateFactoryVisitor: CursorStateFactoryVisitor<Aux>
) : FieldModelState<Aux>(field, stack) {
    override val actionIterator: Iterator<CursorStateAction<Aux>>

    init {
        actionIterator = IteratorAdapter(field.pathKeys.iterator()) { entityKeys ->
            {
                val entityKeysState =
                    entityKeys.dispatch(stateFactoryVisitor) ?: throw IllegalStateException("null entity keys state")
                stateStack.addFirst(entityKeysState)
                entityKeysState.visitCursorMove
            }
        }
    }
}

private class CompositionFieldModelState<Aux>(
    field: CompositionFieldModel<Aux>,
    stack: CursorStateStack<Aux>,
    stateFactoryVisitor: CursorStateFactoryVisitor<Aux>
) : FieldModelState<Aux>(field, stack) {
    override val actionIterator: Iterator<CursorStateAction<Aux>>

    init {
        val actionList = listOf<CursorStateAction<Aux>> {
            val entityState =
                field.value.dispatch(stateFactoryVisitor) ?: throw IllegalStateException("null entity state")
            stateStack.addFirst(entityState)
            entityState.visitCursorMove
        }
        actionIterator = actionList.iterator()
    }
}

// List fields

private abstract class ListFieldModelState<Aux>(
    field: ListFieldModel<Aux>,
    stack: CursorStateStack<Aux>
) : CursorState<Aux>(field, stack) {
    override val visitCursorMove = VisitListFieldModel(field)
    override val leaveCursorMove = LeaveListFieldModel(field)
}

private class PrimitiveListFieldModelState<Aux>(
    field: PrimitiveListFieldModel<Aux>,
    stack: CursorStateStack<Aux>,
    stateFactoryVisitor: CursorStateFactoryVisitor<Aux>
) : ListFieldModelState<Aux>(field, stack) {
    override val actionIterator: Iterator<CursorStateAction<Aux>>

    init {
        actionIterator = IteratorAdapter(field.primitives.iterator()) { primitive ->
            {
                val primitiveState =
                    primitive.dispatch(stateFactoryVisitor) ?: throw IllegalStateException("null primitive state")
                stateStack.addFirst(primitiveState)
                primitiveState.visitCursorMove
            }
        }
    }
}

private class AliasListFieldModelState<Aux>(
    field: AliasListFieldModel<Aux>,
    stack: CursorStateStack<Aux>,
    stateFactoryVisitor: CursorStateFactoryVisitor<Aux>
) : ListFieldModelState<Aux>(field, stack) {
    override val actionIterator: Iterator<CursorStateAction<Aux>>

    init {
        actionIterator = IteratorAdapter(field.aliases.iterator()) { alias ->
            {
                val aliasState = alias.dispatch(stateFactoryVisitor) ?: throw IllegalStateException("null alias state")
                stateStack.addFirst(aliasState)
                aliasState.visitCursorMove
            }
        }
    }
}

private class EnumerationListFieldModelState<Aux>(
    field: EnumerationListFieldModel<Aux>,
    stack: CursorStateStack<Aux>,
    stateFactoryVisitor: CursorStateFactoryVisitor<Aux>
) : ListFieldModelState<Aux>(field, stack) {
    override val actionIterator: Iterator<CursorStateAction<Aux>>

    init {
        actionIterator = IteratorAdapter(field.enumerations.iterator()) { enumeration ->
            {
                val enumerationState =
                    enumeration.dispatch(stateFactoryVisitor) ?: throw IllegalStateException("null enumeration state")
                stateStack.addFirst(enumerationState)
                enumerationState.visitCursorMove
            }
        }
    }
}

private class AssociationListFieldModelState<Aux>(
    field: AssociationListFieldModel<Aux>,
    stack: CursorStateStack<Aux>,
    stateFactoryVisitor: CursorStateFactoryVisitor<Aux>
) : ListFieldModelState<Aux>(field, stack) {
    override val actionIterator: Iterator<CursorStateAction<Aux>>

    init {
        actionIterator = IteratorAdapter(field.value.iterator()) { association ->
            {
                val associationState =
                    association.dispatch(stateFactoryVisitor) ?: throw IllegalStateException("null association state")
                stateStack.addFirst(associationState)
                associationState.visitCursorMove
            }
        }
    }
}

private class CompositionListFieldModelState<Aux>(
    field: CompositionListFieldModel<Aux>,
    stack: CursorStateStack<Aux>,
    stateFactoryVisitor: CursorStateFactoryVisitor<Aux>
) : ListFieldModelState<Aux>(field, stack) {
    override val actionIterator: Iterator<CursorStateAction<Aux>>

    init {
        actionIterator = IteratorAdapter(field.value.iterator()) { entity ->
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

private class EntityKeysModelState<Aux>(
    entityKeys: EntityKeysModel<Aux>,
    stack: CursorStateStack<Aux>,
    stateFactoryVisitor: CursorStateFactoryVisitor<Aux>
) : BaseEntityState<Aux>(entityKeys, stack, stateFactoryVisitor) {
    override val visitCursorMove = VisitEntityKeysModel(entityKeys)
    override val leaveCursorMove = LeaveEntityKeysModel(entityKeys)
}

private class CursorStateFactoryVisitor<Aux>(
    private val stateStack: CursorStateStack<Aux>
) : AbstractModelVisitor<Aux, CursorState<Aux>?>(null) {
    override fun visit(model: Model<Aux>) = ModelState(model, stateStack, this)
    override fun visit(root: RootModel<Aux>) = RootModelState(root, stateStack, this)
    override fun visit(entity: EntityModel<Aux>) = EntityModelState(entity, stateStack, this)

    // Scalar fields

    override fun visit(field: PrimitiveFieldModel<Aux>) = ScalarFieldModelState(field, stateStack)
    override fun visit(field: AliasFieldModel<Aux>) = ScalarFieldModelState(field, stateStack)
    override fun visit(field: EnumerationFieldModel<Aux>) = ScalarFieldModelState(field, stateStack)
    override fun visit(field: AssociationFieldModel<Aux>) = AssociationFieldModelState(field, stateStack, this)
    override fun visit(field: CompositionFieldModel<Aux>) = CompositionFieldModelState(field, stateStack, this)

    // List fields

    override fun visit(field: PrimitiveListFieldModel<Aux>) = PrimitiveListFieldModelState(field, stateStack, this)
    override fun visit(field: AliasListFieldModel<Aux>) = AliasListFieldModelState(field, stateStack, this)
    override fun visit(field: EnumerationListFieldModel<Aux>) = EnumerationListFieldModelState(field, stateStack, this)
    override fun visit(field: AssociationListFieldModel<Aux>) = AssociationListFieldModelState(field, stateStack, this)
    override fun visit(field: CompositionListFieldModel<Aux>) = CompositionListFieldModelState(field, stateStack, this)

    // Field values

    override fun visit(entityKeys: EntityKeysModel<Aux>) = EntityKeysModelState(entityKeys, stateStack, this)
}
