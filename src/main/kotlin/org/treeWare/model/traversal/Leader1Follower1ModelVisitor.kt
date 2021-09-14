package org.treeWare.model.traversal

import org.treeWare.model.core.*

interface Leader1Follower1ModelVisitor<LeaderAux, FollowerAux, Return> {
    // Abstract elements are not visited

    fun visit(leaderMain1: MainModel<LeaderAux>, followerModel1: MainModel<FollowerAux>?): Return
    fun leave(leaderMain1: MainModel<LeaderAux>, followerModel1: MainModel<FollowerAux>?)

    fun visit(leaderRoot1: RootModel<LeaderAux>, followerRoot1: RootModel<FollowerAux>?): Return
    fun leave(leaderRoot1: RootModel<LeaderAux>, followerRoot1: RootModel<FollowerAux>?)

    fun visit(leaderEntity1: EntityModel<LeaderAux>, followerEntity1: EntityModel<FollowerAux>?): Return
    fun leave(leaderEntity1: EntityModel<LeaderAux>, followerEntity1: EntityModel<FollowerAux>?)

    // Fields

    fun visit(leaderField1: SingleFieldModel<LeaderAux>, followerField1: SingleFieldModel<FollowerAux>?): Return
    fun leave(leaderField1: SingleFieldModel<LeaderAux>, followerField1: SingleFieldModel<FollowerAux>?)

    fun visit(leaderField1: ListFieldModel<LeaderAux>, followerField1: ListFieldModel<FollowerAux>?): Return
    fun leave(leaderField1: ListFieldModel<LeaderAux>, followerField1: ListFieldModel<FollowerAux>?)

    // Values

    fun visit(leaderValue1: PrimitiveModel<LeaderAux>, followerValue1: PrimitiveModel<FollowerAux>?): Return
    fun leave(leaderValue1: PrimitiveModel<LeaderAux>, followerValue1: PrimitiveModel<FollowerAux>?)

    fun visit(leaderValue1: AliasModel<LeaderAux>, followerValue1: AliasModel<FollowerAux>?): Return
    fun leave(leaderValue1: AliasModel<LeaderAux>, followerValue1: AliasModel<FollowerAux>?)

    fun visit(leaderValue1: Password1wayModel<LeaderAux>, followerValue1: Password1wayModel<FollowerAux>?): Return
    fun leave(leaderValue1: Password1wayModel<LeaderAux>, followerValue1: Password1wayModel<FollowerAux>?)

    fun visit(leaderValue1: Password2wayModel<LeaderAux>, followerValue1: Password2wayModel<FollowerAux>?): Return
    fun leave(leaderValue1: Password2wayModel<LeaderAux>, followerValue1: Password2wayModel<FollowerAux>?)

    fun visit(
        leaderValue1: EnumerationModel<LeaderAux>,
        followerValue1: EnumerationModel<FollowerAux>?
    ): Return

    fun leave(leaderValue1: EnumerationModel<LeaderAux>, followerValue1: EnumerationModel<FollowerAux>?)

    fun visit(
        leaderValue1: AssociationModel<LeaderAux>,
        followerValue1: AssociationModel<FollowerAux>?
    ): Return

    fun leave(leaderValue1: AssociationModel<LeaderAux>, followerValue1: AssociationModel<FollowerAux>?)

    // Sub-values

    fun visit(
        leaderEntityKeys1: EntityKeysModel<LeaderAux>,
        followerEntityKeys1: EntityKeysModel<FollowerAux>?
    ): Return

    fun leave(
        leaderEntityKeys1: EntityKeysModel<LeaderAux>,
        followerEntityKeys1: EntityKeysModel<FollowerAux>?
    )
}
