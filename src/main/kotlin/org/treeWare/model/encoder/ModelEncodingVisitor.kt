package org.treeWare.model.encoder

import org.treeWare.metaModel.*
import org.treeWare.model.core.*
import org.treeWare.model.traversal.Leader1Follower0ModelVisitor
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.model.traversal.forEach
import java.util.*

const val VALUE_KEY = "value"

class ModelEncodingVisitor<Aux>(
    private val wireFormatEncoder: WireFormatEncoder,
    private val auxEncoder: AuxEncoder?,
    private val encodePasswords: EncodePasswords = EncodePasswords.NONE
) : Leader1Follower0ModelVisitor<Aux, TraversalAction> {
    private var encodingPathKeys = false

    override fun visit(leaderMain1: MainModel<Aux>): TraversalAction {
        wireFormatEncoder.encodeObjectStart(null)
        wireFormatEncoder.encodeObjectStart(leaderMain1.type)
        return TraversalAction.CONTINUE
    }

    override fun leave(leaderMain1: MainModel<Aux>) {
        wireFormatEncoder.encodeObjectEnd()
        wireFormatEncoder.encodeObjectEnd()
    }

    override fun visit(leaderRoot1: RootModel<Aux>): TraversalAction {
        // The root model has a resolved meta-model which does not have the
        // name of the root. The name is in the unresolved meta-model which
        // can be accessed from the main meta-model.
        val mainMeta = leaderRoot1.parent.meta
        val unresolvedRootMeta = mainMeta?.let { getRootMeta(mainMeta) }
        val name = unresolvedRootMeta?.let { getMetaName(unresolvedRootMeta) }
        auxEncoder?.also { it.encode(name, leaderRoot1.aux, wireFormatEncoder) }
        wireFormatEncoder.encodeObjectStart(name)
        return TraversalAction.CONTINUE
    }

    override fun leave(leaderRoot1: RootModel<Aux>) {
        wireFormatEncoder.encodeObjectEnd()
    }

    override fun visit(leaderEntity1: EntityModel<Aux>): TraversalAction {
        val name = leaderEntity1.parent.meta?.let { getMetaName(it) } ?: ""
        wireFormatEncoder.encodeObjectStart(name)
        val isSetElement = leaderEntity1.parent.meta?.let { isSetFieldMeta(it) } ?: false
        if (isSetElement && auxEncoder != null) auxEncoder.encode(null, leaderEntity1.aux, wireFormatEncoder)
        return TraversalAction.CONTINUE
    }

    override fun leave(leaderEntity1: EntityModel<Aux>) {
        wireFormatEncoder.encodeObjectEnd()
    }

    // Fields

    override fun visit(leaderField1: SingleFieldModel<Aux>): TraversalAction {
        val fieldName = leaderField1.meta?.let { getMetaName(it) }
        auxEncoder?.also { it.encode(fieldName, leaderField1.aux, wireFormatEncoder) }
        return TraversalAction.CONTINUE
    }

    override fun leave(leaderField1: SingleFieldModel<Aux>) {}

    override fun visit(leaderField1: ListFieldModel<Aux>): TraversalAction {
        val fieldName = leaderField1.meta?.let { getMetaName(it) }
        auxEncoder?.also { it.encode(fieldName, leaderField1.aux, wireFormatEncoder) }
        wireFormatEncoder.encodeListStart(fieldName)
        return TraversalAction.CONTINUE
    }

    override fun leave(leaderField1: ListFieldModel<Aux>) {
        wireFormatEncoder.encodeListEnd()
    }

    override fun visit(leaderField1: SetFieldModel<Aux>): TraversalAction {
        val fieldName = leaderField1.meta?.let { getMetaName(it) }
        auxEncoder?.also { it.encode(fieldName, leaderField1.aux, wireFormatEncoder) }
        wireFormatEncoder.encodeListStart(fieldName)
        return TraversalAction.CONTINUE
    }

    override fun leave(leaderField1: SetFieldModel<Aux>) {
        wireFormatEncoder.encodeListEnd()
    }

    // Values

    override fun visit(leaderValue1: PrimitiveModel<Aux>): TraversalAction {
        val isListElement = leaderValue1.parent.meta?.let { isListFieldMeta(it) } ?: false
        val fieldName = if (isListElement) VALUE_KEY else leaderValue1.parent.meta?.let { getMetaName(it) } ?: ""
        val auxFieldName = if (isListElement) null else leaderValue1.parent.meta?.let { getMetaName(it) } ?: ""
        if (isListElement) wireFormatEncoder.encodeObjectStart(null)
        auxEncoder?.also { it.encode(auxFieldName, leaderValue1.aux, wireFormatEncoder) }
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

    override fun leave(leaderValue1: PrimitiveModel<Aux>) {
        val isListElement = leaderValue1.parent.meta?.let { isListFieldMeta(it) } ?: false
        if (isListElement) wireFormatEncoder.encodeObjectEnd()
    }

    override fun visit(leaderValue1: AliasModel<Aux>): TraversalAction = TraversalAction.CONTINUE
    override fun leave(leaderValue1: AliasModel<Aux>) {}

    override fun visit(leaderValue1: Password1wayModel<Aux>): TraversalAction {
        when (encodePasswords) {
            EncodePasswords.NONE ->
                if (auxEncoder == null || leaderValue1.aux == null) return TraversalAction.CONTINUE
            EncodePasswords.HASHED_AND_ENCRYPTED ->
                if (leaderValue1.hashed == null && (auxEncoder == null || leaderValue1.aux == null)) return TraversalAction.CONTINUE
            EncodePasswords.ALL -> {
            }
        }

        val isListElement = leaderValue1.parent.meta?.let { isListFieldMeta(it) } ?: false
        val fieldName = leaderValue1.parent.meta?.let { getMetaName(it) } ?: ""
        val auxFieldName = if (isListElement) null else leaderValue1.parent.meta?.let { getMetaName(it) }
        if (!isListElement) auxEncoder?.also { it.encode(auxFieldName, leaderValue1.aux, wireFormatEncoder) }
        wireFormatEncoder.encodeObjectStart(fieldName)
        if (isListElement) auxEncoder?.also { it.encode(auxFieldName, leaderValue1.aux, wireFormatEncoder) }
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

    override fun leave(leaderValue1: Password1wayModel<Aux>) {}

    override fun visit(leaderValue1: Password2wayModel<Aux>): TraversalAction {
        when (encodePasswords) {
            EncodePasswords.NONE ->
                if (auxEncoder == null || leaderValue1.aux == null) return TraversalAction.CONTINUE
            EncodePasswords.HASHED_AND_ENCRYPTED ->
                if (leaderValue1.encrypted == null && (auxEncoder == null || leaderValue1.aux == null)) return TraversalAction.CONTINUE
            EncodePasswords.ALL -> {
            }
        }

        val isListElement = leaderValue1.parent.meta?.let { isListFieldMeta(it) } ?: false
        val fieldName = leaderValue1.parent.meta?.let { getMetaName(it) } ?: ""
        val auxFieldName = if (isListElement) null else leaderValue1.parent.meta?.let { getMetaName(it) }
        if (!isListElement) auxEncoder?.also { it.encode(auxFieldName, leaderValue1.aux, wireFormatEncoder) }
        wireFormatEncoder.encodeObjectStart(fieldName)
        if (isListElement) auxEncoder?.also { it.encode(auxFieldName, leaderValue1.aux, wireFormatEncoder) }
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

    override fun leave(leaderValue1: Password2wayModel<Aux>) {}

    override fun visit(leaderValue1: EnumerationModel<Aux>): TraversalAction {
        val isListElement = leaderValue1.parent.meta?.let { isListFieldMeta(it) } ?: false
        val fieldName = if (isListElement) VALUE_KEY else leaderValue1.parent.meta?.let { getMetaName(it) } ?: ""
        val auxFieldName = if (isListElement) null else leaderValue1.parent.meta?.let { getMetaName(it) }
        if (isListElement) wireFormatEncoder.encodeObjectStart(null)
        auxEncoder?.also { it.encode(auxFieldName, leaderValue1.aux, wireFormatEncoder) }
        val value = leaderValue1.value
        if (value == null) wireFormatEncoder.encodeNullField(fieldName)
        else wireFormatEncoder.encodeStringField(fieldName, value)
        return TraversalAction.CONTINUE
    }

    override fun leave(leaderValue1: EnumerationModel<Aux>) {
        val isListElement = leaderValue1.parent.meta?.let { isListFieldMeta(it) } ?: false
        if (isListElement) wireFormatEncoder.encodeObjectEnd()
    }

    override fun visit(leaderValue1: AssociationModel<Aux>): TraversalAction {
        val isListElement = leaderValue1.parent.meta?.let { isListFieldMeta(it) } ?: false
        val fieldName = leaderValue1.parent.meta?.let { getMetaName(it) } ?: ""
        val auxFieldName = if (isListElement) null else leaderValue1.parent.meta?.let { getMetaName(it) }
        if (!isListElement) auxEncoder?.also { it.encode(auxFieldName, leaderValue1.aux, wireFormatEncoder) }
        if (leaderValue1.value.isEmpty()) {
            wireFormatEncoder.encodeNullField(fieldName)
            return TraversalAction.CONTINUE
        }
        wireFormatEncoder.encodeObjectStart(fieldName)
        if (isListElement) auxEncoder?.also { it.encode(auxFieldName, leaderValue1.aux, wireFormatEncoder) }
        wireFormatEncoder.encodeListStart("path_keys")
        encodingPathKeys = true
        leaderValue1.value.forEach { entityKeys ->
            // Traverse entityKeys with this visitor to encode it.
            forEach(entityKeys, this)
        }
        return TraversalAction.CONTINUE
    }

    override fun leave(leaderValue1: AssociationModel<Aux>) {
        if (leaderValue1.value.isNotEmpty()) {
            encodingPathKeys = false
            wireFormatEncoder.encodeListEnd()
            wireFormatEncoder.encodeObjectEnd()
        }
    }

    // Sub-values

    override fun visit(leaderEntityKeys1: EntityKeysModel<Aux>): TraversalAction {
        wireFormatEncoder.encodeObjectStart(leaderEntityKeys1.meta?.let { getMetaName(it) })
        return TraversalAction.CONTINUE
    }

    override fun leave(leaderEntityKeys1: EntityKeysModel<Aux>) {
        wireFormatEncoder.encodeObjectEnd()
    }
}
