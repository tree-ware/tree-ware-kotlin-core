package org.tree_ware.schema.core

interface VisitableMutableSchema {
    /**
     * Accepts a visitor and traverses the mutable schema with it (Visitor Pattern).
     *
     * If the visitor implements `BracketedVisitor`, then those methods will be called as well.
     *
     * @returns `true` to proceed with schema traversal, `false` to stop schema traversal.
     */
    fun mutableAccept(visitor: MutableSchemaVisitor): Boolean
}

abstract class MutableElementSchema : ElementSchema,
    VisitableMutableSchema {
    var objectId = ""

    override fun accept(visitor: SchemaVisitor): Boolean {
        try {
            (visitor as? BracketedVisitor)?.objectStart(objectId)
            if (!visitSelf(visitor)) return false
            if (!traverseChildren(visitor)) return false
            return true
        } finally {
            (visitor as? BracketedVisitor)?.objectEnd()
        }
    }

    override fun mutableAccept(visitor: MutableSchemaVisitor): Boolean {
        try {
            (visitor as? BracketedVisitor)?.objectStart(objectId)
            if (!mutableVisitSelf(visitor)) return false
            if (!mutableTraverseChildren(visitor)) return false
            return true
        } finally {
            (visitor as? BracketedVisitor)?.objectEnd()
        }
    }

    protected open fun visitSelf(visitor: SchemaVisitor): Boolean {
        return visitor.visit(this)
    }

    protected open fun mutableVisitSelf(visitor: MutableSchemaVisitor): Boolean {
        return visitor.mutableVisit(this)
    }

    protected open fun traverseChildren(visitor: SchemaVisitor): Boolean {
        return true
    }

    protected open fun mutableTraverseChildren(visitor: MutableSchemaVisitor): Boolean {
        return true
    }
}

abstract class MutableNamedElementSchema(override var name: String, override var info: String?) :
    MutableElementSchema(), NamedElementSchema {
    override var fullName: String
        get() = _fullName ?: throw IllegalStateException("Full-name has not been set")
        internal set(value) {
            _fullName = value
        }
    private var _fullName: String? = null

    override fun visitSelf(visitor: SchemaVisitor): Boolean {
        return super.visitSelf(visitor) && visitor.visit(this)
    }

    override fun mutableVisitSelf(visitor: MutableSchemaVisitor): Boolean {
        return super.mutableVisitSelf(visitor) && visitor.mutableVisit(this)
    }
}

class MutableSchema(
    root: MutableRootSchema? = null,
    packages: List<MutablePackageSchema> = listOf()
) : MutableElementSchema(), Schema {
    init {
        objectId = "schema"
    }

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

    override fun visitSelf(visitor: SchemaVisitor): Boolean {
        return super.visitSelf(visitor) && visitor.visit(this)
    }

    override fun mutableVisitSelf(visitor: MutableSchemaVisitor): Boolean {
        return super.mutableVisitSelf(visitor) && visitor.mutableVisit(this)
    }

    override fun traverseChildren(visitor: SchemaVisitor): Boolean {
        if (!super.traverseChildren(visitor)) return false

        _root?.also {
            if (!it.accept(visitor)) return false
        }

        if (packages.isNotEmpty()) try {
            (visitor as? BracketedVisitor)?.listStart("packages")
            for (pkg in packages) {
                if (!pkg.accept(visitor)) return false
            }
        } finally {
            (visitor as? BracketedVisitor)?.listEnd()
        }
        return true
    }

    override fun mutableTraverseChildren(visitor: MutableSchemaVisitor): Boolean {
        if (!super.mutableTraverseChildren(visitor)) return false

        _root?.also {
            if (!it.mutableAccept(visitor)) return false
        }

        if (packages.isNotEmpty()) try {
            (visitor as? BracketedVisitor)?.listStart("packages")
            for (pkg in packages) {
                if (!pkg.mutableAccept(visitor)) return false
            }
        } finally {
            (visitor as? BracketedVisitor)?.listEnd()
        }
        return true
    }
}

