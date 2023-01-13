package org.treeWare.model.operator

import org.treeWare.metaModel.addressBookMetaModel
import org.treeWare.model.assertMatchesJsonString
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import org.treeWare.model.encoder.EncodePasswords
import org.treeWare.model.encoder.MultiAuxEncoder
import org.treeWare.model.getMainModelFromJsonString
import org.treeWare.model.operator.set.aux.SET_AUX_NAME
import org.treeWare.model.operator.set.aux.SetAuxEncoder
import org.treeWare.model.operator.set.aux.SetAuxStateMachine
import kotlin.test.Test
import kotlin.test.assertEquals

private val multiAuxDecodingFactory = MultiAuxDecodingStateMachineFactory(SET_AUX_NAME to { SetAuxStateMachine(it) })
private val multiAuxEncoder = MultiAuxEncoder(SET_AUX_NAME to SetAuxEncoder())

class PopulateSubTreeGranularityDeleteRequestTests {
    @Test
    fun `The operator must populate sub_tree granularity delete-requests`() {
        val setRequestJson = """
            {
              "address_book": {
                "sub_tree_persons": [
                  {
                    "set_": "delete",
                    "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f"
                  }
                ]
              }
            }
        """.trimIndent()
        val setRequest = getMainModelFromJsonString(
            addressBookMetaModel,
            setRequestJson,
            multiAuxDecodingStateMachineFactory = multiAuxDecodingFactory
        )

        val expectedErrors = emptyList<String>()
        val actualErrors = populateSubTreeGranularityDeleteRequest(setRequest)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))

        val expectedSetRequestJson = """
            {
              "address_book": {
                "sub_tree_persons": [
                  {
                    "set_": "delete",
                    "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
                    "relation": [
                      {
                        "set_": "delete",
                        "id": null
                      }
                    ],
                    "hero_details": {
                      "set_": "delete"
                    }
                  }
                ]
              }
            }
        """.trimIndent()
        assertMatchesJsonString(setRequest, expectedSetRequestJson, EncodePasswords.ALL, multiAuxEncoder)
    }

    @Test
    fun `The operator must not populate sub_tree granularity create-requests`() {
        val setRequestJson = """
            {
              "address_book": {
                "sub_tree_persons": [
                  {
                    "set_": "create",
                    "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f"
                  }
                ]
              }
            }
        """.trimIndent()
        val setRequest = getMainModelFromJsonString(
            addressBookMetaModel,
            setRequestJson,
            multiAuxDecodingStateMachineFactory = multiAuxDecodingFactory
        )

        val expectedErrors = emptyList<String>()
        val actualErrors = populateSubTreeGranularityDeleteRequest(setRequest)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
        assertMatchesJsonString(setRequest, setRequestJson, EncodePasswords.ALL, multiAuxEncoder)
    }

    @Test
    fun `The operator must not populate sub_tree granularity update-requests`() {
        val setRequestJson = """
            {
              "address_book": {
                "sub_tree_persons": [
                  {
                    "set_": "update",
                    "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f"
                  }
                ]
              }
            }
        """.trimIndent()
        val setRequest = getMainModelFromJsonString(
            addressBookMetaModel,
            setRequestJson,
            multiAuxDecodingStateMachineFactory = multiAuxDecodingFactory
        )

        val expectedErrors = emptyList<String>()
        val actualErrors = populateSubTreeGranularityDeleteRequest(setRequest)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
        assertMatchesJsonString(setRequest, setRequestJson, EncodePasswords.ALL, multiAuxEncoder)
    }

    @Test
    fun `The operator must not populate field granularity delete-requests`() {
        val setRequestJson = """
            {
              "address_book": {
                "person": [
                  {
                    "set_": "delete",
                    "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f"
                  }
                ]
              }
            }
        """.trimIndent()
        val setRequest = getMainModelFromJsonString(
            addressBookMetaModel,
            setRequestJson,
            multiAuxDecodingStateMachineFactory = multiAuxDecodingFactory
        )

        val expectedErrors = emptyList<String>()
        val actualErrors = populateSubTreeGranularityDeleteRequest(setRequest)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
        assertMatchesJsonString(setRequest, setRequestJson, EncodePasswords.ALL, multiAuxEncoder)
    }

    @Test
    fun `The operator must not populate field granularity create-requests`() {
        val setRequestJson = """
            {
              "address_book": {
                "person": [
                  {
                    "set_": "create",
                    "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f"
                  }
                ]
              }
            }
        """.trimIndent()
        val setRequest = getMainModelFromJsonString(
            addressBookMetaModel,
            setRequestJson,
            multiAuxDecodingStateMachineFactory = multiAuxDecodingFactory
        )

        val expectedErrors = emptyList<String>()
        val actualErrors = populateSubTreeGranularityDeleteRequest(setRequest)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
        assertMatchesJsonString(setRequest, setRequestJson, EncodePasswords.ALL, multiAuxEncoder)
    }

    @Test
    fun `The operator must not populate field granularity update-requests`() {
        val setRequestJson = """
            {
              "address_book": {
                "person": [
                  {
                    "set_": "update",
                    "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f"
                  }
                ]
              }
            }
        """.trimIndent()
        val setRequest = getMainModelFromJsonString(
            addressBookMetaModel,
            setRequestJson,
            multiAuxDecodingStateMachineFactory = multiAuxDecodingFactory
        )

        val expectedErrors = emptyList<String>()
        val actualErrors = populateSubTreeGranularityDeleteRequest(setRequest)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
        assertMatchesJsonString(setRequest, setRequestJson, EncodePasswords.ALL, multiAuxEncoder)
    }

    @Test
    fun `The operator must return errors if sub_tree granularity delete-requests contain sub-paths`() {
        val setRequestJson = """
            {
              "address_book": {
                "sub_tree_persons": [
                  {
                    "set_": "delete",
                    "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
                    "hero_details": {}
                  }
                ]
              }
            }
        """.trimIndent()
        val setRequest = getMainModelFromJsonString(
            addressBookMetaModel,
            setRequestJson,
            multiAuxDecodingStateMachineFactory = multiAuxDecodingFactory
        )

        val expectedErrors = listOf(
            "/address_book/sub_tree_persons/cc477201-48ec-4367-83a4-7fdbd92f8a6f: A delete-request must only specify the root of a sub-tree with sub_tree granularity"
        )
        val actualErrors = populateSubTreeGranularityDeleteRequest(setRequest)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
        assertMatchesJsonString(setRequest, setRequestJson, EncodePasswords.ALL, multiAuxEncoder)
    }

    @Test
    fun `The operator must not return errors if sub_tree granularity create-requests contain sub-paths`() {
        val setRequestJson = """
            {
              "address_book": {
                "sub_tree_persons": [
                  {
                    "set_": "create",
                    "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
                    "hero_details": {}
                  }
                ]
              }
            }
        """.trimIndent()
        val setRequest = getMainModelFromJsonString(
            addressBookMetaModel,
            setRequestJson,
            multiAuxDecodingStateMachineFactory = multiAuxDecodingFactory
        )

        val expectedErrors = emptyList<String>()
        val actualErrors = populateSubTreeGranularityDeleteRequest(setRequest)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
        assertMatchesJsonString(setRequest, setRequestJson, EncodePasswords.ALL, multiAuxEncoder)
    }

    @Test
    fun `The operator must not return errors if sub_tree granularity update-requests contain sub-paths`() {
        val setRequestJson = """
            {
              "address_book": {
                "sub_tree_persons": [
                  {
                    "set_": "update",
                    "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
                    "hero_details": {}
                  }
                ]
              }
            }
        """.trimIndent()
        val setRequest = getMainModelFromJsonString(
            addressBookMetaModel,
            setRequestJson,
            multiAuxDecodingStateMachineFactory = multiAuxDecodingFactory
        )

        val expectedErrors = emptyList<String>()
        val actualErrors = populateSubTreeGranularityDeleteRequest(setRequest)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
        assertMatchesJsonString(setRequest, setRequestJson, EncodePasswords.ALL, multiAuxEncoder)
    }
}