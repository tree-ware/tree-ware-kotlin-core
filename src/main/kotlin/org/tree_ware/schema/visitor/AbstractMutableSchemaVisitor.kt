package org.tree_ware.schema.visitor

import org.tree_ware.schema.core.*

abstract class AbstractMutableSchemaVisitor<T>(private val defaultVisitReturn: T) : MutableSchemaVisitor<T> {
    override fun mutableVisit(element: MutableElementSchema): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(element: MutableElementSchema) {}

    override fun mutableVisit(namedElement: MutableNamedElementSchema): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(namedElement: MutableNamedElementSchema) {}

    override fun mutableVisit(schema: MutableSchema): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(schema: MutableSchema) {}

    override fun mutableVisit(pkg: MutablePackageSchema): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(pkg: MutablePackageSchema) {}

    override fun mutableVisit(root: MutableRootSchema): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(root: MutableRootSchema) {}

    override fun mutableVisit(alias: MutableAliasSchema): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(alias: MutableAliasSchema) {}

    override fun mutableVisit(enumeration: MutableEnumerationSchema): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(enumeration: MutableEnumerationSchema) {}

    override fun mutableVisit(enumerationValue: MutableEnumerationValueSchema): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(enumerationValue: MutableEnumerationValueSchema) {}

    override fun mutableVisit(entity: MutableEntitySchema): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(entity: MutableEntitySchema) {}

    // Fields

    override fun mutableVisit(field: MutableFieldSchema): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(field: MutableFieldSchema) {}

    override fun mutableVisit(primitiveField: MutablePrimitiveFieldSchema): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(primitiveField: MutablePrimitiveFieldSchema) {}

    override fun mutableVisit(aliasField: MutableAliasFieldSchema): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(aliasField: MutableAliasFieldSchema) {}

    override fun mutableVisit(enumerationField: MutableEnumerationFieldSchema): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(enumerationField: MutableEnumerationFieldSchema) {}

    override fun mutableVisit(associationField: MutableAssociationFieldSchema): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(associationField: MutableAssociationFieldSchema) {}

    override fun mutableVisit(compositionField: MutableCompositionFieldSchema): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(compositionField: MutableCompositionFieldSchema) {}

    // Primitives

    override fun mutableVisit(boolean: MutableBooleanSchema): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(boolean: MutableBooleanSchema) {}

    override fun mutableVisit(byte: MutableByteSchema): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(byte: MutableByteSchema) {}

    override fun mutableVisit(short: MutableShortSchema): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(short: MutableShortSchema) {}

    override fun mutableVisit(int: MutableIntSchema): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(int: MutableIntSchema) {}

    override fun mutableVisit(long: MutableLongSchema): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(long: MutableLongSchema) {}

    override fun mutableVisit(float: MutableFloatSchema): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(float: MutableFloatSchema) {}

    override fun mutableVisit(double: MutableDoubleSchema): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(double: MutableDoubleSchema) {}

    override fun mutableVisit(string: MutableStringSchema): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(string: MutableStringSchema) {}

    override fun mutableVisit(password1Way: MutablePassword1WaySchema): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(password1Way: MutablePassword1WaySchema) {}

    override fun mutableVisit(password2Way: MutablePassword2WaySchema): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(password2Way: MutablePassword2WaySchema) {}

    override fun mutableVisit(uuid: MutableUuidSchema): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(uuid: MutableUuidSchema) {}

    override fun mutableVisit(blob: MutableBlobSchema): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(blob: MutableBlobSchema) {}

    override fun mutableVisit(timestamp: MutableTimestampSchema): T {
        return defaultVisitReturn
    }

    override fun mutableLeave(timestamp: MutableTimestampSchema) {}

    // Meta

    override fun mutableVisitList(name: String) {}

    override fun mutableLeaveList(name: String) {}
}
