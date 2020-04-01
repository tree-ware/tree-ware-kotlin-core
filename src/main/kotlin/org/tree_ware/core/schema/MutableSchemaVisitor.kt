package org.tree_ware.core.schema

/** MutableSchema visitor (Visitor Pattern).
 *
 * The `SchemaVisitor` cannot mutate the schema elements it visits,
 * but this interface can mutate the schema elements it visits.
 *
 * `mutableVisit()` methods should return `true` to proceed with schema traversal
 * and `false` to stop schema traversal.
 */
interface MutableSchemaVisitor {
    fun mutableVisit(element: MutableElementSchema): Boolean
    fun mutableVisit(namedElement: MutableNamedElementSchema): Boolean

    fun mutableVisit(schema: MutableSchema): Boolean
    fun mutableVisit(pkg: MutablePackageSchema): Boolean

    fun mutableVisit(alias: MutableAliasSchema): Boolean
    fun mutableVisit(enumeration: MutableEnumerationSchema): Boolean
    fun mutableVisit(enumerationValue: MutableEnumerationValueSchema): Boolean
    fun mutableVisit(entity: MutableEntitySchema): Boolean

    // Fields

    fun mutableVisit(field: MutableFieldSchema): Boolean

    fun mutableVisit(primitiveField: MutablePrimitiveFieldSchema): Boolean
    fun mutableVisit(aliasField: MutableAliasFieldSchema): Boolean
    fun mutableVisit(enumerationField: MutableEnumerationFieldSchema): Boolean
    fun mutableVisit(associationField: MutableAssociationFieldSchema): Boolean
    fun mutableVisit(compositionField: MutableCompositionFieldSchema): Boolean

    // Primitives

    fun mutableVisit(boolean: BooleanSchema): Boolean
    fun mutableVisit(byte: ByteSchema): Boolean
    fun mutableVisit(short: ShortSchema): Boolean
    fun mutableVisit(int: IntSchema): Boolean
    fun mutableVisit(long: LongSchema): Boolean
    fun mutableVisit(float: FloatSchema): Boolean
    fun mutableVisit(double: DoubleSchema): Boolean
    fun mutableVisit(string: MutableStringSchema): Boolean
    fun mutableVisit(password1Way: MutablePassword1WaySchema): Boolean
    fun mutableVisit(password2Way: MutablePassword2WaySchema): Boolean
    fun mutableVisit(uuid: MutableUuidSchema): Boolean
    fun mutableVisit(blob: MutableBlobSchema): Boolean
    fun mutableVisit(timestamp: MutableTimestampSchema): Boolean
}
