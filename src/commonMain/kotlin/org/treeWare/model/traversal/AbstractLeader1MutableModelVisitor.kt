package org.treeWare.model.traversal

import org.treeWare.model.core.*

abstract class AbstractLeader1MutableModelVisitor<Return>(
    private val defaultVisitReturn: Return
) : Leader1MutableModelVisitor<Return> {
    // Abstract elements are not visited

    override fun visitMutableEntity(leaderEntity1: MutableEntityModel): Return {
        return defaultVisitReturn
    }

    override fun leaveMutableEntity(leaderEntity1: MutableEntityModel) {}

    // Fields

    override fun visitMutableSingleField(leaderField1: MutableSingleFieldModel): Return {
        return defaultVisitReturn
    }

    override fun leaveMutableSingleField(leaderField1: MutableSingleFieldModel) {}

    override fun visitMutableListField(leaderField1: MutableListFieldModel): Return {
        return defaultVisitReturn
    }

    override fun leaveMutableListField(leaderField1: MutableListFieldModel) {}

    override fun visitMutableSetField(leaderField1: MutableSetFieldModel): Return {
        return defaultVisitReturn
    }

    override fun leaveMutableSetField(leaderField1: MutableSetFieldModel) {}

    // Values

    override fun visitMutablePrimitive(leaderValue1: MutablePrimitiveModel): Return {
        return defaultVisitReturn
    }

    override fun leaveMutablePrimitive(leaderValue1: MutablePrimitiveModel) {}

    override fun visitMutableAlias(leaderValue1: MutableAliasModel): Return {
        return defaultVisitReturn
    }

    override fun leaveMutableAlias(leaderValue1: MutableAliasModel) {}

    override fun visitMutablePassword1way(leaderValue1: MutablePassword1wayModel): Return {
        return defaultVisitReturn
    }

    override fun leaveMutablePassword1way(leaderValue1: MutablePassword1wayModel) {}

    override fun visitMutablePassword2way(leaderValue1: MutablePassword2wayModel): Return {
        return defaultVisitReturn
    }

    override fun leaveMutablePassword2way(leaderValue1: MutablePassword2wayModel) {}

    override fun visitMutableEnumeration(leaderValue1: MutableEnumerationModel): Return {
        return defaultVisitReturn
    }

    override fun leaveMutableEnumeration(leaderValue1: MutableEnumerationModel) {}

    override fun visitMutableAssociation(leaderValue1: MutableAssociationModel): Return {
        return defaultVisitReturn
    }

    override fun leaveMutableAssociation(leaderValue1: MutableAssociationModel) {}
}