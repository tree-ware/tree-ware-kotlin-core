package org.treeWare.metaModel.validation

import org.apache.logging.log4j.LogManager
import org.treeWare.metaModel.*
import org.treeWare.model.core.*

private val PACKAGE_NAME_REGEX = Regex("^[a-z0-9_.]*$")
private val ELEMENT_NAME_REGEX = Regex("^[a-z0-9_]*$")

/** Validates the names in the meta-model.
 * Returns a list of errors. Returns an empty list if there are no errors.
 *
 * Side effects:
 * 1. full-names are set for named elements
 */
fun validateNames(mainMeta: MutableMainModel<Resolved>, logFullNames: Boolean): List<String> {
    val state = State()

    validateRootName(mainMeta, state)
    validatePackagesNames(mainMeta, state)

    if (logFullNames) {
        val logger = LogManager.getLogger()
        state.fullNames.forEach { logger.debug("element fullName: $it") }
    }
    return state.errors
}

private class State {
    val fullNames = mutableListOf<String>()
    val errors = mutableListOf<String>()

    private val nameParts = mutableListOf("")

    fun getFullName(): String = nameParts.joinToString("/")

    fun pushName(meta: MutableBaseEntityModel<Resolved>, regex: Regex) {
        val name = getMetaName(meta)
        nameParts.add(name)
        val fullName = getFullName()
        if (!regex.matches(name)) errors.add("Invalid name: $fullName")
        if (fullNames.contains(fullName)) errors.add("Duplicate name: $fullName")
        else fullNames.add(fullName)
        meta.aux = Resolved(fullName)
    }

    fun popName() {
        nameParts.removeAt(nameParts.lastIndex)
    }
}

private fun validateRootName(mainMeta: MutableMainModel<Resolved>, state: State) {
    val rootMeta = getMutableRootMeta(mainMeta)
    state.pushName(rootMeta, ELEMENT_NAME_REGEX)
    state.popName()
}

private fun validatePackagesNames(mainMeta: MutableMainModel<Resolved>, state: State) {
    val packagesMeta = getMutablePackagesMeta(mainMeta)
    packagesMeta.values.forEach { validatePackageName(it, state) }
}

private fun validatePackageName(packageElementMeta: MutableElementModel<Resolved>, state: State) {
    val packageMeta = packageElementMeta as MutableEntityModel<Resolved>
    state.pushName(packageMeta, PACKAGE_NAME_REGEX)
    validateEnumerations(packageMeta, state)
    validateEntities(packageMeta, state)
    state.popName()
}

private fun validateEnumerations(packageMeta: MutableEntityModel<Resolved>, state: State) {
    val enumerationsMeta = getMutableEnumerationsMeta(packageMeta)
    enumerationsMeta?.values?.forEach { validateEnumeration(it, state) }
}

private fun validateEnumeration(enumerationElementMeta: MutableElementModel<Resolved>, state: State) {
    val enumerationMeta = enumerationElementMeta as MutableEntityModel<Resolved>
    state.pushName(enumerationMeta, ELEMENT_NAME_REGEX)
    validateEnumerationValues(enumerationMeta, state)
    state.popName()
}

private fun validateEnumerationValues(enumerationMeta: MutableEntityModel<Resolved>, state: State) {
    val enumerationValuesMeta = getMutableEnumerationValuesMeta(enumerationMeta)
    enumerationValuesMeta.values.forEach { validateEnumerationValue(it, state) }
}

private fun validateEnumerationValue(enumerationValueElementMeta: MutableElementModel<Resolved>, state: State) {
    val enumerationValueMeta = enumerationValueElementMeta as MutableEntityModel<Resolved>
    state.pushName(enumerationValueMeta, ELEMENT_NAME_REGEX)
    state.popName()
}

private fun validateEntities(packageMeta: MutableEntityModel<Resolved>, state: State) {
    val entitiesMeta = getMutableEntitiesMeta(packageMeta)
    entitiesMeta?.values?.forEach { validateEntity(it, state) }
}

private fun validateEntity(entityElementMeta: MutableElementModel<Resolved>, state: State) {
    val entityMeta = entityElementMeta as MutableEntityModel<Resolved>
    state.pushName(entityMeta, ELEMENT_NAME_REGEX)
    validateFields(entityMeta, state)
    state.popName()
}

private fun validateFields(entityMeta: MutableEntityModel<Resolved>, state: State) {
    val fieldsMeta = getMutableFieldsMeta(entityMeta)
    fieldsMeta.values.forEach { validateField(it, state) }
}

private fun validateField(fieldElementMeta: MutableElementModel<Resolved>, state: State) {
    val fieldMeta = fieldElementMeta as MutableEntityModel<Resolved>
    state.pushName(fieldMeta, ELEMENT_NAME_REGEX)
    state.popName()
}
