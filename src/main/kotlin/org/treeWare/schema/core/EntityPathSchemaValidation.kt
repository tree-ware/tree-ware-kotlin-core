package org.treeWare.schema.core

/** Validates the specified entity-path-schema.
 * Returns a list of errors. Returns an empty list if there are no errors.
 *
 * Side-effects:
 * 1. `keyEntities` and `resolvedEntity` are resolved
 */
fun validate(schema: MutableEntityPathSchema, root: RootSchema, fieldFullName: String): List<String> {
    var entity: EntitySchema = try {
        root.resolvedEntity
    } catch (e: IllegalStateException) {
        return listOf("Root has not been resolved")
    }
    val errors = mutableListOf<String>()
    schema.entityPath.forEachIndexed { index, pathElement ->
        val nextEntityResult = if (index == 0) {
            getFirstEntity(pathElement, root, fieldFullName)
        } else {
            getNextEntity(pathElement, entity, fieldFullName)
        }
        nextEntityResult.errors?.also { errors.addAll(it) }
        // Abort entity-path resolution if there is no resolved-entity.
        // The lack of a resolved-entity is reported by a different validation visitor.
        entity = nextEntityResult.entity ?: return errors
        schema.pathEntities.add(entity)
        if (entity.fields.any { it.isKey }) {
            schema.keyPath.add(pathElement)
            schema.keyEntities.add(entity)
        }
    }
    schema.resolvedEntity = entity
    return errors
}

private class NextEntityResult(val entity: EntitySchema?, val errors: List<String>? = null)

private fun getFirstEntity(
    pathElement: String,
    root: RootSchema,
    fieldFullName: String?
): NextEntityResult {
    // First path-element must match root.
    return if (pathElement == root.name) {
        NextEntityResult(root.resolvedEntity)
    } else {
        NextEntityResult(null, listOf("Invalid association path root: $fieldFullName"))
    }
}

private fun getNextEntity(
    pathElement: String,
    previousEntity: EntitySchema,
    fieldFullName: String?
): NextEntityResult {
    val field = previousEntity.fields.find { it.name == pathElement }
    return if (field is MutableCompositionFieldSchema) {
        NextEntityResult(field._resolvedEntity)
    } else {
        NextEntityResult(null, listOf("Invalid association path: $fieldFullName"))
    }
}
