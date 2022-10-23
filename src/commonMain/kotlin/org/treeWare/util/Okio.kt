package org.treeWare.util

import okio.BufferedSink
import okio.Sink
import okio.buffer

fun Sink.buffered(): BufferedSink = if (this is BufferedSink) this else this.buffer()