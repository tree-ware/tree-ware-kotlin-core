package org.treeWare.model.core

fun getOrNewMutableSingleEntity(entityModel: MutableBaseEntityModel, fieldName: String): MutableEntityModel {
    val singleField = getOrNewMutableSingleField(entityModel, fieldName)
    return singleField.getOrNewValue() as MutableEntityModel
}

fun getNewMutableSetEntity(setField: MutableSetFieldModel): MutableEntityModel =
    setField.getNewValue() as MutableEntityModel

fun getOrNewMutableSingleField(entityModel: MutableBaseEntityModel, fieldName: String): MutableSingleFieldModel =
    entityModel.getOrNewField(fieldName) as MutableSingleFieldModel

fun getOrNewMutableListField(entityModel: MutableBaseEntityModel, fieldName: String): MutableListFieldModel =
    entityModel.getOrNewField(fieldName) as MutableListFieldModel

fun getOrNewMutableSetField(entityModel: MutableBaseEntityModel, fieldName: String): MutableSetFieldModel =
    entityModel.getOrNewField(fieldName) as MutableSetFieldModel

fun setStringSingleField(entityModel: MutableBaseEntityModel, fieldName: String, value: String) {
    val field = getOrNewMutableSingleField(entityModel, fieldName)
    val primitive = field.getOrNewValue() as MutablePrimitiveModel
    primitive.value = value
}

fun setUuidSingleField(entityModel: MutableBaseEntityModel, fieldName: String, value: String) {
    val field = getOrNewMutableSingleField(entityModel, fieldName)
    val primitive = field.getOrNewValue() as MutablePrimitiveModel
    primitive.value = value
}

fun setTimestampSingleField(entityModel: MutableBaseEntityModel, fieldName: String, value: Long) {
    val field = getOrNewMutableSingleField(entityModel, fieldName)
    val primitive = field.getOrNewValue() as MutablePrimitiveModel
    primitive.value = value
}

fun setBooleanSingleField(entityModel: MutableBaseEntityModel, fieldName: String, value: Boolean) {
    val field = getOrNewMutableSingleField(entityModel, fieldName)
    val primitive = field.getOrNewValue() as MutablePrimitiveModel
    primitive.value = value
}

fun setEnumerationListField(entityModel: MutableBaseEntityModel, fieldName: String, vararg values: String) {
    val field = getOrNewMutableListField(entityModel, fieldName)
    values.forEach { value ->
        val primitive = field.getNewValue() as MutableEnumerationModel
        primitive.value = value
    }
}