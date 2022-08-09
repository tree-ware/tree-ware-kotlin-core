package org.treeWare.metaModel.aux

import org.treeWare.metaModel.ExistsIfOperator
import org.treeWare.model.core.ElementModel
import org.treeWare.model.core.getAux

const val FIELD_EXISTENCE_AUX_NAME = "field_existence"

data class FieldExistenceAux(val fields: MutableList<FieldExistence> = mutableListOf())

fun getFieldExistenceAux(elementMeta: ElementModel?): FieldExistenceAux? = elementMeta?.getAux(FIELD_EXISTENCE_AUX_NAME)

fun setFieldExistenceAux(elementMeta: ElementModel, aux: FieldExistenceAux) {
    elementMeta.setAux(FIELD_EXISTENCE_AUX_NAME, aux)
}

data class FieldExistence(val fieldName: String, val isRequired: Boolean, val existsIf: ExistsIfClause? = null)

sealed class ExistsIfClause(val operator: ExistsIfOperator) {
    data class Equals(val fieldName: String, val value: String) : ExistsIfClause(ExistsIfOperator.EQUALS)
    data class And(val arg1: ExistsIfClause, val arg2: ExistsIfClause) : ExistsIfClause(ExistsIfOperator.AND)
    data class Or(val arg1: ExistsIfClause, val arg2: ExistsIfClause) : ExistsIfClause(ExistsIfOperator.OR)
    data class Not(val arg1: ExistsIfClause) : ExistsIfClause(ExistsIfOperator.NOT)
}