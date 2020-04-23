package org.tree_ware.schema.visitor

import org.tree_ware.schema.core.*

// TODO(deepak-nulu): implement visitor-combinators to combine multiple visitors into a single traversal.

/** Adapts a SchemaVisitor to make it look like a MutableSchemaVisitor.
 * The SchemaVisitor cannot mutate the elements it visits (since the types of the elements passed to it are still
 * the immutable types), but it can be used in mutable-visitor-combinators along with other MutableSchemaVisitor
 * instances.
 */
class MutableAdapter(private val adaptee: SchemaVisitor) :
    MutableSchemaVisitor {
    override fun mutableVisit(element: MutableElementSchema): Boolean {
        return adaptee.visit(element)
    }

    override fun mutableVisit(namedElement: MutableNamedElementSchema): Boolean {
        return adaptee.visit(namedElement)
    }

    override fun mutableVisit(schema: MutableSchema): Boolean {
        return adaptee.visit(schema)
    }

    override fun mutableVisit(pkg: MutablePackageSchema): Boolean {
        return adaptee.visit(pkg)
    }

    override fun mutableVisit(root: MutableRootSchema): Boolean {
        return adaptee.visit(root)
    }

    override fun mutableVisit(alias: MutableAliasSchema): Boolean {
        return adaptee.visit(alias)
    }

    override fun mutableVisit(enumeration: MutableEnumerationSchema): Boolean {
        return adaptee.visit(enumeration)
    }

    override fun mutableVisit(enumerationValue: MutableEnumerationValueSchema): Boolean {
        return adaptee.visit(enumerationValue)
    }

    override fun mutableVisit(entity: MutableEntitySchema): Boolean {
        return adaptee.visit(entity)
    }

    // Fields

    override fun mutableVisit(field: MutableFieldSchema): Boolean {
        return adaptee.visit(field)
    }

    override fun mutableVisit(primitiveField: MutablePrimitiveFieldSchema): Boolean {
        return adaptee.visit(primitiveField)
    }

    override fun mutableVisit(aliasField: MutableAliasFieldSchema): Boolean {
        return adaptee.visit(aliasField)
    }

    override fun mutableVisit(enumerationField: MutableEnumerationFieldSchema): Boolean {
        return adaptee.visit(enumerationField)
    }

    override fun mutableVisit(associationField: MutableAssociationFieldSchema): Boolean {
        return adaptee.visit(associationField)
    }

    override fun mutableVisit(compositionField: MutableCompositionFieldSchema): Boolean {
        return adaptee.visit(compositionField)
    }

    // Primitives

    override fun mutableVisit(boolean: BooleanSchema): Boolean {
        return adaptee.visit(boolean)
    }

    override fun mutableVisit(byte: ByteSchema): Boolean {
        return adaptee.visit(byte)
    }

    override fun mutableVisit(short: ShortSchema): Boolean {
        return adaptee.visit(short)
    }

    override fun mutableVisit(int: IntSchema): Boolean {
        return adaptee.visit(int)
    }

    override fun mutableVisit(long: LongSchema): Boolean {
        return adaptee.visit(long)
    }

    override fun mutableVisit(float: FloatSchema): Boolean {
        return adaptee.visit(float)
    }

    override fun mutableVisit(double: DoubleSchema): Boolean {
        return adaptee.visit(double)
    }

    override fun mutableVisit(string: MutableStringSchema): Boolean {
        return adaptee.visit(string)
    }

    override fun mutableVisit(password1Way: MutablePassword1WaySchema): Boolean {
        return adaptee.visit(password1Way)
    }

    override fun mutableVisit(password2Way: MutablePassword2WaySchema): Boolean {
        return adaptee.visit(password2Way)
    }

    override fun mutableVisit(uuid: MutableUuidSchema): Boolean {
        return adaptee.visit(uuid)
    }

    override fun mutableVisit(blob: MutableBlobSchema): Boolean {
        return adaptee.visit(blob)
    }

    override fun mutableVisit(timestamp: MutableTimestampSchema): Boolean {
        return adaptee.visit(timestamp)
    }
}
