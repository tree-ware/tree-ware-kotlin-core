package org.treeWare.model.core

import org.treeWare.schema.core.EnumerationValueSchema

fun <Aux> getSingleEntity(meta: BaseEntityModel<Aux>, fieldName: String): EntityModel<Aux> {
    val singleField = getSingleField(meta, fieldName)
    return singleField.value as? EntityModel<Aux> ?: throw IllegalStateException()
}

fun <Aux> getSingleString(meta: BaseEntityModel<Aux>, fieldName: String): String {
    val singleField = getSingleField(meta, fieldName)
    val primitive = singleField.value as? PrimitiveModel<Aux> ?: throw IllegalStateException()
    return primitive.value as? String ?: throw IllegalStateException()
}

fun <Aux> getSingleEnumeration(meta: BaseEntityModel<Aux>, fieldName: String): EnumerationValueSchema {
    val singleField = getSingleField(meta, fieldName)
    val enumeration = singleField.value as? EnumerationModel<Aux> ?: throw IllegalStateException()
    return enumeration.value ?: throw IllegalStateException()
}

fun <Aux> getOptionalSingleEnumeration(
    meta: BaseEntityModel<Aux>,
    fieldName: String
): EnumerationValueSchema? {
    val singleField = getOptionalSingleField(meta, fieldName) ?: return null
    val enumeration = singleField.value as? EnumerationModel<Aux> ?: throw IllegalStateException()
    return enumeration.value ?: throw IllegalStateException()
}

fun <Aux> getSingleField(meta: BaseEntityModel<Aux>, fieldName: String): SingleFieldModel<Aux> {
    return meta.getField(fieldName) as? SingleFieldModel<Aux> ?: throw IllegalStateException()
}

fun <Aux> getOptionalSingleField(
    meta: BaseEntityModel<Aux>,
    fieldName: String
): SingleFieldModel<Aux>? {
    return meta.getField(fieldName) as? SingleFieldModel<Aux>?
}

fun <Aux> getListField(meta: BaseEntityModel<Aux>, fieldName: String): ListFieldModel<Aux> {
    return meta.getField(fieldName) as? ListFieldModel<Aux> ?: throw IllegalStateException()
}
