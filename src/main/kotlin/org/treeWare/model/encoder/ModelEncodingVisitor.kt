package org.treeWare.model.encoder

import org.treeWare.metaModel.*
import org.treeWare.model.core.*
import org.treeWare.model.traversal.Leader1Follower0ModelVisitor
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.model.traversal.forEach
import java.util.*

const val VALUE_KEY = "value"

class ModelEncodingVisitor(
    private val wireFormatEncoder: WireFormatEncoder,
    private val multiAuxEncoder: MultiAuxEncoder = MultiAuxEncoder(),
    private val encodePasswords: EncodePasswords = EncodePasswords.NONE
) : Leader1Follower0ModelVisitor<TraversalAction> {
    private var encodingPathKeys = false

    private fun encodeAuxs(name: String?, element: ElementModel) {
        element.auxs?.forEach { (auxName, aux) -> multiAuxEncoder.encode(name, auxName, aux, wireFormatEncoder) }
    }

    override fun visit(leaderMain1: MainModel): TraversalAction {
        wireFormatEncoder.encodeObjectStart(null)
        wireFormatEncoder.encodeObjectStart(leaderMain1.type)
        return TraversalAction.CONTINUE
    }

    override fun leave(leaderMain1: MainModel) {
        wireFormatEncoder.encodeObjectEnd()
        wireFormatEncoder.encodeObjectEnd()
    }

    override fun visit(leaderRoot1: RootModel): TraversalAction {
        // The root model has a resolved meta-model which does not have the
        // name of the root. The name is in the unresolved meta-model which
        // can be accessed from the main meta-model.
        val mainMeta = leaderRoot1.parent.meta
        val unresolvedRootMeta = mainMeta?.let { getRootMeta(mainMeta) }
        val name = unresolvedRootMeta?.let { getMetaName(unresolvedRootMeta) }
        encodeAuxs(name, leaderRoot1)
        wireFormatEncoder.encodeObjectStart(name)
        return TraversalAction.CONTINUE
    }

    override fun leave(leaderRoot1: RootModel) {
        wireFormatEncoder.encodeObjectEnd()
    }

    override fun visit(leaderEntity1: EntityModel): TraversalAction {
        val name = leaderEntity1.parent.meta?.let { getMetaName(it) } ?: ""
        wireFormatEncoder.encodeObjectStart(name)
        val isSetElement = leaderEntity1.parent.meta?.let { isSetFieldMeta(it) } ?: false
        if (isSetElement) encodeAuxs(null, leaderEntity1)
        return TraversalAction.CONTINUE
    }

    override fun leave(leaderEntity1: EntityModel) {
        wireFormatEncoder.encodeObjectEnd()
    }

    // Fields

    override fun visit(leaderField1: SingleFieldModel): TraversalAction {
        val fieldName = getFieldName(leaderField1)
        encodeAuxs(fieldName, leaderField1)
        return TraversalAction.CONTINUE
    }

    override fun leave(leaderField1: SingleFieldModel) {}

    override fun visit(leaderField1: ListFieldModel): TraversalAction {
        val fieldName = getFieldName(leaderField1)
        encodeAuxs(fieldName, leaderField1)
        wireFormatEncoder.encodeListStart(fieldName)
        return TraversalAction.CONTINUE
    }

    override fun leave(leaderField1: ListFieldModel) {
        wireFormatEncoder.encodeListEnd()
    }

    override fun visit(leaderField1: SetFieldModel): TraversalAction {
        val fieldName = getFieldName(leaderField1)
        encodeAuxs(fieldName, leaderField1)
        wireFormatEncoder.encodeListStart(fieldName)
        return TraversalAction.CONTINUE
    }

    override fun leave(leaderField1: SetFieldModel) {
        wireFormatEncoder.encodeListEnd()
    }

    // Values

    override fun visit(leaderValue1: PrimitiveModel): TraversalAction {
        val isListElement = leaderValue1.parent.meta?.let { isListFieldMeta(it) } ?: false
        val fieldName = if (isListElement) VALUE_KEY else leaderValue1.parent.meta?.let { getMetaName(it) } ?: ""
        val auxFieldName = if (isListElement) null else leaderValue1.parent.meta?.let { getMetaName(it) } ?: ""
        if (isListElement) wireFormatEncoder.encodeObjectStart(null)
        encodeAuxs(auxFieldName, leaderValue1)
        val value = leaderValue1.value
        if (value == null) {
            wireFormatEncoder.encodeNullField(fieldName)
            return TraversalAction.CONTINUE
        }
        when (leaderValue1.parent.meta?.let { getFieldTypeMeta(it) }) {
            FieldType.BOOLEAN -> wireFormatEncoder.encodeBooleanField(fieldName, value as Boolean)
            FieldType.BYTE -> wireFormatEncoder.encodeNumericField(fieldName, value as Byte)
            FieldType.SHORT -> wireFormatEncoder.encodeNumericField(fieldName, value as Short)
            FieldType.INT -> wireFormatEncoder.encodeNumericField(fieldName, value as Int)
            FieldType.FLOAT -> wireFormatEncoder.encodeNumericField(fieldName, value as Float)
            FieldType.DOUBLE -> wireFormatEncoder.encodeNumericField(fieldName, value as Double)
            // Integers in JavaScript are limited to 53 bits. So 64-bit values ("long", "timestamp")
            // are encoded as strings.
            FieldType.LONG, FieldType.TIMESTAMP -> wireFormatEncoder.encodeStringField(fieldName, value.toString())
            FieldType.BLOB -> wireFormatEncoder.encodeStringField(
                fieldName,
                Base64.getEncoder().encodeToString(value as ByteArray)
            )
            else -> wireFormatEncoder.encodeStringField(fieldName, value.toString())
        }
        return TraversalAction.CONTINUE
    }

    override fun leave(leaderValue1: PrimitiveModel) {
        val isListElement = leaderValue1.parent.meta?.let { isListFieldMeta(it) } ?: false
        if (isListElement) wireFormatEncoder.encodeObjectEnd()
    }

    override fun visit(leaderValue1: AliasModel): TraversalAction = TraversalAction.CONTINUE
    override fun leave(leaderValue1: AliasModel) {}

    override fun visit(leaderValue1: Password1wayModel): TraversalAction {
        when (encodePasswords) {
            EncodePasswords.NONE -> if (leaderValue1.auxs?.isEmpty() != false) return TraversalAction.CONTINUE
            EncodePasswords.HASHED_AND_ENCRYPTED ->
                if (leaderValue1.hashed == null && leaderValue1.auxs?.isEmpty() != false) return TraversalAction.CONTINUE
            EncodePasswords.ALL -> {
            }
        }

        val isListElement = leaderValue1.parent.meta?.let { isListFieldMeta(it) } ?: false
        val fieldName = leaderValue1.parent.meta?.let { getMetaName(it) } ?: ""
        val auxFieldName = if (isListElement) null else leaderValue1.parent.meta?.let { getMetaName(it) }
        if (!isListElement) encodeAuxs(auxFieldName, leaderValue1)
        wireFormatEncoder.encodeObjectStart(fieldName)
        if (isListElement) encodeAuxs(auxFieldName, leaderValue1)
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

    override fun leave(leaderValue1: Password1wayModel) {}

    override fun visit(leaderValue1: Password2wayModel): TraversalAction {
        when (encodePasswords) {
            EncodePasswords.NONE -> if (leaderValue1.auxs?.isEmpty() != false) return TraversalAction.CONTINUE
            EncodePasswords.HASHED_AND_ENCRYPTED ->
                if (leaderValue1.encrypted == null && leaderValue1.auxs?.isEmpty() != false) return TraversalAction.CONTINUE
            EncodePasswords.ALL -> {
            }
        }

        val isListElement = leaderValue1.parent.meta?.let { isListFieldMeta(it) } ?: false
        val fieldName = leaderValue1.parent.meta?.let { getMetaName(it) } ?: ""
        val auxFieldName = if (isListElement) null else leaderValue1.parent.meta?.let { getMetaName(it) }
        if (!isListElement) encodeAuxs(auxFieldName, leaderValue1)
        wireFormatEncoder.encodeObjectStart(fieldName)
        if (isListElement) encodeAuxs(auxFieldName, leaderValue1)
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

    override fun leave(leaderValue1: Password2wayModel) {}

    override fun visit(leaderValue1: EnumerationModel): TraversalAction {
        val isListElement = leaderValue1.parent.meta?.let { isListFieldMeta(it) } ?: false
        val fieldName = if (isListElement) VALUE_KEY else leaderValue1.parent.meta?.let { getMetaName(it) } ?: ""
        val auxFieldName = if (isListElement) null else leaderValue1.parent.meta?.let { getMetaName(it) }
        if (isListElement) wireFormatEncoder.encodeObjectStart(null)
        encodeAuxs(auxFieldName, leaderValue1)
        val value = leaderValue1.value
        if (value == null) wireFormatEncoder.encodeNullField(fieldName)
        else wireFormatEncoder.encodeStringField(fieldName, value)
        return TraversalAction.CONTINUE
    }

    override fun leave(leaderValue1: EnumerationModel) {
        val isListElement = leaderValue1.parent.meta?.let { isListFieldMeta(it) } ?: false
        if (isListElement) wireFormatEncoder.encodeObjectEnd()
    }

    override fun visit(leaderValue1: AssociationModel): TraversalAction {
        val isListElement = leaderValue1.parent.meta?.let { isListFieldMeta(it) } ?: false
        val fieldName = leaderValue1.parent.meta?.let { getMetaName(it) } ?: ""
        val auxFieldName = if (isListElement) null else leaderValue1.parent.meta?.let { getMetaName(it) }
        if (!isListElement) encodeAuxs(auxFieldName, leaderValue1)
        if (leaderValue1.value.isEmpty()) {
            wireFormatEncoder.encodeNullField(fieldName)
            return TraversalAction.CONTINUE
        }
        wireFormatEncoder.encodeObjectStart(fieldName)
        if (isListElement) encodeAuxs(auxFieldName, leaderValue1)
        wireFormatEncoder.encodeListStart("path_keys")
        encodingPathKeys = true
        leaderValue1.value.forEach { entityKeys ->
            // Traverse entityKeys with this visitor to encode it.
            forEach(entityKeys, this)
        }
        return TraversalAction.CONTINUE
    }

    override fun leave(leaderValue1: AssociationModel) {
        if (leaderValue1.value.isNotEmpty()) {
            encodingPathKeys = false
            wireFormatEncoder.encodeListEnd()
            wireFormatEncoder.encodeObjectEnd()
        }
    }

    // Sub-values

    override fun visit(leaderEntityKeys1: EntityKeysModel): TraversalAction {
        wireFormatEncoder.encodeObjectStart(leaderEntityKeys1.meta?.let { getMetaName(it) })
        return TraversalAction.CONTINUE
    }

    override fun leave(leaderEntityKeys1: EntityKeysModel) {
        wireFormatEncoder.encodeObjectEnd()
    }
}
