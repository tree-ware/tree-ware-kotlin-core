package org.treeWare.model.traversal

import org.treeWare.model.core.*

abstract class AbstractLeader1Follower1ModelVisitor<Return>(
    private val defaultVisitReturn: Return
) : Leader1Follower1ModelVisitor<Return> {
    override fun visitMain(leaderMain1: MainModel, followerMain1: MainModel?): Return = defaultVisitReturn

    override fun leaveMain(leaderMain1: MainModel, followerMain1: MainModel?) {}

    override fun visitEntity(leaderEntity1: EntityModel, followerEntity1: EntityModel?): Return = defaultVisitReturn

    override fun leaveEntity(leaderEntity1: EntityModel, followerEntity1: EntityModel?) {}

    override fun visitSingleField(leaderField1: SingleFieldModel, followerField1: SingleFieldModel?): Return =
        defaultVisitReturn

    override fun leaveSingleField(leaderField1: SingleFieldModel, followerField1: SingleFieldModel?) {}

    override fun visitListField(leaderField1: ListFieldModel, followerField1: ListFieldModel?): Return =
        defaultVisitReturn

    override fun leaveListField(leaderField1: ListFieldModel, followerField1: ListFieldModel?) {}

    override fun visitSetField(leaderField1: SetFieldModel, followerField1: SetFieldModel?): Return = defaultVisitReturn

    override fun leaveSetField(leaderField1: SetFieldModel, followerField1: SetFieldModel?) {}

    override fun visitPrimitive(leaderValue1: PrimitiveModel, followerValue1: PrimitiveModel?): Return =
        defaultVisitReturn

    override fun leavePrimitive(leaderValue1: PrimitiveModel, followerValue1: PrimitiveModel?) {}

    override fun visitAlias(leaderValue1: AliasModel, followerValue1: AliasModel?): Return = defaultVisitReturn

    override fun leaveAlias(leaderValue1: AliasModel, followerValue1: AliasModel?) {}

    override fun visitPassword1way(leaderValue1: Password1wayModel, followerValue1: Password1wayModel?): Return =
        defaultVisitReturn

    override fun leavePassword1way(leaderValue1: Password1wayModel, followerValue1: Password1wayModel?) {}

    override fun visitPassword2way(leaderValue1: Password2wayModel, followerValue1: Password2wayModel?): Return =
        defaultVisitReturn

    override fun leavePassword2way(leaderValue1: Password2wayModel, followerValue1: Password2wayModel?) {}

    override fun visitEnumeration(leaderValue1: EnumerationModel, followerValue1: EnumerationModel?): Return =
        defaultVisitReturn

    override fun leaveEnumeration(leaderValue1: EnumerationModel, followerValue1: EnumerationModel?) {}

    override fun visitAssociation(leaderValue1: AssociationModel, followerValue1: AssociationModel?): Return =
        defaultVisitReturn

    override fun leaveAssociation(leaderValue1: AssociationModel, followerValue1: AssociationModel?) {}
}