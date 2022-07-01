package org.treeWare.model.operator.set.aux

import org.treeWare.model.core.ElementModel
import org.treeWare.model.core.getAux

const val SET_AUX_NAME = "set"

enum class SetAux {
    CREATE,
    UPDATE,
    DELETE
}

fun getSetAux(element: ElementModel?): SetAux? = element?.getAux(SET_AUX_NAME)

fun setSetAux(element: ElementModel, aux: SetAux) {
    element.setAux(SET_AUX_NAME, aux)
}