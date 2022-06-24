package org.treeWare.model.operator.rbac

import org.lighthousegames.logging.logging
import org.treeWare.model.core.MainModel
import org.treeWare.model.encoder.MultiAuxEncoder
import org.treeWare.model.encoder.encodeJson
import org.treeWare.model.operator.rbac.aux.PERMISSIONS_AUX_NAME
import org.treeWare.model.operator.rbac.aux.PermissionsAuxEncoder
import java.io.StringWriter

private val logger = logging()

fun logRbacModel(description: String, rbac: MainModel) {
    logger.info { description }
    val stringWriter = StringWriter()
    val multiAuxEncoder = MultiAuxEncoder(PERMISSIONS_AUX_NAME to PermissionsAuxEncoder())
    val success = encodeJson(rbac, stringWriter, multiAuxEncoder, prettyPrint = true)
    logger.info { stringWriter }
    logger.info { "$description encoding succeeded: $success" }
}