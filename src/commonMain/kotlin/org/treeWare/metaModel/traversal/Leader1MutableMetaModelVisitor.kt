package org.treeWare.metaModel.traversal

import org.treeWare.model.core.MutableEntityModel

interface Leader1MutableMetaModelVisitor<Return> {
    fun visitMetaModel(leaderMeta1: MutableEntityModel): Return
    fun leaveMetaModel(leaderMeta1: MutableEntityModel)

    fun visitVersionMeta(leaderVersionMeta1: MutableEntityModel): Return
    fun leaveVersionMeta(leaderVersionMeta1: MutableEntityModel)

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
