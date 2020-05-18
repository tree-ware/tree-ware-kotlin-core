package org.tree_ware.schema.core

interface VisitableMutableSchema {
    /**
     * Traverses the schema element and visits it and its sub-elements (Visitor Pattern).
     * Traversal continues or aborts (partially or fully) based on the value returned by the visitor.
     */
    fun mutableTraverse(visitor: MutableSchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction

    /**
     * Visits the schema element without traversing its sub-elements.
     * Returns what the visitor returns.
     */
    // TODO(deepak-nulu): implement // fun <T> mutableDispatch(visitor: MutableSchemaVisitor<T>): T
}

abstract class MutableElementSchema : ElementSchema, VisitableMutableSchema {
    override fun traverse(visitor: SchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        try {
            val action = visitSelf(visitor)
            if (action == SchemaTraversalAction.ABORT_TREE) return SchemaTraversalAction.ABORT_TREE
            if (action == SchemaTraversalAction.ABORT_SUB_TREE) return SchemaTraversalAction.CONTINUE
            return traverseChildren(visitor)
        } finally {
            leaveSelf(visitor)
        }
    }

    override fun mutableTraverse(visitor: MutableSchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        try {
            val action = mutableVisitSelf(visitor)
            if (action == SchemaTraversalAction.ABORT_TREE) return SchemaTraversalAction.ABORT_TREE
            if (action == SchemaTraversalAction.ABORT_SUB_TREE) return SchemaTraversalAction.CONTINUE
            return mutableTraverseChildren(visitor)
        } finally {
            mutableLeaveSelf(visitor)
        }
    }

    // NOTE: call super.visitSelf() FIRST when overriding this method
    protected open fun visitSelf(visitor: SchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return visitor.visit(this)
    }

    // NOTE: call super.leaveSelf() LAST when overriding this method
    protected open fun leaveSelf(visitor: SchemaVisitor<SchemaTraversalAction>) {
        visitor.leave(this)
    }

    // NOTE: call super.mutableVisitSelf() FIRST when overriding this method
    protected open fun mutableVisitSelf(visitor: MutableSchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return visitor.mutableVisit(this)
    }

    // NOTE: call super.mutableLeaveSelf() LAST when overriding this method
    protected open fun mutableLeaveSelf(visitor: MutableSchemaVisitor<SchemaTraversalAction>) {
        visitor.mutableLeave(this)
    }

    protected open fun traverseChildren(visitor: SchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }

