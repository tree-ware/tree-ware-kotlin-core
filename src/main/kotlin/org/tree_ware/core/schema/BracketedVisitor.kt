package org.tree_ware.core.schema

/**
 * Visitor that should be called whenever a object or list starts or ends.
 *
 * `name` parameter values will be in snake_case.
 */
public interface BracketedVisitor {
    fun objectStart(name: String)
    fun objectEnd()
    fun listStart(name: String)
    fun listEnd()
}
