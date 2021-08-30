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

fun getFieldsMeta(entityMeta: EntityModel<Resolved>): ListFieldModel<Resolved> = getListField(entityMeta, "fields")

fun getFieldMeta(entityMeta: EntityModel<Resolved>, fieldName: String): EntityModel<Resolved> {
    val fields = getListField(entityMeta, "fields")
    return findListElement(fields, fieldName)
}

fun hasKeyFields(entityMeta: EntityModel<Resolved>): Boolean = getFieldsMeta(entityMeta).values.any { fieldElement ->
    val fieldMeta = fieldElement as? EntityModel<Resolved>
    fieldMeta?.let { isKeyFieldMeta(it) } ?: false
}

fun getMetaName(meta: BaseEntityModel<Resolved>): String = getSingleString(meta, "name")

fun getFieldTypeMeta(fieldMeta: EntityModel<Resolved>): String = getSingleEnumeration(fieldMeta, "type").name

fun getEnumerationInfoMeta(fieldMeta: EntityModel<Resolved>): EntityModel<Resolved> =
    getSingleEntity(fieldMeta, "enumeration")

fun getAssociationInfoMeta(fieldMeta: EntityModel<Resolved>): ListFieldModel<Resolved> =
    getListField(fieldMeta, "association")

fun getEntityInfoMeta(fieldMeta: EntityModel<Resolved>): EntityModel<Resolved> = getSingleEntity(fieldMeta, "entity")

fun getMultiplicityMeta(fieldMeta: EntityModel<Resolved>): String =
    getOptionalSingleEnumeration(fieldMeta, "multiplicity")?.name ?: "required"

fun isListFieldMeta(fieldMeta: EntityModel<Resolved>): Boolean = getMultiplicityMeta(fieldMeta) == "list"

fun isKeyFieldMeta(fieldMeta: EntityModel<Resolved>): Boolean = getOptionalSingleBoolean(fieldMeta, "is_key") ?: false

private fun findListElement(list: ListFieldModel<Resolved>, name: String) =
    list.values.find { entity ->
        if (entity !is EntityModel<Resolved>) false
        else getSingleString(entity, "name") == name
    } as? EntityModel<Resolved> ?: throw IllegalStateException()
