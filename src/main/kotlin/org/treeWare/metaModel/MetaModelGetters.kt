package org.treeWare.metaModel

import org.treeWare.model.core.*

fun getRootMeta(mainMeta: MainModel<Resolved>): EntityModel<Resolved> = getSingleEntity(mainMeta.root, "root")

fun getPackagesMeta(mainMeta: MainModel<Resolved>): ListFieldModel<Resolved> = getListField(mainMeta.root, "packages")

fun getEnumerationsMeta(packageMeta: EntityModel<Resolved>): ListFieldModel<Resolved>? =
    runCatching { getListField(packageMeta, "enumerations") }.getOrNull()

fun getEnumerationValuesMeta(enumerationMeta: EntityModel<Resolved>): ListFieldModel<Resolved> =
    getListField(enumerationMeta, "values")

fun getEnumerationValues(enumerationMeta: EntityModel<Resolved>): List<String> =
    getEnumerationValuesMeta(enumerationMeta).values.map {
        val valueMeta = it as? EntityModel<Resolved> ?: throw IllegalStateException()
        getMetaName(valueMeta)
    }

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

fun hasOnlyPrimitiveKeyFields(entityMeta: EntityModel<Resolved>): Boolean {
    val fields = getFieldsMeta(entityMeta).values
    val keyFields = filterKeyFields(fields)
    val compositionKeyFields = filterCompositionKeyFields(keyFields)
    return keyFields.isNotEmpty() && compositionKeyFields.isEmpty()
}

private fun filterKeyFields(fields: List<ElementModel<Resolved>>): List<ElementModel<Resolved>> =
    fields.filter { fieldElement ->
        val fieldMeta = fieldElement as? EntityModel<Resolved>
        fieldMeta?.let { isKeyFieldMeta(it) } ?: false
    }

private fun filterCompositionKeyFields(fields: List<ElementModel<Resolved>>): List<ElementModel<Resolved>> =
    fields.filter { fieldElement ->
        val fieldMeta = fieldElement as? EntityModel<Resolved>
        fieldMeta?.let { isKeyFieldMeta(it) && isCompositionFieldMeta(it) } ?: false
    }

fun getMetaName(meta: BaseEntityModel<Resolved>?): String = meta?.let { getSingleString(meta, "name") } ?: ""

fun getFieldTypeMeta(fieldMeta: EntityModel<Resolved>?): FieldType? = fieldMeta?.let {
    FieldType.valueOf(getSingleEnumeration(fieldMeta, "type").uppercase())
}

fun getEnumerationInfoMeta(fieldMeta: EntityModel<Resolved>): EntityModel<Resolved> =
    getSingleEntity(fieldMeta, "enumeration")

fun getAssociationInfoMeta(fieldMeta: EntityModel<Resolved>): ListFieldModel<Resolved> =
    getListField(fieldMeta, "association")

fun getEntityInfoMeta(fieldMeta: EntityModel<Resolved>): EntityModel<Resolved> =
    getSingleEntity(fieldMeta, "composition")

fun getMultiplicityMeta(fieldMeta: EntityModel<Resolved>): Multiplicity =
    Multiplicity.valueOf((getOptionalSingleEnumeration(fieldMeta, "multiplicity") ?: "required").uppercase())

fun isListFieldMeta(fieldMeta: EntityModel<Resolved>?): Boolean =
    fieldMeta?.let { getMultiplicityMeta(fieldMeta) == Multiplicity.LIST } ?: false

fun isKeyFieldMeta(fieldMeta: EntityModel<Resolved>?): Boolean = fieldMeta?.let {
    getOptionalSingleBoolean(fieldMeta, "is_key") ?: false
} ?: false

fun isCompositionFieldMeta(fieldMeta: EntityModel<Resolved>?): Boolean =
    getFieldTypeMeta(fieldMeta) == FieldType.COMPOSITION

private fun findListElement(list: ListFieldModel<Resolved>, name: String) =
    list.values.find { entity ->
        if (entity !is EntityModel<Resolved>) false
        else getSingleString(entity, "name") == name
    } as? EntityModel<Resolved> ?: throw IllegalStateException()
