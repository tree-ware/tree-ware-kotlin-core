package org.tree_ware.core.schema

import java.lang.reflect.Field

/** Schema visitor (Visitor Pattern).
 *
 * `visit()` methods should return `true` to proceed with schema traversal and `false` to stop schema traversal.
 */
public interface SchemaVisitor {
    fun visit(element: ElementSchema): Boolean

    fun visit(pkg: PackageSchema): Boolean

    fun visit(alias: AliasSchema): Boolean
    fun visit(enumeration: EnumerationSchema): Boolean
    fun visit(entity: EntitySchema): Boolean

    // Fields

    fun visit(field: FieldSchema): Boolean

    fun visit(primitiveField: PrimitiveFieldSchema): Boolean
    fun visit(aliasField: AliasFieldSchema): Boolean
    fun visit(enumerationField: EnumerationFieldSchema): Boolean
    fun visit(entityField: EntityFieldSchema): Boolean

    // Primitives

    fun visit(boolean: BooleanSchema): Boolean
    fun <T : Number> visit(number: NumericSchema<T>): Boolean
    fun visit(string: StringSchema): Boolean
    fun visit(password1Way: Password1WaySchema): Boolean
    fun visit(password2Way: Password2WaySchema): Boolean
    fun visit(uuid: UuidSchema): Boolean
    fun visit(blob: BlobSchema): Boolean
}
