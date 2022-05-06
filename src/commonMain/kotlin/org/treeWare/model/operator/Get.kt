package org.treeWare.model.operator

import org.treeWare.model.core.*
import org.treeWare.model.operator.get.GetDelegate
import org.treeWare.model.operator.get.GetDelegateVisitor
import org.treeWare.model.traversal.forEach
import org.treeWare.util.assertInDevMode

interface GetEntityDelegate {
    fun isSingleValue(): Boolean
}

object GetOperatorId : OperatorId<GetEntityDelegate>

sealed class GetResponse {
    data class Model(val model: MainModel) : GetResponse()
    data class ErrorList(val errorList: List<String>) : GetResponse()

    /** A model with "error_" aux. */
    data class ErrorModel(val errorModel: MainModel) : GetResponse()
}

fun get(
    request: MainModel,
    getDelegate: GetDelegate,
    entityDelegates: EntityDelegateRegistry<GetEntityDelegate>?
): GetResponse {
    val response = MutableMainModel(request.mainMeta)
    response.getOrNewRoot()
    val getVisitor = GetDelegateVisitor(getDelegate, entityDelegates)
    forEach(response, request, getVisitor, false, ::followerEntityEquals)
    return if (getVisitor.errors.isEmpty()) GetResponse.Model(response)
    else GetResponse.ErrorList(getVisitor.errors.map { it.toString() })
}

private fun followerEntityEquals(
    leaderResponseEntity: BaseEntityModel,
    followerRequestEntity: BaseEntityModel
): Boolean {
    assertInDevMode(getMetaModelFullName(leaderResponseEntity) == getMetaModelFullName(followerRequestEntity))
    val leaderResponseKeyFields = getKeyFields(leaderResponseEntity)
    val followerRequestKeyFields = getKeyFields(followerRequestEntity)
    leaderResponseKeyFields.forEachIndexed { index, leaderResponseKeyField ->
        val followerRequestKeyField = followerRequestKeyFields[index]
        val equals = if (followerRequestKeyField.value == null) true
        else leaderResponseKeyField.matches(followerRequestKeyField)
        if (!equals) return false
    }
    return true
}

private fun getKeyFields(entity: BaseEntityModel): List<SingleFieldModel> {
    val (keyFields, missingKeys) = entity.getKeyFields(true)
    if (missingKeys.isNotEmpty()) {
        val name = getMetaModelFullName(entity)
        throw MissingKeysException("Missing key fields $missingKeys in instance of $name")
    }
    return keyFields
}