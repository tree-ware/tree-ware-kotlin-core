package org.treeWare.model.traversal

import org.treeWare.model.core.*

abstract class AbstractLeaderManyFollower0ModelVisitor<LeaderAux, Return>(
    private val defaultVisitReturn: Return
) : LeaderManyFollower0ModelVisitor<LeaderAux, Return> {
    override fun visitMain(leaderMainList: List<MainModel<LeaderAux>?>): Return {
        return defaultVisitReturn
    }

    override fun leaveMain(leaderMainList: List<MainModel<LeaderAux>?>) {}

    override fun visitRoot(leaderRootList: List<RootModel<LeaderAux>?>): Return {
        return defaultVisitReturn
    }

    override fun leaveRoot(leaderRootList: List<RootModel<LeaderAux>?>) {}

    override fun visitEntity(leaderEntityList: List<EntityModel<LeaderAux>?>): Return {
        return defaultVisitReturn
    }

    override fun leaveEntity(leaderEntityList: List<EntityModel<LeaderAux>?>) {}

    override fun visitSingleField(leaderFieldList: List<SingleFieldModel<LeaderAux>?>): Return {
        return defaultVisitReturn
    }

    override fun leaveSingleField(leaderFieldList: List<SingleFieldModel<LeaderAux>?>) {}

    override fun visitListField(leaderFieldList: List<ListFieldModel<LeaderAux>?>): Return {
        return defaultVisitReturn
    }

    override fun leaveListField(leaderFieldList: List<ListFieldModel<LeaderAux>?>) {}

    override fun visitSetField(leaderFieldList: List<SetFieldModel<LeaderAux>?>): Return {
        return defaultVisitReturn
    }

    override fun leaveSetField(leaderFieldList: List<SetFieldModel<LeaderAux>?>) {}

    override fun visitPrimitive(leaderValueList: List<PrimitiveModel<LeaderAux>?>): Return {
        return defaultVisitReturn
    }

    override fun leavePrimitive(leaderValueList: List<PrimitiveModel<LeaderAux>?>) {}

    override fun visitAlias(leaderValueList: List<AliasModel<LeaderAux>?>): Return {
        return defaultVisitReturn
    }

    override fun leaveAlias(leaderValueList: List<AliasModel<LeaderAux>?>) {}

    override fun visitPassword1way(leaderValueList: List<Password1wayModel<LeaderAux>?>): Return {
        return defaultVisitReturn
    }

    override fun leavePassword1way(leaderValueList: List<Password1wayModel<LeaderAux>?>) {}

    override fun visitPassword2way(leaderValueList: List<Password2wayModel<LeaderAux>?>): Return {
        return defaultVisitReturn
    }

    override fun leavePassword2way(leaderValueList: List<Password2wayModel<LeaderAux>?>) {}

    override fun visitEnumeration(leaderValueList: List<EnumerationModel<LeaderAux>?>): Return {
        return defaultVisitReturn
    }

    override fun leaveEnumeration(leaderValueList: List<EnumerationModel<LeaderAux>?>) {}

    override fun visitAssociation(leaderValueList: List<AssociationModel<LeaderAux>?>): Return {
        return defaultVisitReturn
    }

    override fun leaveAssociation(leaderValueList: List<AssociationModel<LeaderAux>?>) {}
}
