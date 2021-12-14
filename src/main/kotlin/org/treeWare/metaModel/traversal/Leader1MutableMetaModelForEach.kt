package org.treeWare.metaModel.traversal

import org.treeWare.model.core.MutableElementModel
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.model.traversal.mutableForEach

fun mutableMetaModelForEach(
    leaderMeta: MutableElementModel,
    visitor: Leader1MutableMetaModelVisitor<TraversalAction>
): TraversalAction = mutableForEach(leaderMeta, Leader1MutableAdapter(visitor, TraversalAction.CONTINUE))