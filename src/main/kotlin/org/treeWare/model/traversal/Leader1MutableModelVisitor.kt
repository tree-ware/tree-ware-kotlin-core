package org.treeWare.model.traversal

import org.treeWare.model.core.*

interface Leader1MutableModelVisitor<Return> {
    // Abstract elements are not visited

    fun visit(leaderMain1: MutableMainModel): Return
    fun leave(leaderMain1: MutableMainModel)

    fun visit(leaderRoot1: MutableRootModel): Return
    fun leave(leaderRoot1: MutableRootModel)

    fun visit(leaderEntity1: MutableEntityModel): Return
    fun leave(leaderEntity1: MutableEntityModel)

    // Fields

    fun visit(leaderField1: MutableSingleFieldModel): Return
    fun leave(leaderField1: MutableSingleFieldModel)

    fun visit(leaderField1: MutableListFieldModel): Return
    fun leave(leaderField1: MutableListFieldModel)

    fun visit(leaderField1: MutableSetFieldModel): Return
    fun leave(leaderField1: MutableSetFieldModel)

    // Values

    fun visit(leaderValue1: MutablePrimitiveModel): Return
    fun leave(leaderValue1: MutablePrimitiveModel)

    fun visit(leaderValue1: MutableAliasModel): Return
    fun leave(leaderValue1: MutableAliasModel)

    fun visit(leaderValue1: MutablePassword1wayModel): Return
    fun leave(leaderValue1: MutablePassword1wayModel)

    fun visit(leaderValue1: MutablePassword2wayModel): Return
    fun leave(leaderValue1: MutablePassword2wayModel)

    fun visit(leaderValue1: MutableEnumerationModel): Return
    fun leave(leaderValue1: MutableEnumerationModel)

    fun visit(leaderValue1: MutableAssociationModel): Return
    fun leave(leaderValue1: MutableAssociationModel)

    // Sub-values

    fun visit(leaderEntityKeys1: MutableEntityKeysModel): Return
    fun leave(leaderEntityKeys1: MutableEntityKeysModel)
}
