package org.treeWare.model.operator

import org.treeWare.model.core.*
import org.treeWare.model.operator.rbac.aux.PermitGetAuxStack
import org.treeWare.model.operator.rbac.aux.getPermissionsAux
import org.treeWare.model.traversal.AbstractLeader1Follower1ModelVisitor
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.model.traversal.forEach
import org.treeWare.util.assertInDevMode

// NOTE: wildcards in the get-model do not match explicit keys in the RBAC model.

/** Return a subset of `get` that is permitted by `rbac`. */
fun permitGet(get: MainModel, rbac: MainModel): MutableMainModel? {
    val visitor = PermitGetVisitor()
    forEach(get, rbac, visitor, true)
    // TODO(cleanup): add ability to check if root exists without the use of runCatching.
    val permittedRoot = runCatching { visitor.permittedMain.root }.getOrNull()
    return if (permittedRoot == null) null else visitor.permittedMain
}

private class PermitGetVisitor : AbstractLeader1Follower1ModelVisitor<TraversalAction>(
    TraversalAction.CONTINUE
) {
    lateinit var permittedMain: MutableMainModel
    val modelStack = ArrayDeque<MutableElementModel?>()
    val auxStack = PermitGetAuxStack()

    override fun visitMain(leaderMain1: MainModel, followerMain1: MainModel?): TraversalAction {
        permittedMain = MutableMainModel(leaderMain1.mainMeta)
        modelStack.addFirst(permittedMain)
        auxStack.push(getPermissionsAux(followerMain1))
        return if (auxStack.isGetPermitted()) TraversalAction.CONTINUE else TraversalAction.ABORT_SUB_TREE
    }

    override fun leaveMain(leaderMain1: MainModel, followerMain1: MainModel?) {
        auxStack.pop()
        assertInDevMode(auxStack.isEmpty())
        modelStack.removeFirst()
        assertInDevMode(modelStack.isEmpty())
    }

    override fun visitEntity(leaderEntity1: EntityModel, followerEntity1: EntityModel?): TraversalAction {
        val permittedParent = modelStack.first() ?: throw IllegalStateException("Entity parent is null")
        val permittedEntity = newChildValue(permittedParent)
        modelStack.addFirst(permittedEntity)
        auxStack.push(getPermissionsAux(followerEntity1))
        return TraversalAction.CONTINUE
    }

    override fun leaveEntity(leaderEntity1: EntityModel, followerEntity1: EntityModel?) {
        auxStack.pop()
        val permittedEntity = modelStack.removeFirst() ?: throw IllegalStateException("Entity parent is null")
        // NOTE: entities should be added to set-fields only after the entity
        // has key fields.
        val permittedParent = modelStack.firstOrNull() ?: return
        if (permittedParent.elementType == ModelElementType.SET_FIELD) {
            // If the keys don't exist, an exception will be thrown. Catch and ignore it.
            runCatching { (permittedParent as MutableSetFieldModel).addValue(permittedEntity) }
        }
    }

    override fun visitSingleField(leaderField1: SingleFieldModel, followerField1: SingleFieldModel?): TraversalAction =
        visitField(leaderField1, followerField1)

    override fun leaveSingleField(leaderField1: SingleFieldModel, followerField1: SingleFieldModel?) {
        leaveField()
    }

    override fun visitListField(leaderField1: ListFieldModel, followerField1: ListFieldModel?): TraversalAction =
        visitField(leaderField1, followerField1)

    override fun leaveListField(leaderField1: ListFieldModel, followerField1: ListFieldModel?) {
        leaveField()
    }

    override fun visitSetField(leaderField1: SetFieldModel, followerField1: SetFieldModel?): TraversalAction =
        visitField(leaderField1, followerField1)

    override fun leaveSetField(leaderField1: SetFieldModel, followerField1: SetFieldModel?) {
        leaveField()
    }

    override fun visitPrimitive(leaderValue1: PrimitiveModel, followerValue1: PrimitiveModel?): TraversalAction {
        val permittedParent = modelStack.first() ?: throw IllegalStateException("Primitive parent is null")
        val permittedPrimitive = newChildValue(permittedParent) as MutablePrimitiveModel
        permittedPrimitive.copyValueFrom(leaderValue1)
        return TraversalAction.CONTINUE
    }

    override fun visitAlias(leaderValue1: AliasModel, followerValue1: AliasModel?): TraversalAction {
        val permittedParent = modelStack.first() ?: throw IllegalStateException("Alias parent is null")
        val permittedAlias = newChildValue(permittedParent) as MutableAliasModel
        permittedAlias.copyValueFrom(leaderValue1)
        return TraversalAction.CONTINUE
    }

    override fun visitPassword1way(
        leaderValue1: Password1wayModel,
        followerValue1: Password1wayModel?
    ): TraversalAction {
        val permittedParent = modelStack.first() ?: throw IllegalStateException("Password1way parent is null")
        val permittedPassword = newChildValue(permittedParent) as MutablePassword1wayModel
        permittedPassword.copyValueFrom(leaderValue1)
        return TraversalAction.CONTINUE
    }

    override fun visitPassword2way(
        leaderValue1: Password2wayModel,
        followerValue1: Password2wayModel?
    ): TraversalAction {
        val permittedParent = modelStack.first() ?: throw IllegalStateException("Password2way parent is null")
        val permittedPassword = newChildValue(permittedParent) as MutablePassword2wayModel
        permittedPassword.copyValueFrom(leaderValue1)
        return TraversalAction.CONTINUE
    }

    override fun visitEnumeration(leaderValue1: EnumerationModel, followerValue1: EnumerationModel?): TraversalAction {
        val permittedParent = modelStack.first() ?: throw IllegalStateException("Enumeration parent is null")
        val permittedEnumeration = newChildValue(permittedParent) as MutableEnumerationModel
        permittedEnumeration.copyValueFrom(leaderValue1)
        return TraversalAction.CONTINUE
    }

    override fun visitAssociation(leaderValue1: AssociationModel, followerValue1: AssociationModel?): TraversalAction {
        val permittedParent = modelStack.first() ?: throw IllegalStateException("Association parent is null")
        val permittedAssociation = newChildValue(permittedParent)
        modelStack.addFirst(permittedAssociation)
        return TraversalAction.CONTINUE
    }

    override fun leaveAssociation(leaderValue1: AssociationModel, followerValue1: AssociationModel?) {
        modelStack.removeFirst()
    }

    // Helpers

    private fun visitField(leaderField1: FieldModel, followerField1: FieldModel?): TraversalAction {
        auxStack.push(getPermissionsAux(followerField1))
        if (!auxStack.isGetPermitted()) {
            modelStack.addFirst(null)
            return TraversalAction.ABORT_SUB_TREE
        }
        val permittedParent = modelStack.first() as MutableBaseEntityModel
        val leaderFieldName = getFieldName(leaderField1)
        val permittedField = permittedParent.getOrNewField(leaderFieldName)
        modelStack.addFirst(permittedField)
        return TraversalAction.CONTINUE
    }

    private fun leaveField() {
        auxStack.pop()
        modelStack.removeFirst()
    }
}