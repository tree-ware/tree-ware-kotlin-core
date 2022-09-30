---
title: "Versioning"
layout: "titled"
nav_order: "d"
parent: "Contributor Docs"
---

{% include toc.md %}

# Introduction

When a meta-model is changed after a release, it needs to be assigned a new version. Tree-ware can validate whether the
changes made are backward compatible or not and enforce proper versioning.

# Meta-Model Versioning

Tree-ware meta-models use [semantic-versioning](https://semver.org) as follows:

* Major: incremented for backward-incompatible changes
* Minor: incremented for backward-compatible changes
    * Addition of new packages, new entities, new fields
    * Widening of existing constraints
        * This is backward-compatible because existing data is not affected
* Patch: incremented for meta-data changes
    * "info" field changes

Tree-ware meta-models can also have an optional, arbitrary string version name; this is typically set to the name of a
release in order to help identify the changes introduced in a release.

The following changes are not allowed by tree-ware currently; they will be supported in the future:

* Narrowing existing constraints on existing fields
    * This is not allowed until the ability to change existing data is supported
* Adding new constraints to existing fields
    * Any new constraints would be narrowing, so this is not allowed until the ability to change existing data is
      supported
* Deleting packages, entities, or fields
* Changing names
* Changing field types
* Changing multiplicity

## Validation

Tree-ware uses the difference operator to compare the current meta-model against the latest released meta-model to
determine the changes that have been made in the current meta-model since the previous release. Tree-ware then validates
that the semantic-version in the current meta-model has been incremented correctly based on the changes. It also
validates that the version name (if specified) is different from all the previous version names. If these validations
fail, tree-ware will break the build.

If there are no released meta-models, then tree-ware assumes that the current meta-model is the first, and will validate
that the current meta-model specifies `1.0.0` as its semantic-version.

## Released Meta-Models

To perform the above validation, tree-ware needs access to all versions of released meta-models. The easiest approach
(and the only approach currently supported) is to store all versions of released meta-models in a directory and provide
tree-ware with access to that directory.

Since a meta-model can be defined in multiple files, a subdirectory must be used for each version. The semantic-version
of the meta-model must be used as the name of the subdirectory. A symbolic-link called `latest` must point to the
subdirectory of the latest version.

Every time a release is made, a new directory must be created for the meta-models in that release, and the `latest`
symbolic-link must be updated to point to it.

This directory of released versions needs to be accessible to every feature-branch in which meta-models are being
developed, so that the current version of the meta-model can be validated in every feature-branch. If git is being used,
then one easy way to achieve this is to maintain the released versions in a separate repository and to add that
repository as a git submodule that tracks its main branch.

# Model Versioning

Currently there is no requirement for models to be explicitly versioned; models are implicitly versioned since they are
always associated with a meta-model and a meta-model is explicitly versioned.

While the core library does not require explicit model versions, the tree-ware server library requires [a model version
in the API URLs](http://www.tree-ware.org/tree-ware-kotlin-server/user/api.html#versioning).