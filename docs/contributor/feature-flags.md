---
title: "Feature Flags"
layout: "titled"
nav_order: "d"
parent: "Contributor Docs"
---

{% include toc.md %}

**NOTE: not yet implemented**

## Specification

* Feature name
  * This will be used as the flag name
* Feature paths: model paths introduced by the feature
  * Feature paths point to single-primitive-fields and roots of sub-trees
  * All keys in feature paths are typically wildcards (but not enforced)
* Scope paths: model paths indicating where the feature should be enabled
  * If the feature is to be enabled for everyone, the scope path is the model path to the model root
  * If the feature is to be enable for a customer, the scope path is the model path to the customer
  * If the feature is to be enable for a customer site, the scope path is the model path to the customer site
  * Multiple scope paths are allowed so that the feature can be enabled for a handful of customers/sites
  * The scope is not combined with the feature paths in the specification since the person specifying the scope may not
    be the feature developer and thus may not (and need not) know all the feature paths

## Flag Model

* Definitions for all flags are combined into a single flag model
  * The feature/flag name is added as aux data in the combined flag model
* The flag model is computed at startup and every time flags are changed
  * Until subscribing for changes is supported, the flag definitions are polled every minute
* An operator is used to add the feature name as aux data to the end of each feature path
  * TODO: what is this operator?
* For each feature, for each scope, the feature paths are added to the flag model
  * TODO: is this a new operator or can the union operator be used for this?
* The above models are merged using the union operator

## Enforcement

* The model in the API request is matched against the flag model
* If a field in the request matches a field in the flag model
  * and the feature-flag is off
    * an "Unknown field" error is accumulated
    * the sub-tree is aborted
* If accumulated errors
  * is empty -> the request is allowed through
  * is not empty -> the request is not allowed through, and the errors are returned

