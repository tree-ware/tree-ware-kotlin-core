package org.treeWare.model.operator.set

import org.treeWare.metaModel.aux.ExistsIfClause
import org.treeWare.model.core.*
import org.treeWare.model.operator.ElementModelError

sealed interface ExistsIfClauseResult {
    data class Value(val value: Boolean) : ExistsIfClauseResult
    data class Errors(val errors: List<ElementModelError>) : ExistsIfClauseResult
}

fun evaluateExistsIfClause(
    existsIf: ExistsIfClause,
    entity: EntityModel,
    entityPath: String,
): ExistsIfClauseResult = when (existsIf) {
    is ExistsIfClause.Equals -> evaluateEqualsClause(existsIf, entity, entityPath)
    is ExistsIfClause.And -> evaluateAndClause(existsIf, entity, entityPath)
    is ExistsIfClause.Or -> evaluateOrClause(existsIf, entity, entityPath)
    is ExistsIfClause.Not -> evaluateNotClause(existsIf, entity, entityPath)
}

private fun evaluateEqualsClause(
    equalsClause: ExistsIfClause.Equals,
    entity: EntityModel,
    entityPath: String
): ExistsIfClauseResult {
    val fieldName = equalsClause.fieldName
    val field = entity.getField(fieldName) ?: return ExistsIfClauseResult.Errors(
        listOf(ElementModelError(entityPath, "field $fieldName in exists_if not found"))
    )
    val valueModel = (field as SingleFieldModel).value
    return when (val valueType = valueModel?.elementType) {
        ModelElementType.PRIMITIVE -> ExistsIfClauseResult.Value((valueModel as PrimitiveModel).value.toString() == equalsClause.value)
        ModelElementType.ENUMERATION -> ExistsIfClauseResult.Value((valueModel as EnumerationModel).value == equalsClause.value)
        else -> ExistsIfClauseResult.Errors(
            listOf(
                ElementModelError(entityPath, "field $fieldName in exists_if has unsupported value type $valueType")
            )
        )
    }
}

// NOTE: this operator support short-circuiting, so the second result is computed only if needed.
private fun evaluateAndClause(
    andClause: ExistsIfClause.And,
    entity: EntityModel,
    entityPath: String
): ExistsIfClauseResult = when (val arg1Result = evaluateExistsIfClause(andClause.arg1, entity, entityPath)) {
    is ExistsIfClauseResult.Value ->
        if (!arg1Result.value) arg1Result
        else evaluateExistsIfClause(andClause.arg2, entity, entityPath)
    is ExistsIfClauseResult.Errors -> arg1Result
}

// NOTE: this operator support short-circuiting, so the second result is computed only if needed.
private fun evaluateOrClause(
    orClause: ExistsIfClause.Or,
    entity: EntityModel,
    entityPath: String
): ExistsIfClauseResult = when (val arg1Result = evaluateExistsIfClause(orClause.arg1, entity, entityPath)) {
    is ExistsIfClauseResult.Value ->
        if (arg1Result.value) arg1Result
        else evaluateExistsIfClause(orClause.arg2, entity, entityPath)
    is ExistsIfClauseResult.Errors -> arg1Result
}

private fun evaluateNotClause(
    notClause: ExistsIfClause.Not,
    entity: EntityModel,
    entityPath: String
): ExistsIfClauseResult = when (val arg1Result = evaluateExistsIfClause(notClause.arg1, entity, entityPath)) {
    is ExistsIfClauseResult.Value -> ExistsIfClauseResult.Value(!arg1Result.value)
    is ExistsIfClauseResult.Errors -> arg1Result
}