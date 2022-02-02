---
title: "Meta-Model Interfaces"
layout: "titled"
nav_order: "d"
parent: "User Docs"
---

{% include toc.md %}

# Introduction

Meta-model interfaces are like interfaces in programming languages: they define what an implementation should provide.

[Meta-model interfaces are not yet supported](https://github.com/tree-ware/tree-ware-kotlin-core/issues/102).

# Entity Interfaces

TODO: where are interfaces defined?

An entity in the meta-model can declare (TODO) that it implements an interface. The entity must include all the fields
defined in the interface. The entity can have additional fields. If the required fields are missing, meta-model
validation will fail.

# Benefits

Interfaces permit polymorphism and the benefit of polymorphism is the ability to reuse higher-level abstractions. When
higher-level code uses a polymorphic interface, it is hidden from implementation details below the interface. This
allows higher-level code to be reused with different lower-level implementations of the interface.

Tree-ware has high-level abstractions that can be used with different product-specific meta-models. For example, RBAC
requires access to users and roles. Different products may define users and roles at different paths in their model
trees. Different products may have different fields in the user and role entities. Different products may store these
entities in different storage systems. The RBAC library in tree-ware requires certain fields in the user and role
entities in order to perform its RBAC duties. To ensure that the actual user and role entities in the product meta-model
have the required fields, the RBAC library in tree-ware defines interfaces for user and role entities. Any product that
wants to use the RBAC library will have to implement these interfaces in its meta-model. The RBAC library is then
reusable across different products and their differences are immaterial to the library.