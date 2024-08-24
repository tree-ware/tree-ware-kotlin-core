package org.treeWare.metaModel.validation

import org.treeWare.metaModel.assertJsonStringValidationErrors
import kotlin.test.Test

class VersionValidationTests {
    @Test
    fun `Version entity must be specified in the meta-model`() {
        val metaModelJson = """
            {
              "root": {
                "name": "root",
                "type": "composition",
                "composition": {
                  "entity": "entity1",
                  "package": "test.common"
                }
              },
              "packages": []
            }
        """.trimIndent()
        val expectedErrors = listOf("Version is missing")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Semantic-version field must be specified in the meta-model`() {
        val metaModelJson = """
            {
              "version": {},
              "root": {
                "name": "root",
                "type": "composition",
                "composition": {
                  "entity": "entity1",
                  "package": "test.common"
                }
              },
              "packages": []
            }
        """.trimIndent()
        val expectedErrors = listOf("Semantic version is missing")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Semantic-version in the meta-model must be strictly valid`() {
        val metaModelJson = """
            {
              "version": {
                "semantic": "1"
              },
              "root": {
                "name": "root",
                "type": "composition",
                "composition": {
                  "entity": "entity1",
                  "package": "test.common"
                }
              },
              "packages": []
            }
        """.trimIndent()
        val expectedErrors = listOf("Strictly invalid semantic version: 1")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }
}