package org.treeWare.metaModel.validation

import io.github.z4kn4fein.semver.toVersionOrNull
import org.treeWare.metaModel.*
import org.treeWare.metaModel.aux.ResolvedVersionAux
import org.treeWare.metaModel.aux.setResolvedVersionAux
import org.treeWare.model.core.ElementModel
import org.treeWare.model.core.EntityModel
import org.treeWare.model.core.getOptionalSingleString
import org.treeWare.model.core.getSingleString

// TODO(deepak-nulu): replace with a generic validate function which validates a model against its meta-model.

/**
 * Validates the structure of the meta-model.
 * Returns a list of errors. Returns an empty list if there are no errors.
 *
 * Side effects:
 * 1. ResolvedVersionAux is set on `meta` if the version can be resolved
 */
fun validateStructure(meta: EntityModel) = listOf(
    validateSingleStringField(meta, "name", "Meta-model"),
    validateSingleStringField(meta, "package", "Meta-model"),
    validateVersion(meta),
    validateEntityInfo(meta, "Meta-model", "root"),
    validatePackages(meta)
).flatten()

fun validateVersion(meta: EntityModel): List<String> {
    val versionMeta = runCatching { getVersionMeta(meta) }.getOrNull() ?: return listOf("Version is missing")
    val semanticVersionString = runCatching { getSingleString(versionMeta, "semantic") }.getOrNull()
        ?: return listOf("Semantic version is missing")
    val semanticVersion = semanticVersionString.toVersionOrNull()
        ?: return listOf("Strictly invalid semantic version: $semanticVersionString")
    val name = getOptionalSingleString(versionMeta, "name")
    setResolvedVersionAux(meta, ResolvedVersionAux(semanticVersion, name))
    return emptyList()
}

private fun validateRoot(meta: EntityModel): List<String> {
    val rootMeta = runCatching { getRootMeta(meta) }.getOrNull() ?: return listOf("Root is missing")
    return listOf(
        validateEntityInfo(rootMeta, "Root", "composition")
    ).flatten()
}

private fun validatePackages(meta: EntityModel): List<String> {
    val packagesMeta =
        runCatching { getPackagesMeta(meta) }.getOrNull() ?: return listOf("Packages are missing")
    return packagesMeta.values.flatMapIndexed { index, packageMeta -> validatePackage(packageMeta, index) }
}

private fun validatePackage(packageElementMeta: ElementModel, packageIndex: Int): List<String> {
    val packageId = getPackageId(packageIndex)
    val packageMeta = packageElementMeta as? EntityModel
        ?: return listOf("$packageId is not an EntityModel. It is: ${packageElementMeta::class.simpleName}")
    return listOf(
        validateSingleStringField(packageMeta, "name", packageId),
        validateEnumerations(packageMeta, packageId),
        validateEntities(packageMeta, packageId),
    ).flatten()
}

private fun validateEnumerations(packageMeta: EntityModel, packageId: String): List<String> {
    val enumerationsMeta = getEnumerationsMeta(packageMeta)
    return enumerationsMeta?.values?.flatMapIndexed { enumerationIndex, enumerationMeta ->
        validateEnumeration(
            enumerationMeta,
            packageId,
            enumerationIndex
        )
    } ?: emptyList()
}

private fun validateEnumeration(
    enumerationElementMeta: ElementModel,
    packageId: String,
    enumerationIndex: Int
): List<String> {
    val enumerationId = getId(packageId, "enumeration", enumerationIndex)
    val enumerationMeta = enumerationElementMeta as? EntityModel
        ?: return listOf("$enumerationId is not an EntityModel. It is: ${enumerationElementMeta::class.simpleName}")
    return listOf(
        validateSingleStringField(enumerationMeta, "name", enumerationId),
        validateEnumerationValues(enumerationMeta, enumerationId)
    ).flatten()
}

private fun validateEnumerationValues(
    enumerationMeta: EntityModel,
    enumerationId: String
): List<String> {
    val enumerationValuesMeta = runCatching { getEnumerationValuesMeta(enumerationMeta) }.getOrNull()
        ?: return listOf("$enumerationId values are missing")
    if (enumerationValuesMeta.values.isEmpty()) return listOf("$enumerationId values are empty")
    return enumerationValuesMeta.values.flatMapIndexed { valueIndex, enumerationValueMeta ->
        validateEnumerationValue(
            enumerationValueMeta,
            enumerationId,
            valueIndex
        )
    }
}

private fun validateEnumerationValue(
    enumerationValueElementMeta: ElementModel,
    enumerationId: String,
    valueIndex: Int
): List<String> {
    val id = getId(enumerationId, "value", valueIndex)
    val enumerationValueMeta = enumerationValueElementMeta as? EntityModel
        ?: return listOf("$id is not an EntityModel. It is: ${enumerationValueElementMeta::class.simpleName}")
    return validateSingleStringField(enumerationValueMeta, "name", id)
}

