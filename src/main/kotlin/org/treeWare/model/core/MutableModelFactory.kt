package org.treeWare.model.core

import org.treeWare.schema.core.*
import org.treeWare.schema.visitor.AbstractSchemaVisitor

fun <Aux> newMutableModel(
    schema: ElementSchema,
    meta: ElementModel<Resolved>?,
    parent: MutableElementModel<Aux>?
): MutableElementModel<Aux> {
    val newMutableModelVisitor = MutableModelFactoryVisitor(meta, parent)
    return schema.dispatch(newMutableModelVisitor) ?: throw IllegalStateException("Unable to create mutable model")
}

private class MutableModelFactoryVisitor<Aux>(
    private val meta: ElementModel<Resolved>?,
    private val parent: MutableElementModel<Aux>?
) : AbstractSchemaVisitor<MutableElementModel<Aux>?>(null) {
    override fun visit(schema: Schema): MutableElementModel<Aux> {
        val mainMeta = meta as Model<Resolved>?
        return MutableModel(schema, mainMeta)
    }

    override fun visit(root: RootSchema): MutableElementModel<Aux> {
        val rootMeta = meta as EntityModel<Resolved>?
        val rootParent = parent as MutableModel
        return MutableRootModel(root, rootMeta, rootParent)
    }

    override fun visit(entity: EntitySchema): MutableElementModel<Aux> {
        val entityMeta = meta as EntityModel<Resolved>?
        val entityParent = parent as MutableFieldModel
        return MutableEntityModel(entity, entityMeta, entityParent)
    }

    // Fields

    override fun visit(primitiveField: PrimitiveFieldSchema): MutableElementModel<Aux> {
        val fieldMeta = meta as EntityModel<Resolved>?
        val fieldParent = parent as MutableBaseEntityModel
        val isList = primitiveField.multiplicity.isList()
        return if (isList) MutableListFieldModel(primitiveField, fieldMeta, fieldParent)
        else MutableSingleFieldModel(primitiveField, fieldMeta, fieldParent)
    }

    override fun visit(aliasField: AliasFieldSchema): MutableElementModel<Aux> {
        val fieldMeta = meta as EntityModel<Resolved>?
        val fieldParent = parent as MutableBaseEntityModel
        val isList = aliasField.multiplicity.isList()
        return if (isList) MutableListFieldModel(aliasField, fieldMeta, fieldParent)
        else MutableSingleFieldModel(aliasField, fieldMeta, fieldParent)
    }

    override fun visit(enumerationField: EnumerationFieldSchema): MutableElementModel<Aux> {
        val fieldMeta = meta as EntityModel<Resolved>?
        val fieldParent = parent as MutableBaseEntityModel
        val isList = enumerationField.multiplicity.isList()
        return if (isList) MutableListFieldModel(enumerationField, fieldMeta, fieldParent)
        else MutableSingleFieldModel(enumerationField, fieldMeta, fieldParent)
    }

    override fun visit(associationField: AssociationFieldSchema): MutableElementModel<Aux> {
        val fieldMeta = meta as EntityModel<Resolved>?
        val fieldParent = parent as MutableBaseEntityModel
        val isList = associationField.multiplicity.isList()
        return if (isList) MutableListFieldModel(associationField, fieldMeta, fieldParent)
        else MutableSingleFieldModel(associationField, fieldMeta, fieldParent)
    }

    override fun visit(compositionField: CompositionFieldSchema): MutableElementModel<Aux> {
        val fieldMeta = meta as EntityModel<Resolved>?
        val fieldParent = parent as MutableBaseEntityModel
        val isList = compositionField.multiplicity.isList()
        return if (isList) MutableListFieldModel(compositionField, fieldMeta, fieldParent)
        else MutableSingleFieldModel(compositionField, fieldMeta, fieldParent)
    }
}
