package org.treeWare.model.core

class Resolved(val fullName: String) {
    var enumerationMeta: EntityModel<Resolved>? = null
        internal set

    var associationMeta: ResolvedAssociationMeta? = null
        internal set

    var entityMeta: EntityModel<Resolved>? = null
        internal set
}

data class ResolvedAssociationMeta(
    val target: EntityModel<Resolved>,
    val pathEntityMetaList: List<EntityModel<Resolved>>,
    val keyPathElementList: List<String>,
    val keyEntityMetaList: List<EntityModel<Resolved>>
)
