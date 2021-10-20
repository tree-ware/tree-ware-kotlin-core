package org.treeWare.model.decoder

import org.treeWare.model.core.MutableMainModel

data class ModelDecoderResult(val mainModel: MutableMainModel?, val decodeErrors: List<String>)
