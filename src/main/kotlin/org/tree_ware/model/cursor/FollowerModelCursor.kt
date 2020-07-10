package org.tree_ware.model.cursor

import org.tree_ware.model.core.*
import org.tree_ware.model.visitor.AbstractModelVisitor
import java.util.*

class FollowerModelCursor<Aux>(private val initial: ElementModel<Aux>) {
    private val stateStack = FollowerStateStack<Aux>()
    private val stateFactoryVisitor = FollowerStateFactoryVisitor(stateStack)
    private var isAtStart = true

    val element: ElementModel<Aux>? get() = stateStack.peekFirst()?.element

    fun follow(move: LeaderModelCursorMove<Aux>) {
        when {
            isAtStart -> {
                isAtStart = false
                val initialState =
                    initial.dispatch(stateFactoryVisitor) ?: throw IllegalStateException("null initial state")
                stateStack.addFirst(initialState)
            }
            stateStack.isNotEmpty() -> {
                val state = stateStack.peekFirst()
                state.follow(move)
            }
            else -> {
                // The stack is empty and we are not at the start. This means the model has been traversed.
                // So there is nothing to do.
            }
        }
    }
}

private typealias FollowerStateStack<Aux> = ArrayDeque<FollowerState<Aux>>

private abstract class FollowerState<Aux>(
    val element: ElementModel<Aux>?,
    protected val stateStack: FollowerStateStack<Aux>
) {
    open fun follow(move: LeaderModelCursorMove<Aux>) {
        throw IllegalStateException("Unknown move $move in state $this")
    }
}

private class NullFollowerState<Aux>(stateStack: FollowerStateStack<Aux>) : FollowerState<Aux>(null, stateStack) {
    override fun follow(move: LeaderModelCursorMove<Aux>) {
        if (move.direction == CursorMoveDirection.Visit) stateStack.addFirst(this)
        else stateStack.pollFirst()
    }
}

private class ModelFollowerState<Aux>(
    private val model: Model<Aux>,
    stateStack: FollowerStateStack<Aux>,
    private val stateFactoryVisitor: FollowerStateFactoryVisitor<Aux>
) : FollowerState<Aux>(model, stateStack) {
    override fun follow(move: LeaderModelCursorMove<Aux>) {
        when (move) {
            is LeaveLeaderModel -> stateStack.pollFirst()
            is VisitLeaderRootModel -> {
                val rootState =
                    model.root.dispatch(stateFactoryVisitor) ?: throw IllegalStateException("null root state")
                stateStack.addFirst(rootState)
            }
            else -> super.follow(move)
        }
    }
}

private abstract class BaseEntityFollowerState<Aux>(
    private val baseEntity: BaseEntityModel<Aux>,
    stack: FollowerStateStack<Aux>,
    private val stateFactoryVisitor: FollowerStateFactoryVisitor<Aux>
) : FollowerState<Aux>(baseEntity, stack) {
    override fun follow(move: LeaderModelCursorMove<Aux>) {
        when (move) {
            is VisitLeaderFieldModel -> visitField(move.element)
            is VisitLeaderListFieldModel -> visitField(move.element)
            else -> super.follow(move)
        }
    }

    private fun visitField(leaderField: FieldModel<Aux>) {
        val followerField = baseEntity.getField(leaderField.schema.name)
        val fieldState = if (followerField == null) NullFollowerState(stateStack)
        else followerField.dispatch(stateFactoryVisitor) ?: throw IllegalStateException("null field state")
        stateStack.addFirst(fieldState)
    }
}

private class RootFollowerState<Aux>(
    root: RootModel<Aux>,
    stateStack: FollowerStateStack<Aux>,
    stateFactoryVisitor: FollowerStateFactoryVisitor<Aux>
) : BaseEntityFollowerState<Aux>(root, stateStack, stateFactoryVisitor) {
    override fun follow(move: LeaderModelCursorMove<Aux>) {
        when (move) {
            is LeaveLeaderRootModel -> stateStack.pollFirst()
            else -> super.follow(move)
        }
    }
}

private class EntityFollowerState<Aux>(
    entity: EntityModel<Aux>,
    stateStack: FollowerStateStack<Aux>,
    stateFactoryVisitor: FollowerStateFactoryVisitor<Aux>
) : BaseEntityFollowerState<Aux>(entity, stateStack, stateFactoryVisitor) {
    override fun follow(move: LeaderModelCursorMove<Aux>) {
        when (move) {
            is LeaveLeaderEntityModel -> stateStack.pollFirst()
            else -> super.follow(move)
        }
    }
}

