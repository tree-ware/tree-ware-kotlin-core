package org.treeWare.metaModel

import org.treeWare.metaModel.validation.validate
import org.treeWare.model.core.MutableMainModel
import org.treeWare.model.core.Resolved
import org.treeWare.model.getMainModel

const val ADDRESS_BOOK_META_MODEL_FILE_PATH = "metaModel/address_book_meta_model.json"

fun newAddressBookMetaModel(): MutableMainModel<Resolved> {
    val metaMetaModel = newMainMetaMetaModel()
    val metaModel = getMainModel<Resolved>(metaMetaModel, ADDRESS_BOOK_META_MODEL_FILE_PATH)
    val errors = validate(metaModel)
    if (errors.isNotEmpty()) throw IllegalStateException("Address-book meta-model is not valid")
    return metaModel
}
