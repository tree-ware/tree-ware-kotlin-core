package org.treeWare.metaModel.validation

import org.apache.logging.log4j.LogManager
import org.treeWare.model.core.Model
import org.treeWare.model.core.Resolved

/** Validates the specified meta-model.
 * Returns a list of errors. Returns an empty list if there are no errors.
 *
 * Side effects:
 * 1. full-names are set for named elements
 * 2. Non-primitive field types are resolved
 */
fun validate(metaModel: Model<Resolved>, logFullNames: Boolean = false): List<String> {
    val logger = LogManager.getLogger()
    fun logErrors(errors: List<String>) = errors.forEach { logger.error(it) }

    val structureErrors = validateStructure(metaModel)
    if (structureErrors.isNotEmpty()) {
        logErrors(structureErrors)
        return structureErrors
    }

    // Set full-names for named elements in the packages.
    val nameErrors = validateNames(metaModel, logFullNames)
    logErrors(nameErrors)

    return nameErrors
}
