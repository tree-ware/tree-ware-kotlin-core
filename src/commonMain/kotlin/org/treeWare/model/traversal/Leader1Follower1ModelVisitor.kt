package org.treeWare.model.traversal

import org.treeWare.model.core.*

interface Leader1Follower1ModelVisitor<Return> {
    // Abstract elements are not visited

    fun visitMain(leaderMain1: MainModel, followerModel1: MainModel?): Return
    fun leaveMain(leaderMain1: MainModel, followerModel1: MainModel?)

    fun visitEntity(leaderEntity1: EntityModel, followerEntity1: EntityModel?): Return
    fun leaveEntity(leaderEntity1: EntityModel, followerEntity1: EntityModel?)

    // Fields

    fun visitSingleField(leaderField1: SingleFieldModel, followerField1: SingleFieldModel?): Return
    fun leaveSingleField(leaderField1: SingleFieldModel, followerField1: SingleFieldModel?)

    fun visitListField(leaderField1: ListFieldModel, followerField1: ListFieldModel?): Return
    fun leaveListField(leaderField1: ListFieldModel, followerField1: ListFieldModel?)

    fun visitSetField(leaderField1: SetFieldModel, followerField1: SetFieldModel?): Return
    fun leaveSetField(leaderField1: SetFieldModel, followerField1: SetFieldModel?)

    // Values

    fun visitPrimitive(leaderValue1: PrimitiveModel, followerValue1: PrimitiveModel?): Return
    fun leavePrimitive(leaderValue1: PrimitiveModel, followerValue1: PrimitiveModel?)

    fun visitAlias(leaderValue1: AliasModel, followerValue1: AliasModel?): Return
    fun leaveAlias(leaderValue1: AliasModel, followerValue1: AliasModel?)

    fun visitPassword1way(leaderValue1: Password1wayModel, followerValue1: Password1wayModel?): Return
    fun leavePassword1way(leaderValue1: Password1wayModel, followerValue1: Password1wayModel?)

    fun visitPassword2way(leaderValue1: Password2wayModel, followerValue1: Password2wayModel?): Return
    fun leavePassword2way(leaderValue1: Password2wayModel, followerValue1: Password2wayModel?)

    fun visitEnumeration(leaderValue1: EnumerationModel, followerValue1: EnumerationModel?): Return
    fun leaveEnumeration(leaderValue1: EnumerationModel, followerValue1: EnumerationModel?)

    fun visitAssociation(leaderValue1: AssociationModel, followerValue1: AssociationModel?): Return
    fun leaveAssociation(leaderValue1: AssociationModel, followerValue1: AssociationModel?)

    // Sub-values

    fun visitEntityKeys(leaderEntityKeys1: EntityKeysModel, followerEntityKeys1: EntityKeysModel?): Return
    fun leaveEntityKeys(leaderEntityKeys1: EntityKeysModel, followerEntityKeys1: EntityKeysModel?)
}