class MutableRootSchema(
    name: String,
    info: String? = null,
    override var packageName: String,
    override var entityName: String
) : MutableNamedElementSchema(name, info), RootSchema {
    init {
        objectId = "root"
    }

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

    override fun visitSelf(visitor: SchemaVisitor): Boolean {
        return super.visitSelf(visitor) && visitor.visit(this)
    }

    override fun mutableVisitSelf(visitor: MutableSchemaVisitor): Boolean {
        return super.mutableVisitSelf(visitor) && visitor.mutableVisit(this)
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

    override fun visitSelf(visitor: SchemaVisitor): Boolean {
        return super.visitSelf(visitor) && visitor.visit(this)
    }

    override fun mutableVisitSelf(visitor: MutableSchemaVisitor): Boolean {
        return super.mutableVisitSelf(visitor) && visitor.mutableVisit(this)
    }

    override fun traverseChildren(visitor: SchemaVisitor): Boolean {
        if (!super.traverseChildren(visitor)) return false

        if (aliases.isNotEmpty()) try {
            (visitor as? BracketedVisitor)?.listStart("aliases")
            for (alias in aliases) {
                if (!alias.accept(visitor)) return false
            }
        } finally {
            (visitor as? BracketedVisitor)?.listEnd()
        }
        if (enumerations.isNotEmpty()) try {
            (visitor as? BracketedVisitor)?.listStart("enumerations")
            for (enumeration in enumerations) {
                if (!enumeration.accept(visitor)) return false
            }
        } finally {
            (visitor as? BracketedVisitor)?.listEnd()
        }
        if (entities.isNotEmpty()) try {
            (visitor as? BracketedVisitor)?.listStart("entities")
            for (entity in entities) {
                if (!entity.accept(visitor)) return false
            }
        } finally {
            (visitor as? BracketedVisitor)?.listEnd()
        }

        return true
    }

    override fun mutableTraverseChildren(visitor: MutableSchemaVisitor): Boolean {
        if (!super.mutableTraverseChildren(visitor)) return false

        if (aliases.isNotEmpty()) try {
            (visitor as? BracketedVisitor)?.listStart("aliases")
            for (alias in aliases) {
                if (!alias.mutableAccept(visitor)) return false
            }
        } finally {
            (visitor as? BracketedVisitor)?.listEnd()
        }
        if (enumerations.isNotEmpty()) try {
            (visitor as? BracketedVisitor)?.listStart("enumerations")
            for (enumeration in enumerations) {
                if (!enumeration.mutableAccept(visitor)) return false
            }
        } finally {
            (visitor as? BracketedVisitor)?.listEnd()
        }
        if (entities.isNotEmpty()) try {
            (visitor as? BracketedVisitor)?.listStart("entities")
            for (entity in entities) {
                if (!entity.mutableAccept(visitor)) return false
            }
        } finally {
            (visitor as? BracketedVisitor)?.listEnd()
        }

        return true
    }
}

class MutableAliasSchema(
    name: String,
    info: String? = null,
    override var primitive: MutablePrimitiveSchema
) : MutableNamedElementSchema(name, info), AliasSchema {
    override var parent: MutablePackageSchema
        get() = _parent ?: throw IllegalStateException("Parent has not been set")
        internal set(value) {
            _parent = value
        }
    private var _parent: MutablePackageSchema? = null

    override fun visitSelf(visitor: SchemaVisitor): Boolean {
        return super.visitSelf(visitor) && visitor.visit(this)
    }

    override fun mutableVisitSelf(visitor: MutableSchemaVisitor): Boolean {
        return super.mutableVisitSelf(visitor) && visitor.mutableVisit(this)
    }

    override fun traverseChildren(visitor: SchemaVisitor): Boolean {
        return super.traverseChildren(visitor) && primitive.accept(visitor)
    }

    override fun mutableTraverseChildren(visitor: MutableSchemaVisitor): Boolean {
        return super.mutableTraverseChildren(visitor) && primitive.mutableAccept(visitor)
    }
}

class MutableEnumerationSchema(
    name: String,
    info: String? = null,
    override var values: List<MutableEnumerationValueSchema>
) : MutableNamedElementSchema(name, info), EnumerationSchema {
    init {
        values.forEach { it.parent = this }
    }

    override var parent: MutablePackageSchema
        get() = _parent ?: throw IllegalStateException("Parent has not been set")
        internal set(value) {
            _parent = value
        }
    private var _parent: MutablePackageSchema? = null

    override fun visitSelf(visitor: SchemaVisitor): Boolean {
        return super.visitSelf(visitor) && visitor.visit(this)
    }

    override fun mutableVisitSelf(visitor: MutableSchemaVisitor): Boolean {
        return super.mutableVisitSelf(visitor) && visitor.mutableVisit(this)
    }

    override fun traverseChildren(visitor: SchemaVisitor): Boolean {
        if (!super.traverseChildren(visitor)) return false

        if (values.isNotEmpty()) try {
            (visitor as? BracketedVisitor)?.listStart("values")
            for (value in values) {
                if (!value.accept(visitor)) return false
            }
        } finally {
            (visitor as? BracketedVisitor)?.listEnd()
        }

        return true
    }

    override fun mutableTraverseChildren(visitor: MutableSchemaVisitor): Boolean {
        if (!super.mutableTraverseChildren(visitor)) return false

        if (values.isNotEmpty()) try {
            (visitor as? BracketedVisitor)?.listStart("fields")
            for (value in values) {
                if (!value.mutableAccept(visitor)) return false
            }
        } finally {
            (visitor as? BracketedVisitor)?.listEnd()
        }

        return true
    }
}

class MutableEnumerationValueSchema(name: String, info: String? = null) : MutableNamedElementSchema(name, info),
    EnumerationValueSchema {
    override var parent: MutableEnumerationSchema
        get() = _parent ?: throw IllegalStateException("Parent has not been set")
        internal set(value) {
            _parent = value
        }
    private var _parent: MutableEnumerationSchema? = null

    override fun visitSelf(visitor: SchemaVisitor): Boolean {
        return super.visitSelf(visitor) && visitor.visit(this)
    }

    override fun mutableVisitSelf(visitor: MutableSchemaVisitor): Boolean {
        return super.mutableVisitSelf(visitor) && visitor.mutableVisit(this)
    }
}

class MutableEntitySchema(
    name: String, info: String? = null, override var fields: List<MutableFieldSchema>
) : MutableNamedElementSchema(name, info), EntitySchema {
    init {
        fields.forEach { it.parent = this }
    }

    override var parent: MutablePackageSchema
        get() = _parent ?: throw IllegalStateException("Parent has not been set")
        internal set(value) {
            _parent = value
        }
    private var _parent: MutablePackageSchema? = null

    override fun visitSelf(visitor: SchemaVisitor): Boolean {
        return super.visitSelf(visitor) && visitor.visit(this)
    }

    override fun mutableVisitSelf(visitor: MutableSchemaVisitor): Boolean {
        return super.mutableVisitSelf(visitor) && visitor.mutableVisit(this)
    }

    override fun traverseChildren(visitor: SchemaVisitor): Boolean {
        if (!super.traverseChildren(visitor)) return false

        if (fields.isNotEmpty()) try {
            (visitor as? BracketedVisitor)?.listStart("fields")
            for (field in fields) {
                if (!field.accept(visitor)) return false
            }
        } finally {
            (visitor as? BracketedVisitor)?.listEnd()
        }

        return true
    }

    override fun mutableTraverseChildren(visitor: MutableSchemaVisitor): Boolean {
        if (!super.mutableTraverseChildren(visitor)) return false

        if (fields.isNotEmpty()) try {
            (visitor as? BracketedVisitor)?.listStart("fields")
            for (field in fields) {
                if (!field.mutableAccept(visitor)) return false
            }
        } finally {
            (visitor as? BracketedVisitor)?.listEnd()
        }

        return true
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

    override fun visitSelf(visitor: SchemaVisitor): Boolean {
        return super.visitSelf(visitor) && visitor.visit(this)
    }

    override fun mutableVisitSelf(visitor: MutableSchemaVisitor): Boolean {
        return super.mutableVisitSelf(visitor) && visitor.mutableVisit(this)
    }
}

class MutablePrimitiveFieldSchema(
    name: String,
    info: String? = null,
    override var primitive: MutablePrimitiveSchema,
    isKey: Boolean = false,
    multiplicity: MutableMultiplicity = MutableMultiplicity(1, 1)
) : MutableFieldSchema(name, info, isKey, multiplicity),
    PrimitiveFieldSchema {
    override fun visitSelf(visitor: SchemaVisitor): Boolean {
        return super.visitSelf(visitor) && visitor.visit(this)
    }

    override fun mutableVisitSelf(visitor: MutableSchemaVisitor): Boolean {
        return super.mutableVisitSelf(visitor) && visitor.mutableVisit(this)
    }

    override fun traverseChildren(visitor: SchemaVisitor): Boolean {
        return super.traverseChildren(visitor) && primitive.accept(visitor)
    }

    override fun mutableTraverseChildren(visitor: MutableSchemaVisitor): Boolean {
        return super.mutableTraverseChildren(visitor) && primitive.mutableAccept(visitor)
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
    override var resolvedAlias: MutableAliasSchema
        get() = _resolvedAlias
            ?: throw IllegalStateException("Alias /${packageName}/${aliasName} has not been resolved")
        internal set(value) {
            _resolvedAlias = value
        }
    private var _resolvedAlias: MutableAliasSchema? = null

    override fun visitSelf(visitor: SchemaVisitor): Boolean {
        return super.visitSelf(visitor) && visitor.visit(this)
    }

    override fun mutableVisitSelf(visitor: MutableSchemaVisitor): Boolean {
        return super.mutableVisitSelf(visitor) && visitor.mutableVisit(this)
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
) : MutableFieldSchema(name, info, isKey, multiplicity),
    EnumerationFieldSchema {
    override var resolvedEnumeration: MutableEnumerationSchema
        get() = _resolvedEnumeration
            ?: throw IllegalStateException("Enumeration /${packageName}/${enumerationName} has not been resolved")
        internal set(value) {
            _resolvedEnumeration = value
        }
    private var _resolvedEnumeration: MutableEnumerationSchema? = null

    override fun visitSelf(visitor: SchemaVisitor): Boolean {
        return super.visitSelf(visitor) && visitor.visit(this)
    }

    override fun mutableVisitSelf(visitor: MutableSchemaVisitor): Boolean {
        return super.mutableVisitSelf(visitor) && visitor.mutableVisit(this)
    }

    // The resolved type of this field is not considered a child and is therefore not traversed.
}

class MutableAssociationFieldSchema(
    name: String,
    info: String? = null,
    internal val entityPathSchema: MutableEntityPathSchema,
    multiplicity: MutableMultiplicity = MutableMultiplicity(1, 1)
) : MutableFieldSchema(name, info, false, multiplicity), AssociationFieldSchema, EntityPathSchema by entityPathSchema {
    override fun visitSelf(visitor: SchemaVisitor): Boolean {
        return super.visitSelf(visitor) && visitor.visit(this)
    }

    override fun mutableVisitSelf(visitor: MutableSchemaVisitor): Boolean {
        return super.mutableVisitSelf(visitor) && visitor.mutableVisit(this)
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
) : MutableFieldSchema(name, info, isKey, multiplicity),
    CompositionFieldSchema {
    override var resolvedEntity: MutableEntitySchema
        get() = _resolvedEntity
            ?: throw IllegalStateException("Composition /${packageName}/${entityName} has not been resolved")
        internal set(value) {
            _resolvedEntity = value
        }
    internal var _resolvedEntity: MutableEntitySchema? = null
        private set

    override fun visitSelf(visitor: SchemaVisitor): Boolean {
        return super.visitSelf(visitor) && visitor.visit(this)
    }

    override fun mutableVisitSelf(visitor: MutableSchemaVisitor): Boolean {
        return super.mutableVisitSelf(visitor) && visitor.mutableVisit(this)
    }

    // The resolved type of this field is not considered a child and is therefore not traversed.
}

// Special Values

class MutableEntityPathSchema(override var entityPath: List<String>) : EntityPathSchema {
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
    override fun accept(visitor: SchemaVisitor): Boolean {
        return visitor.visit(this)
    }

    override fun mutableAccept(visitor: MutableSchemaVisitor): Boolean {
        return visitor.mutableVisit(this)
    }
}

class MutableByteSchema(
    override var constraints: MutableNumericConstraints<Byte>? = null
) : MutablePrimitiveSchema(), ByteSchema {
    override fun accept(visitor: SchemaVisitor): Boolean {
        return visitor.visit(this)
    }

    override fun mutableAccept(visitor: MutableSchemaVisitor): Boolean {
        return visitor.mutableVisit(this)
    }
}

class MutableShortSchema(
    override var constraints: MutableNumericConstraints<Short>? = null
) : MutablePrimitiveSchema(), ShortSchema {
    override fun accept(visitor: SchemaVisitor): Boolean {
        return visitor.visit(this)
    }

    override fun mutableAccept(visitor: MutableSchemaVisitor): Boolean {
        return visitor.mutableVisit(this)
    }
}

class MutableIntSchema(
    override var constraints: MutableNumericConstraints<Int>? = null
) : MutablePrimitiveSchema(), IntSchema {
    override fun accept(visitor: SchemaVisitor): Boolean {
        return visitor.visit(this)
    }

    override fun mutableAccept(visitor: MutableSchemaVisitor): Boolean {
        return visitor.mutableVisit(this)
    }
}

class MutableLongSchema(
    override var constraints: MutableNumericConstraints<Long>? = null
) : MutablePrimitiveSchema(), LongSchema {
    override fun accept(visitor: SchemaVisitor): Boolean {
        return visitor.visit(this)
    }

    override fun mutableAccept(visitor: MutableSchemaVisitor): Boolean {
        return visitor.mutableVisit(this)
    }
}

class MutableFloatSchema(
    override var constraints: MutableNumericConstraints<Float>? = null
) : MutablePrimitiveSchema(), FloatSchema {
    override fun accept(visitor: SchemaVisitor): Boolean {
        return visitor.visit(this)
    }

    override fun mutableAccept(visitor: MutableSchemaVisitor): Boolean {
        return visitor.mutableVisit(this)
    }
}

class MutableDoubleSchema(
    override var constraints: MutableNumericConstraints<Double>? = null
) : MutablePrimitiveSchema(), DoubleSchema {
    override fun accept(visitor: SchemaVisitor): Boolean {
        return visitor.visit(this)
    }

    override fun mutableAccept(visitor: MutableSchemaVisitor): Boolean {
        return visitor.mutableVisit(this)
    }
}

open class MutableStringSchema(
    override var constraints: MutableStringConstraints? = null
) : MutablePrimitiveSchema(), StringSchema {
    override fun accept(visitor: SchemaVisitor): Boolean {
        return visitor.visit(this)
    }

    override fun mutableAccept(visitor: MutableSchemaVisitor): Boolean {
        return visitor.mutableVisit(this)
    }
}

class MutablePassword1WaySchema(
    constraints: MutableStringConstraints? = null
) : MutableStringSchema(constraints), Password1WaySchema {
    override fun accept(visitor: SchemaVisitor): Boolean {
        return visitor.visit(this)
    }

    override fun mutableAccept(visitor: MutableSchemaVisitor): Boolean {
        return visitor.mutableVisit(this)
    }
}

class MutablePassword2WaySchema(
    constraints: MutableStringConstraints? = null
) : MutableStringSchema(constraints), Password2WaySchema {
    override fun accept(visitor: SchemaVisitor): Boolean {
        return visitor.visit(this)
    }

    override fun mutableAccept(visitor: MutableSchemaVisitor): Boolean {
        return visitor.mutableVisit(this)
    }
}

class MutableUuidSchema : MutablePrimitiveSchema(), UuidSchema {
    override fun accept(visitor: SchemaVisitor): Boolean {
        return visitor.visit(this)
    }

    override fun mutableAccept(visitor: MutableSchemaVisitor): Boolean {
        return visitor.mutableVisit(this)
    }
}

class MutableBlobSchema(
    override var constraints: MutableBlobConstraints? = null
) : MutablePrimitiveSchema(), BlobSchema {
    override fun accept(visitor: SchemaVisitor): Boolean {
        return visitor.visit(this)
    }

    override fun mutableAccept(visitor: MutableSchemaVisitor): Boolean {
        return visitor.mutableVisit(this)
    }
}

class MutableTimestampSchema : MutablePrimitiveSchema(), TimestampSchema {
    override fun accept(visitor: SchemaVisitor): Boolean {
        return visitor.visit(this)
    }

    override fun mutableAccept(visitor: MutableSchemaVisitor): Boolean {
        return visitor.mutableVisit(this)
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
