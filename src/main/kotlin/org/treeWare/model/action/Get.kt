package org.treeWare.model.action

import org.treeWare.model.core.MainModel
import org.treeWare.model.core.MutableMainModel
import org.treeWare.model.traversal.forEach

// IMPLEMENTATION: ./Get.md

suspend fun get(
    request: MainModel,
    mapping: MainModel,
    visitor: GetVisitor
): MainModel {
    val response = MutableMainModel(request.meta)
    response.getOrNewRoot() // create an empty root
    forEach(leader = response, follower1 = request, follower2 = mapping, visitor = visitor)
    return response
}
