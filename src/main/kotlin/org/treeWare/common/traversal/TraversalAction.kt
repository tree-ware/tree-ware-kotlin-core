package org.treeWare.common.traversal

enum class TraversalAction {
    /** Continue traversal. */
    CONTINUE,

    /** Abort traversal of the current sub-tree of the schema. */
    ABORT_SUB_TREE,

    /** Abort traversal of the entire schema. */
    ABORT_TREE
}
