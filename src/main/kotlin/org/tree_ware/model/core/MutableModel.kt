package org.tree_ware.model.core

import org.tree_ware.schema.core.*
import java.math.BigDecimal

interface VisitableMutableModel<Aux> {
    /**
     * Visits the model element and its superclasses.
     * The superclasses are visited first and the model element itself is visited last.
     */
    fun mutableVisitSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction

    /**
     * Leaves the model element and its superclasses.
     * The model element itself is left first and the superclasses are left last.
     */
    fun mutableLeaveSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>)

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

    override fun <Return> dispatch(visitor: ModelVisitor<Aux, Return>): Return {
        return visitor.visit(this)
    }

    override fun <Return> mutableDispatch(visitor: MutableModelVisitor<Aux, Return>): Return {
        return visitor.mutableVisit(this)
    }

    // NOTE: call super.visitSelf() FIRST when overriding this method
    override fun visitSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return visitor.visit(this)
    }

    // NOTE: call super.leaveSelf() LAST when overriding this method
    override fun leaveSelf(visitor: ModelVisitor<Aux, SchemaTraversalAction>) {
        visitor.leave(this)
    }

    // NOTE: call super.mutableVisitSelf() FIRST when overriding this method
    override fun mutableVisitSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
        return visitor.mutableVisit(this)
    }

    // NOTE: call super.mutableLeaveSelf() LAST when overriding this method
    override fun mutableLeaveSelf(visitor: MutableModelVisitor<Aux, SchemaTraversalAction>) {
        visitor.mutableLeave(this)
    }
}

class MutableModel<Aux>(override val schema: Schema) : MutableElementModel<Aux>(), Model<Aux> {
    override val parent: ElementModel<Aux>? = null

    override var type = "data"
        internal set(value) {
            field = value
            _root?.also { it.objectId = value }
        }

