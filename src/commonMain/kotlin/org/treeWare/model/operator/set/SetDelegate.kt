package org.treeWare.model.operator.set

import org.treeWare.model.core.EntityModel
import org.treeWare.model.core.FieldModel
import org.treeWare.model.core.Keys
import org.treeWare.model.core.SingleFieldModel
import org.treeWare.model.operator.ElementModelError
import org.treeWare.model.operator.set.aux.SetAux

interface SetDelegate {
    fun begin(): List<ElementModelError>

    /** Sets the specified entity.
     *
     * @param ancestorKeys First element is "self", second is parent, followed by increasing level of ancestors.
     */
    fun setEntity(
        setAux: SetAux,
        entity: EntityModel,
        entityPath: String,
        ancestorKeys: List<Keys>,
        keys: List<SingleFieldModel>,
        associations: List<FieldModel>,
        other: List<FieldModel>
    ): List<ElementModelError>

    fun end(): List<ElementModelError>
}