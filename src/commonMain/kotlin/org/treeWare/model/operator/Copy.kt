package org.treeWare.model.operator

import org.treeWare.model.core.*
import org.treeWare.model.traversal.AbstractLeader1ModelVisitor
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.model.traversal.forEach
import org.treeWare.util.assertInDevMode

fun copy(from: ElementModel, to: MutableElementModel) {
    if (from.elementType != to.elementType) throw IllegalArgumentException("Types of from and to are different: ${from.elementType}, ${to.elementType}")
    val copyVisitor = CopyVisitor(to)
    forEach(from, copyVisitor)
}

private class CopyVisitor(
    private val to: MutableElementModel
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
            getNewFieldValue(copyParent)
        }
        modelStack.addFirst(copyEntity)
        return TraversalAction.CONTINUE
    }

    override fun leaveEntity(leaderEntity1: EntityModel) {
        val copyEntity = modelStack.removeFirst() as MutableEntityModel
        // NOTE: entities should be added to set-fields only after the entity
        // has key fields.
        val copyParent = modelStack.first()
        if (copyParent.elementType == ModelElementType.SET_FIELD) (copyParent as MutableSetFieldModel).addValue(
            copyEntity
        )
    }

    override fun visitSingleField(leaderField1: SingleFieldModel): TraversalAction = visitField(leaderField1)

    override fun leaveSingleField(leaderField1: SingleFieldModel) {
        modelStack.removeFirst()
    }

    override fun visitListField(leaderField1: ListFieldModel): TraversalAction = visitField(leaderField1)

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
            getNewFieldValue(copyParent)
        } as MutablePrimitiveModel
        copyPrimitive.copyValueFrom(leaderValue1)
        return TraversalAction.CONTINUE
    }

    override fun visitAlias(leaderValue1: AliasModel): TraversalAction {
        val copyAlias = if (modelStack.isEmpty()) to
        else {
            val copyParent = modelStack.first()
            getNewFieldValue(copyParent)
        } as MutableAliasModel
        copyAlias.copyValueFrom(leaderValue1)
        return TraversalAction.CONTINUE
    }

    override fun visitPassword1way(leaderValue1: Password1wayModel): TraversalAction {
        val copyPassword = if (modelStack.isEmpty()) to
        else {
            val copyParent = modelStack.first()
            getNewFieldValue(copyParent)
        } as MutablePassword1wayModel
        copyPassword.copyValueFrom(leaderValue1)
        return TraversalAction.CONTINUE
    }

    override fun visitPassword2way(leaderValue1: Password2wayModel): TraversalAction {
        val copyPassword = if (modelStack.isEmpty()) to
        else {
            val copyParent = modelStack.first()
            getNewFieldValue(copyParent)
        } as MutablePassword2wayModel
        copyPassword.copyValueFrom(leaderValue1)
        return TraversalAction.CONTINUE
    }

    override fun visitEnumeration(leaderValue1: EnumerationModel): TraversalAction {
        val copyEnumeration = if (modelStack.isEmpty()) to
        else {
            val copyParent = modelStack.first()
            getNewFieldValue(copyParent)
        } as MutableEnumerationModel
        copyEnumeration.copyValueFrom(leaderValue1)
        return TraversalAction.CONTINUE
    }

    override fun visitAssociation(leaderValue1: AssociationModel): TraversalAction {
        val copyAssociation = if (modelStack.isEmpty()) to
        else {
            val copyParent = modelStack.first()
            getNewFieldValue(copyParent)
        } as MutableAssociationModel
        // NOTE: forEach() does not traverse the association's entity-keys.
        // So they have to be explicitly copied by this method.
        copyAssociation.newValue()
        leaderValue1.value.zip(copyAssociation.value).forEach { (leaderEntityKeys, copyEntityKeys) ->
            copy(leaderEntityKeys, copyEntityKeys)
        }
        return TraversalAction.CONTINUE
    }

    override fun visitEntityKeys(leaderEntityKeys1: EntityKeysModel): TraversalAction {
        assertInDevMode(modelStack.isEmpty())
        val copyEntityKeys = to as MutableEntityKeysModel
        modelStack.addFirst(copyEntityKeys)
        return TraversalAction.CONTINUE
    }

    override fun leaveEntityKeys(leaderEntityKeys1: EntityKeysModel) {
        modelStack.removeFirst()
    }

    // Helpers

    private fun visitField(leaderField1: FieldModel): TraversalAction {
        val leaderFieldName = getFieldName(leaderField1)
        val copyParent = modelStack.first() as MutableBaseEntityModel
        val copyField = copyParent.getOrNewField(leaderFieldName)
        modelStack.addFirst(copyField)
        return TraversalAction.CONTINUE
    }
}