package org.treeWare.model.traversal

import org.treeWare.model.core.*

interface Leader1ModelVisitor<Return> {
    // Abstract elements are not visited

    fun visitEntity(leaderEntity1: EntityModel): Return
    fun leaveEntity(leaderEntity1: EntityModel)

    // Fields

    fun visitSingleField(leaderField1: SingleFieldModel): Return
    fun leaveSingleField(leaderField1: SingleFieldModel)

    fun visitSetField(leaderField1: SetFieldModel): Return
    fun leaveSetField(leaderField1: SetFieldModel)

    // Values

    fun visitPrimitive(leaderValue1: PrimitiveModel): Return
    fun leavePrimitive(leaderValue1: PrimitiveModel)

    fun visitAlias(leaderValue1: AliasModel): Return
    fun leaveAlias(leaderValue1: AliasModel)

    fun visitPassword1way(leaderValue1: Password1wayModel): Return
    fun leavePassword1way(leaderValue1: Password1wayModel)

    fun visitPassword2way(leaderValue1: Password2wayModel): Return
    fun leavePassword2way(leaderValue1: Password2wayModel)

    fun visitEnumeration(leaderValue1: EnumerationModel): Return
    fun leaveEnumeration(leaderValue1: EnumerationModel)

    fun visitAssociation(leaderValue1: AssociationModel): Return
    fun leaveAssociation(leaderValue1: AssociationModel)
}