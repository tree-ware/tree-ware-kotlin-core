package org.tree_ware.schema.visitor

import org.tree_ware.schema.core.MutableAliasSchema
import org.tree_ware.schema.core.MutableEntitySchema
import org.tree_ware.schema.core.MutableEnumerationSchema
import org.tree_ware.schema.core.SchemaTraversalAction

/** Collects non-primitive field types in the specified maps.
 * The fully-qualified-names of the types are mapped to the types.
 *
 * This visitor does not mutate the visited elements, but it is defined as a mutable-visitor because it needs to
 * collect mutable versions of the non-primitive types.
 */
class CollectNonPrimitiveFieldTypesVisitor(
    private val aliases: MutableMap<String, MutableAliasSchema>,
    private val enumerations: MutableMap<String, MutableEnumerationSchema>,
    private val entities: MutableMap<String, MutableEntitySchema>
) : AbstractMutableSchemaVisitor<SchemaTraversalAction>(SchemaTraversalAction.CONTINUE) {
    override fun mutableVisit(alias: MutableAliasSchema): SchemaTraversalAction {
        val fullName = alias.fullName
        aliases[fullName] = alias
        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableVisit(enumeration: MutableEnumerationSchema): SchemaTraversalAction {
        val fullName = enumeration.fullName
        enumerations[fullName] = enumeration
        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableVisit(entity: MutableEntitySchema): SchemaTraversalAction {
        val fullName = entity.fullName
        entities[fullName] = entity
        return SchemaTraversalAction.CONTINUE
    }
}
