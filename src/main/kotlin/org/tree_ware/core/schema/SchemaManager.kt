package org.tree_ware.core.schema

import org.tree_ware.core.codec.common.SchemaEncoder
import org.tree_ware.core.codec.json.JsonSchemaEncoder
import java.io.Writer

class SchemaManager {
    // TODO(deepak-nulu): remove packages as a constructor parameter of MutableSchema
    // The only way to add packages to a schema should be thru the addPackages() method
    // so that only validated packages are available in MutableSchema. Protect access
    // to the packages setter in MutableSchema so that it cannot be set any other way.

    /** Adds packages to the schema if there are no errors.
     * Returns the list of errors. Returns an empty list if there are no errors.
     */
    fun addPackages(packages: List<MutablePackageSchema>): List<String> {
        if (schema.packages.isNotEmpty()) {
            // TODO(deepak-nulu): return the following as an error message
            throw UnsupportedOperationException("Adding packages a second time is not yet supported")
        }

        // Collect non-primitive field types to help resolve non-primitive fields.
        // NOTE: because of "forward-references", this has to be collected from all packages before we can resolve
        // the types in such fields.
        val aliases = mutableMapOf<String, MutableAliasSchema>()
        val enumerations = mutableMapOf<String, MutableEnumerationSchema>()
        val entities = mutableMapOf<String, MutableEntitySchema>()
        val collectNonPrimitiveFieldTypesVisitor = CollectNonPrimitiveFieldTypesVisitor(aliases, enumerations, entities)
        packages.forEach { pkg ->
            // TODO(deepak-nulu): Combine the following 2 visitors with a visitor-combinator.
            pkg.mutableAccept(SetFullNameVisitor())
            pkg.mutableAccept(collectNonPrimitiveFieldTypesVisitor)
        }

        // Resolve non-primitive field types.
        // TODO(deepak-nulu): Validate the schema.
        val resolveNonPrimitiveFieldTypesVisitor = ResolveNonPrimitiveFieldTypesVisitor(aliases, enumerations, entities)
        packages.forEach { pkg ->
            // TODO(deepak-nulu): Combine the following 2 visitors with a visitor-combinator.
            pkg.mutableAccept(resolveNonPrimitiveFieldTypesVisitor)
        }
        // TODO(deepak-nulu): include errors from validation.
        val allErrors = resolveNonPrimitiveFieldTypesVisitor.errors

        // TODO(deepak-nulu): replace println() with logger.
        if (allErrors.isEmpty()) schema.packages = packages
        else println("Errors: $allErrors")

        return allErrors
    }

    fun encodeJson(writer: Writer, prettyPrint: Boolean = false): Boolean {
        return encode(JsonSchemaEncoder(writer, prettyPrint))
    }

    fun encode(encoder: SchemaEncoder): Boolean {
        return encoder.encode(schema)
    }

    private val schema = MutableSchema()
    private val nameToElementMap = HashMap<String, MutableElementSchema>()
}
