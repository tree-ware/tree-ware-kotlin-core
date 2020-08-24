package org.tree_ware.model.action

import org.tree_ware.model.core.BaseEntityModel
import org.tree_ware.model.core.FieldModel
import org.tree_ware.schema.core.EntitySchema

class GenericEntityPath<FieldValue>(private val valueGetter: (field: FieldModel<*>) -> FieldValue) {
    val value: List<GenericEntity<FieldValue>> get() = _value

    fun pushEntity(entityName: String, leaderEntity: BaseEntityModel<Unit>, leaderEntitySchema: EntitySchema) {
        val keys = leaderEntitySchema.fields.filter { it.isKey }.flatMap { keySchema ->
            val keyField = leaderEntity.getField(keySchema.name) ?: return@flatMap listOf<GenericField<FieldValue>>()
            getKeyFields(entityName, keyField) { valueGetter(it) }
        }
        // TODO(deepak-nulu): error if all keys are not present
        _value.add(GenericEntity(entityName, keys))
    }

    fun popEntity() {
        _value.removeAt(_value.size - 1)
    }

    private val _value = mutableListOf<GenericEntity<FieldValue>>()
}