private fun validateEntities(packageMeta: EntityModel, packageId: String): List<String> {
    val entitiesMeta = getEntitiesMeta(packageMeta)
    return entitiesMeta?.values?.flatMapIndexed { entityIndex, entityMeta ->
        validateEntity(
            entityMeta,
            packageId,
            entityIndex
        )
    } ?: emptyList()
}

private fun validateEntity(
    entityElementMeta: ElementModel,
    packageId: String,
    entityIndex: Int
): List<String> {
    val entityId = getId(packageId, "entity", entityIndex)
    val entityMeta = entityElementMeta as? EntityModel
        ?: return listOf("$entityId is not an EntityModel. It is: ${entityElementMeta::class.simpleName}")
    return listOf(
        validateSingleStringField(entityMeta, "name", entityId),
        validateFields(entityMeta, entityId),
        validateUniques(entityMeta, entityId)
    ).flatten()
}

private fun validateFields(entityMeta: EntityModel, entityId: String): List<String> {
    val fieldsMeta = runCatching { getFieldsMeta(entityMeta) }.getOrNull()
        ?: return listOf("$entityId fields are missing")
    if (fieldsMeta.values.isEmpty()) return listOf("$entityId fields are empty")
    return fieldsMeta.values.flatMapIndexed { fieldIndex, fieldMeta -> validateField(fieldMeta, entityId, fieldIndex) }
}

private fun validateField(
    fieldElementMeta: ElementModel,
    entityId: String,
    fieldIndex: Int
): List<String> {
    val fieldId = getId(entityId, "field", fieldIndex)
    val fieldMeta = fieldElementMeta as? EntityModel
        ?: return listOf("$fieldId is not an EntityModel. It is: ${fieldElementMeta::class.simpleName}")
    return listOf(
        validateSingleStringField(fieldMeta, "name", fieldId),
        validateFieldType(fieldMeta, fieldId),
        validateFieldMultiplicity(fieldMeta, fieldId),
        validateFieldIsKey(fieldMeta, fieldId),
        validateConstraints(fieldMeta, fieldId)
    ).flatten()
}

private fun validateFieldType(fieldMeta: EntityModel, fieldId: String): List<String> {
    val fieldTypeMeta = runCatching { getFieldTypeMeta(fieldMeta) }.getOrNull()
        ?: return listOf("$fieldId type is missing")
    return if (!FieldType.values().contains(fieldTypeMeta)) {
        listOf("$fieldId has an invalid field type: ${fieldTypeMeta.name.lowercase()}")
    } else when (fieldTypeMeta) {
        FieldType.ENUMERATION -> validateEnumerationInfo(fieldMeta, fieldId)
        FieldType.ASSOCIATION -> validateEntityInfo(fieldMeta, fieldId, "association")
        FieldType.COMPOSITION -> validateEntityInfo(fieldMeta, fieldId, "composition")
        else -> emptyList()
    }
}

private fun validateUniques(entityMeta: EntityModel, entityId: String): List<String> {
    val uniquesMeta = getUniquesMeta(entityMeta) ?: return emptyList()
    return uniquesMeta.values.flatMapIndexed { uniqueIndex, uniqueMeta ->
        validateUnique(entityMeta, uniqueMeta, entityId, uniqueIndex)
    }
}

fun validateUnique(
    entityMeta: EntityModel,
    uniqueElementMeta: ElementModel,
    entityId: String,
    uniqueIndex: Int
): List<String> {
    val uniqueId = getId(entityId, "unique", uniqueIndex)
    val uniqueMeta = uniqueElementMeta as? EntityModel
        ?: return listOf("$uniqueId is not an EntityModel. It is: ${uniqueElementMeta::class.simpleName}")
    val fieldsMeta = runCatching { getFieldsMeta(uniqueMeta) }.getOrNull()
        ?: return listOf("$uniqueId fields are missing")
    if (fieldsMeta.values.isEmpty()) return listOf("$uniqueId fields are empty")
    return fieldsMeta.values.flatMapIndexed { uniqueFieldIndex, uniqueFieldMeta ->
        validateUniqueField(entityMeta, uniqueFieldMeta, uniqueId, uniqueFieldIndex)
    }
}

