package org.treeWare.metaModel

import org.treeWare.metaModel.validation.validate
import org.treeWare.model.core.MutableModel
import org.treeWare.model.core.Resolved
import org.treeWare.model.getModel

const val ADDRESS_BOOK_META_MODEL_FILE_PATH = "metaModel/address_book_meta_model.json"

fun newAddressBookMetaModel(): MutableModel<Resolved> {
    val metaMetaModel = newMetaMetaModel()
    val metaModel = getModel<Resolved>(metaMetaModel, ADDRESS_BOOK_META_MODEL_FILE_PATH)
    val errors = validate(metaModel)
    if (errors.isNotEmpty()) throw IllegalStateException("Address-book meta-model is not valid")
    return metaModel
}
