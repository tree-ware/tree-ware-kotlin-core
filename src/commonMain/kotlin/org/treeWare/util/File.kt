package org.treeWare.util

import okio.FileSystem
import okio.Path.Companion.toPath
import okio.Source
import okio.buffer

fun getFileSource(filePath: String, fileSystem: FileSystem = FileSystem.RESOURCES): Source =
    fileSystem.source(filePath.toPath())

fun readFile(filePath: String, fileSystem: FileSystem = FileSystem.RESOURCES): String =
    getFileSource(filePath, fileSystem).use { it.buffer().readUtf8() }