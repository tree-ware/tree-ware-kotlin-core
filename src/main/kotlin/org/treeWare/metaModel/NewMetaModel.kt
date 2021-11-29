package org.treeWare.metaModel

import org.apache.logging.log4j.LogManager
import org.treeWare.metaModel.aux.MetaModelAuxPlugin
import org.treeWare.metaModel.validation.validate
import org.treeWare.model.core.Cipher
import org.treeWare.model.core.Hasher
import org.treeWare.model.core.MainModel
import org.treeWare.model.decoder.decodeJson
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import org.treeWare.model.operator.union
import java.io.InputStreamReader

/** Returns a validated meta-model created from the meta-model files. */
fun newMetaModel(
    metaModelFiles: List<String>,
    logMetaModelFullNames: Boolean,
    hasher: Hasher?,
    cipher: Cipher?,
    metaModelAuxPlugins: List<MetaModelAuxPlugin>
): MainModel {
    val metaMetaModel = newMainMetaMetaModel()
    val metaModelParts = metaModelFiles.map { file ->
        val reader = ClassLoader.getSystemResourceAsStream(file)?.let { InputStreamReader(it) }
            ?: throw IllegalArgumentException("Meta-model file $file not found")
        // TODO(performance): change MultiAuxDecodingStateMachineFactory() varargs to list to avoid array copies.
        val multiAuxDecodingStateMachineFactory =
            MultiAuxDecodingStateMachineFactory(*metaModelAuxPlugins.map { it.auxName to it.auxDecodingStateMachineFactory }
                .toTypedArray())
        val (decodedMetaModel, decodeErrors) = decodeJson(
            reader,
            metaMetaModel,
            "data",
            multiAuxDecodingStateMachineFactory = multiAuxDecodingStateMachineFactory
        )
        if (decodedMetaModel == null || decodeErrors.isNotEmpty()) {
            val logger = LogManager.getLogger()
            decodeErrors.forEach { logger.error(it) }
            throw IllegalArgumentException("Unable to decode meta-model file $file")
        }
        decodedMetaModel
    }
    val metaModel = union(metaModelParts)
    val metaModelErrors = validate(metaModel, hasher, cipher, logMetaModelFullNames)
    if (metaModelErrors.isNotEmpty()) throw IllegalArgumentException("Meta-model has validation errors")
    metaModelAuxPlugins.forEach { plugin ->
        val pluginErrors = plugin.validate(metaModel)
        if (pluginErrors.isNotEmpty()) {
            val logger = LogManager.getLogger()
            pluginErrors.forEach { logger.error(it) }
            throw IllegalArgumentException("Meta-model has plugin validation errors")
        }
    }
    return metaModel
}