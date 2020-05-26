package org.tree_ware.model.core

import org.tree_ware.schema.core.*
import java.math.BigDecimal

interface VisitableMutableModel<Aux> {
    /**
     * Traverses the model element and visits it and its sub-elements (Visitor Pattern).
     * Traversal continues or aborts (partially or fully) based on the value returned by the visitor.
     */
    fun mutableTraverse(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction

    /**
     * Visits the model element without traversing its sub-elements.
     * Leave methods are NOT called.
     * Returns what the visitor returns.
     */
    fun <Return> mutableDispatch(visitor: MutableModelVisitor<Aux, Return>): Return
}

abstract class MutableElementModel<Aux> : ElementModel<Aux>, VisitableMutableModel<Aux> {
    var objectId = ""

    override var aux: Aux? = null
        internal set

    override fun traverse(visitor: ModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        try {
            val action = visitSelf(visitor)
            if (action == SchemaTraversalAction.ABORT_TREE) return SchemaTraversalAction.ABORT_TREE
            if (action == SchemaTraversalAction.ABORT_SUB_TREE) return SchemaTraversalAction.CONTINUE
            return traverseChildren(visitor)
        } finally {
            leaveSelf(visitor)
        }
    }

    override fun mutableTraverse(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        try {
            val action = mutableVisitSelf(visitor)
            if (action == SchemaTraversalAction.ABORT_TREE) return SchemaTraversalAction.ABORT_TREE
            if (action == SchemaTraversalAction.ABORT_SUB_TREE) return SchemaTraversalAction.CONTINUE
            return mutableTraverseChildren(visitor)
        } finally {
            mutableLeaveSelf(visitor)
        }
    }

    override fun <Return> dispatch(visitor: ModelVisitor<Aux, Return>): Return {
        return visitor.visit(this)
    }

    override fun <Return> mutableDispatch(visitor: MutableModelVisitor<Aux, Return>): Return {
        return visitor.mutableVisit(this)
    }

    // NOTE: call super.visitSelf() FIRST when overriding this method
    protected open fun visitSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return visitor.visit(this)
    }

    // NOTE: call super.leaveSelf() LAST when overriding this method
    protected open fun leaveSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>) {
        visitor.leave(this)
    }

    // NOTE: call super.mutableVisitSelf() FIRST when overriding this method
    protected open fun mutableVisitSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return visitor.mutableVisit(this)
    }

    // NOTE: call super.mutableLeaveSelf() LAST when overriding this method
    protected open fun mutableLeaveSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>) {
        visitor.mutableLeave(this)
    }

    protected open fun traverseChildren(visitor: ModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    protected open fun mutableTraverseChildren(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }
}

class MutableModel<Aux>(override val schema: Schema) : MutableElementModel<Aux>(), Model<Aux> {
    override val parent: ElementModel<Aux>? = null

    override var type = ModelType.data
        internal set(value) {
            field = value
            _root?.also { it.objectId = value.name }
        }

    override var root: MutableRootModel<Aux>
        get() = _root ?: throw IllegalStateException("Root has not been set")
        internal set(value) {
            value.objectId = type.name
            _root = value
        }
    private var _root: MutableRootModel<Aux>? = null

    fun getOrNewRoot(): MutableRootModel<Aux> {
        if (_root == null) _root = newMutableModel(schema.root, this) as MutableRootModel<Aux>
        return root
    }

    override fun <Return> dispatch(visitor: ModelVisitor<Aux, Return>): Return {
        return visitor.visit(this)
    }

    override fun <Return> mutableDispatch(visitor: MutableModelVisitor<Aux, Return>): Return {
        return visitor.mutableVisit(this)
    }

    override fun visitSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: ModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.traverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        root.also {
            val action = it.traverse(visitor)
            if (action == SchemaTraversalAction.ABORT_TREE) return action
        }

        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableTraverseChildren(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.mutableTraverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        root.also {
            val action = it.mutableTraverse(visitor)
            if (action == SchemaTraversalAction.ABORT_TREE) return action
        }

        return SchemaTraversalAction.CONTINUE
    }
}

