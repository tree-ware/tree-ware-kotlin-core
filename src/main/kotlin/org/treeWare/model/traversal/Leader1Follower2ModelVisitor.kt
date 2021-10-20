package org.treeWare.model.traversal

import org.treeWare.model.core.*

interface Leader1Follower2ModelVisitor<Return> {
    // Abstract elements are not visited

    suspend fun visit(
        leaderMain1: MainModel,
        followerMain1: MainModel?,
        followerMain2: MainModel?
    ): Return

    suspend fun leave(
        leaderMain1: MainModel,
        followerMain1: MainModel?,
        followerMain2: MainModel?
    )

    suspend fun visit(
        leaderRoot1: RootModel,
        followerRoot1: RootModel?,
        followerRoot2: RootModel?
    ): Return

    suspend fun leave(
        leaderRoot1: RootModel,
        followerRoot1: RootModel?,
        followerRoot2: RootModel?
    )

    suspend fun visit(
        leaderEntity1: EntityModel,
        followerEntity1: EntityModel?,
        followerEntity2: EntityModel?
    ): Return

    suspend fun leave(
        leaderEntity1: EntityModel,
        followerEntity1: EntityModel?,
        followerEntity2: EntityModel?
    )

    // Fields

    suspend fun visit(
        leaderField1: SingleFieldModel,
        followerField1: SingleFieldModel?,
        followerField2: SingleFieldModel?
    ): Return

    suspend fun leave(
        leaderField1: SingleFieldModel,
        followerField1: SingleFieldModel?,
        followerField2: SingleFieldModel?
    )

    suspend fun visit(
        leaderField1: ListFieldModel,
        followerField1: ListFieldModel?,
        followerField2: ListFieldModel?
    ): Return

    suspend fun leave(
        leaderField1: ListFieldModel,
        followerField1: ListFieldModel?,
        followerField2: ListFieldModel?
    )

    suspend fun visit(
        leaderField1: SetFieldModel,
        followerField1: SetFieldModel?,
        followerField2: SetFieldModel?
    ): Return

    suspend fun leave(
        leaderField1: SetFieldModel,
        followerField1: SetFieldModel?,
        followerField2: SetFieldModel?
    )

    // Values

    suspend fun visit(
        leaderValue1: PrimitiveModel,
        followerValue1: PrimitiveModel?,
        followerValue2: PrimitiveModel?
    ): Return

    suspend fun leave(
        leaderValue1: PrimitiveModel,
        followerValue1: PrimitiveModel?,
        followerValue2: PrimitiveModel?
    )

    suspend fun visit(
        leaderValue1: AliasModel,
        followerValue1: AliasModel?,
        followerValue2: AliasModel?
    ): Return

    suspend fun leave(
        leaderValue1: AliasModel,
        followerValue1: AliasModel?,
        followerValue2: AliasModel?
    )

    suspend fun visit(
        leaderValue1: Password1wayModel,
        followerValue1: Password1wayModel?,
        followerValue2: Password1wayModel?
    ): Return

    suspend fun leave(
        leaderValue1: Password1wayModel,
        followerValue1: Password1wayModel?,
        followerValue2: Password1wayModel?
    )

    suspend fun visit(
        leaderValue1: Password2wayModel,
        followerValue1: Password2wayModel?,
        followerValue2: Password2wayModel?
    ): Return

    suspend fun leave(
        leaderValue1: Password2wayModel,
        followerValue1: Password2wayModel?,
        followerValue2: Password2wayModel?
    )

    suspend fun visit(
        leaderValue1: EnumerationModel,
        followerValue1: EnumerationModel?,
        followerValue2: EnumerationModel?
    ): Return

    suspend fun leave(
        leaderValue1: EnumerationModel,
        followerValue1: EnumerationModel?,
        followerValue2: EnumerationModel?
    )

    suspend fun visit(
        leaderValue1: AssociationModel,
        followerValue1: AssociationModel?,
        followerValue2: AssociationModel?
    ): Return

    suspend fun leave(
        leaderValue1: AssociationModel,
        followerValue1: AssociationModel?,
        followerValue2: AssociationModel?
    )

    // Sub-values

    suspend fun visit(
        leaderEntityKeys1: EntityKeysModel,
        followerEntityKeys1: EntityKeysModel?,
        followerEntityKeys2: EntityKeysModel?
    ): Return

    suspend fun leave(
        leaderEntityKeys1: EntityKeysModel,
        followerEntityKeys1: EntityKeysModel?,
        followerEntityKeys2: EntityKeysModel?
    )
}
