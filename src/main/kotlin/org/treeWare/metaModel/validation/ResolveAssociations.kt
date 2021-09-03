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
fun resolveAssociations(mainMeta: Model<Resolved>): List<String> {
    val rootMeta = getRootMeta(mainMeta)
    return resolvePackages(mainMeta, rootMeta)
}

private fun resolvePackages(mainMeta: Model<Resolved>, rootMeta: EntityModel<Resolved>): List<String> {
    val packagesMeta = getPackagesMeta(mainMeta)
    return packagesMeta.values.flatMap { resolvePackage(it, rootMeta) }
}

private fun resolvePackage(packageElementMeta: ElementModel<Resolved>, rootMeta: EntityModel<Resolved>): List<String> {
    val packageMeta = packageElementMeta as EntityModel<Resolved>
    return resolveEntities(packageMeta, rootMeta)
}

private fun resolveEntities(packageMeta: EntityModel<Resolved>, rootMeta: EntityModel<Resolved>): List<String> {
    val entitiesMeta = getEntitiesMeta(packageMeta)
    return entitiesMeta?.values?.flatMap { resolveEntity(it, rootMeta) } ?: listOf()
}

private fun resolveEntity(entityElementMeta: ElementModel<Resolved>, rootMeta: EntityModel<Resolved>): List<String> {
    val entityMeta = entityElementMeta as EntityModel<Resolved>
    return resolveFields(entityMeta, rootMeta)
}

private fun resolveFields(entityMeta: EntityModel<Resolved>, rootMeta: EntityModel<Resolved>): List<String> {
    val fieldsMeta = getFieldsMeta(entityMeta)
    return fieldsMeta.values.flatMap { resolveField(it, rootMeta) }
}

private fun resolveField(fieldElementMeta: ElementModel<Resolved>, rootMeta: EntityModel<Resolved>): List<String> {
    val fieldMeta = fieldElementMeta as EntityModel<Resolved>
    return when (getFieldTypeMeta(fieldMeta)) {
        FieldType.ASSOCIATION -> resolveAssociationField(fieldMeta, rootMeta)
        else -> listOf()
    }
}

private fun resolveAssociationField(fieldMeta: EntityModel<Resolved>, rootMeta: EntityModel<Resolved>): List<String> {
    val fieldFullName = fieldMeta.aux?.fullName ?: ""
    val associationInfoMeta = getAssociationInfoMeta(fieldMeta)
    if (associationInfoMeta.values.size < 2) return listOf("Association field $fieldFullName has an insufficient path")

    var entityMeta: EntityModel<Resolved>? = null
    val pathEntityMetaList = mutableListOf<EntityModel<Resolved>>()
    val keyPathElementList = mutableListOf<String>()
    val keyEntityMetaList = mutableListOf<EntityModel<Resolved>>()

    val pathElements = getListStrings(associationInfoMeta)
    val errors = pathElements.flatMap { pathElement ->
        val nextEntityMetaResult = entityMeta?.let { getNextEntityMeta(pathElement, it, fieldFullName) }
            ?: getFirstEntityMeta(pathElement, rootMeta, fieldFullName)
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
    val resolved = fieldMeta.aux
        ?: throw IllegalStateException("Resolved aux is missing in association field $fieldFullName")
    resolved.associationMeta =
        entityMeta?.let { ResolvedAssociationMeta(it, pathEntityMetaList, keyPathElementList, keyEntityMetaList) }
    return errors
}

private class NextEntityMetaResult(val entityMeta: EntityModel<Resolved>?, val errors: List<String>)

private fun getFirstEntityMeta(
    pathElement: String,
    rootMeta: EntityModel<Resolved>,
    fieldFullName: String
): NextEntityMetaResult {
    // First path element must match root.
    val rootName = getMetaName(rootMeta)
    if (pathElement != rootName) return NextEntityMetaResult(
        null,
        listOf("Association field $fieldFullName has an invalid path root")
    )
    val firstEntityMeta =
        rootMeta.aux?.compositionMeta ?: return NextEntityMetaResult(null, listOf("Root has not been resolved"))
    return NextEntityMetaResult(firstEntityMeta, listOf())
}

private fun getNextEntityMeta(
    pathElement: String,
    previousEntityMeta: EntityModel<Resolved>,
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
    val nextEntityMeta = fieldMeta.aux?.compositionMeta ?: return NextEntityMetaResult(
        null,
        listOf("Association field $fieldFullName path element $pathElement has not been resolved")
    )
    return NextEntityMetaResult(nextEntityMeta, listOf())
}
