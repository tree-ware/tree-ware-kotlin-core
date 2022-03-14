---
title: "Role Based Access Control"
layout: "titled"
nav_order: "f"
parent: "User Docs"
---

{% include toc.md %}

# Introduction

RBAC allows ***users*** to perform operations on ***resources*** based on ***permissions*** granted to them for those
resources.

All RBAC systems have ways of ***granting*** resource permissions to users. Typically, permissions are not granted to
users directly. Instead, permissions for specific resources are grouped into a ***role***. And one or more roles are
assigned to a user. Some systems support grouping of users into ***user-groups*** and assigning of roles to user-groups.

# Resources

Resources in tree-ware are the nodes in the model tree.

# Permissions

Permissions permit operations on resources. There are only certain operations possible on nodes in the model tree (the
resources), and tree-ware has a corresponding permission for each of them:

* `create`: permit the user to create a model node.
* `read`: permit the user to read a model node.
* `update`: permit the user to update a model node.
* `delete`: permit the user to delete a model node.
* `crud`: the above 4 permissions combined.
* `grant`: permit the user to grant access to a model node to other users. Specifically, this permits a user to:
    * create a role with that model node.
    * add that model node to a role.
    * assign such a role to another user.
* `revoke`: permit the user to revoke access to a model node from other users. Specifically, this permits a user to:
    * remove a role with that model node.
    * remove that model node from a role.
    * remove such a role from another user.
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

# Meta-model

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

Since users and roles are part of the meta-model, they can be accessed via the tree-ware API just like any other data
defined in the meta-model.

The role `definition` field is a composition of the root of the model, and is therefore an entire model tree by itself.
As explained in the next section, this entire tree could be stored as a single blob or JSON value by some storage
systems. So incremental updates to the role `definition` are not supported; a `definition` tree in an update-request
will replace the previous definition in the storage system.

# Storage

RBAC data is needed in every API call in order to control access to the data being accessed in the API call. So fetching
of RBAC data needs to be very fast.

When targeting a relational DB where each entity is stored in a separate table, it could take too long to fetch a role
if its `definition` is deeply nested. In such cases, the role `definition` should be stored as a blob or JSON value in a
single column by annotating the role `definition` field appropriately in its meta-model aux. This aux data is not part
of the `role` meta-model *interface* because it is specific to the storage being targeted; some storage systems may be
able to fetch data quickly even if all RBAC data is not stored as a blob or a JSON value, and therefore not even need
such an annotation.

# Examples

TODO

# TODO

* An association can be assigned only if the user has access to the source as well as the target
    * Reason: assigning a target might result in changing the system behavior. For example, changing an organization
      parent can cause metrics to be reported to the new parent.
* Customers are not sub-trees of the MSP, so how does the super-admin in an MSP get access to the sub-trees of their new
  customers?
    * Some side-effect function needs to keep the super-admin role up-to-date.
* How can a customer grant an MSP admin access to customer data?
    * One way is the association workflow mentioned in the roadmap section below.
    * Another could be to have a role to user-email mapping. The `roles` list field in the `user` entity is not being
      updated (since the customer won't have access to the `user` entity of the MSP admin), but if each `organization`
      had its own role to user-email mapping, then they would be able to add such entries.
        * Can this map be the only way to specify user to role mappings?
        * How will the RBAC library find such mappings when an API call is being made and needs to be checked?
            * A side-effect function can mirror the role -> user mapping into a user -> role map
        * Is this option generic and useful in other places?
* Need the ability to store lists of associations as separate rows for each association. This will allow a new role to
  be added to a user without having to know the old list of associations.
    * Recommended way of storing lists in MySQL is to use a join table. The user ID and the role association value will
      have to be keys in the join table. So associations as keys needs to be supported. Associations are long paths, but
      only the final keys in the path are needed when targeting MySQL. So it should be possible to support associations
      as keys.

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