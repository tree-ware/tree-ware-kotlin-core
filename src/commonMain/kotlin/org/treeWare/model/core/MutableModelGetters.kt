package org.treeWare.model.core

fun getMutableSingleEntity(entityModel: MutableEntityModel, fieldName: String): MutableEntityModel {
    val singleField = getMutableSingleField(entityModel, fieldName)
    return singleField.value as? MutableEntityModel ?: throw IllegalStateException()
}

fun getMutableSingleField(entityModel: MutableEntityModel, fieldName: String): MutableSingleFieldModel =
    entityModel.getField(fieldName) as? MutableSingleFieldModel ?: throw IllegalStateException()

fun getMutableCollectionField(entityModel: MutableEntityModel, fieldName: String): MutableCollectionFieldModel =
    entityModel.getField(fieldName) as? MutableCollectionFieldModel ?: throw IllegalStateException()

fun forEachEntity(setField: MutableSetFieldModel, body: (entity: MutableEntityModel) -> Unit) {
    setField.values.forEach { element ->
        val entity = element as MutableEntityModel
        body(entity)
    }
}