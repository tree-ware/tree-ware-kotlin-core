---
title: "Role Based Access Control"
layout: "titled"
nav_order: "f"
parent: "Contributor Docs"
---

{% include toc.md %}

# Introduction

In tree-ware, all functionality is achieved using operators that operate on one or more model trees. The same applies to
RBAC. RBAC operators operate on two model trees: a request (or response) model tree, and a role model tree.

The RBAC operators are similar to intersection operators. Their output is a model tree that only has model nodes that
are in both input model trees. This results in only permitting request (or response) nodes that are in the role model
tree. The actual operators are slightly more complicated that intersection operators since they have to take into
consideration the permissions in the role model tree.

The following diagram shows how RBAC operators are used when a get-request is made. The get-response is also operated on
for safety reasons to ensure nothing slips through.

![RBAC for get-requests](get-request-rbac.drawio.svg)

RBAC operators are used similarly when a set-request is made. The set-response is an error model (if there are errors),
so it does not need to be "intersected".

# Set-Request Access Control

The `controlSetRequestAccess()` operator is for controlling the access of a set-request.

## TODO

* Design
* Assign association value only if user has access to the target.
    * Is it sufficient to have `read` permission on the target to be able to assign the association, or should a special
      `associate` permission on the target be introduced for this purpose?

# Get-Request Access Control

The `controlGetRequestAccess()` operator is for controlling the access of a get-request.

## TODO

* Design

# Get-Response Access Control

The `controlGetResponseAccess()` operator is for controlling the parts of a get-response that can be returned.

## TODO

* Design