package org.treeWare.model.core

fun getMetaModelResolved(elementMeta: ElementModel?): Resolved? =
    elementMeta?.getAux<Resolved>(RESOLVED_AUX)