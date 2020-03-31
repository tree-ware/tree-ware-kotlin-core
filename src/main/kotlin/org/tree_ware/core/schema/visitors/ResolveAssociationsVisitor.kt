package org.tree_ware.core.schema.visitors

import org.tree_ware.core.schema.MutableAssociationFieldSchema
import org.tree_ware.core.schema.MutableCompositionFieldSchema
import org.tree_ware.core.schema.MutableEntitySchema

class ResolveAssociationsVisitor(
    private val root: MutableCompositionFieldSchema?,
    private val entities: Map<String, MutableEntitySchema>
) : AbstractMutableSchemaValidatingVisitor() {
    override fun mutableVisit(associationField: MutableAssociationFieldSchema): Boolean {
        if (associationField.entityPath.size < 2) {
            _errors.add("Association path is too short: ${associationField.fullName}")
            return true
        }
        root?.also { rootComposition ->
            var parentComposition = rootComposition
            // Follow the association's entityPath to the final composition.
            associationField.entityPath.forEachIndexed { index, pathElement ->
                getChildComposition(associationField, index, pathElement, parentComposition)?.also {
                    parentComposition = it
                } ?: return@also
            }
            parentComposition._resolvedEntity?.also { associationField.resolvedEntity = it }
        }
        return true
    }

    private fun getChildComposition(
        associationField: MutableAssociationFieldSchema,
        pathIndex: Int,
        pathElement: String,
        parentComposition: MutableCompositionFieldSchema
    ): MutableCompositionFieldSchema? {
        if (pathIndex == 0) {
            // Root case. pathElement must match root name.
            if (pathElement == parentComposition.name) {
                return parentComposition
            } else {
                _errors.add("Invalid association path root: ${associationField.fullName}")
                return null
            }
        } else {
            val parentEntity = parentComposition._resolvedEntity
            // Abort validation if the composition does not have a resolved-entity.
            // The lack of a resolved-entity is reported by a different validation visitor.
            if (parentEntity == null) return null
            else {
                val field = parentEntity.fields.find { it.name == pathElement }
                if (field is MutableCompositionFieldSchema) {
                    return field
                } else {
                    _errors.add("Invalid association path: ${associationField.fullName}")
                    return null
                }
            }
        }
    }
}
