package org.tree_ware.core.schema

abstract class MutableElementSchema(override var name: String = "") : ElementSchema {
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

    fun mutableAccept(visitor: MutableSchemaVisitor): Boolean {
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

class MutablePackageSchema(name: String,
                           override var aliases: List<MutableAliasSchema> = listOf(),
                           override var enumerations: List<MutableEnumerationSchema> = listOf(),
                           override var entities: List<MutableEntitySchema> = listOf()
) : MutableElementSchema(name), PackageSchema {
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
) : MutableElementSchema(name), AliasSchema {
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

class MutableEnumerationSchema(name: String, override var values: List<String>) : MutableElementSchema(name), EnumerationSchema {
    override val objectType = "enumeration"

    override fun visitSelf(visitor: SchemaVisitor): Boolean {
        return super.visitSelf(visitor) && visitor.visit(this)
    }

    override fun mutableVisitSelf(visitor: MutableSchemaVisitor): Boolean {
        return super.mutableVisitSelf(visitor) && visitor.mutableVisit(this)
    }
}

class MutableEntitySchema(name: String, override var fields: List<MutableFieldSchema>
) : MutableElementSchema(name), EntitySchema {
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
) : MutableElementSchema(name), FieldSchema {
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
                              override var alias: MutableAliasSchema,
                              multiplicity: MutableMultiplicity = MutableMultiplicity(1, 1)
) : MutableFieldSchema(name, multiplicity), AliasFieldSchema {
    override val objectType = "alias_field"

    override fun visitSelf(visitor: SchemaVisitor): Boolean {
        return super.visitSelf(visitor) && visitor.visit(this)
    }

    override fun mutableVisitSelf(visitor: MutableSchemaVisitor): Boolean {
        return super.mutableVisitSelf(visitor) && visitor.mutableVisit(this)
    }

    override fun traverseChildren(visitor: SchemaVisitor): Boolean {
        return super.traverseChildren(visitor) && alias.accept(visitor)
    }

    override fun mutableTraverseChildren(visitor: MutableSchemaVisitor): Boolean {
        return super.mutableTraverseChildren(visitor) && alias.mutableAccept(visitor)
    }
}

class MutableEnumerationFieldSchema(name: String,
                                    override var enumeration: MutableEnumerationSchema,
                                    multiplicity: MutableMultiplicity = MutableMultiplicity(1, 1)
) : MutableFieldSchema(name, multiplicity), EnumerationFieldSchema {
    override val objectType = "enumeration_field"

    override fun visitSelf(visitor: SchemaVisitor): Boolean {
        return super.visitSelf(visitor) && visitor.visit(this)
    }

    override fun mutableVisitSelf(visitor: MutableSchemaVisitor): Boolean {
        return super.mutableVisitSelf(visitor) && visitor.mutableVisit(this)
    }

    override fun traverseChildren(visitor: SchemaVisitor): Boolean {
        return super.traverseChildren(visitor) && enumeration.accept(visitor)
    }

    override fun mutableTraverseChildren(visitor: MutableSchemaVisitor): Boolean {
        return super.mutableTraverseChildren(visitor) && enumeration.mutableAccept(visitor)
    }
}

class MutableEntityFieldSchema(name: String,
                               override var entity: MutableEntitySchema,
                               multiplicity: MutableMultiplicity = MutableMultiplicity(1, 1)
) : MutableFieldSchema(name, multiplicity), EntityFieldSchema {
    override val objectType = "entity_field"

    override fun visitSelf(visitor: SchemaVisitor): Boolean {
        return super.visitSelf(visitor) && visitor.visit(this)
    }

    override fun mutableVisitSelf(visitor: MutableSchemaVisitor): Boolean {
        return super.mutableVisitSelf(visitor) && visitor.mutableVisit(this)
    }

    override fun traverseChildren(visitor: SchemaVisitor): Boolean {
        return super.traverseChildren(visitor) && entity.accept(visitor)
    }

    override fun mutableTraverseChildren(visitor: MutableSchemaVisitor): Boolean {
        return super.mutableTraverseChildren(visitor) && entity.mutableAccept(visitor)
    }
}

// Predefined Primitives

abstract class MutablePrimitiveSchema : PrimitiveSchema {
    abstract fun mutableAccept(visitor: MutableSchemaVisitor): Boolean
}

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
