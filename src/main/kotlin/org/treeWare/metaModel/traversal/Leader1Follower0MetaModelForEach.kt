package org.treeWare.metaModel.traversal

import org.treeWare.model.core.ElementModel
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.model.traversal.forEach

fun metaModelForEach(
    leaderMeta: ElementModel,
    visitor: Leader1Follower0MetaModelVisitor<TraversalAction>
): TraversalAction = forEach(leaderMeta, Leader1Follower0Adapter(visitor, TraversalAction.CONTINUE))
