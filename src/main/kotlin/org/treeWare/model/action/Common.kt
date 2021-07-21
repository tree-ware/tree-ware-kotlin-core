package org.treeWare.model.action

import org.treeWare.model.core.CompositionFieldModel
import org.treeWare.model.core.FieldModel

data class GenericField<Value>(val name: String, val value: Value)
data class GenericEntity<Value>(val name: String, val fields: List<GenericField<Value>>)

internal fun <FieldValue> getKeyFields(
    entityName: String,
    keyField: FieldModel<Unit>,
    valueGetter: (keyField: FieldModel<Unit>) -> FieldValue
): List<GenericField<FieldValue>> =
    if (keyField is CompositionFieldModel) {
        keyField.value.fields.filter { it.schema.isKey }.map { nestedKey ->
            val keyName = "\"/$entityName/${keyField.schema.name}/${nestedKey.schema.name}\""
            val keyValue = valueGetter(nestedKey)
            GenericField(keyName, keyValue)
        }
    } else {
        val keyName = "\"/$entityName/${keyField.schema.name}\""
        val keyValue = valueGetter(keyField)
        listOf(GenericField(keyName, keyValue))
    }
