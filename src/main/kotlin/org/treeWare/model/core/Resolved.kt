package org.treeWare.model.core

class Resolved(val fullName: String) {
    var enumerationMeta: EntityModel<Resolved>? = null
        internal set

    var associationMeta: List<EntityModel<Resolved>>? = null
        internal set

    var entityMeta: EntityModel<Resolved>? = null
        internal set
}
