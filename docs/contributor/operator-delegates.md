---
title: "Operator Delegates"
layout: "titled"
nav_order: "c"
parent: "Contributor Docs"
---

{% include toc.md %}

# Introduction

Tree-ware has default handling for entities defined in a meta-model. But sometimes there is a need to handle a
particular entity in a custom way. For example, consider the following entity for the latitude/longitude of a location:

```json
{
  "name": "point",
  "fields": [
    {
      "name": "latitude",
      "type": "double"
    },
    {
      "name": "longitude",
      "type": "double"
    }
  ]
}
```

Tree-ware's default handling would be to use two numeric columns to store the fields of this entity in a DB. But if the
DB has support for geospatial columns, then those two fields might have to be saved in a single geospatial column.

To permit the above custom handling, model operators in tree-ware can delegate handling of entities to operator-specific
delegates.

# Operator Delegate Interfaces

Model operators support custom handling of entities by defining and using operator-specific delegate interfaces.

# Operator Delegate Registry

Implementations of operator delegate interfaces must be registered in a delegate registry when the system starts. The
registration must identify the entity in the meta-model, the operator, and provide an implementation for that operator's
delegate interface.

The entity is identified by its fully-qualified-name in the meta-model. For example, if the `point` entity in the
example above is defined in a meta-model package called `geo`, then its fully-qualified-name is `/geo/point`.

Operators are defined in different libraries and identifying them with strings cannot guarantee uniqueness. So operators
define objects that extend the `OperatorId` marker interface, and these objects are used as operators IDs.

There are two registries for performance reasons:

1. `OperatorDelegateRegistry` is the main registry and is used for registering entity IDs, operator IDs, and delegate
   implementations.
2. `DelegateRegistry` contains the entity ID to delegate implementation mappings for a single operator in order to speed
   up lookups in that operator.

# Operator Delegate Lookup

Model operators are provided access to the operator delegate registry. When they visit an entity, they look up the
registry for a delegate for that entity, and if one is available, they delegate handling of the entity to that delegate.

# Roadmap

* To reduce deployment package size, the build should automatically include only the delegates that are needed based on
  entities in the meta-model and operators used in the system.