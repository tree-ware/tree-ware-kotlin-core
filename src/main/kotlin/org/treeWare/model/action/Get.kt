package org.treeWare.model.action

import org.treeWare.model.core.MainModel
import org.treeWare.model.core.MutableMainModel
import org.treeWare.model.operator.forEach

// IMPLEMENTATION: ./Get.md

suspend fun <MappingAux> get(
    request: MainModel<Unit>,
    mapping: MainModel<MappingAux>,
    visitor: GetVisitor<MappingAux>
): MainModel<Unit> {
    val response = MutableMainModel<Unit>(request.meta)
    response.getOrNewRoot() // create an empty root
    forEach(leader = response, follower1 = request, follower2 = mapping, visitor = visitor)
    return response
}
