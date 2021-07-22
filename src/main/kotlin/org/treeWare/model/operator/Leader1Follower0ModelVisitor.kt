package org.treeWare.model.operator

import org.treeWare.model.core.*

interface Leader1Follower0ModelVisitor<LeaderAux, Return> {
    // Abstract elements are not visited

    fun visit(leaderModel1: Model<LeaderAux>): Return
    fun leave(leaderModel1: Model<LeaderAux>)

    fun visit(leaderRoot1: RootModel<LeaderAux>): Return
    fun leave(leaderRoot1: RootModel<LeaderAux>)

    fun visit(leaderEntity1: EntityModel<LeaderAux>): Return
    fun leave(leaderEntity1: EntityModel<LeaderAux>)

    // Scalar fields

    fun visit(leaderField1: PrimitiveFieldModel<LeaderAux>): Return
    fun leave(leaderField1: PrimitiveFieldModel<LeaderAux>)

    fun visit(leaderField1: AliasFieldModel<LeaderAux>): Return
    fun leave(leaderField1: AliasFieldModel<LeaderAux>)

    fun visit(leaderField1: EnumerationFieldModel<LeaderAux>): Return
    fun leave(leaderField1: EnumerationFieldModel<LeaderAux>)

    fun visit(leaderField1: AssociationFieldModel<LeaderAux>): Return
    fun leave(leaderField1: AssociationFieldModel<LeaderAux>)

    fun visit(leaderField1: CompositionFieldModel<LeaderAux>): Return
    fun leave(leaderField1: CompositionFieldModel<LeaderAux>)

    // List fields

    fun visit(leaderField1: PrimitiveListFieldModel<LeaderAux>): Return
    fun leave(leaderField1: PrimitiveListFieldModel<LeaderAux>)

    fun visit(leaderField1: AliasListFieldModel<LeaderAux>): Return
    fun leave(leaderField1: AliasListFieldModel<LeaderAux>)

    fun visit(leaderField1: EnumerationListFieldModel<LeaderAux>): Return
    fun leave(leaderField1: EnumerationListFieldModel<LeaderAux>)

    fun visit(leaderField1: AssociationListFieldModel<LeaderAux>): Return
    fun leave(leaderField1: AssociationListFieldModel<LeaderAux>)

    fun visit(leaderField1: CompositionListFieldModel<LeaderAux>): Return
    fun leave(leaderField1: CompositionListFieldModel<LeaderAux>)

    // Field values

    fun visit(leaderEntityKeys1: EntityKeysModel<LeaderAux>): Return
    fun leave(leaderEntityKeys1: EntityKeysModel<LeaderAux>)
}
