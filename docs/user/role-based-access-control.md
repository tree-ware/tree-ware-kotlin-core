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
    Table of contents
  </summary>
  {: .text-delta }
1. TOC
{: toc }
</details>

# Introduction

Role Based Access Control (RBAC) controls user access to a model. A role specifies permissions for nodes in the model
tree, and when assigned to a user, the user can access the model according to the permissions in that role.

# Roles

A role is a (sparse) model with permissions attached to nodes in the model tree. When a role is assigned to a user, the
user can access nodes in the model based on the permissions attached to those nodes in the role.

When multiple roles are assigned to a user, the union of the roles determines user access to the model.

# Permissions

A permission permits a user to do something with the model node to which the permission is assigned. The following
permissions are supported in tree-ware:

* `create`: permit the user to create a model node.
* `read`: permit the user to read a model node.
* `update`: permit the user to update a model node.
* `delete`: permit the user to delete a model node.
* `crud`: the above 4 permissions combined.
* `grant`: permit a user to grant access to a model node to other users.
    * This permission is needed to create a role with that model node or to add that model node to a role.
    * This permission is needed to assign such a role to another user.
* `revoke`: permit the user to revoke access to a model node from other users.
    * This permission is needed to remove a role with that model node or remove that model node from a role.
    * This permission is needed to remove such a role from another user.
* `all`: all of the above permissions combined.

# TODO

* Should roles be part of the model or a separate entity stored separately from the model?
    * The UI needs to list roles, and assign roles to users. So it would be better for roles to be a part of the model
        * This will require the ability for a model to have multiple aux types in the same model
        * This will require the ability to store aux data in the DB
* Examples
* Operators