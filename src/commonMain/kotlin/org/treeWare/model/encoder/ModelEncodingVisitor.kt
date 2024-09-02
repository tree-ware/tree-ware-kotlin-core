package org.treeWare.model.encoder

import org.treeWare.metaModel.FieldType
import org.treeWare.metaModel.getFieldTypeMeta
import org.treeWare.metaModel.getMetaName
import org.treeWare.metaModel.isCollectionFieldMeta
import org.treeWare.model.core.*
import org.treeWare.model.traversal.Leader1ModelVisitor
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.util.encodeBase64

class ModelEncodingVisitor(
    private val wireFormatEncoder: WireFormatEncoder,
    private val multiAuxEncoder: MultiAuxEncoder = MultiAuxEncoder(),
    private val encodePasswords: EncodePasswords = EncodePasswords.NONE
) : Leader1ModelVisitor<TraversalAction> {
    private var isEncodingAssociation = false

    private fun encodeAuxs(name: String?, element: ElementModel) {
        element.auxs?.forEach { (auxName, aux) -> multiAuxEncoder.encode(name, auxName, aux, wireFormatEncoder) }
    }

    override fun visitEntity(leaderEntity1: EntityModel): TraversalAction {
        val name = leaderEntity1.parent?.meta?.let { getMetaName(it) } ?: ""
        wireFormatEncoder.encodeObjectStart(name)
        val isCollectionElement = leaderEntity1.parent?.meta?.let { isCollectionFieldMeta(it) } ?: false
        if (isCollectionElement) encodeAuxs(null, leaderEntity1)
        else if (!isEncodingAssociation) encodeAuxs(null, leaderEntity1)
        return TraversalAction.CONTINUE
    }

    override fun leaveEntity(leaderEntity1: EntityModel) {
        wireFormatEncoder.encodeObjectEnd()
    }

    // Fields

    override fun visitSingleField(leaderField1: SingleFieldModel): TraversalAction {
        val fieldName = getFieldName(leaderField1)
        encodeAuxs(fieldName, leaderField1)
        if (leaderField1.value == null) wireFormatEncoder.encodeNullField(fieldName)
        return TraversalAction.CONTINUE
    }

    override fun leaveSingleField(leaderField1: SingleFieldModel) {}

    override fun visitSetField(leaderField1: SetFieldModel): TraversalAction {
        val fieldName = getFieldName(leaderField1)
        encodeAuxs(fieldName, leaderField1)
        wireFormatEncoder.encodeListStart(fieldName)
        return TraversalAction.CONTINUE
    }

    override fun leaveSetField(leaderField1: SetFieldModel) {
        wireFormatEncoder.encodeListEnd()
    }

    // Values

    override fun visitPrimitive(leaderValue1: PrimitiveModel): TraversalAction {
        val fieldName = leaderValue1.parent.meta?.let { getMetaName(it) } ?: ""
        val auxFieldName = leaderValue1.parent.meta?.let { getMetaName(it) } ?: ""
        encodeAuxs(auxFieldName, leaderValue1)
        val value = leaderValue1.value
        // Integers in JavaScript are limited to 53 bits. So 64-bit values ("long", "timestamp")
        // are encoded as strings.
        when (leaderValue1.parent.meta?.let { getFieldTypeMeta(it) }) {
            FieldType.BOOLEAN -> wireFormatEncoder.encodeBooleanField(fieldName, value as Boolean)
            FieldType.UINT8 -> wireFormatEncoder.encodeNumericField(fieldName, value as UByte)
            FieldType.UINT16 -> wireFormatEncoder.encodeNumericField(fieldName, value as UShort)
            FieldType.UINT32 -> wireFormatEncoder.encodeNumericField(fieldName, value as UInt)
            FieldType.UINT64 -> wireFormatEncoder.encodeStringField(fieldName, value.toString())
            FieldType.INT8 -> wireFormatEncoder.encodeNumericField(fieldName, value as Byte)
            FieldType.INT16 -> wireFormatEncoder.encodeNumericField(fieldName, value as Short)
            FieldType.INT32 -> wireFormatEncoder.encodeNumericField(fieldName, value as Int)
            FieldType.INT64 -> wireFormatEncoder.encodeStringField(fieldName, value.toString())
            FieldType.FLOAT -> wireFormatEncoder.encodeNumericField(fieldName, value as Float)
            FieldType.DOUBLE -> wireFormatEncoder.encodeNumericField(fieldName, value as Double)
            FieldType.BIG_INTEGER -> wireFormatEncoder.encodeStringField(fieldName, value.toString())
            FieldType.BIG_DECIMAL -> wireFormatEncoder.encodeStringField(fieldName, value.toString())
            FieldType.TIMESTAMP -> wireFormatEncoder.encodeStringField(fieldName, value.toString())
            FieldType.BLOB -> wireFormatEncoder.encodeStringField(fieldName, encodeBase64(value as ByteArray))
            else -> wireFormatEncoder.encodeStringField(fieldName, value.toString())
        }
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
            EncodePasswords.ALL -> {
            }
        }

        val fieldName = leaderValue1.parent.meta?.let { getMetaName(it) } ?: ""
        val auxFieldName = leaderValue1.parent.meta?.let { getMetaName(it) }
        encodeAuxs(auxFieldName, leaderValue1)
        wireFormatEncoder.encodeObjectStart(fieldName)
        leaderValue1.unhashed?.also {
            if (encodePasswords == EncodePasswords.ALL) wireFormatEncoder.encodeStringField("unhashed", it)
        }
        leaderValue1.hashed?.also {
            if (encodePasswords == EncodePasswords.ALL || encodePasswords == EncodePasswords.HASHED_AND_ENCRYPTED) {
                wireFormatEncoder.encodeStringField("hashed", it)
                wireFormatEncoder.encodeNumericField("hash_version", leaderValue1.hashVersion)
            }
        }
        wireFormatEncoder.encodeObjectEnd()
        return TraversalAction.CONTINUE
    }

    override fun leavePassword1way(leaderValue1: Password1wayModel) {}

    override fun visitPassword2way(leaderValue1: Password2wayModel): TraversalAction {
        when (encodePasswords) {
            EncodePasswords.NONE -> if (leaderValue1.auxs?.isEmpty() != false) return TraversalAction.CONTINUE
            EncodePasswords.HASHED_AND_ENCRYPTED ->
                if (leaderValue1.encrypted == null && leaderValue1.auxs?.isEmpty() != false) return TraversalAction.CONTINUE
            EncodePasswords.ALL -> {
            }
        }

        val fieldName = leaderValue1.parent.meta?.let { getMetaName(it) } ?: ""
        val auxFieldName = leaderValue1.parent.meta?.let { getMetaName(it) }
        encodeAuxs(auxFieldName, leaderValue1)
        wireFormatEncoder.encodeObjectStart(fieldName)
        leaderValue1.unencrypted?.also {
            if (encodePasswords == EncodePasswords.ALL) wireFormatEncoder.encodeStringField("unencrypted", it)
        }
        leaderValue1.encrypted?.also {
            if (encodePasswords == EncodePasswords.ALL || encodePasswords == EncodePasswords.HASHED_AND_ENCRYPTED) {
                wireFormatEncoder.encodeStringField("encrypted", it)
                wireFormatEncoder.encodeNumericField("cipher_version", leaderValue1.cipherVersion)
            }
        }
        wireFormatEncoder.encodeObjectEnd()
        return TraversalAction.CONTINUE
    }

    override fun leavePassword2way(leaderValue1: Password2wayModel) {}

    override fun visitEnumeration(leaderValue1: EnumerationModel): TraversalAction {
        val fieldName = leaderValue1.parent.meta?.let { getMetaName(it) } ?: ""
        val auxFieldName = leaderValue1.parent.meta?.let { getMetaName(it) }
        encodeAuxs(auxFieldName, leaderValue1)
        val value = leaderValue1.value
        wireFormatEncoder.encodeStringField(fieldName, value)
        return TraversalAction.CONTINUE
    }

    override fun leaveEnumeration(leaderValue1: EnumerationModel) {}

    override fun visitAssociation(leaderValue1: AssociationModel): TraversalAction {
        val auxFieldName = leaderValue1.parent.meta?.let { getMetaName(it) }
        encodeAuxs(auxFieldName, leaderValue1)
        isEncodingAssociation = true
        return TraversalAction.CONTINUE
    }

    override fun leaveAssociation(leaderValue1: AssociationModel) {
        isEncodingAssociation = false
    }
}