package org.tree_ware.schema.visitor

import org.tree_ware.schema.core.*

abstract class AbstractMutableSchemaVisitor : MutableSchemaVisitor {
    override fun mutableVisit(element: MutableElementSchema): Boolean {
        return true
    }

    override fun mutableVisit(namedElement: MutableNamedElementSchema): Boolean {
        return true
    }

    override fun mutableVisit(schema: MutableSchema): Boolean {
        return true
    }

    override fun mutableVisit(pkg: MutablePackageSchema): Boolean {
        return true
    }

    override fun mutableVisit(root: MutableRootSchema): Boolean {
        return true
    }


    override fun mutableVisit(alias: MutableAliasSchema): Boolean {
        return true
    }

    override fun mutableVisit(enumeration: MutableEnumerationSchema): Boolean {
        return true
    }

    override fun mutableVisit(enumerationValue: MutableEnumerationValueSchema): Boolean {
        return true
    }

    override fun mutableVisit(entity: MutableEntitySchema): Boolean {
        return true
    }

    // Fields

    override fun mutableVisit(field: MutableFieldSchema): Boolean {
        return true
    }

    override fun mutableVisit(primitiveField: MutablePrimitiveFieldSchema): Boolean {
        return true
    }

    override fun mutableVisit(aliasField: MutableAliasFieldSchema): Boolean {
        return true
    }

    override fun mutableVisit(enumerationField: MutableEnumerationFieldSchema): Boolean {
        return true
    }

    override fun mutableVisit(associationField: MutableAssociationFieldSchema): Boolean {
        return true
    }

    override fun mutableVisit(compositionField: MutableCompositionFieldSchema): Boolean {
        return true
    }

    // Primitives

    override fun mutableVisit(boolean: BooleanSchema): Boolean {
        return true
    }

    override fun mutableVisit(byte: ByteSchema): Boolean {
        return true
    }

    override fun mutableVisit(short: ShortSchema): Boolean {
        return true
    }

    override fun mutableVisit(int: IntSchema): Boolean {
        return true
    }

    override fun mutableVisit(long: LongSchema): Boolean {
        return true
    }

    override fun mutableVisit(float: FloatSchema): Boolean {
        return true
    }

    override fun mutableVisit(double: DoubleSchema): Boolean {
        return true
    }

    override fun mutableVisit(string: MutableStringSchema): Boolean {
        return true
    }

    override fun mutableVisit(password1Way: MutablePassword1WaySchema): Boolean {
        return true
    }

    override fun mutableVisit(password2Way: MutablePassword2WaySchema): Boolean {
        return true
    }

    override fun mutableVisit(uuid: MutableUuidSchema): Boolean {
        return true
    }

    override fun mutableVisit(blob: MutableBlobSchema): Boolean {
        return true
    }

    override fun mutableVisit(timestamp: MutableTimestampSchema): Boolean {
        return true
    }
}