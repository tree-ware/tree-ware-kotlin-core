package org.treeWare.metaModel

import org.treeWare.model.core.MutableCollectionFieldModel
import org.treeWare.model.core.MutableEntityModel
import org.treeWare.model.core.getMutableCollectionField
import org.treeWare.model.core.getMutableSingleEntity

fun getMutableRootMeta(meta: MutableEntityModel): MutableEntityModel {
    return getMutableSingleEntity(meta, "root")
}

fun getMutablePackagesMeta(meta: MutableEntityModel): MutableCollectionFieldModel {
    return getMutableCollectionField(meta, "packages")
}

fun getMutableEnumerationsMeta(packageMeta: MutableEntityModel): MutableCollectionFieldModel? =
    runCatching { getMutableCollectionField(packageMeta, "enumerations") }.getOrNull()

fun getMutableEnumerationValuesMeta(enumerationMeta: MutableEntityModel): MutableCollectionFieldModel =
    getMutableCollectionField(enumerationMeta, "values")

fun getMutableEntitiesMeta(packageMeta: MutableEntityModel): MutableCollectionFieldModel? =
    runCatching { getMutableCollectionField(packageMeta, "entities") }.getOrNull()

fun getMutableFieldsMeta(entityMeta: MutableEntityModel): MutableCollectionFieldModel =
    getMutableCollectionField(entityMeta, "fields")
