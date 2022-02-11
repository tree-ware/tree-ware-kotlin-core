package org.treeWare.metaModel

import org.lighthousegames.logging.logging
import org.treeWare.metaModel.aux.MetaModelAuxPlugin
import org.treeWare.metaModel.validation.validate
import org.treeWare.model.core.Cipher
import org.treeWare.model.core.Hasher
import org.treeWare.model.decoder.decodeJson
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import org.treeWare.model.operator.union
import org.treeWare.util.getFileReader
import java.io.Reader
import java.io.StringReader

private val logger = logging()

/** Returns a validated meta-model created from the meta-model files. */
fun newMetaModelFromJsonFiles(
    metaModelFiles: List<String>,
    logMetaModelFullNames: Boolean,
    hasher: Hasher?,
    cipher: Cipher?,
    metaModelAuxPlugins: List<MetaModelAuxPlugin>,
    logErrors: Boolean
): ValidatedMetaModel = newMetaModelFromJsonReaders(
    metaModelFiles.map { getFileReader(it) },
    logMetaModelFullNames,
    hasher,
    cipher,
    metaModelAuxPlugins,
    logErrors
)

/** Returns a validated meta-model created from the meta-model strings. */
fun newMetaModelFromJsonStrings(
    metaModelStrings: List<String>,
    logMetaModelFullNames: Boolean,
    hasher: Hasher?,
    cipher: Cipher?,
    metaModelAuxPlugins: List<MetaModelAuxPlugin>,
    logErrors: Boolean
): ValidatedMetaModel = newMetaModelFromJsonReaders(
    metaModelStrings.map { StringReader(it) },
    logMetaModelFullNames,
    hasher,
    cipher,
    metaModelAuxPlugins,
    logErrors
)

/** Returns a validated meta-model created from the meta-model readers. */
fun newMetaModelFromJsonReaders(
    metaModelReaders: List<Reader>,
    logMetaModelFullNames: Boolean,
    hasher: Hasher?,
    cipher: Cipher?,
    metaModelAuxPlugins: List<MetaModelAuxPlugin>,
    logErrors: Boolean
): ValidatedMetaModel {
    val metaMetaModel = newMainMetaMetaModel()
    // TODO(performance): change MultiAuxDecodingStateMachineFactory() varargs to list to avoid array copies.
    val multiAuxDecodingStateMachineFactory =
        MultiAuxDecodingStateMachineFactory(*metaModelAuxPlugins.map { it.auxName to it.auxDecodingStateMachineFactory }
            .toTypedArray())
    val metaModelParts = metaModelReaders.map { reader ->
        val (decodedMetaModel, decodeErrors) = decodeJson(
            reader,
            metaMetaModel,
            multiAuxDecodingStateMachineFactory = multiAuxDecodingStateMachineFactory
        )
        if (decodedMetaModel == null || decodeErrors.isNotEmpty()) {
            if (logErrors) decodeErrors.forEach { logger.error { it } }
            return ValidatedMetaModel(null, decodeErrors)
        }
        decodedMetaModel
    }
    val metaModel = union(metaModelParts)
    val baseErrors = validate(metaModel, hasher, cipher, logMetaModelFullNames)
    val pluginErrors = metaModelAuxPlugins.flatMap { plugin -> plugin.validate(metaModel) }
    val errors = baseErrors + pluginErrors
    if (logErrors) errors.forEach { logger.error { it } }
    return ValidatedMetaModel(metaModel.takeIf { errors.isEmpty() }, errors)
}