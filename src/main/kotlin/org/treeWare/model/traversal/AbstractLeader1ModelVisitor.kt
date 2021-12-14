package org.treeWare.model.traversal

import org.treeWare.model.core.*

abstract class AbstractLeader1ModelVisitor<Return>(
    private val defaultVisitReturn: Return
) : Leader1ModelVisitor<Return> {
    // Abstract elements are not visited

    override fun visit(leaderMain1: MainModel): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderMain1: MainModel) {}

    override fun visit(leaderRoot1: RootModel): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderRoot1: RootModel) {}

    override fun visit(leaderEntity1: EntityModel): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderEntity1: EntityModel) {}

    // Fields

    override fun visit(leaderField1: SingleFieldModel): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderField1: SingleFieldModel) {}

    override fun visit(leaderField1: ListFieldModel): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderField1: ListFieldModel) {}

    override fun visit(leaderField1: SetFieldModel): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderField1: SetFieldModel) {}

    // Values

    override fun visit(leaderValue1: PrimitiveModel): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderValue1: PrimitiveModel) {}

    override fun visit(leaderValue1: AliasModel): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderValue1: AliasModel) {}

    override fun visit(leaderValue1: Password1wayModel): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderValue1: Password1wayModel) {}

    override fun visit(leaderValue1: Password2wayModel): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderValue1: Password2wayModel) {}

    override fun visit(leaderValue1: EnumerationModel): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderValue1: EnumerationModel) {}

    override fun visit(leaderValue1: AssociationModel): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderValue1: AssociationModel) {}

    // Sub-values

    override fun visit(leaderEntityKeys1: EntityKeysModel): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderEntityKeys1: EntityKeysModel) {}
}
