package org.treeWare.model.util

import org.treeWare.model.core.MutableEntityModel
import org.treeWare.model.core.MutableSetFieldModel
import org.treeWare.model.operator.copy

/**
 * Merges the new entity with a matching existing entity if any, else adds the new entity to the set.
 * Fields in the new entity take precedence over fields in the existing entity.
 *
 * @return `true` if the new entity is merged into an existing entity, or `false` if it is added to the set.
 */
fun mergeAddToSet(newEntity: MutableEntityModel, setField: MutableSetFieldModel): Boolean {
    val existing = setField.getValueMatching(newEntity) as MutableEntityModel?
    return if (existing == null) {
        setField.addValue(newEntity)
        false
    } else {
        copy(newEntity, existing)
        true
    }
}