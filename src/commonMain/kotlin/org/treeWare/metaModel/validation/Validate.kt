package org.treeWare.metaModel.validation

import org.treeWare.model.core.Cipher
import org.treeWare.model.core.EntityFactory
import org.treeWare.model.core.Hasher
import org.treeWare.model.core.MutableEntityModel

/**
 * Validates the specified meta-model.
 * Returns a list of errors. Returns an empty list if there are no errors.
 *
 * Side effects:
 * 1. ResolvedVersionAux is set on `meta` if the version can be resolved
 * 2. full-names are set for named elements
 * 3. Non-primitive field types are resolved
 * 4. Key fields are memoized in field-number sorted order
 * 5. Hasher and cipher instances are passed to password meta
 */
fun validate(
    meta: MutableEntityModel,
    hasher: Hasher?,
    cipher: Cipher?,
    rootEntityFactory: EntityFactory?,
    logFullNames: Boolean = false,
    mandatoryFieldNumbers: Boolean = true
): List<String> {
    val structureErrors = validateStructure(meta)
    if (structureErrors.isNotEmpty()) return structureErrors

    // Set full-names for named elements in the packages.
    val nameErrors = validateNames(meta, logFullNames)
    val numberErrors = if (mandatoryFieldNumbers) validateNumbers(meta) else emptyList()

    // Get non-primitive field values to help resolve non-primitive fields.
    // NOTE: because of "forward-references", this has to be collected from all
    // packages before non-primitive fields can be resolved.
    val nonPrimitiveTypes = getNonPrimitiveTypes(meta)
    val nonPrimitiveErrors = resolveNonPrimitiveTypes(meta, hasher, cipher, rootEntityFactory, nonPrimitiveTypes)

    val existsIfErrors = validateExistsIf(meta)
    val granularityErrors = validateGranularity(meta)

    return listOf(nameErrors, numberErrors, nonPrimitiveErrors, existsIfErrors, granularityErrors).flatten()
}