private fun validateUniqueField(
    entityMeta: EntityModel,
    uniqueFieldElementMeta: ElementModel,
    uniqueId: String,
    uniqueFieldIndex: Int
): List<String> {
    // Get the specified field name.
    val uniqueFieldId = getId(uniqueId, "field", uniqueFieldIndex)
    val uniqueFieldMeta = uniqueFieldElementMeta as? EntityModel
        ?: return listOf("$uniqueFieldId is not an EntityModel. It is: ${uniqueFieldElementMeta::class.simpleName}")
    val uniqueFieldName = runCatching { getSingleString(uniqueFieldMeta, "name") }.getOrNull()
        ?: return listOf("$uniqueFieldId 'name' string field not found")

    // Validate that the specified field exists and is of a valid type.
    val entityField = runCatching { getFieldMeta(entityMeta, uniqueFieldName) }.getOrNull()
        ?: return listOf("$uniqueFieldId not found: $uniqueFieldName")
    if (isCollectionFieldMeta(entityField)) return listOf("Collection fields are not supported in uniques: $uniqueFieldId: $uniqueFieldName")
    if (isCompositionFieldMeta(entityField)) return listOf("Composition fields are not supported in uniques: $uniqueFieldId: $uniqueFieldName")
    return emptyList()
}

private fun getClassName(value: Any?): String? = value?.let { it::class.simpleName }

private fun validateEnumerationInfo(fieldMeta: EntityModel, fieldId: String): List<String> {
    val infoId = "$fieldId enumeration info"
    val enumerationInfoMeta = runCatching { getEnumerationInfoMeta(fieldMeta) }.getOrNull()
        ?: return listOf("$infoId is missing")
    return listOf(
        validateSingleStringField(enumerationInfoMeta, "name", infoId),
        validateSingleStringField(enumerationInfoMeta, "package", infoId),
    ).flatten()
}

private fun validateEntityInfo(fieldMeta: EntityModel, fieldId: String, entityInfoFor: String): List<String> {
    val infoId = "$fieldId $entityInfoFor info"
    val entityInfoMeta = runCatching { getEntityInfoMeta(fieldMeta, entityInfoFor) }.getOrNull()
        ?: return listOf("$infoId is missing")
    return listOf(
        validateSingleStringField(entityInfoMeta, "entity", infoId),
        validateSingleStringField(entityInfoMeta, "package", infoId),
    ).flatten()
}

private fun validateFieldMultiplicity(fieldMeta: EntityModel, fieldId: String): List<String> {
    val multiplicityMeta = getMultiplicityMeta(fieldMeta)
    if (!Multiplicity.values().contains(multiplicityMeta)) {
        return listOf("$fieldId has an invalid multiplicity: ${multiplicityMeta.name.lowercase()}")
    }
    val fieldTypeMeta = runCatching { getFieldTypeMeta(fieldMeta) }.getOrNull() ?: return emptyList()
    return when (multiplicityMeta) {
        Multiplicity.SET ->
            if (fieldTypeMeta == FieldType.COMPOSITION) emptyList()
            else listOf("$fieldId cannot be a 'set'. Only compositions can be sets.")
        else -> emptyList()
    }
}

private fun validateFieldIsKey(fieldMeta: EntityModel, fieldId: String): List<String> {
    if (!isKeyFieldMeta(fieldMeta)) return emptyList()
    val errors = mutableListOf<String>()
    if (getMultiplicityMeta(fieldMeta) != Multiplicity.REQUIRED) errors.add("$fieldId is a key but not defined as required")
    when (runCatching { getFieldTypeMeta(fieldMeta) }.getOrNull()) {
        FieldType.PASSWORD1WAY, FieldType.PASSWORD2WAY -> errors.add("$fieldId is a password field and they cannot be keys")
        FieldType.ASSOCIATION -> errors.add("$fieldId is an association field and they cannot be keys")
        else -> Unit
    }
    return errors
}

private fun validateConstraints(fieldMeta: EntityModel, fieldId: String): List<String> {
    val fieldTypeMeta = runCatching { getFieldTypeMeta(fieldMeta) }.getOrNull() ?: return emptyList()
    val errors = mutableListOf<String>()
    val minSize = getMinSizeConstraint(fieldMeta)
    if (minSize != null && fieldTypeMeta != FieldType.STRING) errors.add("$fieldId cannot have min_size string constraint")
    val maxSize = getMaxSizeConstraint(fieldMeta)
    if (maxSize != null && fieldTypeMeta != FieldType.STRING) errors.add("$fieldId cannot have max_size string constraint")
    val regex = getRegexConstraint(fieldMeta)
    if (regex != null && fieldTypeMeta != FieldType.STRING) errors.add("$fieldId cannot have regex string constraint")
    return errors
}

// Helpers

private fun validateSingleStringField(meta: EntityModel, fieldName: String, id: String): List<String> =
    when (runCatching { getSingleString(meta, fieldName) }.getOrNull()) {
        null -> listOf("$id '$fieldName' is missing")
        else -> emptyList()
    }

private fun getPackageId(packageIndex: Int) = "Package $packageIndex"

private fun getId(parentId: String, childType: String, childIndex: Int) = "$parentId $childType $childIndex"