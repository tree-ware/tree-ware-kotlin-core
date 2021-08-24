package org.treeWare.model.core

import org.treeWare.metaModel.getResolvedEntityMeta
import org.treeWare.schema.core.*
import org.treeWare.schema.visitor.AbstractSchemaVisitor

fun <Aux> newMutableValueModel(
    schema: ElementSchema,
    meta: ElementModel<Resolved>?,
    parent: MutableFieldModel<Aux>
): MutableElementModel<Aux> {
    val newMutableValueModelVisitor = MutableValueModelFactoryVisitor(meta, parent)
    return schema.dispatch(newMutableValueModelVisitor)
        ?: throw IllegalStateException("Unable to create mutable value model for schema $schema")
}

private class MutableValueModelFactoryVisitor<Aux>(
    private val meta: ElementModel<Resolved>?,
    private val parent: MutableFieldModel<Aux>
) : AbstractSchemaVisitor<MutableElementModel<Aux>?>(null) {
    override fun visit(entity: EntitySchema): MutableElementModel<Aux> {
        val entityMeta = meta as EntityModel<Resolved>?
        return MutableEntityModel(entity, entityMeta, parent)
    }

    // Values

    override fun visit(primitiveField: PrimitiveFieldSchema): MutableElementModel<Aux> =
        MutablePrimitiveModel(primitiveField, parent)

    override fun visit(aliasField: AliasFieldSchema): MutableElementModel<Aux> =
        MutableAliasModel(aliasField, parent)

    override fun visit(enumerationField: EnumerationFieldSchema): MutableElementModel<Aux> =
        MutableEnumerationModel(enumerationField, parent)

    override fun visit(associationField: AssociationFieldSchema): MutableElementModel<Aux> =
        MutableAssociationModel(associationField, parent)

    override fun visit(compositionField: CompositionFieldSchema): MutableElementModel<Aux> {
        val fieldMeta = meta as EntityModel<Resolved>?
        val resolvedEntityMeta = fieldMeta?.let { getResolvedEntityMeta(fieldMeta) }
        return MutableEntityModel(compositionField.resolvedEntity, resolvedEntityMeta, parent)
    }
}
