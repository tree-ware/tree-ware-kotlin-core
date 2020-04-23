package org.tree_ware.model.visitor

import org.tree_ware.model.core.*
import org.tree_ware.schema.core.*

abstract class AbstractMutableModelVisitor : MutableModelVisitor {
    override fun mutableVisit(element: MutableElementModel): Boolean {
        return true
    }

    override fun mutableLeave(element: MutableElementModel) {}

    override fun mutableVisit(model: MutableModel): Boolean {
        return true
    }

    override fun mutableLeave(model: MutableModel) {}

    override fun mutableVisit(baseEntity: MutableBaseEntityModel): Boolean {
        return true
    }

    override fun mutableLeave(baseEntity: MutableBaseEntityModel) {}

    override fun mutableVisit(root: MutableRootModel): Boolean {
        return true
    }

    override fun mutableLeave(root: MutableRootModel) {}

    override fun mutableVisit(entity: MutableEntityModel): Boolean {
        return true
    }

    override fun mutableLeave(entity: MutableEntityModel) {}

    override fun mutableVisit(field: MutableFieldModel): Boolean {
        return true
    }

    override fun mutableLeave(field: MutableFieldModel) {}

    // Scalar fields

    override fun mutableVisit(field: MutableScalarFieldModel): Boolean {
        return true
    }

    override fun mutableLeave(field: MutableScalarFieldModel) {}

    override fun mutableVisit(field: MutablePrimitiveFieldModel): Boolean {
        return true
    }

    override fun mutableLeave(field: MutablePrimitiveFieldModel) {}

    override fun mutableVisit(field: MutableAliasFieldModel): Boolean {
        return true
    }

    override fun mutableLeave(field: MutableAliasFieldModel) {}

    override fun mutableVisit(field: MutableEnumerationFieldModel): Boolean {
        return true
    }

    override fun mutableLeave(field: MutableEnumerationFieldModel) {}

    override fun mutableVisit(field: MutableAssociationFieldModel): Boolean {
        return true
    }

    override fun mutableLeave(field: MutableAssociationFieldModel) {}

    override fun mutableVisit(field: MutableCompositionFieldModel): Boolean {
        return true
    }

    override fun mutableLeave(field: MutableCompositionFieldModel) {}

    // List fields

    override fun mutableVisit(field: MutableListFieldModel): Boolean {
        return true
    }

    override fun mutableLeave(field: MutableListFieldModel) {}

    override fun mutableVisit(field: MutablePrimitiveListFieldModel): Boolean {
        return true
    }

    override fun mutableLeave(field: MutablePrimitiveListFieldModel) {}

    override fun mutableVisit(field: MutableAliasListFieldModel): Boolean {
        return true
    }

    override fun mutableLeave(field: MutableAliasListFieldModel) {}

    override fun mutableVisit(field: MutableEnumerationListFieldModel): Boolean {
        return true
    }

    override fun mutableLeave(field: MutableEnumerationListFieldModel) {}

    override fun mutableVisit(field: MutableAssociationListFieldModel): Boolean {
        return true
    }

    override fun mutableLeave(field: MutableAssociationListFieldModel) {}

    override fun mutableVisit(field: MutableCompositionListFieldModel): Boolean {
        return true
    }

    override fun mutableLeave(field: MutableCompositionListFieldModel) {}

    // Field values

    override fun mutableVisit(value: Any, fieldSchema: PrimitiveFieldSchema): Boolean {
        return true
    }

    override fun mutableLeave(value: Any, fieldSchema: PrimitiveFieldSchema) {}

    override fun mutableVisit(value: Any, fieldSchema: AliasFieldSchema): Boolean {
        return true
    }

    override fun mutableLeave(value: Any, fieldSchema: AliasFieldSchema) {}

    override fun mutableVisit(value: EnumerationValueSchema, fieldSchema: EnumerationFieldSchema): Boolean {
        return true
    }

    override fun mutableLeave(value: EnumerationValueSchema, fieldSchema: EnumerationFieldSchema) {}

    override fun mutableVisit(value: MutableAssociationValueModel, fieldSchema: AssociationFieldSchema): Boolean {
        return true
    }

    override fun mutableLeave(value: MutableAssociationValueModel, fieldSchema: AssociationFieldSchema) {}

    override fun mutableVisit(entityKeys: MutableEntityKeysModel): Boolean {
        return true
    }

    override fun mutableLeave(entityKeys: MutableEntityKeysModel) {}
}