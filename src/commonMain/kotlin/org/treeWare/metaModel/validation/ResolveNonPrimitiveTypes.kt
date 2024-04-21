package org.treeWare.metaModel.validation

import org.treeWare.metaModel.*
import org.treeWare.model.core.*

/**
 * Resolves all non-primitive field types.
 * Returns a list of errors. Returns an empty list if there are no errors.
 *
 * Side effects:
 * 1. Non-primitive field types are resolved.
 * 2. Key fields are memoized in field-number sorted order.
 */
fun resolveNonPrimitiveTypes(
    mainMeta: MainModel,
    hasher: Hasher?,
    cipher: Cipher?,
    rootEntityFactory: RootEntityFactory,
    nonPrimitiveTypes: NonPrimitiveTypes
): List<String> {
    val rootErrors = resolveRoot(mainMeta, nonPrimitiveTypes)
    val rootEntityMeta = getMetaModelResolved(getRootMeta(mainMeta))?.compositionMeta
    val packageErrors = resolvePackages(mainMeta, rootEntityMeta, hasher, cipher, rootEntityFactory, nonPrimitiveTypes)
    return listOf(rootErrors, packageErrors).flatten()
}

private fun resolveRoot(mainMeta: MainModel, nonPrimitiveTypes: NonPrimitiveTypes): List<String> {
    val rootMeta = getRootMeta(mainMeta)
    return resolveCompositionField(rootMeta, nonPrimitiveTypes)
}

private fun resolvePackages(
    mainMeta: MainModel,
    rootEntityMeta: EntityModel?,
    hasher: Hasher?,
    cipher: Cipher?,
    rootEntityFactory: RootEntityFactory,
    nonPrimitiveTypes: NonPrimitiveTypes
): List<String> {
    val packagesMeta = getPackagesMeta(mainMeta)
    return packagesMeta.values.flatMap {
        resolvePackage(
            it,
            rootEntityMeta,
            hasher,
            cipher,
            rootEntityFactory,
            nonPrimitiveTypes
        )
    }
}

private fun resolvePackage(
    packageElementMeta: ElementModel,
    rootEntityMeta: EntityModel?,
    hasher: Hasher?,
    cipher: Cipher?,
    rootEntityFactory: RootEntityFactory,
    nonPrimitiveTypes: NonPrimitiveTypes
): List<String> {
    val packageMeta = packageElementMeta as EntityModel
    return resolveEntities(packageMeta, rootEntityMeta, hasher, cipher, rootEntityFactory, nonPrimitiveTypes)
}

private fun resolveEntities(
    packageMeta: EntityModel,
    rootEntityMeta: EntityModel?,
    hasher: Hasher?,
    cipher: Cipher?,
    rootEntityFactory: RootEntityFactory,
    nonPrimitiveTypes: NonPrimitiveTypes
): List<String> {
    val entitiesMeta = getEntitiesMeta(packageMeta)
    return entitiesMeta?.values?.flatMap {
        resolveEntity(
            it,
            rootEntityMeta,
            hasher,
            cipher,
            rootEntityFactory,
            nonPrimitiveTypes
        )
    }
        ?: listOf()
}

private fun resolveEntity(
    entityElementMeta: ElementModel,
    rootEntityMeta: EntityModel?,
    hasher: Hasher?,
    cipher: Cipher?,
    rootEntityFactory: RootEntityFactory,
    nonPrimitiveTypes: NonPrimitiveTypes
): List<String> {
    val entityMeta = entityElementMeta as EntityModel
    val entityResolved = getMetaModelResolved(entityMeta)
        ?: throw IllegalStateException("Resolved aux is missing in entity")
    val errors =
        resolveFields(entityMeta, entityResolved, rootEntityMeta, hasher, cipher, rootEntityFactory, nonPrimitiveTypes)
    entityResolved.sortedKeyFieldsMetaInternal.sortBy { getMetaNumber(it) }
    return errors
}

private fun resolveFields(
    entityMeta: EntityModel,
    entityResolved: Resolved,
    rootEntityMeta: EntityModel?,
    hasher: Hasher?,
    cipher: Cipher?,
    rootEntityFactory: RootEntityFactory,
    nonPrimitiveTypes: NonPrimitiveTypes
): List<String> {
    val fieldsMeta = getFieldsMeta(entityMeta)
    return fieldsMeta.values.flatMap {
        resolveField(
            it,
            entityResolved,
            rootEntityMeta,
            hasher,
            cipher,
            rootEntityFactory,
            nonPrimitiveTypes
        )
    }
}

