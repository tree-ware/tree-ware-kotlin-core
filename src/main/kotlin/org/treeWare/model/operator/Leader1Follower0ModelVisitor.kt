package org.treeWare.model.operator

import org.treeWare.model.core.*

interface Leader1Follower0ModelVisitor<LeaderAux, Return> {
    // Abstract elements are not visited

    fun visit(leaderMain1: MainModel<LeaderAux>): Return
    fun leave(leaderMain1: MainModel<LeaderAux>)

    fun visit(leaderRoot1: RootModel<LeaderAux>): Return
    fun leave(leaderRoot1: RootModel<LeaderAux>)

    fun visit(leaderEntity1: EntityModel<LeaderAux>): Return
    fun leave(leaderEntity1: EntityModel<LeaderAux>)

    // Fields

    fun visit(leaderField1: SingleFieldModel<LeaderAux>): Return
    fun leave(leaderField1: SingleFieldModel<LeaderAux>)

    fun visit(leaderField1: ListFieldModel<LeaderAux>): Return
    fun leave(leaderField1: ListFieldModel<LeaderAux>)

    // Values

    fun visit(leaderValue1: PrimitiveModel<LeaderAux>): Return
    fun leave(leaderValue1: PrimitiveModel<LeaderAux>)

    fun visit(leaderValue1: AliasModel<LeaderAux>): Return
    fun leave(leaderValue1: AliasModel<LeaderAux>)

    fun visit(leaderValue1: EnumerationModel<LeaderAux>): Return
    fun leave(leaderValue1: EnumerationModel<LeaderAux>)

    fun visit(leaderValue1: AssociationModel<LeaderAux>): Return
    fun leave(leaderValue1: AssociationModel<LeaderAux>)

    // Sub-values

    fun visit(leaderEntityKeys1: EntityKeysModel<LeaderAux>): Return
    fun leave(leaderEntityKeys1: EntityKeysModel<LeaderAux>)
}
