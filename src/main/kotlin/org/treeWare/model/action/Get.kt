package org.treeWare.model.action

import org.treeWare.model.core.Model
import org.treeWare.model.core.MutableModel
import org.treeWare.model.operator.forEach

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
