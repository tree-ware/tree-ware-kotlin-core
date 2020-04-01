package org.tree_ware.core.schema

interface VisitableSchema {
    /**
     * Accepts a visitor and traverses the schema with it (Visitor Pattern).
     *
     * If the visitor implements `BracketedVisitor`, then those methods will be called as well.
     *
     * @returns `true` to proceed with schema traversal, `false` to stop schema traversal.
     */
    fun accept(visitor: SchemaVisitor): Boolean
}

interface ElementSchema : VisitableSchema

interface NamedElementSchema : ElementSchema {
    val name: String
    val fullName: String?
}

/** The entire schema. */
interface Schema : ElementSchema {
    val packages: List<PackageSchema>
}

/** Schema for a user-defined package. */
interface PackageSchema : NamedElementSchema {
    val root: CompositionFieldSchema?
    val aliases: List<AliasSchema>
    val enumerations: List<EnumerationSchema>
    val entities: List<EntitySchema>
}

/**
 * Schema for user-defined primitive aliases.
 * Primitive aliases allow constrained primitive types to be reused without
 * having to repeat the constraints at every field that needs those
 * constraints.
 */
interface AliasSchema : NamedElementSchema {
    val primitive: PrimitiveSchema
}

/** Schema for user-defined enumerations. */
interface EnumerationSchema : NamedElementSchema {
    val values: List<EnumerationValueSchema>
}

/** Schema for user-defined enumeration values. */
interface EnumerationValueSchema : NamedElementSchema

/** Schema for user-defined entities. */
interface EntitySchema : NamedElementSchema {
    val fields: List<FieldSchema>
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
interface AssociationFieldSchema : FieldSchema {
    val entityPath: List<String>

    val resolvedEntity: EntitySchema
}

/** Schema for fields that are compositions of user-defined entities. */
interface CompositionFieldSchema : FieldSchema {
    val packageName: String
    val entityName: String

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
