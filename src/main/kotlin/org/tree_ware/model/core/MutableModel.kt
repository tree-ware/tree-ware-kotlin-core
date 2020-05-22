package org.tree_ware.model.core

import org.tree_ware.schema.core.*
import java.math.BigDecimal

interface VisitableMutableModel {
    /**
     * Traverses the model element and visits it and its sub-elements (Visitor Pattern).
     * Traversal continues or aborts (partially or fully) based on the value returned by the visitor.
     */
    fun mutableTraverse(visitor: MutableModelVisitor<SchemaTraversalAction>): SchemaTraversalAction

    /**
     * Visits the model element without traversing its sub-elements.
     * Leave methods are NOT called.
     * Returns what the visitor returns.
     */
    fun <T> mutableDispatch(visitor: MutableModelVisitor<T>): T
}

abstract class MutableElementModel : ElementModel, VisitableMutableModel {
    var objectId = ""

    override fun traverse(visitor: ModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        try {
            val action = visitSelf(visitor)
            if (action == SchemaTraversalAction.ABORT_TREE) return SchemaTraversalAction.ABORT_TREE
            if (action == SchemaTraversalAction.ABORT_SUB_TREE) return SchemaTraversalAction.CONTINUE
            return traverseChildren(visitor)
        } finally {
            leaveSelf(visitor)
        }
    }

    override fun mutableTraverse(visitor: MutableModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        try {
            val action = mutableVisitSelf(visitor)
            if (action == SchemaTraversalAction.ABORT_TREE) return SchemaTraversalAction.ABORT_TREE
            if (action == SchemaTraversalAction.ABORT_SUB_TREE) return SchemaTraversalAction.CONTINUE
            return mutableTraverseChildren(visitor)
        } finally {
            mutableLeaveSelf(visitor)
        }
    }

    override fun <T> dispatch(visitor: ModelVisitor<T>): T {
        return visitor.visit(this)
    }

    override fun <T> mutableDispatch(visitor: MutableModelVisitor<T>): T {
        return visitor.mutableVisit(this)
    }

    // NOTE: call super.visitSelf() FIRST when overriding this method
    protected open fun visitSelf(visitor: ModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return visitor.visit(this)
    }

    // NOTE: call super.leaveSelf() LAST when overriding this method
    protected open fun leaveSelf(visitor: ModelVisitor<SchemaTraversalAction>) {
        visitor.leave(this)
    }

    // NOTE: call super.mutableVisitSelf() FIRST when overriding this method
    protected open fun mutableVisitSelf(visitor: MutableModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return visitor.mutableVisit(this)
    }

    // NOTE: call super.mutableLeaveSelf() LAST when overriding this method
    protected open fun mutableLeaveSelf(visitor: MutableModelVisitor<SchemaTraversalAction>) {
        visitor.mutableLeave(this)
    }

    protected open fun traverseChildren(visitor: ModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    protected open fun mutableTraverseChildren(visitor: MutableModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }
}

class MutableModel(override val schema: Schema) : MutableElementModel(), Model {
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
        if (_root == null) _root = newMutableModel(schema.root, this) as MutableRootModel
        return root
    }

    override fun <T> dispatch(visitor: ModelVisitor<T>): T {
        return visitor.visit(this)
    }

    override fun <T> mutableDispatch(visitor: MutableModelVisitor<T>): T {
        return visitor.mutableVisit(this)
    }

