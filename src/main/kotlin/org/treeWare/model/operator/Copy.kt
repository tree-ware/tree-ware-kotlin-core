package org.treeWare.model.operator

import org.treeWare.model.core.*
import org.treeWare.model.traversal.AbstractLeader1Follower0ModelVisitor
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.model.traversal.forEach
import java.util.*

fun <Aux> copy(from: ElementModel<Aux>, to: MutableElementModel<Aux>) {
    if (from.elementType != to.elementType) throw IllegalArgumentException("Types of from and to are different: ${from.elementType}, ${to.elementType}")
    val copyVisitor = CopyVisitor<Aux>(to)
    forEach(from, copyVisitor)
}

private class CopyVisitor<Aux>(
    private val to: MutableElementModel<Aux>
) : AbstractLeader1Follower0ModelVisitor<Aux, TraversalAction>(TraversalAction.CONTINUE) {
    val modelStack = ArrayDeque<MutableElementModel<Aux>>()

    override fun visit(leaderMain1: MainModel<Aux>): TraversalAction {
        assert(modelStack.isEmpty())
        modelStack.addFirst(to)
        return TraversalAction.CONTINUE
    }

    override fun leave(leaderMain1: MainModel<Aux>) {
        modelStack.pollFirst()
    }

    override fun visit(leaderRoot1: RootModel<Aux>): TraversalAction {
        val copyRoot = if (modelStack.isEmpty()) to
        else {
            val copyParent = modelStack.peekFirst() as MutableMainModel<Aux>
            copyParent.getOrNewRoot()
        }
        modelStack.addFirst(copyRoot)
        return TraversalAction.CONTINUE
    }

    override fun leave(leaderRoot1: RootModel<Aux>) {
        modelStack.pollFirst()
    }

    override fun visit(leaderEntity1: EntityModel<Aux>): TraversalAction {
        val copyEntity = if (modelStack.isEmpty()) to
        else {
            val copyParent = modelStack.peekFirst()
            getNewFieldValue(copyParent)
        }
        modelStack.addFirst(copyEntity)
        return TraversalAction.CONTINUE
    }

    override fun leave(leaderEntity1: EntityModel<Aux>) {
        val copyEntity = modelStack.pollFirst() as MutableEntityModel<Aux>
        // NOTE: entities should be added to set-fields only after the entity
        // has key fields.
        val copyParent = modelStack.peekFirst()
        if (copyParent.elementType == ModelElementType.SET_FIELD) (copyParent as MutableSetFieldModel<Aux>).addValue(
            copyEntity
        )
    }

    override fun visit(leaderField1: SingleFieldModel<Aux>): TraversalAction = visitField(leaderField1)

    override fun leave(leaderField1: SingleFieldModel<Aux>) {
        modelStack.pollFirst()
    }

    override fun visit(leaderField1: ListFieldModel<Aux>): TraversalAction = visitField(leaderField1)

    override fun leave(leaderField1: ListFieldModel<Aux>) {
        modelStack.pollFirst()
    }

    override fun visit(leaderField1: SetFieldModel<Aux>): TraversalAction = visitField(leaderField1)

    override fun leave(leaderField1: SetFieldModel<Aux>) {
        modelStack.pollFirst()
    }

    override fun visit(leaderValue1: PrimitiveModel<Aux>): TraversalAction {
        val copyPrimitive = if (modelStack.isEmpty()) to
        else {
            val copyParent = modelStack.peekFirst()
            getNewFieldValue(copyParent)
        } as MutablePrimitiveModel<Aux>
        copyPrimitive.copyValueFrom(leaderValue1)
        return TraversalAction.CONTINUE
    }

    override fun visit(leaderValue1: AliasModel<Aux>): TraversalAction {
        val copyAlias = if (modelStack.isEmpty()) to
        else {
            val copyParent = modelStack.peekFirst()
            getNewFieldValue(copyParent)
        } as MutableAliasModel<Aux>
        copyAlias.copyValueFrom(leaderValue1)
        return TraversalAction.CONTINUE
    }

    override fun visit(leaderValue1: Password1wayModel<Aux>): TraversalAction {
        val copyPassword = if (modelStack.isEmpty()) to
        else {
            val copyParent = modelStack.peekFirst()
            getNewFieldValue(copyParent)
        } as MutablePassword1wayModel<Aux>
        copyPassword.copyValueFrom(leaderValue1)
        return TraversalAction.CONTINUE
    }

    override fun visit(leaderValue1: Password2wayModel<Aux>): TraversalAction {
        val copyPassword = if (modelStack.isEmpty()) to
        else {
            val copyParent = modelStack.peekFirst()
            getNewFieldValue(copyParent)
        } as MutablePassword2wayModel<Aux>
        copyPassword.copyValueFrom(leaderValue1)
        return TraversalAction.CONTINUE
    }

    override fun visit(leaderValue1: EnumerationModel<Aux>): TraversalAction {
        val copyEnumeration = if (modelStack.isEmpty()) to
        else {
            val copyParent = modelStack.peekFirst()
            getNewFieldValue(copyParent)
        } as MutableEnumerationModel<Aux>
        copyEnumeration.copyValueFrom(leaderValue1)
        return TraversalAction.CONTINUE
    }

    override fun visit(leaderValue1: AssociationModel<Aux>): TraversalAction {
        val copyAssociation = if (modelStack.isEmpty()) to
        else {
            val copyParent = modelStack.peekFirst()
            getNewFieldValue(copyParent)
        } as MutableAssociationModel<Aux>
        // NOTE: forEach() does not traverse the association's entity-keys.
        // So they have to be explicitly copied by this method.
        copyAssociation.newValue()
        leaderValue1.value.zip(copyAssociation.value).forEach { (leaderEntityKeys, copyEntityKeys) ->
            copy(leaderEntityKeys, copyEntityKeys)
        }
        return TraversalAction.CONTINUE
    }

    override fun visit(leaderEntityKeys1: EntityKeysModel<Aux>): TraversalAction {
        assert(modelStack.isEmpty())
        val copyEntityKeys = to as MutableEntityKeysModel<Aux>
        modelStack.addFirst(copyEntityKeys)
        return TraversalAction.CONTINUE
    }

    override fun leave(leaderEntityKeys1: EntityKeysModel<Aux>) {
        modelStack.pollFirst()
    }

    // Helpers

    private fun visitField(leaderField1: FieldModel<Aux>): TraversalAction {
        val leaderFieldName = getFieldName(leaderField1)
        val copyParent = modelStack.peekFirst() as MutableBaseEntityModel<Aux>
        val copyField = copyParent.getOrNewField(leaderFieldName)
        modelStack.addFirst(copyField)
        return TraversalAction.CONTINUE
    }
}
