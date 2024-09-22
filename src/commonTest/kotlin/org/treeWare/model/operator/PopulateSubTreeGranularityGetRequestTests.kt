package org.treeWare.model.operator

import org.treeWare.model.AddressBookMutableEntityModelFactory
import org.treeWare.model.assertMatchesJsonString
import org.treeWare.model.decodeJsonStringIntoEntity
import org.treeWare.model.encoder.EncodePasswords
import org.treeWare.model.encoder.MultiAuxEncoder
import org.treeWare.model.operator.set.aux.SET_AUX_NAME
import org.treeWare.model.operator.set.aux.SetAuxEncoder
import kotlin.test.Test

private val multiAuxEncoder = MultiAuxEncoder(SET_AUX_NAME to SetAuxEncoder())

class PopulateSubTreeGranularityGetRequestTests {
    @Test
    fun `The operator must not populate sub_tree granularity get-requests if the sub-tree is partially populated`() {
        val getRequestJson = """
            {
              "sub_tree_persons": [
                {
                  "id": null,
                  "first_name": null,
                  "last_name": null,
                  "relations": [
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
        """.trimIndent()
        val getRequest = AddressBookMutableEntityModelFactory.create()
        decodeJsonStringIntoEntity(getRequestJson, entity = getRequest)

        populateSubTreeGranularityGetRequest(getRequest)

        assertMatchesJsonString(getRequest, getRequestJson, EncodePasswords.ALL, multiAuxEncoder)
    }

    @Test
    fun `The operator must populate sub_tree granularity get-requests if the sub-tree is not populated`() {
        val getRequestJson = """
            {
              "sub_tree_persons": [
                {
                  "id": null
                }
              ]
            }
        """.trimIndent()
        val getRequest = AddressBookMutableEntityModelFactory.create()
        decodeJsonStringIntoEntity(getRequestJson, entity = getRequest)

        populateSubTreeGranularityGetRequest(getRequest)

        val expectedSetRequestJson = """
            {
              "sub_tree_persons": [
                {
                  "id": null,
                  "first_name": null,
                  "last_name": null,
                  "hero_name": null,
                  "picture": null,
                  "relations": [
                    {
                      "id": null,
                      "relationship": null,
                      "person": null
                    }
                  ],
                  "password": null,
                  "main_secret": null,
                  "group": null,
                  "is_hero": null,
                  "hero_details": {
                    "strengths": null,
                    "weaknesses": null
                  }
                }
              ]
            }
        """.trimIndent()
        assertMatchesJsonString(getRequest, expectedSetRequestJson, EncodePasswords.ALL, multiAuxEncoder)
    }
}