package org.treeWare.model.operator

import org.treeWare.model.core.*

abstract class AbstractLeader1Follower0ModelVisitor<LeaderAux, Return>(
    private val defaultVisitReturn: Return
) : Leader1Follower0ModelVisitor<LeaderAux, Return> {
    // Abstract elements are not visited

    override fun visit(leaderModel1: Model<LeaderAux>): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderModel1: Model<LeaderAux>) {}

    override fun visit(leaderRoot1: RootModel<LeaderAux>): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderRoot1: RootModel<LeaderAux>) {}

    override fun visit(leaderEntity1: EntityModel<LeaderAux>): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderEntity1: EntityModel<LeaderAux>) {}

    // Scalar fields

    override fun visit(leaderField1: PrimitiveFieldModel<LeaderAux>): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderField1: PrimitiveFieldModel<LeaderAux>) {}

    override fun visit(leaderField1: AliasFieldModel<LeaderAux>): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderField1: AliasFieldModel<LeaderAux>) {}

    override fun visit(leaderField1: EnumerationFieldModel<LeaderAux>): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderField1: EnumerationFieldModel<LeaderAux>) {}

    override fun visit(leaderField1: AssociationFieldModel<LeaderAux>): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderField1: AssociationFieldModel<LeaderAux>) {}

    override fun visit(leaderField1: CompositionFieldModel<LeaderAux>): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderField1: CompositionFieldModel<LeaderAux>) {}

    // List fields

    override fun visit(leaderField1: PrimitiveListFieldModel<LeaderAux>): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderField1: PrimitiveListFieldModel<LeaderAux>) {}

    override fun visit(leaderField1: AliasListFieldModel<LeaderAux>): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderField1: AliasListFieldModel<LeaderAux>) {}

    override fun visit(leaderField1: EnumerationListFieldModel<LeaderAux>): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderField1: EnumerationListFieldModel<LeaderAux>) {}

    override fun visit(leaderField1: AssociationListFieldModel<LeaderAux>): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderField1: AssociationListFieldModel<LeaderAux>) {}

    override fun visit(leaderField1: CompositionListFieldModel<LeaderAux>): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderField1: CompositionListFieldModel<LeaderAux>) {}

    // Field values

    override fun visit(leaderEntityKeys1: EntityKeysModel<LeaderAux>): Return {
        return defaultVisitReturn
    }

    override fun leave(leaderEntityKeys1: EntityKeysModel<LeaderAux>) {}
}
