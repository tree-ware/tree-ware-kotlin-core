---
title: "Role Based Access Control"
layout: "titled"
nav_order: "f"
parent: "Contributor Docs"
---

{% include toc.md %}

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