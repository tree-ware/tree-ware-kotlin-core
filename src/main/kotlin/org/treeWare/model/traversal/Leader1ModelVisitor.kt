package org.treeWare.model.traversal

import org.treeWare.model.core.*

interface Leader1ModelVisitor<Return> {
    // Abstract elements are not visited

    fun visit(leaderMain1: MainModel): Return
    fun leave(leaderMain1: MainModel)

    fun visit(leaderRoot1: RootModel): Return
    fun leave(leaderRoot1: RootModel)

    fun visit(leaderEntity1: EntityModel): Return
    fun leave(leaderEntity1: EntityModel)

    // Fields

    fun visit(leaderField1: SingleFieldModel): Return
    fun leave(leaderField1: SingleFieldModel)

    fun visit(leaderField1: ListFieldModel): Return
    fun leave(leaderField1: ListFieldModel)

    fun visit(leaderField1: SetFieldModel): Return
    fun leave(leaderField1: SetFieldModel)

    // Values

    fun visit(leaderValue1: PrimitiveModel): Return
    fun leave(leaderValue1: PrimitiveModel)

    fun visit(leaderValue1: AliasModel): Return
    fun leave(leaderValue1: AliasModel)

    fun visit(leaderValue1: Password1wayModel): Return
    fun leave(leaderValue1: Password1wayModel)

    fun visit(leaderValue1: Password2wayModel): Return
    fun leave(leaderValue1: Password2wayModel)

    fun visit(leaderValue1: EnumerationModel): Return
    fun leave(leaderValue1: EnumerationModel)

    fun visit(leaderValue1: AssociationModel): Return
    fun leave(leaderValue1: AssociationModel)

    // Sub-values

    fun visit(leaderEntityKeys1: EntityKeysModel): Return
    fun leave(leaderEntityKeys1: EntityKeysModel)
}
