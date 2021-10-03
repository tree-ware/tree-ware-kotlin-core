package org.treeWare.metaModel

import org.treeWare.metaModel.encoder.encodeDot
import java.io.File
import kotlin.test.Test

// TODO(deepak-nulu): make doc generation a gradle task

class MetaModelDocs {
    @Test
    fun `Generate address-book meta-model docs`() {
        val metaModel = newAddressBookMetaModel(null, null)

        val metaModelName = getMetaName(getRootMeta(metaModel))
        val fileName = "${metaModelName}_meta_model"
        val fileWriter = File("${fileName}.dot").bufferedWriter()
        encodeDot(metaModel, fileWriter)
        fileWriter.flush()

        Runtime.getRuntime().exec("dot -Tpng ${fileName}.dot -o ${fileName}.png").waitFor()
    }
}