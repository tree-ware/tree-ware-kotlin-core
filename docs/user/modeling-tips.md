---
title: "Modeling Tips"
layout: "titled"
nav_order: "g"
parent: "User Docs"
---

{% include toc.md %}

# Introduction

There are multiple ways to model system data. This page discusses which option to choose when multiple options are
available.

# Compositions vs. Associations

Compositions are nesting of entities are result in a tree structure. Associations are references to entities in the tree
structure. In UML, compositions have the additional semantics of child entities existing only if the parent entity
exists. In tree-ware, that behavior is controlled separately like in entity-relationship models. So the decision between
compositions and associations should not be based on whether child entities should be automatically deleted if the
parent entity is deleted. Instead, it should be decided based on RBAC ownership and inheritance of that ownership by
child entities.

For example, consider the model for IoT devices that are located in various sites. Assume that sites are nested by
geography, campuses, buildings, and floors. Assume that there are different administrators for different campuses and
that they get to control everything in their campus. Permissions granted for a campus entity in the model apply to all
entities in the sub-tree (unless overridden in the sub-tree). So if the administrator needs to be able to configure the
IoT devices in their campus, the IoT device entities need to be part of the campus sub-tree. So the site entities need
to compose the IoT device entities.

In UML, this composition would have implied that the IoT device entities will be deleted if their parent site entity is
deleted. This is not desirable since sites are logical entities while IoT devices are physical entities. So tree-ware
does not follow UML conventions. Instead, tree-ware follows entity-relationship modeling conventions and has separate
annotations for specifying what must happen to children if a parent is deleted, and similarly, what must happen to the
target entity of an association if its source entity is deleted.

# Child Compositions vs. Parent Associations

In the example in the previous section, sites were nested by geography, campuses, buildings, and floors. This can be
modeled with a single `site` entity and a `type` field that indicates what type of site it is. The site entity would
also have a `sub_sites` field that composes a set of `site` entities. i.e. a self-referential child composition.

Consider another example: modeling a multi-tenant application. The application provider will need to be modeled. The
customers will need to be modeled. MSP (Managed Service Providers) will need to be modeled. The application provider can
have direct customers, or they can have customer through MSPs. So there is a hierarchy between the application provider,
MSPs, and customers. All of them will have their own employees registered as users and administrators. So one way of
modeling this would be to have an `organization` entity that composes `user` entities as well as a self-referential
child composition called `sub_organizations`.

Another way to model these hierarchies (self-referential or not) is to use parent associations instead of child
compositions. For example, instead of a `sub_sites` composition, the `site` entity would have a `parent` association
that pointed to the parent site. Similarly, instead of a `sub_organizations` composition, the `organization` entity
would have a `parent` association that pointed to the parent organization.

The approach to take is determined by the API that is desired. With child compositions, all ancestor entities and their
keys need to be specified in API requests. With parent associations, only the desired entity and its keys need to be
specified in API requests. If organizations are modeled as child compositions, then a customer would need to specify the
keys of application provider as well as the keys of the MSP if the customer is not a direct customer. This is
undesirable, so organizations should be modeled using parent associations. Furthermore, users in the application
provider or users in the MSP should not have access to data under the customer organization. By not using child
compositions, RBAC permissions for a user in the application provider or for a user in the MSP will not automatically
apply to customer data since the customer is not in the subtree of the application provider or the MSP.

In the case of sites, as seen in the previous section, it is desirable to use child compositions for sites so that RBAC
permissions are effective for the entire sub-tree. This forces the keys of all ancestor sites to be specified in API
requests, but this is not an issue because all sites belong to the customer.