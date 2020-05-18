package org.tree_ware.model.core

import org.tree_ware.schema.core.*
import org.tree_ware.schema.visitor.AbstractSchemaVisitor

fun newMutableModel(schema: ElementSchema, parent: MutableElementModel?): MutableElementModel {
    val newMutableModelVisitor = MutableModelFactoryVisitor(parent)
    return schema.dispatch(newMutableModelVisitor) ?: throw IllegalStateException("Unable to create mutable model")
}

class MutableModelFactoryVisitor(
    private val parent: MutableElementModel?
) : AbstractSchemaVisitor<MutableElementModel?>(null) {
    override fun visit(schema: Schema): MutableElementModel? {
        return MutableModel(schema)
    }

    override fun visit(root: RootSchema): MutableElementModel? {
        val rootParent = parent as MutableModel
        return MutableRootModel(root, rootParent)
    }

    override fun visit(entity: EntitySchema): MutableElementModel? {
        val entityParent = parent as MutableFieldModel
        return MutableEntityModel(entity, entityParent)
    }

    // Fields

    override fun visit(primitiveField: PrimitiveFieldSchema): MutableElementModel? {
        val fieldParent = parent as MutableBaseEntityModel
        val isList = primitiveField.multiplicity.isList()
        return if (isList) MutablePrimitiveListFieldModel(primitiveField, fieldParent)
        else MutablePrimitiveFieldModel(primitiveField, fieldParent)
    }

    override fun visit(aliasField: AliasFieldSchema): MutableElementModel? {
        val fieldParent = parent as MutableBaseEntityModel
        val isList = aliasField.multiplicity.isList()
        return if (isList) MutableAliasListFieldModel(aliasField, fieldParent)
        else MutableAliasFieldModel(aliasField, fieldParent)
    }

    override fun visit(enumerationField: EnumerationFieldSchema): MutableElementModel? {
        val fieldParent = parent as MutableBaseEntityModel
        val isList = enumerationField.multiplicity.isList()
        return if (isList) MutableEnumerationListFieldModel(enumerationField, fieldParent)
        else MutableEnumerationFieldModel(enumerationField, fieldParent)
    }

    override fun visit(associationField: AssociationFieldSchema): MutableElementModel? {
        val fieldParent = parent as MutableBaseEntityModel
        val isList = associationField.multiplicity.isList()
        return if (isList) MutableAssociationListFieldModel(associationField, fieldParent)
        else MutableAssociationFieldModel(associationField, fieldParent)
    }

    override fun visit(compositionField: CompositionFieldSchema): MutableElementModel? {
        val fieldParent = parent as MutableBaseEntityModel
        val isList = compositionField.multiplicity.isList()
        return if (isList) MutableCompositionListFieldModel(compositionField, fieldParent)
        else MutableCompositionFieldModel(compositionField, fieldParent)
    }
}
