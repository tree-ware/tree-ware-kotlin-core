---
title: "Get Models"
layout: "titled"
nav_order: "c"
parent: "Contributor Docs"
---

{% include toc.md %}

# Introduction

The `get()` operator is for reading data from a DB. The inputs are a wildcard request model, and a DB mapping model. The
output is a response model containing the data requested.

The implementation uses three tricks which are described below.

# forEach()

As with most other operators in tree-ware, we use `forEach()` to implement this operator. We first need to decide which
models will be driver models and which models will be follower models.

Tree-ware maps each entity in the meta-model to a separate table in the DB. Each instance of an entity is stored as rows
in its corresponding table. The response for the top layer in the tree contains keys required for querying the next
layer of the tree. Since the response of one layer determines what queries to issue next, the response needs to drive
the traversal, and therefore the response model has to be the driver model! i.e. the output drives the creation of the
output! This is the first trick used by the implementation.

The output response model needs to be mutable so that it can be built up layer by layer. It starts off as a mutable
model with an empty root element. It is supplied as the driver model to the `forEach()` function that traverses 1
driver-model and 2 follower-models. The request model is follower 1, and the DB mapping model is follower 2.

`forEach()` does not mutate the driver (or follower) models, but it does not preclude external changes to the driver
models. The leader-cursor (used by `forEach()`) determines which element to visit next only when it is time to visit the
next element. So newly added elements get visited if they are subsequent elements (but not if they are previous
elements). This is the second trick used by the implementation.

# GetVisitor

The `GetVisitor` is initialized with a delegate that is responsible for reading data from the DB (more about the
delegate in the next section). The `GetVisitor` itself only coordinates the reading. Its `visit()` and `leave()` methods
are passed elements from the following 3 models: response, request, mapping.

If the response element is a composition (single or set) and there is a corresponding element in the DB mapping, then
the visitor asks the delegate to fetch the composition (single or set) from the DB, else it just tells `forEach()` to
continue.

The request specifies fields that are needed in a composition (single or set). The visitor passes all fields except
composition fields to the delegate; composition fields are not passed since they are separate rows and will be fetched
subsequently. The delegate adds fetched elements to the parent entity.

The visitor adds composition-fields that were not passed to the delegate to each of the new elements. These
composition-fields will be fetched subsequently when `forEach()` visits them. This is the third trick used by the
implementation.

# GetVisitorDelegate

This is an interface that DB-specific tree-ware libraries need to implement. The `GetVisitor` has to be initialized with
an instance of an implementation.

The fetch function in the delegate will be passed all the information needed to fetch the entities of a
composition-field (single or set):

1. name of the composition-field,
2. name-value pairs of all key fields along the path,
3. names of fields (columns) that are to be fetched,
4. the parent entity that contains the composition-field, so that the delegate can add the elements to the parent
   entity.