package org.tree_ware.model.core

import org.tree_ware.core.schema.*
import java.math.BigDecimal

interface VisitableMutableModel {
    /**
     * Accepts a visitor and traverses the mutable model with it (Visitor Pattern).
     *
     * @returns `true` to proceed with schema traversal, `false` to stop schema traversal.
     */
    fun mutableAccept(visitor: MutableModelVisitor): Boolean
}

abstract class MutableElementModel : ElementModel, VisitableMutableModel {
    var objectId = ""

    override fun accept(visitor: ModelVisitor): Boolean {
        try {
            if (!visitSelf(visitor)) return false
            if (!traverseChildren(visitor)) return false
            return true
        } finally {
            leaveSelf(visitor)
        }
    }

    override fun mutableAccept(visitor: MutableModelVisitor): Boolean {
        try {
            if (!mutableVisitSelf(visitor)) return false
            if (!mutableTraverseChildren(visitor)) return false
            return true
        } finally {
            mutableLeaveSelf(visitor)
        }
    }

    // NOTE: call super.visitSelf() FIRST when overriding this method
    protected open fun visitSelf(visitor: ModelVisitor): Boolean {
        return visitor.visit(this)
    }

    // NOTE: call super.leaveSelf() LAST when overriding this method
    protected open fun leaveSelf(visitor: ModelVisitor) {
        visitor.leave(this)
    }

    // NOTE: call super.mutableVisitSelf() FIRST when overriding this method
    protected open fun mutableVisitSelf(visitor: MutableModelVisitor): Boolean {
        return visitor.mutableVisit(this)
    }

    // NOTE: call super.mutableLeaveSelf() LAST when overriding this method
    protected open fun mutableLeaveSelf(visitor: MutableModelVisitor) {
        visitor.mutableLeave(this)
    }

    protected open fun traverseChildren(visitor: ModelVisitor): Boolean {
        return true
    }

    protected open fun mutableTraverseChildren(visitor: MutableModelVisitor): Boolean {
        return true
    }
}

// TODO(deepak-nulu): remove rootSchema param once it is available from schema
class MutableModel(override val schema: Schema, private val rootSchema: RootSchema) : MutableElementModel(), Model {
    override val parent: ElementModel? = null

    override var type = ModelType.data
        internal set(value) {
            field = value
            _root?.also { it.objectId = value.name }
        }

    override var root: MutableRootModel
        get() = _root ?: throw IllegalStateException("Root has not been set")
        internal set(value) {
            value.objectId = type.name
            _root = value
        }
    private var _root: MutableRootModel? = null

    fun getOrNewRoot(): MutableRootModel {
        if (_root == null) _root = newMutableModel(rootSchema, this) as MutableRootModel
        return root
    }

    override fun visitSelf(visitor: ModelVisitor): Boolean {
        return super.visitSelf(visitor) && visitor.visit(this)
    }

    override fun leaveSelf(visitor: ModelVisitor) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor): Boolean {
        return super.mutableVisitSelf(visitor) && visitor.mutableVisit(this)
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: ModelVisitor): Boolean {
        if (!super.traverseChildren(visitor)) return false
        return root.accept(visitor)
    }

    override fun mutableTraverseChildren(visitor: MutableModelVisitor): Boolean {
        if (!super.mutableTraverseChildren(visitor)) return false
        return root.mutableAccept(visitor)
    }
}

abstract class MutableBaseEntityModel(private val entitySchema: EntitySchema) : MutableElementModel(), BaseEntityModel {
    override var fields: MutableList<MutableFieldModel> = mutableListOf()
        internal set

    fun getOrNewScalarField(fieldName: String): MutableScalarFieldModel? {
        val fieldSchema = entitySchema.getField(fieldName) ?: return null
        if (fieldSchema.multiplicity.isList()) return null

        var field = getField(fieldName) as MutableScalarFieldModel?
        if (field == null) {
            field = newMutableModel(fieldSchema, this) as MutableScalarFieldModel?
            if (field != null) fields.add(field)
        }
        return field
    }

    fun getOrNewCompositionField(fieldName: String): MutableCompositionFieldModel? {
        val fieldSchema = entitySchema.getField(fieldName) ?: return null
        if (fieldSchema !is CompositionFieldSchema) return null
        if (fieldSchema.multiplicity.isList()) return null

        var field = getField(fieldName) as MutableCompositionFieldModel?
        if (field == null) {
            field = newMutableModel(fieldSchema, this) as MutableCompositionFieldModel?
            if (field != null) fields.add(field)
        }
        return field
    }

