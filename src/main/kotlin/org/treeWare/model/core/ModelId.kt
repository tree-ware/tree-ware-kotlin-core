package org.treeWare.model.core

interface ElementModelId {
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}

fun newElementModelId(element: ElementModel<*>): ElementModelId = when (val type = element.elementType) {
    ModelElementType.ENTITY -> EntityModelId(element as BaseEntityModel<*>)
    else -> throw UnsupportedOperationException("Not supported for $type elements")
}

class EntityModelId<Aux>(private val entity: BaseEntityModel<Aux>) : ElementModelId {
    override fun equals(other: Any?): Boolean {
        val thatId = other as? EntityModelId<*> ?: return false
        return entity.matches(thatId.entity)
    }

    override fun hashCode(): Int = entity.getMatchingHashCode()
}
