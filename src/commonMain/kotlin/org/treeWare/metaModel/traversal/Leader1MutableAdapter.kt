package org.treeWare.metaModel.traversal

import org.treeWare.model.core.*
import org.treeWare.model.traversal.Leader1MutableModelVisitor

class Leader1MutableAdapter<Return>(
    private val adaptee: Leader1MutableMetaModelVisitor<Return>,
    private val defaultVisitReturn: Return
) : Leader1MutableModelVisitor<Return> {
    override fun visitMutableEntity(leaderEntity1: MutableEntityModel): Return =
        when (val metaMetaName = leaderEntity1.getMetaResolved()?.fullName) {
            "/org.tree_ware.meta_model.main/meta_model" -> adaptee.visitMetaModel(leaderEntity1)
            "/org.tree_ware.meta_model.main/version" -> adaptee.visitVersionMeta(leaderEntity1)
            "/org.tree_ware.meta_model.main/root" -> adaptee.visitRootMeta(leaderEntity1)
            "/org.tree_ware.meta_model.main/package" -> adaptee.visitPackageMeta(leaderEntity1)
            "/org.tree_ware.meta_model.main/enumeration" -> adaptee.visitEnumerationMeta(leaderEntity1)
            "/org.tree_ware.meta_model.main/enumeration_value" -> adaptee.visitEnumerationValueMeta(leaderEntity1)
            "/org.tree_ware.meta_model.main/entity" -> adaptee.visitEntityMeta(leaderEntity1)
            "/org.tree_ware.meta_model.main/field" -> adaptee.visitFieldMeta(leaderEntity1)
            "/org.tree_ware.meta_model.main/unique" -> defaultVisitReturn
            "/org.tree_ware.meta_model.main/unique_field" -> defaultVisitReturn
            "/org.tree_ware.meta_model.main/entity_info" -> defaultVisitReturn
            "/org.tree_ware.meta_model.main/enumeration_info" -> defaultVisitReturn
            "/org.tree_ware.meta_model.main/exists_if_clause" -> defaultVisitReturn
            else -> throw IllegalStateException("Illegal metaMetaName $metaMetaName")
        }

    override fun leaveMutableEntity(leaderEntity1: MutableEntityModel) =
        when (val metaMetaName = leaderEntity1.getMetaResolved()?.fullName) {
            "/org.tree_ware.meta_model.main/meta_model" -> adaptee.leaveMetaModel(leaderEntity1)
            "/org.tree_ware.meta_model.main/version" -> adaptee.leaveVersionMeta(leaderEntity1)
            "/org.tree_ware.meta_model.main/root" -> adaptee.leaveRootMeta(leaderEntity1)
            "/org.tree_ware.meta_model.main/package" -> adaptee.leavePackageMeta(leaderEntity1)
            "/org.tree_ware.meta_model.main/enumeration" -> adaptee.leaveEnumerationMeta(leaderEntity1)
            "/org.tree_ware.meta_model.main/enumeration_value" -> adaptee.leaveEnumerationValueMeta(leaderEntity1)
            "/org.tree_ware.meta_model.main/entity" -> adaptee.leaveEntityMeta(leaderEntity1)
            "/org.tree_ware.meta_model.main/field" -> adaptee.leaveFieldMeta(leaderEntity1)
            "/org.tree_ware.meta_model.main/unique" -> Unit
            "/org.tree_ware.meta_model.main/unique_field" -> Unit
            "/org.tree_ware.meta_model.main/entity_info" -> Unit
            "/org.tree_ware.meta_model.main/enumeration_info" -> Unit
            "/org.tree_ware.meta_model.main/exists_if_clause" -> Unit
            else -> throw IllegalStateException("Illegal metaMetaName $metaMetaName")
        }

    // Fields

    override fun visitMutableSingleField(leaderField1: MutableSingleFieldModel): Return = defaultVisitReturn
    override fun leaveMutableSingleField(leaderField1: MutableSingleFieldModel) {}

    override fun visitMutableSetField(leaderField1: MutableSetFieldModel): Return {
        return when (val metaMetaName = leaderField1.getMetaResolved()?.fullName) {
            "/org.tree_ware.meta_model.main/meta_model/packages" -> defaultVisitReturn
            "/org.tree_ware.meta_model.main/package/enumerations" -> defaultVisitReturn
            "/org.tree_ware.meta_model.main/enumeration/values" -> defaultVisitReturn
            "/org.tree_ware.meta_model.main/package/entities" -> defaultVisitReturn
            "/org.tree_ware.meta_model.main/entity/fields" -> defaultVisitReturn
            "/org.tree_ware.meta_model.main/entity/uniques" -> defaultVisitReturn
            "/org.tree_ware.meta_model.main/unique/fields" -> defaultVisitReturn
            else -> throw IllegalStateException("Illegal metaMetaName $metaMetaName")
        }
    }

    override fun leaveMutableSetField(leaderField1: MutableSetFieldModel) {}

    // Values

    override fun visitMutablePrimitive(leaderValue1: MutablePrimitiveModel): Return = defaultVisitReturn
    override fun leaveMutablePrimitive(leaderValue1: MutablePrimitiveModel) {}

    override fun visitMutableAlias(leaderValue1: MutableAliasModel): Return {
        throw IllegalStateException("visit AliasModel")
    }

    override fun leaveMutableAlias(leaderValue1: MutableAliasModel) {
        throw IllegalStateException("leave AliasModel")
    }

    override fun visitMutablePassword1way(leaderValue1: MutablePassword1wayModel): Return {
        throw IllegalStateException("visit Password1WayModel")
    }

    override fun leaveMutablePassword1way(leaderValue1: MutablePassword1wayModel) {
        throw IllegalStateException("leave Password1WayModel")
    }

    override fun visitMutablePassword2way(leaderValue1: MutablePassword2wayModel): Return {
        throw IllegalStateException("visit Password2WayModel")
    }

    override fun leaveMutablePassword2way(leaderValue1: MutablePassword2wayModel) {
        throw IllegalStateException("leave Password2WayModel")
    }

    override fun visitMutableEnumeration(leaderValue1: MutableEnumerationModel): Return = defaultVisitReturn
    override fun leaveMutableEnumeration(leaderValue1: MutableEnumerationModel) {}

    override fun visitMutableAssociation(leaderValue1: MutableAssociationModel): Return {
        throw IllegalStateException("visit AssociationModel")
    }

    override fun leaveMutableAssociation(leaderValue1: MutableAssociationModel) {
        throw IllegalStateException("leave AssociationModel")
    }
}