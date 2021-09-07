package org.treeWare.metaModel

import org.treeWare.model.core.*

fun getMutableRootMeta(mainMeta: MutableMainModel<Resolved>): MutableEntityModel<Resolved> =
    getMutableSingleEntity(mainMeta.root, "root")

fun getMutablePackagesMeta(mainMeta: MutableMainModel<Resolved>): MutableListFieldModel<Resolved> =
    getMutableListField(mainMeta.root, "packages")

fun getMutableEnumerationsMeta(packageMeta: MutableEntityModel<Resolved>): MutableListFieldModel<Resolved>? =
    runCatching { getMutableListField(packageMeta, "enumerations") }.getOrNull()

fun getMutableEnumerationValuesMeta(enumerationMeta: MutableEntityModel<Resolved>): MutableListFieldModel<Resolved> =
    getMutableListField(enumerationMeta, "values")

fun getMutableEntitiesMeta(packageMeta: MutableEntityModel<Resolved>): MutableListFieldModel<Resolved>? =
    runCatching { getMutableListField(packageMeta, "entities") }.getOrNull()

fun getMutableFieldsMeta(entityMeta: MutableEntityModel<Resolved>): MutableListFieldModel<Resolved> =
    getMutableListField(entityMeta, "fields")
