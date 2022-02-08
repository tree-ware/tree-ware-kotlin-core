package org.treeWare.util

import java.util.*

actual fun encodeBase64(value: ByteArray): String = Base64.getEncoder().encodeToString(value)
actual fun decodeBase64(value: String): ByteArray = Base64.getDecoder().decode(value)