package org.tree_ware.model.core

import org.tree_ware.schema.core.*
import org.tree_ware.schema.visitor.AbstractSchemaVisitor

fun newMutableModel(schema: ElementSchema, parent: MutableElementModel?): MutableElementModel {
    val newMutableModelVisitor = MutableModelFactoryVisitor(parent)
    schema.traverse(newMutableModelVisitor)
    return newMutableModelVisitor.newModel
}

class MutableModelFactoryVisitor(private val parent: MutableElementModel?) : AbstractSchemaVisitor() {
    // TODO(deepak-nulu): a non-traversing dispatch() method & visitor with template return type
    // to avoid the following state variable
    var newModel: MutableElementModel
        get() = _newModel ?: throw IllegalStateException("Element has not been set")
        internal set(value) {
            _newModel = value
        }
    private var _newModel: MutableElementModel? = null

    override fun visit(schema: Schema): SchemaTraversalAction {
        newModel = MutableModel(schema)
        return SchemaTraversalAction.CONTINUE
    }

    override fun visit(root: RootSchema): SchemaTraversalAction {
        val rootParent = parent as MutableModel
        newModel = MutableRootModel(root, rootParent)
        return SchemaTraversalAction.CONTINUE
    }

    override fun visit(entity: EntitySchema): SchemaTraversalAction {
        val entityParent = parent as MutableFieldModel
        newModel = MutableEntityModel(entity, entityParent)
        return SchemaTraversalAction.CONTINUE
    }

    // Fields

    override fun visit(primitiveField: PrimitiveFieldSchema): SchemaTraversalAction {
        val fieldParent = parent as MutableBaseEntityModel
        newModel =
            if (primitiveField.multiplicity.isList()) MutablePrimitiveListFieldModel(primitiveField, fieldParent)
            else MutablePrimitiveFieldModel(primitiveField, fieldParent)
        return SchemaTraversalAction.CONTINUE
    }

    override fun visit(aliasField: AliasFieldSchema): SchemaTraversalAction {
        val fieldParent = parent as MutableBaseEntityModel
        newModel =
            if (aliasField.multiplicity.isList()) MutableAliasListFieldModel(aliasField, fieldParent)
            else MutableAliasFieldModel(aliasField, fieldParent)
        return SchemaTraversalAction.CONTINUE
    }

    override fun visit(enumerationField: EnumerationFieldSchema): SchemaTraversalAction {
        val fieldParent = parent as MutableBaseEntityModel
        newModel =
            if (enumerationField.multiplicity.isList()) MutableEnumerationListFieldModel(enumerationField, fieldParent)
            else MutableEnumerationFieldModel(enumerationField, fieldParent)
        return SchemaTraversalAction.CONTINUE
    }

    override fun visit(associationField: AssociationFieldSchema): SchemaTraversalAction {
        val fieldParent = parent as MutableBaseEntityModel
        newModel =
            if (associationField.multiplicity.isList()) MutableAssociationListFieldModel(associationField, fieldParent)
            else MutableAssociationFieldModel(associationField, fieldParent)
        return SchemaTraversalAction.CONTINUE
    }

    override fun visit(compositionField: CompositionFieldSchema): SchemaTraversalAction {
        val fieldParent = parent as MutableBaseEntityModel
        newModel =
            if (compositionField.multiplicity.isList()) MutableCompositionListFieldModel(compositionField, fieldParent)
            else MutableCompositionFieldModel(compositionField, fieldParent)
        return SchemaTraversalAction.CONTINUE
    }
}