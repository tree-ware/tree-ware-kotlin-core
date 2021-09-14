package org.treeWare.model.action

import org.treeWare.model.traversal.Leader1Follower2ModelVisitor
import org.treeWare.model.traversal.TraversalAction

interface GetVisitor<MappingAux> : Leader1Follower2ModelVisitor<Unit, Unit, MappingAux, TraversalAction>
