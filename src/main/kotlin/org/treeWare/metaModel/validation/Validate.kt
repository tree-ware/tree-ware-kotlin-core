package org.treeWare.metaModel.validation

import org.apache.logging.log4j.LogManager
import org.treeWare.model.core.MutableModel
import org.treeWare.model.core.Resolved

/** Validates the specified meta-model.
 * Returns a list of errors. Returns an empty list if there are no errors.
 *
 * Side effects:
 * 1. full-names are set for named elements
 * 2. Non-primitive field types are resolved
 */
fun validate(mainMeta: MutableModel<Resolved>, logFullNames: Boolean = false): List<String> {
    val logger = LogManager.getLogger()
    fun logErrors(errors: List<String>) = errors.forEach { logger.error(it) }

    val structureErrors = validateStructure(mainMeta)
    if (structureErrors.isNotEmpty()) {
        logErrors(structureErrors)
        return structureErrors
    }

    // Set full-names for named elements in the packages.
    val nameErrors = validateNames(mainMeta, logFullNames)
    logErrors(nameErrors)

    // Get non-primitive field values to help resolve non-primitive fields.
    // NOTE: because of "forward-references", this has to be collected from all
    // packages before non-primitive fields can be resolved.
    val nonPrimitiveTypes = getNonPrimitiveTypes(mainMeta)

    return nameErrors
}
