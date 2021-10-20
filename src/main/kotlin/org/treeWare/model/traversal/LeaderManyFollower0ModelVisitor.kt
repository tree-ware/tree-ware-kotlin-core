package org.treeWare.model.traversal

import org.treeWare.model.core.*

interface LeaderManyFollower0ModelVisitor<Return> {
    // Abstract elements are not visited

    fun visitMain(leaderMainList: List<MainModel?>): Return
    fun leaveMain(leaderMainList: List<MainModel?>)

    fun visitRoot(leaderRootList: List<RootModel?>): Return
    fun leaveRoot(leaderRootList: List<RootModel?>)

    fun visitEntity(leaderEntityList: List<EntityModel?>): Return
    fun leaveEntity(leaderEntityList: List<EntityModel?>)

    // Fields

    fun visitSingleField(leaderFieldList: List<SingleFieldModel?>): Return
    fun leaveSingleField(leaderFieldList: List<SingleFieldModel?>)

    fun visitListField(leaderFieldList: List<ListFieldModel?>): Return
    fun leaveListField(leaderFieldList: List<ListFieldModel?>)

    fun visitSetField(leaderFieldList: List<SetFieldModel?>): Return
    fun leaveSetField(leaderFieldList: List<SetFieldModel?>)

    // Values

    fun visitPrimitive(leaderValueList: List<PrimitiveModel?>): Return
    fun leavePrimitive(leaderValueList: List<PrimitiveModel?>)

    fun visitAlias(leaderValueList: List<AliasModel?>): Return
    fun leaveAlias(leaderValueList: List<AliasModel?>)

    fun visitPassword1way(leaderValueList: List<Password1wayModel?>): Return
    fun leavePassword1way(leaderValueList: List<Password1wayModel?>)

    fun visitPassword2way(leaderValueList: List<Password2wayModel?>): Return
    fun leavePassword2way(leaderValueList: List<Password2wayModel?>)

    fun visitEnumeration(leaderValueList: List<EnumerationModel?>): Return
    fun leaveEnumeration(leaderValueList: List<EnumerationModel?>)

    fun visitAssociation(leaderValueList: List<AssociationModel?>): Return
    fun leaveAssociation(leaderValueList: List<AssociationModel?>)
}
