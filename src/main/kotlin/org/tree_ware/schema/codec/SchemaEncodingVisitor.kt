package org.tree_ware.schema.codec

import org.tree_ware.common.codec.WireFormatEncoder
import org.tree_ware.schema.core.*

/** A visitor for encoding a schema. */
class SchemaEncodingVisitor(private val wireFormatEncoder: WireFormatEncoder) : BracketedVisitor,
    SchemaVisitor {
    // BracketedVisitor methods

    override fun objectStart(name: String) {
        wireFormatEncoder.encodeObjectStart(name)
    }

    override fun objectEnd() {
        wireFormatEncoder.encodeObjectEnd()
    }

    override fun listStart(name: String) {
        wireFormatEncoder.encodeListStart(name)
    }

    override fun listEnd() {
        wireFormatEncoder.encodeListEnd()
    }

    // SchemaVisitor methods for user-defined types

    override fun visit(element: ElementSchema): Boolean {
        return true
    }

    override fun visit(namedElement: NamedElementSchema): Boolean {
        wireFormatEncoder.encodeStringField("name", namedElement.name)
        namedElement.info?.also { wireFormatEncoder.encodeStringField("info", it) }
        return true
    }

    override fun visit(schema: Schema): Boolean {
        return true
    }

    override fun visit(pkg: PackageSchema): Boolean {
        return true
    }

    override fun visit(root: RootSchema): Boolean {
        wireFormatEncoder.encodeObjectStart("type")
        wireFormatEncoder.encodeStringField("package", root.packageName)
        wireFormatEncoder.encodeStringField("entity", root.entityName)
        wireFormatEncoder.encodeObjectEnd()
        return true
    }

    override fun visit(alias: AliasSchema): Boolean {
        return true
    }

    override fun visit(enumeration: EnumerationSchema): Boolean {
        return true
    }

    override fun visit(enumerationValue: EnumerationValueSchema): Boolean {
        return true
    }

    override fun visit(entity: EntitySchema): Boolean {
        return true
    }

    // SchemaVisitor methods for fields

    override fun visit(field: FieldSchema): Boolean {
        if (field.isKey) wireFormatEncoder.encodeBooleanField("is_key", field.isKey)
        encodeMultiplicity(field.multiplicity)
        return true
    }

    override fun visit(primitiveField: PrimitiveFieldSchema): Boolean {
        return true
    }

    override fun visit(aliasField: AliasFieldSchema): Boolean {
        wireFormatEncoder.encodeObjectStart("type")
        wireFormatEncoder.encodeStringField("package", aliasField.packageName)
        wireFormatEncoder.encodeStringField("alias", aliasField.aliasName)
        wireFormatEncoder.encodeObjectEnd()
        return true
    }

    override fun visit(enumerationField: EnumerationFieldSchema): Boolean {
        wireFormatEncoder.encodeObjectStart("type")
        wireFormatEncoder.encodeStringField("package", enumerationField.packageName)
        wireFormatEncoder.encodeStringField("enumeration", enumerationField.enumerationName)
        wireFormatEncoder.encodeObjectEnd()
        return true
    }

    override fun visit(associationField: AssociationFieldSchema): Boolean {
        wireFormatEncoder.encodeObjectStart("type")
        wireFormatEncoder.encodeListStart("entity_path")
        associationField.entityPath.forEach {
            wireFormatEncoder.encodeStringField("entity_path_element", it)
        }
        wireFormatEncoder.encodeListEnd()
        wireFormatEncoder.encodeObjectEnd()
        return true
    }

    override fun visit(compositionField: CompositionFieldSchema): Boolean {
        wireFormatEncoder.encodeObjectStart("type")
        wireFormatEncoder.encodeStringField("package", compositionField.packageName)
        wireFormatEncoder.encodeStringField("entity", compositionField.entityName)
        wireFormatEncoder.encodeObjectEnd()
        return true
    }

    // SchemaVisitor methods for predefined primitives

    override fun visit(boolean: BooleanSchema): Boolean {
        wireFormatEncoder.encodeStringField("type", "boolean")
        return true
    }

    override fun visit(byte: ByteSchema): Boolean {
        wireFormatEncoder.encodeStringField("type", "byte")
        byte.constraints?.let { encodeNumericConstraints(it) }
        return true
    }

    override fun visit(short: ShortSchema): Boolean {
        wireFormatEncoder.encodeStringField("type", "short")
        short.constraints?.let { encodeNumericConstraints(it) }
        return true
    }

    override fun visit(int: IntSchema): Boolean {
        wireFormatEncoder.encodeStringField("type", "int")
        int.constraints?.let { encodeNumericConstraints(it) }
        return true
    }

    override fun visit(long: LongSchema): Boolean {
        wireFormatEncoder.encodeStringField("type", "long")
        long.constraints?.let { encodeNumericConstraints(it) }
        return true
    }

    override fun visit(float: FloatSchema): Boolean {
        wireFormatEncoder.encodeStringField("type", "float")
        float.constraints?.let { encodeNumericConstraints(it) }
        return true
    }

    override fun visit(double: DoubleSchema): Boolean {
        wireFormatEncoder.encodeStringField("type", "double")
        double.constraints?.let { encodeNumericConstraints(it) }
        return true
    }

    override fun visit(string: StringSchema): Boolean {
        wireFormatEncoder.encodeStringField("type", "string")
        string.constraints?.let { encodeStringConstraints(it) }
        return true
    }

    override fun visit(password1Way: Password1WaySchema): Boolean {
        wireFormatEncoder.encodeStringField("type", "password_1_way")
        password1Way.constraints?.let { encodeStringConstraints(it) }
        return true
    }

    override fun visit(password2Way: Password2WaySchema): Boolean {
        wireFormatEncoder.encodeStringField("type", "password_2_way")
        password2Way.constraints?.let { encodeStringConstraints(it) }
        return true
    }

    override fun visit(uuid: UuidSchema): Boolean {
        wireFormatEncoder.encodeStringField("type", "uuid")
        return true
    }

    override fun visit(blob: BlobSchema): Boolean {
        wireFormatEncoder.encodeStringField("type", "blob")
        return true
    }

    override fun visit(timestamp: TimestampSchema): Boolean {
        wireFormatEncoder.encodeStringField("type", "timestamp")
        return true
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