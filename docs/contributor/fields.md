---
title: "Fields"
layout: "titled"
nav_order: "b"
parent: "Contributor Docs"
---

{% include toc.md %}

# Numbers

Every entity-field and enumeration-value must have a name and a number. The name is used in text formats (like JSON) and
the number is used in binary formats (like Google protobufs or custom tree-ware binary formats).

A binary codec is [not yet supported](https://github.com/tree-ware/tree-ware-kotlin-core/issues/38) in tree-ware. There
are 2 options for supporting a binary codec: create a custom binary format, or use a format like Google protobufs. The
latter has restrictions on the numbers for fields and enumeration-values. The former could be designed to not have any
restrictions. In order to keep both options viable, tree-ware currently enforces Google protobuf restrictions for field
and enumeration-value numbers in the tree-ware meta-model.