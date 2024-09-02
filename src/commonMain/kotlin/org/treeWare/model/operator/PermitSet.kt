package org.treeWare.model.operator

import org.treeWare.metaModel.hasKeyFields
import org.treeWare.model.core.*
import org.treeWare.model.operator.rbac.FullyPermitted
import org.treeWare.model.operator.rbac.NotPermitted
import org.treeWare.model.operator.rbac.PartiallyPermitted
import org.treeWare.model.operator.rbac.PermitResponse
import org.treeWare.model.operator.rbac.aux.PermitSetAuxStack
import org.treeWare.model.operator.rbac.aux.getPermissionsAux
import org.treeWare.model.operator.set.aux.*
import org.treeWare.model.traversal.AbstractLeader1Follower1ModelVisitor
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.model.traversal.forEach
import org.treeWare.util.assertInDevMode

/** Return a subset of `set` that is permitted by `rbac` */
fun permitSet(
    set: EntityModel,
    rbac: EntityModel,
    mutableEntityModelFactory: MutableEntityModelFactory
): PermitResponse {
    val visitor = PermitSetVisitor(rbac, mutableEntityModelFactory)
    forEach(set, rbac, visitor, false)
    return visitor.permitResponse
}

private class PermitSetVisitor(
    private val rbac: EntityModel,
    private val mutableEntityModelFactory: MutableEntityModelFactory
) : AbstractLeader1Follower1ModelVisitor<TraversalAction>(TraversalAction.CONTINUE) {
    val permitResponse: PermitResponse
        get() {
            val permitted = permittedRoot ?: return NotPermitted
            return if (permitted.isEmpty()) NotPermitted
            else if (partiallyDenied) PartiallyPermitted(permitted)
            else FullyPermitted(permitted)
        }

    private var permittedRoot: MutableEntityModel? = null

    private val permittedStack = ArrayDeque<MutableElementModel?>()
    private val setAuxStack = SetAuxStack()
    private val permitSetAuxStack = PermitSetAuxStack()
    private var partiallyDenied = false

    override fun visitEntity(leaderEntity1: EntityModel, followerEntity1: EntityModel?): TraversalAction {
        val setAuxError = setAuxStack.push(getSetAux(leaderEntity1))
        assertInDevMode(setAuxError == null)
        permitSetAuxStack.push(getPermissionsAux(followerEntity1))
        // Permissions are not checked for an entity because the entity will be needed for any fields that might be
        // permitted in the sub-tree. On the way up, if there are no permitted fields in the sub-tree, the entity will
        // be removed.
        val permittedParent = permittedStack.firstOrNull()
        val permittedEntity =
            if (permittedParent == null) mutableEntityModelFactory.create().also { permittedRoot = it }
            else permittedParent.getOrNewValue()
        copySetAux(leaderEntity1, permittedEntity)
        permittedStack.addFirst(permittedEntity)
        return TraversalAction.CONTINUE
    }

    override fun leaveEntity(leaderEntity1: EntityModel, followerEntity1: EntityModel?) {
        val permittedEntity = permittedStack.removeFirst() as MutableEntityModel?
            ?: throw IllegalStateException("Entity parent is null")
        val permittedParent = permittedStack.firstOrNull() ?: return
        if (!isCompositionKey(permittedEntity) && permittedEntity.hasOnlyKeyFields()) {
            // Even if an entity is not permitted, it and its keys are added on the way down since its descendants
            // might be permitted. On the way up, the entity must be removed if it does not have any other fields.
            // It should be removed if the entity is to be updated, or if it does not have permission to be created
            // or deleted. The entity is indirectly removed by detaching all its fields and unsetting its setAux.
            val setAux = setAuxStack.peekActive()
            if (setAux == SetAux.UPDATE || !permitSetAuxStack.isSetPermitted(setAux)) {
                permittedEntity.detachAllFields()
                unsetSetAux(permittedEntity)
                if (permittedParent.elementType == ModelElementType.SINGLE_FIELD) unsetSetAux(permittedParent)
            }
        }
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
        // Drop composition field if its entity is empty, unless it is a keyless entity being deleted.
        if (isCompositionField(leaderField1)) {
            val permittedSingleField = permittedStack.first() as? MutableSingleFieldModel
            val permittedSingleFieldValue = permittedSingleField?.value
            val permittedEntity = permittedSingleFieldValue as MutableEntityModel?
            if (permittedEntity == null) permittedSingleField?.detachFromParent()
            else if (permittedEntity.isEmpty()) {
                val hasKeyFields = permittedEntity.meta?.let { hasKeyFields(it) } ?: false
                if (hasKeyFields || getSetAux(permittedEntity) != SetAux.DELETE) permittedSingleField?.detachFromParent()
            }
            leaveBranchField()
        } else leaveLeafField()
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
        val permittedPrimitive = permittedParent.getOrNewValue() as MutablePrimitiveModel
        permittedPrimitive.copyValueFrom(leaderValue1)
        return TraversalAction.CONTINUE
    }

    override fun visitAlias(leaderValue1: AliasModel, followerValue1: AliasModel?): TraversalAction {
        val permittedParent = permittedStack.first() ?: throw IllegalStateException("Alias parent is null")
        val permittedAlias = permittedParent.getOrNewValue() as MutableAliasModel
        permittedAlias.copyValueFrom(leaderValue1)
        return TraversalAction.CONTINUE
    }

    override fun visitPassword1way(
        leaderValue1: Password1wayModel,
        followerValue1: Password1wayModel?
    ): TraversalAction {
        val permittedParent = permittedStack.first() ?: throw IllegalStateException("Password1way parent is null")
        val permittedPassword = permittedParent.getOrNewValue() as MutablePassword1wayModel
        permittedPassword.copyValueFrom(leaderValue1)
        return TraversalAction.CONTINUE
    }

    override fun visitPassword2way(
        leaderValue1: Password2wayModel,
        followerValue1: Password2wayModel?
    ): TraversalAction {
        val permittedParent = permittedStack.first() ?: throw IllegalStateException("Password2way parent is null")
        val permittedPassword = permittedParent.getOrNewValue() as MutablePassword2wayModel
        permittedPassword.copyValueFrom(leaderValue1)
        return TraversalAction.CONTINUE
    }

    override fun visitEnumeration(leaderValue1: EnumerationModel, followerValue1: EnumerationModel?): TraversalAction {
        val permittedParent = permittedStack.first() ?: throw IllegalStateException("Enumeration parent is null")
        val permittedEnumeration = permittedParent.getOrNewValue() as MutableEnumerationModel
        permittedEnumeration.copyValueFrom(leaderValue1)
        return TraversalAction.CONTINUE
    }

    override fun visitAssociation(leaderValue1: AssociationModel, followerValue1: AssociationModel?): TraversalAction {
        val permittedParent = permittedStack.first() as MutableFieldModel?
            ?: throw IllegalStateException("Association parent is null")
        // Read permission is required for the association target in the case of CREATE and UPDATE.
        val setAux = setAuxStack.peekActive()
        if (setAux == SetAux.CREATE || setAux == SetAux.UPDATE) {
            val target = leaderValue1.value
            val targetPermitted = permitGet(target, rbac, mutableEntityModelFactory)
            if (targetPermitted !is FullyPermitted) {
                partiallyDenied = true
                if (permittedParent.elementType == ModelElementType.SINGLE_FIELD) permittedParent.detachFromParent()
                return TraversalAction.CONTINUE
            }
        }
        val permittedAssociation = permittedParent.getOrNewValue()
        copy(leaderValue1, permittedAssociation)
        return TraversalAction.CONTINUE
    }

    // Helpers

    private fun visitBranchField(leaderField1: FieldModel, followerField1: FieldModel?): TraversalAction {
        val setAuxError = setAuxStack.push(getSetAux(leaderField1))
        assertInDevMode(setAuxError == null)

        permitSetAuxStack.push(getPermissionsAux(followerField1))
        if (!permitSetAuxStack.isAnySetPermitted() && followerField1 == null) {
            partiallyDenied = true
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
            partiallyDenied = true
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