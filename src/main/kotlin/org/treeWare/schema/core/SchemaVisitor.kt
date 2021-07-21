package org.treeWare.schema.core

/** Schema visitor (Visitor Pattern).
 * This is an enhanced visitor with `visit()` and `leave()` methods for each
 * element instead of just a `visit()` method for each element.
 */
interface SchemaVisitor<T> {
    fun visit(element: ElementSchema): T
    fun leave(element: ElementSchema)

    fun visit(namedElement: NamedElementSchema): T
    fun leave(namedElement: NamedElementSchema)

    // Top level

    fun visit(schema: Schema): T
    fun leave(schema: Schema)

    fun visit(pkg: PackageSchema): T
    fun leave(pkg: PackageSchema)

    fun visit(root: RootSchema): T
    fun leave(root: RootSchema)

    fun visit(alias: AliasSchema): T
    fun leave(alias: AliasSchema)

    fun visit(enumeration: EnumerationSchema): T
    fun leave(enumeration: EnumerationSchema)

    fun visit(enumerationValue: EnumerationValueSchema): T
    fun leave(enumerationValue: EnumerationValueSchema)

    fun visit(entity: EntitySchema): T
    fun leave(entity: EntitySchema)

    // Fields

    fun visit(field: FieldSchema): T
    fun leave(field: FieldSchema)

    fun visit(primitiveField: PrimitiveFieldSchema): T
    fun leave(primitiveField: PrimitiveFieldSchema)

    fun visit(aliasField: AliasFieldSchema): T
    fun leave(aliasField: AliasFieldSchema)

    fun visit(enumerationField: EnumerationFieldSchema): T
    fun leave(enumerationField: EnumerationFieldSchema)

    fun visit(associationField: AssociationFieldSchema): T
    fun leave(associationField: AssociationFieldSchema)

    fun visit(compositionField: CompositionFieldSchema): T
    fun leave(compositionField: CompositionFieldSchema)

    // Primitives

    fun visit(boolean: BooleanSchema): T
    fun leave(boolean: BooleanSchema)

    fun visit(byte: ByteSchema): T
    fun leave(byte: ByteSchema)

    fun visit(short: ShortSchema): T
    fun leave(short: ShortSchema)

    fun visit(int: IntSchema): T
    fun leave(int: IntSchema)

    fun visit(long: LongSchema): T
    fun leave(long: LongSchema)

    fun visit(float: FloatSchema): T
    fun leave(float: FloatSchema)

    fun visit(double: DoubleSchema): T
    fun leave(double: DoubleSchema)

    fun visit(string: StringSchema): T
    fun leave(string: StringSchema)

    fun visit(password1Way: Password1WaySchema): T
    fun leave(password1Way: Password1WaySchema)

    fun visit(password2Way: Password2WaySchema): T
    fun leave(password2Way: Password2WaySchema)

    fun visit(uuid: UuidSchema): T
    fun leave(uuid: UuidSchema)

    fun visit(blob: BlobSchema): T
    fun leave(blob: BlobSchema)

    fun visit(timestamp: TimestampSchema): T
    fun leave(timestamp: TimestampSchema)

    // Meta

    fun visitList(name: String)
    fun leaveList(name: String)
}
