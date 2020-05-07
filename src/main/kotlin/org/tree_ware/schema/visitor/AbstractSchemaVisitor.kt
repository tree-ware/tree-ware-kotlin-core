package org.tree_ware.schema.visitor

import org.tree_ware.schema.core.*

abstract class AbstractSchemaVisitor : SchemaVisitor<SchemaTraversalAction> {
    override fun visit(element: ElementSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(element: ElementSchema) {}

    override fun visit(namedElement: NamedElementSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(namedElement: NamedElementSchema) {}

    override fun visit(schema: Schema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(schema: Schema) {}

    override fun visit(pkg: PackageSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(pkg: PackageSchema) {}

    override fun visit(root: RootSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(root: RootSchema) {}

    override fun visit(alias: AliasSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(alias: AliasSchema) {}

    override fun visit(enumeration: EnumerationSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(enumeration: EnumerationSchema) {}

    override fun visit(enumerationValue: EnumerationValueSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(enumerationValue: EnumerationValueSchema) {}

    override fun visit(entity: EntitySchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(entity: EntitySchema) {}

    // Fields

    override fun visit(field: FieldSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(field: FieldSchema) {}

    override fun visit(primitiveField: PrimitiveFieldSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(primitiveField: PrimitiveFieldSchema) {}

    override fun visit(aliasField: AliasFieldSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(aliasField: AliasFieldSchema) {}

    override fun visit(enumerationField: EnumerationFieldSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(enumerationField: EnumerationFieldSchema) {}

    override fun visit(associationField: AssociationFieldSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(associationField: AssociationFieldSchema) {}

    override fun visit(compositionField: CompositionFieldSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(compositionField: CompositionFieldSchema) {}

    // Primitives

    override fun visit(boolean: BooleanSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(boolean: BooleanSchema) {}

    override fun visit(byte: ByteSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(byte: ByteSchema) {}

    override fun visit(short: ShortSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(short: ShortSchema) {}

    override fun visit(int: IntSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(int: IntSchema) {}

    override fun visit(long: LongSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(long: LongSchema) {}

    override fun visit(float: FloatSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(float: FloatSchema) {}

    override fun visit(double: DoubleSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(double: DoubleSchema) {}

    override fun visit(string: StringSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(string: StringSchema) {}

    override fun visit(password1Way: Password1WaySchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(password1Way: Password1WaySchema) {}

    override fun visit(password2Way: Password2WaySchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(password2Way: Password2WaySchema) {}

    override fun visit(uuid: UuidSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(uuid: UuidSchema) {}

    override fun visit(blob: BlobSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(blob: BlobSchema) {}

    override fun visit(timestamp: TimestampSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun leave(timestamp: TimestampSchema) {}

    // Meta

    override fun visitList(name: String) {}

    override fun leaveList(name: String) {}
}
