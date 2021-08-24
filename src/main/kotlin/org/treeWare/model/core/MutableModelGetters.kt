package org.treeWare.model.core

fun <Aux> getMutableSingleEntity(meta: MutableBaseEntityModel<Aux>, fieldName: String): MutableEntityModel<Aux> {
    val singleField = getMutableSingleField(meta, fieldName)
    return singleField.value as? MutableEntityModel<Aux> ?: throw IllegalStateException()
}

fun <Aux> getMutableSingleField(
    meta: MutableBaseEntityModel<Aux>,
    fieldName: String
): MutableSingleFieldModel<Aux> {
    return meta.getField(fieldName) as? MutableSingleFieldModel<Aux> ?: throw IllegalStateException()
}

fun <Aux> getMutableListField(meta: MutableBaseEntityModel<Aux>, fieldName: String): MutableListFieldModel<Aux> {
    return meta.getField(fieldName) as? MutableListFieldModel<Aux> ?: throw IllegalStateException()
}
