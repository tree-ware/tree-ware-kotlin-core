package org.treeWare.metaModel.traversal

import org.treeWare.model.core.MutableEntityModel
import org.treeWare.model.core.MutableMainModel

interface Leader1Follower0MutableMetaModelVisitor<Return> {
    fun visitMainMeta(leaderMainMeta1: MutableMainModel): Return
    fun leaveMainMeta(leaderMainMeta1: MutableMainModel)

    fun visitRootMeta(leaderRootMeta1: MutableEntityModel): Return
    fun leaveRootMeta(leaderRootMeta1: MutableEntityModel)

    fun visitPackageMeta(leaderPackageMeta1: MutableEntityModel): Return
    fun leavePackageMeta(leaderPackageMeta1: MutableEntityModel)

    fun visitEnumerationMeta(leaderEnumerationMeta1: MutableEntityModel): Return
    fun leaveEnumerationMeta(leaderEnumerationMeta1: MutableEntityModel)

    fun visitEnumerationValueMeta(leaderEnumerationValueMeta1: MutableEntityModel): Return
    fun leaveEnumerationValueMeta(leaderEnumerationValueMeta1: MutableEntityModel)

    fun visitEntityMeta(leaderEntityMeta1: MutableEntityModel): Return
    fun leaveEntityMeta(leaderEntityMeta1: MutableEntityModel)

    fun visitFieldMeta(leaderFieldMeta1: MutableEntityModel): Return
    fun leaveFieldMeta(leaderFieldMeta1: MutableEntityModel)
}
