package org.treeWare.model.decoder

import org.treeWare.model.core.MutableMainModel

data class ModelDecoderResult<Aux>(val mainModel: MutableMainModel<Aux>?, val decodeErrors: List<String>)