abstract class MutableBaseEntityModel<Aux>(
    private val entitySchema: EntitySchema
) : MutableElementModel<Aux>(), BaseEntityModel<Aux> {
    override var fields: MutableList<MutableFieldModel<Aux>> = mutableListOf()
        internal set

    fun getOrNewScalarField(fieldName: String): MutableScalarFieldModel<Aux>? {
        val fieldSchema = entitySchema.getField(fieldName) ?: return null
        if (fieldSchema.multiplicity.isList()) return null

        var field = getField(fieldName) as MutableScalarFieldModel<Aux>?
        if (field == null) {
            field = newMutableModel(fieldSchema, this) as MutableScalarFieldModel<Aux>?
            if (field != null) fields.add(field)
        }
        return field
    }

    fun getOrNewCompositionField(fieldName: String): MutableCompositionFieldModel<Aux>? {
        val fieldSchema = entitySchema.getField(fieldName) ?: return null
        if (fieldSchema !is CompositionFieldSchema) return null
        if (fieldSchema.multiplicity.isList()) return null

        var field = getField(fieldName) as MutableCompositionFieldModel<Aux>?
        if (field == null) {
            field = newMutableModel(fieldSchema, this) as MutableCompositionFieldModel<Aux>?
            if (field != null) fields.add(field)
        }
        return field
    }

    fun getOrNewAssociationField(fieldName: String): MutableAssociationFieldModel<Aux>? {
        val fieldSchema = entitySchema.getField(fieldName) ?: return null
        if (fieldSchema !is AssociationFieldSchema) return null
        if (fieldSchema.multiplicity.isList()) return null

        var field = getField(fieldName) as MutableAssociationFieldModel<Aux>?
        if (field == null) {
            field = newMutableModel(fieldSchema, this) as MutableAssociationFieldModel<Aux>?
            if (field != null) fields.add(field)
        }
        return field
    }

    fun getOrNewListField(fieldName: String): MutableListFieldModel<Aux>? {
        val fieldSchema = entitySchema.getField(fieldName) ?: return null
        if (!fieldSchema.multiplicity.isList()) return null

        var field = getField(fieldName) as MutableListFieldModel<Aux>?
        if (field == null) {
            field = newMutableModel(fieldSchema, this) as MutableListFieldModel<Aux>?
            if (field != null) fields.add(field)
        }
        return field
    }

    // TODO(deepak-nulu): optimize
    private fun getField(fieldName: String): MutableFieldModel<Aux>? = fields.find { it.schema.name == fieldName }

    override fun <Return> dispatch(visitor: ModelVisitor<Aux, Return>): Return {
        return visitor.visit(this)
    }

    override fun <Return> mutableDispatch(visitor: MutableModelVisitor<Aux, Return>): Return {
        return visitor.mutableVisit(this)
    }

    override fun visitSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: ModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.traverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        for (field in fields) {
            val action = field.traverse(visitor)
            if (action == SchemaTraversalAction.ABORT_TREE) return action
        }

        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableTraverseChildren(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.mutableTraverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        for (field in fields) {
            val action = field.mutableTraverse(visitor)
            if (action == SchemaTraversalAction.ABORT_TREE) return action
        }

        return SchemaTraversalAction.CONTINUE
    }
}

class MutableRootModel<Aux>(
    override val schema: RootSchema,
    override val parent: MutableModel<Aux>
) : MutableBaseEntityModel<Aux>(schema.resolvedEntity), RootModel<Aux> {
    override fun <Return> dispatch(visitor: ModelVisitor<Aux, Return>): Return {
        return visitor.visit(this)
    }

    override fun <Return> mutableDispatch(visitor: MutableModelVisitor<Aux, Return>): Return {
        return visitor.mutableVisit(this)
    }

    override fun visitSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }
}

class MutableEntityModel<Aux>(
    override val schema: EntitySchema,
    override val parent: MutableFieldModel<Aux>
) : MutableBaseEntityModel<Aux>(schema), EntityModel<Aux> {
    override fun <Return> dispatch(visitor: ModelVisitor<Aux, Return>): Return {
        return visitor.visit(this)
    }

    override fun <Return> mutableDispatch(visitor: MutableModelVisitor<Aux, Return>): Return {
        return visitor.mutableVisit(this)
    }

    override fun visitSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }
}

abstract class MutableFieldModel<Aux>(
    override val parent: MutableBaseEntityModel<Aux>
) : MutableElementModel<Aux>(), FieldModel<Aux> {
    override fun <Return> dispatch(visitor: ModelVisitor<Aux, Return>): Return {
        return visitor.visit(this)
    }

    override fun <Return> mutableDispatch(visitor: MutableModelVisitor<Aux, Return>): Return {
        return visitor.mutableVisit(this)
    }

    override fun visitSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }
}

// Scalar fields

