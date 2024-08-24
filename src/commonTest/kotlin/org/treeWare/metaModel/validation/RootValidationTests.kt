package org.treeWare.metaModel.validation

import org.treeWare.metaModel.assertJsonStringValidationErrors
import org.treeWare.metaModel.newTestMetaModelJson
import org.treeWare.metaModel.testMetaModelCommonPackageJson
import org.treeWare.metaModel.testMetaModelCommonRootJson
import kotlin.test.Test

class RootValidationTests {
    @Test
    fun `Root must be specified`() {
        val rootJson = null
        val metaModelJson = newTestMetaModelJson(rootJson)
        val expectedErrors = listOf("Meta-model root info is missing")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Root must refer to a valid entity`() {
        val rootJson = """
            | "root": {
            |   "entity": "non_existent_entity",
            |   "package": "non.existent.package"
            | }
        """.trimMargin()
        val metaModelJson = newTestMetaModelJson(rootJson, testMetaModelCommonPackageJson)
        val expectedErrors = listOf("Entity /non.existent.package/non_existent_entity cannot be resolved")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Root must be valid if info can be resolved`() {
        val metaModelJson = newTestMetaModelJson(testMetaModelCommonRootJson, testMetaModelCommonPackageJson)
        val expectedErrors = listOf<String>()
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }
}
