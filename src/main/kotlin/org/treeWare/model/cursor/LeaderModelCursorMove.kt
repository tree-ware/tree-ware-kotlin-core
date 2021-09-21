package org.treeWare.model.cursor

import org.treeWare.metaModel.getMetaName
import org.treeWare.model.core.*

sealed class LeaderModelCursorMove<Aux>(val direction: CursorMoveDirection) {
    abstract val element: ElementModel<Aux>

    override fun toString(): String {
        val name = (element.meta as? BaseEntityModel<Resolved>)?.let { getMetaName(it) } ?: ""
        return "${this::class.simpleName}: $name"
    }
}

class VisitLeaderMainModel<Aux>(override val element: MainModel<Aux>) :
    LeaderModelCursorMove<Aux>(CursorMoveDirection.VISIT)

class LeaveLeaderMainModel<Aux>(override val element: MainModel<Aux>) :
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

class VisitLeaderSetFieldModel<Aux>(override val element: SetFieldModel<Aux>) :
    LeaderModelCursorMove<Aux>(CursorMoveDirection.VISIT)

class LeaveLeaderSetFieldModel<Aux>(override val element: SetFieldModel<Aux>) :
    LeaderModelCursorMove<Aux>(CursorMoveDirection.LEAVE)

class VisitLeaderValueModel<Aux>(override val element: ElementModel<Aux>) :
    LeaderModelCursorMove<Aux>(CursorMoveDirection.VISIT)

class LeaveLeaderValueModel<Aux>(override val element: ElementModel<Aux>) :
    LeaderModelCursorMove<Aux>(CursorMoveDirection.LEAVE)

class VisitLeaderEntityKeysModel<Aux>(override val element: EntityKeysModel<Aux>) :
    LeaderModelCursorMove<Aux>(CursorMoveDirection.VISIT)

class LeaveLeaderEntityKeysModel<Aux>(override val element: EntityKeysModel<Aux>) :
    LeaderModelCursorMove<Aux>(CursorMoveDirection.LEAVE)
