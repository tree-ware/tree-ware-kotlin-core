package org.treeWare.model.traversal

import org.treeWare.model.core.*

interface Leader1Follower2ModelVisitor<Return> {
    // Abstract elements are not visited

    suspend fun visitMain(
        leaderMain1: MainModel,
        followerMain1: MainModel?,
        followerMain2: MainModel?
    ): Return

    suspend fun leaveMain(
        leaderMain1: MainModel,
        followerMain1: MainModel?,
        followerMain2: MainModel?
    )

    suspend fun visitRoot(
        leaderRoot1: RootModel,
        followerRoot1: RootModel?,
        followerRoot2: RootModel?
    ): Return

    suspend fun leaveRoot(
        leaderRoot1: RootModel,
        followerRoot1: RootModel?,
        followerRoot2: RootModel?
    )

    suspend fun visitEntity(
        leaderEntity1: EntityModel,
        followerEntity1: EntityModel?,
        followerEntity2: EntityModel?
    ): Return

    suspend fun leaveEntity(
        leaderEntity1: EntityModel,
        followerEntity1: EntityModel?,
        followerEntity2: EntityModel?
    )

    // Fields

    suspend fun visitSingleField(
        leaderField1: SingleFieldModel,
        followerField1: SingleFieldModel?,
        followerField2: SingleFieldModel?
    ): Return

    suspend fun leaveSingleField(
        leaderField1: SingleFieldModel,
        followerField1: SingleFieldModel?,
        followerField2: SingleFieldModel?
    )

    suspend fun visitListField(
        leaderField1: ListFieldModel,
        followerField1: ListFieldModel?,
        followerField2: ListFieldModel?
    ): Return

    suspend fun leaveListField(
        leaderField1: ListFieldModel,
        followerField1: ListFieldModel?,
        followerField2: ListFieldModel?
    )

    suspend fun visitSetField(
        leaderField1: SetFieldModel,
        followerField1: SetFieldModel?,
        followerField2: SetFieldModel?
    ): Return

    suspend fun leaveSetField(
        leaderField1: SetFieldModel,
        followerField1: SetFieldModel?,
        followerField2: SetFieldModel?
    )

    // Values

    suspend fun visitPrimitive(
        leaderValue1: PrimitiveModel,
        followerValue1: PrimitiveModel?,
        followerValue2: PrimitiveModel?
    ): Return

    suspend fun leavePrimitive(
        leaderValue1: PrimitiveModel,
        followerValue1: PrimitiveModel?,
        followerValue2: PrimitiveModel?
    )

    suspend fun visitAlias(
        leaderValue1: AliasModel,
        followerValue1: AliasModel?,
        followerValue2: AliasModel?
    ): Return

    suspend fun leaveAlias(
        leaderValue1: AliasModel,
        followerValue1: AliasModel?,
        followerValue2: AliasModel?
    )

    suspend fun visitPassword1way(
        leaderValue1: Password1wayModel,
        followerValue1: Password1wayModel?,
        followerValue2: Password1wayModel?
    ): Return

    suspend fun leavePassword1way(
        leaderValue1: Password1wayModel,
        followerValue1: Password1wayModel?,
        followerValue2: Password1wayModel?
    )

    suspend fun visitPassword2way(
        leaderValue1: Password2wayModel,
        followerValue1: Password2wayModel?,
        followerValue2: Password2wayModel?
    ): Return

    suspend fun leavePassword2way(
        leaderValue1: Password2wayModel,
        followerValue1: Password2wayModel?,
        followerValue2: Password2wayModel?
    )

    suspend fun visitEnumeration(
        leaderValue1: EnumerationModel,
        followerValue1: EnumerationModel?,
        followerValue2: EnumerationModel?
    ): Return

    suspend fun leaveEnumeration(
        leaderValue1: EnumerationModel,
        followerValue1: EnumerationModel?,
        followerValue2: EnumerationModel?
    )

    suspend fun visitAssociation(
        leaderValue1: AssociationModel,
        followerValue1: AssociationModel?,
        followerValue2: AssociationModel?
    ): Return

    suspend fun leaveAssociation(
        leaderValue1: AssociationModel,
        followerValue1: AssociationModel?,
        followerValue2: AssociationModel?
    )

    // Sub-values

    suspend fun visitEntityKeys(
        leaderEntityKeys1: EntityKeysModel,
        followerEntityKeys1: EntityKeysModel?,
        followerEntityKeys2: EntityKeysModel?
    ): Return

    suspend fun leaveEntityKeys(
        leaderEntityKeys1: EntityKeysModel,
        followerEntityKeys1: EntityKeysModel?,
        followerEntityKeys2: EntityKeysModel?
    )
}
