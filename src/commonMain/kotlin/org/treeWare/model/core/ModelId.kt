package org.treeWare.model.core

import org.treeWare.util.hash

interface ElementModelId {
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}

fun newElementModelId(element: ElementModel): ElementModelId = when (val type = element.elementType) {
    ModelElementType.ENTITY -> EntityModelId(element as EntityModel)
    else -> throw UnsupportedOperationException("Not supported for $type elements")
}

abstract class AbstractEntityModelId : ElementModelId {
    /** Key values. Keys are in sorted meta-model order and composite keys are flattened.
     */
    abstract val keyValues: List<Any?>

    override fun equals(other: Any?): Boolean {
        val that = other as? EntityModelId ?: return false
        return this.keyValues == that.keyValues
    }

    override fun hashCode(): Int = hash(this.keyValues)
}

class EntityModelId(private val entity: EntityModel) : AbstractEntityModelId() {
    override val keyValues: List<Any?> get() = entity.getKeyValues()
}

/**
 * @param keyValues Key values. Keys must be in sorted meta-model order and composite keys must be flattened.
 */
class EntityKeysModelId(override val keyValues: List<Any?>) : AbstractEntityModelId()
