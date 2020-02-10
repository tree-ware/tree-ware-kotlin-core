package org.tree_ware.core.schema

// TODO(deepak-nulu): replace println() with logger

class SetFullNameVisitor() : AbstractMutableSchemaVisitor(), BracketedVisitor {
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

    override fun mutableVisit(pkg: MutablePackageSchema): Boolean {
        if (nameParts.size != 0) throw IllegalStateException("Invalid nameParts $nameParts for package ${pkg.name} ")
        nameParts.add(pkg.name)
        pkg.fullName = pkg.name
        println("fullName: ${pkg.fullName}")
        return true
    }

    override fun mutableVisit(alias: MutableAliasSchema): Boolean {
        if (nameParts.size != 1) throw IllegalStateException("Invalid nameParts $nameParts for alias ${alias.name} ")
        nameParts.add(alias.name)
        alias.fullName = getFullName()
        println("fullName: ${alias.fullName}")
        return true
    }

    override fun mutableVisit(enumeration: MutableEnumerationSchema): Boolean {
        if (nameParts.size != 1) throw IllegalStateException("Invalid nameParts $nameParts for enumeration ${enumeration.name} ")
        nameParts.add(enumeration.name)
        enumeration.fullName = getFullName()
        println("fullName: ${enumeration.fullName}")
        return true
    }

    override fun mutableVisit(entity: MutableEntitySchema): Boolean {
        if (nameParts.size != 1) throw IllegalStateException("Invalid nameParts $nameParts for entity ${entity.name} ")
        nameParts.add(entity.name)
        entity.fullName = getFullName()
        println("fullName: ${entity.fullName}")
        return true
    }

    override fun mutableVisit(field: MutableFieldSchema): Boolean {
        if (nameParts.size != 2) throw IllegalStateException("Invalid nameParts $nameParts for field ${field.name} ")
        nameParts.add(field.name)
        field.fullName = getFullName()
        println("fullName: ${field.fullName}")
        return true
    }
}
