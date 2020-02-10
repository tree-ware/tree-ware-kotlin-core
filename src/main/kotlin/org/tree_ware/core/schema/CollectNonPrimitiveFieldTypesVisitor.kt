package org.tree_ware.core.schema

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
) : AbstractMutableSchemaVisitor() {
    override fun mutableVisit(alias: MutableAliasSchema): Boolean {
        val fullName = alias.fullName ?: throw IllegalStateException("fullName missing for alias ${alias.name}")
        aliases[fullName] = alias
        return true
    }

    override fun mutableVisit(enumeration: MutableEnumerationSchema): Boolean {
        val fullName = enumeration.fullName
                ?: throw IllegalStateException("fullName missing for enumeration ${enumeration.name}")
        enumerations[fullName] = enumeration
        return true
    }

    override fun mutableVisit(entity: MutableEntitySchema): Boolean {
        val fullName = entity.fullName ?: throw IllegalStateException("fullName missing for entity ${entity.name}")
        entities[fullName] = entity
        return true
    }
}
