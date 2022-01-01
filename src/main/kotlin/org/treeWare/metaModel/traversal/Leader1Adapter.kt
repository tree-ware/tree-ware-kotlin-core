package org.treeWare.metaModel.traversal

import org.treeWare.model.core.*
import org.treeWare.model.traversal.Leader1ModelVisitor

class Leader1Adapter<Return>(
    private val adaptee: Leader1MetaModelVisitor<Return>,
    private val defaultVisitReturn: Return
) : Leader1ModelVisitor<Return> {
    override fun visitMain(leaderMain1: MainModel): Return = adaptee.visitMainMeta(leaderMain1)
    override fun leaveMain(leaderMain1: MainModel) = adaptee.leaveMainMeta(leaderMain1)

    override fun visitRoot(leaderRoot1: RootModel): Return = defaultVisitReturn
    override fun leaveRoot(leaderRoot1: RootModel) {}

    override fun visitEntity(leaderEntity1: EntityModel): Return {
        return when (val metaMetaName = leaderEntity1.getMetaResolved()?.fullName) {
            "/tree_ware_meta_model.main/meta_model" -> defaultVisitReturn
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

    override fun leaveEntity(leaderEntity1: EntityModel) {
        return when (val metaMetaName = leaderEntity1.getMetaResolved()?.fullName) {
            "/tree_ware_meta_model.main/meta_model" -> Unit
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

    override fun visitSingleField(leaderField1: SingleFieldModel): Return = defaultVisitReturn
    override fun leaveSingleField(leaderField1: SingleFieldModel) {}

    override fun visitListField(leaderField1: ListFieldModel): Return {
        return when (val metaMetaName = leaderField1.getMetaResolved()?.fullName) {
            "/tree_ware_meta_model.main/field/association" -> defaultVisitReturn
            else -> throw IllegalStateException("Illegal metaMetaName $metaMetaName")
        }
    }

    override fun leaveListField(leaderField1: ListFieldModel) {
        return when (val metaMetaName = leaderField1.getMetaResolved()?.fullName) {
            "/tree_ware_meta_model.main/field/association" -> Unit
            else -> throw IllegalStateException("Illegal metaMetaName $metaMetaName")
        }
    }

    override fun visitSetField(leaderField1: SetFieldModel): Return {
        return when (val metaMetaName = leaderField1.getMetaResolved()?.fullName) {
            "/tree_ware_meta_model.main/meta_model/packages" -> defaultVisitReturn
            "/tree_ware_meta_model.main/package/enumerations" -> defaultVisitReturn
            "/tree_ware_meta_model.main/enumeration/values" -> defaultVisitReturn
            "/tree_ware_meta_model.main/package/entities" -> defaultVisitReturn
            "/tree_ware_meta_model.main/entity/fields" -> defaultVisitReturn
            else -> throw IllegalStateException("Illegal metaMetaName $metaMetaName")
        }
    }

    override fun leaveSetField(leaderField1: SetFieldModel) {}

    // Values

    override fun visitPrimitive(leaderValue1: PrimitiveModel): Return = defaultVisitReturn
    override fun leavePrimitive(leaderValue1: PrimitiveModel) {}

    override fun visitAlias(leaderValue1: AliasModel): Return {
        throw IllegalStateException("visit AliasModel")
    }

    override fun leaveAlias(leaderValue1: AliasModel) {
        throw IllegalStateException("leave AliasModel")
    }

    override fun visitPassword1way(leaderValue1: Password1wayModel): Return {
        throw IllegalStateException("visit Password1WayModel")
    }

    override fun leavePassword1way(leaderValue1: Password1wayModel) {
        throw IllegalStateException("leave Password1WayModel")
    }

    override fun visitPassword2way(leaderValue1: Password2wayModel): Return {
        throw IllegalStateException("visit Password2WayModel")
    }

    override fun leavePassword2way(leaderValue1: Password2wayModel) {
        throw IllegalStateException("leave Password2WayModel")
    }

    override fun visitEnumeration(leaderValue1: EnumerationModel): Return = defaultVisitReturn
    override fun leaveEnumeration(leaderValue1: EnumerationModel) {}

    override fun visitAssociation(leaderValue1: AssociationModel): Return {
        throw IllegalStateException("visit AssociationModel")
    }

    override fun leaveAssociation(leaderValue1: AssociationModel) {
        throw IllegalStateException("leave AssociationModel")
    }

    // Sub-values

    override fun visitEntityKeys(leaderEntityKeys1: EntityKeysModel): Return {
        throw IllegalStateException("visit EntityKeysModel")
    }

    override fun leaveEntityKeys(leaderEntityKeys1: EntityKeysModel) {
        throw IllegalStateException("leave EntityKeysModel")
    }
}
