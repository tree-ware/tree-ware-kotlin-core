package org.treeWare.metaModel

import org.treeWare.model.core.*

fun getMutableRootMeta(mainMeta: MutableMainModel<Resolved>): MutableEntityModel<Resolved> =
    getMutableSingleEntity(mainMeta.root, "root")

fun getMutablePackagesMeta(mainMeta: MutableMainModel<Resolved>): MutableCollectionFieldModel<Resolved> =
    getMutableCollectionField(mainMeta.root, "packages")

fun getMutableEnumerationsMeta(packageMeta: MutableEntityModel<Resolved>): MutableCollectionFieldModel<Resolved>? =
    runCatching { getMutableCollectionField(packageMeta, "enumerations") }.getOrNull()

fun getMutableEnumerationValuesMeta(enumerationMeta: MutableEntityModel<Resolved>): MutableCollectionFieldModel<Resolved> =
    getMutableCollectionField(enumerationMeta, "values")

fun getMutableEntitiesMeta(packageMeta: MutableEntityModel<Resolved>): MutableCollectionFieldModel<Resolved>? =
    runCatching { getMutableCollectionField(packageMeta, "entities") }.getOrNull()

fun getMutableFieldsMeta(entityMeta: MutableEntityModel<Resolved>): MutableCollectionFieldModel<Resolved> =
    getMutableCollectionField(entityMeta, "fields")
