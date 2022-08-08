---
title: "Meta-Models"
layout: "titled"
nav_order: "a"
parent: "User Docs"
---

{% include toc.md %}

# Introduction

The tree-ware meta-model defines the schema for the tree-ware model.

# Definition

A meta-model contains definitions for the root of the model tree and a set of packages. The definitions must currently
be in JSON format. They can all be defined in a single file or in multiple files. Typically, each package is defined in
its own file, and root definition is defined in its own file. The file names should be in `lower_snake_case`

The [meta-models used by the unit-tests] can be used as a reference.

# Packages

Packages contain a set of entities. Tree-ware package names should be like Java package names: dot-separated names
parts, starting with a reversed domain-name. All name parts must be in `lower_snake_case`.

# Entities

Entities contain a set of [fields](fields.md). All entity names must be in `lower_snake_case`.

# Root

The root definition specifies the root entity and is defined like a [composition field](fields.md#compositions).

[meta-models used by the unit-tests]: https://github.com/tree-ware/tree-ware-kotlin-core/tree/master/test-fixtures/src/commonMain/resources/metaModel