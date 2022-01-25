---
title: "Set Models"
layout: "titled"
nav_order: "b"
parent: "Contributor Docs"
---

{% include toc.md %}

# Introduction

The `set()` operator is for setting a model in a DB. The input is a model and the output is a list of commands (specific
to the DB) and an error model if there are any errors.

# Model Aux Data

When setting a tree-ware model, aux data is used in the model at the entity level to specify if the entity is to be
created, updated, or deleted. A single model can contain a mix of entities to be created, updated, and deleted.

When an entity is marked for creation, it applies to all entities in its subtree; if entities in the subtree are marked
for a different set operation, then the `set()` operator returns an error model with errors for those entities.

When an entity is marked for update, entities in its subtree can be marked for any other set operation.

When an entity is marked for deletion, there is no need to specify any entities in its sub-tree, because it implies that
the entire subtree needs to be deleted. If entities in the subtree are specified, and they are marked for a different
set operation, then the `set()` operator returns an error model with errors for those entities.

TODO: how can entities (recursive entities or entities under them) be moved from one parent to another?

# forEach()

Tree-ware maps entities to rows in a table. To store a row, its keys are needed. So the `set()` operator uses
`forEach()` with options to visit key fields first and composition fields last. These options result in a breadth-first
traversal of the model and help the operator write rows to the DB without having to wait for the traversal to leave the
entity. Without these options, the operator would need to maintain a lot of state since the key fields in the entity
could be after composition fields.

Visiting the key fields first also helps the operator build parent paths to be stored with each entity in the DB. These
parent paths need to be the same for all models of a meta-model, so `forEach()` visits key fields in the order defined
in the meta-model. Meta-model evolution prevents addition of key fields, but the order in which fields are defined in
the meta-model can change. To avoid this affecting old data already stored in the DB, `forEach()` visits key fields in
ascending order of their field numbers.

# SetVisitor

The `SetVisitor` is initialized with a delegate that is responsible for writing data to the DB (more about the delegate
in the next section). The `SetVisitor` itself only coordinates the writing. As model elements are visited, the
`SetVisitor` builds a parent-path, and when all the fields for a DB row are visited, the `SetVisitor` passes the
information to the delegate and lets the delegate do the actual writing to the DB. The `SetVisitor` incorporates any
errors returned by the delegate into an error model which the operator returns at the end.

# SetVisitorDelegate

This is an interface that DB-specific tree-ware libraries need to implement. The `SetVisitor` has to be initialized with
an instance of an implementation. The implementation will be passed information necessary for saving an entity as a
table row in the DB. For example, it is passed the set-type (create/update/delete), the parent-path, entity info, field
info. The delegate returns any errors that it runs into while writing to the DB.