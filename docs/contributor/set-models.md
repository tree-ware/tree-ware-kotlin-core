---
title: "Set Models"
layout: "titled"
nav_order: "b"
parent: "Contributor Docs"
---

{% include toc.md %}

# Introduction

The `set()` operator is for setting a model in a DB. The input is a model, the model is written to the DB as a side
effect, and the output is a list of errors if there are any errors.

# set() operator

The `set()` operator is passed a model to be written to the DB, and a DB-specific delegate which does the actual writing
to the DB. The operator returns a list of errors if any.

The `set()` operator first validates the model using `SetRequestValidationVisitor`. If there are errors, the operator
aborts and returns the errors. Else the operator writes the model to the DB using `SetDelegateVisitor` and the delegate.

# SetDelegateVisitor

The `SetDelegateVisitor` is initialized with the DB-specific delegate. When an entity is visited, it gathers the keys
and non-composition fields from the entity, sorts the keys by their field numbers, and passes them to the delegate. Any
errors returned by the delegate are accumulated in the visitor and returned by the `set()` operator at the end.

# SetDelegate

This is an interface that DB-specific tree-ware libraries need to implement. The `SetDelegateVisitor` has to be
initialized with an instance of an implementation. The implementation will be passed information necessary for saving an
entity as a table row in the DB. The delegate returns any errors that it runs into while writing to the DB.