// Scalar fields

private class ScalarFieldFollowerState<Aux>(
    field: FieldModel<Aux>,
    stack: FollowerStateStack<Aux>
) : FollowerState<Aux>(field, stack) {
    override fun follow(move: LeaderModelCursorMove<Aux>) {
        when (move) {
            is LeaveLeaderFieldModel -> stateStack.pollFirst()
            else -> super.follow(move)
        }
    }
}

private class CompositionFieldFollowerState<Aux>(
    private val field: CompositionFieldModel<Aux>,
    stack: FollowerStateStack<Aux>,
    private val stateFactoryVisitor: FollowerStateFactoryVisitor<Aux>
) : FollowerState<Aux>(field, stack) {
    override fun follow(move: LeaderModelCursorMove<Aux>) {
        when (move) {
            is LeaveLeaderFieldModel -> stateStack.pollFirst()
            is VisitLeaderEntityModel -> {
                val entityState =
                    field.value.dispatch(stateFactoryVisitor) ?: throw IllegalStateException("null entity state")
                stateStack.addFirst(entityState)
            }
            else -> super.follow(move)
        }
    }
}

// List fields

private class PrimitiveListFieldFollowerState<Aux>(
    private val primitiveListField: PrimitiveListFieldModel<Aux>,
    stack: FollowerStateStack<Aux>,
    private val stateFactoryVisitor: FollowerStateFactoryVisitor<Aux>
) : FollowerState<Aux>(primitiveListField, stack) {
    override fun follow(move: LeaderModelCursorMove<Aux>) {
        when (move) {
            is LeaveLeaderListFieldModel -> stateStack.pollFirst()
            is VisitLeaderFieldModel -> {
                val leaderField =
                    move.element as? PrimitiveFieldModel ?: throw IllegalStateException("expected primitive field")
                val followerField = primitiveListField.getPrimitiveField(leaderField.value)
                val elementState = if (followerField == null) NullFollowerState(stateStack)
                else followerField.dispatch(stateFactoryVisitor) ?: throw IllegalStateException("null element state")
                stateStack.addFirst(elementState)
            }
            else -> super.follow(move)
        }
    }
}

private class AliasListFieldFollowerState<Aux>(
    private val aliasListField: AliasListFieldModel<Aux>,
    stack: FollowerStateStack<Aux>,
    private val stateFactoryVisitor: FollowerStateFactoryVisitor<Aux>
) : FollowerState<Aux>(aliasListField, stack) {
    override fun follow(move: LeaderModelCursorMove<Aux>) {
        when (move) {
            is LeaveLeaderListFieldModel -> stateStack.pollFirst()
            is VisitLeaderFieldModel -> {
                val leaderField =
                    move.element as? AliasFieldModel ?: throw IllegalStateException("expected alias field")
                val followerField = aliasListField.getAliasField(leaderField.value)
                val elementState = if (followerField == null) NullFollowerState(stateStack)
                else followerField.dispatch(stateFactoryVisitor) ?: throw IllegalStateException("null element state")
                stateStack.addFirst(elementState)
            }
            else -> super.follow(move)
        }
    }
}

private class EnumerationListFieldFollowerState<Aux>(
    private val enumerationListField: EnumerationListFieldModel<Aux>,
    stack: FollowerStateStack<Aux>,
    private val stateFactoryVisitor: FollowerStateFactoryVisitor<Aux>
) : FollowerState<Aux>(enumerationListField, stack) {
    override fun follow(move: LeaderModelCursorMove<Aux>) {
        when (move) {
            is LeaveLeaderListFieldModel -> stateStack.pollFirst()
            is VisitLeaderFieldModel -> {
                val leaderField =
                    move.element as? EnumerationFieldModel ?: throw IllegalStateException("expected enumeration field")
                val followerField = enumerationListField.getEnumerationField(leaderField.value)
                val elementState = if (followerField == null) NullFollowerState(stateStack)
                else followerField.dispatch(stateFactoryVisitor) ?: throw IllegalStateException("null element state")
                stateStack.addFirst(elementState)
            }
            else -> super.follow(move)
        }
    }
}

