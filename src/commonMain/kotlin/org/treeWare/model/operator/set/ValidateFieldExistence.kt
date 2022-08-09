package org.treeWare.model.operator.set

import org.treeWare.metaModel.aux.FieldExistence
import org.treeWare.metaModel.aux.getFieldExistenceAux
import org.treeWare.metaModel.getFieldMeta
import org.treeWare.metaModel.isRequiredFieldMeta
import org.treeWare.model.core.EntityModel
import org.treeWare.model.operator.ElementModelError

fun validateFieldExistence(
    entity: EntityModel,
    entityPath: String,
    validateRequired: Boolean,
    validatePresence: Boolean,
    validateAbsence: Boolean
): List<ElementModelError> {
    val entityMeta = entity.meta ?: throw IllegalStateException("Entity meta is missing")
    val fieldExistenceAux = getFieldExistenceAux(entityMeta) ?: return emptyList()
    val errors = fieldExistenceAux.fields.flatMap {
        validateFieldExistence(it, entity, entityPath, validateRequired, validatePresence, validateAbsence)
    }
    return errors.toSet().toList()
}

private fun validateFieldExistence(
    fieldExistence: FieldExistence,
    entity: EntityModel,
    entityPath: String,
    validateRequired: Boolean,
    validatePresence: Boolean,
    validateAbsence: Boolean
): List<ElementModelError> =
    if (fieldExistence.existsIf == null) {
        if (validateRequired) validateRequiredField(fieldExistence, entity, entityPath)
        else emptyList()
    } else validateExistsIfClause(fieldExistence, entity, entityPath, validatePresence, validateAbsence)

private fun validateRequiredField(
    fieldExistence: FieldExistence,
    entity: EntityModel,
    entityPath: String
): List<ElementModelError> =
    if (fieldExistence.isRequired) {
        val fieldName = fieldExistence.fieldName
        val field = entity.getField(fieldName)
        if (field != null) emptyList()
        else listOf(ElementModelError(entityPath, "required field not found: $fieldName"))
    } else emptyList()

private fun validateExistsIfClause(
    fieldExistence: FieldExistence,
    entity: EntityModel,
    entityPath: String,
    validatePresence: Boolean,
    validateAbsence: Boolean
): List<ElementModelError> {
    if (fieldExistence.existsIf == null) return emptyList()
    if (!validatePresence && !validateAbsence) return emptyList()
    val fieldName = fieldExistence.fieldName
    val field = entity.getField(fieldName)
    val fieldExists = field != null
    // Field-meta must be obtained from entity-meta and not from the field because the field could be missing (null).
    val entityMeta = entity.meta ?: throw IllegalStateException("Entity meta is missing")
    val fieldMeta = getFieldMeta(entityMeta, fieldName)
    val isRequiredField = isRequiredFieldMeta(fieldMeta)
    return when (val conditionsMet = evaluateExistsIfClause(fieldExistence.existsIf, entity, entityPath)) {
        is ExistsIfClauseResult.Value -> when (conditionsMet.value) {
            true -> validateFieldPresence(fieldName, entityPath, validatePresence, fieldExists, isRequiredField)
            false -> validateFieldAbsence(fieldName, entityPath, validateAbsence, fieldExists)
        }
        is ExistsIfClauseResult.Errors -> conditionsMet.errors
    }
}

fun validateFieldPresence(
    fieldName: String,
    entityPath: String,
    validatePresence: Boolean,
    fieldExists: Boolean,
    isRequiredField: Boolean
): List<ElementModelError> =
    if (validatePresence && !fieldExists && isRequiredField) listOf(
        ElementModelError(
            entityPath,
            "conditions are met for required conditional-field $fieldName, but field is not found"
        )
    )
    else emptyList()

fun validateFieldAbsence(
    fieldName: String,
    entityPath: String,
    validateAbsence: Boolean,
    fieldExists: Boolean
): List<ElementModelError> =
    if (validateAbsence && fieldExists) listOf(
        ElementModelError(
            entityPath,
            "conditions are not met for conditional-field $fieldName, but field is found"
        )
    )
    else emptyList()