abstract class MutableScalarFieldModel<Aux>(
    parent: MutableBaseEntityModel<Aux>
) : MutableFieldModel<Aux>(parent), ScalarFieldModel<Aux> {
    open fun setNullValue(): Boolean = false
    open fun setValue(value: String): Boolean = false
    open fun setValue(value: BigDecimal): Boolean = false
    open fun setValue(value: Boolean): Boolean = false

    override fun <Return> dispatch(visitor: ModelVisitor<Aux, Return>): Return {
        return visitor.visit(this)
    }

    override fun <Return> mutableDispatch(visitor: MutableModelVisitor<Aux, Return>): Return {
        return visitor.mutableVisit(this)
    }

    override fun visitSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }
}

class MutablePrimitiveFieldModel<Aux>(
    override val schema: PrimitiveFieldSchema,
    parent: MutableBaseEntityModel<Aux>
) : MutableScalarFieldModel<Aux>(parent), PrimitiveFieldModel<Aux> {
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

    override fun <Return> dispatch(visitor: ModelVisitor<Aux, Return>): Return {
        return visitor.visit(this)
    }

    override fun <Return> mutableDispatch(visitor: MutableModelVisitor<Aux, Return>): Return {
        return visitor.mutableVisit(this)
    }

    override fun visitSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: ModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.traverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        val valueAction = visitor.visit(value, schema)
        if (valueAction == SchemaTraversalAction.ABORT_TREE) return valueAction
        visitor.leave(value, schema)

        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableTraverseChildren(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.mutableTraverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        val valueAction = visitor.mutableVisit(value, schema)
        if (valueAction == SchemaTraversalAction.ABORT_TREE) return valueAction
        visitor.mutableLeave(value, schema)

        return SchemaTraversalAction.CONTINUE
    }
}

class MutableAliasFieldModel<Aux>(
    override val schema: AliasFieldSchema,
    parent: MutableBaseEntityModel<Aux>
) : MutableScalarFieldModel<Aux>(parent), AliasFieldModel<Aux> {
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

    override fun <Return> dispatch(visitor: ModelVisitor<Aux, Return>): Return {
        return visitor.visit(this)
    }

    override fun <Return> mutableDispatch(visitor: MutableModelVisitor<Aux, Return>): Return {
        return visitor.mutableVisit(this)
    }

    override fun visitSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: ModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.traverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        val valueAction = visitor.visit(value, schema)
        if (valueAction == SchemaTraversalAction.ABORT_TREE) return valueAction
        visitor.leave(value, schema)

        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableTraverseChildren(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.mutableTraverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        val valueAction = visitor.mutableVisit(value, schema)
        if (valueAction == SchemaTraversalAction.ABORT_TREE) return valueAction
        visitor.mutableLeave(value, schema)

        return SchemaTraversalAction.CONTINUE
    }
}

class MutableEnumerationFieldModel<Aux>(
    override val schema: EnumerationFieldSchema,
    parent: MutableBaseEntityModel<Aux>
) : MutableScalarFieldModel<Aux>(parent), EnumerationFieldModel<Aux> {
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

    override fun <Return> dispatch(visitor: ModelVisitor<Aux, Return>): Return {
        return visitor.visit(this)
    }

    override fun <Return> mutableDispatch(visitor: MutableModelVisitor<Aux, Return>): Return {
        return visitor.mutableVisit(this)
    }

    override fun visitSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: ModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.traverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        val valueAction = visitor.visit(value, schema)
        if (valueAction == SchemaTraversalAction.ABORT_TREE) return valueAction
        visitor.leave(value, schema)

        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableTraverseChildren(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.mutableTraverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        val valueAction = visitor.mutableVisit(value, schema)
        if (valueAction == SchemaTraversalAction.ABORT_TREE) return valueAction
        visitor.mutableLeave(value, schema)

        return SchemaTraversalAction.CONTINUE
    }
}

class MutableAssociationFieldModel<Aux>(
    override val schema: AssociationFieldSchema,
    parent: MutableBaseEntityModel<Aux>
) : MutableScalarFieldModel<Aux>(parent), AssociationFieldModel<Aux> {
    override var value: MutableAssociationValueModel<Aux>? = null
        internal set

    override fun setNullValue(): Boolean {
        this.value = null
        return true
    }

    override fun <Return> dispatch(visitor: ModelVisitor<Aux, Return>): Return {
        return visitor.visit(this)
    }

    override fun <Return> mutableDispatch(visitor: MutableModelVisitor<Aux, Return>): Return {
        return visitor.mutableVisit(this)
    }

    override fun visitSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: ModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.traverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        val valueAction = value?.traverse(visitor)
        if (valueAction == SchemaTraversalAction.ABORT_TREE) return valueAction

        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableTraverseChildren(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.mutableTraverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        val valueAction = value?.mutableTraverse(visitor)
        if (valueAction == SchemaTraversalAction.ABORT_TREE) return valueAction

        return SchemaTraversalAction.CONTINUE
    }
}

