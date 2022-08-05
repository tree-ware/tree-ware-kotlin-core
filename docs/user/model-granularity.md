---
title: "Model Granularity"
layout: "titled"
nav_order: "d"
parent: "User Docs"
---

{% include toc.md %}

# Introduction

Tree-ware allows fine-grained (set/get) access to individual fields in a model. Other systems that a tree-ware based
system interacts with may not be able to handle this fine field-level granularity. So tree-ware supports coarser
granularities. Different granularities can be chosen for different parts of the model tree.

# Granularities

* `field`: this is the default level if granularity is not specified.
* `entity`: not yet supported. an entity is the atomic unit.
* `sub_tree`: a sub-tree is the atomic unit.

# Meta-model

Granularity can only be specified for composition-fields. The definition for composition-fields supports an
optional `granularity` field:

```json
{
  "name": "settings",
  "type": "composition",
  "granularity": "sub_tree"
}
```

As mentioned previously, granularity defaults to `field` if not specified.

# API

The API cannot support the specified granularity in some cases. For example, individual fields cannot be created; the
minimum granularity for creation is an entity. And the get-API is able to support field level granularity irrespective
of the granularity specified in the meta-model. The following table indicates what the API is able to support.

| API Operation | Meta-Model Granularity | API Granularity                                                       |
|---------------|------------------------|-----------------------------------------------------------------------|
| `CREATE`      | `field`                | Entity :rage:                                                         |
|               | `entity`               | Entity                                                                |
|               | `sub_tree`             | Sub-tree                                                              |
| `UPDATE`      | `field`                | Field                                                                 |
|               | `entity`               | Entity                                                                |
|               | `sub_tree`             | Sub-tree                                                              |
| `DELETE`      | `field`                | Entity :rage:                                                         |
|               | `entity`               | Entity                                                                |
|               | `sub_tree`             | Sub-tree                                                              |
| get           | `field`                | Only fields specified in the request must be returned                 |
|               | `entity`               | All fields in entity must be returned if the request specifies none   |
|               | `sub_tree`             | All fields in sub-tree must be returned if the request specifies none |

# Storage

Every storage supported by tree-ware is free to choose how to store fields based on their granularity as long as they
can meet the above API requirements.