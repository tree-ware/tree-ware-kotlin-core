package org.treeWare.model.encoder

import okio.BufferedSink
import org.treeWare.metaModel.FieldType
import org.treeWare.metaModel.getFieldTypeMeta
import org.treeWare.model.core.*
import org.treeWare.model.operator.ModelPathStack
import org.treeWare.model.traversal.Leader1ModelVisitor
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.util.encodeBase64

class PathEncodingVisitor(
    private val sink: BufferedSink,
    private val multiAuxEncoder: MultiAuxEncoder = MultiAuxEncoder(),
    private val encodePasswords: EncodePasswords = EncodePasswords.NONE
) : Leader1ModelVisitor<TraversalAction> {
    private val modelPathStack = ModelPathStack()
    private var isEncodingAssociation = false

    override fun visitEntity(leaderEntity1: EntityModel): TraversalAction {
        modelPathStack.pushEntity(leaderEntity1)
        return TraversalAction.CONTINUE
    }

    override fun leaveEntity(leaderEntity1: EntityModel) {
        modelPathStack.popEntity()
    }

    // Fields

    override fun visitSingleField(leaderField1: SingleFieldModel): TraversalAction {
        modelPathStack.pushField(leaderField1)
        return TraversalAction.CONTINUE
    }

    override fun leaveSingleField(leaderField1: SingleFieldModel) {
        modelPathStack.popField()
    }

    override fun visitSetField(leaderField1: SetFieldModel): TraversalAction {
        modelPathStack.pushField(leaderField1)
        return TraversalAction.CONTINUE
    }

    override fun leaveSetField(leaderField1: SetFieldModel) {
        modelPathStack.popField()
    }

    // Values

    override fun visitPrimitive(leaderValue1: PrimitiveModel): TraversalAction {
        val path = modelPathStack.peekModelPath()
        val value = leaderValue1.value

        val formattedValue = when (leaderValue1.parent.meta?.let { getFieldTypeMeta(it) }) {
            FieldType.BOOLEAN,
            FieldType.UINT8,
            FieldType.UINT16,
            FieldType.UINT32,
            FieldType.UINT64,
            FieldType.INT8,
            FieldType.INT16,
            FieldType.INT32,
            FieldType.INT64,
            FieldType.FLOAT,
            FieldType.DOUBLE,
            FieldType.BIG_INTEGER,
            FieldType.BIG_DECIMAL,
            FieldType.TIMESTAMP -> value.toString()
            FieldType.BLOB -> "\"${encodeBase64(value as ByteArray)}\""
            else -> "\"${value}\""
        }

        sink.writeUtf8("$path = $formattedValue\n")
        return TraversalAction.CONTINUE
    }

    override fun leavePrimitive(leaderValue1: PrimitiveModel) {}

    override fun visitAlias(leaderValue1: AliasModel): TraversalAction = TraversalAction.CONTINUE
    override fun leaveAlias(leaderValue1: AliasModel) {}

    override fun visitPassword1way(leaderValue1: Password1wayModel): TraversalAction {
        when (encodePasswords) {
            EncodePasswords.NONE -> if (leaderValue1.auxs?.isEmpty() != false) return TraversalAction.CONTINUE
            EncodePasswords.HASHED_AND_ENCRYPTED ->
                if (leaderValue1.hashed == null && leaderValue1.auxs?.isEmpty() != false) return TraversalAction.CONTINUE
            EncodePasswords.ALL -> {}
        }

        val basePath = modelPathStack.peekModelPath()

        leaderValue1.unhashed?.also {
            if (encodePasswords == EncodePasswords.ALL) {
                sink.writeUtf8("$basePath/unhashed = \"$it\"\n")
            }
        }
        leaderValue1.hashed?.also {
            if (encodePasswords == EncodePasswords.ALL || encodePasswords == EncodePasswords.HASHED_AND_ENCRYPTED) {
                sink.writeUtf8("$basePath/hashed = \"$it\"\n")
                sink.writeUtf8("$basePath/hash_version = ${leaderValue1.hashVersion}\n")
            }
        }
        return TraversalAction.CONTINUE
    }

    override fun leavePassword1way(leaderValue1: Password1wayModel) {}

    override fun visitPassword2way(leaderValue1: Password2wayModel): TraversalAction {
        when (encodePasswords) {
            EncodePasswords.NONE -> if (leaderValue1.auxs?.isEmpty() != false) return TraversalAction.CONTINUE
            EncodePasswords.HASHED_AND_ENCRYPTED ->
                if (leaderValue1.encrypted == null && leaderValue1.auxs?.isEmpty() != false) return TraversalAction.CONTINUE
            EncodePasswords.ALL -> {}
        }

        val basePath = modelPathStack.peekModelPath()

        leaderValue1.unencrypted?.also {
            if (encodePasswords == EncodePasswords.ALL) {
                sink.writeUtf8("$basePath/unencrypted = \"$it\"\n")
            }
        }
        leaderValue1.encrypted?.also {
            if (encodePasswords == EncodePasswords.ALL || encodePasswords == EncodePasswords.HASHED_AND_ENCRYPTED) {
                sink.writeUtf8("$basePath/encrypted = \"$it\"\n")
                sink.writeUtf8("$basePath/cipher_version = ${leaderValue1.cipherVersion}\n")
            }
        }
        return TraversalAction.CONTINUE
    }

    override fun leavePassword2way(leaderValue2: Password2wayModel) {}

    override fun visitEnumeration(leaderValue1: EnumerationModel): TraversalAction {
        val path = modelPathStack.peekModelPath()
        val value = leaderValue1.value
        sink.writeUtf8("$path = \"$value\"\n")
        return TraversalAction.CONTINUE
    }

    override fun leaveEnumeration(leaderValue1: EnumerationModel) {}

    override fun visitAssociation(leaderValue1: AssociationModel): TraversalAction {
        isEncodingAssociation = true
        return TraversalAction.CONTINUE
    }

    override fun leaveAssociation(leaderValue1: AssociationModel) {
        isEncodingAssociation = false
    }
}