    protected open fun mutableTraverseChildren(visitor: MutableSchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return SchemaTraversalAction.CONTINUE
    }
}

abstract class MutableNamedElementSchema(
    override var name: String, override var info: String?
) : MutableElementSchema(), NamedElementSchema {
    override var fullName: String
        get() = _fullName ?: throw IllegalStateException("Full-name has not been set")
        internal set(value) {
            _fullName = value
        }
    private var _fullName: String? = null

    override fun visitSelf(visitor: SchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: SchemaVisitor<SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableSchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableSchemaVisitor<SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }
}

class MutableSchema(
    root: MutableRootSchema? = null,
    packages: List<MutablePackageSchema> = listOf()
) : MutableElementSchema(), Schema {
    override val id = "schema"

    internal var _root: MutableRootSchema? = root

    override var root: MutableRootSchema
        get() = _root ?: throw IllegalStateException("Root has not been set")
        internal set(value) {
            _root = value
        }

    override var packages: MutableList<MutablePackageSchema> = packages.toMutableList()
        internal set(value) {
            field = value
            field.forEach { it.parent = this }
        }

    init {
        packages.forEach { it.parent = this }
    }

    override fun visitSelf(visitor: SchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: SchemaVisitor<SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableSchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableSchemaVisitor<SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: SchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.traverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        _root?.also {
            val action = it.traverse(visitor)
            if (action == SchemaTraversalAction.ABORT_TREE) return action
        }

        if (packages.isNotEmpty()) try {
            visitor.visitList("packages")
            for (pkg in packages) {
                val action = pkg.traverse(visitor)
                if (action == SchemaTraversalAction.ABORT_TREE) return action
            }
        } finally {
            visitor.leaveList("packages")
        }

        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableTraverseChildren(visitor: MutableSchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.mutableTraverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        _root?.also {
            val action = it.mutableTraverse(visitor)
            if (action == SchemaTraversalAction.ABORT_TREE) return action
        }

        if (packages.isNotEmpty()) try {
            visitor.mutableVisitList("packages")
            for (pkg in packages) {
                val action = pkg.mutableTraverse(visitor)
                if (action == SchemaTraversalAction.ABORT_TREE) return action
            }
        } finally {
            visitor.mutableLeaveList("packages")
        }

        return SchemaTraversalAction.CONTINUE
    }
}

class MutableRootSchema(
    name: String,
    info: String? = null,
    override var packageName: String,
    override var entityName: String
) : MutableNamedElementSchema(name, info), RootSchema {
    override val id = "root"

    override var parent: MutablePackageSchema
        get() = _parent ?: throw IllegalStateException("Parent has not been set")
        internal set(value) {
            _parent = value
        }
    private var _parent: MutablePackageSchema? = null

    override var resolvedEntity: MutableEntitySchema
        get() = _resolvedEntity
            ?: throw IllegalStateException("Root /${packageName}/${entityName} has not been resolved")
        internal set(value) {
            _resolvedEntity = value
        }
    internal var _resolvedEntity: MutableEntitySchema? = null
        private set

    override fun visitSelf(visitor: SchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: SchemaVisitor<SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableSchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableSchemaVisitor<SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    // The resolved type of this field is not considered a child and is therefore not traversed.
}

class MutablePackageSchema(
    name: String,
    info: String? = null,
    override var aliases: List<MutableAliasSchema> = listOf(),
    override var enumerations: List<MutableEnumerationSchema> = listOf(),
    override var entities: List<MutableEntitySchema> = listOf()
) : MutableNamedElementSchema(name, info), PackageSchema {
    override val id = "package"

    init {
        aliases.forEach { it.parent = this }
        enumerations.forEach { it.parent = this }
        entities.forEach { it.parent = this }
    }

    override var parent: MutableSchema
        get() = _parent ?: throw IllegalStateException("Parent has not been set")
        internal set(value) {
            _parent = value
        }
    private var _parent: MutableSchema? = null

    override fun visitSelf(visitor: SchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: SchemaVisitor<SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableSchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableSchemaVisitor<SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: SchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.traverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        if (aliases.isNotEmpty()) try {
            visitor.visitList("aliases")
            for (alias in aliases) {
                val action = alias.traverse(visitor)
                if (action == SchemaTraversalAction.ABORT_TREE) return action
            }
        } finally {
            visitor.leaveList("aliases")
        }
        if (enumerations.isNotEmpty()) try {
            visitor.visitList("enumerations")
            for (enumeration in enumerations) {
                val action = enumeration.traverse(visitor)
                if (action == SchemaTraversalAction.ABORT_TREE) return action
            }
        } finally {
            visitor.leaveList("enumerations")
        }
        if (entities.isNotEmpty()) try {
            visitor.visitList("entities")
            for (entity in entities) {
                val action = entity.traverse(visitor)
                if (action == SchemaTraversalAction.ABORT_TREE) return action
            }
        } finally {
            visitor.leaveList("entities")
        }

        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableTraverseChildren(visitor: MutableSchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.mutableTraverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        if (aliases.isNotEmpty()) try {
            visitor.mutableVisitList("aliases")
            for (alias in aliases) {
                val action = alias.mutableTraverse(visitor)
                if (action == SchemaTraversalAction.ABORT_TREE) return action
            }
        } finally {
            visitor.mutableLeaveList("aliases")
        }
        if (enumerations.isNotEmpty()) try {
            visitor.mutableVisitList("enumerations")
            for (enumeration in enumerations) {
                val action = enumeration.mutableTraverse(visitor)
                if (action == SchemaTraversalAction.ABORT_TREE) return action
            }
        } finally {
            visitor.mutableLeaveList("enumerations")
        }
        if (entities.isNotEmpty()) try {
            visitor.mutableVisitList("entities")
            for (entity in entities) {
                val action = entity.mutableTraverse(visitor)
                if (action == SchemaTraversalAction.ABORT_TREE) return action
            }
        } finally {
            visitor.mutableLeaveList("entities")
        }

        return SchemaTraversalAction.CONTINUE
    }
}

class MutableAliasSchema(
    name: String,
    info: String? = null,
    override var primitive: MutablePrimitiveSchema
) : MutableNamedElementSchema(name, info), AliasSchema {
    override val id = "aliases"

    override var parent: MutablePackageSchema
        get() = _parent ?: throw IllegalStateException("Parent has not been set")
        internal set(value) {
            _parent = value
        }
    private var _parent: MutablePackageSchema? = null

    override fun visitSelf(visitor: SchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: SchemaVisitor<SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableSchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableSchemaVisitor<SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: SchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.traverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        primitive.also {
            val action = it.traverse(visitor)
            if (action == SchemaTraversalAction.ABORT_TREE) return action
        }

        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableTraverseChildren(visitor: MutableSchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.mutableTraverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        primitive.also {
            val action = it.mutableTraverse(visitor)
            if (action == SchemaTraversalAction.ABORT_TREE) return action
        }

        return SchemaTraversalAction.CONTINUE
    }
}

class MutableEnumerationSchema(
    name: String,
    info: String? = null,
    override var values: List<MutableEnumerationValueSchema>
) : MutableNamedElementSchema(name, info), EnumerationSchema {
    override val id = "enumeration"

    init {
        values.forEach { it.parent = this }
    }

    override var parent: MutablePackageSchema
        get() = _parent ?: throw IllegalStateException("Parent has not been set")
        internal set(value) {
            _parent = value
        }
    private var _parent: MutablePackageSchema? = null

    override fun visitSelf(visitor: SchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: SchemaVisitor<SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableSchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableSchemaVisitor<SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: SchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.traverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        if (values.isNotEmpty()) try {
            visitor.visitList("values")
            for (value in values) {
                val action = value.traverse(visitor)
                if (action == SchemaTraversalAction.ABORT_TREE) return action
            }
        } finally {
            visitor.leaveList("values")
        }

        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableTraverseChildren(visitor: MutableSchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.mutableTraverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        if (values.isNotEmpty()) try {
            visitor.mutableVisitList("values")
            for (value in values) {
                val action = value.mutableTraverse(visitor)
                if (action == SchemaTraversalAction.ABORT_TREE) return action
            }
        } finally {
            visitor.mutableLeaveList("values")
        }

        return SchemaTraversalAction.CONTINUE
    }
}

class MutableEnumerationValueSchema(
    name: String, info: String? = null
) : MutableNamedElementSchema(name, info), EnumerationValueSchema {
    override val id = "enumeration_value"

    override var parent: MutableEnumerationSchema
        get() = _parent ?: throw IllegalStateException("Parent has not been set")
        internal set(value) {
            _parent = value
        }
    private var _parent: MutableEnumerationSchema? = null

    override fun visitSelf(visitor: SchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: SchemaVisitor<SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableSchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableSchemaVisitor<SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }
}

class MutableEntitySchema(
    name: String, info: String? = null, override var fields: List<MutableFieldSchema>
) : MutableNamedElementSchema(name, info), EntitySchema {
    override val id = "entity"

    init {
        fields.forEach { it.parent = this }
    }

    override var parent: MutablePackageSchema
        get() = _parent ?: throw IllegalStateException("Parent has not been set")
        internal set(value) {
            _parent = value
        }
    private var _parent: MutablePackageSchema? = null

    override fun visitSelf(visitor: SchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: SchemaVisitor<SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableSchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableSchemaVisitor<SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: SchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.traverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        if (fields.isNotEmpty()) try {
            visitor.visitList("fields")
            for (field in fields) {
                val action = field.traverse(visitor)
                if (action == SchemaTraversalAction.ABORT_TREE) return action
            }
        } finally {
            visitor.leaveList("fields")
        }

        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableTraverseChildren(visitor: MutableSchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.mutableTraverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        if (fields.isNotEmpty()) try {
            visitor.mutableVisitList("fields")
            for (field in fields) {
                val action = field.mutableTraverse(visitor)
                if (action == SchemaTraversalAction.ABORT_TREE) return action
            }
        } finally {
            visitor.mutableLeaveList("fields")
        }

        return SchemaTraversalAction.CONTINUE
    }
}

// Fields

abstract class MutableFieldSchema(
    name: String,
    info: String? = null,
    override var isKey: Boolean,
    override var multiplicity: MutableMultiplicity = MutableMultiplicity(1, 1)
) : MutableNamedElementSchema(name, info), FieldSchema {
    override var parent: MutableEntitySchema
        get() = _parent ?: throw IllegalStateException("Parent has not been set")
        internal set(value) {
            _parent = value
        }
    private var _parent: MutableEntitySchema? = null

    override fun visitSelf(visitor: SchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: SchemaVisitor<SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableSchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableSchemaVisitor<SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }
}

class MutablePrimitiveFieldSchema(
    name: String,
    info: String? = null,
    override var primitive: MutablePrimitiveSchema,
    isKey: Boolean = false,
    multiplicity: MutableMultiplicity = MutableMultiplicity(1, 1)
) : MutableFieldSchema(name, info, isKey, multiplicity), PrimitiveFieldSchema {
    override val id = "primitive_field"

    override fun visitSelf(visitor: SchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: SchemaVisitor<SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableSchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableSchemaVisitor<SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    override fun traverseChildren(visitor: SchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.traverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        primitive.also {
            val action = it.traverse(visitor)
            if (action == SchemaTraversalAction.ABORT_TREE) return action
        }

        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableTraverseChildren(visitor: MutableSchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        val superAction = super.mutableTraverseChildren(visitor)
        if (superAction == SchemaTraversalAction.ABORT_TREE) return superAction

        primitive.also {
            val action = it.mutableTraverse(visitor)
            if (action == SchemaTraversalAction.ABORT_TREE) return action
        }

        return SchemaTraversalAction.CONTINUE
    }
}

class MutableAliasFieldSchema(
    name: String,
    info: String? = null,
    override var packageName: String,
    override var aliasName: String,
    isKey: Boolean = false,
    multiplicity: MutableMultiplicity = MutableMultiplicity(1, 1)
) : MutableFieldSchema(name, info, isKey, multiplicity), AliasFieldSchema {
    override val id = "alias_field"

    override var resolvedAlias: MutableAliasSchema
        get() = _resolvedAlias
            ?: throw IllegalStateException("Alias /${packageName}/${aliasName} has not been resolved")
        internal set(value) {
            _resolvedAlias = value
        }
    private var _resolvedAlias: MutableAliasSchema? = null

    override fun visitSelf(visitor: SchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: SchemaVisitor<SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableSchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableSchemaVisitor<SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    // The resolved type of this field is not considered a child and is therefore not traversed.
}

class MutableEnumerationFieldSchema(
    name: String,
    info: String? = null,
    override var packageName: String,
    override var enumerationName: String,
    isKey: Boolean = false,
    multiplicity: MutableMultiplicity = MutableMultiplicity(1, 1)
) : MutableFieldSchema(name, info, isKey, multiplicity), EnumerationFieldSchema {
    override val id = "enumeration_field"

    override var resolvedEnumeration: MutableEnumerationSchema
        get() = _resolvedEnumeration
            ?: throw IllegalStateException("Enumeration /${packageName}/${enumerationName} has not been resolved")
        internal set(value) {
            _resolvedEnumeration = value
        }
    private var _resolvedEnumeration: MutableEnumerationSchema? = null

    override fun visitSelf(visitor: SchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: SchemaVisitor<SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableSchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableSchemaVisitor<SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    // The resolved type of this field is not considered a child and is therefore not traversed.
}

class MutableAssociationFieldSchema(
    name: String,
    info: String? = null,
    internal val entityPathSchema: MutableEntityPathSchema,
    multiplicity: MutableMultiplicity = MutableMultiplicity(1, 1)
) : MutableFieldSchema(name, info, false, multiplicity), AssociationFieldSchema, EntityPathSchema by entityPathSchema {
    override val id = "association_field"

    override fun visitSelf(visitor: SchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: SchemaVisitor<SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableSchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableSchemaVisitor<SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    // The resolved type of this field is not considered a child and is therefore not traversed.
}

class MutableCompositionFieldSchema(
    name: String,
    info: String? = null,
    override var packageName: String,
    override var entityName: String,
    isKey: Boolean = false,
    multiplicity: MutableMultiplicity = MutableMultiplicity(1, 1)
) : MutableFieldSchema(name, info, isKey, multiplicity), CompositionFieldSchema {
    override val id = "composition_field"

    override var resolvedEntity: MutableEntitySchema
        get() = _resolvedEntity
            ?: throw IllegalStateException("Composition /${packageName}/${entityName} has not been resolved")
        internal set(value) {
            _resolvedEntity = value
        }
    internal var _resolvedEntity: MutableEntitySchema? = null
        private set

    override fun visitSelf(visitor: SchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.visitSelf(visitor), visitor.visit(this))
    }

    override fun leaveSelf(visitor: SchemaVisitor<SchemaTraversalAction>) {
        visitor.leave(this)
        super.leaveSelf(visitor)
    }

    override fun mutableVisitSelf(visitor: MutableSchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        return or(super.mutableVisitSelf(visitor), visitor.mutableVisit(this))
    }

    override fun mutableLeaveSelf(visitor: MutableSchemaVisitor<SchemaTraversalAction>) {
        visitor.mutableLeave(this)
        super.mutableLeaveSelf(visitor)
    }

    // The resolved type of this field is not considered a child and is therefore not traversed.
}

// Special Values

class MutableEntityPathSchema(override var entityPath: List<String>) : EntityPathSchema {
    override val pathEntities: MutableList<EntitySchema> = mutableListOf()
    override val keyEntities: MutableList<EntitySchema> = mutableListOf()

    override var resolvedEntity: EntitySchema
        get() = _resolvedEntity
            ?: throw IllegalStateException("Association $entityPath has not been resolved")
        internal set(value) {
            _resolvedEntity = value
        }
    private var _resolvedEntity: EntitySchema? = null
}

// Predefined Primitives

abstract class MutablePrimitiveSchema : PrimitiveSchema,
    VisitableMutableSchema

class MutableBooleanSchema : MutablePrimitiveSchema(), BooleanSchema {
    override fun traverse(visitor: SchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        try {
            return visitor.visit(this)
        } finally {
            visitor.leave(this)
        }
    }

    override fun mutableTraverse(visitor: MutableSchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        try {
            return visitor.mutableVisit(this)
        } finally {
            visitor.mutableLeave(this)
        }
    }
}

class MutableByteSchema(
    override var constraints: MutableNumericConstraints<Byte>? = null
) : MutablePrimitiveSchema(), ByteSchema {
    override fun traverse(visitor: SchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        try {
            return visitor.visit(this)
        } finally {
            visitor.leave(this)
        }
    }

    override fun mutableTraverse(visitor: MutableSchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        try {
            return visitor.mutableVisit(this)
        } finally {
            visitor.mutableLeave(this)
        }
    }
}

class MutableShortSchema(
    override var constraints: MutableNumericConstraints<Short>? = null
) : MutablePrimitiveSchema(), ShortSchema {
    override fun traverse(visitor: SchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        try {
            return visitor.visit(this)
        } finally {
            visitor.leave(this)
        }
    }

    override fun mutableTraverse(visitor: MutableSchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        try {
            return visitor.mutableVisit(this)
        } finally {
            visitor.mutableLeave(this)
        }
    }
}

class MutableIntSchema(
    override var constraints: MutableNumericConstraints<Int>? = null
) : MutablePrimitiveSchema(), IntSchema {
    override fun traverse(visitor: SchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        try {
            return visitor.visit(this)
        } finally {
            visitor.leave(this)
        }
    }

    override fun mutableTraverse(visitor: MutableSchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        try {
            return visitor.mutableVisit(this)
        } finally {
            visitor.mutableLeave(this)
        }
    }
}

class MutableLongSchema(
    override var constraints: MutableNumericConstraints<Long>? = null
) : MutablePrimitiveSchema(), LongSchema {
    override fun traverse(visitor: SchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        try {
            return visitor.visit(this)
        } finally {
            visitor.leave(this)
        }
    }

    override fun mutableTraverse(visitor: MutableSchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        try {
            return visitor.mutableVisit(this)
        } finally {
            visitor.mutableLeave(this)
        }
    }
}

class MutableFloatSchema(
    override var constraints: MutableNumericConstraints<Float>? = null
) : MutablePrimitiveSchema(), FloatSchema {
    override fun traverse(visitor: SchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        try {
            return visitor.visit(this)
        } finally {
            visitor.leave(this)
        }
    }

    override fun mutableTraverse(visitor: MutableSchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        try {
            return visitor.mutableVisit(this)
        } finally {
            visitor.mutableLeave(this)
        }
    }
}

class MutableDoubleSchema(
    override var constraints: MutableNumericConstraints<Double>? = null
) : MutablePrimitiveSchema(), DoubleSchema {
    override fun traverse(visitor: SchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        try {
            return visitor.visit(this)
        } finally {
            visitor.leave(this)
        }
    }

    override fun mutableTraverse(visitor: MutableSchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        try {
            return visitor.mutableVisit(this)
        } finally {
            visitor.mutableLeave(this)
        }
    }
}

open class MutableStringSchema(
    override var constraints: MutableStringConstraints? = null
) : MutablePrimitiveSchema(), StringSchema {
    override fun traverse(visitor: SchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        try {
            return visitor.visit(this)
        } finally {
            visitor.leave(this)
        }
    }

    override fun mutableTraverse(visitor: MutableSchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        try {
            return visitor.mutableVisit(this)
        } finally {
            visitor.mutableLeave(this)
        }
    }
}

class MutablePassword1WaySchema(
    constraints: MutableStringConstraints? = null
) : MutableStringSchema(constraints), Password1WaySchema {
    override fun traverse(visitor: SchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        try {
            return visitor.visit(this)
        } finally {
            visitor.leave(this)
        }
    }

    override fun mutableTraverse(visitor: MutableSchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        try {
            return visitor.mutableVisit(this)
        } finally {
            visitor.mutableLeave(this)
        }
    }
}

class MutablePassword2WaySchema(
    constraints: MutableStringConstraints? = null
) : MutableStringSchema(constraints), Password2WaySchema {
    override fun traverse(visitor: SchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        try {
            return visitor.visit(this)
        } finally {
            visitor.leave(this)
        }
    }

    override fun mutableTraverse(visitor: MutableSchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        try {
            return visitor.mutableVisit(this)
        } finally {
            visitor.mutableLeave(this)
        }
    }
}

class MutableUuidSchema : MutablePrimitiveSchema(), UuidSchema {
    override fun traverse(visitor: SchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        try {
            return visitor.visit(this)
        } finally {
            visitor.leave(this)
        }
    }

    override fun mutableTraverse(visitor: MutableSchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        try {
            return visitor.mutableVisit(this)
        } finally {
            visitor.mutableLeave(this)
        }
    }
}

class MutableBlobSchema(
    override var constraints: MutableBlobConstraints? = null
) : MutablePrimitiveSchema(), BlobSchema {
    override fun traverse(visitor: SchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        try {
            return visitor.visit(this)
        } finally {
            visitor.leave(this)
        }
    }

    override fun mutableTraverse(visitor: MutableSchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        try {
            return visitor.mutableVisit(this)
        } finally {
            visitor.mutableLeave(this)
        }
    }
}

class MutableTimestampSchema : MutablePrimitiveSchema(), TimestampSchema {
    override fun traverse(visitor: SchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        try {
            return visitor.visit(this)
        } finally {
            visitor.leave(this)
        }
    }

    override fun mutableTraverse(visitor: MutableSchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction {
        try {
            return visitor.mutableVisit(this)
        } finally {
            visitor.mutableLeave(this)
        }
    }
}

// Constraints

class MutableMultiplicity(override var min: Long, override var max: Long) :
    Multiplicity

class MutableNumericConstraints<T : Number>(
    override var minBound: MutableNumericBound<T>?,
    override var maxBound: MutableNumericBound<T>?
) : NumericConstraints<T>

class MutableNumericBound<T : Number>(override var value: T, override var inclusive: Boolean) :
    NumericBound<T>

class MutableStringConstraints(
    override var minLength: Int? = null,
    override var maxLength: Int? = null,
    override var regex: String? = null
) : StringConstraints

class MutableBlobConstraints(override var minLength: Int?, override var maxLength: Int?) :
    BlobConstraints
