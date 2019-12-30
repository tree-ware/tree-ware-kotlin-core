package org.tree_ware.core.schema

abstract class MutableElementSchema(override var name: String = "") : ElementSchema

class MutablePackageSchema(name: String,
                           override var aliases: List<MutableAliasSchema> = listOf(),
                           override var enumerations: List<MutableEnumerationSchema> = listOf(),
                           override var entities: List<MutableEntitySchema> = listOf()
) : MutableElementSchema(name), PackageSchema {
    override fun accept(visitor: SchemaVisitor): Boolean {
        try {
            // Visit the package.
            (visitor as? BracketedVisitor)?.objectStart("package")
            if (!visitor.visit(this)) return false

            // Traverse the children.
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
        } finally {
            (visitor as? BracketedVisitor)?.objectEnd()
        }
    }
}

class MutableAliasSchema(name: String, override var primitive: MutablePrimitiveSchema
) : MutableElementSchema(name), AliasSchema {
    override fun accept(visitor: SchemaVisitor): Boolean {
        try {
            // Visit the alias.
            (visitor as? BracketedVisitor)?.objectStart("alias")
            if (!visitor.visit(this)) return false

            // Traverse the children.
            if (!primitive.accept(visitor)) return false

            return true
        } finally {
            (visitor as? BracketedVisitor)?.objectEnd()
        }
    }
}

class MutableEnumerationSchema(name: String, override var values: List<String>) : MutableElementSchema(name), EnumerationSchema {
    override fun accept(visitor: SchemaVisitor): Boolean {
        try {
            // Visit the enumeration.
            (visitor as? BracketedVisitor)?.objectStart("enumeration")
            return visitor.visit(this)
        } finally {
            (visitor as? BracketedVisitor)?.objectEnd()
        }
    }
}

class MutableEntitySchema(name: String, override var fields: List<MutableFieldSchema>
) : MutableElementSchema(name), EntitySchema {
    override fun accept(visitor: SchemaVisitor): Boolean {
        try {
            // Visit the entity.
            (visitor as? BracketedVisitor)?.objectStart("entity")
            if (!visitor.visit(this)) return false

            // Traverse the children.
            if (fields.isNotEmpty()) try {
                (visitor as? BracketedVisitor)?.listStart("fields")
                for (field in fields) {
                    if (!field.accept(visitor)) return false
                }
            } finally {
                (visitor as? BracketedVisitor)?.listEnd()
            }

            return true
        } finally {
            (visitor as? BracketedVisitor)?.objectEnd()
        }
    }
}

// Fields

abstract class MutableFieldSchema(override var multiplicity: MutableMultiplicity = MutableMultiplicity(1, 1)
) : MutableElementSchema(), FieldSchema

class MutablePrimitiveFieldSchema(override var primitive: MutablePrimitiveSchema,
                                  multiplicity: MutableMultiplicity = MutableMultiplicity(1, 1)
) : MutableFieldSchema(multiplicity), PrimitiveFieldSchema {
    override fun accept(visitor: SchemaVisitor): Boolean {
        try {
            // Visit the primitive field.
            (visitor as? BracketedVisitor)?.objectStart("primitive_field")
            if (!visitor.visit(this)) return false

            // Traverse the children.
            if (!primitive.accept(visitor)) return false

            return true
        } finally {
            (visitor as? BracketedVisitor)?.objectEnd()
        }
    }
}

class MutableAliasFieldSchema(override var alias: MutableAliasSchema,
                              multiplicity: MutableMultiplicity = MutableMultiplicity(1, 1)
) : MutableFieldSchema(multiplicity), AliasFieldSchema {
    override fun accept(visitor: SchemaVisitor): Boolean {
        try {
            // Visit the alias field.
            (visitor as? BracketedVisitor)?.objectStart("alias_field")
            if (!visitor.visit(this)) return false

            // Traverse the children.
            if (!alias.accept(visitor)) return false

            return true
        } finally {
            (visitor as? BracketedVisitor)?.objectEnd()
        }
    }
}

class MutableEnumerationFieldSchema(override var enumeration: MutableEnumerationSchema,
                                    multiplicity: MutableMultiplicity = MutableMultiplicity(1, 1)
) : MutableFieldSchema(multiplicity), EnumerationFieldSchema {
    override fun accept(visitor: SchemaVisitor): Boolean {
        try {
            // Visit the enumeration field.
            (visitor as? BracketedVisitor)?.objectStart("enumeration_field")
            if (!visitor.visit(this)) return false

            // Traverse the children.
            if (!enumeration.accept(visitor)) return false

            return true
        } finally {
            (visitor as? BracketedVisitor)?.objectEnd()
        }
    }
}

class MutableEntityFieldSchema(override var entity: MutableEntitySchema,
                               multiplicity: MutableMultiplicity = MutableMultiplicity(1, 1)
) : MutableFieldSchema(multiplicity), EntityFieldSchema {
    override fun accept(visitor: SchemaVisitor): Boolean {
        try {
            // Visit the enumeration field.
            (visitor as? BracketedVisitor)?.objectStart("entity_field")
            if (!visitor.visit(this)) return false

            // Traverse the children.
            if (!entity.accept(visitor)) return false

            return true
        } finally {
            (visitor as? BracketedVisitor)?.objectEnd()
        }
    }
}

// Predefined Primitives

abstract class MutablePrimitiveSchema : PrimitiveSchema

class MutableBooleanSchema : MutablePrimitiveSchema(), BooleanSchema {
    override fun accept(visitor: SchemaVisitor): Boolean {
        return visitor.visit(this)
    }
}

class MutableNumericSchema<T : Number>(override var constraints: MutableNumericConstraints<T>? = null
) : MutablePrimitiveSchema(), NumericSchema<T> {
    override fun accept(visitor: SchemaVisitor): Boolean {
        return visitor.visit(this)
    }
}

open class MutableStringSchema(override var constraints: MutableStringConstraints? = null
) : MutablePrimitiveSchema(), StringSchema {
    override fun accept(visitor: SchemaVisitor): Boolean {
        return visitor.visit(this)
    }
}

class MutablePassword1WaySchema(constraints: MutableStringConstraints? = null
) : MutableStringSchema(constraints), Password1WaySchema {
    override fun accept(visitor: SchemaVisitor): Boolean {
        return visitor.visit(this)
    }
}

class MutablePassword2WaySchema(constraints: MutableStringConstraints? = null
) : MutableStringSchema(constraints), Password2WaySchema {
    override fun accept(visitor: SchemaVisitor): Boolean {
        return visitor.visit(this)
    }
}

class MutableUuidSchema : MutablePrimitiveSchema(), UuidSchema {
    override fun accept(visitor: SchemaVisitor): Boolean {
        return visitor.visit(this)
    }
}

class MutableBlobSchema(override var constraints: MutableBlobConstraints? = null
) : MutablePrimitiveSchema(), BlobSchema {
    override fun accept(visitor: SchemaVisitor): Boolean {
        return visitor.visit(this)
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
