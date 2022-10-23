package org.treeWare.model.operator

import okio.Buffer
import org.lighthousegames.logging.logging
import org.treeWare.model.core.ElementModel
import org.treeWare.model.encoder.AuxEncoder
import org.treeWare.model.encoder.MultiAuxEncoder
import org.treeWare.model.encoder.encodeJson
import org.treeWare.model.operator.rbac.aux.PERMISSIONS_AUX_NAME
import org.treeWare.model.operator.rbac.aux.PermissionsAuxEncoder
import org.treeWare.model.operator.set.aux.SET_AUX_NAME
import org.treeWare.model.operator.set.aux.SetAuxEncoder

private val logger = logging()

private val coreAuxEncoders = arrayOf(
    SET_AUX_NAME to SetAuxEncoder(),
    PERMISSIONS_AUX_NAME to PermissionsAuxEncoder()
)

fun logModel(description: String, element: ElementModel, vararg nonCoreAuxEncoders: Pair<String, AuxEncoder>) {
    logger.info { description }
    val buffer = Buffer()
    val multiAuxEncoder = MultiAuxEncoder(*coreAuxEncoders, *nonCoreAuxEncoders)
    val success = encodeJson(element, buffer, multiAuxEncoder, prettyPrint = true)
    logger.info { buffer.readUtf8() }
    logger.info { "$description encoding succeeded: $success" }
}