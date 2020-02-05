package org.tree_ware.core.schema

interface ElementSchema {
    val name: String

    /**
     * Accepts a visitor and traverses the schema with it (Visitor Pattern).
     *
     * If the visitor implements `BracketedVisitor`, then those methods will be called as well.
     *
     * @returns `true` to proceed with schema traversal, `false` to stop schema traversal.
     */
    fun accept(visitor: SchemaVisitor): Boolean
}

/** Schema for a user-defined package. */
interface PackageSchema : ElementSchema {
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
interface AliasSchema : ElementSchema {
    val primitive: PrimitiveSchema
}

/** Schema for user-defined enumerations. */
interface EnumerationSchema : ElementSchema {
    val values: List<String>
}

/** Schema for user-defined entities. */
interface EntitySchema : ElementSchema {
    val fields: List<FieldSchema>
}

// Fields

/**
 * Schema for fields in an entity.
 * There are subtypes for the schema of different types of fields: primitive
 * fields, alias fields, enum fields, and entity fields.
 */
interface FieldSchema : ElementSchema {
    val multiplicity: Multiplicity
}

/** Schema for fields whose types are predefined primitives. */
interface PrimitiveFieldSchema : FieldSchema {
    val primitive: PrimitiveSchema
}

// TODO(deepak-nulu): ability to refer to aliases, enumerations, entities defined in different packages.

/** Schema for fields whose types are user-defined primitive aliases. */
interface AliasFieldSchema : FieldSchema {
    val alias: AliasSchema
}

/** Schema for fields whose types are user-defined enumerations. */
interface EnumerationFieldSchema : FieldSchema {
    val enumeration: EnumerationSchema
}

/** Schema for fields whose types are user-defined entities. */
interface EntityFieldSchema : FieldSchema {
    val entity: EntitySchema
    // TODO(deepak-nulu): composition vs reference
}

// Predefined Primitives

/**
 * Schema for primitives.
 * There are sub-interfaces for each type of primitive.
 */
interface PrimitiveSchema {
    /**
     * Accepts a visitor and traverses the schema with it (Visitor Pattern).
     *
     * @returns `true` to proceed with schema traversal, `false` to stop schema traversal.
     */
    fun accept(visitor: SchemaVisitor): Boolean
}

/** Schema for boolean primitive. */
interface BooleanSchema : PrimitiveSchema

/** Schema for numeric primitives. */
interface NumericSchema<T : Number> : PrimitiveSchema {
    val constraints: NumericConstraints<T>?
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
interface TimestampSchema: PrimitiveSchema

/** Schema for IPv4 address primitive. */
interface Ipv4AddressSchema: PrimitiveSchema

/** Schema for IPv6 address primitive. */
interface Ipv6AddressSchema: PrimitiveSchema

/** Schema for MAC address primitive. */
interface MacAddressSchema: PrimitiveSchema

// Constraints

/** Constraints on the cardinality of a field. */
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
