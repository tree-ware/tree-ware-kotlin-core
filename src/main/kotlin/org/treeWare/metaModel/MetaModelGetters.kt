package org.treeWare.metaModel

import org.treeWare.model.core.*

fun getMainMetaName(mainMeta: MainModel): String = getMetaName(getRootMeta(mainMeta))

fun getRootMeta(mainMeta: MainModel): EntityModel = getSingleEntity(mainMeta.root, "root")

fun getPackagesMeta(mainMeta: MainModel): CollectionFieldModel =
    getCollectionField(mainMeta.root, "packages")

fun getEnumerationsMeta(packageMeta: EntityModel): CollectionFieldModel? =
    runCatching { getCollectionField(packageMeta, "enumerations") }.getOrNull()

fun getEnumerationValuesMeta(enumerationMeta: EntityModel): CollectionFieldModel =
    getCollectionField(enumerationMeta, "values")

fun getEnumerationValueMeta(enumerationMeta: EntityModel, name: String): EntityModel? {
    return getEnumerationValuesMeta(enumerationMeta).values.find { valueMeta ->
        if (valueMeta !is EntityModel) false
        else getMetaName(valueMeta) == name
    } as? EntityModel
}

fun getEntitiesMeta(packageMeta: EntityModel): CollectionFieldModel? =
    runCatching { getCollectionField(packageMeta, "entities") }.getOrNull()

fun getPackageName(entityMeta: EntityModel): String = getMetaName(entityMeta.parent.parent)

fun getFieldNames(entityMeta: EntityModel): List<String> =
    getFieldsMeta(entityMeta).values.map { getMetaName(it as EntityModel) }

fun getFieldsMeta(entityMeta: EntityModel): CollectionFieldModel =
    getCollectionField(entityMeta, "fields")

fun getFieldMeta(entityMeta: EntityModel, fieldName: String): EntityModel {
    val fields = getCollectionField(entityMeta, "fields")
    return fields.values.find { entity ->
        if (entity !is EntityModel) false
        else getSingleString(entity, "name") == fieldName
    } as? EntityModel ?: throw IllegalStateException("Field $fieldName not found in entity ${getMetaName(entityMeta)}")
}

fun hasKeyFields(entityMeta: EntityModel): Boolean = getFieldsMeta(entityMeta).values.any { fieldElement ->
    val fieldMeta = fieldElement as? EntityModel
    fieldMeta?.let { isKeyFieldMeta(it) } ?: false
}

fun hasOnlyPrimitiveKeyFields(entityMeta: EntityModel): Boolean {
    val fields = getFieldsMeta(entityMeta).values
    val keyFields = filterKeyFields(fields)
    val compositionKeyFields = filterCompositionKeyFields(keyFields)
    return keyFields.size == fields.size && compositionKeyFields.isEmpty()
}

fun filterKeyFields(fields: Collection<ElementModel>): List<EntityModel> =
    fields.mapNotNull { fieldElement ->
        val fieldMeta = fieldElement as? EntityModel
        fieldMeta.takeIf { isKeyFieldMeta(it) }
    }

private fun filterCompositionKeyFields(fields: List<ElementModel>): List<ElementModel> =
    fields.filter { fieldElement ->
        val fieldMeta = fieldElement as? EntityModel
        fieldMeta?.let { isKeyFieldMeta(it) && isCompositionFieldMeta(it) } ?: false
    }

fun getMetaName(meta: BaseEntityModel?): String = meta?.let { getSingleString(meta, "name") } ?: ""

fun getFieldTypeMeta(fieldMeta: EntityModel?): FieldType? = fieldMeta?.let {
    FieldType.valueOf(getSingleEnumeration(fieldMeta, "type").uppercase())
}

fun getEnumerationInfoMeta(fieldMeta: EntityModel): EntityModel =
    getSingleEntity(fieldMeta, "enumeration")

fun getAssociationInfoMeta(fieldMeta: EntityModel): ListFieldModel =
    getListField(fieldMeta, "association")

fun getEntityInfoMeta(fieldMeta: EntityModel): EntityModel =
    getSingleEntity(fieldMeta, "composition")

fun getMultiplicityMeta(fieldMeta: EntityModel): Multiplicity =
    Multiplicity.valueOf((getOptionalSingleEnumeration(fieldMeta, "multiplicity") ?: "required").uppercase())

fun isListFieldMeta(fieldMeta: EntityModel?): Boolean =
    fieldMeta?.let { getMultiplicityMeta(fieldMeta) == Multiplicity.LIST } ?: false

fun isSetFieldMeta(fieldMeta: EntityModel?): Boolean =
    fieldMeta?.let { getMultiplicityMeta(fieldMeta) == Multiplicity.SET } ?: false

fun isKeyFieldMeta(fieldMeta: EntityModel?): Boolean = fieldMeta?.let {
    getOptionalSingleBoolean(fieldMeta, "is_key") ?: false
} ?: false

fun isCompositionFieldMeta(fieldMeta: EntityModel?): Boolean =
    getFieldTypeMeta(fieldMeta) == FieldType.COMPOSITION