    fun getOrNewAssociationField(fieldName: String): MutableAssociationFieldModel? {
        val fieldSchema = entitySchema.getField(fieldName) ?: return null
        if (fieldSchema !is AssociationFieldSchema) return null
        if (fieldSchema.multiplicity.isList()) return null

        var field = getField(fieldName) as MutableAssociationFieldModel?
        if (field == null) {
            field = newMutableModel(fieldSchema, this) as MutableAssociationFieldModel?
            if (field != null) fields.add(field)
        }
        return field
    }

    fun getOrNewListField(fieldName: String): MutableListFieldModel? {
        val fieldSchema = entitySchema.getField(fieldName) ?: return null
        if (!fieldSchema.multiplicity.isList()) return null

        var field = getField(fieldName) as MutableListFieldModel?
        if (field == null) {
            field = newMutableModel(fieldSchema, this) as MutableListFieldModel?
            if (field != null) fields.add(field)
        }
        return field
    }

    // TODO(deepak-nulu): optimize
    private fun getField(fieldName: String): MutableFieldModel? = fields.find { it.schema.name == fieldName }

    override fun visitSelf(visitor: ModelVisitor): Boolean {
        return super.visitSelf(visitor) && visitor.visit(this)
    }

    override fun leaveSelf(visitor: ModelVisitor) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor): Boolean {
        return super.mutableVisitSelf(visitor) && visitor.mutableVisit(this)
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: ModelVisitor): Boolean {
        if (!super.traverseChildren(visitor)) return false

        for (field in fields) {
            if (!field.accept(visitor)) return false
        }

        return true
    }

    override fun mutableTraverseChildren(visitor: MutableModelVisitor): Boolean {
        if (!super.mutableTraverseChildren(visitor)) return false

        for (field in fields) {
            if (!field.mutableAccept(visitor)) return false
        }

        return true
    }
}

class MutableRootModel(
    override val schema: RootSchema,
    override val parent: MutableModel
) : MutableBaseEntityModel(schema.resolvedEntity), RootModel {
    override fun visitSelf(visitor: ModelVisitor): Boolean {
        return super.visitSelf(visitor) && visitor.visit(this)
    }

    override fun leaveSelf(visitor: ModelVisitor) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor): Boolean {
        return super.mutableVisitSelf(visitor) && visitor.mutableVisit(this)
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }
}

class MutableEntityModel(
    override val schema: EntitySchema,
    override val parent: MutableFieldModel
) : MutableBaseEntityModel(schema), EntityModel {
    override fun visitSelf(visitor: ModelVisitor): Boolean {
        return super.visitSelf(visitor) && visitor.visit(this)
    }

    override fun leaveSelf(visitor: ModelVisitor) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor): Boolean {
        return super.mutableVisitSelf(visitor) && visitor.mutableVisit(this)
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }
}

abstract class MutableFieldModel(override val parent: MutableBaseEntityModel) : MutableElementModel(), FieldModel {
    override fun visitSelf(visitor: ModelVisitor): Boolean {
        return super.visitSelf(visitor) && visitor.visit(this)
    }

    override fun leaveSelf(visitor: ModelVisitor) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor): Boolean {
        return super.mutableVisitSelf(visitor) && visitor.mutableVisit(this)
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }
}

// Scalar fields

abstract class MutableScalarFieldModel(parent: MutableBaseEntityModel) : MutableFieldModel(parent), ScalarFieldModel {
    open fun setValue(value: String): Boolean = false
    open fun setValue(value: BigDecimal): Boolean = false
    open fun setValue(value: Boolean): Boolean = false

    override fun visitSelf(visitor: ModelVisitor): Boolean {
        return super.visitSelf(visitor) && visitor.visit(this)
    }

    override fun leaveSelf(visitor: ModelVisitor) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor): Boolean {
        return super.mutableVisitSelf(visitor) && visitor.mutableVisit(this)
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }
}

