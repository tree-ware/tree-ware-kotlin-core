package org.tree_ware.core.schema

class SetFullNameVisitor() : AbstractMutableSchemaVisitor(), BracketedVisitor {
    val fullNames: List<String> get() = _fullNames
    private val _fullNames = mutableListOf<String>()

    private val nameParts = mutableListOf<String>()
    private fun getFullName(): String = nameParts.joinToString(".")

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
        _fullNames.add(fullName)
        return true
    }
}
