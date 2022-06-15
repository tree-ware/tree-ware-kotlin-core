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
need to be created, deleted, and updated (untested) to turn the first 
tree into the second.

### CREATE

The `createModel` tree contains all nodes contained in the
second tree but not the first, and thus must be created.

### DELETE

The `deleteModel` tree contains all nodes contained in the
first tree but not the second, and thus must be deleted.

### UPDATE (untested)

The `updateModel` tree contains all nodes in the first tree
that exist in the second tree, but have been changed, 
and thus must be updated.


# Traversal

When creating each tree, only parts of the tree with
relevant changes are included. This is done by traversing
the trees in a DFS manner, and only connecting relevant 
or necessary nodes to the tree while stepping out.