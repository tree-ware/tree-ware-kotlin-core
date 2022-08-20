package org.treeWare.model.core

fun getMutableSingleEntity(entityModel: MutableBaseEntityModel, fieldName: String): MutableEntityModel {
    val singleField = getMutableSingleField(entityModel, fieldName)
    return singleField.value as? MutableEntityModel ?: throw IllegalStateException()
}

fun getMutableSingleField(entityModel: MutableBaseEntityModel, fieldName: String): MutableSingleFieldModel =
    entityModel.getField(fieldName) as? MutableSingleFieldModel ?: throw IllegalStateException()

fun getMutableCollectionField(entityModel: MutableBaseEntityModel, fieldName: String): MutableCollectionFieldModel =
    entityModel.getField(fieldName) as? MutableCollectionFieldModel ?: throw IllegalStateException()

fun forEachEntity(setField: MutableSetFieldModel, body: (entity: MutableEntityModel) -> Unit) {
    setField.values.forEach { element ->
        val entity = element as MutableEntityModel
        body(entity)
    }
}