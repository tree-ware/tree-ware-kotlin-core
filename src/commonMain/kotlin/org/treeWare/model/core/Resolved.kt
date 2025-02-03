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

    internal val sortedKeyFieldsMetaInternal = mutableListOf<EntityModel>()

    val sortedKeyFieldsMeta: List<EntityModel> = sortedKeyFieldsMetaInternal

    internal val parentFieldsMetaInternal = mutableListOf<EntityModel>()

    val parentFieldsMeta: List<EntityModel> = parentFieldsMetaInternal

    internal val recursiveAssociationFieldsMetaInternal = mutableListOf<EntityModel>()

    val recursiveAssociationFieldsMeta = recursiveAssociationFieldsMetaInternal

    internal val recursiveCompositionFieldsMetaInternal = mutableListOf<EntityModel>()

    val recursiveCompositionFieldsMeta: List<EntityModel> = recursiveCompositionFieldsMetaInternal
}

data class ResolvedAssociationMeta(
    val targetEntityMeta: EntityModel,
    val isRecursive: Boolean,
    val rootEntityFactory: EntityFactory,
)