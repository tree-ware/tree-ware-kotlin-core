package org.treeWare.model.cursor

data class LeaderManyModelCursorMove<Aux>(val direction: CursorMoveDirection, val leaders: Leaders<Aux>)
