package org.tree_ware.core.schema

private val PACKAGE_NAME_REGEX = Regex("^[a-z0-9_.]*$")
private val ELEMENT_NAME_REGEX = Regex("^[a-z0-9_]*$")

class SetFullNameVisitor() : AbstractMutableSchemaValidatingVisitor(), BracketedVisitor {
    val fullNames: List<String> get() = _fullNames
    private val _fullNames = mutableListOf<String>()

    private val nameParts = mutableListOf<String>()
    private fun getFullName(): String = nameParts.joinToString(".")

    private fun validateName(element: NamedElementSchema, regex: Regex) {
        val name = element.name
        if (!regex.matches(name)) _errors.add("Invalid name: ${getFullName()}")
    }

    // BracketedVisitor methods

    override fun objectStart(name: String) {}

    override fun objectEnd() {
        nameParts.removeAt(nameParts.lastIndex)
    }

    override fun listStart(name: String) {}

    override fun listEnd() {}

    // MutableSchemaVisitor methods

    override fun mutableVisit(namedElement: MutableNamedElementSchema): Boolean {
        nameParts.add(namedElement.name)
        val fullName = getFullName()
        namedElement.fullName = fullName
        if (_fullNames.contains(fullName)) _errors.add("Duplicate name: ${fullName}")
        else _fullNames.add(fullName)
        return true
    }

    override fun mutableVisit(pkg: MutablePackageSchema): Boolean {
        validateName(pkg, PACKAGE_NAME_REGEX)
        return true
    }

    override fun mutableVisit(alias: MutableAliasSchema): Boolean {
        validateName(alias, ELEMENT_NAME_REGEX)
        return true
    }

    override fun mutableVisit(enumeration: MutableEnumerationSchema): Boolean {
        validateName(enumeration, ELEMENT_NAME_REGEX)
        return true
    }

    override fun mutableVisit(entity: MutableEntitySchema): Boolean {
        validateName(entity, ELEMENT_NAME_REGEX)
        return true
    }

    override fun mutableVisit(field: MutableFieldSchema): Boolean {
        validateName(field, ELEMENT_NAME_REGEX)
        return true
    }
}
