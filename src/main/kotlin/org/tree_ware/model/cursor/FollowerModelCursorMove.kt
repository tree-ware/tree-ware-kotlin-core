package org.tree_ware.model.cursor

import org.tree_ware.model.core.*
import org.tree_ware.schema.core.NamedElementSchema

sealed class FollowerModelCursorMove<Aux>(val direction: CursorMoveDirection) {
    abstract val element: ElementModel<Aux>?

    override fun toString(): String {
        val schema = element?.schema
        val name = if (schema is NamedElementSchema) schema.name else ""
        return "${this::class.simpleName}: $name"
    }
}

class VisitFollowerModel<Aux>(override val element: Model<Aux>?) :
    FollowerModelCursorMove<Aux>(CursorMoveDirection.Visit)

class LeaveFollowerModel<Aux>(override val element: Model<Aux>?) :
    FollowerModelCursorMove<Aux>(CursorMoveDirection.Leave)

class VisitFollowerRootModel<Aux>(override val element: RootModel<Aux>?) :
    FollowerModelCursorMove<Aux>(CursorMoveDirection.Visit)

class LeaveFollowerRootModel<Aux>(override val element: RootModel<Aux>?) :
    FollowerModelCursorMove<Aux>(CursorMoveDirection.Leave)

class VisitFollowerEntityModel<Aux>(override val element: EntityModel<Aux>?) :
    FollowerModelCursorMove<Aux>(CursorMoveDirection.Visit)

class LeaveFollowerEntityModel<Aux>(override val element: EntityModel<Aux>?) :
    FollowerModelCursorMove<Aux>(CursorMoveDirection.Leave)

class VisitFollowerFieldModel<Aux>(override val element: FieldModel<Aux>?) :
    FollowerModelCursorMove<Aux>(CursorMoveDirection.Visit)

class LeaveFollowerFieldModel<Aux>(override val element: FieldModel<Aux>?) :
    FollowerModelCursorMove<Aux>(CursorMoveDirection.Leave)

class VisitFollowerListFieldModel<Aux>(override val element: FieldModel<Aux>?) :
    FollowerModelCursorMove<Aux>(CursorMoveDirection.Visit)

class LeaveFollowerListFieldModel<Aux>(override val element: FieldModel<Aux>?) :
    FollowerModelCursorMove<Aux>(CursorMoveDirection.Leave)

class VisitFollowerEntityKeysModel<Aux>(override val element: EntityKeysModel<Aux>?) :
    FollowerModelCursorMove<Aux>(CursorMoveDirection.Visit)

class LeaveFollowerEntityKeysModel<Aux>(override val element: EntityKeysModel<Aux>?) :
    FollowerModelCursorMove<Aux>(CursorMoveDirection.Leave)
