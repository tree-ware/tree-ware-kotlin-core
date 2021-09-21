package org.treeWare.model.cursor

import org.treeWare.metaModel.getMetaName
import org.treeWare.model.core.*

sealed class FollowerModelCursorMove<Aux>(val direction: CursorMoveDirection) {
    abstract val element: ElementModel<Aux>?

    override fun toString(): String {
        val name = (element?.meta as? BaseEntityModel<Resolved>)?.let { getMetaName(it) } ?: ""
        return "${this::class.simpleName}: $name"
    }
}

class VisitFollowerMainModel<Aux>(override val element: MainModel<Aux>?) :
    FollowerModelCursorMove<Aux>(CursorMoveDirection.VISIT)

class LeaveFollowerMainModel<Aux>(override val element: MainModel<Aux>?) :
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

class VisitFollowerSetFieldModel<Aux>(override val element: SetFieldModel<Aux>?) :
    FollowerModelCursorMove<Aux>(CursorMoveDirection.VISIT)

class LeaveFollowerSetFieldModel<Aux>(override val element: SetFieldModel<Aux>?) :
    FollowerModelCursorMove<Aux>(CursorMoveDirection.LEAVE)

class VisitFollowerValueModel<Aux>(override val element: ElementModel<Aux>?) :
    FollowerModelCursorMove<Aux>(CursorMoveDirection.VISIT)

class LeaveFollowerValueModel<Aux>(override val element: ElementModel<Aux>?) :
    FollowerModelCursorMove<Aux>(CursorMoveDirection.LEAVE)

class VisitFollowerEntityKeysModel<Aux>(override val element: EntityKeysModel<Aux>?) :
    FollowerModelCursorMove<Aux>(CursorMoveDirection.VISIT)

class LeaveFollowerEntityKeysModel<Aux>(override val element: EntityKeysModel<Aux>?) :
    FollowerModelCursorMove<Aux>(CursorMoveDirection.LEAVE)
