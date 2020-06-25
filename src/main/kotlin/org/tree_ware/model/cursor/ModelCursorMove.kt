package org.tree_ware.model.cursor

import org.tree_ware.model.core.*
import org.tree_ware.schema.core.NamedElementSchema

enum class CursorMoveDirection { Visit, Leave }

sealed class ModelCursorMove<Aux>(val direction: CursorMoveDirection) {
    abstract val element: ElementModel<Aux>

    override fun toString(): String {
        val schema = element.schema
        val name = if (schema is NamedElementSchema) schema.name else ""
        return "${this::class.simpleName}: $name"
    }
}

class VisitModel<Aux>(override val element: ElementModel<Aux>) :
    ModelCursorMove<Aux>(CursorMoveDirection.Visit)

class LeaveModel<Aux>(override val element: Model<Aux>) :
    ModelCursorMove<Aux>(CursorMoveDirection.Leave)

class VisitRootModel<Aux>(override val element: RootModel<Aux>) :
    ModelCursorMove<Aux>(CursorMoveDirection.Visit)

class LeaveRootModel<Aux>(override val element: RootModel<Aux>) :
    ModelCursorMove<Aux>(CursorMoveDirection.Leave)

class VisitEntityModel<Aux>(override val element: EntityModel<Aux>) :
    ModelCursorMove<Aux>(CursorMoveDirection.Visit)

class LeaveEntityModel<Aux>(override val element: EntityModel<Aux>) :
    ModelCursorMove<Aux>(CursorMoveDirection.Leave)

class VisitFieldModel<Aux>(override val element: FieldModel<Aux>) :
    ModelCursorMove<Aux>(CursorMoveDirection.Visit)

class LeaveFieldModel<Aux>(override val element: FieldModel<Aux>) :
    ModelCursorMove<Aux>(CursorMoveDirection.Leave)

class VisitListFieldModel<Aux>(override val element: FieldModel<Aux>) :
    ModelCursorMove<Aux>(CursorMoveDirection.Visit)

class LeaveListFieldModel<Aux>(override val element: FieldModel<Aux>) :
    ModelCursorMove<Aux>(CursorMoveDirection.Leave)

class VisitEntityKeysModel<Aux>(
    override val element: EntityKeysModel<Aux>, val pathKeysIndex: Int
) : ModelCursorMove<Aux>(CursorMoveDirection.Visit) {
    override fun toString(): String = "${super.toString()}; pathKeys[$pathKeysIndex]"
}

class LeaveEntityKeysModel<Aux>(override val element: EntityKeysModel<Aux>) :
    ModelCursorMove<Aux>(CursorMoveDirection.Leave)
