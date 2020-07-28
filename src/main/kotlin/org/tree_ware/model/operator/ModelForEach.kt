package org.tree_ware.model.operator

import org.tree_ware.model.core.ElementModel
import org.tree_ware.model.core.ModelVisitor
import org.tree_ware.model.cursor.CursorMoveDirection
import org.tree_ware.model.cursor.LeaderModelCursor
import org.tree_ware.schema.core.SchemaTraversalAction

fun <Aux> forEach(leader: ElementModel<Aux>, visitor: ModelVisitor<Aux, SchemaTraversalAction>): SchemaTraversalAction {
    val leaderCursor = LeaderModelCursor(leader)
    var action = SchemaTraversalAction.CONTINUE
    while (action != SchemaTraversalAction.ABORT_TREE) {
        val move = leaderCursor.next(action) ?: break
        action = when (move.direction) {
            CursorMoveDirection.Visit -> move.element.visitSelf(visitor)
            CursorMoveDirection.Leave -> {
                move.element.leaveSelf(visitor)
                SchemaTraversalAction.CONTINUE
            }
        }
    }
    return action
}
