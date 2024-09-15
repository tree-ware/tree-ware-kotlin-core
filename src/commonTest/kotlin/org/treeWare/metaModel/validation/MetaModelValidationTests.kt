package org.treeWare.metaModel.validation

import org.treeWare.metaModel.assertJsonStringValidationErrors
import org.treeWare.metaModel.testMetaModelCommonPackageJson
import kotlin.test.Test

class MetaModelValidationTests {
    @Test
    fun `Name must be specified`() {
        val metaModelJson = """
            {
                "package": "org.tree_ware.test",
                "version": {
                  "semantic": "1.0.0",
                  "name": "pacific-ocean"
                },
                "root": {
                  "entity": "entity1",
                  "package": "org.tree_ware.test.common"
                },
                "packages": [$testMetaModelCommonPackageJson]
            }
        """.trimIndent()
        val expectedErrors = listOf("Meta-model 'name' is missing")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Package must be specified`() {
        val metaModelJson = """
            {
                "name": "test_meta_model",
                "version": {
                  "semantic": "1.0.0",
                  "name": "pacific-ocean"
                },
                "root": {
                  "entity": "entity1",
                  "package": "org.tree_ware.test.common"
                },
                "packages": [$testMetaModelCommonPackageJson]
            }
        """.trimIndent()
        val expectedErrors = listOf("Meta-model 'package' is missing")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }
}
