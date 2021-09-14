package org.treeWare.model.traversal

import org.treeWare.model.core.*

abstract class AbstractLeader1Follower0ModelVisitor<LeaderAux, Return>(
    private val defaultVisitReturn: Return
) : Leader1Follower0ModelVisitor<LeaderAux, Return> {
    // Abstract elements are not visited

    override fun visit(leaderMain1: MainModel<LeaderAux>): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderMain1: MainModel<LeaderAux>) {}

    override fun visit(leaderRoot1: RootModel<LeaderAux>): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderRoot1: RootModel<LeaderAux>) {}

    override fun visit(leaderEntity1: EntityModel<LeaderAux>): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderEntity1: EntityModel<LeaderAux>) {}

    // Fields

    override fun visit(leaderField1: SingleFieldModel<LeaderAux>): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderField1: SingleFieldModel<LeaderAux>) {}

    override fun visit(leaderField1: ListFieldModel<LeaderAux>): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderField1: ListFieldModel<LeaderAux>) {}

    // Values

    override fun visit(leaderValue1: PrimitiveModel<LeaderAux>): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderValue1: PrimitiveModel<LeaderAux>) {}

    override fun visit(leaderValue1: AliasModel<LeaderAux>): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderValue1: AliasModel<LeaderAux>) {}

    override fun visit(leaderValue1: Password1wayModel<LeaderAux>): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderValue1: Password1wayModel<LeaderAux>) {}

    override fun visit(leaderValue1: Password2wayModel<LeaderAux>): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderValue1: Password2wayModel<LeaderAux>) {}

    override fun visit(leaderValue1: EnumerationModel<LeaderAux>): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderValue1: EnumerationModel<LeaderAux>) {}

    override fun visit(leaderValue1: AssociationModel<LeaderAux>): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderValue1: AssociationModel<LeaderAux>) {}

    // Sub-values

    override fun visit(leaderEntityKeys1: EntityKeysModel<LeaderAux>): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderEntityKeys1: EntityKeysModel<LeaderAux>) {}
}
