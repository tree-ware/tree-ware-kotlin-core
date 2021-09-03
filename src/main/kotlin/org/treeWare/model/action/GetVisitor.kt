package org.treeWare.model.action

import org.treeWare.model.operator.Leader1Follower2ModelVisitor
import org.treeWare.model.operator.TraversalAction

interface GetVisitor<MappingAux> : Leader1Follower2ModelVisitor<Unit, Unit, MappingAux, TraversalAction>
