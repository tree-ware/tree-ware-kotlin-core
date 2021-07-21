package org.treeWare.schema.visitor

/** Adapts a SchemaVisitor to make it look like a MutableSchemaVisitor.
 * The SchemaVisitor cannot mutate the elements it visits (since the types of the elements passed to it are still
 * the immutable types), but it can be used in mutable-visitor-combinators along with other MutableSchemaVisitor
 * instances.
 */
// TODO(deepak-nulu): implement this adapter when MutableSchemaVisitor gets visit/leave methods like SchemaVisitor
