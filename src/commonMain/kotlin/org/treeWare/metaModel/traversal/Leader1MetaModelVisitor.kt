package org.treeWare.metaModel.traversal

import org.treeWare.model.core.EntityModel

interface Leader1MetaModelVisitor<Return> {
    fun visitMetaModel(leaderMeta1: EntityModel): Return
    fun leaveMetaModel(leaderMeta1: EntityModel)

    fun visitVersionMeta(leaderVersionMeta1: EntityModel): Return
    fun leaveVersionMeta(leaderVersionMeta1: EntityModel)

    fun visitRootMeta(leaderRootMeta1: EntityModel): Return
    fun leaveRootMeta(leaderRootMeta1: EntityModel)

    fun visitPackageMeta(leaderPackageMeta1: EntityModel): Return
    fun leavePackageMeta(leaderPackageMeta1: EntityModel)

    fun visitEnumerationMeta(leaderEnumerationMeta1: EntityModel): Return
    fun leaveEnumerationMeta(leaderEnumerationMeta1: EntityModel)

    fun visitEnumerationValueMeta(leaderEnumerationValueMeta1: EntityModel): Return
    fun leaveEnumerationValueMeta(leaderEnumerationValueMeta1: EntityModel)

    fun visitEntityMeta(leaderEntityMeta1: EntityModel): Return
    fun leaveEntityMeta(leaderEntityMeta1: EntityModel)

    fun visitFieldMeta(leaderFieldMeta1: EntityModel): Return
    fun leaveFieldMeta(leaderFieldMeta1: EntityModel)
}