class MutablePrimitiveFieldModel(
    override val schema: PrimitiveFieldSchema,
    parent: MutableBaseEntityModel
) : MutableScalarFieldModel(parent), PrimitiveFieldModel {
    override var value: Any
        get() = _value ?: throw IllegalStateException("Value has not been set")
        internal set(value) {
            _value = value
        }
    private var _value: Any? = null

    override fun setValue(value: String): Boolean = setValue(schema.primitive, value) { this.value = it }
    override fun setValue(value: BigDecimal): Boolean = setValue(schema.primitive, value) { this.value = it }
    override fun setValue(value: Boolean): Boolean = setValue(schema.primitive, value) { this.value = it }

    override fun visitSelf(visitor: ModelVisitor): Boolean {
        return super.visitSelf(visitor) && visitor.visit(this)
    }

    override fun leaveSelf(visitor: ModelVisitor) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor): Boolean {
        return super.mutableVisitSelf(visitor) && visitor.mutableVisit(this)
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: ModelVisitor): Boolean {
        if (!super.traverseChildren(visitor)) return false
        if (!visitor.visit(value, schema)) return false
        visitor.leave(value, schema)
        return true
    }

    override fun mutableTraverseChildren(visitor: MutableModelVisitor): Boolean {
        if (!super.mutableTraverseChildren(visitor)) return false
        if (!visitor.mutableVisit(value, schema)) return false
        visitor.mutableLeave(value, schema)
        return true
    }
}

class MutableAliasFieldModel(
    override val schema: AliasFieldSchema,
    parent: MutableBaseEntityModel
) : MutableScalarFieldModel(parent), AliasFieldModel {
    override var value: Any
        get() = _value ?: throw IllegalStateException("Value has not been set")
        internal set(value) {
            _value = value
        }
    private var _value: Any? = null

    override fun setValue(value: String): Boolean =
        setValue(schema.resolvedAlias.primitive, value) { this.value = it }

    override fun setValue(value: BigDecimal): Boolean =
        setValue(schema.resolvedAlias.primitive, value) { this.value = it }

    override fun setValue(value: Boolean): Boolean =
        setValue(schema.resolvedAlias.primitive, value) { this.value = it }

    override fun visitSelf(visitor: ModelVisitor): Boolean {
        return super.visitSelf(visitor) && visitor.visit(this)
    }

    override fun leaveSelf(visitor: ModelVisitor) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor): Boolean {
        return super.mutableVisitSelf(visitor) && visitor.mutableVisit(this)
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: ModelVisitor): Boolean {
        if (!super.traverseChildren(visitor)) return false
        if (!visitor.visit(value, schema)) return false
        visitor.leave(value, schema)
        return true
    }

    override fun mutableTraverseChildren(visitor: MutableModelVisitor): Boolean {
        if (!super.mutableTraverseChildren(visitor)) return false
        if (!visitor.mutableVisit(value, schema)) return false
        visitor.mutableLeave(value, schema)
        return true
    }
}

class MutableEnumerationFieldModel(
    override val schema: EnumerationFieldSchema,
    parent: MutableBaseEntityModel
) : MutableScalarFieldModel(parent), EnumerationFieldModel {
    override var value: EnumerationValueSchema
        get() = _value ?: throw IllegalStateException("Value has not been set")
        internal set(value) {
            _value = value
        }
    private var _value: EnumerationValueSchema? = null

    override fun setValue(value: String): Boolean = setValue(schema.resolvedEnumeration, value) { this.value = it }

    override fun visitSelf(visitor: ModelVisitor): Boolean {
        return super.visitSelf(visitor) && visitor.visit(this)
    }

    override fun leaveSelf(visitor: ModelVisitor) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor): Boolean {
        return super.mutableVisitSelf(visitor) && visitor.mutableVisit(this)
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: ModelVisitor): Boolean {
        if (!super.traverseChildren(visitor)) return false
        if (!visitor.visit(value, schema)) return false
        visitor.leave(value, schema)
        return true
    }

    override fun mutableTraverseChildren(visitor: MutableModelVisitor): Boolean {
        if (!super.mutableTraverseChildren(visitor)) return false
        if (!visitor.mutableVisit(value, schema)) return false
        visitor.mutableLeave(value, schema)
        return true
    }
}

