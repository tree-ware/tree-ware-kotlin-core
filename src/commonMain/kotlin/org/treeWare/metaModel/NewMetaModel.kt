package org.treeWare.metaModel

import okio.Buffer
import okio.BufferedSource
import okio.buffer
import org.lighthousegames.logging.logging
import org.treeWare.metaModel.aux.MetaModelAuxPlugin
import org.treeWare.metaModel.validation.validate
import org.treeWare.model.core.Cipher
import org.treeWare.model.core.Hasher
import org.treeWare.model.decoder.decodeJson
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import org.treeWare.util.getFileSource

private val logger = logging()

/** Returns a validated meta-model created from the meta-model files. */
fun newMetaModelFromJsonFiles(
    metaModelFiles: List<String>,
    logMetaModelFullNames: Boolean,
    hasher: Hasher?,
    cipher: Cipher?,
    metaModelAuxPlugins: List<MetaModelAuxPlugin>,
    logErrors: Boolean
): ValidatedMetaModel {
    val sources = metaModelFiles.map { getFileSource(it) }
    return try {
        newMetaModelFromJsonReaders(
            sources.map { it.buffer() },
            logMetaModelFullNames,
            hasher,
            cipher,
            metaModelAuxPlugins,
            logErrors
        )
    } finally {
        sources.forEach { it.close() }
    }
}

/** Returns a validated meta-model created from the meta-model strings. */
fun newMetaModelFromJsonStrings(
    metaModelStrings: List<String>,
    logMetaModelFullNames: Boolean,
    hasher: Hasher?,
    cipher: Cipher?,
    metaModelAuxPlugins: List<MetaModelAuxPlugin>,
    logErrors: Boolean
): ValidatedMetaModel {
    val sources = metaModelStrings.map { Buffer().writeUtf8(it) }
    return newMetaModelFromJsonReaders(
        sources,
        logMetaModelFullNames,
        hasher,
        cipher,
        metaModelAuxPlugins,
        logErrors
    )
}

/** Returns a validated meta-model created from the meta-model readers. */
fun newMetaModelFromJsonReaders(
    metaModelSources: List<BufferedSource>,
    logMetaModelFullNames: Boolean,
    hasher: Hasher?,
    cipher: Cipher?,
    metaModelAuxPlugins: List<MetaModelAuxPlugin>,
    logErrors: Boolean
): ValidatedMetaModel {
    val metaModel = MetaModelMutableMainModelFactory.getNewInstance()
    // TODO(performance): change MultiAuxDecodingStateMachineFactory() varargs to list to avoid array copies.
    val multiAuxDecodingStateMachineFactory =
        MultiAuxDecodingStateMachineFactory(*metaModelAuxPlugins.map { it.auxName to it.auxDecodingStateMachineFactory }
            .toTypedArray())
    metaModelSources.forEach { source ->
        val decodeErrors = decodeJson(
            source,
            metaModel,
            multiAuxDecodingStateMachineFactory = multiAuxDecodingStateMachineFactory
        )
        if (decodeErrors.isNotEmpty()) {
            if (logErrors) decodeErrors.forEach { logger.error { it } }
            return ValidatedMetaModel(null, decodeErrors)
        }
    }
    val baseErrors = validate(metaModel, hasher, cipher, logMetaModelFullNames)
    if (logErrors) baseErrors.forEach { logger.error { it } }
    if (baseErrors.isNotEmpty()) return ValidatedMetaModel(null, baseErrors)
    val pluginErrors = metaModelAuxPlugins.flatMap { plugin -> plugin.validate(metaModel) }
    if (logErrors) pluginErrors.forEach { logger.error { it } }
    return ValidatedMetaModel(metaModel.takeIf { pluginErrors.isEmpty() }, pluginErrors)
}