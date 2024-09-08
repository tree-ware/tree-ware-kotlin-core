package org.treeWare.model.core

fun getOrNewMutableSingleEntity(entityModel: MutableEntityModel, fieldName: String): MutableEntityModel {
    val singleField = getOrNewMutableSingleField(entityModel, fieldName)
    return singleField.getOrNewValue() as MutableEntityModel
}

fun getOrNewMutableSingleAssociation(entityModel: MutableEntityModel, fieldName: String): MutableAssociationModel {
    val singleField = getOrNewMutableSingleField(entityModel, fieldName)
    return singleField.getOrNewValue() as MutableAssociationModel
}

fun getNewMutableSetEntity(setField: MutableSetFieldModel): MutableEntityModel =
    setField.getOrNewValue() as MutableEntityModel

fun getOrNewMutableSingleField(entityModel: MutableEntityModel, fieldName: String): MutableSingleFieldModel =
    entityModel.getOrNewField(fieldName) as MutableSingleFieldModel

fun getOrNewMutableSetField(entityModel: MutableEntityModel, fieldName: String): MutableSetFieldModel =
    entityModel.getOrNewField(fieldName) as MutableSetFieldModel

fun setDoubleSingleField(
    entityModel: MutableEntityModel,
    fieldName: String,
    value: Double
): MutableSingleFieldModel {
    val field = getOrNewMutableSingleField(entityModel, fieldName)
    val primitive = field.getOrNewValue() as MutablePrimitiveModel
    primitive.value = value
    return field
}

fun setStringSingleField(
    entityModel: MutableEntityModel,
    fieldName: String,
    value: String
): MutableSingleFieldModel {
    val field = getOrNewMutableSingleField(entityModel, fieldName)
    val primitive = field.getOrNewValue() as MutablePrimitiveModel
    primitive.setValue(value)
    return field
}

fun setUuidSingleField(entityModel: MutableEntityModel, fieldName: String, value: String) {
    val field = getOrNewMutableSingleField(entityModel, fieldName)
    val primitive = field.getOrNewValue() as MutablePrimitiveModel
    primitive.setValue(value)
}

fun setTimestampSingleField(entityModel: MutableEntityModel, fieldName: String, value: ULong) {
    val field = getOrNewMutableSingleField(entityModel, fieldName)
    val primitive = field.getOrNewValue() as MutablePrimitiveModel
    primitive.value = value
}

fun setBooleanSingleField(entityModel: MutableEntityModel, fieldName: String, value: Boolean) {
    val field = getOrNewMutableSingleField(entityModel, fieldName)
    val primitive = field.getOrNewValue() as MutablePrimitiveModel
    primitive.setValue(value)
}

fun setPassword1waySingleField(
    entityModel: MutableEntityModel,
    fieldName: String,
    configure: MutablePassword1wayModel.() -> Unit
) {
    val field = getOrNewMutableSingleField(entityModel, fieldName)
    val password = field.getOrNewValue() as MutablePassword1wayModel
    password.configure()
}

fun setPassword2waySingleField(
    entityModel: MutableEntityModel,
    fieldName: String,
    configure: MutablePassword2wayModel.() -> Unit
) {
    val field = getOrNewMutableSingleField(entityModel, fieldName)
    val password = field.getOrNewValue() as MutablePassword2wayModel
    password.configure()
}

fun setEnumerationSingleField(
    entityModel: MutableEntityModel,
    fieldName: String,
    value: String
): MutableSingleFieldModel {
    val field = getOrNewMutableSingleField(entityModel, fieldName)
    val enumeration = field.getOrNewValue() as MutableEnumerationModel
    enumeration.setValue(value)
    return field
}