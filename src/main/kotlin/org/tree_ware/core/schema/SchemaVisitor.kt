package org.tree_ware.core.schema

/** Schema visitor (Visitor Pattern).
 *
 * `visit()` methods should return `true` to proceed with schema traversal
 * and `false` to stop schema traversal.
 */
public interface SchemaVisitor {
    fun visit(element: ElementSchema): Boolean
    fun visit(namedElement: NamedElementSchema): Boolean

    fun visit(schema: Schema): Boolean
    fun visit(pkg: PackageSchema): Boolean

    fun visit(alias: AliasSchema): Boolean
    fun visit(enumeration: EnumerationSchema): Boolean
    fun visit(enumerationValue: EnumerationValueSchema): Boolean
    fun visit(entity: EntitySchema): Boolean

    // Fields

    fun visit(field: FieldSchema): Boolean

    fun visit(primitiveField: PrimitiveFieldSchema): Boolean
    fun visit(aliasField: AliasFieldSchema): Boolean
    fun visit(enumerationField: EnumerationFieldSchema): Boolean
    fun visit(associationField: AssociationFieldSchema): Boolean
    fun visit(compositionField: CompositionFieldSchema): Boolean

    // Primitives

    fun visit(boolean: BooleanSchema): Boolean
    fun <T : Number> visit(number: NumericSchema<T>): Boolean
    fun visit(string: StringSchema): Boolean
    fun visit(password1Way: Password1WaySchema): Boolean
    fun visit(password2Way: Password2WaySchema): Boolean
    fun visit(uuid: UuidSchema): Boolean
    fun visit(blob: BlobSchema): Boolean
    fun visit(timestamp: TimestampSchema): Boolean
    fun visit(ipv4Address: Ipv4AddressSchema): Boolean
    fun visit(ipv6Address: Ipv6AddressSchema): Boolean
    fun visit(macAddress: MacAddressSchema): Boolean
}
