package org.treeWare.model.encoder

import okio.BufferedSink
import org.treeWare.model.core.*
import org.treeWare.model.operator.ModelPathStack
import org.treeWare.model.traversal.*

class PathEncodingVisitor(
    private val sink: BufferedSink
) : ModelTraversalVisitor {

    private val pathStack = ModelPathStack()

    override fun visitEntity(entity: EntityModel): TraversalAction {
        pathStack.pushEntity(entity)
        return TraversalAction.CONTINUE
    }

    override fun leaveEntity(entity: EntityModel) {
        pathStack.pop()
    }

    override fun visitCompositionField(field: CompositionFieldModel): TraversalAction {
        pathStack.pushField(field)
        return TraversalAction.CONTINUE
    }

    override fun leaveCompositionField(field: CompositionFieldModel) {
        pathStack.pop()
    }

    override fun visitPrimitiveField(field: PrimitiveFieldModel): TraversalAction {
        pathStack.pushField(field)

        sink.writeUtf8(pathStack.getCurrentPath())
        sink.writeUtf8("\n")

        return TraversalAction.CONTINUE
    }

    override fun leavePrimitiveField(field: PrimitiveFieldModel) {
        pathStack.pop()
    }

    override fun visitListField(field: ListFieldModel): TraversalAction {
        pathStack.pushField(field)
        return TraversalAction.CONTINUE
    }

    override fun leaveListField(field: ListFieldModel) {
        pathStack.pop()
    }

    override fun visitListItem(item: ListItemModel): TraversalAction {
        pathStack.pushListItem(item)
        return TraversalAction.CONTINUE
    }

    override fun leaveListItem(item: ListItemModel) {
        pathStack.pop()
    }
}
