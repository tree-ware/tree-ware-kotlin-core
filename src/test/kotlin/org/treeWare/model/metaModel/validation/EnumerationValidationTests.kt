package org.treeWare.model.metaModel.validation

import org.treeWare.metaModel.assertJsonStringValidationErrors
import org.treeWare.metaModel.newTestMetaModelJson
import org.treeWare.metaModel.testMetaModelCommonPackageJson
import org.treeWare.metaModel.testMetaModelCommonRootJson
import kotlin.test.Test

class EnumerationValidationTests {
    @Test
    fun `Enumeration must have info`() {
        val testPackageJson = """
            | {
            |   "name": "test.main",
            |   "entities": [
            |     {
            |       "name": "test_entity",
            |       "fields": [
            |         {
            |           "name": "test_field",
            |           "type": "enumeration"
            |         }
            |       ]
            |     }
            |   ]
            | }
        """.trimMargin()
        val metaModelJson =
            newTestMetaModelJson(testMetaModelCommonRootJson, testMetaModelCommonPackageJson, testPackageJson)
        val expectedErrors = listOf("Package 1 entity 0 field 0 enumeration info is missing")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Enumeration must have non-empty info`() {
        val testPackageJson = """
            | {
            |   "name": "test.main",
            |   "entities": [
            |     {
            |       "name": "test_entity",
            |       "fields": [
            |         {
            |           "name": "test_field",
            |           "type": "enumeration",
            |           "enumeration": {}
            |         }
            |       ]
            |     }
            |   ]
            | }
        """.trimMargin()
        val metaModelJson =
            newTestMetaModelJson(testMetaModelCommonRootJson, testMetaModelCommonPackageJson, testPackageJson)
        val expectedErrors = listOf(
            "Package 1 entity 0 field 0 enumeration info name is missing",
            "Package 1 entity 0 field 0 enumeration info package is missing"
        )
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Enumeration must refer to a defined enumeration`() {
        val testPackageJson = """
            | {
            |   "name": "test.main",
            |   "entities": [
            |     {
            |       "name": "test_entity",
            |       "fields": [
            |         {
            |           "name": "test_field",
            |           "type": "enumeration",
            |           "enumeration": {
            |             "name": "undefined_enumeration",
            |             "package": "test.common"
            |           }
            |         }
            |       ]
            |     }
            |   ]
            | }
        """.trimMargin()
        val metaModelJson =
            newTestMetaModelJson(testMetaModelCommonRootJson, testMetaModelCommonPackageJson, testPackageJson)
        val expectedErrors = listOf("Enumeration /test.common/undefined_enumeration cannot be resolved")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Enumeration must be valid if info can be resolved`() {
        val testPackageJson = """
            | {
            |   "name": "test.main",
            |   "entities": [
            |     {
            |       "name": "test_entity",
            |       "fields": [
            |         {
            |           "name": "test_field",
            |           "type": "enumeration",
            |           "enumeration": {
            |             "name": "enumeration1",
            |             "package": "test.common"
            |           }
            |         }
            |       ]
            |     }
            |   ]
            | }
        """.trimMargin()
        val metaModelJson =
            newTestMetaModelJson(testMetaModelCommonRootJson, testMetaModelCommonPackageJson, testPackageJson)
        val expectedErrors = listOf<String>()
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }
}