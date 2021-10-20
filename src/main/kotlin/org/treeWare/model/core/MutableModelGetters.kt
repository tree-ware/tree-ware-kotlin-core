package org.treeWare.model.core

fun getMutableSingleEntity(meta: MutableBaseEntityModel, fieldName: String): MutableEntityModel {
    val singleField = getMutableSingleField(meta, fieldName)
    return singleField.value as? MutableEntityModel ?: throw IllegalStateException()
}

fun getMutableSingleField(
    meta: MutableBaseEntityModel,
    fieldName: String
): MutableSingleFieldModel {
    return meta.getField(fieldName) as? MutableSingleFieldModel ?: throw IllegalStateException()
}

fun getMutableCollectionField(
    meta: MutableBaseEntityModel,
    fieldName: String
): MutableCollectionFieldModel {
    return meta.getField(fieldName) as? MutableCollectionFieldModel ?: throw IllegalStateException()
}
