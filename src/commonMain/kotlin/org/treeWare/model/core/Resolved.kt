package org.treeWare.model.core

const val RESOLVED_AUX = "resolved"

class Resolved(val fullName: String) {
    var password1wayHasher: Hasher? = null
        internal set

    var password2wayCipher: Cipher? = null
        internal set

    var enumerationMeta: EntityModel? = null
        internal set

    var associationMeta: ResolvedAssociationMeta? = null
        internal set

    var compositionMeta: EntityModel? = null
        internal set

    internal val parentFieldsMetaInternal = mutableListOf<EntityModel>()

    var parentFieldsMeta: List<EntityModel> = parentFieldsMetaInternal
}

data class ResolvedAssociationMeta(
    val rootEntityMeta: EntityModel,
    val targetEntityMeta: EntityModel
)