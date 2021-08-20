package org.treeWare.model.core

import org.treeWare.schema.core.EnumerationValueSchema

// TODO(performance): cache the results, in the meta-model (similar to schema.resolvedEntity), or in a map here.
fun getResolvedRootMeta(mainMeta: Model<Resolved>): EntityModel<Resolved> {
    val rootMeta = getSingleEntity(mainMeta.root, "root")
    val entityName = getSingleString(rootMeta, "entity")
    val packageName = getSingleString(rootMeta, "package")

    val packages = getListField(mainMeta.root, "packages")
    return resolveEntityMeta(packages, packageName, entityName)
}

fun getFieldMeta(entityMeta: EntityModel<Resolved>, fieldName: String): EntityModel<Resolved> {
    val fields = getListField(entityMeta, "fields")
    return findListElement(fields, fieldName)
}

fun getMetaName(meta: BaseEntityModel<Resolved>): String = getSingleString(meta, "name")

fun getFieldMetaType(fieldMeta: EntityModel<Resolved>): String = getSingleEnumeration(fieldMeta, "type").name

fun isFieldMetaList(fieldMeta: EntityModel<Resolved>): Boolean =
    getOptionalSingleEnumeration(fieldMeta, "multiplicity")?.name == "list"

// TODO(performance): cache the results, in the meta-model (similar to schema.resolvedEntity), or in a map here.
fun getResolvedEntityMeta(fieldMeta: EntityModel<Resolved>): EntityModel<Resolved> {
    val entityInfo = getSingleEntity(fieldMeta, "entity")
    val entityName = getSingleString(entityInfo, "name")
    val packageName = getSingleString(entityInfo, "package")

    val packages = getPackagesFromField(fieldMeta)
    return resolveEntityMeta(packages, packageName, entityName)
}

private fun getPackagesFromField(fieldMeta: EntityModel<Resolved>): ListFieldModel<Resolved> {
    // Walk up the parents to the "packages" entity.
    val fields = fieldMeta.parent
    val entity = fields.parent
    val entities = entity.parent ?: throw IllegalStateException()
    val `package` = entities.parent ?: throw IllegalStateException()
    return `package`.parent as? ListFieldModel<Resolved> ?: throw IllegalStateException()
}

// Helpers

private fun getSingleEntity(meta: BaseEntityModel<Resolved>, fieldName: String): EntityModel<Resolved> {
    val singleField = getSingleField(meta, fieldName)
    return singleField.value as? EntityModel<Resolved> ?: throw IllegalStateException()
}

private fun getSingleString(meta: BaseEntityModel<Resolved>, fieldName: String): String {
    val singleField = getSingleField(meta, fieldName)
    val primitive = singleField.value as? PrimitiveModel<Resolved> ?: throw IllegalStateException()
    return primitive.value as? String ?: throw IllegalStateException()
}

private fun getSingleEnumeration(meta: BaseEntityModel<Resolved>, fieldName: String): EnumerationValueSchema {
    val singleField = getSingleField(meta, fieldName)
    val enumeration = singleField.value as? EnumerationModel<Resolved> ?: throw IllegalStateException()
    return enumeration.value ?: throw IllegalStateException()
}

private fun getOptionalSingleEnumeration(
    meta: BaseEntityModel<Resolved>,
    fieldName: String
): EnumerationValueSchema? {
    val singleField = getOptionalSingleField(meta, fieldName) ?: return null
    val enumeration = singleField.value as? EnumerationModel<Resolved> ?: throw IllegalStateException()
    return enumeration.value ?: throw IllegalStateException()
}

private fun getSingleField(meta: BaseEntityModel<Resolved>, fieldName: String): SingleFieldModel<Resolved> {
    return meta.getField(fieldName) as? SingleFieldModel<Resolved> ?: throw IllegalStateException()
}

private fun getOptionalSingleField(
    meta: BaseEntityModel<Resolved>,
    fieldName: String
): SingleFieldModel<Resolved>? {
    return meta.getField(fieldName) as? SingleFieldModel<Resolved>?
}

private fun getListField(meta: BaseEntityModel<Resolved>, fieldName: String): ListFieldModel<Resolved> {
    return meta.getField(fieldName) as? ListFieldModel<Resolved> ?: throw IllegalStateException()
}

private fun resolveEntityMeta(
    packages: ListFieldModel<Resolved>,
    packageName: String,
    entityName: String
): EntityModel<Resolved> {
    val packageMeta = findListElement(packages, packageName)
    val entitiesMeta = getListField(packageMeta, "entities")
    return findListElement(entitiesMeta, entityName)
}

private fun findListElement(list: ListFieldModel<Resolved>, name: String) =
    list.values.find { entity ->
        if (entity !is EntityModel<Resolved>) false
        else getSingleString(entity, "name") == name
    } as? EntityModel<Resolved> ?: throw IllegalStateException()
