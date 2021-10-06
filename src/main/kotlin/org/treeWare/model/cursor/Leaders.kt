package org.treeWare.model.cursor

import org.treeWare.metaModel.getMetaName
import org.treeWare.model.core.BaseEntityModel
import org.treeWare.model.core.ElementModel
import org.treeWare.model.core.Resolved

class Leaders<Aux>(val elements: List<ElementModel<Aux>?>) {
    val nonNullElement = elements.firstNotNullOfOrNull { it }

    val elementType
        get() = nonNullElement?.elementType ?: throw IllegalArgumentException("There are no non-null elements")

    override fun toString(): String {
        val name = (nonNullElement?.meta as? BaseEntityModel<Resolved>?)?.let { getMetaName(it) } ?: ""
        return "$elementType $name $elements"
    }
}
