package org.tree_ware.schema.codec

import org.tree_ware.schema.core.ElementSchema

interface SchemaEncoder {
    /** @returns `false` if entire schema is not encoded. */
    fun encode(element: ElementSchema): Boolean
}
