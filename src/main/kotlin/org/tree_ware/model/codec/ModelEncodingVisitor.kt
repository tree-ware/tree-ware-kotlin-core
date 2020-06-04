package org.tree_ware.model.codec

import org.tree_ware.common.codec.WireFormatEncoder
import org.tree_ware.model.codec.aux_encoder.AuxEncoder
import org.tree_ware.model.codec.aux_encoder.ErrorAuxEncoder
import org.tree_ware.model.core.*
import org.tree_ware.model.visitor.AbstractModelVisitor
import org.tree_ware.schema.core.*

const val VALUE_KEY = "value"

class ModelEncodingVisitor<Aux>(
    private val wireFormatEncoder: WireFormatEncoder
) : AbstractModelVisitor<Aux, SchemaTraversalAction>(SchemaTraversalAction.CONTINUE) {
    var auxEncoder: AuxEncoder? = null
    var encodingPathKeys = false

    override fun visit(model: Model<Aux>): SchemaTraversalAction {
        wireFormatEncoder.encodeObjectStart(null)
        wireFormatEncoder.encodeObjectStart(model.type.name)
        auxEncoder = when (model.type) {
            ModelType.data -> null
            ModelType.error -> ErrorAuxEncoder()
        }
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(model: Model<Aux>) {
        auxEncoder = null
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

    override fun visit(field: AssociationFieldModel<Aux>): SchemaTraversalAction {
        // NOTE: null values are encoded here. non-null values are encoded when the value is visited
        // in visit(value: AssociationValueModel, fieldSchema: AssociationFieldSchema).
        if (field.value == null) {
            wireFormatEncoder.encodeNullField(field.schema.name)
            return SchemaTraversalAction.ABORT_SUB_TREE
        }
        return SchemaTraversalAction.CONTINUE
    }

    override fun visit(field: ListFieldModel<Aux>): SchemaTraversalAction {
        val fieldName = if (auxEncoder != null && !encodingPathKeys) {
            wireFormatEncoder.encodeObjectStart(field.schema.name)
            auxEncoder?.also { it.encode(field.aux, wireFormatEncoder) }
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

    override fun visit(value: Any?, fieldSchema: PrimitiveFieldSchema): SchemaTraversalAction {
        val fieldName = if (auxEncoder != null && !encodingPathKeys) {
            wireFormatEncoder.encodeObjectStart(fieldSchema.name)
            // TODO(deepak-nulu): encode aux once primitive values are enhanced to store aux
            VALUE_KEY
        } else fieldSchema.name
        if (value == null) {
            wireFormatEncoder.encodeNullField(fieldName)
            return SchemaTraversalAction.CONTINUE
        }
        when (fieldSchema.primitive) {
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

    override fun leave(value: Any?, fieldSchema: PrimitiveFieldSchema) {
        if (auxEncoder != null && !encodingPathKeys) wireFormatEncoder.encodeObjectEnd()
    }


    override fun visit(value: EnumerationValueSchema?, fieldSchema: EnumerationFieldSchema): SchemaTraversalAction {
        val fieldName = if (auxEncoder != null && !encodingPathKeys) {
            wireFormatEncoder.encodeObjectStart(fieldSchema.name)
            // TODO(deepak-nulu): encode aux once enumeration values are enhanced to store aux
            VALUE_KEY
        } else fieldSchema.name
        if (value == null) wireFormatEncoder.encodeNullField(fieldName)
        else wireFormatEncoder.encodeStringField(fieldName, value.name)
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(value: EnumerationValueSchema?, fieldSchema: EnumerationFieldSchema) {
        if (auxEncoder != null && !encodingPathKeys) wireFormatEncoder.encodeObjectEnd()
    }

    override fun visit(value: AssociationValueModel<Aux>, fieldSchema: AssociationFieldSchema): SchemaTraversalAction {
        val fieldName = if (auxEncoder != null && !encodingPathKeys) {
            wireFormatEncoder.encodeObjectStart(fieldSchema.name)
            auxEncoder?.also { it.encode(value.aux, wireFormatEncoder) }
            VALUE_KEY
        } else fieldSchema.name
        wireFormatEncoder.encodeObjectStart(fieldName)
        wireFormatEncoder.encodeListStart("path_keys")
        encodingPathKeys = true
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(value: AssociationValueModel<Aux>, fieldSchema: AssociationFieldSchema) {
        encodingPathKeys = false
        wireFormatEncoder.encodeListEnd()
        wireFormatEncoder.encodeObjectEnd()
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
