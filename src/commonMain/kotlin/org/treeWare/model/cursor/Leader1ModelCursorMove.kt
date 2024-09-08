package org.treeWare.model.cursor

import org.treeWare.metaModel.getMetaName
import org.treeWare.model.core.EntityModel
import org.treeWare.model.core.ElementModel

class Leader1ModelCursorMove(val direction: CursorMoveDirection, val element: ElementModel) {
    override fun toString(): String {
        val name = (element.meta as? EntityModel)?.let { getMetaName(it) } ?: ""
        return "$direction ${element.elementType} $name"
    }
}
