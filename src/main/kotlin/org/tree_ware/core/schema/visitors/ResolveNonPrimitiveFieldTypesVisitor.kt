package org.tree_ware.core.schema.visitors

import org.tree_ware.core.schema.*

class ResolveNonPrimitiveFieldTypesVisitor(
    private val aliases: Map<String, MutableAliasSchema>,
    private val enumerations: Map<String, MutableEnumerationSchema>,
    private val entities: Map<String, MutableEntitySchema>
) : AbstractMutableSchemaValidatingVisitor() {
    override fun mutableVisit(aliasField: MutableAliasFieldSchema): Boolean {
        val aliasFullName = "/${aliasField.packageName}/${aliasField.aliasName}"
        val alias = aliases[aliasFullName]
        if (alias == null) _errors.add("Unknown field type: ${aliasField.fullName}")
        else aliasField.resolvedAlias = alias
        return true
    }

    override fun mutableVisit(enumerationField: MutableEnumerationFieldSchema): Boolean {
        val enumerationFullName = "/${enumerationField.packageName}/${enumerationField.enumerationName}"
        val enumeration = enumerations[enumerationFullName]
        if (enumeration == null) _errors.add("Unknown field type: ${enumerationField.fullName}")
        else enumerationField.resolvedEnumeration = enumeration
        return true
    }

    override fun mutableVisit(associationField: MutableAssociationFieldSchema): Boolean {
        // TODO(deepak-nulu): implement resolution of associations
        return true
    }

    override fun mutableVisit(compositionField: MutableCompositionFieldSchema): Boolean {
        val entityFullName = "/${compositionField.packageName}/${compositionField.entityName}"
        val entity = entities[entityFullName]
        if (entity == null) _errors.add("Unknown field type: ${compositionField.fullName}")
        else {
            compositionField.resolvedEntity = entity
            if (compositionField.isKey && !hasOnlyPrimitiveKeys(entity)) _errors.add(
                "Target of composition key does not have only primitive keys: ${compositionField.fullName}"
            )
        }
        return true
    }
}

fun hasOnlyPrimitiveKeys(entity: EntitySchema): Boolean {
    val keys = entity.fields.filter { it.isKey }
    val primitiveKeys = keys.filterNot { it is CompositionFieldSchema }
    return keys.isNotEmpty() && (keys.size == primitiveKeys.size)
}
