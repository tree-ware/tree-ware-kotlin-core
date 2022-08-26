package org.treeWare.model.operator

import org.treeWare.model.core.ElementModel
import org.treeWare.model.encoder.AuxEncoder
import org.treeWare.model.encoder.MultiAuxEncoder
import org.treeWare.model.encoder.encodeJson
import org.treeWare.model.operator.rbac.aux.PERMISSIONS_AUX_NAME
import org.treeWare.model.operator.rbac.aux.PermissionsAuxEncoder
import org.treeWare.model.operator.set.aux.SET_AUX_NAME
import org.treeWare.model.operator.set.aux.SetAuxEncoder
import java.io.PrintWriter

private val coreAuxEncoders = arrayOf(
    SET_AUX_NAME to SetAuxEncoder(),
    PERMISSIONS_AUX_NAME to PermissionsAuxEncoder()
)

fun print(description: String, element: ElementModel, vararg nonCoreAuxEncoders: Pair<String, AuxEncoder>) {
    println("$description:")
    val printWriter = PrintWriter(System.out)
    val multiAuxEncoder = MultiAuxEncoder(*coreAuxEncoders, *nonCoreAuxEncoders)
    val success = encodeJson(element, printWriter, multiAuxEncoder, prettyPrint = true)
    printWriter.flush()
    println() // since encodeJson() ends without a new line.
    println("$description encoding succeeded: $success")
}