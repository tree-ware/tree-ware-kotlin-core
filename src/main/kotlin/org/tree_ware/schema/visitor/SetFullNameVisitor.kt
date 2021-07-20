package org.tree_ware.schema.visitor

import org.tree_ware.common.traversal.TraversalAction
import org.tree_ware.schema.core.*

private val PACKAGE_NAME_REGEX = Regex("^[a-z0-9_.]*$")
private val ELEMENT_NAME_REGEX = Regex("^[a-z0-9_]*$")

class SetFullNameVisitor : AbstractMutableSchemaValidatingVisitor() {
    val fullNames: List<String> get() = _fullNames
    private val _fullNames = mutableListOf<String>()

    private val nameParts = mutableListOf("")
    private fun getFullName(): String = nameParts.joinToString("/")

    private fun validateName(element: NamedElementSchema, regex: Regex) {
        val name = element.name
        if (!regex.matches(name)) _errors.add("Invalid name: ${getFullName()}")
    }

    // MutableSchemaVisitor methods

    override fun mutableVisit(namedElement: MutableNamedElementSchema): TraversalAction {
        nameParts.add(namedElement.name)
        val fullName = getFullName()
        namedElement.fullName = fullName
        if (_fullNames.contains(fullName)) _errors.add("Duplicate name: $fullName")
        else _fullNames.add(fullName)
        return TraversalAction.CONTINUE
    }

    override fun mutableLeave(namedElement: MutableNamedElementSchema) {
        nameParts.removeAt(nameParts.lastIndex)
    }

    override fun mutableVisit(pkg: MutablePackageSchema): TraversalAction {
        validateName(pkg, PACKAGE_NAME_REGEX)
        return TraversalAction.CONTINUE
    }

    override fun mutableVisit(root: MutableRootSchema): TraversalAction {
        validateName(root, ELEMENT_NAME_REGEX)
        return TraversalAction.CONTINUE
    }

    override fun mutableVisit(alias: MutableAliasSchema): TraversalAction {
        validateName(alias, ELEMENT_NAME_REGEX)
        return TraversalAction.CONTINUE
    }

    override fun mutableVisit(enumeration: MutableEnumerationSchema): TraversalAction {
        validateName(enumeration, ELEMENT_NAME_REGEX)
        return TraversalAction.CONTINUE
    }

    override fun mutableVisit(entity: MutableEntitySchema): TraversalAction {
        validateName(entity, ELEMENT_NAME_REGEX)
        return TraversalAction.CONTINUE
    }

    override fun mutableVisit(field: MutableFieldSchema): TraversalAction {
        validateName(field, ELEMENT_NAME_REGEX)
        return TraversalAction.CONTINUE
    }
}
