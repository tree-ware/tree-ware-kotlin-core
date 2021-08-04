package org.treeWare.model.codec.encoder

import org.treeWare.common.codec.WireFormatEncoder
import org.treeWare.common.traversal.TraversalAction
import org.treeWare.model.core.*
import org.treeWare.model.operator.Leader1Follower0ModelVisitor
import org.treeWare.model.operator.forEach
import org.treeWare.schema.core.*

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
        auxEncoder?.also { it.encode(leaderRoot1.schema.name, leaderRoot1.aux, wireFormatEncoder) }
        wireFormatEncoder.encodeObjectStart(leaderRoot1.schema.name)
        return TraversalAction.CONTINUE
    }

    override fun leave(leaderRoot1: RootModel<Aux>) {
        wireFormatEncoder.encodeObjectEnd()
    }

    override fun visit(leaderEntity1: EntityModel<Aux>): TraversalAction {
        wireFormatEncoder.encodeObjectStart(leaderEntity1.parent.schema.name)
        val isListElement = leaderEntity1.parent.schema.multiplicity.isList()
        if (isListElement && auxEncoder != null) auxEncoder.encode(null, leaderEntity1.aux, wireFormatEncoder)
        return TraversalAction.CONTINUE
    }

    override fun leave(leaderEntity1: EntityModel<Aux>) {
        wireFormatEncoder.encodeObjectEnd()
    }

    // Fields

    override fun visit(leaderField1: SingleFieldModel<Aux>): TraversalAction {
        val fieldName = leaderField1.schema.name
        auxEncoder?.also { it.encode(fieldName, leaderField1.aux, wireFormatEncoder) }
        return TraversalAction.CONTINUE
    }

    override fun leave(leaderField1: SingleFieldModel<Aux>) {}

    override fun visit(leaderField1: ListFieldModel<Aux>): TraversalAction {
        val fieldName = leaderField1.schema.name
        auxEncoder?.also { it.encode(fieldName, leaderField1.aux, wireFormatEncoder) }
        wireFormatEncoder.encodeListStart(fieldName)
        return TraversalAction.CONTINUE
    }

    override fun leave(leaderField1: ListFieldModel<Aux>) {
        wireFormatEncoder.encodeListEnd()
    }

    // Scalar fields

    override fun visit(leaderValue1: PrimitiveModel<Aux>): TraversalAction {
        val isListElement = leaderValue1.schema.multiplicity.isList()
        val fieldName = if (isListElement) VALUE_KEY else leaderValue1.schema.name
        val auxFieldName = if (isListElement) null else leaderValue1.schema.name
        if (isListElement) wireFormatEncoder.encodeObjectStart(null)
        auxEncoder?.also { it.encode(auxFieldName, leaderValue1.aux, wireFormatEncoder) }
        val value = leaderValue1.value
        if (value == null) {
            wireFormatEncoder.encodeNullField(fieldName)
            return TraversalAction.CONTINUE
        }
        when (leaderValue1.schema.primitive) {
            is BooleanSchema -> wireFormatEncoder.encodeBooleanField(fieldName, value as Boolean)
            is ByteSchema -> wireFormatEncoder.encodeNumericField(fieldName, value as Byte)
            is ShortSchema -> wireFormatEncoder.encodeNumericField(fieldName, value as Short)
            is IntSchema -> wireFormatEncoder.encodeNumericField(fieldName, value as Int)
            is FloatSchema -> wireFormatEncoder.encodeNumericField(fieldName, value as Float)
            is DoubleSchema -> wireFormatEncoder.encodeNumericField(fieldName, value as Double)
            // Integers in JavaScript are limited to 53 bits. So 64-bit values (LongSchema, TimestampSchema)
            // are encoded as strings.
            else -> wireFormatEncoder.encodeStringField(fieldName, value.toString())
            // TODO(deepak-nulu): special handling for Password1WaySchema, Password2WaySchema, BlobSchema
        }
        return TraversalAction.CONTINUE
    }

    override fun leave(leaderValue1: PrimitiveModel<Aux>) {
        val isListElement = leaderValue1.schema.multiplicity.isList()
        if (isListElement) wireFormatEncoder.encodeObjectEnd()
    }

    override fun visit(leaderValue1: AliasModel<Aux>): TraversalAction = TraversalAction.CONTINUE
    override fun leave(leaderValue1: AliasModel<Aux>) {}

    override fun visit(leaderValue1: EnumerationModel<Aux>): TraversalAction {
        val isListElement = leaderValue1.schema.multiplicity.isList()
        val fieldName = if (isListElement) VALUE_KEY else leaderValue1.schema.name
        val auxFieldName = if (isListElement) null else leaderValue1.schema.name
        if (isListElement) wireFormatEncoder.encodeObjectStart(null)
        auxEncoder?.also { it.encode(auxFieldName, leaderValue1.aux, wireFormatEncoder) }
        val value = leaderValue1.value
        if (value == null) wireFormatEncoder.encodeNullField(fieldName)
        else wireFormatEncoder.encodeStringField(fieldName, value.name)
        return TraversalAction.CONTINUE
    }

    override fun leave(leaderValue1: EnumerationModel<Aux>) {
        val isListElement = leaderValue1.schema.multiplicity.isList()
        if (isListElement) wireFormatEncoder.encodeObjectEnd()
    }

    override fun visit(leaderValue1: AssociationModel<Aux>): TraversalAction {
        val isListElement = leaderValue1.schema.multiplicity.isList()
        val fieldName = leaderValue1.schema.name
        val auxFieldName = if (isListElement) null else leaderValue1.schema.name
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
        wireFormatEncoder.encodeObjectStart(leaderEntityKeys1.schema.name)
        return TraversalAction.CONTINUE
    }

    override fun leave(leaderEntityKeys1: EntityKeysModel<Aux>) {
        wireFormatEncoder.encodeObjectEnd()
    }
}
