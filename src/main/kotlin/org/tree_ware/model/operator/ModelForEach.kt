package org.tree_ware.model.operator

import org.tree_ware.model.core.ElementModel
import org.tree_ware.model.core.ModelVisitor
import org.tree_ware.model.cursor.CursorMoveDirection
import org.tree_ware.model.cursor.LeaderModelCursor
import org.tree_ware.common.traversal.TraversalAction

fun <Aux> forEach(leader: ElementModel<Aux>, visitor: ModelVisitor<Aux, TraversalAction>): TraversalAction {
    val leaderCursor = LeaderModelCursor(leader)
    var action = TraversalAction.CONTINUE
    while (action != TraversalAction.ABORT_TREE) {
        val move = leaderCursor.next(action) ?: break
        action = when (move.direction) {
            CursorMoveDirection.Visit -> move.element.visitSelf(visitor)
            CursorMoveDirection.Leave -> {
                move.element.leaveSelf(visitor)
                TraversalAction.CONTINUE
            }
        }
    }
    return action
}
