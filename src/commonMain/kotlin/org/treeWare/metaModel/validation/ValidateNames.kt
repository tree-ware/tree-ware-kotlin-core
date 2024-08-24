package org.treeWare.metaModel.validation

import org.lighthousegames.logging.logging
import org.treeWare.metaModel.*
import org.treeWare.model.core.*

private val PACKAGE_NAME_REGEX = Regex("^[a-z0-9_.]*$")
private val ELEMENT_NAME_REGEX = Regex("^[a-z0-9_]*$")

/**
 * Validates the names in the meta-model.
 * Returns a list of errors. Returns an empty list if there are no errors.
 *
 * Side effects:
 * 1. full-names are set for named elements
 */
fun validateNames(meta: MutableEntityModel, logFullNames: Boolean): List<String> {
    val state = State()

    meta.setAux(RESOLVED_AUX, Resolved("/"))
    validateMetaModelName(meta, state)
    validatePackagesNames(meta, state)

    if (logFullNames) {
        val logger = logging()
        state.fullNames.forEach { logger.info { "element fullName: $it" } }
    }
    return state.errors
}

private class State {
    val fullNames = mutableListOf<String>()
    val errors = mutableListOf<String>()

    private val nameParts = mutableListOf("")

    fun getFullName(): String = nameParts.joinToString("/")

    fun pushName(meta: MutableBaseEntityModel, regex: Regex) {
        val name = getMetaName(meta)
        nameParts.add(name)
        val fullName = getFullName()
        if (!regex.matches(name)) errors.add("Invalid name: $fullName")
        if (fullNames.contains(fullName)) errors.add("Duplicate name: $fullName")
        else fullNames.add(fullName)
        meta.setAux(RESOLVED_AUX, Resolved(fullName))
    }

    fun popName() {
        nameParts.removeAt(nameParts.lastIndex)
    }
}

private fun validateMetaModelName(meta: MutableEntityModel, state: State) {
    state.pushName(meta, ELEMENT_NAME_REGEX)
    state.popName()
}

private fun validatePackagesNames(meta: MutableEntityModel, state: State) {
    val packagesMeta = getMutablePackagesMeta(meta)
    packagesMeta.values.forEach { validatePackageName(it, state) }
}

private fun validatePackageName(packageElementMeta: MutableElementModel, state: State) {
    val packageMeta = packageElementMeta as MutableEntityModel
    state.pushName(packageMeta, PACKAGE_NAME_REGEX)
    validateEnumerations(packageMeta, state)
    validateEntities(packageMeta, state)
    state.popName()
}

private fun validateEnumerations(packageMeta: MutableEntityModel, state: State) {
    val enumerationsMeta = getMutableEnumerationsMeta(packageMeta)
    enumerationsMeta?.values?.forEach { validateEnumeration(it, state) }
}

private fun validateEnumeration(enumerationElementMeta: MutableElementModel, state: State) {
    val enumerationMeta = enumerationElementMeta as MutableEntityModel
    state.pushName(enumerationMeta, ELEMENT_NAME_REGEX)
    validateEnumerationValues(enumerationMeta, state)
    state.popName()
}

private fun validateEnumerationValues(enumerationMeta: MutableEntityModel, state: State) {
    val enumerationValuesMeta = getMutableEnumerationValuesMeta(enumerationMeta)
    enumerationValuesMeta.values.forEach { validateEnumerationValue(it, state) }
}

private fun validateEnumerationValue(enumerationValueElementMeta: MutableElementModel, state: State) {
    val enumerationValueMeta = enumerationValueElementMeta as MutableEntityModel
    state.pushName(enumerationValueMeta, ELEMENT_NAME_REGEX)
    state.popName()
}

private fun validateEntities(packageMeta: MutableEntityModel, state: State) {
    val entitiesMeta = getMutableEntitiesMeta(packageMeta)
    entitiesMeta?.values?.forEach { validateEntity(it, state) }
}

private fun validateEntity(entityElementMeta: MutableElementModel, state: State) {
    val entityMeta = entityElementMeta as MutableEntityModel
    state.pushName(entityMeta, ELEMENT_NAME_REGEX)
    validateFields(entityMeta, state)
    state.popName()
}

private fun validateFields(entityMeta: MutableEntityModel, state: State) {
    val fieldsMeta = getMutableFieldsMeta(entityMeta)
    fieldsMeta.values.forEach { validateField(it, state) }
}

private fun validateField(fieldElementMeta: MutableElementModel, state: State) {
    val fieldMeta = fieldElementMeta as MutableEntityModel
    state.pushName(fieldMeta, ELEMENT_NAME_REGEX)
    state.popName()
}