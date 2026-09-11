package org.treeWare.model.operator

import org.treeWare.model.core.*
import org.treeWare.model.traversal.AbstractLeaderManyModelVisitor
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.model.traversal.forEach

/**
 * Returns `true` if the two model elements are equal, `false` otherwise.
 *
 * This is a lighter-weight alternative to using the `difference()` operator
 * for detecting if model elements are equal. It aborts the traversal and
 * returns `false` as soon as inequality is detected instead of traversing
 * the entire tree.
 */
fun equals(first: ElementModel, second: ElementModel): Boolean {
    if (first === second) return true
    if (first.elementType != second.elementType) return false
    if (first.meta !== second.meta) return false
    val equalsVisitor = EqualsVisitor()
    return forEach(listOf(first, second), equalsVisitor, true) != TraversalAction.ABORT_TREE
}

private class EqualsVisitor :
    AbstractLeaderManyModelVisitor<TraversalAction>(TraversalAction.CONTINUE) {
    override fun visitEntity(leaderEntityList: List<EntityModel?>): TraversalAction =
        visitElement(leaderEntityList)

    override fun visitSingleField(leaderFieldList: List<SingleFieldModel?>): TraversalAction =
        visitElement(leaderFieldList)

    override fun visitSetField(leaderFieldList: List<SetFieldModel?>): TraversalAction =
        visitElement(leaderFieldList)

    override fun visitPrimitive(leaderValueList: List<PrimitiveModel?>): TraversalAction =
        visitValue(leaderValueList)

    override fun visitAlias(leaderValueList: List<AliasModel?>): TraversalAction =
        visitValue(leaderValueList)

    override fun visitPassword1way(leaderValueList: List<Password1wayModel?>): TraversalAction =
        visitValue(leaderValueList)

    override fun visitPassword2way(leaderValueList: List<Password2wayModel?>): TraversalAction =
        visitValue(leaderValueList)

    override fun visitEnumeration(leaderValueList: List<EnumerationModel?>): TraversalAction =
        visitValue(leaderValueList)

    // Associations are traversed (traverseAssociations is true) and compared
    // via their value trees instead of using the `difference()` operator
    // (which copies the association path trees into new entity models before
    // comparing them) so that the comparison stays light-weight and aborts
    // as soon as inequality is detected.
    override fun visitAssociation(leaderValueList: List<AssociationModel?>): TraversalAction =
        visitElement(leaderValueList)

    // Helpers

    private fun visitElement(leaderElementList: List<ElementModel?>): TraversalAction {
        val first = leaderElementList.first()
        val second = leaderElementList.last()
        if (first == null) return if (second == null) TraversalAction.CONTINUE else TraversalAction.ABORT_TREE
        if (second == null) return TraversalAction.ABORT_TREE
        if (first.meta !== second.meta) return TraversalAction.ABORT_TREE
        return TraversalAction.CONTINUE
    }

    private fun visitValue(leaderValueList: List<ElementModel?>): TraversalAction {
        val first = leaderValueList.first()
        val second = leaderValueList.last()
        if (first == null) return if (second == null) TraversalAction.CONTINUE else TraversalAction.ABORT_TREE
        if (second == null) return TraversalAction.ABORT_TREE
        return if (first.matches(second)) TraversalAction.CONTINUE else TraversalAction.ABORT_TREE
    }
}
