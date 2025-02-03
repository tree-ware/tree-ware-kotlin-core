package org.treeWare.metaModel

import org.treeWare.model.core.Cipher
import org.treeWare.model.core.Hasher
import org.treeWare.model.core.MutableEntityModel
import org.treeWare.model.core.MutableFieldModel

val ADDRESS_BOOK_META_MODEL_FILES = listOf(
    "tree_ware/meta_model/address_book_root.json",
    "tree_ware/meta_model/address_book_main.json",
    "tree_ware/meta_model/address_book_city.json",
    "tree_ware/meta_model/address_book_club.json",
    "tree_ware/meta_model/address_book_keyless.json",
    "org/treeWare/metaModel/geo.json"
)

fun newAddressBookMetaModel(hasher: Hasher?, cipher: Cipher?): ValidatedMetaModel = newMetaModelFromJsonFiles(
    ADDRESS_BOOK_META_MODEL_FILES,
    false,
    hasher,
    cipher,
    ::metaModelRootEntityFactory,
    emptyList(),
    true
)

val addressBookMetaModel = newAddressBookMetaModel(null, null).metaModel
    ?: throw IllegalStateException("Meta-model has validation errors")

val addressBookRootEntityMeta = getResolvedRootMeta(addressBookMetaModel)

fun addressBookRootEntityFactory(parent: MutableFieldModel?) =
    MutableEntityModel(addressBookRootEntityMeta, parent)