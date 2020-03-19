package org.tree_ware.core.codec.common

import org.tree_ware.core.schema.*

/** A visitor for encoding a schema. */
class SchemaEncodingVisitor(private val wireFormatEncoder: WireFormatEncoder) : BracketedVisitor, SchemaVisitor {
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
        return true
    }

    override fun visit(schema: Schema): Boolean {
        return true
    }

    override fun visit(pkg: PackageSchema): Boolean {
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
        objectStart("type")
        listStart("entity_path")
        associationField.entityPath.forEach {
            wireFormatEncoder.encodeStringField("entity_path_element", it)
        }
        listEnd()
        objectEnd()
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

    override fun <T : Number> visit(number: NumericSchema<T>): Boolean {
        wireFormatEncoder.encodeStringField("type", "numeric")
        number.constraints?.let { encodeNumericConstraints(it) }
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

    override fun visit(ipv4Address: Ipv4AddressSchema): Boolean {
        wireFormatEncoder.encodeStringField("type", "ipv4_address")
        return true
    }

    override fun visit(ipv6Address: Ipv6AddressSchema): Boolean {
        wireFormatEncoder.encodeStringField("type", "ipv6_address")
        return true
    }

    override fun visit(macAddress: MacAddressSchema): Boolean {
        wireFormatEncoder.encodeStringField("type", "mac_address")
        return true
    }

    // Helper methods for constraints

    private fun <T : Number> encodeNumericConstraints(constraints: NumericConstraints<T>) = try {
        objectStart("constraints")
        constraints.minBound?.let { encodeNumericBound("min_bound", it) }
        constraints.maxBound?.let { encodeNumericBound("max_bound", it) }
    } finally {
        objectEnd()
    }

    private fun <T : Number> encodeNumericBound(name: String, bound: NumericBound<T>) = try {
        objectStart(name)
        wireFormatEncoder.encodeNumericField("value", bound.value)
        wireFormatEncoder.encodeBooleanField("inclusive", bound.inclusive)
    } finally {
        objectEnd()
    }

    private fun encodeStringConstraints(constraints: StringConstraints) = try {
        objectStart("constraints")
        constraints.minLength?.let { wireFormatEncoder.encodeNumericField("min_length", it) }
        constraints.maxLength?.let { wireFormatEncoder.encodeNumericField("max_length", it) }
        constraints.regex?.let { wireFormatEncoder.encodeStringField("regex", it) }
    } finally {
        objectEnd()
    }
}
