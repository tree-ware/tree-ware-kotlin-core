package org.tree_ware.model.action

import org.tree_ware.model.core.Model
import org.tree_ware.model.core.MutableModel
import org.tree_ware.model.operator.forEach

// IMPLEMENTATION: ./Get.md

suspend fun <MappingAux> get(
    request: Model<Unit>,
    mapping: Model<MappingAux>,
    visitor: GetVisitor<MappingAux>
): Model<Unit> {
    val response = MutableModel<Unit>(request.schema)
    response.getOrNewRoot() // create an empty root
    forEach(leader = response, follower1 = request, follower2 = mapping, visitor = visitor)
    return response
}
