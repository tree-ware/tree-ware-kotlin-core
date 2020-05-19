package org.tree_ware.model.visitor

import org.tree_ware.model.core.*
import org.tree_ware.schema.core.*

abstract class AbstractMutableModelVisitor<T>(private val defaultVisitReturn: T) : MutableModelVisitor<T> {
    override fun mutableVisit(element: MutableElementModel): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(element: MutableElementModel) {}

    override fun mutableVisit(model: MutableModel): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(model: MutableModel) {}

    override fun mutableVisit(baseEntity: MutableBaseEntityModel): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(baseEntity: MutableBaseEntityModel) {}

    override fun mutableVisit(root: MutableRootModel): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(root: MutableRootModel) {}

    override fun mutableVisit(entity: MutableEntityModel): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(entity: MutableEntityModel) {}

    override fun mutableVisit(field: MutableFieldModel): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(field: MutableFieldModel) {}

    // Scalar fields

    override fun mutableVisit(field: MutableScalarFieldModel): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(field: MutableScalarFieldModel) {}

    override fun mutableVisit(field: MutablePrimitiveFieldModel): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(field: MutablePrimitiveFieldModel) {}

    override fun mutableVisit(field: MutableAliasFieldModel): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(field: MutableAliasFieldModel) {}

    override fun mutableVisit(field: MutableEnumerationFieldModel): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(field: MutableEnumerationFieldModel) {}

    override fun mutableVisit(field: MutableAssociationFieldModel): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(field: MutableAssociationFieldModel) {}

    override fun mutableVisit(field: MutableCompositionFieldModel): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(field: MutableCompositionFieldModel) {}

    // List fields

    override fun mutableVisit(field: MutableListFieldModel): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(field: MutableListFieldModel) {}

    override fun mutableVisit(field: MutablePrimitiveListFieldModel): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(field: MutablePrimitiveListFieldModel) {}

    override fun mutableVisit(field: MutableAliasListFieldModel): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(field: MutableAliasListFieldModel) {}

    override fun mutableVisit(field: MutableEnumerationListFieldModel): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(field: MutableEnumerationListFieldModel) {}

    override fun mutableVisit(field: MutableAssociationListFieldModel): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(field: MutableAssociationListFieldModel) {}

    override fun mutableVisit(field: MutableCompositionListFieldModel): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(field: MutableCompositionListFieldModel) {}

    // Field values

    override fun mutableVisit(value: Any, fieldSchema: PrimitiveFieldSchema): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(value: Any, fieldSchema: PrimitiveFieldSchema) {}

    override fun mutableVisit(value: Any, fieldSchema: AliasFieldSchema): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(value: Any, fieldSchema: AliasFieldSchema) {}

    override fun mutableVisit(value: EnumerationValueSchema, fieldSchema: EnumerationFieldSchema): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(value: EnumerationValueSchema, fieldSchema: EnumerationFieldSchema) {}

    override fun mutableVisit(value: MutableAssociationValueModel, fieldSchema: AssociationFieldSchema): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(value: MutableAssociationValueModel, fieldSchema: AssociationFieldSchema) {}

    override fun mutableVisit(entityKeys: MutableEntityKeysModel): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(entityKeys: MutableEntityKeysModel) {}
}