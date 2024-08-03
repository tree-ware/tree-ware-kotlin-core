package org.treeWare.model.operator

import org.treeWare.metaModel.addressBookMetaModel
import org.treeWare.model.AddressBookMutableMainModelFactory
import org.treeWare.model.assertMatchesJsonString
import org.treeWare.model.core.*
import org.treeWare.model.encoder.EncodePasswords
import org.treeWare.model.getMainModelFromJsonString
import org.treeWare.model.operator.rbac.FullyPermitted
import org.treeWare.model.operator.rbac.NotPermitted
import org.treeWare.model.operator.rbac.PartiallyPermitted
import org.treeWare.model.operator.rbac.aux.PermissionScope
import org.treeWare.model.operator.rbac.aux.PermissionsAux
import org.treeWare.model.operator.rbac.aux.setPermissionsAux
import org.treeWare.util.readFile
import kotlin.test.Test
import kotlin.test.assertIs

private const val CLARK_KENT_ID = "cc477201-48ec-4367-83a4-7fdbd92f8a6f"
private const val LOIS_LANE_ID = "a8aacf55-7810-4b43-afe5-4344f25435fd"

private val nonWildcardGetJson = readFile("model/address_book_1_main_model.json")
private val nonWildcardClarkKentGetJson = readFile("model/address_book_clark_kent.json")

private val wildcardGetJson = readFile("org/treeWare/model/operator/get_request_specific_and_wildcard_entities.json")
private val specificClarkKentWithWildcardChildrenGetJson =
    readFile("org/treeWare/model/operator/get_request_specific_clark_kent_with_wildcard_children.json")
private val specificClarkKentAndWildcardPersonGetJson =
    readFile("org/treeWare/model/operator/get_request_specific_clark_kent_and_wildcard_person.json")

class PermitGetTests {
    private fun testPermitGet(
        getJson: String,
        rbac: MainModel,
        expectedPermittedJson: String?,
        isFullyPermitted: Boolean
    ) {
        val getModel = getMainModelFromJsonString(addressBookMetaModel, getJson)
        val actual = permitGet(getModel, rbac, AddressBookMutableMainModelFactory)
        if (expectedPermittedJson == null) {
            when (actual) {
                is FullyPermitted -> assertMatchesJsonString(
                    actual.permitted,
                    "NotPermitted instead of FullyPermitted",
                    EncodePasswords.ALL
                )
                is PartiallyPermitted -> assertMatchesJsonString(
                    actual.permitted,
                    "NotPermitted instead of PartiallyPermitted",
                    EncodePasswords.ALL
                )
                NotPermitted -> {}
            }
        } else if (isFullyPermitted) {
            assertIs<FullyPermitted>(actual)
            assertMatchesJsonString(actual.permitted, expectedPermittedJson, EncodePasswords.ALL)
        } else {
            assertIs<PartiallyPermitted>(actual)
            assertMatchesJsonString(actual.permitted, expectedPermittedJson, EncodePasswords.ALL)
        }
    }

    // region Pruning

    @Test
    fun `Empty single-compositions in get-model are pruned by the permitGet operator`() {
        val singleCompositionGetJson = """
            |{
            |  "address_book": {
            |    "settings": {}
            |  }
            |}
        """.trimMargin()
        val rbac = newRootRbac(PermissionScope.SUB_TREE)
        testPermitGet(singleCompositionGetJson, rbac, expectedPermittedJson = null, false)
    }

    @Test
    fun `Get-model with only non-wildcard keys must not be pruned by the permitGet operator`() {
        val onlyNonWildcardKeysGetJson = """
            |{
            |  "address_book": {
            |    "person": [
            |      {
            |        "id": "$CLARK_KENT_ID"
            |      }
            |    ],
            |    "city_info": [
            |      {
            |        "city": {
            |          "name": "New York City",
            |          "state": "New York",
            |          "country": "United States of America"
            |        }
            |      }
            |    ]
            |  }
            |}
        """.trimMargin()
        val rbac = newRootRbac(PermissionScope.SUB_TREE)
        testPermitGet(onlyNonWildcardKeysGetJson, rbac, expectedPermittedJson = onlyNonWildcardKeysGetJson, true)
    }