    override var root: MutableRootModel<Aux>
        get() = _root ?: throw IllegalStateException("Root has not been set")
        internal set(value) {
            value.objectId = type
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
}

abstract class MutableBaseEntityModel<Aux>(
    internal val entitySchema: EntitySchema
) : MutableElementModel<Aux>(), BaseEntityModel<Aux> {
    override var fields: MutableList<MutableFieldModel<Aux>> = mutableListOf()
        internal set

    override fun <ThatAux> keysMatch(that: BaseEntityModel<ThatAux>): Boolean {
        val thisKeyFields = this.fields.filter { it.schema.isKey }
        return thisKeyFields.all { thisKeyField ->
            val thatKeyField = that.getField(thisKeyField.schema.name) ?: return false
            thisKeyField.keysMatch(thatKeyField)
        }
    }

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
    override fun getField(fieldName: String): MutableFieldModel<Aux>? = fields.find { it.schema.name == fieldName }

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

abstract class MutableFieldModel<Aux> : MutableElementModel<Aux>(), FieldModel<Aux> {
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
    override val parent: MutableElementModel<Aux>
) : MutableFieldModel<Aux>(), ScalarFieldModel<Aux> {
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
    parent: MutableElementModel<Aux>
) : MutableScalarFieldModel<Aux>(parent), PrimitiveFieldModel<Aux> {
    override var value: Any? = null
        internal set

    override fun setNullValue(): Boolean {
        this.value = null
        return true
    }

    override fun setValue(value: String): Boolean = setValue(schema.primitive, value) { this.value = it }
    override fun setValue(value: BigDecimal): Boolean = setValue(schema.primitive, value) { this.value = it }
    override fun setValue(value: Boolean): Boolean = setValue(schema.primitive, value) { this.value = it }

    override fun <ThatAux> keysMatch(that: FieldModel<ThatAux>): Boolean {
        val thatField: PrimitiveFieldModel<ThatAux> = that as? PrimitiveFieldModel ?: return false
        return this.value == thatField.value
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
}

class MutableAliasFieldModel<Aux>(
    override val schema: AliasFieldSchema,
    parent: MutableElementModel<Aux>
) : MutableScalarFieldModel<Aux>(parent), AliasFieldModel<Aux> {
    override var value: Any? = null
        internal set

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

    override fun <ThatAux> keysMatch(that: FieldModel<ThatAux>): Boolean {
        val thatField: AliasFieldModel<ThatAux> = that as? AliasFieldModel ?: return false
        return this.value == thatField.value
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
}

class MutableEnumerationFieldModel<Aux>(
    override val schema: EnumerationFieldSchema,
    parent: MutableElementModel<Aux>
) : MutableScalarFieldModel<Aux>(parent), EnumerationFieldModel<Aux> {
    override var value: EnumerationValueSchema? = null
        internal set

    override fun setNullValue(): Boolean {
        this.value = null
        return true
    }

    override fun setValue(value: String): Boolean = setValue(schema.resolvedEnumeration, value) { this.value = it }

    override fun <ThatAux> keysMatch(that: FieldModel<ThatAux>): Boolean {
        val thatField: EnumerationFieldModel<ThatAux> = that as? EnumerationFieldModel ?: return false
        return this.value == thatField.value
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
}

class MutableAssociationFieldModel<Aux>(
    override val schema: AssociationFieldSchema,
    override val parent: MutableElementModel<Aux>
) : MutableFieldModel<Aux>(), AssociationFieldModel<Aux> {
    override var value: List<MutableEntityKeysModel<Aux>> = listOf()
        internal set

    fun setNullValue(): Boolean {
        this.value = listOf()
        return true
    }

    fun newValue(): List<MutableEntityKeysModel<Aux>> {
        value = schema.keyEntities.map { MutableEntityKeysModel<Aux>(it) }
        return value
    }

    override fun <ThatAux> keysMatch(that: FieldModel<ThatAux>): Boolean {
        return false // associations cannot be keys
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
}

class MutableCompositionFieldModel<Aux>(
    override val schema: CompositionFieldSchema,
    override val parent: MutableBaseEntityModel<Aux>
) : MutableFieldModel<Aux>(), CompositionFieldModel<Aux> {
    override var value: MutableEntityModel<Aux> = MutableEntityModel(schema.resolvedEntity, this)
        internal set(value) {
            field = value
            field.objectId = schema.name
        }

    init {
        value.objectId = schema.name
    }

    override fun <ThatAux> keysMatch(that: FieldModel<ThatAux>): Boolean {
        val thatField: CompositionFieldModel<ThatAux> = that as? CompositionFieldModel ?: return false
        return this.value.keysMatch(thatField.value)
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
}

// List fields

abstract class MutableListFieldModel<Aux>(
    override val parent: MutableBaseEntityModel<Aux>
) : MutableFieldModel<Aux>(), ListFieldModel<Aux> {
    override fun <ThatAux> keysMatch(that: FieldModel<ThatAux>): Boolean {
        return false // lists cannot be keys
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
}

abstract class MutableScalarListFieldModel<Aux>(
    parent: MutableBaseEntityModel<Aux>
) : MutableListFieldModel<Aux>(parent), ScalarListFieldModel<Aux> {
    abstract fun addElement(): MutableScalarFieldModel<Aux>

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
) : MutableScalarListFieldModel<Aux>(parent), PrimitiveListFieldModel<Aux> {
    override var primitives: MutableList<MutablePrimitiveFieldModel<Aux>> = mutableListOf()
        internal set

    override fun getPrimitiveField(matching: Any?): MutablePrimitiveFieldModel<Aux>? =
        if (matching == null) null else primitives.find { it.value == matching }

    override fun addElement(): MutableScalarFieldModel<Aux> {
        val element = MutablePrimitiveFieldModel(schema, this)
        primitives.add(element)
        return element
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
}

class MutableAliasListFieldModel<Aux>(
    override val schema: AliasFieldSchema,
    parent: MutableBaseEntityModel<Aux>
) : MutableScalarListFieldModel<Aux>(parent), AliasListFieldModel<Aux> {
    override var aliases: MutableList<MutableAliasFieldModel<Aux>> = mutableListOf()
        internal set

    override fun getAliasField(matching: Any?): MutableAliasFieldModel<Aux>? =
        if (matching == null) null else aliases.find { it.value == matching }

    override fun addElement(): MutableScalarFieldModel<Aux> {
        val element = MutableAliasFieldModel(schema, this)
        aliases.add(element)
        return element
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
}

class MutableEnumerationListFieldModel<Aux>(
    override val schema: EnumerationFieldSchema,
    parent: MutableBaseEntityModel<Aux>
) : MutableScalarListFieldModel<Aux>(parent), EnumerationListFieldModel<Aux> {
    override var enumerations: MutableList<MutableEnumerationFieldModel<Aux>> = mutableListOf()
        internal set

    override fun addElement(): MutableScalarFieldModel<Aux> {
        val element = MutableEnumerationFieldModel(schema, this)
        enumerations.add(element)
        return element
    }

    override fun getEnumerationField(matching: EnumerationValueSchema?): MutableEnumerationFieldModel<Aux>? =
        if (matching == null) null else enumerations.find { it.value == matching }

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

class MutableAssociationListFieldModel<Aux>(
    override val schema: AssociationFieldSchema,
    parent: MutableBaseEntityModel<Aux>
) : MutableListFieldModel<Aux>(parent), AssociationListFieldModel<Aux> {
    override var associations: MutableList<MutableAssociationFieldModel<Aux>> = mutableListOf()
        internal set

    fun addAssociation(): MutableAssociationFieldModel<Aux> {
        val association = MutableAssociationFieldModel(schema, this)
        associations.add(association)
        return association
    }

    // TODO(deepak-nulu): optimize
    override fun <MatchingAux> getAssociationField(matching: List<EntityKeysModel<MatchingAux>>): MutableAssociationFieldModel<Aux>? {
        val matchingSize = matching.size
        if (matchingSize == 0) return null
        return associations.find { association ->
            if (association.value.size != matchingSize) false
            else association.value.zip(matching).all { (a, m) -> a.keysMatch(m) }
        }
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
}

class MutableCompositionListFieldModel<Aux>(
    override val schema: CompositionFieldSchema,
    parent: MutableBaseEntityModel<Aux>
) : MutableListFieldModel<Aux>(parent), CompositionListFieldModel<Aux> {
    override var entities: MutableList<MutableEntityModel<Aux>> = mutableListOf()
        internal set

    fun first(): MutableEntityModel<Aux>? = entities.firstOrNull()

    fun addEntity(): MutableEntityModel<Aux> {
        val entity = MutableEntityModel(schema.resolvedEntity, this)
        entities.add(entity)
        return entity
    }

    // TODO(deepak-nulu): optimize
    override fun <MatchingAux> getEntity(matching: EntityModel<MatchingAux>): MutableEntityModel<Aux>? =
        entities.find { it.keysMatch(matching) }

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

// Field values

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
