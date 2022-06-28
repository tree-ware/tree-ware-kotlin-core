---
title: "Fields"
layout: "titled"
nav_order: "d"
parent: "Contributor Docs"
---

{% include toc.md %}

# Introduction

The `difference()` operator takes two trees as inputs , 
and returns a class containing three sparse trees containing what would 
need to be created, deleted, and updated to turn the first 
tree into the second.

### CREATE

The `createModel` tree contains all elements contained in the
second tree but not the first, and thus must be created. This ignores
values in lists that exist in both the first and second trees, 
which are instead handled in `updateModel`.

### DELETE

The `deleteModel` tree contains all elements contained in the
first tree but not the second, and thus must be deleted. This ignores
values in lists that exist in both the first and second trees, 
which are instead handled in `updateModel`.

### UPDATE

The `updateModel` tree contains all fields that exist in both trees 
but have had at least one value changed.


# Traversal

When creating each tree, only parts of the tree with
relevant changes are included. This is done by traversing
the trees in a DFS manner, and only connecting relevant 
or necessary nodes to the tree while stepping out.