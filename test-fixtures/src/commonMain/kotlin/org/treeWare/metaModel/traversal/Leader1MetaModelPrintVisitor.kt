package org.treeWare.metaModel.traversal

import okio.BufferedSink
import org.treeWare.metaModel.getMetaName
import org.treeWare.model.core.EntityModel
import org.treeWare.model.encoder.JsonWireFormatEncoder
import org.treeWare.model.traversal.TraversalAction

class Leader1MetaModelPrintVisitor(bufferedSink: BufferedSink) : Leader1MetaModelVisitor<TraversalAction> {
    private val jsonEncoder = JsonWireFormatEncoder(bufferedSink, true)

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

    override fun visitMetaModel(leaderMeta1: EntityModel): TraversalAction {
        startObject("")
        startObject("meta_model", leaderMeta1)
        return TraversalAction.CONTINUE
    }

    override fun leaveMetaModel(leaderMeta1: EntityModel) {
        endObject()
        endObject()
    }

    override fun visitVersionMeta(leaderVersionMeta1: EntityModel): TraversalAction {
        startObject("version", leaderVersionMeta1)
        return TraversalAction.CONTINUE
    }

    override fun leaveVersionMeta(leaderVersionMeta1: EntityModel) {
        endObject()
    }

    override fun visitRootMeta(leaderRootMeta1: EntityModel): TraversalAction {
        startObject("root")
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