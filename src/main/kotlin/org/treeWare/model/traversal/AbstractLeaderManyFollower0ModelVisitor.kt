package org.treeWare.model.traversal

import org.treeWare.model.core.*

abstract class AbstractLeaderManyFollower0ModelVisitor<LeaderAux, Return>(
    private val defaultVisitReturn: Return
) : LeaderManyFollower0ModelVisitor<LeaderAux, Return> {
    override suspend fun visitMain(leaderMainList: List<MainModel<LeaderAux>?>): Return {
        return defaultVisitReturn
    }

    override suspend fun leaveMain(leaderMainList: List<MainModel<LeaderAux>?>) {}

    override suspend fun visitRoot(leaderRootList: List<RootModel<LeaderAux>?>): Return {
        return defaultVisitReturn
    }

    override suspend fun leaveRoot(leaderRootList: List<RootModel<LeaderAux>?>) {}

    override suspend fun visitEntity(leaderEntityList: List<EntityModel<LeaderAux>?>): Return {
        return defaultVisitReturn
    }

    override suspend fun leaveEntity(leaderEntityList: List<EntityModel<LeaderAux>?>) {}

    override suspend fun visitSingleField(leaderFieldList: List<SingleFieldModel<LeaderAux>?>): Return {
        return defaultVisitReturn
    }

    override suspend fun leaveSingleField(leaderFieldList: List<SingleFieldModel<LeaderAux>?>) {}

    override suspend fun visitListField(leaderFieldList: List<ListFieldModel<LeaderAux>?>): Return {
        return defaultVisitReturn
    }

    override suspend fun leaveListField(leaderFieldList: List<ListFieldModel<LeaderAux>?>) {}

    override suspend fun visitSetField(leaderFieldList: List<SetFieldModel<LeaderAux>?>): Return {
        return defaultVisitReturn
    }

    override suspend fun leaveSetField(leaderFieldList: List<SetFieldModel<LeaderAux>?>) {}

    override suspend fun visitPrimitive(leaderValueList: List<PrimitiveModel<LeaderAux>?>): Return {
        return defaultVisitReturn
    }

    override suspend fun leavePrimitive(leaderValueList: List<PrimitiveModel<LeaderAux>?>) {}

    override suspend fun visitAlias(leaderValueList: List<AliasModel<LeaderAux>?>): Return {
        return defaultVisitReturn
    }

    override suspend fun leaveAlias(leaderValueList: List<AliasModel<LeaderAux>?>) {}

    override suspend fun visitPassword1way(leaderValueList: List<Password1wayModel<LeaderAux>?>): Return {
        return defaultVisitReturn
    }

    override suspend fun leavePassword1way(leaderValueList: List<Password1wayModel<LeaderAux>?>) {}

    override suspend fun visitPassword2way(leaderValueList: List<Password2wayModel<LeaderAux>?>): Return {
        return defaultVisitReturn
    }

    override suspend fun leavePassword2way(leaderValueList: List<Password2wayModel<LeaderAux>?>) {}

    override suspend fun visitEnumeration(leaderValueList: List<EnumerationModel<LeaderAux>?>): Return {
        return defaultVisitReturn
    }

    override suspend fun leaveEnumeration(leaderValueList: List<EnumerationModel<LeaderAux>?>) {}

    override suspend fun visitAssociation(leaderValueList: List<AssociationModel<LeaderAux>?>): Return {
        return defaultVisitReturn
    }

    override suspend fun leaveAssociation(leaderValueList: List<AssociationModel<LeaderAux>?>) {}
}
