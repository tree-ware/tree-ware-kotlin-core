package org.treeWare.model.core

import org.treeWare.metaModel.getMetaName
import org.treeWare.metaModel.getRootMetaName

fun getRootName(rootModel: RootModel): String? = rootModel.parent.meta?.let { getRootMetaName(it) }

fun getSingleEntity(entityModel: BaseEntityModel, fieldName: String): EntityModel {
    val singleField = getSingleField(entityModel, fieldName)
    return singleField.value as? EntityModel ?: throw IllegalStateException()
}

fun getSingleString(entityModel: BaseEntityModel, fieldName: String): String {
    val singleField = getSingleField(entityModel, fieldName)
    val primitive = singleField.value as? PrimitiveModel ?: throw IllegalStateException()
    return primitive.value as? String ?: throw IllegalStateException()
}

fun getOptionalSingleBoolean(entityModel: BaseEntityModel, fieldName: String): Boolean? {
    val singleField = getOptionalSingleField(entityModel, fieldName) ?: return null
    val primitive = singleField.value as? PrimitiveModel ?: throw IllegalStateException()
    return primitive.value as? Boolean ?: throw IllegalStateException()
}

fun getSingleEnumeration(entityModel: BaseEntityModel, fieldName: String): String {
    val singleField = getSingleField(entityModel, fieldName)
    val enumeration = singleField.value as? EnumerationModel ?: throw IllegalStateException()
    return enumeration.value ?: throw IllegalStateException()
}

fun getOptionalSingleEnumeration(entityModel: BaseEntityModel, fieldName: String): String? {
    val singleField = getOptionalSingleField(entityModel, fieldName) ?: return null
    val enumeration = singleField.value as? EnumerationModel ?: throw IllegalStateException()
    return enumeration.value ?: throw IllegalStateException()
}

fun getSingleField(entityModel: BaseEntityModel, fieldName: String): SingleFieldModel =
    entityModel.getField(fieldName) as? SingleFieldModel ?: throw IllegalStateException()

fun getOptionalSingleField(entityModel: BaseEntityModel, fieldName: String): SingleFieldModel? =
    entityModel.getField(fieldName) as? SingleFieldModel?

fun getListField(entityModel: BaseEntityModel, fieldName: String): ListFieldModel =
    entityModel.getField(fieldName) as? ListFieldModel ?: throw IllegalStateException()

fun getCollectionField(entityModel: BaseEntityModel, fieldName: String): CollectionFieldModel =
    entityModel.getField(fieldName) as? CollectionFieldModel ?: throw IllegalStateException()

fun getFieldName(fieldModel: FieldModel): String =
    fieldModel.meta?.let { getMetaName(it) } ?: throw IllegalStateException()

fun getListStrings(listFieldModel: ListFieldModel): List<String> {
    val firstElement = listFieldModel.values.firstOrNull() ?: return listOf()
    val firstPrimitive = firstElement as? PrimitiveModel ?: throw IllegalStateException()
    firstPrimitive.value as? String ?: throw IllegalStateException()
    return listFieldModel.values.map { (it as PrimitiveModel).value as String }
}