package org.tree_ware.model.cursor

import org.tree_ware.model.core.*
import org.tree_ware.model.visitor.AbstractModelVisitor
import java.util.*

class FollowerModelCursor<Aux>(private val initial: ElementModel<Aux>) {
    private val stateStack = FollowerStateStack<Aux>()
    private val stateFactoryVisitor = FollowerStateFactoryVisitor(stateStack)
    private var isAtStart = true

    val element: ElementModel<Aux>? get() = stateStack.peekFirst()?.element

    fun follow(move: LeaderModelCursorMove<Aux>): FollowerModelCursorMove<Aux>? = when {
        isAtStart -> {
            isAtStart = false
            val initialState =
                initial.dispatch(stateFactoryVisitor) ?: throw IllegalStateException("null initial state")
            stateStack.addFirst(initialState)
            initialState.visitCursorMove
        }
        stateStack.isNotEmpty() -> {
            val state = stateStack.peekFirst()
            state.follow(move)
        }
        else -> {
            // The stack is empty and we are not at the start. This means the model has been traversed.
            // So there are no more moves.
            null
        }
    }
}

private typealias FollowerStateStack<Aux> = ArrayDeque<FollowerState<Aux>>

private abstract class FollowerState<Aux>(
    val element: ElementModel<Aux>?,
    protected val stateStack: FollowerStateStack<Aux>
) {
    abstract val visitCursorMove: FollowerModelCursorMove<Aux>

    open fun follow(move: LeaderModelCursorMove<Aux>): FollowerModelCursorMove<Aux>? {
        throw IllegalStateException("Unknown move $move in state $this")
    }
}

private class NullFollowerState<Aux>(
    override val visitCursorMove: FollowerModelCursorMove<Aux>,
    stateStack: FollowerStateStack<Aux>
) : FollowerState<Aux>(null, stateStack) {
    override fun follow(move: LeaderModelCursorMove<Aux>) = when (move) {
        is VisitLeaderModel -> {
            stateStack.addFirst(this)
            VisitFollowerModel<Aux>(null)
        }
        is LeaveLeaderModel -> {
            stateStack.pollFirst()
            LeaveFollowerModel<Aux>(null)
        }
        is VisitLeaderRootModel -> {
            stateStack.addFirst(this)
            VisitFollowerRootModel<Aux>(null)
        }
        is LeaveLeaderRootModel -> {
            stateStack.pollFirst()
            LeaveFollowerRootModel<Aux>(null)
        }
        is VisitLeaderEntityModel -> {
            stateStack.addFirst(this)
            VisitFollowerEntityModel<Aux>(null)
        }
        is LeaveLeaderEntityModel -> {
            stateStack.pollFirst()
            LeaveFollowerEntityModel<Aux>(null)
        }
        is VisitLeaderFieldModel -> {
            stateStack.addFirst(this)
            VisitFollowerFieldModel<Aux>(null)
        }
        is LeaveLeaderFieldModel -> {
            stateStack.pollFirst()
            LeaveFollowerFieldModel<Aux>(null)
        }
        is VisitLeaderListFieldModel -> {
            stateStack.addFirst(this)
            VisitFollowerListFieldModel<Aux>(null)
        }
        is LeaveLeaderListFieldModel -> {
            stateStack.pollFirst()
            LeaveFollowerListFieldModel<Aux>(null)
        }
        is VisitLeaderEntityKeysModel -> {
            stateStack.addFirst(this)
            VisitFollowerEntityKeysModel<Aux>(null)
        }
        is LeaveLeaderEntityKeysModel -> {
            stateStack.pollFirst()
            LeaveFollowerEntityKeysModel<Aux>(null)
        }
    }
}

private class ModelFollowerState<Aux>(
    private val model: Model<Aux>,
    stateStack: FollowerStateStack<Aux>,
    private val stateFactoryVisitor: FollowerStateFactoryVisitor<Aux>
) : FollowerState<Aux>(model, stateStack) {
    override val visitCursorMove = VisitFollowerModel(model)

    override fun follow(move: LeaderModelCursorMove<Aux>) = when (move) {
        is LeaveLeaderModel -> {
            stateStack.pollFirst()
            LeaveFollowerModel(model)
        }
        is VisitLeaderRootModel -> {
            val rootState =
                model.root.dispatch(stateFactoryVisitor) ?: throw IllegalStateException("null root state")
            stateStack.addFirst(rootState)
            rootState.visitCursorMove
        }
        else -> super.follow(move)
    }
}

