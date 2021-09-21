package org.treeWare.model.traversal

import org.treeWare.model.core.*

interface Leader1Follower2ModelVisitor<LeaderAux, Follower1Aux, Follower2Aux, Return> {
    // Abstract elements are not visited

    suspend fun visit(
        leaderMain1: MainModel<LeaderAux>,
        followerMain1: MainModel<Follower1Aux>?,
        followerMain2: MainModel<Follower2Aux>?
    ): Return

    suspend fun leave(
        leaderMain1: MainModel<LeaderAux>,
        followerMain1: MainModel<Follower1Aux>?,
        followerMain2: MainModel<Follower2Aux>?
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

    // Fields

    suspend fun visit(
        leaderField1: SingleFieldModel<LeaderAux>,
        followerField1: SingleFieldModel<Follower1Aux>?,
        followerField2: SingleFieldModel<Follower2Aux>?
    ): Return

    suspend fun leave(
        leaderField1: SingleFieldModel<LeaderAux>,
        followerField1: SingleFieldModel<Follower1Aux>?,
        followerField2: SingleFieldModel<Follower2Aux>?
    )

    suspend fun visit(
        leaderField1: ListFieldModel<LeaderAux>,
        followerField1: ListFieldModel<Follower1Aux>?,
        followerField2: ListFieldModel<Follower2Aux>?
    ): Return

    suspend fun leave(
        leaderField1: ListFieldModel<LeaderAux>,
        followerField1: ListFieldModel<Follower1Aux>?,
        followerField2: ListFieldModel<Follower2Aux>?
    )

    suspend fun visit(
        leaderField1: SetFieldModel<LeaderAux>,
        followerField1: SetFieldModel<Follower1Aux>?,
        followerField2: SetFieldModel<Follower2Aux>?
    ): Return

    suspend fun leave(
        leaderField1: SetFieldModel<LeaderAux>,
        followerField1: SetFieldModel<Follower1Aux>?,
        followerField2: SetFieldModel<Follower2Aux>?
    )

    // Values

    suspend fun visit(
        leaderValue1: PrimitiveModel<LeaderAux>,
        followerValue1: PrimitiveModel<Follower1Aux>?,
        followerValue2: PrimitiveModel<Follower2Aux>?
    ): Return

    suspend fun leave(
        leaderValue1: PrimitiveModel<LeaderAux>,
        followerValue1: PrimitiveModel<Follower1Aux>?,
        followerValue2: PrimitiveModel<Follower2Aux>?
    )

    suspend fun visit(
        leaderValue1: AliasModel<LeaderAux>,
        followerValue1: AliasModel<Follower1Aux>?,
        followerValue2: AliasModel<Follower2Aux>?
    ): Return

    suspend fun leave(
        leaderValue1: AliasModel<LeaderAux>,
        followerValue1: AliasModel<Follower1Aux>?,
        followerValue2: AliasModel<Follower2Aux>?
    )

    suspend fun visit(
        leaderValue1: Password1wayModel<LeaderAux>,
        followerValue1: Password1wayModel<Follower1Aux>?,
        followerValue2: Password1wayModel<Follower2Aux>?
    ): Return

    suspend fun leave(
        leaderValue1: Password1wayModel<LeaderAux>,
        followerValue1: Password1wayModel<Follower1Aux>?,
        followerValue2: Password1wayModel<Follower2Aux>?
    )

    suspend fun visit(
        leaderValue1: Password2wayModel<LeaderAux>,
        followerValue1: Password2wayModel<Follower1Aux>?,
        followerValue2: Password2wayModel<Follower2Aux>?
    ): Return

    suspend fun leave(
        leaderValue1: Password2wayModel<LeaderAux>,
        followerValue1: Password2wayModel<Follower1Aux>?,
        followerValue2: Password2wayModel<Follower2Aux>?
    )

    suspend fun visit(
        leaderValue1: EnumerationModel<LeaderAux>,
        followerValue1: EnumerationModel<Follower1Aux>?,
        followerValue2: EnumerationModel<Follower2Aux>?
    ): Return

    suspend fun leave(
        leaderValue1: EnumerationModel<LeaderAux>,
        followerValue1: EnumerationModel<Follower1Aux>?,
        followerValue2: EnumerationModel<Follower2Aux>?
    )

    suspend fun visit(
        leaderValue1: AssociationModel<LeaderAux>,
        followerValue1: AssociationModel<Follower1Aux>?,
        followerValue2: AssociationModel<Follower2Aux>?
    ): Return

    suspend fun leave(
        leaderValue1: AssociationModel<LeaderAux>,
        followerValue1: AssociationModel<Follower1Aux>?,
        followerValue2: AssociationModel<Follower2Aux>?
    )

    // Sub-values

    suspend fun visit(
        leaderEntityKeys1: EntityKeysModel<LeaderAux>,
        followerEntityKeys1: EntityKeysModel<Follower1Aux>?,
        followerEntityKeys2: EntityKeysModel<Follower2Aux>?
    ): Return

    suspend fun leave(
        leaderEntityKeys1: EntityKeysModel<LeaderAux>,
        followerEntityKeys1: EntityKeysModel<Follower1Aux>?,
        followerEntityKeys2: EntityKeysModel<Follower2Aux>?
    )
}
