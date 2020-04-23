package org.tree_ware.model.core

import org.tree_ware.core.schema.*

fun newMutableModel(schema: ElementSchema, parent: MutableElementModel?): MutableElementModel {
    val newMutableModelVisitor = MutableModelFactoryVisitor(parent)
    schema.accept(newMutableModelVisitor)
    return newMutableModelVisitor.newModel
}

class MutableModelFactoryVisitor(private val parent: MutableElementModel?) : SchemaVisitor {
    // TODO(deepak-nulu): a non-traversing dispatch() method & visitor with template return type
    // to avoid the following state variable
    var newModel: MutableElementModel
        get() = _newModel ?: throw IllegalStateException("Element has not been set")
        internal set(value) {
            _newModel = value
        }
    private var _newModel: MutableElementModel? = null

    override fun visit(element: ElementSchema): Boolean {
        return true
    }

    override fun visit(namedElement: NamedElementSchema): Boolean {
        return true
    }

    override fun visit(schema: Schema): Boolean {
        // TODO(deepak-nulu): uncomment once rootSchema is not needed for creating MutableModel
        // newModel = MutableModel(schema, rootSchema)
        return true
    }

    override fun visit(pkg: PackageSchema): Boolean {
        return true
    }

    override fun visit(root: RootSchema): Boolean {
        val rootParent = parent as MutableModel
        newModel = MutableRootModel(root, rootParent)
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
        val entityParent = parent as MutableFieldModel
        newModel = MutableEntityModel(entity, entityParent)
        return true
    }

    // Fields

    override fun visit(field: FieldSchema): Boolean {
        return true
    }

    override fun visit(primitiveField: PrimitiveFieldSchema): Boolean {
        val fieldParent = parent as MutableBaseEntityModel
        newModel =
            if (primitiveField.multiplicity.isList()) MutablePrimitiveListFieldModel(primitiveField, fieldParent)
            else MutablePrimitiveFieldModel(primitiveField, fieldParent)
        return true
    }

    override fun visit(aliasField: AliasFieldSchema): Boolean {
        val fieldParent = parent as MutableBaseEntityModel
        newModel =
            if (aliasField.multiplicity.isList()) MutableAliasListFieldModel(aliasField, fieldParent)
            else MutableAliasFieldModel(aliasField, fieldParent)
        return true
    }

    override fun visit(enumerationField: EnumerationFieldSchema): Boolean {
        val fieldParent = parent as MutableBaseEntityModel
        newModel =
            if (enumerationField.multiplicity.isList()) MutableEnumerationListFieldModel(enumerationField, fieldParent)
            else MutableEnumerationFieldModel(enumerationField, fieldParent)
        return true
    }

    override fun visit(associationField: AssociationFieldSchema): Boolean {
        val fieldParent = parent as MutableBaseEntityModel
        newModel =
            if (associationField.multiplicity.isList()) MutableAssociationListFieldModel(associationField, fieldParent)
            else MutableAssociationFieldModel(associationField, fieldParent)
        return true
    }

    override fun visit(compositionField: CompositionFieldSchema): Boolean {
        val fieldParent = parent as MutableBaseEntityModel
        newModel =
            if (compositionField.multiplicity.isList()) MutableCompositionListFieldModel(compositionField, fieldParent)
            else MutableCompositionFieldModel(compositionField, fieldParent)
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