package org.treeWare.metaModel.validation

import org.treeWare.metaModel.assertJsonStringValidationErrors
import kotlin.test.Test

class VersionValidationTests {
    @Test
    fun `Version entity must be specified in the meta-model`() {
        val metaModelJson = """
            {
              "name": "test_meta_model",
              "root": {
                "entity": "entity1",
                "package": "test.common"
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
              "name": "test_meta_model",
              "version": {},
              "root": {
                "entity": "entity1",
                "package": "test.common"
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
              "name": "test_meta_model",
              "version": {
                "semantic": "1"
              },
              "root": {
                "entity": "entity1",
                "package": "test.common"
              },
              "packages": []
            }
        """.trimIndent()
        val expectedErrors = listOf("Strictly invalid semantic version: 1")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }
}