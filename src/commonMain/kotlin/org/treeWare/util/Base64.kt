package org.treeWare.util

expect fun encodeBase64(value: ByteArray): String
expect fun decodeBase64(value: String): ByteArray