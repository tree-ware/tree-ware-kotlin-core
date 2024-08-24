package org.treeWare.model.traversal

import org.treeWare.model.core.*

abstract class IllegalStateLeader1ModelVisitor<Return> : Leader1ModelVisitor<Return> {
    // region visit/leave top elements

    override fun visitEntity(leaderEntity1: EntityModel): Return {
        throw IllegalStateException()
    }

    override fun leaveEntity(leaderEntity1: EntityModel) {
        throw IllegalStateException()
    }

    // endregion

    // region visit/leave field elements

    override fun visitSingleField(leaderField1: SingleFieldModel): Return {
        throw IllegalStateException()
    }

    override fun leaveSingleField(leaderField1: SingleFieldModel) {
        throw IllegalStateException()
    }

    override fun visitListField(leaderField1: ListFieldModel): Return {
        throw IllegalStateException()
    }

    override fun leaveListField(leaderField1: ListFieldModel) {
        throw IllegalStateException()
    }

    override fun visitSetField(leaderField1: SetFieldModel): Return {
        throw IllegalStateException()
    }

    override fun leaveSetField(leaderField1: SetFieldModel) {
        throw IllegalStateException()
    }

    // endregion

    // region visit/leave value elements

    override fun visitPrimitive(leaderValue1: PrimitiveModel): Return {
        throw IllegalStateException()
    }

    override fun leavePrimitive(leaderValue1: PrimitiveModel) {
        throw IllegalStateException()
    }

    override fun visitAlias(leaderValue1: AliasModel): Return {
        throw IllegalStateException()
    }

    override fun leaveAlias(leaderValue1: AliasModel) {
        throw IllegalStateException()
    }

    override fun visitPassword1way(leaderValue1: Password1wayModel): Return {
        throw IllegalStateException()
    }

    override fun leavePassword1way(leaderValue1: Password1wayModel) {
        throw IllegalStateException()
    }

    override fun visitPassword2way(leaderValue1: Password2wayModel): Return {
        throw IllegalStateException()
    }

    override fun leavePassword2way(leaderValue1: Password2wayModel) {
        throw IllegalStateException()
    }

    override fun visitEnumeration(leaderValue1: EnumerationModel): Return {
        throw IllegalStateException()
    }

    override fun leaveEnumeration(leaderValue1: EnumerationModel) {
        throw IllegalStateException()
    }

    override fun visitAssociation(leaderValue1: AssociationModel): Return {
        throw IllegalStateException()
    }

    override fun leaveAssociation(leaderValue1: AssociationModel) {
        throw IllegalStateException()
    }

    // endregion
}