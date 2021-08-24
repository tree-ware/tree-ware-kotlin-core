package org.treeWare.model.core

import org.treeWare.schema.core.EnumerationValueSchema

fun getSingleEntity(meta: BaseEntityModel<Resolved>, fieldName: String): EntityModel<Resolved> {
    val singleField = getSingleField(meta, fieldName)
    return singleField.value as? EntityModel<Resolved> ?: throw IllegalStateException()
}

fun getSingleString(meta: BaseEntityModel<Resolved>, fieldName: String): String {
    val singleField = getSingleField(meta, fieldName)
    val primitive = singleField.value as? PrimitiveModel<Resolved> ?: throw IllegalStateException()
    return primitive.value as? String ?: throw IllegalStateException()
}

fun getSingleEnumeration(meta: BaseEntityModel<Resolved>, fieldName: String): EnumerationValueSchema {
    val singleField = getSingleField(meta, fieldName)
    val enumeration = singleField.value as? EnumerationModel<Resolved> ?: throw IllegalStateException()
    return enumeration.value ?: throw IllegalStateException()
}

fun getOptionalSingleEnumeration(
    meta: BaseEntityModel<Resolved>,
    fieldName: String
): EnumerationValueSchema? {
    val singleField = getOptionalSingleField(meta, fieldName) ?: return null
    val enumeration = singleField.value as? EnumerationModel<Resolved> ?: throw IllegalStateException()
    return enumeration.value ?: throw IllegalStateException()
}

fun getSingleField(meta: BaseEntityModel<Resolved>, fieldName: String): SingleFieldModel<Resolved> {
    return meta.getField(fieldName) as? SingleFieldModel<Resolved> ?: throw IllegalStateException()
}

fun getOptionalSingleField(
    meta: BaseEntityModel<Resolved>,
    fieldName: String
): SingleFieldModel<Resolved>? {
    return meta.getField(fieldName) as? SingleFieldModel<Resolved>?
}

fun getListField(meta: BaseEntityModel<Resolved>, fieldName: String): ListFieldModel<Resolved> {
    return meta.getField(fieldName) as? ListFieldModel<Resolved> ?: throw IllegalStateException()
}
