package org.treeWare.model.traversal

import org.treeWare.model.core.*

interface Leader1Follower2ModelVisitor<Return> {
    // Abstract elements are not visited

    fun visitMain(
        leaderMain1: MainModel,
        followerMain1: MainModel?,
        followerMain2: MainModel?
    ): Return

    fun leaveMain(
        leaderMain1: MainModel,
        followerMain1: MainModel?,
        followerMain2: MainModel?
    )

    fun visitEntity(
        leaderEntity1: EntityModel,
        followerEntity1: EntityModel?,
        followerEntity2: EntityModel?
    ): Return

    fun leaveEntity(
        leaderEntity1: EntityModel,
        followerEntity1: EntityModel?,
        followerEntity2: EntityModel?
    )

    // Fields

    fun visitSingleField(
        leaderField1: SingleFieldModel,
        followerField1: SingleFieldModel?,
        followerField2: SingleFieldModel?
    ): Return

    fun leaveSingleField(
        leaderField1: SingleFieldModel,
        followerField1: SingleFieldModel?,
        followerField2: SingleFieldModel?
    )

    fun visitListField(
        leaderField1: ListFieldModel,
        followerField1: ListFieldModel?,
        followerField2: ListFieldModel?
    ): Return

    fun leaveListField(
        leaderField1: ListFieldModel,
        followerField1: ListFieldModel?,
        followerField2: ListFieldModel?
    )

    fun visitSetField(
        leaderField1: SetFieldModel,
        followerField1: SetFieldModel?,
        followerField2: SetFieldModel?
    ): Return

    fun leaveSetField(
        leaderField1: SetFieldModel,
        followerField1: SetFieldModel?,
        followerField2: SetFieldModel?
    )

    // Values

    fun visitPrimitive(
        leaderValue1: PrimitiveModel,
        followerValue1: PrimitiveModel?,
        followerValue2: PrimitiveModel?
    ): Return

    fun leavePrimitive(
        leaderValue1: PrimitiveModel,
        followerValue1: PrimitiveModel?,
        followerValue2: PrimitiveModel?
    )

    fun visitAlias(
        leaderValue1: AliasModel,
        followerValue1: AliasModel?,
        followerValue2: AliasModel?
    ): Return

    fun leaveAlias(
        leaderValue1: AliasModel,
        followerValue1: AliasModel?,
        followerValue2: AliasModel?
    )

    fun visitPassword1way(
        leaderValue1: Password1wayModel,
        followerValue1: Password1wayModel?,
        followerValue2: Password1wayModel?
    ): Return

    fun leavePassword1way(
        leaderValue1: Password1wayModel,
        followerValue1: Password1wayModel?,
        followerValue2: Password1wayModel?
    )

    fun visitPassword2way(
        leaderValue1: Password2wayModel,
        followerValue1: Password2wayModel?,
        followerValue2: Password2wayModel?
    ): Return

    fun leavePassword2way(
        leaderValue1: Password2wayModel,
        followerValue1: Password2wayModel?,
        followerValue2: Password2wayModel?
    )

    fun visitEnumeration(
        leaderValue1: EnumerationModel,
        followerValue1: EnumerationModel?,
        followerValue2: EnumerationModel?
    ): Return

    fun leaveEnumeration(
        leaderValue1: EnumerationModel,
        followerValue1: EnumerationModel?,
        followerValue2: EnumerationModel?
    )

    fun visitAssociation(
        leaderValue1: AssociationModel,
        followerValue1: AssociationModel?,
        followerValue2: AssociationModel?
    ): Return

    fun leaveAssociation(
        leaderValue1: AssociationModel,
        followerValue1: AssociationModel?,
        followerValue2: AssociationModel?
    )
}