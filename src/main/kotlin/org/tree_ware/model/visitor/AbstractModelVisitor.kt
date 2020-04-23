package org.tree_ware.model.visitor

import org.tree_ware.core.schema.*
import org.tree_ware.model.core.*

abstract class AbstractModelVisitor : ModelVisitor {
    override fun visit(element: ElementModel): Boolean {
        return true
    }

    override fun leave(element: ElementModel) {}

    override fun visit(model: Model): Boolean {
        return true
    }

    override fun leave(model: Model) {}

    override fun visit(baseEntity: BaseEntityModel): Boolean {
        return true
    }

    override fun leave(baseEntity: BaseEntityModel) {}

    override fun visit(root: RootModel): Boolean {
        return true
    }

    override fun leave(root: RootModel) {}

    override fun visit(entity: EntityModel): Boolean {
        return true
    }

    override fun leave(entity: EntityModel) {}

    override fun visit(field: FieldModel): Boolean {
        return true
    }

    override fun leave(field: FieldModel) {}

    // Scalar fields

    override fun visit(field: ScalarFieldModel): Boolean {
        return true
    }

    override fun leave(field: ScalarFieldModel) {}

    override fun visit(field: PrimitiveFieldModel): Boolean {
        return true
    }

    override fun leave(field: PrimitiveFieldModel) {}

    override fun visit(field: AliasFieldModel): Boolean {
        return true
    }

    override fun leave(field: AliasFieldModel) {}

    override fun visit(field: EnumerationFieldModel): Boolean {
        return true
    }

    override fun leave(field: EnumerationFieldModel) {}

    override fun visit(field: AssociationFieldModel): Boolean {
        return true
    }

    override fun leave(field: AssociationFieldModel) {}

    override fun visit(field: CompositionFieldModel): Boolean {
        return true
    }

    override fun leave(field: CompositionFieldModel) {}

    // List fields

    override fun visit(field: ListFieldModel): Boolean {
        return true
    }

    override fun leave(field: ListFieldModel) {}

    override fun visit(field: PrimitiveListFieldModel): Boolean {
        return true
    }

    override fun leave(field: PrimitiveListFieldModel) {}

    override fun visit(field: AliasListFieldModel): Boolean {
        return true
    }

    override fun leave(field: AliasListFieldModel) {}

    override fun visit(field: EnumerationListFieldModel): Boolean {
        return true
    }

    override fun leave(field: EnumerationListFieldModel) {}

    override fun visit(field: AssociationListFieldModel): Boolean {
        return true
    }

    override fun leave(field: AssociationListFieldModel) {}

    override fun visit(field: CompositionListFieldModel): Boolean {
        return true
    }

    override fun leave(field: CompositionListFieldModel) {}

    // Field values

    override fun visit(value: Any, fieldSchema: PrimitiveFieldSchema): Boolean {
        return true
    }

    override fun leave(value: Any, fieldSchema: PrimitiveFieldSchema) {}

    override fun visit(value: Any, fieldSchema: AliasFieldSchema): Boolean {
        return true
    }

    override fun leave(value: Any, fieldSchema: AliasFieldSchema) {}

    override fun visit(value: EnumerationValueSchema, fieldSchema: EnumerationFieldSchema): Boolean {
        return true
    }

    override fun leave(value: EnumerationValueSchema, fieldSchema: EnumerationFieldSchema) {}

    override fun visit(value: AssociationValueModel, fieldSchema: AssociationFieldSchema): Boolean {
        return true
    }

    override fun leave(value: AssociationValueModel, fieldSchema: AssociationFieldSchema) {}

    // NOTE: composition-values are EntityModel instances and so they don't have separate visit/leave methods.

    override fun visit(entityKeys: EntityKeysModel): Boolean {
        return true
    }

    override fun leave(entityKeys: EntityKeysModel) {}
}