class MutableAssociationFieldModel(
    override val schema: AssociationFieldSchema,
    parent: MutableBaseEntityModel
) : MutableScalarFieldModel(parent), AssociationFieldModel {
    override var value = MutableAssociationValueModel(schema)
        internal set

    override fun visitSelf(visitor: ModelVisitor): Boolean {
        return super.visitSelf(visitor) && visitor.visit(this)
    }

    override fun leaveSelf(visitor: ModelVisitor) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor): Boolean {
        return super.mutableVisitSelf(visitor) && visitor.mutableVisit(this)
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: ModelVisitor): Boolean {
        if (!super.traverseChildren(visitor)) return false
        if (!value.accept(visitor)) return false
        return true
    }

    override fun mutableTraverseChildren(visitor: MutableModelVisitor): Boolean {
        if (!super.mutableTraverseChildren(visitor)) return false
        if (!value.mutableAccept(visitor)) return false
        return true
    }
}

class MutableCompositionFieldModel(
    override val schema: CompositionFieldSchema,
    parent: MutableBaseEntityModel
) : MutableScalarFieldModel(parent), CompositionFieldModel {
    override var value: MutableEntityModel = MutableEntityModel(schema.resolvedEntity, this)
        internal set(value) {
            field = value
            field.objectId = schema.name
        }

    init {
        value.objectId = schema.name
    }

    override fun visitSelf(visitor: ModelVisitor): Boolean {
        return super.visitSelf(visitor) && visitor.visit(this)
    }

    override fun leaveSelf(visitor: ModelVisitor) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor): Boolean {
        return super.mutableVisitSelf(visitor) && visitor.mutableVisit(this)
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: ModelVisitor): Boolean {
        if (!super.traverseChildren(visitor)) return false
        return value.accept(visitor)
    }

    override fun mutableTraverseChildren(visitor: MutableModelVisitor): Boolean {
        if (!super.mutableTraverseChildren(visitor)) return false
        return value.mutableAccept(visitor)
    }
}

// List fields

abstract class MutableListFieldModel(parent: MutableBaseEntityModel) : MutableFieldModel(parent), ListFieldModel {
    open fun addValue(value: String): Boolean = false
    open fun addValue(value: BigDecimal): Boolean = false
    open fun addValue(value: Boolean): Boolean = false

    override fun visitSelf(visitor: ModelVisitor): Boolean {
        return super.visitSelf(visitor) && visitor.visit(this)
    }

    override fun leaveSelf(visitor: ModelVisitor) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor): Boolean {
        return super.mutableVisitSelf(visitor) && visitor.mutableVisit(this)
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }
}

class MutablePrimitiveListFieldModel(
    override val schema: PrimitiveFieldSchema,
    parent: MutableBaseEntityModel
) : MutableListFieldModel(parent), PrimitiveListFieldModel {
    override var value: MutableList<Any> = mutableListOf()
        internal set

    override fun addValue(value: String): Boolean = setValue(schema.primitive, value) { this.value.add(it) }
    override fun addValue(value: BigDecimal): Boolean = setValue(schema.primitive, value) { this.value.add(it) }
    override fun addValue(value: Boolean): Boolean = setValue(schema.primitive, value) { this.value.add(it) }

    override fun visitSelf(visitor: ModelVisitor): Boolean {
        return super.visitSelf(visitor) && visitor.visit(this)
    }

    override fun leaveSelf(visitor: ModelVisitor) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor): Boolean {
        return super.mutableVisitSelf(visitor) && visitor.mutableVisit(this)
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: ModelVisitor): Boolean {
        if (!super.traverseChildren(visitor)) return false
        value.forEach {
            if (!visitor.visit(it, schema)) return false
            visitor.leave(it, schema)
        }
        return true
    }

    override fun mutableTraverseChildren(visitor: MutableModelVisitor): Boolean {
        if (!super.mutableTraverseChildren(visitor)) return false
        value.forEach {
            if (!visitor.mutableVisit(it, schema)) return false
            visitor.mutableLeave(it, schema)
        }
        return true
    }
}