    @Test
    fun `Get-model with only wildcard keys must not be pruned by the permitGet operator`() {
        val onlyWildcardKeysGetJson = """
            |{
            |  "address_book": {
            |    "person": [
            |      {
            |        "id": null
            |      }
            |    ],
            |    "city_info": [
            |      {
            |        "city": {
            |          "name": null,
            |          "state": null,
            |          "country": null
            |        }
            |      }
            |    ]
            |  }
            |}
        """.trimMargin()
        val rbac = newRootRbac(PermissionScope.SUB_TREE)
        testPermitGet(onlyWildcardKeysGetJson, rbac, expectedPermittedJson = onlyWildcardKeysGetJson, true)
    }

    // endregion

    // region No-level RBAC tests

    private fun newNoLevelRbac(): MainModel {
        val rbac = MutableMainModel(addressBookMetaModel)
        rbac.getOrNewRoot()
        return rbac
    }

    @Test
    fun `Fully matching non-wildcard get-model must not be permitted for no-level RBAC`() {
        val rbac = newNoLevelRbac()
        testPermitGet(nonWildcardGetJson, rbac, expectedPermittedJson = null, false)
    }

    @Test
    fun `Fully matching wildcard get-model must not be permitted for no-level RBAC`() {
        val rbac = newNoLevelRbac()
        testPermitGet(wildcardGetJson, rbac, expectedPermittedJson = null, false)
    }

    // endregion

    // region Root-level RBAC tests

    private fun newRootRbac(readScope: PermissionScope): MainModel {
        val rbac = MutableMainModel(addressBookMetaModel)
        setPermissionsAux(rbac, PermissionsAux(read = readScope))
        rbac.getOrNewRoot()
        return rbac
    }

    @Test
    fun `Fully matching non-wildcard get-model must be fully permitted for root-level sub-tree-scoped RBAC`() {
        val rbac = newRootRbac(PermissionScope.SUB_TREE)
        testPermitGet(nonWildcardGetJson, rbac, expectedPermittedJson = nonWildcardGetJson, true)
    }

    @Test
    fun `Fully matching wildcard get-model must be fully permitted for root-level sub-tree-scoped RBAC`() {
        val rbac = newRootRbac(PermissionScope.SUB_TREE)
        testPermitGet(wildcardGetJson, rbac, expectedPermittedJson = wildcardGetJson, true)
    }

    @Test
    fun `Fully matching non-wildcard get-model must not be permitted for root-level node-scoped RBAC`() {
        val rbac = newRootRbac(PermissionScope.NODE)
        testPermitGet(nonWildcardGetJson, rbac, expectedPermittedJson = null, false)
    }

    @Test
    fun `Fully matching wildcard get-model must not be permitted for root-level node-scoped RBAC`() {
        val rbac = newRootRbac(PermissionScope.NODE)
        testPermitGet(wildcardGetJson, rbac, expectedPermittedJson = null, false)
    }

    @Test
    fun `Fully matching non-wildcard get-model must not be permitted for root-level none-scoped RBAC`() {
        val rbac = newRootRbac(PermissionScope.NONE)
        testPermitGet(nonWildcardGetJson, rbac, expectedPermittedJson = null, false)
    }

    @Test
    fun `Fully matching wildcard get-model must not be permitted for root-level none-scoped RBAC`() {
        val rbac = newRootRbac(PermissionScope.NONE)
        testPermitGet(wildcardGetJson, rbac, expectedPermittedJson = null, false)
    }

    // NOTE: Partially matching & non-matching tests don't exist for root-level RBAC since everything is under the root.

    // endregion

    // region First-level RBAC tests

    private fun newPersonRbac(
        personId: String,
        personReadScope: PermissionScope,
        personsReadScope: PermissionScope? = null
    ): MainModel {
        val rbac = MutableMainModel(addressBookMetaModel)
        val rbacRoot = rbac.getOrNewRoot()
        val rbacPersons = getOrNewMutableSetField(rbacRoot, "person")
        personsReadScope?.also { setPermissionsAux(rbacPersons, PermissionsAux(read = it)) }
        val rbacPerson = getNewMutableSetEntity(rbacPersons)
        setUuidSingleField(rbacPerson, "id", personId)
        rbacPersons.addValue(rbacPerson)
        setPermissionsAux(rbacPerson, PermissionsAux(read = personReadScope))
        return rbac
    }

