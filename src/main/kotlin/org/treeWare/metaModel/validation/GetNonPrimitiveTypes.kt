package org.treeWare.metaModel.validation

import org.treeWare.metaModel.getEntitiesMeta
import org.treeWare.metaModel.getEnumerationsMeta
import org.treeWare.metaModel.getPackagesMeta
import org.treeWare.model.core.ElementModel
import org.treeWare.model.core.EntityModel
import org.treeWare.model.core.Model
import org.treeWare.model.core.Resolved

private class NonPrimitiveState {
    val enumerations = mutableMapOf<String, EntityModel<Resolved>>()
    val entities = mutableMapOf<String, EntityModel<Resolved>>()
}

fun getNonPrimitiveTypes(mainMeta: Model<Resolved>): NonPrimitiveTypes {
    val state = NonPrimitiveState()
    getFromPackages(mainMeta, state)
    return NonPrimitiveTypes(state.enumerations, state.entities)
}

private fun getFromPackages(mainMeta: Model<Resolved>, state: NonPrimitiveState) {
    val packagesMeta = getPackagesMeta(mainMeta)
    packagesMeta.values.forEach { getFromPackage(it, state) }
}

private fun getFromPackage(packageElementMeta: ElementModel<Resolved>, state: NonPrimitiveState) {
    val packageMeta = packageElementMeta as EntityModel<Resolved>
    getFromEnumerations(packageMeta, state)
    getFromEntities(packageMeta, state)
}

private fun getFromEnumerations(packageMeta: EntityModel<Resolved>, state: NonPrimitiveState) {
    val enumerationsMeta = getEnumerationsMeta(packageMeta)
    enumerationsMeta?.values?.forEach { getFromEnumeration(it, state) }
}

private fun getFromEnumeration(enumerationElementMeta: ElementModel<Resolved>, state: NonPrimitiveState) {
    val enumerationMeta = enumerationElementMeta as EntityModel<Resolved>
    val resolved = enumerationMeta.aux ?: throw IllegalStateException("Resolved aux is missing in enumeration")
    state.enumerations[resolved.fullName] = enumerationMeta
}

private fun getFromEntities(packageMeta: EntityModel<Resolved>, state: NonPrimitiveState) {
    val entitiesMeta = getEntitiesMeta(packageMeta)
    entitiesMeta?.values?.forEach { getFromEntity(it, state) }
}

private fun getFromEntity(entityElementMeta: ElementModel<Resolved>, state: NonPrimitiveState) {
    val entityMeta = entityElementMeta as EntityModel<Resolved>
    val resolved = entityMeta.aux ?: throw IllegalStateException("Resolved aux is missing in entity")
    state.entities[resolved.fullName] = entityMeta
}
