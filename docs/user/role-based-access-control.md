---
title: "Role Based Access Control"
layout: "titled"
nav_order: "f"
parent: "User Docs"
---

{% include toc.md %}

# Introduction

RBAC allows ***users*** to perform ***operations*** on ***resources*** based on ***permissions*** that are
***granted***.

All RBAC systems have ways of granting resource permissions to users. Typically, permissions are not granted to users
directly. Instead, permissions for specific resources are grouped into a ***role***. And one or more roles are
***assigned*** to a user. Some systems support grouping users into ***user-groups*** and assigning roles to user-groups.

# Resources

Resources in tree-ware are the nodes in the model tree.

Resources can be fine-grained (leaf-nodes representing individual fields) or coarse grained (branch nodes representing
an entity of fields or a sub-tree of nested entities and fields).

# Permissions

Permissions permit operations on resources. There are only certain operations possible on nodes in the model tree (the
resources), and tree-ware has a corresponding permission for each of them:

* `create`: permit the user to create a model node.
* `read`: permit the user to read a model node.
* `update`: permit the user to update a model node.
* `delete`: permit the user to delete a model node.
* `crud`: the above 4 permissions combined.
* `grant` (not yet supported): permit the user to grant access to a model node to other users. Specifically, this
  permits a user to:
    * create a role with that model node.
    * add that model node to a role.
    * assign such a role to another user.
* `revoke` (not yet supported): permit the user to revoke access to a model node from other users. Specifically, this
  permits a user to:
    * remove a role with that model node.
    * remove that model node from a role.
    * remove such a role from another user.
* `associate` (partially supported): permit the user to create an association to a model node.
    * Associating to a target node can result in changing the system behavior. For example, changing an organization
      parent can cause metrics to be reported to the new parent. So associations are prevented if the user does not have
      permission to associate to the target node.
    * **WARNING**: in the current implementation, associations can be made if the user has `read` permission on the
      target node.
* `all`: all of the above permissions combined.
    * NOTE: if a user has this permission, then that user will automatically get new permissions that are created in the
      future. So this permission should be used primarily for administrators.

# Roles

As mentioned in the [Introduction](#Introduction), a role is a group of permissions for resources. Each entry in the
group is a specific permission for a specific resource. Since resources are model nodes in tree-ware, a role is a group
of model nodes. And a group of model nodes is a sparse model tree. So a role in tree-ware is a sparse model tree!

A role must also associate a permission with each resource (model node). In tree-ware, aux data is used for attaching
information to model nodes in the tree. So permissions are specified in aux data. So a role in tree-ware is a model tree
with aux data.

This leads to a natural interpretation of RBAC in tree-ware: when a role is assigned to a user, the user can access
nodes in the model based on the permissions attached to those nodes in the role.

When multiple roles are assigned to a user, the union of the individual role model trees is computed, and that unified
model tree determines user access to the model.

# Permission Inheritance/Scopes

Permissions granted to a user for a particular node in the model tree can be inherited by all the nodes in the sub-tree.

This allows a tree-ware model to be organized by "ownership" as described in [Modeling Tips](modeling-tips.md). For
example, everything a customer can access is organized in the sub-tree of a customer node in the model. So the admin
role for a customer needs to only specify the customer node in the model tree; it does not have to specify the rest of
the sub-tree below the customer node.

To control whether permissions are inherited or not, each assignment of a permission to a node in the role model can
specify one of the following permission-scopes:

* `sub_tree`: the permission is for the assigned node and is inherited by its sub-tree.
* `node`: the permission is for the assigned node but is not inherited by its sub-tree. Overrides inherited permissions.
* `none`: the permission is removed for the assigned node and its sub-tree. Overrides inherited permissions.

# Examples

## Global-Admin Role

A global-admin role for an entire address-book model assigns `all` permissions to the root node with `sub_tree` scope:

```json
{
  "address_book__permissions_": {
    "all": "sub_tree"
  },
  "address_book": {}
}
```

## Global-Observer Role

A global-observer role for an entire address-book model assigns `read` permissions to the root node with `sub_tree`
scope:

```json
{
  "address_book__permissions_": {
    "read": "sub_tree"
  },
  "address_book": {}
}
```

## Specific-Admin Role

An admin role for a specific person in the address-book assigns `all` permissions to the specific person in the model
with `sub_tree` scope:

```json
{
  "address_book": {
    "person": [
      {
        "permissions_": {
          "all": "sub_tree"
        },
        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f"
      }
    ]
  }
}
```

## List-Only Role

A role that allows the listing of the IDs and names of people in the address-book assigns `read` permissions to the
person list with `node` scope, and lists only the ID and name fields in the person entity:

```json
{
  "address_book": {
    "person__permissions_": {
      "read": "node"
    },
    "person": [
      {
        "id": null,
        "first_name": null,
        "last_name": null
      }
    ]
  }
}
```

# Meta-model

Not yet supported.

Users, roles, and role assignment are defined in the same meta-model as the rest of the product-specific system. The
user and role entities need to implement the following [meta-model interfaces](meta-model-interfaces.md):

```json
{
  "name": "user",
  "info": "Meta-model interface for user entity",
  "fields": [
    {
      "name": "roles",
      "type": "association",
      "target": {
        "name": "role",
        "package": "org.tree_ware.rbac"
      },
      "multiplicity": "list"
    }
  ]
}
```

```json
{
  "name": "role",
  "info": "Meta-model interface for role entity",
  "fields": [
    {
      "name": "definition",
      "type": "root"
    }
  ]
}
```

# API

Not yet supported.

Since users and roles are part of the meta-model, they can be accessed via the tree-ware API just like any other data
defined in the meta-model.

The role `definition` field is a composition of the root of the model, and is therefore an entire model tree by itself.
As explained in the next section, this entire tree could be stored as a single blob or JSON value by some storage
systems. So incremental updates to the role `definition` are not supported; a `definition` tree in an update-request
will replace the previous definition in the storage system.

# Storage

Not yet supported.

RBAC data is needed in every API call in order to control access to the data being accessed in the API call. So fetching
of RBAC data needs to be very fast.

When targeting a relational DB where each entity is stored in a separate table, it could take too long to fetch a role
if its `definition` is deeply nested. In such cases, the role `definition` should be stored as a blob or JSON value in a
single column by annotating the role `definition` field appropriately in its meta-model aux. This aux data is not part
of the `role` meta-model *interface* because it is specific to the storage being targeted; some storage systems may be
able to fetch data quickly even if all RBAC data is not stored as a blob or a JSON value, and therefore not even need
such an annotation.

# Roadmap

* Template/parametric roles.
    * For example, when there are nested sites, there might be a custom role that allows access only to certain entities
      under a site, and customers may want to apply this custom role at different sites/levels for different people
      without having to specify a separate role for each site where it needs to be applied (since each role must define
      a model tree from the root).
    * Parameters can be specified as `$1`, `$2`, etc.
    * The parameter for the site-specific custom role example above would be the path to the site. This would require an
      alternate representation of the model tree: entity name, parent path (the parameter), and a sub-tree of the named
      entity.
        * This representation would also be useful for listing entities based on a certain ancestor (the ancestor would
          be specified like the path parameter).
* A workflow for creating an association when source and target are owned by different people.
    * When the owner of the association sets the target, the system notifies all owners of the target (if the target is
      now owned by the user setting the target). When a target owner approves the request, the system sets the target as
      the value of the association and notifies the requester.