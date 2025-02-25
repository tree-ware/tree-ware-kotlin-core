package org.treeWare.model.operator

import org.treeWare.model.core.*
import org.treeWare.model.operator.rbac.FullyPermitted
import org.treeWare.model.operator.rbac.NotPermitted
import org.treeWare.model.operator.rbac.PartiallyPermitted
import org.treeWare.model.operator.rbac.PermitResponse
import org.treeWare.model.operator.rbac.aux.PermitGetAuxStack
import org.treeWare.model.operator.rbac.aux.getPermissionsAux
import org.treeWare.model.traversal.AbstractLeader1Follower1ModelVisitor
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.model.traversal.forEach

/**
 *  Return a subset of `get` that is permitted by `rbac`.
 *  NOTE: wildcards in `get` do not match explicit keys in `rbac`.
 */
fun permitGet(
    get: EntityModel,
    rbac: EntityModel,
    rootEntityFactory: EntityFactory
): PermitResponse {
    val visitor = PermitGetVisitor(rootEntityFactory)
    forEach(get, rbac, visitor, false)
    return visitor.permitResponse
}

private class PermitGetVisitor(
    private val rootEntityFactory: EntityFactory
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
    private val permitGetAuxStack = PermitGetAuxStack()
    private var partiallyDenied = false

    override fun visitEntity(leaderEntity1: EntityModel, followerEntity1: EntityModel?): TraversalAction {
        permitGetAuxStack.push(getPermissionsAux(followerEntity1))
        // Permissions are not checked for an entity because the entity will be needed for any fields that might be
        // permitted in the sub-tree. On the way up, if there are no permitted fields in the sub-tree, the entity will
        // be removed.
        val permittedParent = permittedStack.firstOrNull()
        val permittedEntity = permittedParent?.getOrNewValue() ?: rootEntityFactory(null).also { permittedRoot = it }
        permittedStack.addFirst(permittedEntity)
        return TraversalAction.CONTINUE
    }

    override fun leaveEntity(leaderEntity1: EntityModel, followerEntity1: EntityModel?) {
        val permittedEntity = permittedStack.removeFirst() as MutableEntityModel?
            ?: throw IllegalStateException("Entity is null")
        if (!isCompositionKey(permittedEntity) && permittedEntity.hasOnlyKeyFields()) {
            // Even if an entity is not permitted, it and its keys are added on the way down since its descendants
            // might be permitted. On the way up, the entity must be removed if it does not have any other fields.
            // The entity is indirectly removed by detaching all its fields.
            if (!permitGetAuxStack.isGetPermitted()) permittedEntity.detachAllFields()
        }
        val permittedParent = permittedStack.firstOrNull() ?: return
        if (permittedParent.elementType == ModelElementType.SET_FIELD) {
            // NOTE: entities should be added to set-fields only after the entity has key fields.
            // If the keys don't exist, an exception will be thrown. Catch and ignore it.
            runCatching { (permittedParent as MutableSetFieldModel).addValue(permittedEntity) }
        }
        permitGetAuxStack.pop()
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
        val permittedParent = permittedStack.first() ?: throw IllegalStateException("Association parent is null")
        val permittedAssociation = permittedParent.getOrNewValue()
        copy(leaderValue1, permittedAssociation)
        return TraversalAction.CONTINUE
    }

    // Helpers

    private fun visitBranchField(leaderField1: FieldModel, followerField1: FieldModel?): TraversalAction {
        permitGetAuxStack.push(getPermissionsAux(followerField1))
        if (!permitGetAuxStack.isGetPermitted() && followerField1 == null) {
            partiallyDenied = true
            permittedStack.addFirst(null)
            return TraversalAction.ABORT_SUB_TREE
        }
        val permittedParent = permittedStack.first() as MutableEntityModel
        val leaderFieldName = getFieldName(leaderField1)
        val permittedField = permittedParent.getOrNewField(leaderFieldName)
        permittedStack.addFirst(permittedField)
        return TraversalAction.CONTINUE
    }

    private fun leaveBranchField() {
        permittedStack.removeFirst()
        permitGetAuxStack.pop()
    }

    private fun visitLeafField(leaderField1: FieldModel, followerField1: FieldModel?): TraversalAction {
        permitGetAuxStack.push(getPermissionsAux(followerField1))
        // Keys need to be added irrespective of whether they are permitted because some fields in the sub-tree might
        // be permitted, and they will need all their ancestor keys to be present. On the way up, if there are no such
        // fields in the sub-tree, the keys (and their containing entity) will be removed.
        val isReadPermitted = isKeyField(leaderField1) || permitGetAuxStack.isGetPermitted()
        if (!isReadPermitted) {
            partiallyDenied = true
            permittedStack.addFirst(null)
            return TraversalAction.ABORT_SUB_TREE
        }
        val permittedParent = permittedStack.first() as MutableEntityModel
        val leaderFieldName = getFieldName(leaderField1)
        val permittedField = permittedParent.getOrNewField(leaderFieldName)
        permittedStack.addFirst(permittedField)
        return TraversalAction.CONTINUE
    }

    private fun leaveLeafField() {
        permittedStack.removeFirst()
        permitGetAuxStack.pop()
    }
}