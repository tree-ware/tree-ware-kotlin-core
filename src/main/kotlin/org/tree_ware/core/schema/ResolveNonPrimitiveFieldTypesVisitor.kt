package org.tree_ware.core.schema

class ResolveNonPrimitiveFieldTypesVisitor(
        private val aliases: Map<String, MutableAliasSchema>,
        private val enumerations: Map<String, MutableEnumerationSchema>,
        private val entities: Map<String, MutableEntitySchema>
) : AbstractMutableSchemaVisitor() {
    val errors = mutableListOf<String>()

    override fun mutableVisit(aliasField: MutableAliasFieldSchema): Boolean {
        val aliasFullName = "${aliasField.packageName}.${aliasField.aliasName}"
        val alias = aliases[aliasFullName]
        if (alias == null) errors.add("${aliasField.fullName}: unknown field type")
        else aliasField.resolvedAlias = alias
        return true
    }

    override fun mutableVisit(enumerationField: MutableEnumerationFieldSchema): Boolean {
        val enumerationFullName = "${enumerationField.packageName}.${enumerationField.enumerationName}"
        val enumeration = enumerations[enumerationFullName]
        if (enumeration == null) errors.add("${enumerationField.fullName}: unknown field type")
        else enumerationField.resolvedEnumeration = enumeration
        return true
    }

    override fun mutableVisit(entityField: MutableEntityFieldSchema): Boolean {
        val entityFullName = "${entityField.packageName}.${entityField.entityName}"
        val entity = entities[entityFullName]
        if (entity == null) errors.add("${entityField.fullName}: unknown field type")
        else entityField.resolvedEntity = entity
        return true
    }
}
