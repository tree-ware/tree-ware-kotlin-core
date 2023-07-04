package org.treeWare.metaModel

import org.treeWare.model.core.*

fun getMutableRootMeta(mainMeta: MutableMainModel): MutableEntityModel {
    val mainMetaRoot = mainMeta.root ?: throw IllegalStateException("Root has not been set")
    return getMutableSingleEntity(mainMetaRoot, "root")
}

fun getMutablePackagesMeta(mainMeta: MutableMainModel): MutableCollectionFieldModel {
    val mainMetaRoot = mainMeta.root ?: throw IllegalStateException("Root has not been set")
    return getMutableCollectionField(mainMetaRoot, "packages")
}

fun getMutableEnumerationsMeta(packageMeta: MutableEntityModel): MutableCollectionFieldModel? =
    runCatching { getMutableCollectionField(packageMeta, "enumerations") }.getOrNull()

fun getMutableEnumerationValuesMeta(enumerationMeta: MutableEntityModel): MutableCollectionFieldModel =
    getMutableCollectionField(enumerationMeta, "values")

fun getMutableEntitiesMeta(packageMeta: MutableEntityModel): MutableCollectionFieldModel? =
    runCatching { getMutableCollectionField(packageMeta, "entities") }.getOrNull()

fun getMutableFieldsMeta(entityMeta: MutableEntityModel): MutableCollectionFieldModel =
    getMutableCollectionField(entityMeta, "fields")
