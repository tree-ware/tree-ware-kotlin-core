---
layout: default
title: Field Types
nav_order: 1
parent: User Docs
---

# Field Types

{: .no_toc }

---

<details open markdown="block">
  <summary>
    Table of contents
  </summary>
  {: .text-delta }
1. TOC
{: toc }
</details>

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
of the type (`unit` vs `int`)
. [Protocol Buffers](https://developers.google.com/protocol-buffers/docs/proto?csw=1#scalar)
go even further with two signed types (`int32` vs. `sint32`) with `sint32` optimized for storing negative numbers. At
the opposite end of the spectrum, there are programming languages and encoding formats (JSON) that have only a single
number type for all types of numbers.

So tree-ware has a choice to make for numbers: (1) use separate types with constraints included, or (2) use a single
number type with constraints defined separately. With option 2, pre-defined aliases will be needed for user convenience.
Since [aliases are not yet supported](https://github.com/tree-ware/tree-ware-kotlin-core/issues/85), and since aliases
are more verbose than pre-defined types, tree-ware chooses option 1, but with constraints for more esoteric types like
`sint32` in proto buffers.

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
entity in the set from another. The following property in JSON definition of a field in the meta-model determines
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

Signed integer fields (`int8`, `int16`, `int32`, `int64`) support the following constraints:

| Constraint | Values                        | Default | Description                                                 |
|------------|-------------------------------|---------|-------------------------------------------------------------|
| `negative` | `true` or `false`             | `false` | Whether the values are mostly negative                      |

### Strings

String fields support the following constraints:

| Constraint | Values           | Default       | Description                               |
|------------|------------------|---------------|-------------------------------------------|
| `length`   | unsigned integer | No constraint | Maximum string length                     |
| `regex`    | string           | No constraint | Regular expression that values must match |

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

# Compositions

Compositions are nested entities. They are the reason why the model is a tree. Entities are defined in the meta-model
and fields can use them as their type.

The following is an example meta-model definition for a composition field:

```json
{
  "name": "settings",
  "type": "composition",
  "composition": {
    "name": "address_book_settings",
    "package": "address_book.main"
  }
}
```

# Associations

Associations are like pointers to other nodes in the tree. Unlike pointers in programming languages, associations can be
serialized and sent to another machine, and the other machine will be able to use it. They work because they are paths
in the tree and not memory locations.

Association fields need to define the path to their target from the root of the tree. Since the path is defined in the
meta-model, runtime values are restricted to that path, with the only variation being the key values along the path.

The following is an example meta-model definition for an association field:

```json
{
  "name": "person",
  "type": "association",
  "association": [
    {
      "value": "address_book"
    },
    {
      "value": "person"
    }
  ]
}
```

The `"association"` array in the above example defines the path by listing the fields in the path from the root.