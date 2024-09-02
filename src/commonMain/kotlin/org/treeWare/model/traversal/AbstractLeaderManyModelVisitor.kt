package org.treeWare.model.traversal

import org.treeWare.model.core.*

abstract class AbstractLeaderManyModelVisitor<Return>(
    private val defaultVisitReturn: Return
) : LeaderManyModelVisitor<Return> {
    override fun visitEntity(leaderEntityList: List<EntityModel?>): Return {
        return defaultVisitReturn
    }

    override fun leaveEntity(leaderEntityList: List<EntityModel?>) {}

    override fun visitSingleField(leaderFieldList: List<SingleFieldModel?>): Return {
        return defaultVisitReturn
    }

    override fun leaveSingleField(leaderFieldList: List<SingleFieldModel?>) {}

    override fun visitSetField(leaderFieldList: List<SetFieldModel?>): Return {
        return defaultVisitReturn
    }

    override fun leaveSetField(leaderFieldList: List<SetFieldModel?>) {}

    override fun visitPrimitive(leaderValueList: List<PrimitiveModel?>): Return {
        return defaultVisitReturn
    }

    override fun leavePrimitive(leaderValueList: List<PrimitiveModel?>) {}

    override fun visitAlias(leaderValueList: List<AliasModel?>): Return {
        return defaultVisitReturn
    }

    override fun leaveAlias(leaderValueList: List<AliasModel?>) {}

    override fun visitPassword1way(leaderValueList: List<Password1wayModel?>): Return {
        return defaultVisitReturn
    }

    override fun leavePassword1way(leaderValueList: List<Password1wayModel?>) {}

    override fun visitPassword2way(leaderValueList: List<Password2wayModel?>): Return {
        return defaultVisitReturn
    }

    override fun leavePassword2way(leaderValueList: List<Password2wayModel?>) {}

    override fun visitEnumeration(leaderValueList: List<EnumerationModel?>): Return {
        return defaultVisitReturn
    }

    override fun leaveEnumeration(leaderValueList: List<EnumerationModel?>) {}

    override fun visitAssociation(leaderValueList: List<AssociationModel?>): Return {
        return defaultVisitReturn
    }

    override fun leaveAssociation(leaderValueList: List<AssociationModel?>) {}
}