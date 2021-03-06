package org.tree_ware.model.codec

import org.tree_ware.common.codec.WireFormatEncoder
import org.tree_ware.model.codec.aux_encoder.AuxEncoder
import org.tree_ware.model.core.*
import org.tree_ware.model.operator.forEach
import org.tree_ware.model.visitor.AbstractModelVisitor
import org.tree_ware.schema.core.*

const val VALUE_KEY = "value"

class ModelEncodingVisitor<Aux>(
    private val wireFormatEncoder: WireFormatEncoder,
    private val auxEncoder: AuxEncoder?
) : AbstractModelVisitor<Aux, SchemaTraversalAction>(SchemaTraversalAction.CONTINUE) {
    private var encodingPathKeys = false

    override fun visit(model: Model<Aux>): SchemaTraversalAction {
        wireFormatEncoder.encodeObjectStart(null)
        wireFormatEncoder.encodeObjectStart(model.type)
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(model: Model<Aux>) {
        wireFormatEncoder.encodeObjectEnd()
        wireFormatEncoder.encodeObjectEnd()
    }

    override fun visit(root: RootModel<Aux>): SchemaTraversalAction {
        wireFormatEncoder.encodeObjectStart(root.schema.name)
        auxEncoder?.also {
            it.encode(root.aux, wireFormatEncoder)
            wireFormatEncoder.encodeObjectStart(VALUE_KEY)
        }
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(root: RootModel<Aux>) {
        if (auxEncoder != null) {
            wireFormatEncoder.encodeObjectEnd()
        }
        wireFormatEncoder.encodeObjectEnd()
    }

    override fun visit(entity: EntityModel<Aux>): SchemaTraversalAction {
        wireFormatEncoder.encodeObjectStart(entity.parent.schema.name)
        auxEncoder?.also {
            it.encode(entity.aux, wireFormatEncoder)
            wireFormatEncoder.encodeObjectStart(VALUE_KEY)
        }
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(entity: EntityModel<Aux>) {
        if (auxEncoder != null) {
            wireFormatEncoder.encodeObjectEnd()
        }
        wireFormatEncoder.encodeObjectEnd()
    }

    override fun visit(field: PrimitiveFieldModel<Aux>): SchemaTraversalAction {
        val fieldName = if (auxEncoder != null && !encodingPathKeys) {
            wireFormatEncoder.encodeObjectStart(field.schema.name)
            auxEncoder.also { it.encode(field.aux, wireFormatEncoder) }
            VALUE_KEY
        } else field.schema.name
        val value = field.value
        if (value == null) {
            wireFormatEncoder.encodeNullField(fieldName)
            return SchemaTraversalAction.CONTINUE
        }
        when (field.schema.primitive) {
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
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(field: PrimitiveFieldModel<Aux>) {
        if (auxEncoder != null && !encodingPathKeys) wireFormatEncoder.encodeObjectEnd()
    }

    override fun visit(field: EnumerationFieldModel<Aux>): SchemaTraversalAction {
        val fieldName = if (auxEncoder != null && !encodingPathKeys) {
            wireFormatEncoder.encodeObjectStart(field.schema.name)
            auxEncoder.also { it.encode(field.aux, wireFormatEncoder) }
            VALUE_KEY
        } else field.schema.name
        val value = field.value
        if (value == null) wireFormatEncoder.encodeNullField(fieldName)
        else wireFormatEncoder.encodeStringField(fieldName, value.name)
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(field: EnumerationFieldModel<Aux>) {
        if (auxEncoder != null && !encodingPathKeys) wireFormatEncoder.encodeObjectEnd()
    }

    override fun visit(field: AssociationFieldModel<Aux>): SchemaTraversalAction {
        val fieldName = if (auxEncoder != null && !encodingPathKeys) {
            wireFormatEncoder.encodeObjectStart(field.schema.name)
            auxEncoder.also { it.encode(field.aux, wireFormatEncoder) }
            VALUE_KEY
        } else field.schema.name
        if (field.value.isEmpty()) {
            wireFormatEncoder.encodeNullField(fieldName)
            return SchemaTraversalAction.CONTINUE
        }
        wireFormatEncoder.encodeObjectStart(fieldName)
        wireFormatEncoder.encodeListStart("path_keys")
        encodingPathKeys = true
        field.value.forEach { entityKeys ->
            // Traverse entityKeys with this visitor to encode it.
            forEach(entityKeys, this)
        }
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(field: AssociationFieldModel<Aux>) {
        if (field.value.isNotEmpty()) {
            encodingPathKeys = false
            wireFormatEncoder.encodeListEnd()
            wireFormatEncoder.encodeObjectEnd()
        }
        if (auxEncoder != null && !encodingPathKeys) {
            wireFormatEncoder.encodeObjectEnd()
        }
    }

    override fun visit(field: ListFieldModel<Aux>): SchemaTraversalAction {
        val fieldName = if (auxEncoder != null && !encodingPathKeys) {
            wireFormatEncoder.encodeObjectStart(field.schema.name)
            auxEncoder.also { it.encode(field.aux, wireFormatEncoder) }
            VALUE_KEY
        } else field.schema.name
        wireFormatEncoder.encodeListStart(fieldName)
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(field: ListFieldModel<Aux>) {
        wireFormatEncoder.encodeListEnd()
        if (auxEncoder != null && !encodingPathKeys) {
            wireFormatEncoder.encodeObjectEnd()
        }
    }

    override fun visit(entityKeys: EntityKeysModel<Aux>): SchemaTraversalAction {
        wireFormatEncoder.encodeObjectStart(entityKeys.schema.name)
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(entityKeys: EntityKeysModel<Aux>) {
        wireFormatEncoder.encodeObjectEnd()
    }
}