private abstract class BaseEntityFollowerState<Aux>(
    private val baseEntity: BaseEntityModel<Aux>,
    stack: FollowerStateStack<Aux>,
    private val stateFactoryVisitor: FollowerStateFactoryVisitor<Aux>
) : FollowerState<Aux>(baseEntity, stack) {
    override fun follow(move: LeaderModelCursorMove<Aux>) = when (move) {
        is VisitLeaderFieldModel -> visitField(move.element, VisitFollowerFieldModel(null))
        is VisitLeaderListFieldModel -> visitField(move.element, VisitFollowerListFieldModel(null))
        else -> super.follow(move)
    }

    private fun visitField(
        leaderField: FieldModel<Aux>,
        nullVisitCursorMove: FollowerModelCursorMove<Aux>
    ): FollowerModelCursorMove<Aux>? {
        val followerField = baseEntity.getField(leaderField.schema.name)
        val fieldState = if (followerField == null) NullFollowerState(nullVisitCursorMove, stateStack)
        else followerField.dispatch(stateFactoryVisitor) ?: throw IllegalStateException("null field state")
        stateStack.addFirst(fieldState)
        return fieldState.visitCursorMove
    }
}

private class RootFollowerState<Aux>(
    private val root: RootModel<Aux>,
    stateStack: FollowerStateStack<Aux>,
    stateFactoryVisitor: FollowerStateFactoryVisitor<Aux>
) : BaseEntityFollowerState<Aux>(root, stateStack, stateFactoryVisitor) {
    override val visitCursorMove = VisitFollowerRootModel(root)

    override fun follow(move: LeaderModelCursorMove<Aux>) = when (move) {
        is LeaveLeaderRootModel -> {
            stateStack.pollFirst()
            LeaveFollowerRootModel(root)
        }
        else -> super.follow(move)
    }
}

private class EntityFollowerState<Aux>(
    private val entity: EntityModel<Aux>,
    stateStack: FollowerStateStack<Aux>,
    stateFactoryVisitor: FollowerStateFactoryVisitor<Aux>
) : BaseEntityFollowerState<Aux>(entity, stateStack, stateFactoryVisitor) {
    override val visitCursorMove = VisitFollowerEntityModel(entity)

    override fun follow(move: LeaderModelCursorMove<Aux>) = when (move) {
        is LeaveLeaderEntityModel -> {
            stateStack.pollFirst()
            LeaveFollowerEntityModel(entity)
        }
        else -> super.follow(move)
    }
}

// Scalar fields

private class ScalarFieldFollowerState<Aux>(
    private val field: FieldModel<Aux>,
    stack: FollowerStateStack<Aux>
) : FollowerState<Aux>(field, stack) {
    override val visitCursorMove = VisitFollowerFieldModel(field)

    override fun follow(move: LeaderModelCursorMove<Aux>) = when (move) {
        is LeaveLeaderFieldModel -> {
            stateStack.pollFirst()
            LeaveFollowerFieldModel(field)
        }
        else -> super.follow(move)
    }
}

private class CompositionFieldFollowerState<Aux>(
    private val field: CompositionFieldModel<Aux>,
    stack: FollowerStateStack<Aux>,
    private val stateFactoryVisitor: FollowerStateFactoryVisitor<Aux>
) : FollowerState<Aux>(field, stack) {
    override val visitCursorMove = VisitFollowerFieldModel(field)

    override fun follow(move: LeaderModelCursorMove<Aux>) = when (move) {
        is LeaveLeaderFieldModel -> {
            stateStack.pollFirst()
            LeaveFollowerFieldModel(field)
        }
        is VisitLeaderEntityModel -> {
            val entityState =
                field.value.dispatch(stateFactoryVisitor) ?: throw IllegalStateException("null entity state")
            stateStack.addFirst(entityState)
            entityState.visitCursorMove
        }
        else -> super.follow(move)
    }
}

// List fields

private class PrimitiveListFieldFollowerState<Aux>(
    private val primitiveListField: PrimitiveListFieldModel<Aux>,
    stack: FollowerStateStack<Aux>,
    private val stateFactoryVisitor: FollowerStateFactoryVisitor<Aux>
) : FollowerState<Aux>(primitiveListField, stack) {
    override val visitCursorMove = VisitFollowerListFieldModel(primitiveListField)

    override fun follow(move: LeaderModelCursorMove<Aux>) = when (move) {
        is LeaveLeaderListFieldModel -> {
            stateStack.pollFirst()
            LeaveFollowerListFieldModel(primitiveListField)
        }
        is VisitLeaderFieldModel -> {
            val leaderField =
                move.element as? PrimitiveFieldModel ?: throw IllegalStateException("expected primitive field")
            val followerField = primitiveListField.getPrimitiveField(leaderField.value)
            val elementState = if (followerField == null) NullFollowerState(VisitFollowerFieldModel(null), stateStack)
            else followerField.dispatch(stateFactoryVisitor) ?: throw IllegalStateException("null element state")
            stateStack.addFirst(elementState)
            elementState.visitCursorMove
        }
        else -> super.follow(move)
    }
}

