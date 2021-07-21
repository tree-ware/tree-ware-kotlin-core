package org.treeWare.schema.visitor

import org.treeWare.schema.core.*

abstract class AbstractSchemaVisitor<T>(private val defaultVisitReturn: T) : SchemaVisitor<T> {
    override fun visit(element: ElementSchema): T {
        return defaultVisitReturn
    }

    override fun leave(element: ElementSchema) {}

    override fun visit(namedElement: NamedElementSchema): T {
        return defaultVisitReturn
    }

    override fun leave(namedElement: NamedElementSchema) {}

    override fun visit(schema: Schema): T {
        return defaultVisitReturn
    }

    override fun leave(schema: Schema) {}

    override fun visit(pkg: PackageSchema): T {
        return defaultVisitReturn
    }

    override fun leave(pkg: PackageSchema) {}

    override fun visit(root: RootSchema): T {
        return defaultVisitReturn
    }

    override fun leave(root: RootSchema) {}

    override fun visit(alias: AliasSchema): T {
        return defaultVisitReturn
    }

    override fun leave(alias: AliasSchema) {}

    override fun visit(enumeration: EnumerationSchema): T {
        return defaultVisitReturn
    }

    override fun leave(enumeration: EnumerationSchema) {}

    override fun visit(enumerationValue: EnumerationValueSchema): T {
        return defaultVisitReturn
    }

    override fun leave(enumerationValue: EnumerationValueSchema) {}

    override fun visit(entity: EntitySchema): T {
        return defaultVisitReturn
    }

    override fun leave(entity: EntitySchema) {}

    // Fields

    override fun visit(field: FieldSchema): T {
        return defaultVisitReturn
    }

    override fun leave(field: FieldSchema) {}

    override fun visit(primitiveField: PrimitiveFieldSchema): T {
        return defaultVisitReturn
    }

    override fun leave(primitiveField: PrimitiveFieldSchema) {}

    override fun visit(aliasField: AliasFieldSchema): T {
        return defaultVisitReturn
    }

    override fun leave(aliasField: AliasFieldSchema) {}

    override fun visit(enumerationField: EnumerationFieldSchema): T {
        return defaultVisitReturn
    }

    override fun leave(enumerationField: EnumerationFieldSchema) {}

    override fun visit(associationField: AssociationFieldSchema): T {
        return defaultVisitReturn
    }

    override fun leave(associationField: AssociationFieldSchema) {}

    override fun visit(compositionField: CompositionFieldSchema): T {
        return defaultVisitReturn
    }

    override fun leave(compositionField: CompositionFieldSchema) {}

    // Primitives

    override fun visit(boolean: BooleanSchema): T {
        return defaultVisitReturn
    }

    override fun leave(boolean: BooleanSchema) {}

    override fun visit(byte: ByteSchema): T {
        return defaultVisitReturn
    }

    override fun leave(byte: ByteSchema) {}

    override fun visit(short: ShortSchema): T {
        return defaultVisitReturn
    }

    override fun leave(short: ShortSchema) {}

    override fun visit(int: IntSchema): T {
        return defaultVisitReturn
    }

    override fun leave(int: IntSchema) {}

    override fun visit(long: LongSchema): T {
        return defaultVisitReturn
    }

    override fun leave(long: LongSchema) {}

    override fun visit(float: FloatSchema): T {
        return defaultVisitReturn
    }

    override fun leave(float: FloatSchema) {}

    override fun visit(double: DoubleSchema): T {
        return defaultVisitReturn
    }

    override fun leave(double: DoubleSchema) {}

    override fun visit(string: StringSchema): T {
        return defaultVisitReturn
    }

    override fun leave(string: StringSchema) {}

    override fun visit(password1Way: Password1WaySchema): T {
        return defaultVisitReturn
    }

    override fun leave(password1Way: Password1WaySchema) {}

    override fun visit(password2Way: Password2WaySchema): T {
        return defaultVisitReturn
    }

    override fun leave(password2Way: Password2WaySchema) {}

    override fun visit(uuid: UuidSchema): T {
        return defaultVisitReturn
    }

    override fun leave(uuid: UuidSchema) {}

    override fun visit(blob: BlobSchema): T {
        return defaultVisitReturn
    }

    override fun leave(blob: BlobSchema) {}

    override fun visit(timestamp: TimestampSchema): T {
        return defaultVisitReturn
    }

    override fun leave(timestamp: TimestampSchema) {}

    // Meta

    override fun visitList(name: String) {}

    override fun leaveList(name: String) {}
}
