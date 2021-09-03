package org.treeWare.model.operator

enum class TraversalAction {
    /** Continue traversal. */
    CONTINUE,

    /** Abort traversal of the current sub-tree. */
    ABORT_SUB_TREE,

    /** Abort traversal of the entire tree. */
    ABORT_TREE
}
