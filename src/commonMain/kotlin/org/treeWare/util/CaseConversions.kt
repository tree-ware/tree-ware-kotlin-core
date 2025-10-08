package org.treeWare.util

fun String.snakeCaseToUpperCamelCase(): String =
    this.splitToSequence("_").joinToString("") { it.replaceFirstChar(Char::uppercase) }

fun String.snakeCaseToLowerCamelCase(): String =
    this.snakeCaseToUpperCamelCase().replaceFirstChar(Char::lowercase)