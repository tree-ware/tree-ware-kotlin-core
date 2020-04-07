package org.tree_ware.core.schema.visitors

import org.tree_ware.core.schema.*

/**
 * Resolves all non-primitive field types except associations.
 * Associations can be resolved only after compositions are resolved.
 */
class ResolveNonPrimitiveFieldTypesVisitor(
    private val aliases: Map<String, MutableAliasSchema>,
    private val enumerations: Map<String, MutableEnumerationSchema>,
    private val entities: Map<String, MutableEntitySchema>
) : AbstractMutableSchemaValidatingVisitor() {
    override fun mutableVisit(field: MutableFieldSchema): Boolean {
        // Set parentEntity
        val fieldFullName = field.fullName ?: ""
        val index = fieldFullName.lastIndexOf("/")
        val parentFullName = if (index == -1) "" else fieldFullName.substring(0, index)
        field.parentEntity = entities[parentFullName]

        return true
    }

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

    override fun mutableVisit(compositionField: MutableCompositionFieldSchema): Boolean {
        // Set resolvedEntity
        val targetFullName = "/${compositionField.packageName}/${compositionField.entityName}"
        val entity = entities[targetFullName]
        if (entity == null) _errors.add("Unknown field type: ${compositionField.fullName}")
        else {
            compositionField.resolvedEntity = entity
            if (compositionField.isKey && !hasOnlyPrimitiveKeys(entity)) _errors.add(
                "Target of composition key does not have only primitive keys: ${compositionField.fullName}"
            )
            if (compositionField.multiplicity.max != 1L && !hasKeys(entity)) _errors.add(
                "Target of composition list does not have keys: ${compositionField.fullName}"
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

fun hasKeys(entity: EntitySchema): Boolean {
    val keys = entity.fields.filter { it.isKey }
    return keys.isNotEmpty()
}
