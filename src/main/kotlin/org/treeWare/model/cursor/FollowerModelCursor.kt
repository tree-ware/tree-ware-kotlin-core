package org.treeWare.model.cursor

import org.treeWare.model.core.*
import org.treeWare.model.operator.AbstractLeader1Follower0ModelVisitor
import org.treeWare.model.operator.dispatchVisit
import java.util.*

class FollowerModelCursor<LeaderAux, FollowerAux>(private val initial: ElementModel<FollowerAux>) {
    private val stateStack = FollowerStateStack<LeaderAux, FollowerAux>()
    private val stateFactoryVisitor = FollowerStateFactoryVisitor(stateStack)
    private var isAtStart = true

    val element: ElementModel<FollowerAux>? get() = stateStack.peekFirst()?.element

    fun follow(move: LeaderModelCursorMove<LeaderAux>): FollowerModelCursorMove<FollowerAux>? = when {
        isAtStart -> {
            isAtStart = false
            val initialState =
                dispatchVisit(initial, stateFactoryVisitor) ?: throw IllegalStateException("null initial state")
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

private typealias FollowerStateStack<LeaderAux, FollowerAux> = ArrayDeque<FollowerState<LeaderAux, FollowerAux>>

private abstract class FollowerState<LeaderAux, FollowerAux>(
    val element: ElementModel<FollowerAux>?,
    protected val stateStack: FollowerStateStack<LeaderAux, FollowerAux>
) {
    abstract val visitCursorMove: FollowerModelCursorMove<FollowerAux>

    open fun follow(move: LeaderModelCursorMove<LeaderAux>): FollowerModelCursorMove<FollowerAux>? {
        throw IllegalStateException("Unknown move $move in state $this")
    }
}

private class NullFollowerState<LeaderAux, FollowerAux>(
    override val visitCursorMove: FollowerModelCursorMove<FollowerAux>,
    stateStack: FollowerStateStack<LeaderAux, FollowerAux>
) : FollowerState<LeaderAux, FollowerAux>(null, stateStack) {
    override fun follow(move: LeaderModelCursorMove<LeaderAux>) = when (move) {
        is VisitLeaderModel -> {
            stateStack.addFirst(this)
            VisitFollowerModel(null)
        }
        is LeaveLeaderModel -> {
            stateStack.pollFirst()
            LeaveFollowerModel(null)
        }
        is VisitLeaderRootModel -> {
            stateStack.addFirst(this)
            VisitFollowerRootModel(null)
        }
        is LeaveLeaderRootModel -> {
            stateStack.pollFirst()
            LeaveFollowerRootModel(null)
        }
        is VisitLeaderEntityModel -> {
            stateStack.addFirst(this)
            VisitFollowerEntityModel(null)
        }
        is LeaveLeaderEntityModel -> {
            stateStack.pollFirst()
            LeaveFollowerEntityModel(null)
        }
        is VisitLeaderFieldModel -> {
            stateStack.addFirst(this)
            VisitFollowerFieldModel(null)
        }
        is LeaveLeaderFieldModel -> {
            stateStack.pollFirst()
            LeaveFollowerFieldModel(null)
        }
        is VisitLeaderListFieldModel -> {
            stateStack.addFirst(this)
            VisitFollowerListFieldModel(null)
        }
        is LeaveLeaderListFieldModel -> {
            stateStack.pollFirst()
            LeaveFollowerListFieldModel(null)
        }
        is VisitLeaderEntityKeysModel -> {
            stateStack.addFirst(this)
            VisitFollowerEntityKeysModel<FollowerAux>(null)
        }
        is LeaveLeaderEntityKeysModel -> {
            stateStack.pollFirst()
            LeaveFollowerEntityKeysModel<FollowerAux>(null)
        }
    }
}

private class ModelFollowerState<LeaderAux, FollowerAux>(
    private val model: Model<FollowerAux>,
    stateStack: FollowerStateStack<LeaderAux, FollowerAux>,
    private val stateFactoryVisitor: FollowerStateFactoryVisitor<LeaderAux, FollowerAux>
) : FollowerState<LeaderAux, FollowerAux>(model, stateStack) {
    override val visitCursorMove = VisitFollowerModel(model)

    override fun follow(move: LeaderModelCursorMove<LeaderAux>) = when (move) {
        is LeaveLeaderModel -> {
            stateStack.pollFirst()
            LeaveFollowerModel(model)
        }
        is VisitLeaderRootModel -> {
            val rootState =
                dispatchVisit(model.root, stateFactoryVisitor) ?: throw IllegalStateException("null root state")
            stateStack.addFirst(rootState)
            rootState.visitCursorMove
        }
        else -> super.follow(move)
    }
}

private abstract class BaseEntityFollowerState<LeaderAux, FollowerAux>(
    private val baseEntity: BaseEntityModel<FollowerAux>,
    stack: FollowerStateStack<LeaderAux, FollowerAux>,
    private val stateFactoryVisitor: FollowerStateFactoryVisitor<LeaderAux, FollowerAux>
) : FollowerState<LeaderAux, FollowerAux>(baseEntity, stack) {
    override fun follow(move: LeaderModelCursorMove<LeaderAux>) = when (move) {
        is VisitLeaderFieldModel -> visitField(move.element, VisitFollowerFieldModel(null))
        is VisitLeaderListFieldModel -> visitField(move.element, VisitFollowerListFieldModel(null))
        else -> super.follow(move)
    }

    private fun visitField(
        leaderField: FieldModel<LeaderAux>,
        nullVisitCursorMove: FollowerModelCursorMove<FollowerAux>
    ): FollowerModelCursorMove<FollowerAux> {
        val followerField = baseEntity.getField(leaderField.schema.name)
        val fieldState = if (followerField == null) NullFollowerState(nullVisitCursorMove, stateStack)
        else dispatchVisit(followerField, stateFactoryVisitor) ?: throw IllegalStateException("null field state")
        stateStack.addFirst(fieldState)
        return fieldState.visitCursorMove
    }
}

private class RootFollowerState<LeaderAux, FollowerAux>(
    private val root: RootModel<FollowerAux>,
    stateStack: FollowerStateStack<LeaderAux, FollowerAux>,
    stateFactoryVisitor: FollowerStateFactoryVisitor<LeaderAux, FollowerAux>
) : BaseEntityFollowerState<LeaderAux, FollowerAux>(root, stateStack, stateFactoryVisitor) {
    override val visitCursorMove = VisitFollowerRootModel(root)

    override fun follow(move: LeaderModelCursorMove<LeaderAux>) = when (move) {
        is LeaveLeaderRootModel -> {
            stateStack.pollFirst()
            LeaveFollowerRootModel(root)
        }
        else -> super.follow(move)
    }
}

private class EntityFollowerState<LeaderAux, FollowerAux>(
    private val entity: EntityModel<FollowerAux>,
    stateStack: FollowerStateStack<LeaderAux, FollowerAux>,
    stateFactoryVisitor: FollowerStateFactoryVisitor<LeaderAux, FollowerAux>
) : BaseEntityFollowerState<LeaderAux, FollowerAux>(entity, stateStack, stateFactoryVisitor) {
    override val visitCursorMove = VisitFollowerEntityModel(entity)

    override fun follow(move: LeaderModelCursorMove<LeaderAux>) = when (move) {
        is LeaveLeaderEntityModel -> {
            stateStack.pollFirst()
            LeaveFollowerEntityModel(entity)
        }
        else -> super.follow(move)
    }
}

// Scalar fields

private class ScalarFieldFollowerState<LeaderAux, FollowerAux>(
    private val field: FieldModel<FollowerAux>,
    stack: FollowerStateStack<LeaderAux, FollowerAux>
) : FollowerState<LeaderAux, FollowerAux>(field, stack) {
    override val visitCursorMove = VisitFollowerFieldModel(field)

    override fun follow(move: LeaderModelCursorMove<LeaderAux>) = when (move) {
        is LeaveLeaderFieldModel -> {
            stateStack.pollFirst()
            LeaveFollowerFieldModel(field)
        }
        else -> super.follow(move)
    }
}

private class CompositionFieldFollowerState<LeaderAux, FollowerAux>(
    private val field: CompositionFieldModel<FollowerAux>,
    stack: FollowerStateStack<LeaderAux, FollowerAux>,
    private val stateFactoryVisitor: FollowerStateFactoryVisitor<LeaderAux, FollowerAux>
) : FollowerState<LeaderAux, FollowerAux>(field, stack) {
    override val visitCursorMove = VisitFollowerFieldModel(field)

    override fun follow(move: LeaderModelCursorMove<LeaderAux>) = when (move) {
        is LeaveLeaderFieldModel -> {
            stateStack.pollFirst()
            LeaveFollowerFieldModel(field)
        }
        is VisitLeaderEntityModel -> {
            val entityState =
                dispatchVisit(field.value, stateFactoryVisitor) ?: throw IllegalStateException("null entity state")
            stateStack.addFirst(entityState)
            entityState.visitCursorMove
        }
        else -> super.follow(move)
    }
}

// List fields

private class PrimitiveListFieldFollowerState<LeaderAux, FollowerAux>(
    private val primitiveListField: PrimitiveListFieldModel<FollowerAux>,
    stack: FollowerStateStack<LeaderAux, FollowerAux>,
    private val stateFactoryVisitor: FollowerStateFactoryVisitor<LeaderAux, FollowerAux>
) : FollowerState<LeaderAux, FollowerAux>(primitiveListField, stack) {
    override val visitCursorMove = VisitFollowerListFieldModel(primitiveListField)

    override fun follow(move: LeaderModelCursorMove<LeaderAux>) = when (move) {
        is LeaveLeaderListFieldModel -> {
            stateStack.pollFirst()
            LeaveFollowerListFieldModel(primitiveListField)
        }
        is VisitLeaderFieldModel -> {
            val leaderField =
                move.element as? PrimitiveFieldModel ?: throw IllegalStateException("expected primitive field")
            val followerField = primitiveListField.getPrimitiveField(leaderField.value)
            val elementState = if (followerField == null) NullFollowerState(VisitFollowerFieldModel(null), stateStack)
            else dispatchVisit(followerField, stateFactoryVisitor) ?: throw IllegalStateException("null element state")
            stateStack.addFirst(elementState)
            elementState.visitCursorMove
        }
        else -> super.follow(move)
    }
}

private class AliasListFieldFollowerState<LeaderAux, FollowerAux>(
    private val aliasListField: AliasListFieldModel<FollowerAux>,
    stack: FollowerStateStack<LeaderAux, FollowerAux>,
    private val stateFactoryVisitor: FollowerStateFactoryVisitor<LeaderAux, FollowerAux>
) : FollowerState<LeaderAux, FollowerAux>(aliasListField, stack) {
    override val visitCursorMove = VisitFollowerListFieldModel(aliasListField)

    override fun follow(move: LeaderModelCursorMove<LeaderAux>) = when (move) {
        is LeaveLeaderListFieldModel -> {
            stateStack.pollFirst()
            LeaveFollowerListFieldModel(aliasListField)
        }
        is VisitLeaderFieldModel -> {
            val leaderField =
                move.element as? AliasFieldModel ?: throw IllegalStateException("expected alias field")
            val followerField = aliasListField.getAliasField(leaderField.value)
            val elementState = if (followerField == null) NullFollowerState(VisitFollowerFieldModel(null), stateStack)
            else dispatchVisit(followerField, stateFactoryVisitor) ?: throw IllegalStateException("null element state")
            stateStack.addFirst(elementState)
            elementState.visitCursorMove
        }
        else -> super.follow(move)
    }
}

private class EnumerationListFieldFollowerState<LeaderAux, FollowerAux>(
    private val enumerationListField: EnumerationListFieldModel<FollowerAux>,
    stack: FollowerStateStack<LeaderAux, FollowerAux>,
    private val stateFactoryVisitor: FollowerStateFactoryVisitor<LeaderAux, FollowerAux>
) : FollowerState<LeaderAux, FollowerAux>(enumerationListField, stack) {
    override val visitCursorMove = VisitFollowerListFieldModel(enumerationListField)

    override fun follow(move: LeaderModelCursorMove<LeaderAux>) = when (move) {
        is LeaveLeaderListFieldModel -> {
            stateStack.pollFirst()
            LeaveFollowerListFieldModel(enumerationListField)
        }
        is VisitLeaderFieldModel -> {
            val leaderField =
                move.element as? EnumerationFieldModel ?: throw IllegalStateException("expected enumeration field")
            val followerField = enumerationListField.getEnumerationField(leaderField.value)
            val elementState = if (followerField == null) NullFollowerState(VisitFollowerFieldModel(null), stateStack)
            else dispatchVisit(followerField, stateFactoryVisitor) ?: throw IllegalStateException("null element state")
            stateStack.addFirst(elementState)
            elementState.visitCursorMove
        }
        else -> super.follow(move)
    }
}

private class AssociationListFieldFollowerState<LeaderAux, FollowerAux>(
    private val associationListField: AssociationListFieldModel<FollowerAux>,
    stack: FollowerStateStack<LeaderAux, FollowerAux>,
    private val stateFactoryVisitor: FollowerStateFactoryVisitor<LeaderAux, FollowerAux>
) : FollowerState<LeaderAux, FollowerAux>(associationListField, stack) {
    override val visitCursorMove = VisitFollowerListFieldModel(associationListField)

    override fun follow(move: LeaderModelCursorMove<LeaderAux>) = when (move) {
        is LeaveLeaderListFieldModel -> {
            stateStack.pollFirst()
            LeaveFollowerListFieldModel(associationListField)
        }
        is VisitLeaderFieldModel -> {
            val leaderField =
                move.element as? AssociationFieldModel ?: throw IllegalStateException("expected association field")
            val followerField = associationListField.getAssociationField(leaderField.value)
            val elementState = if (followerField == null) NullFollowerState(VisitFollowerFieldModel(null), stateStack)
            else dispatchVisit(followerField, stateFactoryVisitor) ?: throw IllegalStateException("null element state")
            stateStack.addFirst(elementState)
            elementState.visitCursorMove
        }
        else -> super.follow(move)
    }
}

private class CompositionListFieldFollowerState<LeaderAux, FollowerAux>(
    private val compositionListField: CompositionListFieldModel<FollowerAux>,
    stack: FollowerStateStack<LeaderAux, FollowerAux>,
    private val stateFactoryVisitor: FollowerStateFactoryVisitor<LeaderAux, FollowerAux>
) : FollowerState<LeaderAux, FollowerAux>(compositionListField, stack) {
    override val visitCursorMove = VisitFollowerListFieldModel(compositionListField)

    override fun follow(move: LeaderModelCursorMove<LeaderAux>) = when (move) {
        is LeaveLeaderListFieldModel -> {
            stateStack.pollFirst()
            LeaveFollowerListFieldModel(compositionListField)
        }
        is VisitLeaderEntityModel -> {
            val leaderEntity = move.element
            val followerField = compositionListField.getEntity(leaderEntity)
            val elementState = if (followerField == null) NullFollowerState(VisitFollowerEntityModel(null), stateStack)
            else dispatchVisit(followerField, stateFactoryVisitor) ?: throw IllegalStateException("null element state")
            stateStack.addFirst(elementState)
            elementState.visitCursorMove
        }
        else -> super.follow(move)
    }
}

// Field values

private class EntityKeysFollowerState<LeaderAux, FollowerAux>(
    private val entityKeys: EntityKeysModel<FollowerAux>,
    stateStack: FollowerStateStack<LeaderAux, FollowerAux>,
    stateFactoryVisitor: FollowerStateFactoryVisitor<LeaderAux, FollowerAux>
) : BaseEntityFollowerState<LeaderAux, FollowerAux>(entityKeys, stateStack, stateFactoryVisitor) {
    override val visitCursorMove = VisitFollowerEntityKeysModel(entityKeys)

    override fun follow(move: LeaderModelCursorMove<LeaderAux>) = when (move) {
        is LeaveLeaderEntityKeysModel -> {
            stateStack.pollFirst()
            LeaveFollowerEntityKeysModel(entityKeys)
        }
        else -> super.follow(move)
    }
}

// State factory visitor

private class FollowerStateFactoryVisitor<LeaderAux, FollowerAux>(
    private val stateStack: FollowerStateStack<LeaderAux, FollowerAux>
) : AbstractLeader1Follower0ModelVisitor<FollowerAux, FollowerState<LeaderAux, FollowerAux>?>(null) {
    override fun visit(leaderModel1: Model<FollowerAux>) = ModelFollowerState(leaderModel1, stateStack, this)
    override fun visit(leaderRoot1: RootModel<FollowerAux>) = RootFollowerState(leaderRoot1, stateStack, this)
    override fun visit(leaderEntity1: EntityModel<FollowerAux>) = EntityFollowerState(leaderEntity1, stateStack, this)

    // Scalar fields

    override fun visit(leaderField1: PrimitiveFieldModel<FollowerAux>) =
        ScalarFieldFollowerState(leaderField1, stateStack)

    override fun visit(leaderField1: AliasFieldModel<FollowerAux>) = ScalarFieldFollowerState(leaderField1, stateStack)
    override fun visit(leaderField1: EnumerationFieldModel<FollowerAux>) =
        ScalarFieldFollowerState(leaderField1, stateStack)

    override fun visit(leaderField1: AssociationFieldModel<FollowerAux>) =
        ScalarFieldFollowerState(leaderField1, stateStack)

    override fun visit(leaderField1: CompositionFieldModel<FollowerAux>) =
        CompositionFieldFollowerState(leaderField1, stateStack, this)

    // List fields

    override fun visit(leaderField1: PrimitiveListFieldModel<FollowerAux>) =
        PrimitiveListFieldFollowerState(leaderField1, stateStack, this)

    override fun visit(leaderField1: AliasListFieldModel<FollowerAux>) =
        AliasListFieldFollowerState(leaderField1, stateStack, this)

    override fun visit(leaderField1: EnumerationListFieldModel<FollowerAux>) =
        EnumerationListFieldFollowerState(leaderField1, stateStack, this)

    override fun visit(leaderField1: AssociationListFieldModel<FollowerAux>) =
        AssociationListFieldFollowerState(leaderField1, stateStack, this)

    override fun visit(leaderField1: CompositionListFieldModel<FollowerAux>) =
        CompositionListFieldFollowerState(leaderField1, stateStack, this)

    // Field values

    override fun visit(leaderEntityKeys1: EntityKeysModel<FollowerAux>) =
        EntityKeysFollowerState(leaderEntityKeys1, stateStack, this)
}
