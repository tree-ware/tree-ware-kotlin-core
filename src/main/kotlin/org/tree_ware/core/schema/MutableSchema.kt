package org.tree_ware.core.schema

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

abstract class MutableElementSchema() : ElementSchema, VisitableMutableSchema {
    abstract val objectType: String

    override fun accept(visitor: SchemaVisitor): Boolean {
        try {
            (visitor as? BracketedVisitor)?.objectStart(objectType)
            if (!visitSelf(visitor)) return false
            if (!traverseChildren(visitor)) return false
            return true
        } finally {
            (visitor as? BracketedVisitor)?.objectEnd()
        }
    }

    override fun mutableAccept(visitor: MutableSchemaVisitor): Boolean {
        try {
            (visitor as? BracketedVisitor)?.objectStart(objectType)
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

abstract class MutableNamedElementSchema(override var name: String = "") : MutableElementSchema(), NamedElementSchema {
    override fun visitSelf(visitor: SchemaVisitor): Boolean {
        return super.visitSelf(visitor) && visitor.visit(this)
    }

    override fun mutableVisitSelf(visitor: MutableSchemaVisitor): Boolean {
        return super.mutableVisitSelf(visitor) && visitor.mutableVisit(this)
    }
}

class MutableSchema() : MutableElementSchema(), Schema {
    override var packages: List<MutablePackageSchema> = listOf()
        internal set

    override val objectType = "schema"

    override fun visitSelf(visitor: SchemaVisitor): Boolean {
        return super.visitSelf(visitor) && visitor.visit(this)
    }

    override fun mutableVisitSelf(visitor: MutableSchemaVisitor): Boolean {
        return super.mutableVisitSelf(visitor) && visitor.mutableVisit(this)
    }

    override fun traverseChildren(visitor: SchemaVisitor): Boolean {
        if (!super.traverseChildren(visitor)) return false

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

class MutablePackageSchema(name: String,
                           override var aliases: List<MutableAliasSchema> = listOf(),
                           override var enumerations: List<MutableEnumerationSchema> = listOf(),
                           override var entities: List<MutableEntitySchema> = listOf()
) : MutableNamedElementSchema(name), PackageSchema {
    override val objectType = "package"

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

class MutableAliasSchema(name: String, override var primitive: MutablePrimitiveSchema
) : MutableNamedElementSchema(name), AliasSchema {
    override val objectType = "alias"

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

class MutableEnumerationSchema(name: String, override var values: List<String>) : MutableNamedElementSchema(name), EnumerationSchema {
    override val objectType = "enumeration"

    override fun visitSelf(visitor: SchemaVisitor): Boolean {
        return super.visitSelf(visitor) && visitor.visit(this)
    }

    override fun mutableVisitSelf(visitor: MutableSchemaVisitor): Boolean {
        return super.mutableVisitSelf(visitor) && visitor.mutableVisit(this)
    }
}

class MutableEntitySchema(name: String, override var fields: List<MutableFieldSchema>
) : MutableNamedElementSchema(name), EntitySchema {
    override val objectType = "entity"

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

abstract class MutableFieldSchema(name: String,
                                  override var multiplicity: MutableMultiplicity = MutableMultiplicity(1, 1)
) : MutableNamedElementSchema(name), FieldSchema {
    override fun visitSelf(visitor: SchemaVisitor): Boolean {
        return super.visitSelf(visitor) && visitor.visit(this)
    }

    override fun mutableVisitSelf(visitor: MutableSchemaVisitor): Boolean {
        return super.mutableVisitSelf(visitor) && visitor.mutableVisit(this)
    }
}

class MutablePrimitiveFieldSchema(name: String,
                                  override var primitive: MutablePrimitiveSchema,
                                  multiplicity: MutableMultiplicity = MutableMultiplicity(1, 1)
) : MutableFieldSchema(name, multiplicity), PrimitiveFieldSchema {
    override val objectType = "primitive_field"

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

class MutableAliasFieldSchema(name: String,
                              override var packageName: String,
                              override var aliasName: String,
                              multiplicity: MutableMultiplicity = MutableMultiplicity(1, 1)
) : MutableFieldSchema(name, multiplicity), AliasFieldSchema {
    override val objectType = "alias_field"

    override var resolvedAlias: MutableAliasSchema
        get() = _resolvedAlias ?: throw IllegalStateException("Alias ${packageName}.${aliasName} has not been resolved")
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

class MutableEnumerationFieldSchema(name: String,
                                    override var packageName: String,
                                    override var enumerationName: String,
                                    multiplicity: MutableMultiplicity = MutableMultiplicity(1, 1)
) : MutableFieldSchema(name, multiplicity), EnumerationFieldSchema {
    override val objectType = "enumeration_field"

    override var resolvedEnumeration: MutableEnumerationSchema
        get() = _resolvedEnumeration
                ?: throw IllegalStateException("Enumeration ${packageName}.${enumerationName} has not been resolved")
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

class MutableEntityFieldSchema(name: String,
                               override var packageName: String,
                               override var entityName: String,
                               multiplicity: MutableMultiplicity = MutableMultiplicity(1, 1)
) : MutableFieldSchema(name, multiplicity), EntityFieldSchema {
    override val objectType = "entity_field"

    override var resolvedEntity: MutableEntitySchema
        get() = _resolvedEntity
                ?: throw IllegalStateException("Entity ${packageName}.${entityName} has not been resolved")
        internal set(value) {
            _resolvedEntity = value
        }
    private var _resolvedEntity: MutableEntitySchema? = null

    override fun visitSelf(visitor: SchemaVisitor): Boolean {
        return super.visitSelf(visitor) && visitor.visit(this)
    }

    override fun mutableVisitSelf(visitor: MutableSchemaVisitor): Boolean {
        return super.mutableVisitSelf(visitor) && visitor.mutableVisit(this)
    }

    // The resolved type of this field is not considered a child and is therefore not traversed.
}

// Predefined Primitives

abstract class MutablePrimitiveSchema : PrimitiveSchema, VisitableMutableSchema

class MutableBooleanSchema : MutablePrimitiveSchema(), BooleanSchema {
    override fun accept(visitor: SchemaVisitor): Boolean {
        return visitor.visit(this)
    }

    override fun mutableAccept(visitor: MutableSchemaVisitor): Boolean {
        return visitor.mutableVisit(this)
    }
}

class MutableNumericSchema<T : Number>(override var constraints: MutableNumericConstraints<T>? = null
) : MutablePrimitiveSchema(), NumericSchema<T> {
    override fun accept(visitor: SchemaVisitor): Boolean {
        return visitor.visit(this)
    }

    override fun mutableAccept(visitor: MutableSchemaVisitor): Boolean {
        return visitor.mutableVisit(this)
    }
}

open class MutableStringSchema(override var constraints: MutableStringConstraints? = null
) : MutablePrimitiveSchema(), StringSchema {
    override fun accept(visitor: SchemaVisitor): Boolean {
        return visitor.visit(this)
    }

    override fun mutableAccept(visitor: MutableSchemaVisitor): Boolean {
        return visitor.mutableVisit(this)
    }
}

class MutablePassword1WaySchema(constraints: MutableStringConstraints? = null
) : MutableStringSchema(constraints), Password1WaySchema {
    override fun accept(visitor: SchemaVisitor): Boolean {
        return visitor.visit(this)
    }

    override fun mutableAccept(visitor: MutableSchemaVisitor): Boolean {
        return visitor.mutableVisit(this)
    }
}

class MutablePassword2WaySchema(constraints: MutableStringConstraints? = null
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

class MutableBlobSchema(override var constraints: MutableBlobConstraints? = null
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

class MutableIpv4AddressSchema : MutablePrimitiveSchema(), Ipv4AddressSchema {
    override fun accept(visitor: SchemaVisitor): Boolean {
        return visitor.visit(this)
    }

    override fun mutableAccept(visitor: MutableSchemaVisitor): Boolean {
        return visitor.mutableVisit(this)
    }
}

class MutableIpv6AddressSchema : MutablePrimitiveSchema(), Ipv6AddressSchema {
    override fun accept(visitor: SchemaVisitor): Boolean {
        return visitor.visit(this)
    }

    override fun mutableAccept(visitor: MutableSchemaVisitor): Boolean {
        return visitor.mutableVisit(this)
    }
}

class MutableMacAddressSchema : MutablePrimitiveSchema(), MacAddressSchema {
    override fun accept(visitor: SchemaVisitor): Boolean {
        return visitor.visit(this)
    }

    override fun mutableAccept(visitor: MutableSchemaVisitor): Boolean {
        return visitor.mutableVisit(this)
    }
}

// Constraints

class MutableMultiplicity(override var min: Long, override var max: Long) : Multiplicity

class MutableNumericConstraints<T : Number>(override var minBound: MutableNumericBound<T>?,
                                            override var maxBound: MutableNumericBound<T>?) : NumericConstraints<T>

class MutableNumericBound<T : Number>(override var value: T, override var inclusive: Boolean) : NumericBound<T>

class MutableStringConstraints(override var minLength: Int? = null,
                               override var maxLength: Int? = null,
                               override var regex: String? = null) : StringConstraints

class MutableBlobConstraints(override var minLength: Int?, override var maxLength: Int?) : BlobConstraints
