package org.treeWare.model.metaModel.validation

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
            |   "data": {
            |     "meta_model": {
            |       "root": {
            |         "name": "root",
            |         "entity": "entity1",
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
            |   "data": {
            |     "meta_model": {
            |       "root": {
            |         "name": "root",
            |         "entity": "entity1",
            |         "package": "test.common"
            |       },
            |       "packages": []
            |     }
            |   }
            | }
        """.trimMargin()
        val expectedErrors = listOf("Root entity cannot be resolved")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Package must have a name`() {
        val metaModelJson = """
            | {
            |   "data": {
            |     "meta_model": {
            |       "root": {
            |         "name": "root",
            |         "entity": "entity1",
            |         "package": "test.common"
            |       },
            |       "packages": [
            |         {},
            |         {"name": "package1"},
            |         {}
            |       ]
            |     }
            |   }
            | }
        """.trimMargin()
        val expectedErrors = listOf(
            "Package 0 name is missing",
            "Package 2 name is missing"
        )
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
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