class MutableAliasListFieldModel(
    override val schema: AliasFieldSchema,
    parent: MutableBaseEntityModel
) : MutableListFieldModel(parent), AliasListFieldModel {
    override var value: MutableList<Any> = mutableListOf()
        internal set

    override fun addValue(value: String): Boolean =
        setValue(schema.resolvedAlias.primitive, value) { this.value.add(it) }

    override fun addValue(value: BigDecimal): Boolean =
        setValue(schema.resolvedAlias.primitive, value) { this.value.add(it) }

    override fun addValue(value: Boolean): Boolean =
        setValue(schema.resolvedAlias.primitive, value) { this.value.add(it) }

    override fun visitSelf(visitor: ModelVisitor): Boolean {
        return super.visitSelf(visitor) && visitor.visit(this)
    }

    override fun leaveSelf(visitor: ModelVisitor) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor): Boolean {
        return super.mutableVisitSelf(visitor) && visitor.mutableVisit(this)
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: ModelVisitor): Boolean {
        if (!super.traverseChildren(visitor)) return false
        value.forEach {
            if (!visitor.visit(it, schema)) return false
            visitor.leave(it, schema)
        }
        return true
    }

    override fun mutableTraverseChildren(visitor: MutableModelVisitor): Boolean {
        if (!super.mutableTraverseChildren(visitor)) return false
        value.forEach {
            if (!visitor.mutableVisit(it, schema)) return false
            visitor.mutableLeave(it, schema)
        }
        return true
    }
}

class MutableEnumerationListFieldModel(
    override val schema: EnumerationFieldSchema,
    parent: MutableBaseEntityModel
) : MutableListFieldModel(parent), EnumerationListFieldModel {
    override var value: MutableList<EnumerationValueSchema> = mutableListOf()
        internal set

    override fun addValue(value: String): Boolean = setValue(schema.resolvedEnumeration, value) { this.value.add(it) }

    override fun visitSelf(visitor: ModelVisitor): Boolean {
        return super.visitSelf(visitor) && visitor.visit(this)
    }

    override fun leaveSelf(visitor: ModelVisitor) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor): Boolean {
        return super.mutableVisitSelf(visitor) && visitor.mutableVisit(this)
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: ModelVisitor): Boolean {
        if (!super.traverseChildren(visitor)) return false
        value.forEach {
            if (!visitor.visit(it, schema)) return false
            visitor.leave(it, schema)
        }
        return true
    }

    override fun mutableTraverseChildren(visitor: MutableModelVisitor): Boolean {
        if (!super.mutableTraverseChildren(visitor)) return false
        value.forEach {
            if (!visitor.mutableVisit(it, schema)) return false
            visitor.mutableLeave(it, schema)
        }
        return true
    }
}

class MutableAssociationListFieldModel(
    override val schema: AssociationFieldSchema,
    parent: MutableBaseEntityModel
) : MutableListFieldModel(parent), AssociationListFieldModel {
    override var value: MutableList<MutableAssociationValueModel> = mutableListOf()
        internal set

    fun addAssociation(): MutableAssociationValueModel {
        val association = MutableAssociationValueModel(schema)
        value.add(association)
        return association
    }

    override fun visitSelf(visitor: ModelVisitor): Boolean {
        return super.visitSelf(visitor) && visitor.visit(this)
    }

    override fun leaveSelf(visitor: ModelVisitor) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor): Boolean {
        return super.mutableVisitSelf(visitor) && visitor.mutableVisit(this)
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: ModelVisitor): Boolean {
        if (!super.traverseChildren(visitor)) return false
        value.forEach {
            if (!visitor.visit(it, schema)) return false
            visitor.leave(it, schema)
        }
        return true
    }

    override fun mutableTraverseChildren(visitor: MutableModelVisitor): Boolean {
        if (!super.mutableTraverseChildren(visitor)) return false
        value.forEach {
            if (!visitor.mutableVisit(it, schema)) return false
            visitor.mutableLeave(it, schema)
        }
        return true
    }
}

class MutableCompositionListFieldModel(
    override val schema: CompositionFieldSchema,
    parent: MutableBaseEntityModel
) : MutableListFieldModel(parent), CompositionListFieldModel {
    override var value: MutableList<MutableEntityModel> = mutableListOf()
        internal set

    fun addEntity(): MutableEntityModel {
        val entity = MutableEntityModel(schema.resolvedEntity, this)
        value.add(entity)
        return entity
    }

    override fun visitSelf(visitor: ModelVisitor): Boolean {
        return super.visitSelf(visitor) && visitor.visit(this)
    }

    override fun leaveSelf(visitor: ModelVisitor) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor): Boolean {
        return super.mutableVisitSelf(visitor) && visitor.mutableVisit(this)
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: ModelVisitor): Boolean {
        if (!super.traverseChildren(visitor)) return false
        value.forEach {
            if (!it.accept(visitor)) return false
        }
        return true
    }

    override fun mutableTraverseChildren(visitor: MutableModelVisitor): Boolean {
        if (!super.mutableTraverseChildren(visitor)) return false
        value.forEach {
            if (!it.mutableAccept(visitor)) return false
        }
        return true
    }
}

