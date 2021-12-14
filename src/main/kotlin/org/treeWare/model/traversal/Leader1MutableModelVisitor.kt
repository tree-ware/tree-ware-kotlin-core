package org.treeWare.model.traversal

import org.treeWare.model.core.*

interface Leader1MutableModelVisitor<Return> {
    // Abstract elements are not visited

    fun visitMutableMain(leaderMain1: MutableMainModel): Return
    fun leaveMutableMain(leaderMain1: MutableMainModel)

    fun visitMutableRoot(leaderRoot1: MutableRootModel): Return
    fun leaveMutableRoot(leaderRoot1: MutableRootModel)

    fun visitMutableEntity(leaderEntity1: MutableEntityModel): Return
    fun leaveMutableEntity(leaderEntity1: MutableEntityModel)

    // Fields

    fun visitMutableSingleField(leaderField1: MutableSingleFieldModel): Return
    fun leaveMutableSingleField(leaderField1: MutableSingleFieldModel)

    fun visitMutableListField(leaderField1: MutableListFieldModel): Return
    fun leaveMutableListField(leaderField1: MutableListFieldModel)

    fun visitMutableSetField(leaderField1: MutableSetFieldModel): Return
    fun leaveMutableSetField(leaderField1: MutableSetFieldModel)

    // Values

    fun visitMutablePrimitive(leaderValue1: MutablePrimitiveModel): Return
    fun leaveMutablePrimitive(leaderValue1: MutablePrimitiveModel)

    fun visitMutableAlias(leaderValue1: MutableAliasModel): Return
    fun leaveMutableAlias(leaderValue1: MutableAliasModel)

    fun visitMutablePassword1way(leaderValue1: MutablePassword1wayModel): Return
    fun leaveMutablePassword1way(leaderValue1: MutablePassword1wayModel)

    fun visitMutablePassword2way(leaderValue1: MutablePassword2wayModel): Return
    fun leaveMutablePassword2way(leaderValue1: MutablePassword2wayModel)

    fun visitMutableEnumeration(leaderValue1: MutableEnumerationModel): Return
    fun leaveMutableEnumeration(leaderValue1: MutableEnumerationModel)

    fun visitMutableAssociation(leaderValue1: MutableAssociationModel): Return
    fun leaveMutableAssociation(leaderValue1: MutableAssociationModel)

    // Sub-values

    fun visitMutableEntityKeys(leaderEntityKeys1: MutableEntityKeysModel): Return
    fun leaveMutableEntityKeys(leaderEntityKeys1: MutableEntityKeysModel)
}