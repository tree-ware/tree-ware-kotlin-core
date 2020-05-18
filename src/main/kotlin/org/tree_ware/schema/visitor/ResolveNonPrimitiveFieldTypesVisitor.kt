package org.tree_ware.schema.visitor

import org.tree_ware.schema.core.*

/**
 * Resolves all non-primitive field types except associations.
 * Associations can be resolved only after compositions are resolved.
 */
class ResolveNonPrimitiveFieldTypesVisitor(
    private val aliases: Map<String, MutableAliasSchema>,
    private val enumerations: Map<String, MutableEnumerationSchema>,
    private val entities: Map<String, MutableEntitySchema>
) : AbstractMutableSchemaValidatingVisitor() {
    override fun mutableVisit(root: MutableRootSchema): SchemaTraversalAction {
        // Set resolvedEntity
        val targetFullName = "/${root.packageName}/${root.entityName}"
        val entity = entities[targetFullName]
        if (entity == null) _errors.add("Unknown root type: ${root.fullName}")
        else {
            root.resolvedEntity = entity
        }
        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableVisit(aliasField: MutableAliasFieldSchema): SchemaTraversalAction {
        val aliasFullName = "/${aliasField.packageName}/${aliasField.aliasName}"
        val alias = aliases[aliasFullName]
        if (alias == null) _errors.add("Unknown field type: ${aliasField.fullName}")
        else aliasField.resolvedAlias = alias
        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableVisit(enumerationField: MutableEnumerationFieldSchema): SchemaTraversalAction {
        val enumerationFullName = "/${enumerationField.packageName}/${enumerationField.enumerationName}"
        val enumeration = enumerations[enumerationFullName]
        if (enumeration == null) _errors.add("Unknown field type: ${enumerationField.fullName}")
        else enumerationField.resolvedEnumeration = enumeration
        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableVisit(compositionField: MutableCompositionFieldSchema): SchemaTraversalAction {
        // Set resolvedEntity
        val targetFullName = "/${compositionField.packageName}/${compositionField.entityName}"
        val entity = entities[targetFullName]
        if (entity == null) _errors.add("Unknown field type: ${compositionField.fullName}")
        else {
            compositionField.resolvedEntity = entity
            if (compositionField.isKey && !hasOnlyPrimitiveKeys(entity)) _errors.add(
                "Target of composition key does not have only primitive keys: ${compositionField.fullName}"
            )
            if (compositionField.multiplicity.isList() && !hasKeys(entity)) _errors.add(
                "Target of composition list does not have keys: ${compositionField.fullName}"
            )
        }
        return SchemaTraversalAction.CONTINUE
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
