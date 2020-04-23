package org.tree_ware.schema.visitor

import org.tree_ware.schema.core.*

abstract class AbstractSchemaVisitor : SchemaVisitor {
    override fun visit(element: ElementSchema): Boolean {
        return true
    }

    override fun visit(namedElement: NamedElementSchema): Boolean {
        return true
    }

    override fun visit(schema: Schema): Boolean {
        return true
    }

    override fun visit(pkg: PackageSchema): Boolean {
        return true
    }

    override fun visit(root: RootSchema): Boolean {
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

    // Fields

    override fun visit(field: FieldSchema): Boolean {
        return true
    }

    override fun visit(primitiveField: PrimitiveFieldSchema): Boolean {
        return true
    }

    override fun visit(aliasField: AliasFieldSchema): Boolean {
        return true
    }

    override fun visit(enumerationField: EnumerationFieldSchema): Boolean {
        return true
    }

    override fun visit(associationField: AssociationFieldSchema): Boolean {
        return true
    }

    override fun visit(compositionField: CompositionFieldSchema): Boolean {
        return true
    }

    // Primitives

    override fun visit(boolean: BooleanSchema): Boolean {
        return true
    }

    override fun visit(byte: ByteSchema): Boolean {
        return true
    }

    override fun visit(short: ShortSchema): Boolean {
        return true
    }

    override fun visit(int: IntSchema): Boolean {
        return true
    }

    override fun visit(long: LongSchema): Boolean {
        return true
    }

    override fun visit(float: FloatSchema): Boolean {
        return true
    }

    override fun visit(double: DoubleSchema): Boolean {
        return true
    }

    override fun visit(string: StringSchema): Boolean {
        return true
    }

    override fun visit(password1Way: Password1WaySchema): Boolean {
        return true
    }

    override fun visit(password2Way: Password2WaySchema): Boolean {
        return true
    }

    override fun visit(uuid: UuidSchema): Boolean {
        return true
    }

    override fun visit(blob: BlobSchema): Boolean {
        return true
    }

    override fun visit(timestamp: TimestampSchema): Boolean {
        return true
    }
}