package org.treeWare.metaModel.validation

import org.treeWare.metaModel.*
import org.treeWare.metaModel.aux.*
import org.treeWare.metaModel.traversal.AbstractLeader1MetaModelVisitor
import org.treeWare.metaModel.traversal.metaModelForEach
import org.treeWare.model.core.*
import org.treeWare.model.traversal.TraversalAction

/**
 * Validates exists_if clauses.
 * Requires the meta-model to be resolved.
 * Returns a list of errors. Returns an empty list if there are no errors.
 *
 * Side effects: none
 */
fun validateExistsIf(mainMeta: MainModel): List<String> {
    val visitor = ValidateExistsIfVisitor()
    metaModelForEach(mainMeta, visitor)
    return visitor.errors
}

private class ValidateExistsIfVisitor : AbstractLeader1MetaModelVisitor<TraversalAction>(TraversalAction.CONTINUE) {
    val errors = mutableListOf<String>()

    override fun visitEntityMeta(leaderEntityMeta1: EntityModel): TraversalAction {
        val aux = FieldExistenceAux()
        setFieldExistenceAux(leaderEntityMeta1, aux)
        return TraversalAction.CONTINUE
    }

    override fun visitFieldMeta(leaderFieldMeta1: EntityModel): TraversalAction {
        val fieldName = getMetaName(leaderFieldMeta1)
        val isRequired = isRequiredFieldMeta(leaderFieldMeta1)

        val existsIfMeta = getExistsIfMeta(leaderFieldMeta1)
        val existsIf = existsIfMeta?.let {
            val fieldFullName = getMetaModelResolved(leaderFieldMeta1)?.fullName ?: ""
            validateExistsIfClause(leaderFieldMeta1, fieldFullName, it, errors)
        }

        if (isRequired || existsIf != null) {
            val entityMeta = getParentEntityMeta(leaderFieldMeta1)
                ?: throw IllegalStateException("Field meta does not have parent entity meta")
            val aux = getFieldExistenceAux(entityMeta)
                ?: throw IllegalStateException("Entity meta does not have field_existence aux")
            aux.fields.add(FieldExistence(fieldName, isRequired, existsIf))
        }

        return TraversalAction.CONTINUE
    }
}

private fun validateExistsIfClause(
    fieldMeta: EntityModel,
    fieldFullName: String,
    clauseMeta: EntityModel,
    clauseErrors: MutableList<String>
): ExistsIfClause? {
    val operator = getOptionalSingleEnumeration(clauseMeta, "operator")
        ?.let { ExistsIfOperator.valueOf(it.uppercase()) }
    if (operator == null) {
        clauseErrors.add("$fieldFullName exists_if operator is missing")
        return null
    }
    return when (operator) {
        ExistsIfOperator.EQUALS -> validateExistsIfEquals(fieldMeta, fieldFullName, clauseMeta, clauseErrors)
        ExistsIfOperator.AND -> validateExistsIfAnd(fieldMeta, fieldFullName, clauseMeta, clauseErrors)
        ExistsIfOperator.OR -> validateExistsIfOr(fieldMeta, fieldFullName, clauseMeta, clauseErrors)
        ExistsIfOperator.NOT -> validateExistsIfNot(fieldMeta, fieldFullName, clauseMeta, clauseErrors)
    }
}

private fun validateExistsIfEquals(
    fieldMeta: EntityModel,
    fieldFullName: String,
    clauseMeta: EntityModel,
    clauseErrors: MutableList<String>
): ExistsIfClause? {
    val errors = mutableListOf<String>()
    val fieldClause = getOptionalSingleString(clauseMeta, "field")
    if (fieldClause == null) errors.add("$fieldFullName exists_if EQUALS clause field missing")
    val valueClause = getOptionalSingleString(clauseMeta, "value")
    if (valueClause == null) errors.add("$fieldFullName exists_if EQUALS clause value missing")
    errors.addAll(validateExistsIfFieldValue(fieldMeta, fieldFullName, fieldClause, valueClause))
    val arg1Clause = getOptionalSingleEntity(clauseMeta, "arg1")
    if (arg1Clause != null) errors.add("$fieldFullName exists_if EQUALS clause arg1 must not be specified")
    val arg2Clause = getOptionalSingleEntity(clauseMeta, "arg2")
    if (arg2Clause != null) errors.add("$fieldFullName exists_if EQUALS clause arg2 must not be specified")
    clauseErrors.addAll(errors)
    return if (errors.isNotEmpty()) null
    else ExistsIfClause.Equals(requireNotNull(fieldClause), requireNotNull(valueClause))
}

