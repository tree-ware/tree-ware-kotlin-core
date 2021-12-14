package org.treeWare.metaModel.traversal

import org.treeWare.model.core.*
import org.treeWare.model.traversal.Leader1Follower0MutableModelVisitor

class Leader1Follower0MutableAdapter<Return>(
    private val adaptee: Leader1Follower0MutableMetaModelVisitor<Return>,
    private val defaultVisitReturn: Return
) : Leader1Follower0MutableModelVisitor<Return> {
    override fun visit(leaderMain1: MutableMainModel): Return = adaptee.visitMainMeta(leaderMain1)
    override fun leave(leaderMain1: MutableMainModel) = adaptee.leaveMainMeta(leaderMain1)

    override fun visit(leaderRoot1: MutableRootModel): Return = defaultVisitReturn
    override fun leave(leaderRoot1: MutableRootModel) {}

    override fun visit(leaderEntity1: MutableEntityModel): Return {
        return when (val metaMetaName = leaderEntity1.getMetaAux()?.fullName) {
            "/tree_ware_meta_model.main/root" -> adaptee.visitRootMeta(leaderEntity1)
            "/tree_ware_meta_model.main/package" -> adaptee.visitPackageMeta(leaderEntity1)
            "/tree_ware_meta_model.main/enumeration" -> adaptee.visitEnumerationMeta(leaderEntity1)
            "/tree_ware_meta_model.main/enumeration_value" -> adaptee.visitEnumerationValueMeta(leaderEntity1)
            "/tree_ware_meta_model.main/entity" -> adaptee.visitEntityMeta(leaderEntity1)
            "/tree_ware_meta_model.main/field" -> adaptee.visitFieldMeta(leaderEntity1)
            "/tree_ware_meta_model.main/composition_info" -> defaultVisitReturn
            "/tree_ware_meta_model.main/enumeration_info" -> defaultVisitReturn
            else -> throw IllegalStateException("Illegal metaMetaName $metaMetaName")
        }
    }

    override fun leave(leaderEntity1: MutableEntityModel) {
        return when (val metaMetaName = leaderEntity1.getMetaAux()?.fullName) {
            "/tree_ware_meta_model.main/root" -> adaptee.leaveRootMeta(leaderEntity1)
            "/tree_ware_meta_model.main/package" -> adaptee.leavePackageMeta(leaderEntity1)
            "/tree_ware_meta_model.main/enumeration" -> adaptee.leaveEnumerationMeta(leaderEntity1)
            "/tree_ware_meta_model.main/enumeration_value" -> adaptee.leaveEnumerationValueMeta(leaderEntity1)
            "/tree_ware_meta_model.main/entity" -> adaptee.leaveEntityMeta(leaderEntity1)
            "/tree_ware_meta_model.main/field" -> adaptee.leaveFieldMeta(leaderEntity1)
            "/tree_ware_meta_model.main/composition_info" -> Unit
            "/tree_ware_meta_model.main/enumeration_info" -> Unit
            else -> throw IllegalStateException("Illegal metaMetaName $metaMetaName")
        }
    }

    // Fields

    override fun visit(leaderField1: MutableSingleFieldModel): Return = defaultVisitReturn
    override fun leave(leaderField1: MutableSingleFieldModel) {}

    override fun visit(leaderField1: MutableListFieldModel): Return {
        return when (val metaMetaName = leaderField1.getMetaAux()?.fullName) {
            "/tree_ware_meta_model.main/field/association" -> defaultVisitReturn
            else -> throw IllegalStateException("Illegal metaMetaName $metaMetaName")
        }
    }

    override fun leave(leaderField1: MutableListFieldModel) {
        return when (val metaMetaName = leaderField1.getMetaAux()?.fullName) {
            "/tree_ware_meta_model.main/field/association" -> Unit
            else -> throw IllegalStateException("Illegal metaMetaName $metaMetaName")
        }
    }

    override fun visit(leaderField1: MutableSetFieldModel): Return {
        return when (val metaMetaName = leaderField1.getMetaAux()?.fullName) {
            "/tree_ware_meta_model.main/meta_model/packages" -> defaultVisitReturn
            "/tree_ware_meta_model.main/package/enumerations" -> defaultVisitReturn
            "/tree_ware_meta_model.main/enumeration/values" -> defaultVisitReturn
            "/tree_ware_meta_model.main/package/entities" -> defaultVisitReturn
            "/tree_ware_meta_model.main/entity/fields" -> defaultVisitReturn
            else -> throw IllegalStateException("Illegal metaMetaName $metaMetaName")
        }
    }

    override fun leave(leaderField1: MutableSetFieldModel) {}

    // Values

    override fun visit(leaderValue1: MutablePrimitiveModel): Return = defaultVisitReturn
    override fun leave(leaderValue1: MutablePrimitiveModel) {}

    override fun visit(leaderValue1: MutableAliasModel): Return {
        throw IllegalStateException("visit AliasModel")
    }

    override fun leave(leaderValue1: MutableAliasModel) {
        throw IllegalStateException("leave AliasModel")
    }

    override fun visit(leaderValue1: MutablePassword1wayModel): Return {
        throw IllegalStateException("visit Password1WayModel")
    }

    override fun leave(leaderValue1: MutablePassword1wayModel) {
        throw IllegalStateException("leave Password1WayModel")
    }

    override fun visit(leaderValue1: MutablePassword2wayModel): Return {
        throw IllegalStateException("visit Password2WayModel")
    }

    override fun leave(leaderValue1: MutablePassword2wayModel) {
        throw IllegalStateException("leave Password2WayModel")
    }

    override fun visit(leaderValue1: MutableEnumerationModel): Return = defaultVisitReturn
    override fun leave(leaderValue1: MutableEnumerationModel) {}

    override fun visit(leaderValue1: MutableAssociationModel): Return {
        throw IllegalStateException("visit AssociationModel")
    }

    override fun leave(leaderValue1: MutableAssociationModel) {
        throw IllegalStateException("leave AssociationModel")
    }

    // Sub-values

    override fun visit(leaderEntityKeys1: MutableEntityKeysModel): Return {
        throw IllegalStateException("visit EntityKeysModel")
    }

    override fun leave(leaderEntityKeys1: MutableEntityKeysModel) {
        throw IllegalStateException("leave EntityKeysModel")
    }
}
