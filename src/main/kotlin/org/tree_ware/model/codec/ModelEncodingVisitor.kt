package org.tree_ware.model.codec

import org.tree_ware.common.codec.WireFormatEncoder
import org.tree_ware.model.core.*
import org.tree_ware.model.visitor.AbstractModelVisitor
import org.tree_ware.schema.core.*

class ModelEncodingVisitor(
    private val wireFormatEncoder: WireFormatEncoder
) : AbstractModelVisitor<SchemaTraversalAction>(SchemaTraversalAction.CONTINUE) {
    override fun visit(model: Model): SchemaTraversalAction {
        wireFormatEncoder.encodeObjectStart(null)
        wireFormatEncoder.encodeObjectStart(model.type.name)
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(model: Model) {
        wireFormatEncoder.encodeObjectEnd()
        wireFormatEncoder.encodeObjectEnd()
    }

    override fun visit(root: RootModel): SchemaTraversalAction {
        wireFormatEncoder.encodeObjectStart(root.schema.name)
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(root: RootModel) {
        wireFormatEncoder.encodeObjectEnd()
    }

    override fun visit(entity: EntityModel): SchemaTraversalAction {
        wireFormatEncoder.encodeObjectStart(entity.parent.schema.name)
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(entity: EntityModel) {
        wireFormatEncoder.encodeObjectEnd()
    }

    override fun visit(field: AssociationFieldModel): SchemaTraversalAction {
        // NOTE: null values are encoded here. non-null values are encoded when the value is visited
        // in visit(value: AssociationValueModel, fieldSchema: AssociationFieldSchema).
        if (field.value == null) {
            wireFormatEncoder.encodeNullField(field.schema.name)
            return SchemaTraversalAction.ABORT_SUB_TREE
        }
        return SchemaTraversalAction.CONTINUE
    }

    override fun visit(field: ListFieldModel): SchemaTraversalAction {
        wireFormatEncoder.encodeListStart(field.schema.name)
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(field: ListFieldModel) {
        wireFormatEncoder.encodeListEnd()
    }

    override fun visit(value: Any?, fieldSchema: PrimitiveFieldSchema): SchemaTraversalAction {
        val fieldName = fieldSchema.name
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

    override fun visit(value: EnumerationValueSchema?, fieldSchema: EnumerationFieldSchema): SchemaTraversalAction {
        if (value == null) wireFormatEncoder.encodeNullField(fieldSchema.name)
        else wireFormatEncoder.encodeStringField(fieldSchema.name, value.name)
        return SchemaTraversalAction.CONTINUE
    }

    override fun visit(value: AssociationValueModel, fieldSchema: AssociationFieldSchema): SchemaTraversalAction {
        wireFormatEncoder.encodeObjectStart(fieldSchema.name)
        wireFormatEncoder.encodeListStart("path_keys")
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(value: AssociationValueModel, fieldSchema: AssociationFieldSchema) {
        wireFormatEncoder.encodeListEnd()
        wireFormatEncoder.encodeObjectEnd()
    }

    override fun visit(entityKeys: EntityKeysModel): SchemaTraversalAction {
        wireFormatEncoder.encodeObjectStart(entityKeys.schema.name)
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(entityKeys: EntityKeysModel) {
        wireFormatEncoder.encodeObjectEnd()
    }
}
