package org.treeWare.util

import java.io.InputStreamReader
import java.io.Reader

fun getFileReader(filePath: String): Reader =
    ClassLoader.getSystemResourceAsStream(filePath)?.let { InputStreamReader(it) }
        ?: throw IllegalArgumentException("File $filePath not found")