package org.tree_ware.schema.visitor

import org.tree_ware.schema.core.*

abstract class AbstractMutableSchemaVisitor : MutableSchemaVisitor<SchemaTraversalAction> {
    override fun mutableVisit(element: MutableElementSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableLeave(element: MutableElementSchema) {}

    override fun mutableVisit(namedElement: MutableNamedElementSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableLeave(namedElement: MutableNamedElementSchema) {}

    override fun mutableVisit(schema: MutableSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableLeave(schema: MutableSchema) {}

    override fun mutableVisit(pkg: MutablePackageSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableLeave(pkg: MutablePackageSchema) {}

    override fun mutableVisit(root: MutableRootSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableLeave(root: MutableRootSchema) {}

    override fun mutableVisit(alias: MutableAliasSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableLeave(alias: MutableAliasSchema) {}

    override fun mutableVisit(enumeration: MutableEnumerationSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableLeave(enumeration: MutableEnumerationSchema) {}

    override fun mutableVisit(enumerationValue: MutableEnumerationValueSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableLeave(enumerationValue: MutableEnumerationValueSchema) {}

    override fun mutableVisit(entity: MutableEntitySchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableLeave(entity: MutableEntitySchema) {}

    // Fields

    override fun mutableVisit(field: MutableFieldSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableLeave(field: MutableFieldSchema) {}

    override fun mutableVisit(primitiveField: MutablePrimitiveFieldSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableLeave(primitiveField: MutablePrimitiveFieldSchema) {}

    override fun mutableVisit(aliasField: MutableAliasFieldSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableLeave(aliasField: MutableAliasFieldSchema) {}

    override fun mutableVisit(enumerationField: MutableEnumerationFieldSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableLeave(enumerationField: MutableEnumerationFieldSchema) {}

    override fun mutableVisit(associationField: MutableAssociationFieldSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableLeave(associationField: MutableAssociationFieldSchema) {}

    override fun mutableVisit(compositionField: MutableCompositionFieldSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableLeave(compositionField: MutableCompositionFieldSchema) {}

    // Primitives

    override fun mutableVisit(boolean: MutableBooleanSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableLeave(boolean: MutableBooleanSchema) {}

    override fun mutableVisit(byte: MutableByteSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableLeave(byte: MutableByteSchema) {}

    override fun mutableVisit(short: MutableShortSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableLeave(short: MutableShortSchema) {}

    override fun mutableVisit(int: MutableIntSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableLeave(int: MutableIntSchema) {}

    override fun mutableVisit(long: MutableLongSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableLeave(long: MutableLongSchema) {}

    override fun mutableVisit(float: MutableFloatSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableLeave(float: MutableFloatSchema) {}

    override fun mutableVisit(double: MutableDoubleSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableLeave(double: MutableDoubleSchema) {}

    override fun mutableVisit(string: MutableStringSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableLeave(string: MutableStringSchema) {}

    override fun mutableVisit(password1Way: MutablePassword1WaySchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableLeave(password1Way: MutablePassword1WaySchema) {}

    override fun mutableVisit(password2Way: MutablePassword2WaySchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableLeave(password2Way: MutablePassword2WaySchema) {}

    override fun mutableVisit(uuid: MutableUuidSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableLeave(uuid: MutableUuidSchema) {}

    override fun mutableVisit(blob: MutableBlobSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableLeave(blob: MutableBlobSchema) {}

    override fun mutableVisit(timestamp: MutableTimestampSchema): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableLeave(timestamp: MutableTimestampSchema) {}

    // Meta

    override fun mutableVisitList(name: String) {}

    override fun mutableLeaveList(name: String) {}
}
