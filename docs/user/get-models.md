---
title: "Get Models"
layout: "titled"
nav_order: "e"
parent: "User Docs"
---

{% include toc.md %}

# Queries

* Get specific entities by specifying key values in the request model.
* List all entities under an immediate parent entity:
    * Do not specify key field values for the entities to be listed.
    * Specify the page size and next-page-token in the composition-set-field aux data in the request model.
        * Page size defaults to 10 if it is not specified.
        * The first page is returned if next-page-token is not specified.
        * The current-page-token is included in the aux data of the composition-set-field in the response model.
* Fetch nested lists by leaving out the key field values at those levels in the request model.
* List all entities with specific field values by specifying those field values in the request model.
    * List all entities pointing to a particular target entity by specifying it as the value of the association field.
* Include the targets of associations in the response model by specifying they should be included in the aux data of the
  association fields in the request model.

# Roadmap

* List all entities under an ancestor entity.
    * This is supported for all ancestors specified in the `list_by_` aux data of the entity in the meta-model.
    * There are 3 options for how to specify this kind of request:
        * A flat request with entity names from the meta-model and corresponding ancestor paths.
            * This is a new format. Need to figure out how to have a mix of this and regular queries.
            * This format is useful for template/parametric RBAC roles as well. Trees can be specified in many ways, so
              this approach is not going against the grain of tree-ware.
        * Full model hierarchy with entities between the desired ancestor and leaf marked as "ignore" in aux data.
            * This is more verbose, but since it is a tree, it can contain a mix of this and regular queries.
        * A partial model hierarchy that does not require the intermediate entities to be specified.
            * Not verbose, and allows a mix of this and regular queries, but complicates the implementation.
* Specify constraints on field values in the field aux data in the request model.
    * If the field has to equal a value, it can be specified as the field value (supported in the MVP) or in this aux
      data.