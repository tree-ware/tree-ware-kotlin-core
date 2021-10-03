package org.treeWare.model.cursor

import org.treeWare.metaModel.getMetaName
import org.treeWare.model.core.BaseEntityModel
import org.treeWare.model.core.ElementModel
import org.treeWare.model.core.Resolved

class LeaderModelCursorMove<Aux>(val direction: CursorMoveDirection, val element: ElementModel<Aux>) {
    override fun toString(): String {
        val name = (element.meta as? BaseEntityModel<Resolved>)?.let { getMetaName(it) } ?: ""
        return "$direction ${element.elementType} $name"
    }
}
