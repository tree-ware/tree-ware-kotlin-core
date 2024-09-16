package org.treeWare.metaModel

import org.treeWare.model.core.*

fun getFullName(elementMeta: ElementModel?): String? = getMetaModelResolved(elementMeta)?.fullName

fun getMetaModelName(meta: EntityModel): String = getMetaName(meta)

fun getMetaModelPackageName(meta: EntityModel): String = getSingleString(meta, "package")

fun getVersionMeta(meta: EntityModel): EntityModel {
    return getSingleEntity(meta, "version")
}

/**
 * Returns the resolved root meta. This is the meta to be used for the root entity of the *model*.
 * NOTE: this is different than the getRootMeta() function which returns the unresolved root entity of the meta-model.
 */
fun getResolvedRootMeta(meta: EntityModel): EntityModel =
    getMetaModelResolved(meta)?.compositionMeta ?: throw IllegalStateException("Root entity not found")

fun getRootMeta(meta: EntityModel): EntityModel {
    return getSingleEntity(meta, "root")
}

fun getPackagesMeta(meta: EntityModel): CollectionFieldModel {
    return getCollectionField(meta, "packages")
}

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

fun getParentEnumerationMeta(enumerationValueMeta: EntityModel): EntityModel? = enumerationValueMeta.parent?.parent

fun getEntitiesMeta(packageMeta: EntityModel): CollectionFieldModel? =
    runCatching { getCollectionField(packageMeta, "entities") }.getOrNull()

fun isEntityMeta(entityMeta: EntityModel): Boolean =
    getMetaModelFullName(entityMeta) == "/org.tree_ware.meta_model.main/entity"

fun getPackageName(entityMeta: EntityModel): String = getMetaName(entityMeta.parent?.parent)

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

fun getUniquesMeta(entityMeta: EntityModel): CollectionFieldModel? =
    runCatching { getCollectionField(entityMeta, "uniques") }.getOrNull()

// TODO(cleanup): getMetaName() should return `String?`. Callers should handle the null value as applicable.
fun getMetaName(meta: EntityModel?): String = meta?.let { getSingleString(it, "name") } ?: ""

fun getMetaNumber(meta: EntityModel?): UInt? = meta?.let { getOptionalSingleUint32(it, "number") }

fun getMetaInfo(meta: EntityModel?): String? = meta?.let { getOptionalSingleString(it, "info") }

fun getFieldTypeMeta(fieldMeta: EntityModel?): FieldType? = fieldMeta?.let {
    FieldType.valueOf(getSingleEnumeration(fieldMeta, "type").uppercase())
}

fun getParentEntityMeta(fieldMeta: EntityModel): EntityModel? = fieldMeta.parent?.parent

fun getEnumerationInfoMeta(fieldMeta: EntityModel): EntityModel =
    getSingleEntity(fieldMeta, "enumeration")

fun getEntityInfoMeta(fieldMeta: EntityModel, entityInfoFor: String): EntityModel =
    getSingleEntity(fieldMeta, entityInfoFor)

fun getMultiplicityMeta(fieldMeta: EntityModel): Multiplicity =
    Multiplicity.valueOf((getOptionalSingleEnumeration(fieldMeta, "multiplicity") ?: "required").uppercase())

fun isCollectionFieldMeta(fieldMeta: EntityModel?): Boolean = fieldMeta?.let {
    val multiplicity = getMultiplicityMeta(fieldMeta)
    multiplicity == Multiplicity.SET
} ?: false

fun isRequiredFieldMeta(fieldMeta: EntityModel?): Boolean =
    fieldMeta?.let { getMultiplicityMeta(it) == Multiplicity.REQUIRED } ?: false

fun isConditionalFieldMeta(fieldMeta: EntityModel?): Boolean =
    fieldMeta?.let { getExistsIfMeta(it) != null } ?: false

fun isUnconditionallyRequiredFieldMeta(fieldMeta: EntityModel?): Boolean =
    isRequiredFieldMeta(fieldMeta) && !isConditionalFieldMeta(fieldMeta)

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

fun getExistsIfMeta(fieldMeta: EntityModel): EntityModel? = getOptionalSingleEntity(fieldMeta, "exists_if")

fun getGranularityMeta(fieldMeta: EntityModel): Granularity? =
    getOptionalSingleEnumeration(fieldMeta, "granularity")?.let { Granularity.valueOf(it.uppercase()) }