private class AliasListFieldFollowerState<Aux>(
    private val aliasListField: AliasListFieldModel<Aux>,
    stack: FollowerStateStack<Aux>,
    private val stateFactoryVisitor: FollowerStateFactoryVisitor<Aux>
) : FollowerState<Aux>(aliasListField, stack) {
    override val visitCursorMove = VisitFollowerListFieldModel(aliasListField)

    override fun follow(move: LeaderModelCursorMove<Aux>) = when (move) {
        is LeaveLeaderListFieldModel -> {
            stateStack.pollFirst()
            LeaveFollowerListFieldModel(aliasListField)
        }
        is VisitLeaderFieldModel -> {
            val leaderField =
                move.element as? AliasFieldModel ?: throw IllegalStateException("expected alias field")
            val followerField = aliasListField.getAliasField(leaderField.value)
            val elementState = if (followerField == null) NullFollowerState(VisitFollowerFieldModel(null), stateStack)
            else followerField.dispatch(stateFactoryVisitor) ?: throw IllegalStateException("null element state")
            stateStack.addFirst(elementState)
            elementState.visitCursorMove
        }
        else -> super.follow(move)
    }
}

private class EnumerationListFieldFollowerState<Aux>(
    private val enumerationListField: EnumerationListFieldModel<Aux>,
    stack: FollowerStateStack<Aux>,
    private val stateFactoryVisitor: FollowerStateFactoryVisitor<Aux>
) : FollowerState<Aux>(enumerationListField, stack) {
    override val visitCursorMove = VisitFollowerListFieldModel(enumerationListField)

    override fun follow(move: LeaderModelCursorMove<Aux>) = when (move) {
        is LeaveLeaderListFieldModel -> {
            stateStack.pollFirst()
            LeaveFollowerListFieldModel(enumerationListField)
        }
        is VisitLeaderFieldModel -> {
            val leaderField =
                move.element as? EnumerationFieldModel ?: throw IllegalStateException("expected enumeration field")
            val followerField = enumerationListField.getEnumerationField(leaderField.value)
            val elementState = if (followerField == null) NullFollowerState(VisitFollowerFieldModel(null), stateStack)
            else followerField.dispatch(stateFactoryVisitor) ?: throw IllegalStateException("null element state")
            stateStack.addFirst(elementState)
            elementState.visitCursorMove
        }
        else -> super.follow(move)
    }
}

private class AssociationListFieldFollowerState<Aux>(
    private val associationListField: AssociationListFieldModel<Aux>,
    stack: FollowerStateStack<Aux>,
    private val stateFactoryVisitor: FollowerStateFactoryVisitor<Aux>
) : FollowerState<Aux>(associationListField, stack) {
    override val visitCursorMove = VisitFollowerListFieldModel(associationListField)

    override fun follow(move: LeaderModelCursorMove<Aux>) = when (move) {
        is LeaveLeaderListFieldModel -> {
            stateStack.pollFirst()
            LeaveFollowerListFieldModel(associationListField)
        }
        is VisitLeaderFieldModel -> {
            val leaderField =
                move.element as? AssociationFieldModel ?: throw IllegalStateException("expected association field")
            val followerField = associationListField.getAssociationField(leaderField.value)
            val elementState = if (followerField == null) NullFollowerState(VisitFollowerFieldModel(null), stateStack)
            else followerField.dispatch(stateFactoryVisitor) ?: throw IllegalStateException("null element state")
            stateStack.addFirst(elementState)
            elementState.visitCursorMove
        }
        else -> super.follow(move)
    }
}

private class CompositionListFieldFollowerState<Aux>(
    private val compositionListField: CompositionListFieldModel<Aux>,
    stack: FollowerStateStack<Aux>,
    private val stateFactoryVisitor: FollowerStateFactoryVisitor<Aux>
) : FollowerState<Aux>(compositionListField, stack) {
    override val visitCursorMove = VisitFollowerListFieldModel(compositionListField)

    override fun follow(move: LeaderModelCursorMove<Aux>) = when (move) {
        is LeaveLeaderListFieldModel -> {
            stateStack.pollFirst()
            LeaveFollowerListFieldModel(compositionListField)
        }
        is VisitLeaderEntityModel -> {
            val leaderEntity = move.element
            val followerField = compositionListField.getEntity(leaderEntity)
            val elementState = if (followerField == null) NullFollowerState(VisitFollowerEntityModel(null), stateStack)
            else followerField.dispatch(stateFactoryVisitor) ?: throw IllegalStateException("null element state")
            stateStack.addFirst(elementState)
            elementState.visitCursorMove
        }
        else -> super.follow(move)
    }
}

// Field values

private class EntityKeysFollowerState<Aux>(
    private val entityKeys: EntityKeysModel<Aux>,
    stateStack: FollowerStateStack<Aux>,
    stateFactoryVisitor: FollowerStateFactoryVisitor<Aux>
) : BaseEntityFollowerState<Aux>(entityKeys, stateStack, stateFactoryVisitor) {
    override val visitCursorMove = VisitFollowerEntityKeysModel(entityKeys)

    override fun follow(move: LeaderModelCursorMove<Aux>) = when (move) {
        is LeaveLeaderEntityKeysModel -> {
            stateStack.pollFirst()
            LeaveFollowerEntityKeysModel(entityKeys)
        }
        else -> super.follow(move)
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
