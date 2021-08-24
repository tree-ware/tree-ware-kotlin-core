package org.treeWare.metaModel

import org.treeWare.model.core.*

fun getRootMeta(mainMeta: Model<Resolved>): EntityModel<Resolved> = getSingleEntity(mainMeta.root, "root")

fun getPackagesMeta(mainMeta: Model<Resolved>): ListFieldModel<Resolved> = getListField(mainMeta.root, "packages")

fun getEnumerationsMeta(packageMeta: EntityModel<Resolved>): ListFieldModel<Resolved>? =
    runCatching { getListField(packageMeta, "enumerations") }.getOrNull()

fun getEnumerationValuesMeta(enumerationMeta: EntityModel<Resolved>): ListFieldModel<Resolved> =
    getListField(enumerationMeta, "values")

fun getEntitiesMeta(packageMeta: EntityModel<Resolved>): ListFieldModel<Resolved>? =
    runCatching { getListField(packageMeta, "entities") }.getOrNull()

fun getEntityMeta(
    packages: ListFieldModel<Resolved>,
    packageName: String,
    entityName: String
): EntityModel<Resolved> {
    val packageMeta = findListElement(packages, packageName)
    val entitiesMeta = getListField(packageMeta, "entities")
    return findListElement(entitiesMeta, entityName)
}

// TODO(performance): cache the results, in the meta-model (similar to schema.resolvedEntity), or in a map here.
fun getResolvedRootMeta(mainMeta: Model<Resolved>): EntityModel<Resolved> {
    val rootMeta = getSingleEntity(mainMeta.root, "root")
    val entityName = getSingleString(rootMeta, "entity")
    val packageName = getSingleString(rootMeta, "package")

    val packages = getListField(mainMeta.root, "packages")
    return getEntityMeta(packages, packageName, entityName)
}

fun getFieldsMeta(entityMeta: EntityModel<Resolved>): ListFieldModel<Resolved> = getListField(entityMeta, "fields")

fun getFieldMeta(entityMeta: EntityModel<Resolved>, fieldName: String): EntityModel<Resolved> {
    val fields = getListField(entityMeta, "fields")
    return findListElement(fields, fieldName)
}

fun getMetaName(meta: BaseEntityModel<Resolved>): String = getSingleString(meta, "name")

fun getFieldTypeMeta(fieldMeta: EntityModel<Resolved>): String = getSingleEnumeration(fieldMeta, "type").name

fun getEnumerationInfoMeta(fieldMeta: EntityModel<Resolved>): EntityModel<Resolved> =
    getSingleEntity(fieldMeta, "enumeration")

fun getAssociationInfoMeta(fieldMeta: EntityModel<Resolved>): ListFieldModel<Resolved> =
    getListField(fieldMeta, "association")

fun getEntityInfoMeta(fieldMeta: EntityModel<Resolved>): EntityModel<Resolved> = getSingleEntity(fieldMeta, "entity")

fun getMultiplicityMeta(fieldMeta: EntityModel<Resolved>): String? =
    getOptionalSingleEnumeration(fieldMeta, "multiplicity")?.name

fun isFieldMetaList(fieldMeta: EntityModel<Resolved>): Boolean = getMultiplicityMeta(fieldMeta) == "list"

// TODO(performance): cache the results, in the meta-model (similar to schema.resolvedEntity), or in a map here.
fun getResolvedEntityMeta(fieldMeta: EntityModel<Resolved>): EntityModel<Resolved> {
    val entityInfo = getSingleEntity(fieldMeta, "entity")
    val entityName = getSingleString(entityInfo, "name")
    val packageName = getSingleString(entityInfo, "package")

    val packages = getPackagesFromField(fieldMeta)
    return getEntityMeta(packages, packageName, entityName)
}

private fun getPackagesFromField(fieldMeta: EntityModel<Resolved>): ListFieldModel<Resolved> {
    // Walk up the parents to the "packages" entity.
    val fields = fieldMeta.parent
    val entity = fields.parent
    val entities = entity.parent ?: throw IllegalStateException()
    val `package` = entities.parent ?: throw IllegalStateException()
    return `package`.parent as? ListFieldModel<Resolved> ?: throw IllegalStateException()
}

private fun findListElement(list: ListFieldModel<Resolved>, name: String) =
    list.values.find { entity ->
        if (entity !is EntityModel<Resolved>) false
        else getSingleString(entity, "name") == name
    } as? EntityModel<Resolved> ?: throw IllegalStateException()