class MutableCompositionFieldModel<Aux>(
    override val schema: CompositionFieldSchema,
    parent: MutableBaseEntityModel<Aux>
) : MutableScalarFieldModel<Aux>(parent), CompositionFieldModel<Aux> {
    override var value: MutableEntityModel<Aux> = MutableEntityModel(schema.resolvedEntity, this)
        internal set(value) {
            field = value
            field.objectId = schema.name
        }

    init {
        value.objectId = schema.name
    }

    override fun <Return> dispatch(visitor: ModelVisitor<Aux, Return>): Return {
        return visitor.visit(this)
    }

    override fun <Return> mutableDispatch(visitor: MutableModelVisitor<Aux, Return>): Return {
        return visitor.mutableVisit(this)
    }

    override fun visitSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: ModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.traverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        val valueAction = value.traverse(visitor)
        if (valueAction == SchemaTraversalAction.ABORT_TREE) return valueAction

        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableTraverseChildren(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.mutableTraverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        val valueAction = value.mutableTraverse(visitor)
        if (valueAction == SchemaTraversalAction.ABORT_TREE) return valueAction

        return SchemaTraversalAction.CONTINUE
    }
}

// List fields

abstract class MutableListFieldModel<Aux>(
    parent: MutableBaseEntityModel<Aux>
) : MutableFieldModel<Aux>(parent), ListFieldModel<Aux> {
    open fun addValue(value: String): Boolean = false
    open fun addValue(value: BigDecimal): Boolean = false
    open fun addValue(value: Boolean): Boolean = false

    override fun <Return> dispatch(visitor: ModelVisitor<Aux, Return>): Return {
        return visitor.visit(this)
    }

    override fun <Return> mutableDispatch(visitor: MutableModelVisitor<Aux, Return>): Return {
        return visitor.mutableVisit(this)
    }

    override fun visitSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }
}

