package org.treeWare.json

import kotlinx.benchmark.*
import okio.Buffer

@State(Scope.Benchmark)
open class TokenizeJsonBenchmark {
    @Benchmark
    fun tokenize() {
        val buffer = Buffer().writeUtf8(
            """
            {
                "true-field": true,
                "false_field": false,
                "nullField": null,
                "StringField": "hello",
                "NUMBER_FIELD": -123.45e+6,
                "empty object": {},
                "non-empty object": {"key": "value"},
                "empty array": [],
                "non-empty array": [true, false]
            }
            """.trimIndent()
        )
        tokenizeJson(buffer).toList()
    }
}