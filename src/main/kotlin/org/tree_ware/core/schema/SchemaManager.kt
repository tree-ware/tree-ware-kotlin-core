package org.tree_ware.core.schema

import org.apache.logging.log4j.LogManager
import org.tree_ware.core.codec.common.SchemaEncoder
import org.tree_ware.core.codec.dot.DotSchemaEncoder
import org.tree_ware.core.codec.json.JsonSchemaEncoder
import org.tree_ware.core.schema.visitors.*
import java.io.Writer

class SchemaManager {
    val root: RootSchema
        get() = _root ?: throw IllegalStateException("root has not been set")
    private var _root: MutableRootSchema? = null

    /** Adds packages to the schema if there are no errors.
     * Returns the list of errors. Returns an empty list if there are no errors.
     *
     * Side-effects:
     * 1. full-names are set for named elements
     * 2. Non-primitive field types are resolved
     */
    fun addPackages(packages: List<MutablePackageSchema>): List<String> {
        if (schema.packages.isNotEmpty()) {
            return listOf("Adding packages a second time is not yet supported")
        }

        // Set full-names for named elements in the packages
        val setFullNameVisitor = SetFullNameVisitor()

        // Collect non-primitive field types to help resolve non-primitive fields.
        // NOTE: because of "forward-references", this has to be collected from all packages before we can resolve
        // the types in such fields.
        val aliases = mutableMapOf<String, MutableAliasSchema>()
        val enumerations = mutableMapOf<String, MutableEnumerationSchema>()
        val entities = mutableMapOf<String, MutableEntitySchema>()
        val collectNonPrimitiveFieldTypesVisitor = CollectNonPrimitiveFieldTypesVisitor(aliases, enumerations, entities)

        val validationVisitor = ValidationVisitor()

        packages.forEach { pkg ->
            // TODO(deepak-nulu): Combine the following visitors with a visitor-combinator.
            pkg.mutableAccept(setFullNameVisitor)
            pkg.mutableAccept(validationVisitor)
            pkg.mutableAccept(collectNonPrimitiveFieldTypesVisitor)
        }
        setFullNameVisitor.fullNames.forEach { logger.debug("element fullName: $it") }
        _root = validationVisitor.root

        // Resolve non-primitive field types, except associations.
        // Associations can be resolved only after compositions are resolved.
        val resolveNonPrimitiveFieldTypesVisitor =
            ResolveNonPrimitiveFieldTypesVisitor(aliases, enumerations, entities)

        packages.forEach { pkg ->
            pkg.mutableAccept(resolveNonPrimitiveFieldTypesVisitor)
        }

        // Resolve associations.
        val resolveAssociationsVisitor = ResolveAssociationsVisitor(_root)

        packages.forEach { pkg ->
            pkg.mutableAccept(resolveAssociationsVisitor)
        }

        val validationVisitors: List<SchemaValidatingVisitor> = listOf(
            setFullNameVisitor,
            validationVisitor,
            resolveNonPrimitiveFieldTypesVisitor,
            resolveAssociationsVisitor
        )
        val allErrors = validationVisitors.flatMap {
            it.finalizeValidation()
            it.errors
        }

        if (allErrors.isEmpty()) schema.packages = packages
        else allErrors.forEach { logger.error(it) }

        return allErrors
    }

    fun encodeJson(writer: Writer, prettyPrint: Boolean = false): Boolean {
        return encode(JsonSchemaEncoder(writer, prettyPrint))
    }

    fun encodeDot(writer: Writer): Boolean {
        return encode(DotSchemaEncoder(writer))
    }

    fun encode(encoder: SchemaEncoder): Boolean {
        return encoder.encode(schema)
    }

    val schema = MutableSchema().also { it.objectId = "schema" }

    private val logger = LogManager.getLogger()
}
