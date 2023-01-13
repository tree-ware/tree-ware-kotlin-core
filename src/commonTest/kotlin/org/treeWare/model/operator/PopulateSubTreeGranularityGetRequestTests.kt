package org.treeWare.model.operator

import org.treeWare.metaModel.addressBookMetaModel
import org.treeWare.model.assertMatchesJsonString
import org.treeWare.model.encoder.EncodePasswords
import org.treeWare.model.encoder.MultiAuxEncoder
import org.treeWare.model.getMainModelFromJsonString
import org.treeWare.model.operator.set.aux.SET_AUX_NAME
import org.treeWare.model.operator.set.aux.SetAuxEncoder
import kotlin.test.Test

private val multiAuxEncoder = MultiAuxEncoder(SET_AUX_NAME to SetAuxEncoder())

class PopulateSubTreeGranularityGetRequestTests {
    @Test
    fun `The operator must not populate sub_tree granularity get-requests if the sub-tree is partially populated`() {
        val getRequestJson = """
            {
              "address_book": {
                "sub_tree_persons": [
                  {
                    "id": null,
                    "first_name": null,
                    "last_name": null,
                    "relation": [
                      {
                        "id": null,
                        "relationship": null,
                        "person": null
                      }
                    ],
                    "is_hero": null,
                    "hero_details": {
                      "strengths": null,
                      "weaknesses": null
                    }
                  }
                ]
              }
            }
        """.trimIndent()
        val getRequest = getMainModelFromJsonString(addressBookMetaModel, getRequestJson)

        populateSubTreeGranularityGetRequest(getRequest)

        assertMatchesJsonString(getRequest, getRequestJson, EncodePasswords.ALL, multiAuxEncoder)
    }

    @Test
    fun `The operator must populate sub_tree granularity get-requests if the sub-tree is not populated`() {
        val getRequestJson = """
            {
              "address_book": {
                "sub_tree_persons": [
                  {
                    "id": null
                  }
                ]
              }
            }
        """.trimIndent()
        val getRequest = getMainModelFromJsonString(addressBookMetaModel, getRequestJson)

        populateSubTreeGranularityGetRequest(getRequest)

        val expectedSetRequestJson = """
            {
              "address_book": {
                "sub_tree_persons": [
                  {
                    "id": null,
                    "first_name": null,
                    "last_name": null,
                    "hero_name": null,
                    "email": [],
                    "picture": null,
                    "relation": [
                      {
                        "id": null,
                        "relationship": null,
                        "person": null
                      }
                    ],
                    "password": null,
                    "previous_passwords": [],
                    "main_secret": null,
                    "other_secrets": [],
                    "group": null,
                    "is_hero": null,
                    "hero_details": {
                      "strengths": null,
                      "weaknesses": null
                    }
                  }
                ]
              }
            }
        """.trimIndent()
        assertMatchesJsonString(getRequest, expectedSetRequestJson, EncodePasswords.ALL, multiAuxEncoder)
    }
}