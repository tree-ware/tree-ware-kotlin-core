package org.treeWare.model.traversal

import okio.BufferedSink
import org.treeWare.metaModel.FieldType
import org.treeWare.metaModel.getFieldTypeMeta
import org.treeWare.metaModel.getMetaName
import org.treeWare.metaModel.getPackageName
import org.treeWare.model.core.*
import org.treeWare.model.encoder.PrettyPrintHelper
import java.util.*

class Leader1PrintVisitor(private val bufferedSink: BufferedSink) :
    AbstractLeader1ModelVisitor<TraversalAction>(TraversalAction.CONTINUE) {
    private val prettyPrinter = PrettyPrintHelper(true)

    private fun print(key: String, value: String? = "TODO") {
        bufferedSink.writeUtf8(prettyPrinter.currentIndent)
        bufferedSink.writeUtf8(key)
        bufferedSink.writeUtf8(": ")
        bufferedSink.writeUtf8(value ?: "null")
        bufferedSink.writeUtf8(prettyPrinter.endOfLine)
    }

    private fun printFieldName(key: String, leaderField: FieldModel) {
        val fieldName = leaderField.meta?.let { getMetaName(it) }
        print(key, fieldName)
    }

    override fun visitMain(leaderMain1: MainModel): TraversalAction {
        val mainName = leaderMain1.meta?.let { getMetaName(it) }
        print("Main", mainName)
        prettyPrinter.indent()
        return TraversalAction.CONTINUE
    }

    override fun leaveMain(leaderMain1: MainModel) {
        prettyPrinter.unindent()
    }

    override fun visitEntity(leaderEntity1: EntityModel): TraversalAction {
        val entityName = leaderEntity1.meta?.let {
            val packageName = getPackageName(it)
            val entityName = getMetaName(it)
            "$packageName/$entityName"
        }
        print("Entity", entityName)
        prettyPrinter.indent()
        return TraversalAction.CONTINUE
    }

    override fun leaveEntity(leaderEntity1: EntityModel) {
        prettyPrinter.unindent()
    }

    override fun visitSingleField(leaderField1: SingleFieldModel): TraversalAction {
        printFieldName("Single field", leaderField1)
        prettyPrinter.indent()
        return TraversalAction.CONTINUE
    }

    override fun leaveSingleField(leaderField1: SingleFieldModel) {
        prettyPrinter.unindent()
    }

    override fun visitListField(leaderField1: ListFieldModel): TraversalAction {
        printFieldName("List field", leaderField1)
        prettyPrinter.indent()
        return TraversalAction.CONTINUE
    }

    override fun leaveListField(leaderField1: ListFieldModel) {
        prettyPrinter.unindent()
    }

    override fun visitSetField(leaderField1: SetFieldModel): TraversalAction {
        printFieldName("Set field", leaderField1)
        prettyPrinter.indent()
        return TraversalAction.CONTINUE
    }

    override fun leaveSetField(leaderField1: SetFieldModel) {
        prettyPrinter.unindent()
    }

    override fun visitPrimitive(leaderValue1: PrimitiveModel): TraversalAction {
        val fieldType = leaderValue1.parent.meta?.let { getFieldTypeMeta(it) }
        val value = if (fieldType == FieldType.BLOB) Base64.getEncoder().encodeToString(leaderValue1.value as ByteArray)
        else leaderValue1.value.toString()
        print("Primitive", value)
        return TraversalAction.CONTINUE
    }

    override fun visitAlias(leaderValue1: AliasModel): TraversalAction {
        print("Alias")
        return TraversalAction.CONTINUE
    }

    override fun visitPassword1way(leaderValue1: Password1wayModel): TraversalAction {
        val value = leaderValue1.let { "{${it.unhashed}, ${it.hashed}, ${it.hashVersion}}" }
        print("Password1way", value)
        return TraversalAction.CONTINUE
    }

    override fun visitPassword2way(leaderValue1: Password2wayModel): TraversalAction {
        val value = leaderValue1.let { "{${it.unencrypted}, ${it.encrypted}, ${it.cipherVersion}}" }
        print("Password2way", value)
        return TraversalAction.CONTINUE
    }

    override fun visitEnumeration(leaderValue1: EnumerationModel): TraversalAction {
        print("Enumeration", leaderValue1.value)
        return TraversalAction.CONTINUE
    }

    override fun visitAssociation(leaderValue1: AssociationModel): TraversalAction {
        print("Association", "...")
        prettyPrinter.indent()
        return TraversalAction.CONTINUE
    }

    override fun leaveAssociation(leaderValue1: AssociationModel) {
        prettyPrinter.unindent()
    }
}