    override fun visitSelf(visitor: ModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: ModelVisitor<SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor<SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: ModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.traverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        root.also {
            val action = it.traverse(visitor)
            if (action == SchemaTraversalAction.ABORT_TREE) return action
        }

        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableTraverseChildren(visitor: MutableModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.mutableTraverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        root.also {
            val action = it.mutableTraverse(visitor)
            if (action == SchemaTraversalAction.ABORT_TREE) return action
        }

        return SchemaTraversalAction.CONTINUE
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

    override fun <T> dispatch(visitor: ModelVisitor<T>): T {
        return visitor.visit(this)
    }

    override fun <T> mutableDispatch(visitor: MutableModelVisitor<T>): T {
        return visitor.mutableVisit(this)
    }

    override fun visitSelf(visitor: ModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: ModelVisitor<SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor<SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: ModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.traverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        for (field in fields) {
            val action = field.traverse(visitor)
            if (action == SchemaTraversalAction.ABORT_TREE) return action
        }

        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableTraverseChildren(visitor: MutableModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.mutableTraverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        for (field in fields) {
            val action = field.mutableTraverse(visitor)
            if (action == SchemaTraversalAction.ABORT_TREE) return action
        }

        return SchemaTraversalAction.CONTINUE
    }
}

class MutableRootModel(
    override val schema: RootSchema,
    override val parent: MutableModel
) : MutableBaseEntityModel(schema.resolvedEntity), RootModel {
    override fun <T> dispatch(visitor: ModelVisitor<T>): T {
        return visitor.visit(this)
    }

    override fun <T> mutableDispatch(visitor: MutableModelVisitor<T>): T {
        return visitor.mutableVisit(this)
    }

    override fun visitSelf(visitor: ModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: ModelVisitor<SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor<SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }
}

class MutableEntityModel(
    override val schema: EntitySchema,
    override val parent: MutableFieldModel
) : MutableBaseEntityModel(schema), EntityModel {
    override fun <T> dispatch(visitor: ModelVisitor<T>): T {
        return visitor.visit(this)
    }

    override fun <T> mutableDispatch(visitor: MutableModelVisitor<T>): T {
        return visitor.mutableVisit(this)
    }

    override fun visitSelf(visitor: ModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: ModelVisitor<SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor<SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }
}

abstract class MutableFieldModel(override val parent: MutableBaseEntityModel) : MutableElementModel(), FieldModel {
    override fun <T> dispatch(visitor: ModelVisitor<T>): T {
        return visitor.visit(this)
    }

    override fun <T> mutableDispatch(visitor: MutableModelVisitor<T>): T {
        return visitor.mutableVisit(this)
    }

    override fun visitSelf(visitor: ModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: ModelVisitor<SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor<SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }
}

// Scalar fields

abstract class MutableScalarFieldModel(parent: MutableBaseEntityModel) : MutableFieldModel(parent), ScalarFieldModel {
    open fun setNullValue(): Boolean = false
    open fun setValue(value: String): Boolean = false
    open fun setValue(value: BigDecimal): Boolean = false
    open fun setValue(value: Boolean): Boolean = false

    override fun <T> dispatch(visitor: ModelVisitor<T>): T {
        return visitor.visit(this)
    }

    override fun <T> mutableDispatch(visitor: MutableModelVisitor<T>): T {
        return visitor.mutableVisit(this)
    }

    override fun visitSelf(visitor: ModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: ModelVisitor<SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor<SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }
}

class MutablePrimitiveFieldModel(
    override val schema: PrimitiveFieldSchema,
    parent: MutableBaseEntityModel
) : MutableScalarFieldModel(parent), PrimitiveFieldModel {
    override var value: Any?
        get() = _value
        internal set(value) {
            _value = value
        }
    private var _value: Any? = null

    override fun setNullValue(): Boolean {
        this.value = null
        return true
    }

    override fun setValue(value: String): Boolean = setValue(schema.primitive, value) { this.value = it }
    override fun setValue(value: BigDecimal): Boolean = setValue(schema.primitive, value) { this.value = it }
    override fun setValue(value: Boolean): Boolean = setValue(schema.primitive, value) { this.value = it }

    override fun <T> dispatch(visitor: ModelVisitor<T>): T {
        return visitor.visit(this)
    }

    override fun <T> mutableDispatch(visitor: MutableModelVisitor<T>): T {
        return visitor.mutableVisit(this)
    }

    override fun visitSelf(visitor: ModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: ModelVisitor<SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor<SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: ModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.traverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        val valueAction = visitor.visit(value, schema)
        if (valueAction == SchemaTraversalAction.ABORT_TREE) return valueAction
        visitor.leave(value, schema)

        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableTraverseChildren(visitor: MutableModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.mutableTraverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        val valueAction = visitor.mutableVisit(value, schema)
        if (valueAction == SchemaTraversalAction.ABORT_TREE) return valueAction
        visitor.mutableLeave(value, schema)

        return SchemaTraversalAction.CONTINUE
    }
}

class MutableAliasFieldModel(
    override val schema: AliasFieldSchema,
    parent: MutableBaseEntityModel
) : MutableScalarFieldModel(parent), AliasFieldModel {
    override var value: Any?
        get() = _value
        internal set(value) {
            _value = value
        }
    private var _value: Any? = null

    override fun setNullValue(): Boolean {
        this.value = null
        return true
    }

    override fun setValue(value: String): Boolean =
        setValue(schema.resolvedAlias.primitive, value) { this.value = it }

    override fun setValue(value: BigDecimal): Boolean =
        setValue(schema.resolvedAlias.primitive, value) { this.value = it }

    override fun setValue(value: Boolean): Boolean =
        setValue(schema.resolvedAlias.primitive, value) { this.value = it }

    override fun <T> dispatch(visitor: ModelVisitor<T>): T {
        return visitor.visit(this)
    }

    override fun <T> mutableDispatch(visitor: MutableModelVisitor<T>): T {
        return visitor.mutableVisit(this)
    }

    override fun visitSelf(visitor: ModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: ModelVisitor<SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor<SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: ModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.traverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        val valueAction = visitor.visit(value, schema)
        if (valueAction == SchemaTraversalAction.ABORT_TREE) return valueAction
        visitor.leave(value, schema)

        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableTraverseChildren(visitor: MutableModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.mutableTraverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        val valueAction = visitor.mutableVisit(value, schema)
        if (valueAction == SchemaTraversalAction.ABORT_TREE) return valueAction
        visitor.mutableLeave(value, schema)

        return SchemaTraversalAction.CONTINUE
    }
}

class MutableEnumerationFieldModel(
    override val schema: EnumerationFieldSchema,
    parent: MutableBaseEntityModel
) : MutableScalarFieldModel(parent), EnumerationFieldModel {
    override var value: EnumerationValueSchema?
        get() = _value
        internal set(value) {
            _value = value
        }
    private var _value: EnumerationValueSchema? = null

    override fun setNullValue(): Boolean {
        this.value = null
        return true
    }

    override fun setValue(value: String): Boolean = setValue(schema.resolvedEnumeration, value) { this.value = it }

    override fun <T> dispatch(visitor: ModelVisitor<T>): T {
        return visitor.visit(this)
    }

    override fun <T> mutableDispatch(visitor: MutableModelVisitor<T>): T {
        return visitor.mutableVisit(this)
    }

    override fun visitSelf(visitor: ModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: ModelVisitor<SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor<SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: ModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.traverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        val valueAction = visitor.visit(value, schema)
        if (valueAction == SchemaTraversalAction.ABORT_TREE) return valueAction
        visitor.leave(value, schema)

        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableTraverseChildren(visitor: MutableModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.mutableTraverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        val valueAction = visitor.mutableVisit(value, schema)
        if (valueAction == SchemaTraversalAction.ABORT_TREE) return valueAction
        visitor.mutableLeave(value, schema)

        return SchemaTraversalAction.CONTINUE
    }
}

class MutableAssociationFieldModel(
    override val schema: AssociationFieldSchema,
    parent: MutableBaseEntityModel
) : MutableScalarFieldModel(parent), AssociationFieldModel {
    override var value: MutableAssociationValueModel? = null
        internal set

    override fun setNullValue(): Boolean {
        this.value = null
        return true
    }

    override fun <T> dispatch(visitor: ModelVisitor<T>): T {
        return visitor.visit(this)
    }

    override fun <T> mutableDispatch(visitor: MutableModelVisitor<T>): T {
        return visitor.mutableVisit(this)
    }

    override fun visitSelf(visitor: ModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: ModelVisitor<SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor<SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: ModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.traverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        val valueAction = value?.traverse(visitor)
        if (valueAction == SchemaTraversalAction.ABORT_TREE) return valueAction

        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableTraverseChildren(visitor: MutableModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.mutableTraverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        val valueAction = value?.mutableTraverse(visitor)
        if (valueAction == SchemaTraversalAction.ABORT_TREE) return valueAction

        return SchemaTraversalAction.CONTINUE
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

    override fun <T> dispatch(visitor: ModelVisitor<T>): T {
        return visitor.visit(this)
    }

    override fun <T> mutableDispatch(visitor: MutableModelVisitor<T>): T {
        return visitor.mutableVisit(this)
    }

    override fun visitSelf(visitor: ModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: ModelVisitor<SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor<SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: ModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.traverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        val valueAction = value.traverse(visitor)
        if (valueAction == SchemaTraversalAction.ABORT_TREE) return valueAction

        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableTraverseChildren(visitor: MutableModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.mutableTraverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        val valueAction = value.mutableTraverse(visitor)
        if (valueAction == SchemaTraversalAction.ABORT_TREE) return valueAction

        return SchemaTraversalAction.CONTINUE
    }
}

// List fields

abstract class MutableListFieldModel(parent: MutableBaseEntityModel) : MutableFieldModel(parent), ListFieldModel {
    open fun addValue(value: String): Boolean = false
    open fun addValue(value: BigDecimal): Boolean = false
    open fun addValue(value: Boolean): Boolean = false

    override fun <T> dispatch(visitor: ModelVisitor<T>): T {
        return visitor.visit(this)
    }

    override fun <T> mutableDispatch(visitor: MutableModelVisitor<T>): T {
        return visitor.mutableVisit(this)
    }

    override fun visitSelf(visitor: ModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: ModelVisitor<SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor<SchemaTraversalAction>) {
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

    override fun <T> dispatch(visitor: ModelVisitor<T>): T {
        return visitor.visit(this)
    }

    override fun <T> mutableDispatch(visitor: MutableModelVisitor<T>): T {
        return visitor.mutableVisit(this)
    }

    override fun visitSelf(visitor: ModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: ModelVisitor<SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor<SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: ModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.traverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        value.forEach {
            val action = visitor.visit(it, schema)
            if (action == SchemaTraversalAction.ABORT_TREE) return action
            visitor.leave(it, schema)
        }

        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableTraverseChildren(visitor: MutableModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.mutableTraverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        value.forEach {
            val action = visitor.mutableVisit(it, schema)
            if (action == SchemaTraversalAction.ABORT_TREE) return action
            visitor.mutableLeave(it, schema)
        }

        return SchemaTraversalAction.CONTINUE
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

    override fun <T> dispatch(visitor: ModelVisitor<T>): T {
        return visitor.visit(this)
    }

    override fun <T> mutableDispatch(visitor: MutableModelVisitor<T>): T {
        return visitor.mutableVisit(this)
    }

    override fun visitSelf(visitor: ModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: ModelVisitor<SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor<SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: ModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.traverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        value.forEach {
            val action = visitor.visit(it, schema)
            if (action == SchemaTraversalAction.ABORT_TREE) return action
            visitor.leave(it, schema)
        }

        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableTraverseChildren(visitor: MutableModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.mutableTraverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        value.forEach {
            val action = visitor.mutableVisit(it, schema)
            if (action == SchemaTraversalAction.ABORT_TREE) return action
            visitor.mutableLeave(it, schema)
        }

        return SchemaTraversalAction.CONTINUE
    }
}

class MutableEnumerationListFieldModel(
    override val schema: EnumerationFieldSchema,
    parent: MutableBaseEntityModel
) : MutableListFieldModel(parent), EnumerationListFieldModel {
    override var value: MutableList<EnumerationValueSchema> = mutableListOf()
        internal set

    override fun addValue(value: String): Boolean = setValue(schema.resolvedEnumeration, value) { this.value.add(it) }

    override fun <T> dispatch(visitor: ModelVisitor<T>): T {
        return visitor.visit(this)
    }

    override fun <T> mutableDispatch(visitor: MutableModelVisitor<T>): T {
        return visitor.mutableVisit(this)
    }

    override fun visitSelf(visitor: ModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: ModelVisitor<SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor<SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: ModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.traverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        value.forEach {
            val action = visitor.visit(it, schema)
            if (action == SchemaTraversalAction.ABORT_TREE) return action
            visitor.leave(it, schema)
        }

        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableTraverseChildren(visitor: MutableModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.mutableTraverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        value.forEach {
            val action = visitor.mutableVisit(it, schema)
            if (action == SchemaTraversalAction.ABORT_TREE) return action
            visitor.mutableLeave(it, schema)
        }

        return SchemaTraversalAction.CONTINUE
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

    override fun <T> dispatch(visitor: ModelVisitor<T>): T {
        return visitor.visit(this)
    }

    override fun <T> mutableDispatch(visitor: MutableModelVisitor<T>): T {
        return visitor.mutableVisit(this)
    }

    override fun visitSelf(visitor: ModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: ModelVisitor<SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor<SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: ModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.traverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        value.forEach {
            val action = visitor.visit(it, schema)
            if (action == SchemaTraversalAction.ABORT_TREE) return action
            visitor.leave(it, schema)
        }

        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableTraverseChildren(visitor: MutableModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.mutableTraverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        value.forEach {
            val action = visitor.mutableVisit(it, schema)
            if (action == SchemaTraversalAction.ABORT_TREE) return action
            visitor.mutableLeave(it, schema)
        }

        return SchemaTraversalAction.CONTINUE
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

    override fun <T> dispatch(visitor: ModelVisitor<T>): T {
        return visitor.visit(this)
    }

    override fun <T> mutableDispatch(visitor: MutableModelVisitor<T>): T {
        return visitor.mutableVisit(this)
    }

    override fun visitSelf(visitor: ModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: ModelVisitor<SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor<SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: ModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.traverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        value.forEach {
            val action = it.traverse(visitor)
            if (action == SchemaTraversalAction.ABORT_TREE) return action
        }

        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableTraverseChildren(visitor: MutableModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.mutableTraverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        value.forEach {
            val action = it.mutableTraverse(visitor)
            if (action == SchemaTraversalAction.ABORT_TREE) return action
        }

        return SchemaTraversalAction.CONTINUE
    }
}

// Field values

class MutableAssociationValueModel(
    override val schema: AssociationFieldSchema
) : MutableElementModel(), AssociationValueModel {
    override val parent: MutableElementModel? = null

    override var pathKeys: List<MutableEntityKeysModel> = schema.keyEntities.map { MutableEntityKeysModel(it) }
        internal set

    override fun <T> dispatch(visitor: ModelVisitor<T>): T {
        return visitor.visit(this)
    }

    override fun <T> mutableDispatch(visitor: MutableModelVisitor<T>): T {
        return visitor.mutableVisit(this)
    }

    override fun visitSelf(visitor: ModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this, schema))
    }

    override fun leaveSelf(visitor: ModelVisitor<SchemaTraversalAction>) {
        visitor.leave(this, schema)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this, schema))
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor<SchemaTraversalAction>) {
        visitor.mutableLeave(this, schema)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: ModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.traverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        pathKeys.forEach {
            val action = it.traverse(visitor)
            if (action == SchemaTraversalAction.ABORT_TREE) return action
        }

        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableTraverseChildren(visitor: MutableModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.mutableTraverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        pathKeys.forEach {
            val action = it.mutableTraverse(visitor)
            if (action == SchemaTraversalAction.ABORT_TREE) return action
        }

        return SchemaTraversalAction.CONTINUE
    }
}

class MutableEntityKeysModel(
    override val schema: EntitySchema
) : MutableBaseEntityModel(schema), EntityKeysModel {
    override val parent: ElementModel? = null

    override fun <T> dispatch(visitor: ModelVisitor<T>): T {
        return visitor.visit(this)
    }

    override fun <T> mutableDispatch(visitor: MutableModelVisitor<T>): T {
        return visitor.mutableVisit(this)
    }

    override fun visitSelf(visitor: ModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: ModelVisitor<SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor<SchemaTraversalAction>) {
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
