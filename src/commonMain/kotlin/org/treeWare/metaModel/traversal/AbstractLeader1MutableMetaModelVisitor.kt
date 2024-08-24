package org.treeWare.metaModel.traversal

import org.treeWare.model.core.MutableEntityModel

abstract class AbstractLeader1MutableMetaModelVisitor<Return>(
    private val defaultVisitReturn: Return
) : Leader1MutableMetaModelVisitor<Return> {
    override fun visitVersionMeta(leaderVersionMeta1: MutableEntityModel): Return = defaultVisitReturn
    override fun leaveVersionMeta(leaderVersionMeta1: MutableEntityModel) {}

    override fun visitRootMeta(leaderRootMeta1: MutableEntityModel): Return = defaultVisitReturn
    override fun leaveRootMeta(leaderRootMeta1: MutableEntityModel) {}

    override fun visitPackageMeta(leaderPackageMeta1: MutableEntityModel): Return = defaultVisitReturn
    override fun leavePackageMeta(leaderPackageMeta1: MutableEntityModel) {}

    override fun visitEnumerationMeta(leaderEnumerationMeta1: MutableEntityModel): Return = defaultVisitReturn
    override fun leaveEnumerationMeta(leaderEnumerationMeta1: MutableEntityModel) {}

    override fun visitEnumerationValueMeta(leaderEnumerationValueMeta1: MutableEntityModel): Return = defaultVisitReturn
    override fun leaveEnumerationValueMeta(leaderEnumerationValueMeta1: MutableEntityModel) {}

    override fun visitEntityMeta(leaderEntityMeta1: MutableEntityModel): Return = defaultVisitReturn
    override fun leaveEntityMeta(leaderEntityMeta1: MutableEntityModel) {}

    override fun visitFieldMeta(leaderFieldMeta1: MutableEntityModel): Return = defaultVisitReturn
    override fun leaveFieldMeta(leaderFieldMeta1: MutableEntityModel) {}
}
