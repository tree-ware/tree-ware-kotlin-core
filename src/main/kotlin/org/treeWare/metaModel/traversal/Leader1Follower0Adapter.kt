package org.treeWare.metaModel.traversal

import org.treeWare.model.core.*
import org.treeWare.model.traversal.Leader1Follower0ModelVisitor

class Leader1Follower0Adapter<Return>(
    private val adaptee: Leader1Follower0MetaModelVisitor<Return>,
    private val defaultVisitReturn: Return
) : Leader1Follower0ModelVisitor<Return> {
    override fun visit(leaderMain1: MainModel): Return = adaptee.visitMainMeta(leaderMain1)
    override fun leave(leaderMain1: MainModel) = adaptee.leaveMainMeta(leaderMain1)

    override fun visit(leaderRoot1: RootModel): Return = defaultVisitReturn
    override fun leave(leaderRoot1: RootModel) {}

    override fun visit(leaderEntity1: EntityModel): Return {
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

    override fun leave(leaderEntity1: EntityModel) {
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

    override fun visit(leaderField1: SingleFieldModel): Return = defaultVisitReturn
    override fun leave(leaderField1: SingleFieldModel) {}

    override fun visit(leaderField1: ListFieldModel): Return {
        return when (val metaMetaName = leaderField1.getMetaAux()?.fullName) {
            "/tree_ware_meta_model.main/field/association" -> defaultVisitReturn
            else -> throw IllegalStateException("Illegal metaMetaName $metaMetaName")
        }
    }

    override fun leave(leaderField1: ListFieldModel) {
        return when (val metaMetaName = leaderField1.getMetaAux()?.fullName) {
            "/tree_ware_meta_model.main/field/association" -> Unit
            else -> throw IllegalStateException("Illegal metaMetaName $metaMetaName")
        }
    }

    override fun visit(leaderField1: SetFieldModel): Return {
        return when (val metaMetaName = leaderField1.getMetaAux()?.fullName) {
            "/tree_ware_meta_model.main/meta_model/packages" -> defaultVisitReturn
            "/tree_ware_meta_model.main/package/enumerations" -> defaultVisitReturn
            "/tree_ware_meta_model.main/enumeration/values" -> defaultVisitReturn
            "/tree_ware_meta_model.main/package/entities" -> defaultVisitReturn
            "/tree_ware_meta_model.main/entity/fields" -> defaultVisitReturn
            else -> throw IllegalStateException("Illegal metaMetaName $metaMetaName")
        }
    }

    override fun leave(leaderField1: SetFieldModel) {}

    // Values

    override fun visit(leaderValue1: PrimitiveModel): Return = defaultVisitReturn
    override fun leave(leaderValue1: PrimitiveModel) {}

    override fun visit(leaderValue1: AliasModel): Return {
        throw IllegalStateException("visit AliasModel")
    }

    override fun leave(leaderValue1: AliasModel) {
        throw IllegalStateException("leave AliasModel")
    }

    override fun visit(leaderValue1: Password1wayModel): Return {
        throw IllegalStateException("visit Password1WayModel")
    }

    override fun leave(leaderValue1: Password1wayModel) {
        throw IllegalStateException("leave Password1WayModel")
    }

    override fun visit(leaderValue1: Password2wayModel): Return {
        throw IllegalStateException("visit Password2WayModel")
    }

    override fun leave(leaderValue1: Password2wayModel) {
        throw IllegalStateException("leave Password2WayModel")
    }

    override fun visit(leaderValue1: EnumerationModel): Return = defaultVisitReturn
    override fun leave(leaderValue1: EnumerationModel) {}

    override fun visit(leaderValue1: AssociationModel): Return {
        throw IllegalStateException("visit AssociationModel")
    }

    override fun leave(leaderValue1: AssociationModel) {
        throw IllegalStateException("leave AssociationModel")
    }

    // Sub-values

    override fun visit(leaderEntityKeys1: EntityKeysModel): Return {
        throw IllegalStateException("visit EntityKeysModel")
    }

    override fun leave(leaderEntityKeys1: EntityKeysModel) {
        throw IllegalStateException("leave EntityKeysModel")
    }
}
