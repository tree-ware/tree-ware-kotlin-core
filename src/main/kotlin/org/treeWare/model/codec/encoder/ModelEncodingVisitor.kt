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

    // Scalar fields

    override fun visit(leaderField1: PrimitiveFieldModel<Aux>): TraversalAction {
        val isListElement = leaderField1.schema.multiplicity.isList()
        val fieldName = if (isListElement) VALUE_KEY else leaderField1.schema.name
        val auxFieldName = if (isListElement) null else leaderField1.schema.name
        if (isListElement) wireFormatEncoder.encodeObjectStart(null)
        auxEncoder?.also { it.encode(auxFieldName, leaderField1.aux, wireFormatEncoder) }
        val value = leaderField1.value
        if (value == null) {
            wireFormatEncoder.encodeNullField(fieldName)
            return TraversalAction.CONTINUE
        }
        when (leaderField1.schema.primitive) {
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

    override fun leave(leaderField1: PrimitiveFieldModel<Aux>) {
        val isListElement = leaderField1.schema.multiplicity.isList()
        if (isListElement) wireFormatEncoder.encodeObjectEnd()
    }

    override fun visit(leaderField1: AliasFieldModel<Aux>): TraversalAction = TraversalAction.CONTINUE
    override fun leave(leaderField1: AliasFieldModel<Aux>) {}

    override fun visit(leaderField1: EnumerationFieldModel<Aux>): TraversalAction {
        val isListElement = leaderField1.schema.multiplicity.isList()
        val fieldName = if (isListElement) VALUE_KEY else leaderField1.schema.name
        val auxFieldName = if (isListElement) null else leaderField1.schema.name
        if (isListElement) wireFormatEncoder.encodeObjectStart(null)
        auxEncoder?.also { it.encode(auxFieldName, leaderField1.aux, wireFormatEncoder) }
        val value = leaderField1.value
        if (value == null) wireFormatEncoder.encodeNullField(fieldName)
        else wireFormatEncoder.encodeStringField(fieldName, value.name)
        return TraversalAction.CONTINUE
    }

    override fun leave(leaderField1: EnumerationFieldModel<Aux>) {
        val isListElement = leaderField1.schema.multiplicity.isList()
        if (isListElement) wireFormatEncoder.encodeObjectEnd()
    }

    override fun visit(leaderField1: AssociationFieldModel<Aux>): TraversalAction {
        val isListElement = leaderField1.schema.multiplicity.isList()
        val fieldName = leaderField1.schema.name
        val auxFieldName = if (isListElement) null else leaderField1.schema.name
        if (!isListElement) auxEncoder?.also { it.encode(auxFieldName, leaderField1.aux, wireFormatEncoder) }
        if (leaderField1.value.isEmpty()) {
            wireFormatEncoder.encodeNullField(fieldName)
            return TraversalAction.CONTINUE
        }
        wireFormatEncoder.encodeObjectStart(fieldName)
        if (isListElement) auxEncoder?.also { it.encode(auxFieldName, leaderField1.aux, wireFormatEncoder) }
        wireFormatEncoder.encodeListStart("path_keys")
        encodingPathKeys = true
        leaderField1.value.forEach { entityKeys ->
            // Traverse entityKeys with this visitor to encode it.
            forEach(entityKeys, this)
        }
        return TraversalAction.CONTINUE
    }

    override fun leave(leaderField1: AssociationFieldModel<Aux>) {
        if (leaderField1.value.isNotEmpty()) {
            encodingPathKeys = false
            wireFormatEncoder.encodeListEnd()
            wireFormatEncoder.encodeObjectEnd()
        }
    }

    override fun visit(leaderField1: CompositionFieldModel<Aux>): TraversalAction {
        auxEncoder?.also { it.encode(leaderField1.schema.name, leaderField1.aux, wireFormatEncoder) }
        return TraversalAction.CONTINUE
    }

    override fun leave(leaderField1: CompositionFieldModel<Aux>) {}

    // List fields

    override fun visit(leaderField1: PrimitiveListFieldModel<Aux>): TraversalAction = visitListField(leaderField1)
    override fun leave(leaderField1: PrimitiveListFieldModel<Aux>) = leaveListField()

    override fun visit(leaderField1: AliasListFieldModel<Aux>): TraversalAction = visitListField(leaderField1)
    override fun leave(leaderField1: AliasListFieldModel<Aux>) = leaveListField()

    override fun visit(leaderField1: EnumerationListFieldModel<Aux>): TraversalAction = visitListField(leaderField1)
    override fun leave(leaderField1: EnumerationListFieldModel<Aux>) = leaveListField()

    override fun visit(leaderField1: AssociationListFieldModel<Aux>): TraversalAction = visitListField(leaderField1)
    override fun leave(leaderField1: AssociationListFieldModel<Aux>) = leaveListField()

    override fun visit(leaderField1: CompositionListFieldModel<Aux>): TraversalAction = visitListField(leaderField1)
    override fun leave(leaderField1: CompositionListFieldModel<Aux>) = leaveListField()

    private fun visitListField(field: ListFieldModel<Aux>): TraversalAction {
        val fieldName = field.schema.name
        auxEncoder?.also { it.encode(fieldName, field.aux, wireFormatEncoder) }
        wireFormatEncoder.encodeListStart(fieldName)
        return TraversalAction.CONTINUE
    }

    private fun leaveListField() {
        wireFormatEncoder.encodeListEnd()
    }

    // Field values

    override fun visit(leaderEntityKeys1: EntityKeysModel<Aux>): TraversalAction {
        wireFormatEncoder.encodeObjectStart(leaderEntityKeys1.schema.name)
        return TraversalAction.CONTINUE
    }

    override fun leave(leaderEntityKeys1: EntityKeysModel<Aux>) {
        wireFormatEncoder.encodeObjectEnd()
    }
}
