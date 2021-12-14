package org.treeWare.metaModel.traversal

import org.treeWare.metaModel.getMetaName
import org.treeWare.model.core.EntityModel
import org.treeWare.model.core.MainModel
import org.treeWare.model.encoder.JsonWireFormatEncoder
import org.treeWare.model.traversal.TraversalAction
import java.io.Writer

class Leader1MetaModelPrintVisitor(
    writer: Writer
) : Leader1MetaModelVisitor<TraversalAction> {
    private val jsonEncoder = JsonWireFormatEncoder(writer, true)

    private fun startObject(prefix: String, namedMeta: EntityModel? = null) {
        val name = listOf(prefix, getMetaName(namedMeta)).filter { it.isNotEmpty() }.joinToString(":")
        jsonEncoder.encodeObjectStart(name)
    }

    private fun endObject() {
        jsonEncoder.encodeObjectEnd()
    }

    private fun startList(prefix: String, namedMeta: EntityModel) {
        val name = getMetaName(namedMeta)
        jsonEncoder.encodeListStart("$prefix:$name")
    }

    private fun endList() {
        jsonEncoder.encodeListEnd()
    }

    override fun visitMainMeta(leaderMainMeta1: MainModel): TraversalAction {
        startObject("")
        startObject("meta_model")
        return TraversalAction.CONTINUE
    }

    override fun leaveMainMeta(leaderMainMeta1: MainModel) {
        endObject()
        endObject()
    }

    override fun visitRootMeta(leaderRootMeta1: EntityModel): TraversalAction {
        startObject("root", leaderRootMeta1)
        return TraversalAction.CONTINUE
    }

    override fun leaveRootMeta(leaderRootMeta1: EntityModel) {
        endObject()
    }

    override fun visitPackageMeta(leaderPackageMeta1: EntityModel): TraversalAction {
        startObject("package", leaderPackageMeta1)
        return TraversalAction.CONTINUE
    }

    override fun leavePackageMeta(leaderPackageMeta1: EntityModel) {
        endObject()
    }

    override fun visitEnumerationMeta(leaderEnumerationMeta1: EntityModel): TraversalAction {
        startList("enumeration", leaderEnumerationMeta1)
        return TraversalAction.CONTINUE
    }

    override fun leaveEnumerationMeta(leaderEnumerationMeta1: EntityModel) {
        endList()
    }

    override fun visitEnumerationValueMeta(leaderEnumerationValueMeta1: EntityModel): TraversalAction {
        val value = getMetaName(leaderEnumerationValueMeta1)
        jsonEncoder.encodeStringField("", value)
        return TraversalAction.CONTINUE
    }

    override fun leaveEnumerationValueMeta(leaderEnumerationValueMeta1: EntityModel) {}

    override fun visitEntityMeta(leaderEntityMeta1: EntityModel): TraversalAction {
        startObject("entity", leaderEntityMeta1)
        return TraversalAction.CONTINUE
    }

    override fun leaveEntityMeta(leaderEntityMeta1: EntityModel) {
        endObject()
    }

    override fun visitFieldMeta(leaderFieldMeta1: EntityModel): TraversalAction {
        startObject("field", leaderFieldMeta1)
        return TraversalAction.CONTINUE
    }

    override fun leaveFieldMeta(leaderFieldMeta1: EntityModel) {
        endObject()
    }
}
