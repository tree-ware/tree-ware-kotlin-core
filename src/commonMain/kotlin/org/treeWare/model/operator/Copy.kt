package org.treeWare.model.operator

import org.treeWare.model.core.*
import org.treeWare.model.traversal.AbstractLeader1ModelVisitor
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.model.traversal.forEach
import org.treeWare.util.assertInDevMode

fun copy(from: ElementModel, to: MutableElementModel, replaceLists: Boolean = false) {
    if (from.elementType != to.elementType) throw IllegalArgumentException("Types of from and to are different: ${from.elementType}, ${to.elementType}")
    val copyVisitor = CopyVisitor(to, replaceLists)
    forEach(from, copyVisitor, true)
}

private class CopyVisitor(
    private val to: MutableElementModel,
    private val replaceLists: Boolean
) : AbstractLeader1ModelVisitor<TraversalAction>(TraversalAction.CONTINUE) {
    val modelStack = ArrayDeque<MutableElementModel>()

    override fun visitMain(leaderMain1: MainModel): TraversalAction {
        assertInDevMode(modelStack.isEmpty())
        modelStack.addFirst(to)
        return TraversalAction.CONTINUE
    }

    override fun leaveMain(leaderMain1: MainModel) {
        modelStack.removeFirst()
    }

    override fun visitEntity(leaderEntity1: EntityModel): TraversalAction {
        val copyEntity = if (modelStack.isEmpty()) to
        else {
            val copyParent = modelStack.first()
            copyParent.getNewValue()
        }
        modelStack.addFirst(copyEntity)
        return TraversalAction.CONTINUE
    }

    override fun leaveEntity(leaderEntity1: EntityModel) {
        val copyEntity = modelStack.removeFirst() as MutableEntityModel
        // NOTE: entities should be added to set-fields only after the entity
        // has key fields.
        val copyParent = modelStack.firstOrNull() ?: return
        if (copyParent.elementType == ModelElementType.SET_FIELD) (copyParent as MutableSetFieldModel).addValue(
            copyEntity
        )
    }

    override fun visitSingleField(leaderField1: SingleFieldModel): TraversalAction = visitField(leaderField1)

    override fun leaveSingleField(leaderField1: SingleFieldModel) {
        modelStack.removeFirst()
    }

    override fun visitListField(leaderField1: ListFieldModel): TraversalAction {
        val action = visitField(leaderField1)
        if (replaceLists) (modelStack.first() as MutableListFieldModel).clear()
        return action
    }

    override fun leaveListField(leaderField1: ListFieldModel) {
        modelStack.removeFirst()
    }

    override fun visitSetField(leaderField1: SetFieldModel): TraversalAction = visitField(leaderField1)

    override fun leaveSetField(leaderField1: SetFieldModel) {
        modelStack.removeFirst()
    }

    override fun visitPrimitive(leaderValue1: PrimitiveModel): TraversalAction {
        val copyPrimitive = if (modelStack.isEmpty()) to
        else {
            val copyParent = modelStack.first()
            copyParent.getNewValue()
        } as MutablePrimitiveModel
        copyPrimitive.copyValueFrom(leaderValue1)
        return TraversalAction.CONTINUE
    }

    override fun visitAlias(leaderValue1: AliasModel): TraversalAction {
        val copyAlias = if (modelStack.isEmpty()) to
        else {
            val copyParent = modelStack.first()
            copyParent.getNewValue()
        } as MutableAliasModel
        copyAlias.copyValueFrom(leaderValue1)
        return TraversalAction.CONTINUE
    }

    override fun visitPassword1way(leaderValue1: Password1wayModel): TraversalAction {
        val copyPassword = if (modelStack.isEmpty()) to
        else {
            val copyParent = modelStack.first()
            copyParent.getNewValue()
        } as MutablePassword1wayModel
        copyPassword.copyValueFrom(leaderValue1)
        return TraversalAction.CONTINUE
    }

    override fun visitPassword2way(leaderValue1: Password2wayModel): TraversalAction {
        val copyPassword = if (modelStack.isEmpty()) to
        else {
            val copyParent = modelStack.first()
            copyParent.getNewValue()
        } as MutablePassword2wayModel
        copyPassword.copyValueFrom(leaderValue1)
        return TraversalAction.CONTINUE
    }

    override fun visitEnumeration(leaderValue1: EnumerationModel): TraversalAction {
        val copyEnumeration = if (modelStack.isEmpty()) to
        else {
            val copyParent = modelStack.first()
            copyParent.getNewValue()
        } as MutableEnumerationModel
        copyEnumeration.copyValueFrom(leaderValue1)
        return TraversalAction.CONTINUE
    }

    override fun visitAssociation(leaderValue1: AssociationModel): TraversalAction {
        val copyAssociation = if (modelStack.isEmpty()) to
        else {
            val copyParent = modelStack.first()
            copyParent.getNewValue()
        } as MutableAssociationModel
        modelStack.addFirst(copyAssociation)
        return TraversalAction.CONTINUE
    }

    override fun leaveAssociation(leaderValue1: AssociationModel) {
        modelStack.removeFirst()
    }

    // Helpers

    private fun visitField(leaderField1: FieldModel): TraversalAction {
        val copyField = if (modelStack.isEmpty()) to
        else {
            val leaderFieldName = getFieldName(leaderField1)
            val copyParent = modelStack.first() as MutableBaseEntityModel
            copyParent.getOrNewField(leaderFieldName)
        }
        modelStack.addFirst(copyField)
        return TraversalAction.CONTINUE
    }
}