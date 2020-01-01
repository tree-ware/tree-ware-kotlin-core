package org.tree_ware.core.codec.common

import org.tree_ware.core.schema.ElementSchema

interface SchemaEncoder {
    /** @returns `false` if entire schema is not encoded. */
    fun encode(element: ElementSchema): Boolean

    /** @returns `false` if entire schema is not encoded. */
    fun encode(elements: Collection<ElementSchema>): Boolean
}
