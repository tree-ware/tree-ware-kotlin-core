package org.tree_ware.model.cursor

import org.tree_ware.model.core.*
import org.tree_ware.schema.core.NamedElementSchema

sealed class LeaderModelCursorMove<Aux>(val direction: CursorMoveDirection) {
    abstract val element: ElementModel<Aux>

    override fun toString(): String {
        val schema = element.schema
        val name = if (schema is NamedElementSchema) schema.name else ""
        return "${this::class.simpleName}: $name"
    }
}

class VisitLeaderModel<Aux>(override val element: ElementModel<Aux>) :
    LeaderModelCursorMove<Aux>(CursorMoveDirection.Visit)

class LeaveLeaderModel<Aux>(override val element: Model<Aux>) :
    LeaderModelCursorMove<Aux>(CursorMoveDirection.Leave)

class VisitLeaderRootModel<Aux>(override val element: RootModel<Aux>) :
    LeaderModelCursorMove<Aux>(CursorMoveDirection.Visit)

class LeaveLeaderRootModel<Aux>(override val element: RootModel<Aux>) :
    LeaderModelCursorMove<Aux>(CursorMoveDirection.Leave)

class VisitLeaderEntityModel<Aux>(override val element: EntityModel<Aux>) :
    LeaderModelCursorMove<Aux>(CursorMoveDirection.Visit)

class LeaveLeaderEntityModel<Aux>(override val element: EntityModel<Aux>) :
    LeaderModelCursorMove<Aux>(CursorMoveDirection.Leave)

class VisitLeaderFieldModel<Aux>(override val element: FieldModel<Aux>) :
    LeaderModelCursorMove<Aux>(CursorMoveDirection.Visit)

class LeaveLeaderFieldModel<Aux>(override val element: FieldModel<Aux>) :
    LeaderModelCursorMove<Aux>(CursorMoveDirection.Leave)

class VisitLeaderListFieldModel<Aux>(override val element: FieldModel<Aux>) :
    LeaderModelCursorMove<Aux>(CursorMoveDirection.Visit)

class LeaveLeaderListFieldModel<Aux>(override val element: FieldModel<Aux>) :
    LeaderModelCursorMove<Aux>(CursorMoveDirection.Leave)

class VisitLeaderEntityKeysModel<Aux>(override val element: EntityKeysModel<Aux>) :
    LeaderModelCursorMove<Aux>(CursorMoveDirection.Visit)

class LeaveLeaderEntityKeysModel<Aux>(override val element: EntityKeysModel<Aux>) :
    LeaderModelCursorMove<Aux>(CursorMoveDirection.Leave)