private fun resolveField(
    fieldElementMeta: ElementModel,
    entityResolved: Resolved,
    rootEntityMeta: EntityModel?,
    hasher: Hasher?,
    cipher: Cipher?,
    rootEntityFactory: RootEntityFactory,
    nonPrimitiveTypes: NonPrimitiveTypes
): List<String> {
    val fieldMeta = fieldElementMeta as EntityModel
    if (isKeyFieldMeta(fieldMeta)) {
        entityResolved.sortedKeyFieldsMetaInternal.add(fieldElementMeta)
    }
    return when (getFieldTypeMeta(fieldMeta)) {
        FieldType.PASSWORD1WAY -> resolvePassword1wayField(fieldMeta, hasher)
        FieldType.PASSWORD2WAY -> resolvePassword2wayField(fieldMeta, cipher)
        FieldType.ENUMERATION -> resolveEnumerationField(fieldMeta, nonPrimitiveTypes)
        FieldType.ASSOCIATION -> resolveAssociationField(
            fieldMeta,
            rootEntityMeta,
            rootEntityFactory,
            nonPrimitiveTypes
        )
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

private fun resolveAssociationField(
    fieldMeta: EntityModel,
    rootEntityMeta: EntityModel?,
    rootEntityFactory: RootEntityFactory,
    nonPrimitiveTypes: NonPrimitiveTypes
): List<String> {
    val entityInfoMeta = getEntityInfoMeta(fieldMeta, "association")
    val packageName = getSingleString(entityInfoMeta, "package")
    val entityName = getSingleString(entityInfoMeta, "entity")
    val targetFullName = "/$packageName/$entityName"
    val targetEntityMeta = nonPrimitiveTypes.entities[targetFullName]
        ?: return listOf("Entity $targetFullName cannot be resolved")
    val parentEntityMeta = getParentEntityMeta(fieldMeta)
    val isRecursive = targetEntityMeta == parentEntityMeta

    val resolved = getMetaModelResolved(fieldMeta)
        ?: throw IllegalStateException("Resolved aux is missing in entity field targeting $targetFullName")
    resolved.associationMeta =
        rootEntityMeta?.let { ResolvedAssociationMeta(it, targetEntityMeta, isRecursive, rootEntityFactory) }

    // Record recursive association fields in the parent entity.
    if (isRecursive) {
        val parentResolved = getMetaModelResolved(parentEntityMeta)
            ?: throw IllegalStateException("Resolved aux is missing for parent entity of ${resolved.fullName}")
        parentResolved.recursiveAssociationFieldsMetaInternal.add(fieldMeta)
    }

    return emptyList()
}

private fun resolveCompositionField(
    fieldMeta: EntityModel,
    nonPrimitiveTypes: NonPrimitiveTypes
): List<String> {
    val entityInfoMeta = getEntityInfoMeta(fieldMeta, "composition")
    val packageName = getSingleString(entityInfoMeta, "package")
    val entityName = getSingleString(entityInfoMeta, "entity")
    val targetFullName = "/$packageName/$entityName"
    val targetEntityMeta = nonPrimitiveTypes.entities[targetFullName]
        ?: return listOf("Entity $targetFullName cannot be resolved")

    val resolved = getMetaModelResolved(fieldMeta)
        ?: throw IllegalStateException("Resolved aux is missing in entity field targeting $targetFullName")
    resolved.compositionMeta = targetEntityMeta

    // Record recursive composition fields in the parent entity.
    val parentEntityMeta = getParentEntityMeta(fieldMeta)
    if (targetEntityMeta == parentEntityMeta) {
        val parentResolved = getMetaModelResolved(parentEntityMeta)
            ?: throw IllegalStateException("Resolved aux is missing for parent entity of ${resolved.fullName}")
        parentResolved.recursiveCompositionFieldsMetaInternal.add(fieldMeta)
    }

    // Record this field as a parent of the target entity.
    val targetResolved = getMetaModelResolved(targetEntityMeta)
        ?: throw IllegalStateException("Resolved aux is missing for target entity $targetFullName")
    targetResolved.parentFieldsMetaInternal.add(fieldMeta)

    val errors = mutableListOf<String>()
    if (isKeyFieldMeta(fieldMeta) && !hasOnlyPrimitiveKeyFields(targetEntityMeta)) errors.add(
        "Composition key field ${resolved.fullName} target entity does not have only primitive keys"
    )
    if (isSetFieldMeta(fieldMeta) && !hasKeyFields(targetEntityMeta)) errors.add(
        "Composition set field ${resolved.fullName} target entity does not have keys"
    )
    return errors
}