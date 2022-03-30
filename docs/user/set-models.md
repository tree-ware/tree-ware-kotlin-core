---
title: "Set Models"
layout: "titled"
nav_order: "d"
parent: "User Docs"
---

{% include toc.md %}

# Introduction

Tree-ware allows multiple entities to be set together in a single request by submitting a model tree. The response is a
list of errors if there are any errors.

# Model Aux Data

When setting a tree-ware model, `SetAux` data attached to (single or set) composition fields and entities specify if
the (composed) entities are to be created, updated, or deleted. A single model can contain a mix of entities to be
created, updated, and deleted. This aux data is valid only when attached to (single or set) composition fields or
entities; if it is attached to other elements, it is silently ignored without errors. If there is no valid `SetAux`
data, an error is returned.

SetAux data is inherited by elements in the sub-tree. So not all elements need to specify SetAux data.

When an entity is marked for creation, it applies to all entities in its subtree; if entities in the subtree are marked
for a different set operation, errors are returned for those entities.

When an entity is marked for update, entities in its subtree can be marked for any other set operation.

When an entity is marked for deletion, it implies that the entire subtree needs to be deleted. If entities in the
subtree are marked for a different set operation, errors are returned for those entities.

# Roadmap

* Moving recursive entities or entities under them from one parent to another