// Field values

class MutableAssociationValueModel(
    override val schema: AssociationFieldSchema
) : MutableElementModel(), AssociationValueModel {
    override val parent: MutableElementModel? = null

    override var pathKeys: List<MutableEntityKeysModel> = schema.keyEntities.map { MutableEntityKeysModel(it) }
        internal set

    override fun visitSelf(visitor: ModelVisitor): Boolean {
        return super.visitSelf(visitor) && visitor.visit(this, schema)
    }

    override fun leaveSelf(visitor: ModelVisitor) {
        visitor.leave(this, schema)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor): Boolean {
        return super.mutableVisitSelf(visitor) && visitor.mutableVisit(this, schema)
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor) {
        visitor.mutableLeave(this, schema)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: ModelVisitor): Boolean {
        if (!super.traverseChildren(visitor)) return false
        pathKeys.forEach {
            if (!it.accept(visitor)) return false
        }
        return true
    }

    override fun mutableTraverseChildren(visitor: MutableModelVisitor): Boolean {
        if (!super.mutableTraverseChildren(visitor)) return false
        pathKeys.forEach {
            if (!it.mutableAccept(visitor)) return false
        }
        return true
    }
}

class MutableEntityKeysModel(
    override val schema: EntitySchema
) : MutableBaseEntityModel(schema), EntityKeysModel {
    override val parent: ElementModel? = null

    override fun visitSelf(visitor: ModelVisitor): Boolean {
        return super.visitSelf(visitor) && visitor.visit(this)
    }

    override fun leaveSelf(visitor: ModelVisitor) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor): Boolean {
        return super.mutableVisitSelf(visitor) && visitor.mutableVisit(this)
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }
}

typealias ValueSetter = (Any) -> Unit

fun setValue(primitive: PrimitiveSchema, value: String, setter: ValueSetter): Boolean {
    return when (primitive) {
        is StringSchema,
        is UuidSchema -> {
            setter(value)
            true
        }
        is Password1WaySchema,
        is Password2WaySchema,
        is BlobSchema -> {
            // TODO(deepak-nulu): special handling for Password1WaySchema, Password2WaySchema, BlobSchema
            setter(value)
            true
        }
        // 64-bit integers are encoded as strings because JavaScript integers are only 53-bits
        is LongSchema,
        is TimestampSchema ->
            try {
                setter(value.toLong())
                true
            } catch (e: NumberFormatException) {
                false
            }
        else -> false
    }
}

fun setValue(primitive: PrimitiveSchema, value: BigDecimal, setter: ValueSetter): Boolean {
    return when (primitive) {
        is ByteSchema ->
            try {
                setter(value.toByte())
                true
            } catch (e: java.lang.NumberFormatException) {
                false
            }
        is ShortSchema ->
            try {
                setter(value.toShort())
                true
            } catch (e: java.lang.NumberFormatException) {
                false
            }
        is IntSchema ->
            try {
                setter(value.toInt())
                true
            } catch (e: java.lang.NumberFormatException) {
                false
            }
        is FloatSchema ->
            try {
                setter(value.toFloat())
                true
            } catch (e: java.lang.NumberFormatException) {
                false
            }
        is DoubleSchema ->
            try {
                setter(value.toDouble())
                true
            } catch (e: java.lang.NumberFormatException) {
                false
            }
        else -> false
    }
}

fun setValue(primitive: PrimitiveSchema, value: Boolean, setter: ValueSetter): Boolean {
    return when (primitive) {
        is BooleanSchema -> {
            setter(value)
            true
        }
        else -> false
    }
}

typealias EnumerationSetter = (EnumerationValueSchema) -> Unit

fun setValue(enumeration: EnumerationSchema, value: String, setter: EnumerationSetter): Boolean {
    val enumerationValue = enumeration.valueFromString(value) ?: return false
    setter(enumerationValue)
    return true
}
