package org.treeWare.schema.core

/** MutableSchema visitor (Visitor Pattern).
 * This is an enhanced visitor with `visit()` and `leave()` methods for each
 * element instead of just a `visit()` method for each element.
 *
 * The `SchemaVisitor` cannot mutate the schema elements it visits,
 * but this interface can mutate the schema elements it visits.
 */
interface MutableSchemaVisitor<T> {
    fun mutableVisit(element: MutableElementSchema): T
    fun mutableLeave(element: MutableElementSchema)

    fun mutableVisit(namedElement: MutableNamedElementSchema): T
    fun mutableLeave(namedElement: MutableNamedElementSchema)

    // Top level

    fun mutableVisit(schema: MutableSchema): T
    fun mutableLeave(schema: MutableSchema)

    fun mutableVisit(pkg: MutablePackageSchema): T
    fun mutableLeave(pkg: MutablePackageSchema)

    fun mutableVisit(root: MutableRootSchema): T
    fun mutableLeave(root: MutableRootSchema)

    fun mutableVisit(alias: MutableAliasSchema): T
    fun mutableLeave(alias: MutableAliasSchema)

    fun mutableVisit(enumeration: MutableEnumerationSchema): T
    fun mutableLeave(enumeration: MutableEnumerationSchema)

    fun mutableVisit(enumerationValue: MutableEnumerationValueSchema): T
    fun mutableLeave(enumerationValue: MutableEnumerationValueSchema)

    fun mutableVisit(entity: MutableEntitySchema): T
    fun mutableLeave(entity: MutableEntitySchema)

    // Fields

    fun mutableVisit(field: MutableFieldSchema): T
    fun mutableLeave(field: MutableFieldSchema)

    fun mutableVisit(primitiveField: MutablePrimitiveFieldSchema): T
    fun mutableLeave(primitiveField: MutablePrimitiveFieldSchema)

    fun mutableVisit(aliasField: MutableAliasFieldSchema): T
    fun mutableLeave(aliasField: MutableAliasFieldSchema)

    fun mutableVisit(enumerationField: MutableEnumerationFieldSchema): T
    fun mutableLeave(enumerationField: MutableEnumerationFieldSchema)

    fun mutableVisit(associationField: MutableAssociationFieldSchema): T
    fun mutableLeave(associationField: MutableAssociationFieldSchema)

    fun mutableVisit(compositionField: MutableCompositionFieldSchema): T
    fun mutableLeave(compositionField: MutableCompositionFieldSchema)

    // Primitives

    fun mutableVisit(boolean: MutableBooleanSchema): T
    fun mutableLeave(boolean: MutableBooleanSchema)

    fun mutableVisit(byte: MutableByteSchema): T
    fun mutableLeave(byte: MutableByteSchema)

    fun mutableVisit(short: MutableShortSchema): T
    fun mutableLeave(short: MutableShortSchema)

    fun mutableVisit(int: MutableIntSchema): T
    fun mutableLeave(int: MutableIntSchema)

    fun mutableVisit(long: MutableLongSchema): T
    fun mutableLeave(long: MutableLongSchema)

    fun mutableVisit(float: MutableFloatSchema): T
    fun mutableLeave(float: MutableFloatSchema)

    fun mutableVisit(double: MutableDoubleSchema): T
    fun mutableLeave(double: MutableDoubleSchema)

    fun mutableVisit(string: MutableStringSchema): T
    fun mutableLeave(string: MutableStringSchema)

    fun mutableVisit(password1Way: MutablePassword1WaySchema): T
    fun mutableLeave(password1Way: MutablePassword1WaySchema)

    fun mutableVisit(password2Way: MutablePassword2WaySchema): T
    fun mutableLeave(password2Way: MutablePassword2WaySchema)

    fun mutableVisit(uuid: MutableUuidSchema): T
    fun mutableLeave(uuid: MutableUuidSchema)

    fun mutableVisit(blob: MutableBlobSchema): T
    fun mutableLeave(blob: MutableBlobSchema)

    fun mutableVisit(timestamp: MutableTimestampSchema): T
    fun mutableLeave(timestamp: MutableTimestampSchema)

    // Meta

    fun mutableVisitList(name: String)
    fun mutableLeaveList(name: String)
}
