package org.treeWare.model.traversal

import org.treeWare.model.core.*

interface Leader1Follower1ModelVisitor<Return> {
    // Abstract elements are not visited

    fun visit(leaderMain1: MainModel, followerModel1: MainModel?): Return
    fun leave(leaderMain1: MainModel, followerModel1: MainModel?)

    fun visit(leaderRoot1: RootModel, followerRoot1: RootModel?): Return
    fun leave(leaderRoot1: RootModel, followerRoot1: RootModel?)

    fun visit(leaderEntity1: EntityModel, followerEntity1: EntityModel?): Return
    fun leave(leaderEntity1: EntityModel, followerEntity1: EntityModel?)

    // Fields

    fun visit(leaderField1: SingleFieldModel, followerField1: SingleFieldModel?): Return
    fun leave(leaderField1: SingleFieldModel, followerField1: SingleFieldModel?)

    fun visit(leaderField1: ListFieldModel, followerField1: ListFieldModel?): Return
    fun leave(leaderField1: ListFieldModel, followerField1: ListFieldModel?)

    fun visit(leaderField1: SetFieldModel, followerField1: SetFieldModel?): Return
    fun leave(leaderField1: SetFieldModel, followerField1: SetFieldModel?)

    // Values

    fun visit(leaderValue1: PrimitiveModel, followerValue1: PrimitiveModel?): Return
    fun leave(leaderValue1: PrimitiveModel, followerValue1: PrimitiveModel?)

    fun visit(leaderValue1: AliasModel, followerValue1: AliasModel?): Return
    fun leave(leaderValue1: AliasModel, followerValue1: AliasModel?)

    fun visit(leaderValue1: Password1wayModel, followerValue1: Password1wayModel?): Return
    fun leave(leaderValue1: Password1wayModel, followerValue1: Password1wayModel?)

    fun visit(leaderValue1: Password2wayModel, followerValue1: Password2wayModel?): Return
    fun leave(leaderValue1: Password2wayModel, followerValue1: Password2wayModel?)

    fun visit(
        leaderValue1: EnumerationModel,
        followerValue1: EnumerationModel?
    ): Return

    fun leave(leaderValue1: EnumerationModel, followerValue1: EnumerationModel?)

    fun visit(
        leaderValue1: AssociationModel,
        followerValue1: AssociationModel?
    ): Return

    fun leave(leaderValue1: AssociationModel, followerValue1: AssociationModel?)

    // Sub-values

    fun visit(
        leaderEntityKeys1: EntityKeysModel,
        followerEntityKeys1: EntityKeysModel?
    ): Return

    fun leave(
        leaderEntityKeys1: EntityKeysModel,
        followerEntityKeys1: EntityKeysModel?
    )
}
