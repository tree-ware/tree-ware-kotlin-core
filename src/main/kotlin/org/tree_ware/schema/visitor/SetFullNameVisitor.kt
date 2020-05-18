package org.tree_ware.schema.visitor

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

    override fun mutableVisit(namedElement: MutableNamedElementSchema): SchemaTraversalAction {
        nameParts.add(namedElement.name)
        val fullName = getFullName()
        namedElement.fullName = fullName
        if (_fullNames.contains(fullName)) _errors.add("Duplicate name: $fullName")
        else _fullNames.add(fullName)
        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableLeave(namedElement: MutableNamedElementSchema) {
        nameParts.removeAt(nameParts.lastIndex)
    }

    override fun mutableVisit(pkg: MutablePackageSchema): SchemaTraversalAction {
        validateName(pkg, PACKAGE_NAME_REGEX)
        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableVisit(root: MutableRootSchema): SchemaTraversalAction {
        validateName(root, ELEMENT_NAME_REGEX)
        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableVisit(alias: MutableAliasSchema): SchemaTraversalAction {
        validateName(alias, ELEMENT_NAME_REGEX)
        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableVisit(enumeration: MutableEnumerationSchema): SchemaTraversalAction {
        validateName(enumeration, ELEMENT_NAME_REGEX)
        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableVisit(entity: MutableEntitySchema): SchemaTraversalAction {
        validateName(entity, ELEMENT_NAME_REGEX)
        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableVisit(field: MutableFieldSchema): SchemaTraversalAction {
        validateName(field, ELEMENT_NAME_REGEX)
        return SchemaTraversalAction.CONTINUE
    }
}
