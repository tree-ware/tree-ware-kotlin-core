package org.treeWare.model.cursor

import org.treeWare.metaModel.getMetaName
import org.treeWare.model.core.EntityModel
import org.treeWare.model.core.ElementModel

class Leaders(val elements: List<ElementModel?>) {
    val nonNullElement = elements.firstNotNullOfOrNull { it }

    val elementType
        get() = nonNullElement?.elementType ?: throw IllegalArgumentException("There are no non-null elements")

    override fun toString(): String {
        val name = (nonNullElement?.meta as? EntityModel?)?.let { getMetaName(it) } ?: ""
        return "$elementType $name $elements"
    }
}
