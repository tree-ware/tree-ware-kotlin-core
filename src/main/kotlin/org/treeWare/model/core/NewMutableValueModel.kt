package org.treeWare.model.core

import org.treeWare.metaModel.getFieldTypeMeta
import org.treeWare.schema.core.*

fun <Aux> newMutableValueModel(
    schema: ElementSchema,
    fieldMeta: EntityModel<Resolved>?,
    parent: MutableFieldModel<Aux>
): MutableElementModel<Aux> {
    if (fieldMeta == null) throw IllegalStateException("fieldMeta is null when creating mutable value model")
    return when (getFieldTypeMeta(fieldMeta)) {
        "enumeration" -> MutableEnumerationModel(schema as EnumerationFieldSchema, parent)
        "association" -> MutableAssociationModel(schema as AssociationFieldSchema, parent)
        "entity" -> MutableEntityModel(
            schema as? EntitySchema ?: (schema as CompositionFieldSchema).resolvedEntity,
            fieldMeta.aux?.entityMeta,
            parent
        )
        else -> MutablePrimitiveModel(schema as PrimitiveFieldSchema, parent)
    }
}
