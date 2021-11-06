package org.treeWare.model.traversal

import org.treeWare.model.core.*

abstract class AbstractLeader1Follower0MutableModelVisitor<Return>(
    private val defaultVisitReturn: Return
) : Leader1Follower0MutableModelVisitor<Return> {
    // Abstract elements are not visited

    override fun visit(leaderMain1: MutableMainModel): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderMain1: MutableMainModel) {}

    override fun visit(leaderRoot1: MutableRootModel): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderRoot1: MutableRootModel) {}

    override fun visit(leaderEntity1: MutableEntityModel): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderEntity1: MutableEntityModel) {}

    // Fields

    override fun visit(leaderField1: MutableSingleFieldModel): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderField1: MutableSingleFieldModel) {}

    override fun visit(leaderField1: MutableListFieldModel): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderField1: MutableListFieldModel) {}

    override fun visit(leaderField1: MutableSetFieldModel): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderField1: MutableSetFieldModel) {}

    // Values

    override fun visit(leaderValue1: MutablePrimitiveModel): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderValue1: MutablePrimitiveModel) {}

    override fun visit(leaderValue1: MutableAliasModel): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderValue1: MutableAliasModel) {}

    override fun visit(leaderValue1: MutablePassword1wayModel): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderValue1: MutablePassword1wayModel) {}

    override fun visit(leaderValue1: MutablePassword2wayModel): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderValue1: MutablePassword2wayModel) {}

    override fun visit(leaderValue1: MutableEnumerationModel): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderValue1: MutableEnumerationModel) {}

    override fun visit(leaderValue1: MutableAssociationModel): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderValue1: MutableAssociationModel) {}

    // Sub-values

    override fun visit(leaderEntityKeys1: MutableEntityKeysModel): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderEntityKeys1: MutableEntityKeysModel) {}
}
