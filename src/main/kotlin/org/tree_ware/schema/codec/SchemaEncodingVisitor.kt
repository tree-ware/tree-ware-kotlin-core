package org.tree_ware.schema.codec

import org.tree_ware.common.codec.WireFormatEncoder
import org.tree_ware.common.traversal.TraversalAction
import org.tree_ware.schema.core.*
import org.tree_ware.schema.visitor.AbstractSchemaVisitor

/** A visitor for encoding a schema. */
class SchemaEncodingVisitor(
    private val wireFormatEncoder: WireFormatEncoder
) : AbstractSchemaVisitor<TraversalAction>(TraversalAction.CONTINUE) {
    // SchemaVisitor methods
    override fun visit(namedElement: NamedElementSchema): TraversalAction {
        wireFormatEncoder.encodeObjectStart(namedElement.id)
        wireFormatEncoder.encodeStringField("name", namedElement.name)
        namedElement.info?.also { wireFormatEncoder.encodeStringField("info", it) }
        return TraversalAction.CONTINUE
    }

    override fun leave(namedElement: NamedElementSchema) {
        wireFormatEncoder.encodeObjectEnd()
    }

    // SchemaVisitor methods for top level

    override fun visit(schema: Schema): TraversalAction {
        wireFormatEncoder.encodeObjectStart(schema.id)
        return TraversalAction.CONTINUE
    }

    override fun leave(schema: Schema) {
        wireFormatEncoder.encodeObjectEnd()
    }

    override fun visit(root: RootSchema): TraversalAction {
        wireFormatEncoder.encodeObjectStart("type")
        wireFormatEncoder.encodeStringField("package", root.packageName)
        wireFormatEncoder.encodeStringField("entity", root.entityName)
        wireFormatEncoder.encodeObjectEnd()
        return TraversalAction.CONTINUE
    }

    // SchemaVisitor methods for fields

    override fun visit(field: FieldSchema): TraversalAction {
        if (field.isKey) wireFormatEncoder.encodeBooleanField("is_key", field.isKey)
        encodeMultiplicity(field.multiplicity)
        return TraversalAction.CONTINUE
    }

    override fun visit(aliasField: AliasFieldSchema): TraversalAction {
        wireFormatEncoder.encodeObjectStart("type")
        wireFormatEncoder.encodeStringField("package", aliasField.packageName)
        wireFormatEncoder.encodeStringField("alias", aliasField.aliasName)
        wireFormatEncoder.encodeObjectEnd()
        return TraversalAction.CONTINUE
    }

    override fun visit(enumerationField: EnumerationFieldSchema): TraversalAction {
        wireFormatEncoder.encodeObjectStart("type")
        wireFormatEncoder.encodeStringField("package", enumerationField.packageName)
        wireFormatEncoder.encodeStringField("enumeration", enumerationField.enumerationName)
        wireFormatEncoder.encodeObjectEnd()
        return TraversalAction.CONTINUE
    }

    override fun visit(associationField: AssociationFieldSchema): TraversalAction {
        wireFormatEncoder.encodeObjectStart("type")
        wireFormatEncoder.encodeListStart("entity_path")
        associationField.entityPath.forEach {
            wireFormatEncoder.encodeStringField("entity_path_element", it)
        }
        wireFormatEncoder.encodeListEnd()
        wireFormatEncoder.encodeObjectEnd()
        return TraversalAction.CONTINUE
    }

    override fun visit(compositionField: CompositionFieldSchema): TraversalAction {
        wireFormatEncoder.encodeObjectStart("type")
        wireFormatEncoder.encodeStringField("package", compositionField.packageName)
        wireFormatEncoder.encodeStringField("entity", compositionField.entityName)
        wireFormatEncoder.encodeObjectEnd()
        return TraversalAction.CONTINUE
    }

    // SchemaVisitor methods for primitives

    override fun visit(boolean: BooleanSchema): TraversalAction {
        wireFormatEncoder.encodeStringField("type", "boolean")
        return TraversalAction.CONTINUE
    }

    override fun visit(byte: ByteSchema): TraversalAction {
        wireFormatEncoder.encodeStringField("type", "byte")
        byte.constraints?.let { encodeNumericConstraints(it) }
        return TraversalAction.CONTINUE
    }

    override fun visit(short: ShortSchema): TraversalAction {
        wireFormatEncoder.encodeStringField("type", "short")
        short.constraints?.let { encodeNumericConstraints(it) }
        return TraversalAction.CONTINUE
    }

    override fun visit(int: IntSchema): TraversalAction {
        wireFormatEncoder.encodeStringField("type", "int")
        int.constraints?.let { encodeNumericConstraints(it) }
        return TraversalAction.CONTINUE
    }

    override fun visit(long: LongSchema): TraversalAction {
        wireFormatEncoder.encodeStringField("type", "long")
        long.constraints?.let { encodeNumericConstraints(it) }
        return TraversalAction.CONTINUE
    }

    override fun visit(float: FloatSchema): TraversalAction {
        wireFormatEncoder.encodeStringField("type", "float")
        float.constraints?.let { encodeNumericConstraints(it) }
        return TraversalAction.CONTINUE
    }

    override fun visit(double: DoubleSchema): TraversalAction {
        wireFormatEncoder.encodeStringField("type", "double")
        double.constraints?.let { encodeNumericConstraints(it) }
        return TraversalAction.CONTINUE
    }

    override fun visit(string: StringSchema): TraversalAction {
        wireFormatEncoder.encodeStringField("type", "string")
        string.constraints?.let { encodeStringConstraints(it) }
        return TraversalAction.CONTINUE
    }

    override fun visit(password1Way: Password1WaySchema): TraversalAction {
        wireFormatEncoder.encodeStringField("type", "password_1_way")
        password1Way.constraints?.let { encodeStringConstraints(it) }
        return TraversalAction.CONTINUE
    }

    override fun visit(password2Way: Password2WaySchema): TraversalAction {
        wireFormatEncoder.encodeStringField("type", "password_2_way")
        password2Way.constraints?.let { encodeStringConstraints(it) }
        return TraversalAction.CONTINUE
    }

    override fun visit(uuid: UuidSchema): TraversalAction {
        wireFormatEncoder.encodeStringField("type", "uuid")
        return TraversalAction.CONTINUE
    }

    override fun visit(blob: BlobSchema): TraversalAction {
        wireFormatEncoder.encodeStringField("type", "blob")
        return TraversalAction.CONTINUE
    }

    override fun visit(timestamp: TimestampSchema): TraversalAction {
        wireFormatEncoder.encodeStringField("type", "timestamp")
        return TraversalAction.CONTINUE
    }

    // Schema visitor methods for meta

    override fun visitList(name: String) {
        wireFormatEncoder.encodeListStart(name)
    }

    override fun leaveList(name: String) {
        wireFormatEncoder.encodeListEnd()
    }

    // Helper methods

    private fun encodeMultiplicity(multiplicity: Multiplicity) {
        if (multiplicity.isRequired()) return
        wireFormatEncoder.encodeObjectStart("multiplicity")
        wireFormatEncoder.encodeNumericField("min", multiplicity.min)
        wireFormatEncoder.encodeNumericField("max", multiplicity.max)
        wireFormatEncoder.encodeObjectEnd()
    }

    private fun <T : Number> encodeNumericConstraints(constraints: NumericConstraints<T>) = try {
        wireFormatEncoder.encodeObjectStart("constraints")
        constraints.minBound?.let { encodeNumericBound("min_bound", it) }
        constraints.maxBound?.let { encodeNumericBound("max_bound", it) }
    } finally {
        wireFormatEncoder.encodeObjectEnd()
    }

    private fun <T : Number> encodeNumericBound(name: String, bound: NumericBound<T>) = try {
        wireFormatEncoder.encodeObjectStart(name)
        wireFormatEncoder.encodeNumericField("value", bound.value)
        wireFormatEncoder.encodeBooleanField("inclusive", bound.inclusive)
    } finally {
        wireFormatEncoder.encodeObjectEnd()
    }

    private fun encodeStringConstraints(constraints: StringConstraints) = try {
        wireFormatEncoder.encodeObjectStart("constraints")
        constraints.minLength?.let { wireFormatEncoder.encodeNumericField("min_length", it) }
        constraints.maxLength?.let { wireFormatEncoder.encodeNumericField("max_length", it) }
        constraints.regex?.let { wireFormatEncoder.encodeStringField("regex", it) }
    } finally {
        wireFormatEncoder.encodeObjectEnd()
    }
}
