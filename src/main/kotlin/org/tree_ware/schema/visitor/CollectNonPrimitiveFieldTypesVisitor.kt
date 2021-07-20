package org.tree_ware.schema.visitor

import org.tree_ware.schema.core.MutableAliasSchema
import org.tree_ware.schema.core.MutableEntitySchema
import org.tree_ware.schema.core.MutableEnumerationSchema
import org.tree_ware.common.traversal.TraversalAction

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
) : AbstractMutableSchemaVisitor<TraversalAction>(TraversalAction.CONTINUE) {
    override fun mutableVisit(alias: MutableAliasSchema): TraversalAction {
        val fullName = alias.fullName
        aliases[fullName] = alias
        return TraversalAction.CONTINUE
    }

    override fun mutableVisit(enumeration: MutableEnumerationSchema): TraversalAction {
        val fullName = enumeration.fullName
        enumerations[fullName] = enumeration
        return TraversalAction.CONTINUE
    }

    override fun mutableVisit(entity: MutableEntitySchema): TraversalAction {
        val fullName = entity.fullName
        entities[fullName] = entity
        return TraversalAction.CONTINUE
    }
}
