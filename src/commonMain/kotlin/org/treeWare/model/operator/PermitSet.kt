package org.treeWare.model.operator

import org.treeWare.model.core.*
import org.treeWare.model.operator.rbac.aux.PermitSetAuxStack
import org.treeWare.model.operator.rbac.aux.getPermissionsAux
import org.treeWare.model.operator.set.aux.SET_AUX_NAME
import org.treeWare.model.operator.set.aux.SetAux
import org.treeWare.model.operator.set.aux.SetAuxStack
import org.treeWare.model.operator.set.aux.getSetAux
import org.treeWare.model.traversal.AbstractLeader1Follower1ModelVisitor
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.model.traversal.forEach
import org.treeWare.util.assertInDevMode

/** Return a subset of `set` that is permitted by `rbac` */
fun permitSet(set: MainModel, rbac: MainModel): MutableMainModel? {
    val visitor = PermitSetVisitor(rbac)
    forEach(set, rbac, visitor, false)
    return visitor.permittedMain
}

private class PermitSetVisitor(private val rbac: MainModel) : AbstractLeader1Follower1ModelVisitor<TraversalAction>(
    TraversalAction.CONTINUE
) {
    val permittedMain: MutableMainModel?
        get() = permittedMainInternal.takeIf {
            val root = runCatching { it.root }.getOrNull()
            root != null && !root.isEmpty()
        }

    private lateinit var permittedMainInternal: MutableMainModel
    private val permittedStack = ArrayDeque<MutableElementModel?>()
    private val setAuxStack = SetAuxStack()
    private val permitSetAuxStack = PermitSetAuxStack()

    override fun visitMain(leaderMain1: MainModel, followerMain1: MainModel?): TraversalAction {
        val setAuxError = setAuxStack.push(getSetAux(leaderMain1))
        assertInDevMode(setAuxError == null)
        permitSetAuxStack.push(getPermissionsAux(followerMain1))
        permittedMainInternal = MutableMainModel(leaderMain1.mainMeta)
        copySetAux(leaderMain1, permittedMainInternal)
        return if (permitSetAuxStack.isAnySetPermitted()) {
            permittedStack.addFirst(permittedMainInternal)
            TraversalAction.CONTINUE
        } else if (followerMain1 != null) {
            permittedStack.addFirst(permittedMainInternal)
            TraversalAction.CONTINUE
        } else {
            permittedStack.addFirst(null)
            TraversalAction.ABORT_SUB_TREE
        }
    }

    override fun leaveMain(leaderMain1: MainModel, followerMain1: MainModel?) {
        permittedStack.removeFirst()
        assertInDevMode(permittedStack.isEmpty())
        permitSetAuxStack.pop()
        setAuxStack.pop()
        assertInDevMode(setAuxStack.isEmpty())
    }

    override fun visitEntity(leaderEntity1: EntityModel, followerEntity1: EntityModel?): TraversalAction {
        val setAuxError = setAuxStack.push(getSetAux(leaderEntity1))
        assertInDevMode(setAuxError == null)
        permitSetAuxStack.push(getPermissionsAux(followerEntity1))
        // Permissions are not checked for an entity because the entity will be needed for any fields that might be
        // permitted in the sub-tree. On the way up, if there are no permitted fields in the sub-tree, the entity will
        // be removed.
        val permittedParent = permittedStack.first() ?: throw IllegalStateException("Entity parent is null")
        val permittedEntity = newChildValue(permittedParent)
        copySetAux(leaderEntity1, permittedEntity)
        permittedStack.addFirst(permittedEntity)
        return TraversalAction.CONTINUE
    }

    override fun leaveEntity(leaderEntity1: EntityModel, followerEntity1: EntityModel?) {
        val permittedEntity = permittedStack.removeFirst() as MutableEntityModel?
            ?: throw IllegalStateException("Entity parent is null")
        if (!isCompositionKey(permittedEntity) && permittedEntity.hasOnlyKeyFields()) {
            // Even if an entity is not permitted, it and its keys are added on the way down since its descendants
            // might be permitted. On the way up, the entity must be removed if it does not have any other fields.
            // It should be removed if the entity is to be updated, or if it does not have permission to be created
            // or deleted. The entity is indirectly removed by detaching all its fields.
            val setAux = setAuxStack.peekActive()
            if (setAux == SetAux.UPDATE || !permitSetAuxStack.isSetPermitted(setAux)) permittedEntity.detachAllFields()
        }
        val permittedParent = permittedStack.firstOrNull() ?: return
        if (permittedParent.elementType == ModelElementType.SET_FIELD) {
            // NOTE: entities should be added to set-fields only after the entity has key fields.
            // If the keys don't exist, an exception will be thrown. Catch and ignore it.
            runCatching { (permittedParent as MutableSetFieldModel).addValue(permittedEntity) }
        }
        permitSetAuxStack.pop()
        setAuxStack.pop()
    }

    override fun visitSingleField(leaderField1: SingleFieldModel, followerField1: SingleFieldModel?): TraversalAction =
        if (isCompositionField(leaderField1)) visitBranchField(leaderField1, followerField1)
        else visitLeafField(leaderField1, followerField1)

    override fun leaveSingleField(leaderField1: SingleFieldModel, followerField1: SingleFieldModel?) {
        // Drop composition field if its entity is empty.
        if (isCompositionField(leaderField1)) {
            val permittedSingleField = permittedStack.first() as? MutableSingleFieldModel
            val permittedSingleFieldValue = permittedSingleField?.value
            val permittedEntity = permittedSingleFieldValue as MutableEntityModel?
            if (permittedEntity == null || permittedEntity.isEmpty()) permittedSingleField?.detachFromParent()
            leaveBranchField()
        } else leaveLeafField()
    }

    override fun visitListField(leaderField1: ListFieldModel, followerField1: ListFieldModel?): TraversalAction =
        visitLeafField(leaderField1, followerField1)

    override fun leaveListField(leaderField1: ListFieldModel, followerField1: ListFieldModel?) {
        leaveLeafField()
    }

    override fun visitSetField(leaderField1: SetFieldModel, followerField1: SetFieldModel?): TraversalAction =
        visitBranchField(leaderField1, followerField1)

    override fun leaveSetField(leaderField1: SetFieldModel, followerField1: SetFieldModel?) {
        // Drop set field if it is empty.
        permittedStack.first()?.also {
            val permittedSetField = it as MutableSetFieldModel
            if (permittedSetField.isEmpty()) permittedSetField.detachFromParent()
        }
        leaveBranchField()
    }

    override fun visitPrimitive(leaderValue1: PrimitiveModel, followerValue1: PrimitiveModel?): TraversalAction {
        val permittedParent = permittedStack.first() ?: throw IllegalStateException("Primitive parent is null")
        val permittedPrimitive = newChildValue(permittedParent) as MutablePrimitiveModel
        permittedPrimitive.copyValueFrom(leaderValue1)
        return TraversalAction.CONTINUE
    }

    override fun visitAlias(leaderValue1: AliasModel, followerValue1: AliasModel?): TraversalAction {
        val permittedParent = permittedStack.first() ?: throw IllegalStateException("Alias parent is null")
        val permittedAlias = newChildValue(permittedParent) as MutableAliasModel
        permittedAlias.copyValueFrom(leaderValue1)
        return TraversalAction.CONTINUE
    }

    override fun visitPassword1way(
        leaderValue1: Password1wayModel,
        followerValue1: Password1wayModel?
    ): TraversalAction {
        val permittedParent = permittedStack.first() ?: throw IllegalStateException("Password1way parent is null")
        val permittedPassword = newChildValue(permittedParent) as MutablePassword1wayModel
        permittedPassword.copyValueFrom(leaderValue1)
        return TraversalAction.CONTINUE
    }

    override fun visitPassword2way(
        leaderValue1: Password2wayModel,
        followerValue1: Password2wayModel?
    ): TraversalAction {
        val permittedParent = permittedStack.first() ?: throw IllegalStateException("Password2way parent is null")
        val permittedPassword = newChildValue(permittedParent) as MutablePassword2wayModel
        permittedPassword.copyValueFrom(leaderValue1)
        return TraversalAction.CONTINUE
    }

    override fun visitEnumeration(leaderValue1: EnumerationModel, followerValue1: EnumerationModel?): TraversalAction {
        val permittedParent = permittedStack.first() ?: throw IllegalStateException("Enumeration parent is null")
        val permittedEnumeration = newChildValue(permittedParent) as MutableEnumerationModel
        permittedEnumeration.copyValueFrom(leaderValue1)
        return TraversalAction.CONTINUE
    }

    override fun visitAssociation(leaderValue1: AssociationModel, followerValue1: AssociationModel?): TraversalAction {
        val permittedParent = permittedStack.first() as MutableFieldModel?
            ?: throw IllegalStateException("Association parent is null")
        // Read permission is required for the association target in the case of CREATE and UPDATE.
        val setAux = setAuxStack.peekActive()
        if (setAux == SetAux.CREATE || setAux == SetAux.UPDATE) {
            // TODO(cleanup): drop valueAsMain & typecast by updating PermitSetVisitor to use root entities as starting points.
            val target = MutableMainModel(rbac.mainMeta)
            target.root = leaderValue1.value as MutableEntityModel
            val targetPermitted = permitGet(target, rbac)
            if (targetPermitted == null) {
                if (permittedParent.elementType == ModelElementType.SINGLE_FIELD) permittedParent.detachFromParent()
                return TraversalAction.CONTINUE
            }
        }
        val permittedAssociation = newChildValue(permittedParent)
        copy(leaderValue1, permittedAssociation)
        return TraversalAction.CONTINUE
    }

    // Helpers

    private fun visitBranchField(leaderField1: FieldModel, followerField1: FieldModel?): TraversalAction {
        val setAuxError = setAuxStack.push(getSetAux(leaderField1))
        assertInDevMode(setAuxError == null)

        permitSetAuxStack.push(getPermissionsAux(followerField1))
        if (!permitSetAuxStack.isAnySetPermitted() && followerField1 == null) {
            permittedStack.addFirst(null)
            return TraversalAction.ABORT_SUB_TREE
        }

        val permittedParent = permittedStack.first() as MutableBaseEntityModel
        val leaderFieldName = getFieldName(leaderField1)
        val permittedField = permittedParent.getOrNewField(leaderFieldName)
        copySetAux(leaderField1, permittedField)
        permittedStack.addFirst(permittedField)
        return TraversalAction.CONTINUE
    }

    private fun leaveBranchField() {
        permittedStack.removeFirst()
        permitSetAuxStack.pop()
        setAuxStack.pop()
    }

    private fun visitLeafField(leaderField1: FieldModel, followerField1: FieldModel?): TraversalAction {
        val setAuxError = setAuxStack.push(getSetAux(leaderField1))
        assertInDevMode(setAuxError == null)

        permitSetAuxStack.push(getPermissionsAux(followerField1))
        // Keys need to be added irrespective of whether they are permitted because some fields in the sub-tree might
        // be permitted, and they will need all their ancestor keys to be present. On the way up, if there are no such
        // fields in the sub-tree, the keys (and their containing entity) will be removed.
        val isWritePermitted = isKeyField(leaderField1) || permitSetAuxStack.isSetPermitted(setAuxStack.peekActive())
        if (!isWritePermitted) {
            permittedStack.addFirst(null)
            return TraversalAction.ABORT_SUB_TREE
        }

        val permittedParent = permittedStack.first() as MutableBaseEntityModel
        val leaderFieldName = getFieldName(leaderField1)
        val permittedField = permittedParent.getOrNewField(leaderFieldName)
        copySetAux(leaderField1, permittedField)
        permittedStack.addFirst(permittedField)
        return TraversalAction.CONTINUE
    }

    private fun leaveLeafField() {
        permittedStack.removeFirst()
        permitSetAuxStack.pop()
        setAuxStack.pop()
    }

    private fun copySetAux(leaderElement: ElementModel, permittedElement: MutableElementModel) {
        getSetAux(leaderElement)?.also { permittedElement.setAux(SET_AUX_NAME, it) }
    }
}