private class AssociationListFieldFollowerState<Aux>(
    private val associationListField: AssociationListFieldModel<Aux>,
    stack: FollowerStateStack<Aux>,
    private val stateFactoryVisitor: FollowerStateFactoryVisitor<Aux>
) : FollowerState<Aux>(associationListField, stack) {
    override fun follow(move: LeaderModelCursorMove<Aux>) {
        when (move) {
            is LeaveLeaderListFieldModel -> stateStack.pollFirst()
            is VisitLeaderFieldModel -> {
                val leaderField =
                    move.element as? AssociationFieldModel ?: throw IllegalStateException("expected association field")
                val followerField = associationListField.getAssociationField(leaderField.value)
                val elementState = if (followerField == null) NullFollowerState(stateStack)
                else followerField.dispatch(stateFactoryVisitor) ?: throw IllegalStateException("null element state")
                stateStack.addFirst(elementState)
            }
            else -> super.follow(move)
        }
    }
}

private class CompositionListFieldFollowerState<Aux>(
    private val compositionListField: CompositionListFieldModel<Aux>,
    stack: FollowerStateStack<Aux>,
    private val stateFactoryVisitor: FollowerStateFactoryVisitor<Aux>
) : FollowerState<Aux>(compositionListField, stack) {
    override fun follow(move: LeaderModelCursorMove<Aux>) {
        when (move) {
            is LeaveLeaderListFieldModel -> stateStack.pollFirst()
            is VisitLeaderEntityModel -> {
                val leaderEntity = move.element
                val followerField = compositionListField.getEntity(leaderEntity)
                val elementState = if (followerField == null) NullFollowerState(stateStack)
                else followerField.dispatch(stateFactoryVisitor) ?: throw IllegalStateException("null element state")
                stateStack.addFirst(elementState)
            }
            else -> super.follow(move)
        }
    }
}

// Field values

private class EntityKeysFollowerState<Aux>(
    entityKeys: EntityKeysModel<Aux>,
    stateStack: FollowerStateStack<Aux>,
    stateFactoryVisitor: FollowerStateFactoryVisitor<Aux>
) : BaseEntityFollowerState<Aux>(entityKeys, stateStack, stateFactoryVisitor) {
    override fun follow(move: LeaderModelCursorMove<Aux>) {
        when (move) {
            is LeaveLeaderEntityKeysModel -> stateStack.pollFirst()
            else -> super.follow(move)
        }
    }
}

// State factory visitor

private class FollowerStateFactoryVisitor<Aux>(
    private val stateStack: FollowerStateStack<Aux>
) : AbstractModelVisitor<Aux, FollowerState<Aux>?>(null) {
    override fun visit(model: Model<Aux>) = ModelFollowerState(model, stateStack, this)
    override fun visit(root: RootModel<Aux>) = RootFollowerState(root, stateStack, this)
    override fun visit(entity: EntityModel<Aux>) = EntityFollowerState(entity, stateStack, this)

    // Scalar fields

    override fun visit(field: PrimitiveFieldModel<Aux>) = ScalarFieldFollowerState(field, stateStack)
    override fun visit(field: AliasFieldModel<Aux>) = ScalarFieldFollowerState(field, stateStack)
    override fun visit(field: EnumerationFieldModel<Aux>) = ScalarFieldFollowerState(field, stateStack)
    override fun visit(field: AssociationFieldModel<Aux>) = ScalarFieldFollowerState(field, stateStack)
    override fun visit(field: CompositionFieldModel<Aux>) = CompositionFieldFollowerState(field, stateStack, this)

    // List fields

    override fun visit(field: PrimitiveListFieldModel<Aux>) = PrimitiveListFieldFollowerState(field, stateStack, this)
    override fun visit(field: AliasListFieldModel<Aux>) = AliasListFieldFollowerState(field, stateStack, this)

    override fun visit(field: EnumerationListFieldModel<Aux>) =
        EnumerationListFieldFollowerState(field, stateStack, this)

    override fun visit(field: AssociationListFieldModel<Aux>) =
        AssociationListFieldFollowerState(field, stateStack, this)

    override fun visit(field: CompositionListFieldModel<Aux>) =
        CompositionListFieldFollowerState(field, stateStack, this)

    // Field values

    override fun visit(entityKeys: EntityKeysModel<Aux>) = EntityKeysFollowerState(entityKeys, stateStack, this)
}
