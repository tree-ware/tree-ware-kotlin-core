# Get Action Implementation

## Introduction

The `get()` action is for reading data from a DB (or other source). The inputs
are a wildcard request model, and a DB mapping model. The output is a response
model containing the data requested.

The implementation uses a few tricks which are described below.

## forEach()

As with most other operators and actions in tree-ware, we use `forEach()`
to implement this action. We first need to decide which models will be
driver models and which models will be follower models.

The DB stores each composition-list in a separate table. The response for the
top layer in the tree contains keys required for querying the next layer of
the tree. Since the response of one layer determines what queries to issue
next, the response needs to drive the traversal, and therefore the response
model has to be the driver model! i.e. the output drives the creation of the
output! This is the first trick used by the implementation.
  
The output response model needs to be mutable so that it can be built up
layer by layer. It starts off as a mutable model with an empty root element.
It is supplied as the driver model to the `forEach()` function that traverses
1 driver-model and 2 follower-models. The request model is follower 1, and
the DB mapping model is follower 2.

`forEach()` does not mutate the driver (or follower) models, but it does
not preclude external changes to the driver models. The leader-cursor (used
by `forEach()`) determines which element to visit next only when it is time
to visit the next element. So newly added elements get visited if they are
subsequent elements (but not if they are previous elements). This is the
second trick used by the implementation.

## GetVisitor

The `GetVisitor` is a marker-interface. It is passed elements from the
following 3 models: response, request, mapping. An implementation of this
interface should use the mapping model to fetch data requested in the request
model and populate the response model.

## CompositionTableGetVisitor

There are many ways to store a tree-ware model. One way is to store each
composition-list in a separate database table. The database table stores each
composition in the list as a row in the table. The `CompositionTableGetVisitor`
is a `GetVisitor` implementation for fetching data from such a store.

The `CompositionTableGetVisitor` is initialized with a delegate that is
responsible for fetching data (more about the delegate in the next section).

If the response element is a composition-list-element and there is a
corresponding element in the DB mapping, then the visitor asks the delegate to
fetch the elements of the list, else it just tells `forEach()` to continue.

The request specifies fields that are needed in a composition list. The
visitor passes these fields to the delegate. Since composition-lists are
separate tables, composition-list-fields are not passed to the delegate.
The delegate adds fetched elements to the parent entity.

The visitor adds composition-list-fields that were not passed to the delegate
to each of the new elements. These composition-list-fields will be fetched
later when `forEach()` visits them. This is the third trick used by the
implementation.

## CompositionTableGetVisitorDelegate

The `CompositionTableGetVisitorDelegate` delegates fetching of data to a
delegate which is passed to the `CompositionTableGetVisitorDelegate` when it
is constructed. This allows `CompositionTableGetVisitorDelegate` to be reused
with different types of data-sources by using data-source-specific delegates.

The fetch function in the delegate will be passed all the information needed
to fetch the entities of a composition-list:

1. name of the composition-list-field,
2. name-value pairs of all key fields along the path,
3. names of fields (in the entities in the list) that are to be fetched,
4. the parent entity that contains the composition-list-field, so that the
   delegate can add the elements to the parent entity.

Since the delegate might have to talk to a database to fetch the data, it is
a `suspend` function. Since the `CompositionTableGetVisitorDelegate` methods
call the delegate, the `CompositionTableGetVisitorDelegate` methods are also
`suspend` methods. Finally, since `forEach()` calls the `GetVisitor` methods,
`forEach()` is also a `suspend` function.

## CompositionTableFieldNameVisitor

Only composition-lists are stored as tables. Non-list compositions are flattened
and stored in the parent table. `CompositionTableFieldNameVisitor` creates
the flattened field names for the parent table.
