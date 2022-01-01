package org.treeWare.metaModel.validation

import org.treeWare.metaModel.assertJsonStringValidationErrors
import org.treeWare.metaModel.newTestMetaModelJson
import org.treeWare.metaModel.testMetaModelCommonPackageJson
import org.treeWare.metaModel.testMetaModelCommonRootJson
import kotlin.test.Test

class PackageValidationTests {
    @Test
    fun `Packages list must be specified`() {
        val metaModelJson = """
            | {
            |   "meta_model": {
            |     "root": {
            |       "name": "root",
            |       "type": "composition",
            |       "composition": {
            |         "name": "entity1",
            |         "package": "test.common"
            |       }
            |     }
            |   }
            | }
        """.trimMargin()
        val expectedErrors = listOf("Packages are missing")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Packages list must not be empty`() {
        val metaModelJson = """
            | {
            |   "meta_model": {
            |     "root": {
            |       "name": "root",
            |       "type": "composition",
            |       "composition": {
            |         "name": "entity1",
            |         "package": "test.common"
            |       }
            |     },
            |     "packages": []
            |   }
            | }
        """.trimMargin()
        val expectedErrors = listOf("Entity /test.common/entity1 cannot be resolved")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Package must have a name`() {
        val metaModelJson = """
            | {
            |   "meta_model": {
            |     "root": {
            |       "name": "root",
            |       "type": "composition",
            |       "composition": {
            |         "name": "entity1",
            |         "package": "test.common"
            |       }
            |     },
            |     "packages": [
            |       {},
            |       {"name": "package1"},
            |       {}
            |     ]
            |   }
            | }
        """.trimMargin()
        val expectedDecodeErrors = listOf(
            "Missing key fields: [name]",
            "Missing key fields: [name]"
        )
        val expectedErrors = listOf("Entity /test.common/entity1 cannot be resolved")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors, expectedDecodeErrors)
    }

    @Test
    fun `Package entities may be empty`() {
        val testPackageJson = """
            | {
            |   "name": "package1",
            |   "entities": []
            | }
        """.trimMargin()
        val metaModelJson =
            newTestMetaModelJson(testMetaModelCommonRootJson, testMetaModelCommonPackageJson, testPackageJson)
        val expectedErrors = listOf<String>()
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Package enumerations may be empty`() {
        val testPackage = """
            | {
            |   "name": "package1",
            |   "enumerations": []
            | }
        """.trimMargin()
        val metaModelJson =
            newTestMetaModelJson(testMetaModelCommonRootJson, testMetaModelCommonPackageJson, testPackage)
        val expectedErrors = listOf<String>()
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }
}
