package org.treeWare.metaModel

import org.treeWare.model.core.*

fun getRootMeta(mainMeta: MainModel<Resolved>): EntityModel<Resolved> = getSingleEntity(mainMeta.root, "root")

fun getPackagesMeta(mainMeta: MainModel<Resolved>): CollectionFieldModel<Resolved> =
    getCollectionField(mainMeta.root, "packages")

fun getEnumerationsMeta(packageMeta: EntityModel<Resolved>): CollectionFieldModel<Resolved>? =
    runCatching { getCollectionField(packageMeta, "enumerations") }.getOrNull()

fun getEnumerationValuesMeta(enumerationMeta: EntityModel<Resolved>): CollectionFieldModel<Resolved> =
    getCollectionField(enumerationMeta, "values")

fun getEnumerationValues(enumerationMeta: EntityModel<Resolved>): List<String> =
    getEnumerationValuesMeta(enumerationMeta).values.map {
        val valueMeta = it as? EntityModel<Resolved> ?: throw IllegalStateException()
        getMetaName(valueMeta)
    }

fun getEntitiesMeta(packageMeta: EntityModel<Resolved>): CollectionFieldModel<Resolved>? =
    runCatching { getCollectionField(packageMeta, "entities") }.getOrNull()

fun getFieldsMeta(entityMeta: EntityModel<Resolved>): CollectionFieldModel<Resolved> =
    getCollectionField(entityMeta, "fields")

fun getFieldMeta(entityMeta: EntityModel<Resolved>, fieldName: String): EntityModel<Resolved> {
    val fields = getCollectionField(entityMeta, "fields")
    return fields.values.find { entity ->
        if (entity !is EntityModel<Resolved>) false
        else getSingleString(entity, "name") == fieldName
    } as? EntityModel<Resolved> ?: throw IllegalStateException()
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

fun filterKeyFields(fields: Collection<ElementModel<Resolved>>): List<EntityModel<Resolved>> =
    fields.mapNotNull { fieldElement ->
        val fieldMeta = fieldElement as? EntityModel<Resolved>
        fieldMeta.takeIf { isKeyFieldMeta(it) }
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

fun isSetFieldMeta(fieldMeta: EntityModel<Resolved>?): Boolean =
    fieldMeta?.let { getMultiplicityMeta(fieldMeta) == Multiplicity.SET } ?: false

fun isKeyFieldMeta(fieldMeta: EntityModel<Resolved>?): Boolean = fieldMeta?.let {
    getOptionalSingleBoolean(fieldMeta, "is_key") ?: false
} ?: false

fun isCompositionFieldMeta(fieldMeta: EntityModel<Resolved>?): Boolean =
    getFieldTypeMeta(fieldMeta) == FieldType.COMPOSITION
