package org.treeWare.model.core

import org.treeWare.schema.core.EnumerationValueSchema

// TODO(performance): cache the results, in the meta-model (similar to schema.resolvedEntity), or in a map here.
fun getResolvedRootMeta(mainMeta: Model<Unit>): EntityModel<Unit> {
    val rootMeta = getSingleEntity(mainMeta.root, "root")
    val entityName = getSingleString(rootMeta, "entity")
    val packageName = getSingleString(rootMeta, "package")

    val packages = getListField(mainMeta.root, "packages")
    return resolveEntityMeta(packages, packageName, entityName)
}

fun getFieldMeta(entityMeta: EntityModel<Unit>, fieldName: String): EntityModel<Unit> {
    val fields = getListField(entityMeta, "fields")
    return findListElement(fields, fieldName)
}

fun getMetaName(meta: BaseEntityModel<Unit>): String = getSingleString(meta, "name")

fun getFieldMetaType(fieldMeta: EntityModel<Unit>): String = getSingleEnumeration(fieldMeta, "type").name

fun isFieldMetaList(fieldMeta: EntityModel<Unit>): Boolean =
    getOptionalSingleEnumeration(fieldMeta, "multiplicity")?.name == "list"

// TODO(performance): cache the results, in the meta-model (similar to schema.resolvedEntity), or in a map here.
fun getResolvedEntityMeta(fieldMeta: EntityModel<Unit>): EntityModel<Unit> {
    val entityInfo = getSingleEntity(fieldMeta, "entity")
    val entityName = getSingleString(entityInfo, "name")
    val packageName = getSingleString(entityInfo, "package")

    val packages = getPackagesFromField(fieldMeta)
    return resolveEntityMeta(packages, packageName, entityName)
}

private fun getPackagesFromField(fieldMeta: EntityModel<Unit>): ListFieldModel<Unit> {
    // Walk up the parents to the "packages" entity.
    val fields = fieldMeta.parent
    val entity = fields.parent
    val entities = entity.parent ?: throw IllegalStateException()
    val `package` = entities.parent ?: throw IllegalStateException()
    return `package`.parent as? ListFieldModel<Unit> ?: throw IllegalStateException()
}

// Helpers

private fun getSingleEntity(meta: BaseEntityModel<Unit>, fieldName: String): EntityModel<Unit> {
    val singleField = getSingleField(meta, fieldName)
    return singleField.value as? EntityModel<Unit> ?: throw IllegalStateException()
}

private fun getSingleString(meta: BaseEntityModel<Unit>, fieldName: String): String {
    val singleField = getSingleField(meta, fieldName)
    val primitive = singleField.value as? PrimitiveModel<Unit> ?: throw IllegalStateException()
    return primitive.value as? String ?: throw IllegalStateException()
}

private fun getSingleEnumeration(meta: BaseEntityModel<Unit>, fieldName: String): EnumerationValueSchema {
    val singleField = getSingleField(meta, fieldName)
    val enumeration = singleField.value as? EnumerationModel<Unit> ?: throw IllegalStateException()
    return enumeration.value ?: throw IllegalStateException()
}

private fun getOptionalSingleEnumeration(meta: BaseEntityModel<Unit>, fieldName: String): EnumerationValueSchema? {
    val singleField = getOptionalSingleField(meta, fieldName) ?: return null
    val enumeration = singleField.value as? EnumerationModel<Unit> ?: throw IllegalStateException()
    return enumeration.value ?: throw IllegalStateException()
}

private fun getSingleField(meta: BaseEntityModel<Unit>, fieldName: String): SingleFieldModel<Unit> {
    return meta.getField(fieldName) as? SingleFieldModel<Unit> ?: throw IllegalStateException()
}

private fun getOptionalSingleField(meta: BaseEntityModel<Unit>, fieldName: String): SingleFieldModel<Unit>? {
    return meta.getField(fieldName) as? SingleFieldModel<Unit>?
}

private fun getListField(meta: BaseEntityModel<Unit>, fieldName: String): ListFieldModel<Unit> {
    return meta.getField(fieldName) as? ListFieldModel<Unit> ?: throw IllegalStateException()
}

private fun resolveEntityMeta(
    packages: ListFieldModel<Unit>,
    packageName: String,
    entityName: String
): EntityModel<Unit> {
    val packageMeta = findListElement(packages, packageName)
    val entitiesMeta = getListField(packageMeta, "entities")
    return findListElement(entitiesMeta, entityName)
}

private fun findListElement(list: ListFieldModel<Unit>, name: String) =
    list.values.find { entity ->
        if (entity !is EntityModel<Unit>) false
        else getSingleString(entity, "name") == name
    } as? EntityModel<Unit> ?: throw IllegalStateException()
