package org.treeWare.schema.core

import org.apache.logging.log4j.LogManager
import org.treeWare.schema.visitor.*

/** Validates the specified schema.
 * Returns a list of errors. Returns an empty list if there are no errors.
 *
 * Side-effects:
 * 1. full-names are set for named elements
 * 2. Non-primitive field types are resolved
 */
fun validate(schema: MutableSchema, logFullNames: Boolean = false): List<String> {
    val logger = LogManager.getLogger()

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

    // TODO(deepak-nulu): Combine the following visitors with a visitor-combinator.
    schema.mutableTraverse(setFullNameVisitor)
    schema.mutableTraverse(validationVisitor)
    schema.mutableTraverse(collectNonPrimitiveFieldTypesVisitor)
    if (logFullNames) setFullNameVisitor.fullNames.forEach { logger.debug("element fullName: $it") }

    // Resolve non-primitive field types, except associations.
    // Associations can be resolved only after compositions are resolved.
    val resolveNonPrimitiveFieldTypesVisitor =
        ResolveNonPrimitiveFieldTypesVisitor(aliases, enumerations, entities)

    schema.mutableTraverse(resolveNonPrimitiveFieldTypesVisitor)

    // Resolve associations.
    val resolveAssociationsVisitor = ResolveAssociationsVisitor(schema._root)

    schema.mutableTraverse(resolveAssociationsVisitor)

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

    if (allErrors.isNotEmpty()) allErrors.forEach { logger.error(it) }

    return allErrors
}