class MutablePrimitiveListFieldModel<Aux>(
    override val schema: PrimitiveFieldSchema,
    parent: MutableBaseEntityModel<Aux>
) : MutableListFieldModel<Aux>(parent), PrimitiveListFieldModel<Aux> {
    override var value: MutableList<Any> = mutableListOf()
        internal set

    override fun addValue(value: String): Boolean = setValue(schema.primitive, value) { this.value.add(it) }
    override fun addValue(value: BigDecimal): Boolean = setValue(schema.primitive, value) { this.value.add(it) }
    override fun addValue(value: Boolean): Boolean = setValue(schema.primitive, value) { this.value.add(it) }

    override fun <Return> dispatch(visitor: ModelVisitor<Aux, Return>): Return {
        return visitor.visit(this)
    }

    override fun <Return> mutableDispatch(visitor: MutableModelVisitor<Aux, Return>): Return {
        return visitor.mutableVisit(this)
    }

    override fun visitSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: ModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.traverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        value.forEach {
            val action = visitor.visit(it, schema)
            if (action == SchemaTraversalAction.ABORT_TREE) return action
            visitor.leave(it, schema)
        }

        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableTraverseChildren(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
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

class MutableAliasListFieldModel<Aux>(
    override val schema: AliasFieldSchema,
    parent: MutableBaseEntityModel<Aux>
) : MutableListFieldModel<Aux>(parent), AliasListFieldModel<Aux> {
    override var value: MutableList<Any> = mutableListOf()
        internal set

    override fun addValue(value: String): Boolean =
        setValue(schema.resolvedAlias.primitive, value) { this.value.add(it) }

    override fun addValue(value: BigDecimal): Boolean =
        setValue(schema.resolvedAlias.primitive, value) { this.value.add(it) }

    override fun addValue(value: Boolean): Boolean =
        setValue(schema.resolvedAlias.primitive, value) { this.value.add(it) }

    override fun <Return> dispatch(visitor: ModelVisitor<Aux, Return>): Return {
        return visitor.visit(this)
    }

    override fun <Return> mutableDispatch(visitor: MutableModelVisitor<Aux, Return>): Return {
        return visitor.mutableVisit(this)
    }

    override fun visitSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: ModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.traverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        value.forEach {
            val action = visitor.visit(it, schema)
            if (action == SchemaTraversalAction.ABORT_TREE) return action
            visitor.leave(it, schema)
        }

        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableTraverseChildren(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
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

class MutableEnumerationListFieldModel<Aux>(
    override val schema: EnumerationFieldSchema,
    parent: MutableBaseEntityModel<Aux>
) : MutableListFieldModel<Aux>(parent), EnumerationListFieldModel<Aux> {
    override var value: MutableList<EnumerationValueSchema> = mutableListOf()
        internal set

    override fun addValue(value: String): Boolean = setValue(schema.resolvedEnumeration, value) { this.value.add(it) }

    override fun <Return> dispatch(visitor: ModelVisitor<Aux, Return>): Return {
        return visitor.visit(this)
    }

    override fun <Return> mutableDispatch(visitor: MutableModelVisitor<Aux, Return>): Return {
        return visitor.mutableVisit(this)
    }

    override fun visitSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: ModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.traverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        value.forEach {
            val action = visitor.visit(it, schema)
            if (action == SchemaTraversalAction.ABORT_TREE) return action
            visitor.leave(it, schema)
        }

        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableTraverseChildren(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
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

class MutableAssociationListFieldModel<Aux>(
    override val schema: AssociationFieldSchema,
    parent: MutableBaseEntityModel<Aux>
) : MutableListFieldModel<Aux>(parent), AssociationListFieldModel<Aux> {
    override var value: MutableList<MutableAssociationValueModel<Aux>> = mutableListOf()
        internal set

    fun addAssociation(): MutableAssociationValueModel<Aux> {
        val association = MutableAssociationValueModel<Aux>(schema)
        value.add(association)
        return association
    }

    override fun <Return> dispatch(visitor: ModelVisitor<Aux, Return>): Return {
        return visitor.visit(this)
    }

    override fun <Return> mutableDispatch(visitor: MutableModelVisitor<Aux, Return>): Return {
        return visitor.mutableVisit(this)
    }

    override fun visitSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: ModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.traverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        value.forEach {
            val action = visitor.visit(it, schema)
            if (action == SchemaTraversalAction.ABORT_TREE) return action
            visitor.leave(it, schema)
        }

        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableTraverseChildren(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
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

class MutableCompositionListFieldModel<Aux>(
    override val schema: CompositionFieldSchema,
    parent: MutableBaseEntityModel<Aux>
) : MutableListFieldModel<Aux>(parent), CompositionListFieldModel<Aux> {
    override var value: MutableList<MutableEntityModel<Aux>> = mutableListOf()
        internal set

    fun addEntity(): MutableEntityModel<Aux> {
        val entity = MutableEntityModel(schema.resolvedEntity, this)
        value.add(entity)
        return entity
    }

    override fun <Return> dispatch(visitor: ModelVisitor<Aux, Return>): Return {
        return visitor.visit(this)
    }

    override fun <Return> mutableDispatch(visitor: MutableModelVisitor<Aux, Return>): Return {
        return visitor.mutableVisit(this)
    }

    override fun visitSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: ModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.traverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        value.forEach {
            val action = it.traverse(visitor)
            if (action == SchemaTraversalAction.ABORT_TREE) return action
        }

        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableTraverseChildren(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
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

class MutableAssociationValueModel<Aux>(
    override val schema: AssociationFieldSchema
) : MutableElementModel<Aux>(), AssociationValueModel<Aux> {
    override val parent: MutableElementModel<Aux>? = null

    override var pathKeys: List<MutableEntityKeysModel<Aux>> =
        schema.keyEntities.map { MutableEntityKeysModel<Aux>(it) }
        internal set

    override fun <Return> dispatch(visitor: ModelVisitor<Aux, Return>): Return {
        return visitor.visit(this)
    }

    override fun <Return> mutableDispatch(visitor: MutableModelVisitor<Aux, Return>): Return {
        return visitor.mutableVisit(this)
    }

    override fun visitSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this, schema))
    }

    override fun leaveSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>) {
        visitor.leave(this, schema)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this, schema))
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>) {
        visitor.mutableLeave(this, schema)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: ModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.traverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        pathKeys.forEach {
            val action = it.traverse(visitor)
            if (action == SchemaTraversalAction.ABORT_TREE) return action
        }

        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableTraverseChildren(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.mutableTraverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        pathKeys.forEach {
            val action = it.mutableTraverse(visitor)
            if (action == SchemaTraversalAction.ABORT_TREE) return action
        }

        return SchemaTraversalAction.CONTINUE
    }
}

class MutableEntityKeysModel<Aux>(
    override val schema: EntitySchema
) : MutableBaseEntityModel<Aux>(schema), EntityKeysModel<Aux> {
    override val parent: ElementModel<Aux>? = null

    override fun <Return> dispatch(visitor: ModelVisitor<Aux, Return>): Return {
        return visitor.visit(this)
    }

    override fun <Return> mutableDispatch(visitor: MutableModelVisitor<Aux, Return>): Return {
        return visitor.mutableVisit(this)
    }

    override fun visitSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>) {
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
