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

fun <Aux> getMutableCollectionField(
    meta: MutableBaseEntityModel<Aux>,
    fieldName: String
): MutableCollectionFieldModel<Aux> {
    return meta.getField(fieldName) as? MutableCollectionFieldModel<Aux> ?: throw IllegalStateException()
}
