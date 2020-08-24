package org.tree_ware.model.action

import org.tree_ware.model.operator.Leader1Follower2ModelVisitor
import org.tree_ware.schema.core.SchemaTraversalAction

interface GetVisitor<MappingAux> : Leader1Follower2ModelVisitor<Unit, Unit, MappingAux, SchemaTraversalAction>
