package org.treeWare.model.cursor

import org.treeWare.model.core.*
import org.treeWare.schema.core.NamedElementSchema

sealed class LeaderModelCursorMove<Aux>(val direction: CursorMoveDirection) {
    abstract val element: ElementModel<Aux>

    override fun toString(): String {
        val schema = element.schema
        val name = if (schema is NamedElementSchema) schema.name else ""
        return "${this::class.simpleName}: $name"
    }
}

class VisitLeaderModel<Aux>(override val element: Model<Aux>) :
    LeaderModelCursorMove<Aux>(CursorMoveDirection.VISIT)

class LeaveLeaderModel<Aux>(override val element: Model<Aux>) :
    LeaderModelCursorMove<Aux>(CursorMoveDirection.LEAVE)

class VisitLeaderRootModel<Aux>(override val element: RootModel<Aux>) :
    LeaderModelCursorMove<Aux>(CursorMoveDirection.VISIT)

class LeaveLeaderRootModel<Aux>(override val element: RootModel<Aux>) :
    LeaderModelCursorMove<Aux>(CursorMoveDirection.LEAVE)

class VisitLeaderEntityModel<Aux>(override val element: EntityModel<Aux>) :
    LeaderModelCursorMove<Aux>(CursorMoveDirection.VISIT)

class LeaveLeaderEntityModel<Aux>(override val element: EntityModel<Aux>) :
    LeaderModelCursorMove<Aux>(CursorMoveDirection.LEAVE)

class VisitLeaderSingleFieldModel<Aux>(override val element: SingleFieldModel<Aux>) :
    LeaderModelCursorMove<Aux>(CursorMoveDirection.VISIT)

class LeaveLeaderSingleFieldModel<Aux>(override val element: SingleFieldModel<Aux>) :
    LeaderModelCursorMove<Aux>(CursorMoveDirection.LEAVE)

class VisitLeaderListFieldModel<Aux>(override val element: ListFieldModel<Aux>) :
    LeaderModelCursorMove<Aux>(CursorMoveDirection.VISIT)

class LeaveLeaderListFieldModel<Aux>(override val element: ListFieldModel<Aux>) :
    LeaderModelCursorMove<Aux>(CursorMoveDirection.LEAVE)

class VisitLeaderValueModel<Aux>(override val element: ElementModel<Aux>) :
    LeaderModelCursorMove<Aux>(CursorMoveDirection.VISIT)

class LeaveLeaderValueModel<Aux>(override val element: ElementModel<Aux>) :
    LeaderModelCursorMove<Aux>(CursorMoveDirection.LEAVE)

class VisitLeaderEntityKeysModel<Aux>(override val element: EntityKeysModel<Aux>) :
    LeaderModelCursorMove<Aux>(CursorMoveDirection.VISIT)

class LeaveLeaderEntityKeysModel<Aux>(override val element: EntityKeysModel<Aux>) :
    LeaderModelCursorMove<Aux>(CursorMoveDirection.LEAVE)