    @Test
    fun `Fully matching non-wildcard get-model must be fully permitted for first-level sub-tree-scoped RBAC`() {
        val rbac = newPersonRbac(CLARK_KENT_ID, PermissionScope.SUB_TREE)
        testPermitGet(nonWildcardClarkKentGetJson, rbac, expectedPermittedJson = nonWildcardClarkKentGetJson, true)
    }

    @Test
    fun `Fully matching wildcard get-model must be fully permitted for first-level sub-tree-scoped RBAC`() {
        // NOTE: wildcards in the get-model do not match explicit keys in the RBAC model.
        val rbac = newPersonRbac(CLARK_KENT_ID, PermissionScope.SUB_TREE)
        testPermitGet(
            specificClarkKentWithWildcardChildrenGetJson,
            rbac,
            expectedPermittedJson = specificClarkKentWithWildcardChildrenGetJson,
            true
        )
    }

    @Test
    fun `Partially matching non-wildcard get-model must be partially permitted for first-level sub-tree-scoped RBAC`() {
        val rbac = newPersonRbac(CLARK_KENT_ID, PermissionScope.SUB_TREE)
        testPermitGet(nonWildcardGetJson, rbac, expectedPermittedJson = nonWildcardClarkKentGetJson, false)
    }

    @Test
    fun `Partially matching wildcard get-model must be partially permitted for first-level sub-tree-scoped RBAC`() {
        val rbac = newPersonRbac(CLARK_KENT_ID, PermissionScope.SUB_TREE)
        testPermitGet(
            wildcardGetJson,
            rbac,
            expectedPermittedJson = specificClarkKentWithWildcardChildrenGetJson,
            false
        )
    }

    @Test
    fun `Non-matching non-wildcard get-model must not be permitted for first-level sub-tree-scoped RBAC`() {
        val rbac = newPersonRbac(LOIS_LANE_ID, PermissionScope.SUB_TREE)
        testPermitGet(nonWildcardClarkKentGetJson, rbac, expectedPermittedJson = null, false)
    }

    @Test
    fun `Non-matching wildcard get-model must not be permitted for first-level sub-tree-scoped RBAC`() {
        val rbac = newPersonRbac(LOIS_LANE_ID, PermissionScope.SUB_TREE)
        testPermitGet(specificClarkKentWithWildcardChildrenGetJson, rbac, expectedPermittedJson = null, false)
    }

    @Test
    fun `Partially matching non-wildcard get-model must be partially permitted for first-level none-scoped-inside-sub-tree-scoped RBAC`() {
        // Permit all persons except Lois Lane
        val rbac = newPersonRbac(LOIS_LANE_ID, PermissionScope.NONE, PermissionScope.SUB_TREE)
        testPermitGet(nonWildcardGetJson, rbac, expectedPermittedJson = nonWildcardClarkKentGetJson, false)
    }

    @Test
    fun `Partially matching wildcard get-model must be partially permitted for first-level none-scoped-inside-sub-tree-scoped RBAC`() {
        // Permit all persons except Lois Lane
        val rbac = newPersonRbac(LOIS_LANE_ID, PermissionScope.NONE, PermissionScope.SUB_TREE)
        testPermitGet(wildcardGetJson, rbac, expectedPermittedJson = specificClarkKentAndWildcardPersonGetJson, false)
    }

    // endregion

    // region Not yet supported

    @Test
    fun `Wildcards in the get-model currently do not match explicit keys in the RBAC model`() {
        // NOTE: wildcards in the get-model will eventually be replaced by explicit keys from the RBAC model.
        val getJson = """
            |{
            |  "address_book": {
            |    "person": [
            |      {
            |        "id": null,
            |        "last_name": null
            |      }
            |    ]
            |  }
            |}
        """.trimMargin()
        val rbac = newPersonRbac(CLARK_KENT_ID, PermissionScope.SUB_TREE)
        testPermitGet(getJson, rbac, expectedPermittedJson = null, false)
    }

    // endregion
}