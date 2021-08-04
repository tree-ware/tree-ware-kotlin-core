package org.treeWare.model.cursor

import org.treeWare.model.core.*
import org.treeWare.schema.core.NamedElementSchema

sealed class FollowerModelCursorMove<Aux>(val direction: CursorMoveDirection) {
    abstract val element: ElementModel<Aux>?

    override fun toString(): String {
        val schema = element?.schema
        val name = if (schema is NamedElementSchema) schema.name else ""
        return "${this::class.simpleName}: $name"
    }
}

class VisitFollowerModel<Aux>(override val element: Model<Aux>?) :
    FollowerModelCursorMove<Aux>(CursorMoveDirection.VISIT)

class LeaveFollowerModel<Aux>(override val element: Model<Aux>?) :
    FollowerModelCursorMove<Aux>(CursorMoveDirection.LEAVE)

class VisitFollowerRootModel<Aux>(override val element: RootModel<Aux>?) :
    FollowerModelCursorMove<Aux>(CursorMoveDirection.VISIT)

class LeaveFollowerRootModel<Aux>(override val element: RootModel<Aux>?) :
    FollowerModelCursorMove<Aux>(CursorMoveDirection.LEAVE)

class VisitFollowerEntityModel<Aux>(override val element: EntityModel<Aux>?) :
    FollowerModelCursorMove<Aux>(CursorMoveDirection.VISIT)

class LeaveFollowerEntityModel<Aux>(override val element: EntityModel<Aux>?) :
    FollowerModelCursorMove<Aux>(CursorMoveDirection.LEAVE)

class VisitFollowerSingleFieldModel<Aux>(override val element: SingleFieldModel<Aux>?) :
    FollowerModelCursorMove<Aux>(CursorMoveDirection.VISIT)

class LeaveFollowerSingleFieldModel<Aux>(override val element: SingleFieldModel<Aux>?) :
    FollowerModelCursorMove<Aux>(CursorMoveDirection.LEAVE)

class VisitFollowerListFieldModel<Aux>(override val element: ListFieldModel<Aux>?) :
    FollowerModelCursorMove<Aux>(CursorMoveDirection.VISIT)

class LeaveFollowerListFieldModel<Aux>(override val element: ListFieldModel<Aux>?) :
    FollowerModelCursorMove<Aux>(CursorMoveDirection.LEAVE)

class VisitFollowerValueModel<Aux>(override val element: ElementModel<Aux>?) :
    FollowerModelCursorMove<Aux>(CursorMoveDirection.VISIT)

class LeaveFollowerValueModel<Aux>(override val element: ElementModel<Aux>?) :
    FollowerModelCursorMove<Aux>(CursorMoveDirection.LEAVE)

class VisitFollowerEntityKeysModel<Aux>(override val element: EntityKeysModel<Aux>?) :
    FollowerModelCursorMove<Aux>(CursorMoveDirection.VISIT)

class LeaveFollowerEntityKeysModel<Aux>(override val element: EntityKeysModel<Aux>?) :
    FollowerModelCursorMove<Aux>(CursorMoveDirection.LEAVE)
