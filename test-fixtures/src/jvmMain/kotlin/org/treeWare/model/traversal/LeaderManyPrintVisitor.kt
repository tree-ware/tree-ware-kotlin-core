package org.treeWare.model.traversal

import okio.BufferedSink
import org.treeWare.metaModel.FieldType
import org.treeWare.metaModel.getFieldTypeMeta
import org.treeWare.metaModel.getMetaName
import org.treeWare.metaModel.getPackageName
import org.treeWare.model.core.*
import org.treeWare.model.encoder.PrettyPrintHelper
import org.treeWare.util.encodeBase64

class LeaderManyPrintVisitor(private val bufferedSink: BufferedSink) :
    AbstractLeaderManyModelVisitor<TraversalAction>(TraversalAction.CONTINUE) {
    private val prettyPrinter = PrettyPrintHelper(true)

    private fun print(key: String, values: List<String?> = listOf("TODO")) {
        bufferedSink.writeUtf8(prettyPrinter.currentIndent)
        bufferedSink.writeUtf8(key)
        bufferedSink.writeUtf8(": ")
        bufferedSink.writeUtf8(values.joinToString(", "))
        bufferedSink.writeUtf8(prettyPrinter.endOfLine)
    }

    private fun printFieldNames(key: String, leaderFieldList: List<FieldModel?>) {
        val fieldNames = leaderFieldList.map { field -> field?.meta?.let { getMetaName(it) } }
        print(key, fieldNames)
    }

    override fun visitEntity(leaderEntityList: List<EntityModel?>): TraversalAction {
        val entityNames = leaderEntityList.map { entity ->
            entity?.meta?.let {
                val packageName = getPackageName(it)
                val entityName = getMetaName(it)
                "$packageName/$entityName"
            }
        }
        print("Entity", entityNames)
        prettyPrinter.indent()
        return TraversalAction.CONTINUE
    }

    override fun leaveEntity(leaderEntityList: List<EntityModel?>) {
        prettyPrinter.unindent()
    }

    override fun visitSingleField(leaderFieldList: List<SingleFieldModel?>): TraversalAction {
        printFieldNames("Single field", leaderFieldList)
        prettyPrinter.indent()
        return TraversalAction.CONTINUE
    }

    override fun leaveSingleField(leaderFieldList: List<SingleFieldModel?>) {
        prettyPrinter.unindent()
    }

    override fun visitSetField(leaderFieldList: List<SetFieldModel?>): TraversalAction {
        printFieldNames("Set field", leaderFieldList)
        prettyPrinter.indent()
        return TraversalAction.CONTINUE
    }

    override fun leaveSetField(leaderFieldList: List<SetFieldModel?>) {
        prettyPrinter.unindent()
    }

    override fun visitPrimitive(leaderValueList: List<PrimitiveModel?>): TraversalAction {
        val values = leaderValueList.map { valueElement ->
            valueElement?.value?.let { value ->
                val fieldType = valueElement.parent.meta?.let { getFieldTypeMeta(it) }
                if (fieldType == FieldType.BLOB) encodeBase64(value as ByteArray)
                else value.toString()
            }
        }
        print("Primitive", values)
        return TraversalAction.CONTINUE
    }

    override fun visitAlias(leaderValueList: List<AliasModel?>): TraversalAction {
        print("Alias")
        return TraversalAction.CONTINUE
    }

    override fun visitPassword1way(leaderValueList: List<Password1wayModel?>): TraversalAction {
        val values = leaderValueList.map { it?.let { "{${it.unhashed}, ${it.hashed}, ${it.hashVersion}}" } }
        print("Password1way", values)
        return TraversalAction.CONTINUE
    }

    override fun visitPassword2way(leaderValueList: List<Password2wayModel?>): TraversalAction {
        val values = leaderValueList.map { it?.let { "{${it.unencrypted}, ${it.encrypted}, ${it.cipherVersion}}" } }
        print("Password2way", values)
        return TraversalAction.CONTINUE
    }

    override fun visitEnumeration(leaderValueList: List<EnumerationModel?>): TraversalAction {
        val values = leaderValueList.map { it?.value }
        print("Enumeration", values)
        return TraversalAction.CONTINUE
    }

    override fun visitAssociation(leaderValueList: List<AssociationModel?>): TraversalAction {
        print("Association", listOf("..."))
        prettyPrinter.indent()
        return TraversalAction.CONTINUE
    }

    override fun leaveAssociation(leaderValueList: List<AssociationModel?>) {
        prettyPrinter.unindent()
    }
}