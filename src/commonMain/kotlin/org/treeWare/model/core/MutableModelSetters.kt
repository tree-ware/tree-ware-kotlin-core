package org.treeWare.model.core

fun getOrNewMutableSingleEntity(entityModel: MutableBaseEntityModel, fieldName: String): MutableEntityModel {
    val singleField = getOrNewMutableSingleField(entityModel, fieldName)
    return singleField.getOrNewValue() as MutableEntityModel
}

fun getOrNewMutableSingleAssociation(entityModel: MutableBaseEntityModel, fieldName: String): MutableAssociationModel {
    val singleField = getOrNewMutableSingleField(entityModel, fieldName)
    return singleField.getOrNewValue() as MutableAssociationModel
}

fun getNewMutableSetEntity(setField: MutableSetFieldModel): MutableEntityModel =
    setField.getNewValue() as MutableEntityModel

fun getOrNewMutableSingleField(entityModel: MutableBaseEntityModel, fieldName: String): MutableSingleFieldModel =
    entityModel.getOrNewField(fieldName) as MutableSingleFieldModel

fun getOrNewMutableListField(entityModel: MutableBaseEntityModel, fieldName: String): MutableListFieldModel =
    entityModel.getOrNewField(fieldName) as MutableListFieldModel

fun getOrNewMutableSetField(entityModel: MutableBaseEntityModel, fieldName: String): MutableSetFieldModel =
    entityModel.getOrNewField(fieldName) as MutableSetFieldModel

fun setDoubleSingleField(
    entityModel: MutableBaseEntityModel,
    fieldName: String,
    value: Double
): MutableSingleFieldModel {
    val field = getOrNewMutableSingleField(entityModel, fieldName)
    val primitive = field.getOrNewValue() as MutablePrimitiveModel
    primitive.value = value
    return field
}

fun setStringSingleField(
    entityModel: MutableBaseEntityModel,
    fieldName: String,
    value: String
): MutableSingleFieldModel {
    val field = getOrNewMutableSingleField(entityModel, fieldName)
    val primitive = field.getOrNewValue() as MutablePrimitiveModel
    primitive.setValue(value)
    return field
}

fun setUuidSingleField(entityModel: MutableBaseEntityModel, fieldName: String, value: String) {
    val field = getOrNewMutableSingleField(entityModel, fieldName)
    val primitive = field.getOrNewValue() as MutablePrimitiveModel
    primitive.setValue(value)
}

fun setTimestampSingleField(entityModel: MutableBaseEntityModel, fieldName: String, value: Long) {
    val field = getOrNewMutableSingleField(entityModel, fieldName)
    val primitive = field.getOrNewValue() as MutablePrimitiveModel
    primitive.value = value
}

fun setBooleanSingleField(entityModel: MutableBaseEntityModel, fieldName: String, value: Boolean) {
    val field = getOrNewMutableSingleField(entityModel, fieldName)
    val primitive = field.getOrNewValue() as MutablePrimitiveModel
    primitive.setValue(value)
}

fun setEnumerationSingleField(
    entityModel: MutableBaseEntityModel,
    fieldName: String,
    value: String
): MutableSingleFieldModel {
    val field = getOrNewMutableSingleField(entityModel, fieldName)
    val enumeration = field.getOrNewValue() as MutableEnumerationModel
    enumeration.setValue(value)
    return field
}

fun addEnumerationListFieldElement(listField: MutableListFieldModel, value: String): MutableEnumerationModel {
    val enumeration = listField.getNewValue() as MutableEnumerationModel
    enumeration.setValue(value)
    return enumeration
}