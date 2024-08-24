package org.treeWare.model.operator

import org.treeWare.metaModel.Granularity
import org.treeWare.metaModel.getGranularityMeta
import org.treeWare.model.core.EntityModel
import org.treeWare.util.assertInDevMode

class GranularityStack {
    fun peekActive(): Granularity = activeGranularityStack.firstOrNull() ?: Granularity.FIELD
    fun isEmpty(): Boolean = currentGranularityStack.isEmpty()

    fun push(entity: EntityModel) {
        val parentFieldMeta = entity.parent?.meta
        // Unspecified defaults to FIELD:
        val granularity = parentFieldMeta?.let { getGranularityMeta(it) } ?: Granularity.FIELD
        push(granularity)
    }

    private fun push(granularity: Granularity) {
        val active = activeGranularityStack.firstOrNull()
        if (active == null || granularity > active) {
            currentGranularityStack.addFirst(granularity)
            activeGranularityStack.addFirst(granularity)
        } else currentGranularityStack.addFirst(null)
    }

    fun pop() {
        val current = currentGranularityStack.removeFirst()
        current?.also {
            val active = activeGranularityStack.removeFirst()
            assertInDevMode(active == it)
        }
    }

    private val currentGranularityStack = ArrayDeque<Granularity?>()
    private val activeGranularityStack = ArrayDeque<Granularity>()
}