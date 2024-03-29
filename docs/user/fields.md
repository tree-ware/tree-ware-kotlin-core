---
title: "Fields"
layout: "titled"
nav_order: "b"
parent: "User Docs"
---

{% include toc.md %}

# Types, Constraints, Aliases

Every field in tree-ware needs to specify a type. The type indicates the set of legal values that a field can have. A
field can also specify constraints that further restricts the set of legal values.

This raises the question: why aren't constraints included in the type? Consider fields with string type. We may want one
field to have string values that are no more than 10 characters. We may want another field to have string values that
are no more than 30 characters. And we may want yet another field to contain strings with only lower case characters and
no more than 40 characters. The variation in possible constraints is too large to create specific string types. Hence,
the need to separate constraints from types.

This raises a question of convenience: what if we need to use the same constraints for a large number of fields? For
example, we may have many name fields in different entities and all the names need the same constraints. For this,
tree-ware supports aliases. An alias names a particular combination of a type and constraints. Fields can use aliases as
their type.

The example above of constrained-strings is a case where there are too many combinations. But there are other cases
where there will not be too many combinations. For example, the number type. In many programming languages, whether the
number has a decimal value or not is part of the type (`int` vs. `float`), the number of bits used to represent the
number is part of the type name (`int8` vs. `int32`, `float` vs. `double`), whether the values are signed or not is part
of the type (`unit` vs `int`).
[Protocol Buffers](https://developers.google.com/protocol-buffers/docs/proto?csw=1#scalar)
go even further with two signed types (`int32` vs. `sint32`) with `sint32` optimized for storing negative numbers. At
the opposite end of the spectrum, there are programming languages and encoding formats (JSON) that have only a single
number type for all types of numbers.

So tree-ware has a choice to make for numbers: (1) use separate types with constraints included, or (2) use a single
number type with constraints defined separately. With option 2, pre-defined aliases will be needed for user convenience.
Since [aliases are not yet supported](https://github.com/tree-ware/tree-ware-kotlin-core/issues/85), and since aliases
are more verbose than pre-defined types, tree-ware chooses option 1, but with constraints for more esoteric types like
`sint32` in proto buffers.

# Names and Numbers

Every field must have a name and a number. They must be unique within an entity (but can be repeated across entities).
The name is used in text formats (like JSON) and the number is used in binary formats (like Google protobufs or custom
tree-ware binary formats). Tree-ware field numbers follow Google protobuf
[field number constraints](https://developers.google.com/protocol-buffers/docs/proto3#assigning_field_numbers):

* Field numbers must be within one of the following open intervals: (0, 19000), (19999, 2^29)

# Multiplicity

The `multiplicity` of a field determines how many values the field can contain and how they are contained.

* `required`: a required single value (always 1 value). This is the default value if multiplicity is not specified.
* `optional`: an optional single value (0 or 1 value)
* `list`: 0 or more values contained in a list
    * Lists are not supported for compositions (entities)
* `set`: 0 or more values contained in a set
    * Sets are currently supported only for compositions (entities) and are
      [not yet supported for other types](https://github.com/tree-ware/tree-ware-kotlin-core/issues/47)

# Key Fields

Fields can be marked as key fields in the meta-model. Key fields help identify entities in a set and differentiate one
entity in the set from another. The following property in the JSON definition of a field in the meta-model determines
whether the field is a key field or not. It defaults to `false` if omitted.

```json
{
  "is_key": true
}
```

# Built-In Types

Built-in types are simple types like booleans and numbers as well as compound types like strings and blobs. These types
are built-in and not defined by users in the meta-model. On the other hand, types like aliases, enumerations,
compositions, and associations are defined by users in the meta-model.

The following is an example meta-model definition for a field with a built-in type (UUID):

```json
{
  "name": "id",
  "type": "uuid"
}
```

## Language Mappings

| tree-ware      | JSON                       | Kotlin              | Description                  | Constraints                    |
|----------------|----------------------------|---------------------|------------------------------|--------------------------------|
| `boolean`      | `true` or `false`          | `Boolean`           | Boolean values               |                                |
| `uint8`        | `number`                   | `UByte`             | Unsigned 8-bit integers      |                                |
| `uint16`       | `number`                   | `UShort`            | Unsigned 16-bit integers     |                                |
| `uint32`       | `number`                   | `UInt`              | Unsigned 32-bit integers     |                                |
| `uint64`       | `string`                   | `ULong`             | Unsigned 64-bit integers     |                                |
| `int8`         | `number`                   | `Byte`              | Signed 8-bit integers        | [&#128279;](#signed-integers)  |
| `int16`        | `number`                   | `Short`             | Signed 16-bit integers       | [&#128279;](#signed-integers)  |
| `int32`        | `number`                   | `Int`               | Signed 32-bit integers       | [&#128279;](#signed-integers)  |
| `int64`        | `string`                   | `Long`              | Signed 64-bit integers       | [&#128279;](#signed-integers)  |
| `float`        | `number`                   | `Float`             | 32-bit decimals              |                                |
| `double`       | `number`                   | `Double`            | 64-bit decimals              |                                |
| `big_integer`  | `string`                   | `BigInteger`        | Arbitrary precision integers |                                |
| `big_decimal`  | `string`                   | `BigDecimal`        | Arbitrary precision decimals |                                |
| `timestamp`    | `string`                   | `ULong`             | Milliseconds since the Epoch |                                |
| `string`       | `string`                   | `String`            | String values                | [&#128279;](#strings)          |
| `uuid`         | `string`                   | `String`            | UUID values                  |                                |
| `blob`         | base-64 encoded `string`   | `ByteArray`         | Binary data                  |                                |
| `password1way` | [`json`](#1-way-passwords) | `Password1wayModel` | Hashed passwords             | [&#128279;](#passwords)        |
| `password2way` | [`json`](#2-way-passwords) | `Password2wayModel` | Encrypted passwords          | [&#128279;](#passwords)        |

## Constraints

Only certain types of primitive fields support constraints. They are covered in the subsections below.

### Signed Integers

Signed integer fields (`int8`, `int16`, `int32`
, `int64`) [do not yet](https://github.com/tree-ware/tree-ware-kotlin-core/issues/93) support the following constraints:

| Constraint        | Values            | Default | Description                            |
|-------------------|-------------------|---------|----------------------------------------|
| `mostly_negative` | `true` or `false` | `false` | Whether the values are mostly negative |

### Strings

String fields support the following constraints:

| Constraint | Values           | Default       | Description                                |
|------------|------------------|---------------|--------------------------------------------|
| `min_size` | unsigned integer | No constraint | Minimum string length                      |
| `max_size` | unsigned integer | No constraint | Maximum string length                      |
| `regex`    | string           | No constraint | Regular expression that strings must match |

### Passwords

[Password constraints are not yet supported](https://github.com/tree-ware/tree-ware-kotlin-core/issues/86).

## JSON Value Representation

Some primitive values have custom JSON representations. They are covered in the subsections below.

NOTE: these are the JSON representations of the values, not the JSON representations of the field definitions in the
meta-model.

### 1-Way Passwords

Unhashed 1-way password:

```json
{
  "unhashed": "<unhashed-value>"
}
```

Hashed 1-way password:

```json
{
  "hashed": "<hashed-value>",
  "hash_version": 1
}
```

### 2-Way Passwords

Unencrypted 2-way password:

```json
{
  "unencrypted": "<unencrypted-value>"
}
```

Encrypted 2-way password:

```json
{
  "encrypted": "<encrypted-value>",
  "cipher_version": 1
}
```

# Aliases

[Aliases are not yet supported](https://github.com/tree-ware/tree-ware-kotlin-core/issues/85)

# Enumerations

Enumerations can be defined in the meta-model and fields can use them as their type.

The following is an example meta-model definition for an enumeration field:

```json
{
  "name": "relationship",
  "type": "enumeration",
  "enumeration": {
    "name": "address_book_relationship",
    "package": "address_book.main"
  }
}
```

Each enumeration-value must have a name and a number. They must be unique within an enumeration (but can be repeated
across enumerations). The name is used in text formats (like JSON) and the number is used in binary formats (like Google
protobufs or custom tree-ware binary formats). Tree-ware enumeration-value numbers follow Google protobuf
[enumeration-value number constraints](https://developers.google.com/protocol-buffers/docs/proto3#enum):

* First enumeration value number MUST be 0
* Other enumeration value numbers can be any valid 32-bit unsigned-integer value

# Compositions

Compositions are nested entities. They are the reason why the model is a tree. Entities are defined in the meta-model
and fields can use them as their type.

The following is an example meta-model definition for a composition field:

```json
{
  "name": "settings",
  "type": "composition",
  "composition": {
    "entity": "address_book_settings",
    "package": "address_book.main"
  }
}
```

# Associations

Associations are like pointers to other nodes in the tree. Unlike pointers in programming languages, associations can be
serialized and sent to another machine, and the other machine will be able to use it. They work because they are paths
in the tree and not memory locations.

Association fields need to define the target entity. In the model, associations can store any path that leads to an
entity of the type defined in the meta-model.

The following is an example meta-model definition for an association field:

```json
{
  "name": "person",
  "type": "association",
  "association": {
    "entity": "address_book_person",
    "package": "address_book.main"
  }
}
```

The `"association"` array in the above example defines the path by listing the fields in the path from the root.

# Conditionally-Existing Fields

Certain fields make sense only when other fields have certain values. They should not exist if the other fields do not
have the desired values.

NOTE: If the other fields do have the desired values, then these fields **_can_** exist, but if their
[multiplicity](#multiplicity) is `optional`, then they do not **_need_** to exist; they need to exist only if their
multiplicity is not `optional`.

The conditions can be specified as a boolean expression in the meta-model definition of the field using the `exists_if`
attribute. The boolean expression must currently be specified in Abstract Syntax Tree (AST) form; a simpler string
syntax will be supported in the future.

```json
{
  "exists_if": {
    "operator": "equals",
    "field": "protocol",
    "value": "ip"
  }
}
```

The field specified in the `equals` clause must be:

* a [required or optional](#multiplicity) field; it cannot be a collection (`list` or `set`) field
* a [built-in type](#built-in-types) or [enumeration](#enumerations) field
* in the same entity (current limitation, can be anywhere in the model in the future)

The value specified in the `equality` clause must be:

* of the same type as the field specified in that clause

The other boolean operators supported are `and`, `or`, `not`. The first two (`and`, `or`) must have two arguments `arg1`
and `arg2`, while `not` must have only one argument `arg1`.

```json
{
  "exists_if": {
    "operator": "and",
    "arg1": {
      "operator": "equals",
      "field": "protocol",
      "value": "ip"
    },
    "arg2": {
      "operator": "equals",
      "field": "version",
      "value": "6"
    }
  }
}
```

Conditionally-existing fields can be used to simulate [tagged-unions](https://en.wikipedia.org/wiki/Tagged_union) and
has the following advantages over tagged-unions:

* The tag can be read and used without the union being read.
* The tag can be anywhere in the model tree (but currently it must be in the same entity as the conditional field).
* More than one tag can be used in the boolean expression.
* The boolean expression can be elaborate with `not`, `and`, `or` operators.
* The tag can be used by more than one conditional field.
* Which values are expected for which tags are documented in the meta-model.
  * These expectations can be validated at runtime.

That being said, tagged-unions still have a place and will be supported eventually.

# Uniqueness

The composite of all the key fields in an entity need to be unique and the uniqueness is enforced by the storage system
used. In some cases, there can be non-key fields that are unique. This should be specified in the meta-model since it is
useful information about the model, and can also be used by the storage system to ensure/enforce uniqueness for those
fields.

A single field can be unique by itself, or a composite of multiple fields can be unique as a combination. To permit
both, uniqueness is specified at the entity level in the meta-model rather than at the field level. Multiple uniqueness
definitions can be specified for a single entity. Each definition must include the following:

* a definition name that is unique within the entity
* a uniqueness type:
    * `global` (default if not specified), or
    * `sub_tree` ([not yet supported](https://github.com/tree-ware/tree-ware-kotlin-mysql/issues/52))
* The names of the fields

The following is an example meta-model definition for uniqueness in an entity. Note that "unique" as a noun is archaic,
but convenient since it allows a plural to be used for the list of uniqueness definitions.

```json
{
  "uniques": [
    {
      "name": "serial_number",
      "type": "global",
      "fields": [
        {
          "value": "make"
        },
        {
          "value": "serial_number"
        }
      ]
    },
    {
      "name": "mac",
      "fields": [
        {
          "value": "mac_address"
        }
      ]
    }
  ]
}
```