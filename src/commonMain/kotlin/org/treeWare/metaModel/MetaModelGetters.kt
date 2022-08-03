package org.treeWare.metaModel

import org.treeWare.model.core.*

fun getFullName(elementMeta: ElementModel?): String? = getMetaModelResolved(elementMeta)?.fullName

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
        if (valueMeta !is EntityModel) false else getMetaName(valueMeta) == name
    } as? EntityModel
}

fun getEnumerationValueMeta(enumerationMeta: EntityModel, number: UInt): EntityModel? {
    return getEnumerationValuesMeta(enumerationMeta).values.find { valueMeta ->
        if (valueMeta !is EntityModel) false else getMetaNumber(valueMeta) == number
    } as? EntityModel
}

fun getEntitiesMeta(packageMeta: EntityModel): CollectionFieldModel? =
    runCatching { getCollectionField(packageMeta, "entities") }.getOrNull()

fun isEntityMeta(entityMeta: BaseEntityModel): Boolean =
    getMetaModelFullName(entityMeta) == "/tree_ware_meta_model.main/entity"

fun getPackageName(entityMeta: EntityModel): String = getMetaName(entityMeta.parent.parent)

fun getFieldNames(entityMeta: EntityModel): List<String> =
    getFieldsMeta(entityMeta).values.map { getMetaName(it as EntityModel) }

fun getFieldsMeta(entityMeta: EntityModel): CollectionFieldModel =
    getCollectionField(entityMeta, "fields")

fun getFieldMeta(entityMeta: EntityModel, fieldName: String): EntityModel {
    val fields = getFieldsMeta(entityMeta)
    return fields.values.find { fieldMeta ->
        if (fieldMeta !is EntityModel) false else getMetaName(fieldMeta) == fieldName
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

fun getKeyFieldsMeta(entityMeta: EntityModel): List<EntityModel> {
    val entityResolved = getMetaModelResolved(entityMeta)
        ?: throw IllegalStateException("Resolved aux is missing in entity ${getMetaName(entityMeta)}")
    return entityResolved.sortedKeyFieldsMeta
}

private fun filterKeyFields(fields: Collection<ElementModel>): List<EntityModel> =
    fields.mapNotNull { fieldElement ->
        val fieldMeta = fieldElement as? EntityModel
        fieldMeta.takeIf { isKeyFieldMeta(it) }
    }

private fun filterCompositionKeyFields(fields: List<ElementModel>): List<ElementModel> =
    fields.filter { fieldElement ->
        val fieldMeta = fieldElement as? EntityModel
        fieldMeta?.let { isKeyFieldMeta(it) && isCompositionFieldMeta(it) } ?: false
    }

fun getRequiredNonKeyFieldsMeta(entityMeta: EntityModel): List<EntityModel> =
    getFieldsMeta(entityMeta).values.mapNotNull { fieldElement ->
        (fieldElement as? EntityModel)?.takeIf { isRequiredFieldMeta(it) && !isKeyFieldMeta(it) }
    }

fun getUniquesMeta(entityMeta: EntityModel): CollectionFieldModel? =
    runCatching { getCollectionField(entityMeta, "uniques") }.getOrNull()

// TODO(cleanup): getMetaName() should return `String?`. Callers should handle the null value as applicable.
fun getMetaName(meta: BaseEntityModel?): String = meta?.let { getSingleString(it, "name") } ?: ""

fun getMetaNumber(meta: BaseEntityModel?): UInt? = meta?.let { getOptionalSingleUint32(it, "number") }

fun getFieldTypeMeta(fieldMeta: EntityModel?): FieldType? = fieldMeta?.let {
    FieldType.valueOf(getSingleEnumeration(fieldMeta, "type").uppercase())
}

fun getParentEntityMeta(fieldMeta: EntityModel): BaseEntityModel? = fieldMeta.parent.parent

fun getEnumerationInfoMeta(fieldMeta: EntityModel): EntityModel =
    getSingleEntity(fieldMeta, "enumeration")

fun getEntityInfoMeta(fieldMeta: EntityModel, entityInfoFor: String): EntityModel =
    getSingleEntity(fieldMeta, entityInfoFor)

fun getMultiplicityMeta(fieldMeta: EntityModel): Multiplicity =
    Multiplicity.valueOf((getOptionalSingleEnumeration(fieldMeta, "multiplicity") ?: "required").uppercase())

fun isCollectionFieldMeta(fieldMeta: EntityModel?): Boolean = fieldMeta?.let {
    val multiplicity = getMultiplicityMeta(fieldMeta)
    multiplicity == Multiplicity.LIST || multiplicity == Multiplicity.SET
} ?: false

fun isRequiredFieldMeta(fieldMeta: EntityModel?): Boolean =
    fieldMeta?.let { getMultiplicityMeta(fieldMeta) == Multiplicity.REQUIRED } ?: false

fun isListFieldMeta(fieldMeta: EntityModel?): Boolean =
    fieldMeta?.let { getMultiplicityMeta(fieldMeta) == Multiplicity.LIST } ?: false

fun isSetFieldMeta(fieldMeta: EntityModel?): Boolean =
    fieldMeta?.let { getMultiplicityMeta(fieldMeta) == Multiplicity.SET } ?: false

fun isKeyFieldMeta(fieldMeta: EntityModel?): Boolean = fieldMeta?.let {
    getOptionalSingleBoolean(fieldMeta, "is_key") ?: false
} ?: false

fun isCompositionFieldMeta(fieldMeta: EntityModel?): Boolean =
    getFieldTypeMeta(fieldMeta) == FieldType.COMPOSITION

fun isAssociationFieldMeta(fieldMeta: EntityModel?): Boolean =
    getFieldTypeMeta(fieldMeta) == FieldType.ASSOCIATION

// Constraints

fun getMinSizeConstraint(fieldMeta: EntityModel): UInt? = getOptionalSingleUint32(fieldMeta, "min_size")

fun getMaxSizeConstraint(fieldMeta: EntityModel): UInt? = getOptionalSingleUint32(fieldMeta, "max_size")

fun getRegexConstraint(fieldMeta: EntityModel): String? = getOptionalSingleString(fieldMeta, "regex")