package org.treeWare.model.traversal

import org.treeWare.model.core.*

interface LeaderManyFollower0ModelVisitor<LeaderAux, Return> {
    // Abstract elements are not visited

    suspend fun visitMain(leaderMainList: List<MainModel<LeaderAux>?>): Return
    suspend fun leaveMain(leaderMainList: List<MainModel<LeaderAux>?>)

    suspend fun visitRoot(leaderRootList: List<RootModel<LeaderAux>?>): Return
    suspend fun leaveRoot(leaderRootList: List<RootModel<LeaderAux>?>)

    suspend fun visitEntity(leaderEntityList: List<EntityModel<LeaderAux>?>): Return
    suspend fun leaveEntity(leaderEntityList: List<EntityModel<LeaderAux>?>)

    // Fields

    suspend fun visitSingleField(leaderFieldList: List<SingleFieldModel<LeaderAux>?>): Return
    suspend fun leaveSingleField(leaderFieldList: List<SingleFieldModel<LeaderAux>?>)

    suspend fun visitListField(leaderFieldList: List<ListFieldModel<LeaderAux>?>): Return
    suspend fun leaveListField(leaderFieldList: List<ListFieldModel<LeaderAux>?>)

    suspend fun visitSetField(leaderFieldList: List<SetFieldModel<LeaderAux>?>): Return
    suspend fun leaveSetField(leaderFieldList: List<SetFieldModel<LeaderAux>?>)

    // Values

    suspend fun visitPrimitive(leaderValueList: List<PrimitiveModel<LeaderAux>?>): Return
    suspend fun leavePrimitive(leaderValueList: List<PrimitiveModel<LeaderAux>?>)

    suspend fun visitAlias(leaderValueList: List<AliasModel<LeaderAux>?>): Return
    suspend fun leaveAlias(leaderValueList: List<AliasModel<LeaderAux>?>)

    suspend fun visitPassword1way(leaderValueList: List<Password1wayModel<LeaderAux>?>): Return
    suspend fun leavePassword1way(leaderValueList: List<Password1wayModel<LeaderAux>?>)

    suspend fun visitPassword2way(leaderValueList: List<Password2wayModel<LeaderAux>?>): Return
    suspend fun leavePassword2way(leaderValueList: List<Password2wayModel<LeaderAux>?>)

    suspend fun visitEnumeration(leaderValueList: List<EnumerationModel<LeaderAux>?>): Return
    suspend fun leaveEnumeration(leaderValueList: List<EnumerationModel<LeaderAux>?>)

    suspend fun visitAssociation(leaderValueList: List<AssociationModel<LeaderAux>?>): Return
    suspend fun leaveAssociation(leaderValueList: List<AssociationModel<LeaderAux>?>)
}
