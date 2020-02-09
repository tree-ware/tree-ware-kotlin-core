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
    private var currentPackage: PackageSchema? = null

    private fun getFullName(namedElement: NamedElementSchema): String {
        val packageName = currentPackage?.name
                ?: throw IllegalStateException("Schema element ${namedElement.name} is not in a package")
        return "${packageName}.${namedElement.name}"
    }

    override fun mutableVisit(pkg: MutablePackageSchema): Boolean {
        currentPackage = pkg
        return true
    }

    override fun mutableVisit(alias: MutableAliasSchema): Boolean {
        aliases[getFullName(alias)] = alias
        return true
    }

    override fun mutableVisit(enumeration: MutableEnumerationSchema): Boolean {
        enumerations[getFullName(enumeration)] = enumeration
        return true
    }

    override fun mutableVisit(entity: MutableEntitySchema): Boolean {
        entities[getFullName(entity)] = entity
        return true
    }
}
