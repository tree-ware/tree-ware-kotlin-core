package org.treeWare.model.operator

import org.treeWare.model.core.*

interface Leader1Follower1ModelVisitor<LeaderAux, FollowerAux, Return> {
    // Abstract elements are not visited

    fun visit(leaderModel1: Model<LeaderAux>, followerModel1: Model<FollowerAux>?): Return
    fun leave(leaderModel1: Model<LeaderAux>, followerModel1: Model<FollowerAux>?)

    fun visit(leaderRoot1: RootModel<LeaderAux>, followerRoot1: RootModel<FollowerAux>?): Return
    fun leave(leaderRoot1: RootModel<LeaderAux>, followerRoot1: RootModel<FollowerAux>?)

    fun visit(leaderEntity1: EntityModel<LeaderAux>, followerEntity1: EntityModel<FollowerAux>?): Return
    fun leave(leaderEntity1: EntityModel<LeaderAux>, followerEntity1: EntityModel<FollowerAux>?)

    // Scalar fields

    fun visit(leaderField1: PrimitiveFieldModel<LeaderAux>, followerField1: PrimitiveFieldModel<FollowerAux>?): Return
    fun leave(leaderField1: PrimitiveFieldModel<LeaderAux>, followerField1: PrimitiveFieldModel<FollowerAux>?)

    fun visit(leaderField1: AliasFieldModel<LeaderAux>, followerField1: AliasFieldModel<FollowerAux>?): Return
    fun leave(leaderField1: AliasFieldModel<LeaderAux>, followerField1: AliasFieldModel<FollowerAux>?)

    fun visit(
        leaderField1: EnumerationFieldModel<LeaderAux>,
        followerField1: EnumerationFieldModel<FollowerAux>?
    ): Return

    fun leave(leaderField1: EnumerationFieldModel<LeaderAux>, followerField1: EnumerationFieldModel<FollowerAux>?)

    fun visit(
        leaderField1: AssociationFieldModel<LeaderAux>,
        followerField1: AssociationFieldModel<FollowerAux>?
    ): Return

    fun leave(leaderField1: AssociationFieldModel<LeaderAux>, followerField1: AssociationFieldModel<FollowerAux>?)

    fun visit(
        leaderField1: CompositionFieldModel<LeaderAux>,
        followerField1: CompositionFieldModel<FollowerAux>?
    ): Return

    fun leave(leaderField1: CompositionFieldModel<LeaderAux>, followerField1: CompositionFieldModel<FollowerAux>?)

    // List fields

    fun visit(
        leaderField1: PrimitiveListFieldModel<LeaderAux>,
        followerField1: PrimitiveListFieldModel<FollowerAux>?
    ): Return

    fun leave(leaderField1: PrimitiveListFieldModel<LeaderAux>, followerField1: PrimitiveListFieldModel<FollowerAux>?)

    fun visit(leaderField1: AliasListFieldModel<LeaderAux>, followerField1: AliasListFieldModel<FollowerAux>?): Return
    fun leave(leaderField1: AliasListFieldModel<LeaderAux>, followerField1: AliasListFieldModel<FollowerAux>?)

    fun visit(
        leaderField1: EnumerationListFieldModel<LeaderAux>,
        followerField1: EnumerationListFieldModel<FollowerAux>?
    ): Return

    fun leave(
        leaderField1: EnumerationListFieldModel<LeaderAux>,
        followerField1: EnumerationListFieldModel<FollowerAux>?
    )

    fun visit(
        leaderField1: AssociationListFieldModel<LeaderAux>,
        followerField1: AssociationListFieldModel<FollowerAux>?
    ): Return

    fun leave(
        leaderField1: AssociationListFieldModel<LeaderAux>,
        followerField1: AssociationListFieldModel<FollowerAux>?
    )

    fun visit(
        leaderField1: CompositionListFieldModel<LeaderAux>,
        followerField1: CompositionListFieldModel<FollowerAux>?
    ): Return

    fun leave(
        leaderField1: CompositionListFieldModel<LeaderAux>,
        followerField1: CompositionListFieldModel<FollowerAux>?
    )
}
