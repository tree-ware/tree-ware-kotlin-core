package org.treeWare.model.core

import org.treeWare.util.hash

interface ElementModelId {
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}

fun newElementModelId(element: ElementModel): ElementModelId = when (val type = element.elementType) {
    ModelElementType.ENTITY -> BaseEntityModelId(element as BaseEntityModel)
    else -> throw UnsupportedOperationException("Not supported for $type elements")
}

interface EntityModelId : ElementModelId {
    /** Key values. Keys are in sorted meta-model order and composite keys are flattened.
     */
    val keyValues: List<Any?>
}

abstract class AbstractEntityModelId : EntityModelId {
    override fun equals(other: Any?): Boolean {
        val that = other as? EntityModelId ?: return false
        return this.keyValues == that.keyValues
    }

    override fun hashCode(): Int = hash(this.keyValues)

}

class BaseEntityModelId(private val entity: BaseEntityModel) : AbstractEntityModelId() {
    override val keyValues: List<Any?> get() = entity.getKeyValues()
}

/**
 * @param keyValues Key values. Keys must be in sorted meta-model order and composite keys must be flattened.
 */
class EntityKeysModelId(override val keyValues: List<Any?>) : AbstractEntityModelId()
