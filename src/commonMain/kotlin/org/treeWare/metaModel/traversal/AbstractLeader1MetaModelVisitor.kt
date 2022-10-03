package org.treeWare.metaModel.traversal

import org.treeWare.model.core.EntityModel
import org.treeWare.model.core.MainModel

abstract class AbstractLeader1MetaModelVisitor<Return>(
    private val defaultVisitReturn: Return
) : Leader1MetaModelVisitor<Return> {
    override fun visitMainMeta(leaderMainMeta1: MainModel): Return = defaultVisitReturn
    override fun leaveMainMeta(leaderMainMeta1: MainModel) {}

    override fun visitVersionMeta(leaderVersionMeta1: EntityModel): Return = defaultVisitReturn
    override fun leaveVersionMeta(leaderVersionMeta1: EntityModel) {}

    override fun visitRootMeta(leaderRootMeta1: EntityModel): Return = defaultVisitReturn
    override fun leaveRootMeta(leaderRootMeta1: EntityModel) {}

    override fun visitPackageMeta(leaderPackageMeta1: EntityModel): Return = defaultVisitReturn
    override fun leavePackageMeta(leaderPackageMeta1: EntityModel) {}

    override fun visitEnumerationMeta(leaderEnumerationMeta1: EntityModel): Return = defaultVisitReturn
    override fun leaveEnumerationMeta(leaderEnumerationMeta1: EntityModel) {}

    override fun visitEnumerationValueMeta(leaderEnumerationValueMeta1: EntityModel): Return = defaultVisitReturn
    override fun leaveEnumerationValueMeta(leaderEnumerationValueMeta1: EntityModel) {}

    override fun visitEntityMeta(leaderEntityMeta1: EntityModel): Return = defaultVisitReturn
    override fun leaveEntityMeta(leaderEntityMeta1: EntityModel) {}

    override fun visitFieldMeta(leaderFieldMeta1: EntityModel): Return = defaultVisitReturn
    override fun leaveFieldMeta(leaderFieldMeta1: EntityModel) {}
}
