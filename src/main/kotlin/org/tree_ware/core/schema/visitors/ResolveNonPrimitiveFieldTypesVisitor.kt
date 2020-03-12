package org.tree_ware.core.schema.visitors

import org.tree_ware.core.schema.*

class ResolveNonPrimitiveFieldTypesVisitor(
        private val aliases: Map<String, MutableAliasSchema>,
        private val enumerations: Map<String, MutableEnumerationSchema>,
        private val entities: Map<String, MutableEntitySchema>
) : AbstractMutableSchemaValidatingVisitor() {
    override fun mutableVisit(aliasField: MutableAliasFieldSchema): Boolean {
        val aliasFullName = "${aliasField.packageName}.${aliasField.aliasName}"
        val alias = aliases[aliasFullName]
        if (alias == null) _errors.add("Unknown field type: ${aliasField.fullName}")
        else aliasField.resolvedAlias = alias
        return true
    }

    override fun mutableVisit(enumerationField: MutableEnumerationFieldSchema): Boolean {
        val enumerationFullName = "${enumerationField.packageName}.${enumerationField.enumerationName}"
        val enumeration = enumerations[enumerationFullName]
        if (enumeration == null) _errors.add("Unknown field type: ${enumerationField.fullName}")
        else enumerationField.resolvedEnumeration = enumeration
        return true
    }

    override fun mutableVisit(entityField: MutableEntityFieldSchema): Boolean {
        val entityFullName = "${entityField.packageName}.${entityField.entityName}"
        val entity = entities[entityFullName]
        if (entity == null) _errors.add("Unknown field type: ${entityField.fullName}")
        else entityField.resolvedEntity = entity
        return true
    }
}
