package org.treeWare.model.core

import org.treeWare.metaModel.*

fun getMetaModelFullName(element: ElementModel): String? = getMetaModelResolved(element.meta)?.fullName

fun isRootEntity(entityModel: EntityModel): Boolean = entityModel.parent == null

fun isCompositionKey(entityModel: EntityModel): Boolean = entityModel.parent?.let { isKeyField(it) } ?: false

fun getEntityFieldName(entityModel: EntityModel): String? = entityModel.parent?.meta?.let { getMetaName(it) }

fun getSingleEntity(entityModel: BaseEntityModel, fieldName: String): EntityModel {
    val singleField = getSingleField(entityModel, fieldName)
    return singleField.value as? EntityModel ?: throw IllegalStateException()
}

fun getOptionalSingleEntity(entityModel: BaseEntityModel, fieldName: String): EntityModel? {
    val singleField = getOptionalSingleField(entityModel, fieldName) ?: return null
    return singleField.value as? EntityModel ?: throw IllegalStateException()
}

fun getSingleString(entityModel: BaseEntityModel, fieldName: String): String {
    val singleField = getSingleField(entityModel, fieldName)
    val primitive = singleField.value as? PrimitiveModel ?: throw IllegalStateException()
    return primitive.value as? String ?: throw IllegalStateException()
}

fun getOptionalSingleString(entityModel: BaseEntityModel, fieldName: String): String? {
    val primitive = getOptionalSinglePrimitive(entityModel, fieldName) ?: return null
    return primitive.value as? String ?: throw IllegalStateException()
}

fun getSingleBoolean(entityModel: BaseEntityModel, fieldName: String): Boolean {
    val primitive = getSinglePrimitive(entityModel, fieldName)
    return primitive.value as? Boolean ?: throw IllegalStateException()
}

fun getOptionalSingleBoolean(entityModel: BaseEntityModel, fieldName: String): Boolean? {
    val primitive = getOptionalSinglePrimitive(entityModel, fieldName) ?: return null
    return primitive.value as? Boolean ?: throw IllegalStateException()
}

fun getOptionalSingleUint32(entityModel: BaseEntityModel, fieldName: String): UInt? {
    val primitive = getOptionalSinglePrimitive(entityModel, fieldName) ?: return null
    return primitive.value as? UInt ?: throw IllegalStateException()
}

fun getSingleEnumeration(entityModel: BaseEntityModel, fieldName: String): String {
    val singleField = getSingleField(entityModel, fieldName)
    val enumeration = singleField.value as? EnumerationModel ?: throw IllegalStateException()
    return enumeration.value
}

fun getOptionalSingleEnumeration(entityModel: BaseEntityModel, fieldName: String): String? {
    val singleField = getOptionalSingleField(entityModel, fieldName) ?: return null
    val enumeration = singleField.value as? EnumerationModel ?: throw IllegalStateException()
    return enumeration.value
}

fun getSingleDouble(entityModel: BaseEntityModel, fieldName: String): Double? {
    val singleField = getSingleField(entityModel, fieldName)
    val primitive = singleField.value as? PrimitiveModel ?: return null
    return primitive.value as? Double
}

fun getSingleField(entityModel: BaseEntityModel, fieldName: String): SingleFieldModel =
    entityModel.getField(fieldName) as? SingleFieldModel ?: throw IllegalStateException()

fun getSinglePrimitive(entityModel: BaseEntityModel, fieldName: String): PrimitiveModel =
    getSingleField(entityModel, fieldName).value as? PrimitiveModel ?: throw IllegalStateException()

fun getOptionalSingleField(entityModel: BaseEntityModel, fieldName: String): SingleFieldModel? =
    entityModel.getField(fieldName) as? SingleFieldModel?

fun getOptionalSinglePrimitive(entityModel: BaseEntityModel, fieldName: String): PrimitiveModel? {
    val singleField = getOptionalSingleField(entityModel, fieldName) ?: return null
    return singleField.value as? PrimitiveModel ?: throw IllegalStateException()
}

fun getCollectionField(entityModel: BaseEntityModel, fieldName: String): CollectionFieldModel =
    entityModel.getField(fieldName) as? CollectionFieldModel ?: throw IllegalStateException()

fun getFieldName(fieldModel: FieldModel): String =
    fieldModel.meta?.let { getMetaName(it) } ?: throw IllegalStateException()

fun getFieldNumber(fieldModel: FieldModel): UInt =
    fieldModel.meta?.let { getMetaNumber(it) } ?: throw IllegalStateException()

fun getFieldType(fieldModel: FieldModel): FieldType =
    fieldModel.meta?.let { getFieldTypeMeta(it) } ?: throw IllegalStateException()

fun isKeyField(fieldModel: FieldModel): Boolean =
    fieldModel.meta?.let { isKeyFieldMeta(it) } ?: throw IllegalStateException()

fun isListField(fieldModel: FieldModel): Boolean =
    fieldModel.meta?.let { isListFieldMeta(it) } ?: throw IllegalStateException()

fun isSetField(fieldModel: FieldModel): Boolean =
    fieldModel.meta?.let { isSetFieldMeta(it) } ?: throw IllegalStateException()

fun isCompositionField(fieldModel: FieldModel): Boolean =
    fieldModel.meta?.let { isCompositionFieldMeta(it) } ?: throw IllegalStateException()

fun isAssociationField(fieldModel: FieldModel): Boolean =
    fieldModel.meta?.let { isAssociationFieldMeta(it) } ?: throw IllegalStateException()

fun getAssociationTargetEntityMeta(fieldModel: FieldModel): EntityModel {
    if (!isAssociationField(fieldModel)) throw IllegalArgumentException("Not an association field")
    val fieldMeta = requireNotNull(fieldModel.meta) { "Field meta is missing" }
    return getMetaModelResolved(fieldMeta)?.associationMeta?.targetEntityMeta
        ?: throw IllegalStateException("Association meta-model is not resolved")
}

fun getCompositionEntityMeta(fieldModel: FieldModel): EntityModel {
    if (!isCompositionField(fieldModel)) throw IllegalArgumentException("Not a composition field")
    val fieldMeta = requireNotNull(fieldModel.meta) { "Field meta is missing" }
    val fieldResolved = requireNotNull(getMetaModelResolved(fieldMeta)) { "Field is not resolved" }
    return requireNotNull(fieldResolved.compositionMeta) { "Field composition is not resolved" }
}

fun forEachEntity(setField: SetFieldModel, body: (entity: EntityModel) -> Unit) {
    setField.values.forEach { element ->
        val entity = element as EntityModel
        body(entity)
    }
}