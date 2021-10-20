package org.treeWare.model.core

import org.treeWare.metaModel.FieldType
import org.treeWare.metaModel.getFieldTypeMeta

fun newMutableValueModel(
    fieldMeta: EntityModel?,
    parent: MutableFieldModel
): MutableElementModel {
    if (fieldMeta == null) throw IllegalStateException("fieldMeta is null when creating mutable value model")
    return when (getFieldTypeMeta(fieldMeta)) {
        FieldType.PASSWORD1WAY -> MutablePassword1wayModel(parent)
        FieldType.PASSWORD2WAY -> MutablePassword2wayModel(parent)
        FieldType.ENUMERATION -> MutableEnumerationModel(parent)
        FieldType.ASSOCIATION -> MutableAssociationModel(parent)
        FieldType.COMPOSITION -> MutableEntityModel(fieldMeta.getAux<Resolved>(RESOLVED_AUX)?.compositionMeta, parent)
        else -> MutablePrimitiveModel(parent)
    }
}
