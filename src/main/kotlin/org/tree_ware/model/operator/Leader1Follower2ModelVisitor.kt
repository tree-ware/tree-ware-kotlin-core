package org.tree_ware.model.operator

import org.tree_ware.model.core.*

interface Leader1Follower2ModelVisitor<LeaderAux, Follower1Aux, Follower2Aux, Return> {
    // Abstract elements are not visited

    suspend fun visit(
        leaderModel1: Model<LeaderAux>,
        followerModel1: Model<Follower1Aux>?,
        followerModel2: Model<Follower2Aux>?
    ): Return

    suspend fun leave(
        leaderModel1: Model<LeaderAux>,
        followerModel1: Model<Follower1Aux>?,
        followerModel2: Model<Follower2Aux>?
    )

    suspend fun visit(
        leaderRoot1: RootModel<LeaderAux>,
        followerRoot1: RootModel<Follower1Aux>?,
        followerRoot2: RootModel<Follower2Aux>?
    ): Return

    suspend fun leave(
        leaderRoot1: RootModel<LeaderAux>,
        followerRoot1: RootModel<Follower1Aux>?,
        followerRoot2: RootModel<Follower2Aux>?
    )

    suspend fun visit(
        leaderEntity1: EntityModel<LeaderAux>,
        followerEntity1: EntityModel<Follower1Aux>?,
        followerEntity2: EntityModel<Follower2Aux>?
    ): Return

    suspend fun leave(
        leaderEntity1: EntityModel<LeaderAux>,
        followerEntity1: EntityModel<Follower1Aux>?,
        followerEntity2: EntityModel<Follower2Aux>?
    )

    // Scalar fields

    suspend fun visit(
        leaderField1: PrimitiveFieldModel<LeaderAux>,
        followerField1: PrimitiveFieldModel<Follower1Aux>?,
        followerField2: PrimitiveFieldModel<Follower2Aux>?
    ): Return

    suspend fun leave(
        leaderField1: PrimitiveFieldModel<LeaderAux>,
        followerField1: PrimitiveFieldModel<Follower1Aux>?,
        followerField2: PrimitiveFieldModel<Follower2Aux>?
    )

    suspend fun visit(
        leaderField1: AliasFieldModel<LeaderAux>,
        followerField1: AliasFieldModel<Follower1Aux>?,
        followerField2: AliasFieldModel<Follower2Aux>?
    ): Return

    suspend fun leave(
        leaderField1: AliasFieldModel<LeaderAux>,
        followerField1: AliasFieldModel<Follower1Aux>?,
        followerField2: AliasFieldModel<Follower2Aux>?
    )

    suspend fun visit(
        leaderField1: EnumerationFieldModel<LeaderAux>,
        followerField1: EnumerationFieldModel<Follower1Aux>?,
        followerField2: EnumerationFieldModel<Follower2Aux>?
    ): Return

    suspend fun leave(
        leaderField1: EnumerationFieldModel<LeaderAux>,
        followerField1: EnumerationFieldModel<Follower1Aux>?,
        followerField2: EnumerationFieldModel<Follower2Aux>?
    )

    suspend fun visit(
        leaderField1: AssociationFieldModel<LeaderAux>,
        followerField1: AssociationFieldModel<Follower1Aux>?,
        followerField2: AssociationFieldModel<Follower2Aux>?
    ): Return

    suspend fun leave(
        leaderField1: AssociationFieldModel<LeaderAux>,
        followerField1: AssociationFieldModel<Follower1Aux>?,
        followerField2: AssociationFieldModel<Follower2Aux>?
    )

    suspend fun visit(
        leaderField1: CompositionFieldModel<LeaderAux>,
        followerField1: CompositionFieldModel<Follower1Aux>?,
        followerField2: CompositionFieldModel<Follower2Aux>?
    ): Return

    suspend fun leave(
        leaderField1: CompositionFieldModel<LeaderAux>,
        followerField1: CompositionFieldModel<Follower1Aux>?,
        followerField2: CompositionFieldModel<Follower2Aux>?
    )

    // List fields

    suspend fun visit(
        leaderField1: PrimitiveListFieldModel<LeaderAux>,
        followerField1: PrimitiveListFieldModel<Follower1Aux>?,
        followerField2: PrimitiveListFieldModel<Follower2Aux>?
    ): Return

    suspend fun leave(
        leaderField1: PrimitiveListFieldModel<LeaderAux>,
        followerField1: PrimitiveListFieldModel<Follower1Aux>?,
        followerField2: PrimitiveListFieldModel<Follower2Aux>?
    )

    suspend fun visit(
        leaderField1: AliasListFieldModel<LeaderAux>,
        followerField1: AliasListFieldModel<Follower1Aux>?,
        followerField2: AliasListFieldModel<Follower2Aux>?
    ): Return

    suspend fun leave(
        leaderField1: AliasListFieldModel<LeaderAux>,
        followerField1: AliasListFieldModel<Follower1Aux>?,
        followerField2: AliasListFieldModel<Follower2Aux>?
    )

    suspend fun visit(
        leaderField1: EnumerationListFieldModel<LeaderAux>,
        followerField1: EnumerationListFieldModel<Follower1Aux>?,
        followerField2: EnumerationListFieldModel<Follower2Aux>?
    ): Return

    suspend fun leave(
        leaderField1: EnumerationListFieldModel<LeaderAux>,
        followerField1: EnumerationListFieldModel<Follower1Aux>?,
        followerField2: EnumerationListFieldModel<Follower2Aux>?
    )

    suspend fun visit(
        leaderField1: AssociationListFieldModel<LeaderAux>,
        followerField1: AssociationListFieldModel<Follower1Aux>?,
        followerField2: AssociationListFieldModel<Follower2Aux>?
    ): Return

    suspend fun leave(
        leaderField1: AssociationListFieldModel<LeaderAux>,
        followerField1: AssociationListFieldModel<Follower1Aux>?,
        followerField2: AssociationListFieldModel<Follower2Aux>?
    )

    suspend fun visit(
        leaderField1: CompositionListFieldModel<LeaderAux>,
        followerField1: CompositionListFieldModel<Follower1Aux>?,
        followerField2: CompositionListFieldModel<Follower2Aux>?
    ): Return

    suspend fun leave(
        leaderField1: CompositionListFieldModel<LeaderAux>,
        followerField1: CompositionListFieldModel<Follower1Aux>?,
        followerField2: CompositionListFieldModel<Follower2Aux>?
    )
}