private fun validateExistsIfFieldValue(
    fieldMeta: EntityModel,
    fieldFullName: String,
    fieldClause: String?,
    valueClause: String?
): List<String> {
    // Validate fieldClause.
    if (fieldClause == null || valueClause == null) return emptyList()
    val clauseId = "$fieldFullName exists_if EQUALS clause field `$fieldClause`"
    val selfName = getMetaName(fieldMeta)
    if (fieldClause == selfName) return listOf("$clauseId refers to self")
    val entityMeta = getParentEntityMeta(fieldMeta)
        ?: throw IllegalStateException("$fieldFullName does not have parent entity meta")
    val referencedFieldMeta = runCatching { getFieldMeta(entityMeta as EntityModel, fieldClause) }.getOrNull()
        ?: return listOf("$clauseId is not found")
    val errors = mutableListOf<String>()
    val referencedFieldType = getFieldTypeMeta(referencedFieldMeta)
    val isSupportedFieldType = when (referencedFieldType) {
        null,
        FieldType.PASSWORD1WAY,
        FieldType.PASSWORD2WAY,
        FieldType.ASSOCIATION,
        FieldType.COMPOSITION -> false
        else -> true
    }
    if (!isSupportedFieldType) errors.add("$clauseId is not a supported field type")
    val isSingleField = when (getMultiplicityMeta(referencedFieldMeta)) {
        Multiplicity.REQUIRED,
        Multiplicity.OPTIONAL -> true
        else -> false
    }
    if (!isSingleField) errors.add("$clauseId is not a single field")
    if (errors.isNotEmpty()) return errors

    // Validate valueClause; the value must be assignable to the referenced field.
    val assignable = when (referencedFieldType) {
        FieldType.ENUMERATION -> setEnumerationValue(referencedFieldMeta, valueClause) { _, _, _ -> }
        else -> setValue(referencedFieldMeta, valueClause) {}
    }
    return if (!assignable) listOf("$fieldFullName exists_if EQUALS clause value `$valueClause` is not assignable to field `$fieldClause`")
    else emptyList()
}

private fun validateExistsIfAnd(
    fieldMeta: EntityModel,
    fieldFullName: String,
    clauseMeta: EntityModel,
    clauseErrors: MutableList<String>
): ExistsIfClause? {
    val errors = mutableListOf<String>()
    val fieldClause = getOptionalSingleString(clauseMeta, "field")
    if (fieldClause != null) errors.add("$fieldFullName exists_if AND clause field must not be specified")
    val valueClause = getOptionalSingleString(clauseMeta, "value")
    if (valueClause != null) errors.add("$fieldFullName exists_if AND clause value must not be specified")
    val arg1Clause = getOptionalSingleEntity(clauseMeta, "arg1")
    val arg1 = if (arg1Clause == null) {
        errors.add("$fieldFullName exists_if AND clause arg1 missing")
        null
    } else validateExistsIfClause(fieldMeta, fieldFullName, arg1Clause, errors)
    val arg2Clause = getOptionalSingleEntity(clauseMeta, "arg2")
    val arg2 = if (arg2Clause == null) {
        errors.add("$fieldFullName exists_if AND clause arg2 missing")
        null
    } else validateExistsIfClause(fieldMeta, fieldFullName, arg2Clause, errors)
    clauseErrors.addAll(errors)
    return if (errors.isNotEmpty()) null
    else ExistsIfClause.And(requireNotNull(arg1), requireNotNull(arg2))
}

private fun validateExistsIfOr(
    fieldMeta: EntityModel,
    fieldFullName: String,
    clauseMeta: EntityModel,
    clauseErrors: MutableList<String>
): ExistsIfClause? {
    val errors = mutableListOf<String>()
    val fieldClause = getOptionalSingleString(clauseMeta, "field")
    if (fieldClause != null) errors.add("$fieldFullName exists_if OR clause field must not be specified")
    val valueClause = getOptionalSingleString(clauseMeta, "value")
    if (valueClause != null) errors.add("$fieldFullName exists_if OR clause value must not be specified")
    val arg1Clause = getOptionalSingleEntity(clauseMeta, "arg1")
    val arg1 = if (arg1Clause == null) {
        errors.add("$fieldFullName exists_if OR clause arg1 missing")
        null
    } else validateExistsIfClause(fieldMeta, fieldFullName, arg1Clause, errors)
    val arg2Clause = getOptionalSingleEntity(clauseMeta, "arg2")
    val arg2 = if (arg2Clause == null) {
        errors.add("$fieldFullName exists_if OR clause arg2 missing")
        null
    } else validateExistsIfClause(fieldMeta, fieldFullName, arg2Clause, errors)
    clauseErrors.addAll(errors)
    return if (errors.isNotEmpty()) null
    else ExistsIfClause.Or(requireNotNull(arg1), requireNotNull(arg2))
}

private fun validateExistsIfNot(
    fieldMeta: EntityModel,
    fieldFullName: String,
    clauseMeta: EntityModel,
    clauseErrors: MutableList<String>
): ExistsIfClause? {
    val errors = mutableListOf<String>()
    val fieldClause = getOptionalSingleString(clauseMeta, "field")
    if (fieldClause != null) errors.add("$fieldFullName exists_if NOT clause field must not be specified")
    val valueClause = getOptionalSingleString(clauseMeta, "value")
    if (valueClause != null) errors.add("$fieldFullName exists_if NOT clause value must not be specified")
    val arg1Clause = getOptionalSingleEntity(clauseMeta, "arg1")
    val arg1 = if (arg1Clause == null) {
        errors.add("$fieldFullName exists_if NOT clause arg1 missing")
        null
    } else validateExistsIfClause(fieldMeta, fieldFullName, arg1Clause, errors)
    val arg2Clause = getOptionalSingleEntity(clauseMeta, "arg2")
    if (arg2Clause != null) errors.add("$fieldFullName exists_if NOT clause arg2 must not be specified")
    clauseErrors.addAll(errors)
    return if (errors.isNotEmpty()) null
    else ExistsIfClause.Not(requireNotNull(arg1))
}