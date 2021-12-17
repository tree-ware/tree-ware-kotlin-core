package org.treeWare.metaModel.validation

import org.treeWare.metaModel.*
import org.treeWare.model.core.*

/**
 * Resolves association fields.
 * Returns a list of errors. Returns an empty list if there are no errors.
 *
 * Side effects:
 * 1. Association fields are resolved.
 */
fun resolveAssociations(mainMeta: MainModel): List<String> = resolvePackages(mainMeta)

private fun resolvePackages(mainMeta: MainModel): List<String> {
    val packagesMeta = getPackagesMeta(mainMeta)
    return packagesMeta.values.flatMap { resolvePackage(it, mainMeta) }
}

private fun resolvePackage(packageElementMeta: ElementModel, mainMeta: MainModel): List<String> {
    val packageMeta = packageElementMeta as EntityModel
    return resolveEntities(packageMeta, mainMeta)
}

private fun resolveEntities(packageMeta: EntityModel, mainMeta: MainModel): List<String> {
    val entitiesMeta = getEntitiesMeta(packageMeta)
    return entitiesMeta?.values?.flatMap { resolveEntity(it, mainMeta) } ?: listOf()
}

private fun resolveEntity(entityElementMeta: ElementModel, mainMeta: MainModel): List<String> {
    val entityMeta = entityElementMeta as EntityModel
    return resolveFields(entityMeta, mainMeta)
}

private fun resolveFields(entityMeta: EntityModel, mainMeta: MainModel): List<String> {
    val fieldsMeta = getFieldsMeta(entityMeta)
    return fieldsMeta.values.flatMap { resolveField(it, mainMeta) }
}

private fun resolveField(fieldElementMeta: ElementModel, mainMeta: MainModel): List<String> {
    val fieldMeta = fieldElementMeta as EntityModel
    return when (getFieldTypeMeta(fieldMeta)) {
        FieldType.ASSOCIATION -> resolveAssociationField(fieldMeta, mainMeta)
        else -> listOf()
    }
}

private fun resolveAssociationField(fieldMeta: EntityModel, mainMeta: MainModel): List<String> {
    val fieldFullName = getMetaModelResolved(fieldMeta)?.fullName ?: ""
    val associationInfoMeta = getAssociationInfoMeta(fieldMeta)
    if (associationInfoMeta.values.size < 2) return listOf("Association field $fieldFullName has an insufficient path")

    var entityMeta: EntityModel? = null
    val pathEntityMetaList = mutableListOf<EntityModel>()
    val keyPathElementList = mutableListOf<String>()
    val keyEntityMetaList = mutableListOf<EntityModel>()

    val pathElements = getListStrings(associationInfoMeta)
    val errors = pathElements.flatMap { pathElement ->
        val nextEntityMetaResult = entityMeta?.let { getNextEntityMeta(pathElement, it, fieldFullName) }
            ?: getFirstEntityMeta(pathElement, mainMeta, fieldFullName)
        if (nextEntityMetaResult.entityMeta == null) return nextEntityMetaResult.errors
        entityMeta = nextEntityMetaResult.entityMeta
        entityMeta?.also {
            pathEntityMetaList.add(it)
            if (hasKeyFields(it)) {
                keyPathElementList.add(pathElement)
                keyEntityMetaList.add(it)
            }
        }
        nextEntityMetaResult.errors
    }
    if (isListFieldMeta(fieldMeta) && keyEntityMetaList.isEmpty()) return listOf("Association list field $fieldFullName path does not have keys")
    val resolved = getMetaModelResolved(fieldMeta)
        ?: throw IllegalStateException("Resolved aux is missing in association field $fieldFullName")
    resolved.associationMeta =
        entityMeta?.let { ResolvedAssociationMeta(it, pathEntityMetaList, keyPathElementList, keyEntityMetaList) }
    return errors
}

private class NextEntityMetaResult(val entityMeta: EntityModel?, val errors: List<String>)

private fun getFirstEntityMeta(
    pathElement: String,
    mainMeta: MainModel,
    fieldFullName: String
): NextEntityMetaResult {
    // First path element must match root.
    val rootName = getMetaName(getRootMeta(mainMeta))
    if (pathElement != rootName) return NextEntityMetaResult(
        null,
        listOf("Association field $fieldFullName has an invalid path root")
    )
    val firstEntityMeta = getMetaModelResolved(mainMeta)?.compositionMeta ?: return NextEntityMetaResult(
        null,
        listOf("Root has not been resolved")
    )
    return NextEntityMetaResult(firstEntityMeta, listOf())
}

private fun getNextEntityMeta(
    pathElement: String,
    previousEntityMeta: EntityModel,
    fieldFullName: String
): NextEntityMetaResult {
    val fieldMeta = runCatching { getFieldMeta(previousEntityMeta, pathElement) }.getOrNull()
        ?: return NextEntityMetaResult(
            null,
            listOf("Association field $fieldFullName has an invalid path element $pathElement")
        )
    val fieldTypeMeta = getFieldTypeMeta(fieldMeta)
    if (fieldTypeMeta != FieldType.COMPOSITION) return NextEntityMetaResult(
        null,
        listOf("Association field $fieldFullName path element $pathElement is not an entity")
    )
    val nextEntityMeta = getMetaModelResolved(fieldMeta)?.compositionMeta ?: return NextEntityMetaResult(
        null,
        listOf("Association field $fieldFullName path element $pathElement has not been resolved")
    )
    return NextEntityMetaResult(nextEntityMeta, listOf())
}