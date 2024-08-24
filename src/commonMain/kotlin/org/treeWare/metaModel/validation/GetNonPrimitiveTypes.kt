package org.treeWare.metaModel.validation

import org.treeWare.metaModel.getEntitiesMeta
import org.treeWare.metaModel.getEnumerationsMeta
import org.treeWare.metaModel.getPackagesMeta
import org.treeWare.model.core.ElementModel
import org.treeWare.model.core.EntityModel
import org.treeWare.model.core.getMetaModelResolved

private class NonPrimitiveState {
    val enumerations = mutableMapOf<String, EntityModel>()
    val entities = mutableMapOf<String, EntityModel>()
}

fun getNonPrimitiveTypes(meta: EntityModel): NonPrimitiveTypes {
    val state = NonPrimitiveState()
    getFromPackages(meta, state)
    return NonPrimitiveTypes(state.enumerations, state.entities)
}

private fun getFromPackages(meta: EntityModel, state: NonPrimitiveState) {
    val packagesMeta = getPackagesMeta(meta)
    packagesMeta.values.forEach { getFromPackage(it, state) }
}

private fun getFromPackage(packageElementMeta: ElementModel, state: NonPrimitiveState) {
    val packageMeta = packageElementMeta as EntityModel
    getFromEnumerations(packageMeta, state)
    getFromEntities(packageMeta, state)
}

private fun getFromEnumerations(packageMeta: EntityModel, state: NonPrimitiveState) {
    val enumerationsMeta = getEnumerationsMeta(packageMeta)
    enumerationsMeta?.values?.forEach { getFromEnumeration(it, state) }
}

private fun getFromEnumeration(enumerationElementMeta: ElementModel, state: NonPrimitiveState) {
    val enumerationMeta = enumerationElementMeta as EntityModel
    val resolved = getMetaModelResolved(enumerationMeta)
        ?: throw IllegalStateException("Resolved aux is missing in enumeration")
    state.enumerations[resolved.fullName] = enumerationMeta
}

private fun getFromEntities(packageMeta: EntityModel, state: NonPrimitiveState) {
    val entitiesMeta = getEntitiesMeta(packageMeta)
    entitiesMeta?.values?.forEach { getFromEntity(it, state) }
}

private fun getFromEntity(entityElementMeta: ElementModel, state: NonPrimitiveState) {
    val entityMeta = entityElementMeta as EntityModel
    val resolved = getMetaModelResolved(entityMeta) ?: throw IllegalStateException("Resolved aux is missing in entity")
    state.entities[resolved.fullName] = entityMeta
}