package org.treeWare.metaModel.validation

import org.treeWare.metaModel.*
import org.treeWare.model.core.*

// TODO(deepak-nulu): replace with a generic validate function which validates a model against its meta-model.

/** Validates the structure of the meta-model.
 * Returns a list of errors. Returns an empty list if there are no errors.
 *
 * Side effects: none
 */
fun validateStructure(mainMeta: MainModel) = listOf(
    validateRoot(mainMeta),
    validatePackages(mainMeta)
).flatten()

private fun validateRoot(mainMeta: MainModel): List<String> {
    val rootMeta = runCatching { getRootMeta(mainMeta) }.getOrNull() ?: return listOf("Root is missing")
    return listOf(
        validateSingleStringField(rootMeta, "name", "Root"),
        validateRootType(rootMeta),
        validateEntityInfo(rootMeta, "Root")
    ).flatten()
}

private fun validateRootType(rootMeta: EntityModel): List<String> {
    val fieldTypeMeta = runCatching { getFieldTypeMeta(rootMeta) }.getOrNull()
        ?: return listOf("Root type is missing")
    return if (fieldTypeMeta == FieldType.COMPOSITION) emptyList()
    else listOf("""Root type must be "composition"""")
}

private fun validatePackages(mainMeta: MainModel): List<String> {
    val packagesMeta =
        runCatching { getPackagesMeta(mainMeta) }.getOrNull() ?: return listOf("Packages are missing")
    return packagesMeta.values.flatMapIndexed { index, packageMeta -> validatePackage(packageMeta, index) }
}

private fun validatePackage(packageElementMeta: ElementModel, packageIndex: Int): List<String> {
    val packageId = getPackageId(packageIndex)
    val packageMeta = packageElementMeta as? EntityModel
        ?: return listOf("$packageId is not an EntityModel. It is: ${packageElementMeta.javaClass.simpleName}")
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
    } ?: listOf()
}

private fun validateEnumeration(
    enumerationElementMeta: ElementModel,
    packageId: String,
    enumerationIndex: Int
): List<String> {
    val enumerationId = getEnumerationId(packageId, enumerationIndex)
    val enumerationMeta = enumerationElementMeta as? EntityModel
        ?: return listOf("$enumerationId is not an EntityModel. It is: ${enumerationElementMeta.javaClass.simpleName}")
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
    val id = getEnumerationValueId(enumerationId, valueIndex)
    val enumerationValueMeta = enumerationValueElementMeta as? EntityModel
        ?: return listOf("$id is not an EntityModel. It is: ${enumerationValueElementMeta.javaClass.simpleName}")
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
    } ?: listOf()
}

