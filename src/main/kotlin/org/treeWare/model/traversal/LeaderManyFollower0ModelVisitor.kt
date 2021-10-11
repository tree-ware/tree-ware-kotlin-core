package org.treeWare.model.traversal

import org.treeWare.model.core.*

interface LeaderManyFollower0ModelVisitor<LeaderAux, Return> {
    // Abstract elements are not visited

    fun visitMain(leaderMainList: List<MainModel<LeaderAux>?>): Return
    fun leaveMain(leaderMainList: List<MainModel<LeaderAux>?>)

    fun visitRoot(leaderRootList: List<RootModel<LeaderAux>?>): Return
    fun leaveRoot(leaderRootList: List<RootModel<LeaderAux>?>)

    fun visitEntity(leaderEntityList: List<EntityModel<LeaderAux>?>): Return
    fun leaveEntity(leaderEntityList: List<EntityModel<LeaderAux>?>)

    // Fields

    fun visitSingleField(leaderFieldList: List<SingleFieldModel<LeaderAux>?>): Return
    fun leaveSingleField(leaderFieldList: List<SingleFieldModel<LeaderAux>?>)

    fun visitListField(leaderFieldList: List<ListFieldModel<LeaderAux>?>): Return
    fun leaveListField(leaderFieldList: List<ListFieldModel<LeaderAux>?>)

    fun visitSetField(leaderFieldList: List<SetFieldModel<LeaderAux>?>): Return
    fun leaveSetField(leaderFieldList: List<SetFieldModel<LeaderAux>?>)

    // Values

    fun visitPrimitive(leaderValueList: List<PrimitiveModel<LeaderAux>?>): Return
    fun leavePrimitive(leaderValueList: List<PrimitiveModel<LeaderAux>?>)

    fun visitAlias(leaderValueList: List<AliasModel<LeaderAux>?>): Return
    fun leaveAlias(leaderValueList: List<AliasModel<LeaderAux>?>)

    fun visitPassword1way(leaderValueList: List<Password1wayModel<LeaderAux>?>): Return
    fun leavePassword1way(leaderValueList: List<Password1wayModel<LeaderAux>?>)

    fun visitPassword2way(leaderValueList: List<Password2wayModel<LeaderAux>?>): Return
    fun leavePassword2way(leaderValueList: List<Password2wayModel<LeaderAux>?>)

    fun visitEnumeration(leaderValueList: List<EnumerationModel<LeaderAux>?>): Return
    fun leaveEnumeration(leaderValueList: List<EnumerationModel<LeaderAux>?>)

    fun visitAssociation(leaderValueList: List<AssociationModel<LeaderAux>?>): Return
    fun leaveAssociation(leaderValueList: List<AssociationModel<LeaderAux>?>)
}
