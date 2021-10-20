package org.treeWare.model.action

import org.treeWare.metaModel.getMetaName
import org.treeWare.metaModel.isCompositionFieldMeta
import org.treeWare.metaModel.isKeyFieldMeta
import org.treeWare.metaModel.isSetFieldMeta
import org.treeWare.model.core.EntityModel
import org.treeWare.model.core.FieldModel
import org.treeWare.model.core.SingleFieldModel

data class GenericField<Value>(val name: String, val value: Value)
data class GenericEntity<Value>(val name: String, val fields: List<GenericField<Value>>)

internal fun <FieldValue> getKeyFields(
    entityName: String,
    keyField: FieldModel,
    valueGetter: (keyField: FieldModel) -> FieldValue
): List<GenericField<FieldValue>> {
    if (keyField is SingleFieldModel) {
        val value = keyField.value
        if (value is EntityModel) {
            return value.fields.values.filter { isKeyFieldMeta(it.meta) }.map { nestedKey ->
                val keyName = "\"/$entityName/${getMetaName(keyField.meta)}/${getMetaName(nestedKey.meta)}\""
                val keyValue = valueGetter(nestedKey)
                GenericField(keyName, keyValue)
            }
        }
    }

    val keyName = "\"/$entityName/${getMetaName(keyField.meta)}\""
    val keyValue = valueGetter(keyField)
    return listOf(GenericField(keyName, keyValue))
}

fun isCompositionField(field: FieldModel): Boolean =
    !isSetFieldMeta(field.meta) && isCompositionFieldMeta(field.meta)

fun isCompositionSetField(field: FieldModel): Boolean =
    isSetFieldMeta(field.meta) && isCompositionFieldMeta(field.meta)
