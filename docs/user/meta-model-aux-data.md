---
title: "Meta-Model Aux Data"
layout: "titled"
nav_order: "c"
parent: "User Docs"
---

{% include toc.md %}

# Introduction

This page describes all the meta-model aux data supported by the core library.

# List By

Roadmap (not yet implemented). Optional. `list_by_` is associated with entities in the meta-model. The value is a list
and each list element is a path to an ancestor of the entity. It allows all entities under an ancestor to be listed.
Listing all entities under an immediate parent is always supported, and therefore should not be specified in this aux
data.