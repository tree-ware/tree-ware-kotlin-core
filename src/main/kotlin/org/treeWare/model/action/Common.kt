package org.treeWare.model.action

import org.treeWare.model.core.EntityModel
import org.treeWare.model.core.FieldModel
import org.treeWare.model.core.SingleFieldModel
import org.treeWare.schema.core.CompositionFieldSchema

data class GenericField<Value>(val name: String, val value: Value)
data class GenericEntity<Value>(val name: String, val fields: List<GenericField<Value>>)

internal fun <FieldValue> getKeyFields(
    entityName: String,
    keyField: FieldModel<Unit>,
    valueGetter: (keyField: FieldModel<Unit>) -> FieldValue
): List<GenericField<FieldValue>> {
    if (keyField is SingleFieldModel<Unit>) {
        val value = keyField.value
        if (value is EntityModel<Unit>) {
            return value.fields.filter { it.schema.isKey }.map { nestedKey ->
                val keyName = "\"/$entityName/${keyField.schema.name}/${nestedKey.schema.name}\""
                val keyValue = valueGetter(nestedKey)
                GenericField(keyName, keyValue)
            }
        }
    }

    val keyName = "\"/$entityName/${keyField.schema.name}\""
    val keyValue = valueGetter(keyField)
    return listOf(GenericField(keyName, keyValue))
}

fun isCompositionField(field: FieldModel<*>): Boolean =
    !field.schema.multiplicity.isList() && field.schema is CompositionFieldSchema

fun isCompositionListField(field: FieldModel<*>): Boolean =
    field.schema.multiplicity.isList() && field.schema is CompositionFieldSchema
