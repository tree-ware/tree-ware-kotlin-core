package org.tree_ware.schema.visitor

import org.tree_ware.schema.core.MutableAssociationFieldSchema
import org.tree_ware.schema.core.MutableCompositionFieldSchema
import org.tree_ware.schema.core.MutableEntitySchema
import org.tree_ware.schema.core.MutableRootSchema

class ResolveAssociationsVisitor(private val root: MutableRootSchema?) : AbstractMutableSchemaValidatingVisitor() {
    override fun mutableVisit(associationField: MutableAssociationFieldSchema): Boolean {
        // Set resolvedEntity
        if (associationField.entityPath.size < 2) {
            _errors.add("Association path is too short: ${associationField.fullName}")
            return true
        }
        root?.also { resolveEntityPath(root, associationField) }
        return true
    }

    private fun resolveEntityPath(root: MutableRootSchema, associationField: MutableAssociationFieldSchema) {
        // Abort entity-path resolution if there is no root resolved-entity.
        // The lack of a resolved-entity is reported by a different validation visitor.
        var entity: MutableEntitySchema = root._resolvedEntity ?: return
        associationField.entityPath.forEachIndexed { index, pathElement ->
            val nextEntity = if (index == 0) {
                getFirstEntity(pathElement, root, associationField.fullName)
            } else {
                getNextEntity(pathElement, entity, associationField.fullName)
            }
            // Abort entity-path resolution if there is no resolved-entity.
            // The lack of a resolved-entity is reported by a different validation visitor.
            entity = nextEntity ?: return
            if (entity.fields.any { it.isKey }) associationField.keyEntities.add(entity)
        }
        associationField.resolvedEntity = entity
        if (associationField.multiplicity.isList() && associationField.keyEntities.isEmpty()) _errors.add(
            "Association list entity path does not have keys: ${associationField.fullName}"
        )
    }

    private fun getFirstEntity(
        pathElement: String,
        root: MutableRootSchema,
        associationFullName: String?
    ): MutableEntitySchema? {
        // First path-element must match root.
        return if (pathElement == root.name) {
            root._resolvedEntity
        } else {
            _errors.add("Invalid association path root: $associationFullName")
            null
        }
    }

    private fun getNextEntity(
        pathElement: String,
        previousEntity: MutableEntitySchema,
        associationFullName: String?
    ): MutableEntitySchema? {
        val field = previousEntity.fields.find { it.name == pathElement }
        return if (field is MutableCompositionFieldSchema) {
            field._resolvedEntity
        } else {
            _errors.add("Invalid association path: $associationFullName")
            null
        }
    }
}
