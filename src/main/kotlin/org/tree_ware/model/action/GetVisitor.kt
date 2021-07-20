package org.tree_ware.model.action

import org.tree_ware.model.operator.Leader1Follower2ModelVisitor
import org.tree_ware.common.traversal.TraversalAction

interface GetVisitor<MappingAux> : Leader1Follower2ModelVisitor<Unit, Unit, MappingAux, TraversalAction>
