package org.tree_ware.model.cursor

import org.tree_ware.model.core.*

enum class CursorMoveDirection { Visit, Leave }

sealed class ModelCursorMove<Aux>(val direction: CursorMoveDirection) {
    abstract val element: ElementModel<Aux>
}

data class VisitModel<Aux>(override val element: ElementModel<Aux>) :
    ModelCursorMove<Aux>(CursorMoveDirection.Visit)

data class LeaveModel<Aux>(override val element: Model<Aux>) :
    ModelCursorMove<Aux>(CursorMoveDirection.Leave)

data class VisitRootModel<Aux>(override val element: RootModel<Aux>) :
    ModelCursorMove<Aux>(CursorMoveDirection.Visit)

data class LeaveRootModel<Aux>(override val element: RootModel<Aux>) :
    ModelCursorMove<Aux>(CursorMoveDirection.Leave)

data class VisitEntityModel<Aux>(override val element: EntityModel<Aux>) :
    ModelCursorMove<Aux>(CursorMoveDirection.Visit)

data class LeaveEntityModel<Aux>(override val element: EntityModel<Aux>) :
    ModelCursorMove<Aux>(CursorMoveDirection.Leave)

data class VisitFieldModel<Aux>(override val element: FieldModel<Aux>) :
    ModelCursorMove<Aux>(CursorMoveDirection.Visit)

data class LeaveFieldModel<Aux>(override val element: FieldModel<Aux>) :
    ModelCursorMove<Aux>(CursorMoveDirection.Leave)

data class VisitListFieldModel<Aux>(override val element: FieldModel<Aux>) :
    ModelCursorMove<Aux>(CursorMoveDirection.Visit)

data class LeaveListFieldModel<Aux>(override val element: FieldModel<Aux>) :
    ModelCursorMove<Aux>(CursorMoveDirection.Leave)

data class VisitEntityKeysModel<Aux>(override val element: EntityKeysModel<Aux>) :
    ModelCursorMove<Aux>(CursorMoveDirection.Visit)

data class LeaveEntityKeysModel<Aux>(override val element: EntityKeysModel<Aux>) :
    ModelCursorMove<Aux>(CursorMoveDirection.Leave)
