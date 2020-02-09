package org.tree_ware.core.schema

import org.tree_ware.core.codec.common.SchemaEncoder
import org.tree_ware.core.codec.json.JsonSchemaEncoder
import java.io.Writer

class SchemaManager {
    // TODO(deepak-nulu): remove packages as a constructor parameter of MutableSchema
    // The only way to add packages to a schema should be thru the addPackages() method
    // so that only validated packages are available in MutableSchema. Protect access
    // to the packages setter in MutableSchema so that it cannot be set any other way.

    fun addPackages(packages: List<MutablePackageSchema>) {
        if (schema.packages.isNotEmpty()) {
            // TODO(deepak-nulu): return the following as an error message
            throw UnsupportedOperationException("Adding packages a second time is not yet supported")
        }

        // Collect non-primitive field types to help resolve non-primitive fields.
        val aliases = mutableMapOf<String, MutableAliasSchema>()
        val enumerations = mutableMapOf<String, MutableEnumerationSchema>()
        val entities = mutableMapOf<String, MutableEntitySchema>()
        val collectNonPrimitiveFieldTypesVisitor = CollectNonPrimitiveFieldTypesVisitor(aliases, enumerations, entities)
        packages.forEach { it.mutableAccept(collectNonPrimitiveFieldTypesVisitor) }

        // TODO(deepak-nulu): Resolve non-primitive fields.

        schema.packages = packages
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
