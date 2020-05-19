package org.tree_ware.model.visitor

import org.tree_ware.model.core.*
import org.tree_ware.schema.core.*

abstract class AbstractModelVisitor<T>(private val defaultVisitReturn: T) : ModelVisitor<T> {
    override fun visit(element: ElementModel): T {
        return defaultVisitReturn
    }

    override fun leave(element: ElementModel) {}

    override fun visit(model: Model): T {
        return defaultVisitReturn
    }

    override fun leave(model: Model) {}

    override fun visit(baseEntity: BaseEntityModel): T {
        return defaultVisitReturn
    }

    override fun leave(baseEntity: BaseEntityModel) {}

    override fun visit(root: RootModel): T {
        return defaultVisitReturn
    }

    override fun leave(root: RootModel) {}

    override fun visit(entity: EntityModel): T {
        return defaultVisitReturn
    }

    override fun leave(entity: EntityModel) {}

    override fun visit(field: FieldModel): T {
        return defaultVisitReturn
    }

    override fun leave(field: FieldModel) {}

    // Scalar fields

    override fun visit(field: ScalarFieldModel): T {
        return defaultVisitReturn
    }

    override fun leave(field: ScalarFieldModel) {}

    override fun visit(field: PrimitiveFieldModel): T {
        return defaultVisitReturn
    }

    override fun leave(field: PrimitiveFieldModel) {}

    override fun visit(field: AliasFieldModel): T {
        return defaultVisitReturn
    }

    override fun leave(field: AliasFieldModel) {}

    override fun visit(field: EnumerationFieldModel): T {
        return defaultVisitReturn
    }

    override fun leave(field: EnumerationFieldModel) {}

    override fun visit(field: AssociationFieldModel): T {
        return defaultVisitReturn
    }

    override fun leave(field: AssociationFieldModel) {}

    override fun visit(field: CompositionFieldModel): T {
        return defaultVisitReturn
    }

    override fun leave(field: CompositionFieldModel) {}

    // List fields

    override fun visit(field: ListFieldModel): T {
        return defaultVisitReturn
    }

    override fun leave(field: ListFieldModel) {}

    override fun visit(field: PrimitiveListFieldModel): T {
        return defaultVisitReturn
    }

    override fun leave(field: PrimitiveListFieldModel) {}

    override fun visit(field: AliasListFieldModel): T {
        return defaultVisitReturn
    }

    override fun leave(field: AliasListFieldModel) {}

    override fun visit(field: EnumerationListFieldModel): T {
        return defaultVisitReturn
    }

    override fun leave(field: EnumerationListFieldModel) {}

    override fun visit(field: AssociationListFieldModel): T {
        return defaultVisitReturn
    }

    override fun leave(field: AssociationListFieldModel) {}

    override fun visit(field: CompositionListFieldModel): T {
        return defaultVisitReturn
    }

    override fun leave(field: CompositionListFieldModel) {}

    // Field values

    override fun visit(value: Any, fieldSchema: PrimitiveFieldSchema): T {
        return defaultVisitReturn
    }

    override fun leave(value: Any, fieldSchema: PrimitiveFieldSchema) {}

    override fun visit(value: Any, fieldSchema: AliasFieldSchema): T {
        return defaultVisitReturn
    }

    override fun leave(value: Any, fieldSchema: AliasFieldSchema) {}

    override fun visit(value: EnumerationValueSchema, fieldSchema: EnumerationFieldSchema): T {
        return defaultVisitReturn
    }

    override fun leave(value: EnumerationValueSchema, fieldSchema: EnumerationFieldSchema) {}

    override fun visit(value: AssociationValueModel, fieldSchema: AssociationFieldSchema): T {
        return defaultVisitReturn
    }

    override fun leave(value: AssociationValueModel, fieldSchema: AssociationFieldSchema) {}

    // NOTE: composition-values are EntityModel instances and so they don't have separate visit/leave methods.

    override fun visit(entityKeys: EntityKeysModel): T {
        return defaultVisitReturn
    }

    override fun leave(entityKeys: EntityKeysModel) {}
}
