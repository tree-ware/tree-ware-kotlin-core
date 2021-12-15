package org.treeWare.metaModel.validation

import org.treeWare.metaModel.*
import org.treeWare.model.core.*

/**
 * Resolves all non-primitive field types except associations.
 * Associations can be resolved only after compositions are resolved.
 * Returns a list of errors. Returns an empty list if there are no errors.
 *
 * Side effects:
 * 1. Non-primitive field types (except associations) are resolved.
 */
fun resolveNonPrimitiveTypes(
    mainMeta: MainModel,
    hasher: Hasher?,
    cipher: Cipher?,
    nonPrimitiveTypes: NonPrimitiveTypes
): List<String> =
    listOf(
        resolveRoot(mainMeta, nonPrimitiveTypes),
        resolvePackages(mainMeta, hasher, cipher, nonPrimitiveTypes)
    ).flatten()

private fun resolveRoot(mainMeta: MainModel, nonPrimitiveTypes: NonPrimitiveTypes): List<String> {
    val rootMeta = getRootMeta(mainMeta)
    val packageName = getSingleString(rootMeta, "package")
    val entityName = getSingleString(rootMeta, "entity")
    val targetFullName = "/$packageName/$entityName"
    val targetEntity = nonPrimitiveTypes.entities[targetFullName] ?: return listOf("Root entity cannot be resolved")
    val resolved = getMetaModelResolved(rootMeta) ?: throw IllegalStateException("Resolved aux is missing in root")
    resolved.compositionMeta = targetEntity
    return listOf()
}

private fun resolvePackages(
    mainMeta: MainModel,
    hasher: Hasher?,
    cipher: Cipher?,
    nonPrimitiveTypes: NonPrimitiveTypes
): List<String> {
    val packagesMeta = getPackagesMeta(mainMeta)
    return packagesMeta.values.flatMap { resolvePackage(it, hasher, cipher, nonPrimitiveTypes) }
}

private fun resolvePackage(
    packageElementMeta: ElementModel,
    hasher: Hasher?,
    cipher: Cipher?,
    nonPrimitiveTypes: NonPrimitiveTypes
): List<String> {
    val packageMeta = packageElementMeta as EntityModel
    return resolveEntities(packageMeta, hasher, cipher, nonPrimitiveTypes)
}

private fun resolveEntities(
    packageMeta: EntityModel,
    hasher: Hasher?,
    cipher: Cipher?,
    nonPrimitiveTypes: NonPrimitiveTypes
): List<String> {
    val entitiesMeta = getEntitiesMeta(packageMeta)
    return entitiesMeta?.values?.flatMap { resolveEntity(it, hasher, cipher, nonPrimitiveTypes) } ?: listOf()
}

private fun resolveEntity(
    entityElementMeta: ElementModel,
    hasher: Hasher?,
    cipher: Cipher?,
    nonPrimitiveTypes: NonPrimitiveTypes
): List<String> {
    val entityMeta = entityElementMeta as EntityModel
    return resolveFields(entityMeta, hasher, cipher, nonPrimitiveTypes)
}

private fun resolveFields(
    entityMeta: EntityModel,
    hasher: Hasher?,
    cipher: Cipher?,
    nonPrimitiveTypes: NonPrimitiveTypes
): List<String> {
    val fieldsMeta = getFieldsMeta(entityMeta)
    return fieldsMeta.values.flatMap { resolveField(it, hasher, cipher, nonPrimitiveTypes) }
}

private fun resolveField(
    fieldElementMeta: ElementModel,
    hasher: Hasher?,
    cipher: Cipher?,
    nonPrimitiveTypes: NonPrimitiveTypes
): List<String> {
    val fieldMeta = fieldElementMeta as EntityModel
    return when (getFieldTypeMeta(fieldMeta)) {
        FieldType.PASSWORD1WAY -> resolvePassword1wayField(fieldMeta, hasher)
        FieldType.PASSWORD2WAY -> resolvePassword2wayField(fieldMeta, cipher)
        FieldType.ENUMERATION -> resolveEnumerationField(fieldMeta, nonPrimitiveTypes)
        FieldType.COMPOSITION -> resolveCompositionField(fieldMeta, nonPrimitiveTypes)
        else -> listOf()
    }
}

fun resolvePassword1wayField(fieldMeta: EntityModel, hasher: Hasher?): List<String> {
    val resolved = getMetaModelResolved(fieldMeta)
        ?: throw IllegalStateException("Resolved aux is missing in password1way field")
    resolved.password1wayHasher = hasher
    return listOf()
}

fun resolvePassword2wayField(fieldMeta: EntityModel, cipher: Cipher?): List<String> {
    val resolved = getMetaModelResolved(fieldMeta)
        ?: throw IllegalStateException("Resolved aux is missing in password2way field")
    resolved.password2wayCipher = cipher
    return listOf()
}

private fun resolveEnumerationField(
    fieldMeta: EntityModel,
    nonPrimitiveTypes: NonPrimitiveTypes
): List<String> {
    val enumerationInfoMeta = getEnumerationInfoMeta(fieldMeta)
    val packageName = getSingleString(enumerationInfoMeta, "package")
    val enumerationName = getSingleString(enumerationInfoMeta, "name")
    val targetFullName = "/$packageName/$enumerationName"
    val targetEnumeration = nonPrimitiveTypes.enumerations[targetFullName]
        ?: return listOf("Enumeration $targetFullName cannot be resolved")
    val resolved = getMetaModelResolved(fieldMeta)
        ?: throw IllegalStateException("Resolved aux is missing in enumeration field targeting $targetFullName")
    resolved.enumerationMeta = targetEnumeration
    return listOf()
}

private fun resolveCompositionField(
    fieldMeta: EntityModel,
    nonPrimitiveTypes: NonPrimitiveTypes
): List<String> {
    val entityInfoMeta = getEntityInfoMeta(fieldMeta)
    val packageName = getSingleString(entityInfoMeta, "package")
    val entityName = getSingleString(entityInfoMeta, "name")
    val targetFullName = "/$packageName/$entityName"
    val targetEntity = nonPrimitiveTypes.entities[targetFullName]
        ?: return listOf("Entity $targetFullName cannot be resolved")
    val resolved = getMetaModelResolved(fieldMeta)
        ?: throw IllegalStateException("Resolved aux is missing in entity field targeting $targetFullName")
    resolved.compositionMeta = targetEntity
    val errors = mutableListOf<String>()
    if (isKeyFieldMeta(fieldMeta) && !hasOnlyPrimitiveKeyFields(targetEntity)) errors.add(
        "Composition key field ${resolved.fullName} target entity does not have only primitive keys"
    )
    if (isSetFieldMeta(fieldMeta) && !hasKeyFields(targetEntity)) errors.add(
        "Composition set field ${resolved.fullName} target entity does not have keys"
    )
    return errors
}