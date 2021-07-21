package org.treeWare.model.operator

import org.treeWare.model.core.ElementModel
import org.treeWare.model.core.ModelVisitor
import org.treeWare.model.cursor.CursorMoveDirection
import org.treeWare.model.cursor.LeaderModelCursor
import org.treeWare.common.traversal.TraversalAction

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
