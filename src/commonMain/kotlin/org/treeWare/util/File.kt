package org.treeWare.util

import okio.*
import okio.Path.Companion.toPath
import kotlin.io.use

fun getFileSource(filePath: String): Source = FileSystem.RESOURCES.openReadOnly(filePath.toPath()).source()

fun readFile(filePath: String): String = getFileSource(filePath).use { it.buffer().readUtf8() }