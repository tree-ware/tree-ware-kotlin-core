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
fun resolveNonPrimitiveTypes(mainMeta: Model<Resolved>, nonPrimitiveTypes: NonPrimitiveTypes): List<String> =
    listOf(
        resolveRoot(mainMeta, nonPrimitiveTypes),
        resolvePackages(mainMeta, nonPrimitiveTypes)
    ).flatten()

private fun resolveRoot(mainMeta: Model<Resolved>, nonPrimitiveTypes: NonPrimitiveTypes): List<String> {
    val rootMeta = getRootMeta(mainMeta)
    val packageName = getSingleString(rootMeta, "package")
    val entityName = getSingleString(rootMeta, "entity")
    val targetFullName = "/$packageName/$entityName"
    val targetEntity = nonPrimitiveTypes.entities[targetFullName] ?: return listOf("Root entity cannot be resolved")
    val resolved = rootMeta.aux ?: throw IllegalStateException("Resolved aux is missing in root")
    resolved.entityMeta = targetEntity
    return listOf()
}

private fun resolvePackages(mainMeta: Model<Resolved>, nonPrimitiveTypes: NonPrimitiveTypes): List<String> {
    val packagesMeta = getPackagesMeta(mainMeta)
    return packagesMeta.values.flatMap { resolvePackage(it, nonPrimitiveTypes) }
}

private fun resolvePackage(
    packageElementMeta: ElementModel<Resolved>,
    nonPrimitiveTypes: NonPrimitiveTypes
): List<String> {
    val packageMeta = packageElementMeta as EntityModel<Resolved>
    return resolveEntities(packageMeta, nonPrimitiveTypes)
}

private fun resolveEntities(packageMeta: EntityModel<Resolved>, nonPrimitiveTypes: NonPrimitiveTypes): List<String> {
    val entitiesMeta = getEntitiesMeta(packageMeta)
    return entitiesMeta?.values?.flatMap { resolveEntity(it, nonPrimitiveTypes) } ?: listOf()
}

private fun resolveEntity(
    entityElementMeta: ElementModel<Resolved>,
    nonPrimitiveTypes: NonPrimitiveTypes
): List<String> {
    val entityMeta = entityElementMeta as EntityModel<Resolved>
    return resolveFields(entityMeta, nonPrimitiveTypes)
}

private fun resolveFields(entityMeta: EntityModel<Resolved>, nonPrimitiveTypes: NonPrimitiveTypes): List<String> {
    val fieldsMeta = getFieldsMeta(entityMeta)
    return fieldsMeta.values.flatMap { resolveField(it, nonPrimitiveTypes) }
}

private fun resolveField(fieldElementMeta: ElementModel<Resolved>, nonPrimitiveTypes: NonPrimitiveTypes): List<String> {
    val fieldMeta = fieldElementMeta as EntityModel<Resolved>
    return when (getFieldTypeMeta(fieldMeta)) {
        "enumeration" -> resolveEnumerationField(fieldMeta, nonPrimitiveTypes)
        "entity" -> resolveEntityField(fieldMeta, nonPrimitiveTypes)
        else -> listOf()
    }
}

private fun resolveEnumerationField(
    fieldMeta: EntityModel<Resolved>,
    nonPrimitiveTypes: NonPrimitiveTypes
): List<String> {
    val enumerationInfoMeta = getEnumerationInfoMeta(fieldMeta)
    val packageName = getSingleString(enumerationInfoMeta, "package")
    val enumerationName = getSingleString(enumerationInfoMeta, "name")
    val targetFullName = "/$packageName/$enumerationName"
    val targetEnumeration = nonPrimitiveTypes.enumerations[targetFullName]
        ?: return listOf("Enumeration $targetFullName cannot be resolved")
    val resolved = fieldMeta.aux
        ?: throw IllegalStateException("Resolved aux is missing in enumeration field targeting $targetFullName")
    resolved.enumerationMeta = targetEnumeration
    return listOf()
}

private fun resolveEntityField(fieldMeta: EntityModel<Resolved>, nonPrimitiveTypes: NonPrimitiveTypes): List<String> {
    val entityInfoMeta = getEntityInfoMeta(fieldMeta)
    val packageName = getSingleString(entityInfoMeta, "package")
    val entityName = getSingleString(entityInfoMeta, "name")
    val targetFullName = "/$packageName/$entityName"
    val targetEntity = nonPrimitiveTypes.entities[targetFullName]
        ?: return listOf("Entity $targetFullName cannot be resolved")
    val resolved = fieldMeta.aux
        ?: throw IllegalStateException("Resolved aux is missing in entity field targeting $targetFullName")
    resolved.entityMeta = targetEntity
    return listOf()
}
