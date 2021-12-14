package org.treeWare.model.traversal

import org.treeWare.model.core.*

abstract class AbstractLeader1ModelVisitor<Return>(
    private val defaultVisitReturn: Return
) : Leader1ModelVisitor<Return> {
    // Abstract elements are not visited

    override fun visitMain(leaderMain1: MainModel): Return {
        return defaultVisitReturn
    }

    override fun leaveMain(leaderMain1: MainModel) {}

    override fun visitRoot(leaderRoot1: RootModel): Return {
        return defaultVisitReturn
    }

    override fun leaveRoot(leaderRoot1: RootModel) {}

    override fun visitEntity(leaderEntity1: EntityModel): Return {
        return defaultVisitReturn
    }

    override fun leaveEntity(leaderEntity1: EntityModel) {}

    // Fields

    override fun visitSingleField(leaderField1: SingleFieldModel): Return {
        return defaultVisitReturn
    }

    override fun leaveSingleField(leaderField1: SingleFieldModel) {}

    override fun visitListField(leaderField1: ListFieldModel): Return {
        return defaultVisitReturn
    }

    override fun leaveListField(leaderField1: ListFieldModel) {}

    override fun visitSetField(leaderField1: SetFieldModel): Return {
        return defaultVisitReturn
    }

    override fun leaveSetField(leaderField1: SetFieldModel) {}

    // Values

    override fun visitPrimitive(leaderValue1: PrimitiveModel): Return {
        return defaultVisitReturn
    }

    override fun leavePrimitive(leaderValue1: PrimitiveModel) {}

    override fun visitAlias(leaderValue1: AliasModel): Return {
        return defaultVisitReturn
    }

    override fun leaveAlias(leaderValue1: AliasModel) {}

    override fun visitPassword1way(leaderValue1: Password1wayModel): Return {
        return defaultVisitReturn
    }

    override fun leavePassword1way(leaderValue1: Password1wayModel) {}

    override fun visitPassword2way(leaderValue1: Password2wayModel): Return {
        return defaultVisitReturn
    }

    override fun leavePassword2way(leaderValue1: Password2wayModel) {}

    override fun visitEnumeration(leaderValue1: EnumerationModel): Return {
        return defaultVisitReturn
    }

    override fun leaveEnumeration(leaderValue1: EnumerationModel) {}

    override fun visitAssociation(leaderValue1: AssociationModel): Return {
        return defaultVisitReturn
    }

    override fun leaveAssociation(leaderValue1: AssociationModel) {}

    // Sub-values

    override fun visitEntityKeys(leaderEntityKeys1: EntityKeysModel): Return {
        return defaultVisitReturn
    }

    override fun leaveEntityKeys(leaderEntityKeys1: EntityKeysModel) {}
}
