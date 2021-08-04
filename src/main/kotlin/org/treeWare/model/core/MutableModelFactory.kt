package org.treeWare.model.core

import org.treeWare.schema.core.*
import org.treeWare.schema.visitor.AbstractSchemaVisitor

fun <Aux> newMutableModel(schema: ElementSchema, parent: MutableElementModel<Aux>?): MutableElementModel<Aux> {
    val newMutableModelVisitor = MutableModelFactoryVisitor(parent)
    return schema.dispatch(newMutableModelVisitor) ?: throw IllegalStateException("Unable to create mutable model")
}

private class MutableModelFactoryVisitor<Aux>(
    private val parent: MutableElementModel<Aux>?
) : AbstractSchemaVisitor<MutableElementModel<Aux>?>(null) {
    override fun visit(schema: Schema): MutableElementModel<Aux> {
        return MutableModel(schema)
    }

    override fun visit(root: RootSchema): MutableElementModel<Aux> {
        val rootParent = parent as MutableModel
        return MutableRootModel(root, rootParent)
    }

    override fun visit(entity: EntitySchema): MutableElementModel<Aux> {
        val entityParent = parent as MutableFieldModel
        return MutableEntityModel(entity, entityParent)
    }

    // Fields

    override fun visit(primitiveField: PrimitiveFieldSchema): MutableElementModel<Aux> {
        val fieldParent = parent as MutableBaseEntityModel
        val isList = primitiveField.multiplicity.isList()
        return if (isList) MutableListFieldModel(primitiveField, fieldParent)
        else MutableSingleFieldModel(primitiveField, fieldParent)
    }

    override fun visit(aliasField: AliasFieldSchema): MutableElementModel<Aux> {
        val fieldParent = parent as MutableBaseEntityModel
        val isList = aliasField.multiplicity.isList()
        return if (isList) MutableListFieldModel(aliasField, fieldParent)
        else MutableSingleFieldModel(aliasField, fieldParent)
    }

    override fun visit(enumerationField: EnumerationFieldSchema): MutableElementModel<Aux> {
        val fieldParent = parent as MutableBaseEntityModel
        val isList = enumerationField.multiplicity.isList()
        return if (isList) MutableListFieldModel(enumerationField, fieldParent)
        else MutableSingleFieldModel(enumerationField, fieldParent)
    }

    override fun visit(associationField: AssociationFieldSchema): MutableElementModel<Aux> {
        val fieldParent = parent as MutableBaseEntityModel
        val isList = associationField.multiplicity.isList()
        return if (isList) MutableListFieldModel(associationField, fieldParent)
        else MutableSingleFieldModel(associationField, fieldParent)
    }

    override fun visit(compositionField: CompositionFieldSchema): MutableElementModel<Aux> {
        val fieldParent = parent as MutableBaseEntityModel
        val isList = compositionField.multiplicity.isList()
        return if (isList) MutableListFieldModel(compositionField, fieldParent)
        else MutableSingleFieldModel(compositionField, fieldParent)
    }
}
