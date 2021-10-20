package org.treeWare.model.cursor

import org.treeWare.metaModel.getMetaName
import org.treeWare.model.core.BaseEntityModel
import org.treeWare.model.core.ElementModel

class FollowerModelCursorMove(private val direction: CursorMoveDirection, val element: ElementModel?) {
    override fun toString(): String {
        val name = (element?.meta as? BaseEntityModel)?.let { getMetaName(it) } ?: ""
        return "$direction ${element?.elementType} $name"
    }
}
