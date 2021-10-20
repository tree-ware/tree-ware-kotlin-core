package org.treeWare.model.core

import org.treeWare.metaModel.getMetaName

fun getSingleEntity(meta: BaseEntityModel, fieldName: String): EntityModel {
    val singleField = getSingleField(meta, fieldName)
    return singleField.value as? EntityModel ?: throw IllegalStateException()
}

fun getSingleString(meta: BaseEntityModel, fieldName: String): String {
    val singleField = getSingleField(meta, fieldName)
    val primitive = singleField.value as? PrimitiveModel ?: throw IllegalStateException()
    return primitive.value as? String ?: throw IllegalStateException()
}

fun getOptionalSingleBoolean(meta: BaseEntityModel, fieldName: String): Boolean? {
    val singleField = getOptionalSingleField(meta, fieldName) ?: return null
    val primitive = singleField.value as? PrimitiveModel ?: throw IllegalStateException()
    return primitive.value as? Boolean ?: throw IllegalStateException()
}

fun getSingleEnumeration(meta: BaseEntityModel, fieldName: String): String {
    val singleField = getSingleField(meta, fieldName)
    val enumeration = singleField.value as? EnumerationModel ?: throw IllegalStateException()
    return enumeration.value ?: throw IllegalStateException()
}

fun getOptionalSingleEnumeration(
    meta: BaseEntityModel,
    fieldName: String
): String? {
    val singleField = getOptionalSingleField(meta, fieldName) ?: return null
    val enumeration = singleField.value as? EnumerationModel ?: throw IllegalStateException()
    return enumeration.value ?: throw IllegalStateException()
}

fun getSingleField(meta: BaseEntityModel, fieldName: String): SingleFieldModel {
    return meta.getField(fieldName) as? SingleFieldModel ?: throw IllegalStateException()
}

fun getOptionalSingleField(
    meta: BaseEntityModel,
    fieldName: String
): SingleFieldModel? {
    return meta.getField(fieldName) as? SingleFieldModel?
}

fun getListField(meta: BaseEntityModel, fieldName: String): ListFieldModel {
    return meta.getField(fieldName) as? ListFieldModel ?: throw IllegalStateException()
}

fun getCollectionField(meta: BaseEntityModel, fieldName: String): CollectionFieldModel {
    return meta.getField(fieldName) as? CollectionFieldModel ?: throw IllegalStateException()
}

fun getFieldName(field: FieldModel): String =
    field.meta?.let { getMetaName(it) } ?: throw IllegalStateException()

fun getListStrings(listFieldMeta: ListFieldModel): List<String> {
    val firstElement = listFieldMeta.values.firstOrNull() ?: return listOf()
    val firstPrimitive = firstElement as? PrimitiveModel ?: throw IllegalStateException()
    firstPrimitive.value as? String ?: throw IllegalStateException()
    return listFieldMeta.values.map { (it as PrimitiveModel).value as String }
}
