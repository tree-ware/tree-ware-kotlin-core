package org.treeWare.model.codec.encoder

import org.treeWare.common.codec.WireFormatEncoder
import org.treeWare.common.traversal.TraversalAction
import org.treeWare.model.core.*
import org.treeWare.model.operator.Leader1Follower0ModelVisitor
import org.treeWare.model.operator.forEach

const val VALUE_KEY = "value"

class ModelEncodingVisitor<Aux>(
    private val wireFormatEncoder: WireFormatEncoder,
    private val auxEncoder: AuxEncoder?
) : Leader1Follower0ModelVisitor<Aux, TraversalAction> {
    private var encodingPathKeys = false

    override fun visit(leaderModel1: Model<Aux>): TraversalAction {
        wireFormatEncoder.encodeObjectStart(null)
        wireFormatEncoder.encodeObjectStart(leaderModel1.type)
        return TraversalAction.CONTINUE
    }

    override fun leave(leaderModel1: Model<Aux>) {
        wireFormatEncoder.encodeObjectEnd()
        wireFormatEncoder.encodeObjectEnd()
    }

    override fun visit(leaderRoot1: RootModel<Aux>): TraversalAction {
        val name = leaderRoot1.meta?.let { getMetaName(it) }
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
        val isListElement = leaderEntity1.parent.meta?.let { isFieldMetaList(it) } ?: false
        if (isListElement && auxEncoder != null) auxEncoder.encode(null, leaderEntity1.aux, wireFormatEncoder)
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

    // Scalar fields

    override fun visit(leaderValue1: PrimitiveModel<Aux>): TraversalAction {
        val isListElement = leaderValue1.parent.meta?.let { isFieldMetaList(it) } ?: false
        val fieldName = if (isListElement) VALUE_KEY else leaderValue1.parent.meta?.let { getMetaName(it) } ?: ""
        val auxFieldName = if (isListElement) null else leaderValue1.parent.meta?.let { getMetaName(it) } ?: ""
        if (isListElement) wireFormatEncoder.encodeObjectStart(null)
        auxEncoder?.also { it.encode(auxFieldName, leaderValue1.aux, wireFormatEncoder) }
        val value = leaderValue1.value
        if (value == null) {
            wireFormatEncoder.encodeNullField(fieldName)
            return TraversalAction.CONTINUE
        }
        // TODO(self-hosting): replace string literals with an enum defined in the meta-meta-model.
        when (leaderValue1.parent.meta?.let { getFieldMetaType(it) }) {
            "boolean" -> wireFormatEncoder.encodeBooleanField(fieldName, value as Boolean)
            "byte" -> wireFormatEncoder.encodeNumericField(fieldName, value as Byte)
            "short" -> wireFormatEncoder.encodeNumericField(fieldName, value as Short)
            "int" -> wireFormatEncoder.encodeNumericField(fieldName, value as Int)
            "float" -> wireFormatEncoder.encodeNumericField(fieldName, value as Float)
            "double" -> wireFormatEncoder.encodeNumericField(fieldName, value as Double)
            // Integers in JavaScript are limited to 53 bits. So 64-bit values ("long", "timestamp")
            // are encoded as strings.
            else -> wireFormatEncoder.encodeStringField(fieldName, value.toString())
            // TODO(deepak-nulu): special handling for "password1way", "password2way", "blob"
        }
        return TraversalAction.CONTINUE
    }

    override fun leave(leaderValue1: PrimitiveModel<Aux>) {
        val isListElement = leaderValue1.parent.meta?.let { isFieldMetaList(it) } ?: false
        if (isListElement) wireFormatEncoder.encodeObjectEnd()
    }

    override fun visit(leaderValue1: AliasModel<Aux>): TraversalAction = TraversalAction.CONTINUE
    override fun leave(leaderValue1: AliasModel<Aux>) {}

    override fun visit(leaderValue1: EnumerationModel<Aux>): TraversalAction {
        val isListElement = leaderValue1.parent.meta?.let { isFieldMetaList(it) } ?: false
        val fieldName = if (isListElement) VALUE_KEY else leaderValue1.parent.meta?.let { getMetaName(it) } ?: ""
        val auxFieldName = if (isListElement) null else leaderValue1.parent.meta?.let { getMetaName(it) }
        if (isListElement) wireFormatEncoder.encodeObjectStart(null)
        auxEncoder?.also { it.encode(auxFieldName, leaderValue1.aux, wireFormatEncoder) }
        val value = leaderValue1.value
        if (value == null) wireFormatEncoder.encodeNullField(fieldName)
        else wireFormatEncoder.encodeStringField(fieldName, value.name)
        return TraversalAction.CONTINUE
    }

    override fun leave(leaderValue1: EnumerationModel<Aux>) {
        val isListElement = leaderValue1.parent.meta?.let { isFieldMetaList(it) } ?: false
        if (isListElement) wireFormatEncoder.encodeObjectEnd()
    }

    override fun visit(leaderValue1: AssociationModel<Aux>): TraversalAction {
        val isListElement = leaderValue1.parent.meta?.let { isFieldMetaList(it) } ?: false
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