private fun validateEntity(
    entityElementMeta: ElementModel,
    packageId: String,
    entityIndex: Int
): List<String> {
    val entityId = getEntityId(packageId, entityIndex)
    val entityMeta = entityElementMeta as? EntityModel
        ?: return listOf("$entityId is not an EntityModel. It is: ${entityElementMeta.javaClass.simpleName}")
    return listOf(
        validateSingleStringField(entityMeta, "name", entityId),
        validateFields(entityMeta, entityId)
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
    val fieldId = getFieldId(entityId, fieldIndex)
    val fieldMeta = fieldElementMeta as? EntityModel
        ?: return listOf("$fieldId is not an EntityModel. It is: ${fieldElementMeta.javaClass.simpleName}")
    return listOf(
        validateSingleStringField(fieldMeta, "name", fieldId),
        validateFieldType(fieldMeta, fieldId),
        validateFieldMultiplicity(fieldMeta, fieldId),
        validateFieldIsKey(fieldMeta, fieldId)
    ).flatten()
}

private fun validateFieldType(fieldMeta: EntityModel, fieldId: String): List<String> {
    val fieldTypeMeta = runCatching { getFieldTypeMeta(fieldMeta) }.getOrNull()
        ?: return listOf("$fieldId type is missing")
    return if (!FieldType.values().contains(fieldTypeMeta)) {
        listOf("$fieldId has an invalid field type: ${fieldTypeMeta.name.lowercase()}")
    } else when (fieldTypeMeta) {
        FieldType.ENUMERATION -> validateEnumerationInfo(fieldMeta, fieldId)
        FieldType.ASSOCIATION -> validateAssociationInfo(fieldMeta, fieldId)
        FieldType.COMPOSITION -> validateEntityInfo(fieldMeta, fieldId)
        else -> listOf()
    }
}

private fun validateEnumerationInfo(fieldMeta: EntityModel, fieldId: String): List<String> {
    val infoId = "$fieldId enumeration info"
    val enumerationInfoMeta = runCatching { getEnumerationInfoMeta(fieldMeta) }.getOrNull()
        ?: return listOf("$infoId is missing")
    return listOf(
        validateSingleStringField(enumerationInfoMeta, "name", infoId),
        validateSingleStringField(enumerationInfoMeta, "package", infoId),
    ).flatten()
}

private fun validateAssociationInfo(fieldMeta: EntityModel, fieldId: String): List<String> {
    val infoId = "$fieldId association info"
    val associationInfoMeta = runCatching { getAssociationInfoMeta(fieldMeta) }.getOrNull()
        ?: return listOf("$infoId is missing")
    return if (associationInfoMeta.values.isEmpty()) listOf("$infoId is empty")
    else listOf()
}

private fun validateEntityInfo(fieldMeta: EntityModel, fieldId: String): List<String> {
    val infoId = "$fieldId composition info"
    val entityInfoMeta = runCatching { getEntityInfoMeta(fieldMeta) }.getOrNull()
        ?: return listOf("$infoId is missing")
    return listOf(
        validateSingleStringField(entityInfoMeta, "name", infoId),
        validateSingleStringField(entityInfoMeta, "package", infoId),
    ).flatten()
}

private fun validateFieldMultiplicity(fieldMeta: EntityModel, fieldId: String): List<String> {
    val multiplicityMeta = getMultiplicityMeta(fieldMeta)
    if (!Multiplicity.values().contains(multiplicityMeta)) {
        return listOf("$fieldId has an invalid multiplicity: ${multiplicityMeta.name.lowercase()}")
    }
    val fieldTypeMeta = runCatching { getFieldTypeMeta(fieldMeta) }.getOrNull() ?: return listOf()
    return when (multiplicityMeta) {
        Multiplicity.LIST ->
            if (fieldTypeMeta != FieldType.COMPOSITION) listOf()
            else listOf("$fieldId is a composition field and they cannot be lists")
        Multiplicity.SET ->
            if (fieldTypeMeta == FieldType.COMPOSITION) listOf()
            else listOf("$fieldId cannot be a 'set'. Only compositions can be sets.")
        else -> listOf()
    }
}

private fun validateFieldIsKey(fieldMeta: EntityModel, fieldId: String): List<String> {
    if (!isKeyFieldMeta(fieldMeta)) return listOf()
    val errors = mutableListOf<String>()
    if (getMultiplicityMeta(fieldMeta) != Multiplicity.REQUIRED) errors.add("$fieldId is a key but not defined as required")
    when (runCatching { getFieldTypeMeta(fieldMeta) }.getOrNull()) {
        FieldType.PASSWORD1WAY, FieldType.PASSWORD2WAY -> errors.add("$fieldId is a password field and they cannot be keys")
        FieldType.ASSOCIATION -> errors.add("$fieldId is an association field and they cannot be keys")
        else -> Unit
    }
    return errors
}

// Helpers

private fun validateSingleStringField(meta: BaseEntityModel, fieldName: String, id: String): List<String> =
    when (runCatching { getSingleString(meta, fieldName) }.getOrNull()) {
        null -> listOf("$id $fieldName is missing")
        else -> listOf()
    }

private fun getPackageId(packageIndex: Int) = "Package $packageIndex"

private fun getEnumerationId(packageId: String, enumerationIndex: Int) = "$packageId enumeration $enumerationIndex"

private fun getEnumerationValueId(enumerationId: String, valueIndex: Int) = "$enumerationId value $valueIndex"

private fun getEntityId(packageId: String, entityIndex: Int) = "$packageId entity $entityIndex"

private fun getFieldId(entityId: String, fieldIndex: Int) = "$entityId field $fieldIndex"
