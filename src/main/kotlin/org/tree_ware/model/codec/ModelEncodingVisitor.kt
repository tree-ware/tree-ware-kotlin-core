package org.tree_ware.model.codec

import org.tree_ware.core.codec.common.WireFormatEncoder
import org.tree_ware.core.schema.*
import org.tree_ware.model.core.*
import org.tree_ware.model.visitor.AbstractModelVisitor

class ModelEncodingVisitor(private val wireFormatEncoder: WireFormatEncoder) : AbstractModelVisitor() {
    override fun visit(model: Model): Boolean {
        wireFormatEncoder.encodeObjectStart(null)
        wireFormatEncoder.encodeObjectStart(model.type.name)
        return true
    }

    override fun leave(model: Model) {
        wireFormatEncoder.encodeObjectEnd()
        wireFormatEncoder.encodeObjectEnd()
    }

    override fun visit(root: RootModel): Boolean {
        wireFormatEncoder.encodeObjectStart(root.schema.name)
        return true
    }

    override fun leave(root: RootModel) {
        wireFormatEncoder.encodeObjectEnd()
    }

    override fun visit(entity: EntityModel): Boolean {
        wireFormatEncoder.encodeObjectStart(entity.parent.schema.name)
        return true
    }

    override fun leave(entity: EntityModel) {
        wireFormatEncoder.encodeObjectEnd()
    }

    override fun visit(field: ListFieldModel): Boolean {
        wireFormatEncoder.encodeListStart(field.schema.name)
        return true
    }

    override fun leave(field: ListFieldModel) {
        wireFormatEncoder.encodeListEnd()
    }

    override fun visit(value: Any, fieldSchema: PrimitiveFieldSchema): Boolean {
        val fieldName = fieldSchema.name
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
        return true
    }

    override fun visit(value: EnumerationValueSchema, fieldSchema: EnumerationFieldSchema): Boolean {
        wireFormatEncoder.encodeStringField(fieldSchema.name, value.name)
        return true
    }

    override fun visit(value: AssociationValueModel, fieldSchema: AssociationFieldSchema): Boolean {
        wireFormatEncoder.encodeObjectStart(fieldSchema.name)
        wireFormatEncoder.encodeListStart("path_keys")
        return true
    }

    override fun leave(value: AssociationValueModel, fieldSchema: AssociationFieldSchema) {
        wireFormatEncoder.encodeListEnd()
        wireFormatEncoder.encodeObjectEnd()
    }

    override fun visit(entityKeys: EntityKeysModel): Boolean {
        wireFormatEncoder.encodeObjectStart(entityKeys.schema.name)
        return true
    }

    override fun leave(entityKeys: EntityKeysModel) {
        wireFormatEncoder.encodeObjectEnd()
    }
}
