package org.treeWare.model.operator

import org.treeWare.model.core.ElementModel
import org.treeWare.model.encoder.encodeJson
import java.io.PrintWriter

fun print(description: String, element: ElementModel) {
    println("$description:")
    val printWriter = PrintWriter(System.out)
    val success = encodeJson(element, printWriter, prettyPrint = true)
    printWriter.flush()
    println() // since encodeJson() ends without a new line.
    println("$description encoding succeeded: $success")
}
