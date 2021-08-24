package org.treeWare.metaModel.validation

import org.apache.logging.log4j.LogManager
import org.treeWare.metaModel.*
import org.treeWare.model.core.ElementModel
import org.treeWare.model.core.EntityModel
import org.treeWare.model.core.Model
import org.treeWare.model.core.Resolved

private val PACKAGE_NAME_REGEX = Regex("^[a-z0-9_.]*$")
private val ELEMENT_NAME_REGEX = Regex("^[a-z0-9_]*$")

/** Validates the names in the meta-model.
 * Returns a list of errors. Returns an empty list if there are no errors.
 *
 * Side effects:
 * 1. full-names are set for named elements
 */
fun validateNames(mainMeta: Model<Resolved>, logFullNames: Boolean): List<String> {
    val state = State()

    validateRootName(mainMeta, state)
    validatePackagesNames(mainMeta, state)

    if (logFullNames) {
        val logger = LogManager.getLogger()
        state.fullNames.forEach { logger.debug("element fullName: $it") }
    }
    return state.errors
}

private class State() {
    val fullNames = mutableListOf<String>()
    val errors = mutableListOf<String>()

    private val nameParts = mutableListOf("")

    fun getFullName(): String = nameParts.joinToString("/")

    fun validateName(elementName: String, regex: Regex) {
        if (!regex.matches(elementName)) errors.add("Invalid name: ${getFullName()}")
    }

    fun pushName(name: String, regex: Regex) {
        nameParts.add(name)
        val fullName = getFullName()
        if (!regex.matches(name)) errors.add("Invalid name: $fullName")
        if (fullNames.contains(fullName)) errors.add("Duplicate name: $fullName")
        else fullNames.add(fullName)
        // TODO(deepak-nulu): set full-names in the Resolved aux.
    }

    fun popName() {
        nameParts.removeAt(nameParts.lastIndex)
    }
}

private fun validateRootName(mainMeta: Model<Resolved>, state: State) {
    val rootMeta = getRootMeta(mainMeta)
    val name = getMetaName(rootMeta)
    state.pushName(name, ELEMENT_NAME_REGEX)
    state.popName()
}

private fun validatePackagesNames(mainMeta: Model<Resolved>, state: State) {
    val packagesMeta = getPackagesMeta(mainMeta)
    packagesMeta.values.forEach { validatePackageName(it, state) }
}

private fun validatePackageName(packageElementMeta: ElementModel<Resolved>, state: State) {
    val packageMeta = packageElementMeta as EntityModel<Resolved>
    val name = getMetaName(packageMeta)
    state.pushName(name, PACKAGE_NAME_REGEX)
    validateEnumerations(packageMeta, state)
    validateEntities(packageMeta, state)
    state.popName()
}

private fun validateEnumerations(packageMeta: EntityModel<Resolved>, state: State) {
    val enumerationsMeta = getEnumerationsMeta(packageMeta)
    enumerationsMeta?.values?.forEach { validateEnumeration(it, state) }
}

private fun validateEnumeration(enumerationElementMeta: ElementModel<Resolved>, state: State) {
    val enumerationMeta = enumerationElementMeta as EntityModel<Resolved>
    val name = getMetaName(enumerationMeta)
    state.pushName(name, ELEMENT_NAME_REGEX)
    validateEnumerationValues(enumerationMeta, state)
    state.popName()
}

private fun validateEnumerationValues(enumerationMeta: EntityModel<Resolved>, state: State) {
    val enumerationValuesMeta = getEnumerationValuesMeta(enumerationMeta)
    enumerationValuesMeta.values.forEach { validateEnumerationValue(it, state) }
}

private fun validateEnumerationValue(enumerationValueElementMeta: ElementModel<Resolved>, state: State) {
    val enumerationValueMeta = enumerationValueElementMeta as EntityModel<Resolved>
    val name = getMetaName(enumerationValueMeta)
    state.pushName(name, ELEMENT_NAME_REGEX)
    state.popName()
}

private fun validateEntities(packageMeta: EntityModel<Resolved>, state: State) {
    val entitiesMeta = getEntitiesMeta(packageMeta)
    entitiesMeta?.values?.forEach { validateEntity(it, state) }
}

private fun validateEntity(entityElementMeta: ElementModel<Resolved>, state: State) {
    val entityMeta = entityElementMeta as EntityModel<Resolved>
    val name = getMetaName(entityMeta)
    state.pushName(name, ELEMENT_NAME_REGEX)
    validateFields(entityMeta, state)
    state.popName()
}

private fun validateFields(entityMeta: EntityModel<Resolved>, state: State) {
    val fieldsMeta = getFieldsMeta(entityMeta)
    fieldsMeta.values.forEach { validateField(it, state) }
}

private fun validateField(fieldElementMeta: ElementModel<Resolved>, state: State) {
    val fieldMeta = fieldElementMeta as EntityModel<Resolved>
    val name = getMetaName(fieldMeta)
    state.pushName(name, ELEMENT_NAME_REGEX)
    state.popName()
}
