package org.treeWare.metaModel.validation

import org.lighthousegames.logging.KmLog
import org.treeWare.model.core.Cipher
import org.treeWare.model.core.Hasher
import org.treeWare.model.core.MutableMainModel

/** Validates the specified meta-model.
 * Returns a list of errors. Returns an empty list if there are no errors.
 *
 * Side effects:
 * 1. full-names are set for named elements
 * 2. Non-primitive field types are resolved
 * 3. Hasher and cipher instances are passed to password meta.
 */
fun validate(
    mainMeta: MutableMainModel,
    hasher: Hasher?,
    cipher: Cipher?,
    logFullNames: Boolean = false
): List<String> {
    val logger = KmLog()

    // TODO(cleanup): this function should return errors without logging it.
    fun logErrors(errors: List<String>) = errors.forEach { logger.error { it } }

    val structureErrors = validateStructure(mainMeta)
    if (structureErrors.isNotEmpty()) {
        logErrors(structureErrors)
        return structureErrors
    }

    // Set full-names for named elements in the packages.
    val nameErrors = validateNames(mainMeta, logFullNames)

    // Get non-primitive field values to help resolve non-primitive fields.
    // NOTE: because of "forward-references", this has to be collected from all
    // packages before non-primitive fields can be resolved.
    val nonPrimitiveTypes = getNonPrimitiveTypes(mainMeta)

    // Resolve non-primitive field types.
    // Associations can be resolved only after compositions are resolved.
    val nonPrimitiveErrors = resolveNonPrimitiveTypes(mainMeta, hasher, cipher, nonPrimitiveTypes)
    val associationErrors = resolveAssociations(mainMeta)

    val allErrors = listOf(nameErrors, nonPrimitiveErrors, associationErrors).flatten()
    logErrors(allErrors)
    return allErrors
}
