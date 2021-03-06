package org.tree_ware.schema.core

// TODO(deepak-nulu): drop "Schema" prefix from enum class name
// TODO(deepak-nulu): move to a common package
enum class SchemaTraversalAction {
    /** Continue traversal. */
    CONTINUE,

    /** Abort traversal of the current sub-tree of the schema. */
    ABORT_SUB_TREE,

    /** Abort traversal of the entire schema. */
    ABORT_TREE
}

fun or(a: SchemaTraversalAction, b: SchemaTraversalAction): SchemaTraversalAction =
    if (a == SchemaTraversalAction.ABORT_TREE || b == SchemaTraversalAction.ABORT_TREE) SchemaTraversalAction.ABORT_TREE
    else if (a == SchemaTraversalAction.ABORT_SUB_TREE || b == SchemaTraversalAction.ABORT_SUB_TREE) SchemaTraversalAction.ABORT_SUB_TREE
    else SchemaTraversalAction.CONTINUE

interface VisitableSchema {
    /**
     * Traverses the schema element and visits it and its sub-elements (Visitor Pattern).
     * Traversal continues or aborts (partially or fully) based on the value returned by the visitor.
     */
    fun traverse(visitor: SchemaVisitor<SchemaTraversalAction>): SchemaTraversalAction

    /**
     * Visits the schema element without traversing its sub-elements.
     * Leave methods are NOT called.
     * Returns what the visitor returns.
     */
    fun <T> dispatch(visitor: SchemaVisitor<T>): T
}

interface ElementSchema : VisitableSchema {
    val id: String
}

interface NamedElementSchema : ElementSchema {
    val name: String
    val info: String?

    val fullName: String
    val parent: ElementSchema
}

/** The entire schema. */
interface Schema : ElementSchema {
    val root: RootSchema
    val packages: List<PackageSchema>
}

/** Schema for the root of the model tree. */
interface RootSchema : NamedElementSchema {
    val packageName: String
    val entityName: String

    override val parent: PackageSchema
    val resolvedEntity: EntitySchema
}

/** Schema for a user-defined package. */
interface PackageSchema : NamedElementSchema {
    val aliases: List<AliasSchema>
    val enumerations: List<EnumerationSchema>
    val entities: List<EntitySchema>

    override val parent: Schema
}

/**
 * Schema for user-defined primitive aliases.
 * Primitive aliases allow constrained primitive types to be reused without
 * having to repeat the constraints at every field that needs those
 * constraints.
 */
interface AliasSchema : NamedElementSchema {
    val primitive: PrimitiveSchema

    override val parent: PackageSchema
}

/** Schema for user-defined enumerations. */
interface EnumerationSchema : NamedElementSchema {
    val values: List<EnumerationValueSchema>

    override val parent: PackageSchema

    // TODO(deepak-nulu): optimize
    fun valueFromString(string: String): EnumerationValueSchema? = values.find { it.name == string }
}

/** Schema for user-defined enumeration values. */
interface EnumerationValueSchema : NamedElementSchema {
    override val parent: EnumerationSchema
}

/** Schema for user-defined entities. */
interface EntitySchema : NamedElementSchema {
    val fields: List<FieldSchema>

    override val parent: PackageSchema

    // TODO(deepak-nulu): optimize
    fun getField(name: String): FieldSchema? {
        return fields.find { it.name == name }
    }
}

// Fields

/**
 * Schema for fields in an entity.
 * There are subtypes for the schema of different types of fields: primitive
 * fields, alias fields, enum fields, and entity fields.
 */
interface FieldSchema : NamedElementSchema {
    val isKey: Boolean
    val multiplicity: Multiplicity

    override val parent: EntitySchema
}

/** Schema for fields whose types are predefined primitives. */
interface PrimitiveFieldSchema : FieldSchema {
    val primitive: PrimitiveSchema
}

/** Schema for fields whose types are user-defined primitive aliases. */
interface AliasFieldSchema : FieldSchema {
    val packageName: String
    val aliasName: String

    val resolvedAlias: AliasSchema
}

/** Schema for fields whose types are user-defined enumerations. */
interface EnumerationFieldSchema : FieldSchema {
    val packageName: String
    val enumerationName: String

    val resolvedEnumeration: EnumerationSchema
}

/** Schema for fields that are associations to user-defined entities. */
interface AssociationFieldSchema : FieldSchema, EntityPathSchema

/** Schema for fields that are compositions of user-defined entities. */
interface CompositionFieldSchema : FieldSchema {
    val packageName: String
    val entityName: String

    val resolvedEntity: EntitySchema
}

// Special Values

interface EntityPathSchema {
    val entityPath: List<String>

    val pathEntities: List<EntitySchema>
    val keyPath: List<String>
    val keyEntities: List<EntitySchema>
    val resolvedEntity: EntitySchema
}

// Predefined Primitives

/**
 * Schema for primitives.
 * There are sub-interfaces for each type of primitive.
 */
interface PrimitiveSchema : VisitableSchema

/** Schema for boolean primitive. */
interface BooleanSchema : PrimitiveSchema

/** Schema for numeric byte primitives. */
interface ByteSchema : PrimitiveSchema {
    val constraints: NumericConstraints<Byte>?
}

/** Schema for numeric byte primitives. */
interface ShortSchema : PrimitiveSchema {
    val constraints: NumericConstraints<Short>?
}

/** Schema for numeric byte primitives. */
interface IntSchema : PrimitiveSchema {
    val constraints: NumericConstraints<Int>?
}

/** Schema for numeric byte primitives. */
interface LongSchema : PrimitiveSchema {
    val constraints: NumericConstraints<Long>?
}

/** Schema for numeric byte primitives. */
interface FloatSchema : PrimitiveSchema {
    val constraints: NumericConstraints<Float>?
}

/** Schema for numeric byte primitives. */
interface DoubleSchema : PrimitiveSchema {
    val constraints: NumericConstraints<Double>?
}

/** Schema for string primitive. */
interface StringSchema : PrimitiveSchema {
    val constraints: StringConstraints?
}

/** Schema for password primitive that can be encrypted but not decrypted (1-way). */
interface Password1WaySchema : StringSchema

/** Schema for password primitive that can be encrypted as well as decrypted (2-way). */
interface Password2WaySchema : StringSchema

/** Schema for UUID fields. */
interface UuidSchema : PrimitiveSchema

/** Schema for blob primitive that can store binary data. */
interface BlobSchema : PrimitiveSchema {
    val constraints: BlobConstraints?
}

/** Schema for timestamp (milliseconds since epoch) primitive. */
interface TimestampSchema : PrimitiveSchema

// Constraints

/**
 * Constraints on the cardinality of a field.
 * Defaults to `[1, 1]`
 * A `max` value of `0` indicates that there is no explicit upper bound.
 */
interface Multiplicity {
    val min: Long
    val max: Long

    fun isRequired(): Boolean {
        return min == 1L && max == 1L
    }

    fun isOptional(): Boolean {
        return min == 0L && max == 1L
    }

    fun isList(): Boolean {
        return max != 1L
    }
}

/** Constraints for numeric fields. */
interface NumericConstraints<T : Number> {
    val minBound: NumericBound<T>?
    val maxBound: NumericBound<T>?
}

/** An inclusive/exclusive bound for a numeric field. */
interface NumericBound<T : Number> {
    val value: T
    val inclusive: Boolean
}

/** Constraints for string fields. */
interface StringConstraints {
    val minLength: Int?
    val maxLength: Int?
    val regex: String?
}

/** Constraints for blob fields. */
interface BlobConstraints {
    val minLength: Int?
    val maxLength: Int?
}
