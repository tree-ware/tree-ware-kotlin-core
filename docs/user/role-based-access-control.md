---
layout: default
title: Role Based Access Control
nav_order: 10
parent: User Docs
---

# Role Based Access Control
{: .no_toc }

---
<details open markdown="block">
  <summary>
    Table of Contents
  </summary>
  {: .text-delta }
1. TOC
{: toc }
</details>
---

# Introduction

Role Based Access Control (RBAC) controls user access to a model.

# Roles

A role is a (sparse) model with permissions attached to nodes in the model tree. When a role is assigned to a user, the
user can access nodes in the model based on the permissions attached to those nodes in the role.

When multiple roles are assigned to a user, the union of the roles determines user access to the model.

# Permissions

A permission permits a user to do something with the model node to which the permission is attached. The following
permissions are supported in tree-ware:

* `create`: permit the user to create a model node.
* `read`: permit the user to read a model node.
* `update`: permit the user to update a model node.
* `delete`: permit the user to delete a model node.
* `crud`: the above 4 permissions combined.
* `grant`: permit the user to grant access to a model node to other users.
    * This permission is needed to create a role with that model node or to add that model node to a role.
    * This permission is needed to assign such a role to another user.
* `revoke`: permit the user to revoke access to a model node from other users.
    * This permission is needed to remove a role with that model node or remove that model node from a role.
    * This permission is needed to remove such a role from another user.
* `all`: all of the above permissions combined.

# TODO

* Should roles be part of the model or stored separately from the model?
    * The UI needs to list roles, and assign roles to users, so it would be better for roles to be a part of the model
        * This will require the ability for a model to have multiple aux types in the same model
        * This will require the ability to store aux data in the DB
* Examples
* Operators