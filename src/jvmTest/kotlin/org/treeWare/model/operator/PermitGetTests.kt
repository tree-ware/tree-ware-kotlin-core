package org.treeWare.model.operator

import org.treeWare.metaModel.addressBookMetaModel
import org.treeWare.model.assertMatchesJsonString
import org.treeWare.model.core.*
import org.treeWare.model.encoder.EncodePasswords
import org.treeWare.model.getMainModelFromJsonString
import org.treeWare.model.operator.rbac.aux.PERMISSIONS_AUX_NAME
import org.treeWare.model.operator.rbac.aux.PermissionScope
import org.treeWare.model.operator.rbac.aux.PermissionsAux
import org.treeWare.model.readFile
import kotlin.test.Test
import kotlin.test.assertNotNull

private const val CLARK_KENT_ID = "cc477201-48ec-4367-83a4-7fdbd92f8a6f"
private const val LOIS_LANE_ID = "a8aacf55-7810-4b43-afe5-4344f25435fd"

private val nonWildcardGetJson = readFile("model/address_book_1.json")
private val nonWildcardClarkKentGetJson = readFile("model/address_book_clark_kent.json")

private val wildcardGetJson = readFile("org/treeWare/model/operator/get_request_specific_and_wildcard_entities.json")
private val specificClarkKentWithWildcardChildrenGetJson =
    readFile("org/treeWare/model/operator/get_request_specific_clark_kent_with_wildcard_children.json")
private val specificClarkKentAndWildcardPersonGetJson =
    readFile("org/treeWare/model/operator/get_request_specific_clark_kent_and_wildcard_person.json")

class PermitGetTests {
    private fun testPermitGet(getJson: String, rbac: MainModel, expectedPermittedJson: String?) {
        val getModel = getMainModelFromJsonString(addressBookMetaModel, getJson)
        val actualPermitted = permitGet(getModel, rbac)
        if (expectedPermittedJson == null) {
            if (actualPermitted != null) assertMatchesJsonString(actualPermitted, "null", EncodePasswords.ALL)
        } else {
            assertNotNull(actualPermitted)
            assertMatchesJsonString(actualPermitted, expectedPermittedJson, EncodePasswords.ALL)
        }
    }

    // region No-level RBAC tests

    private fun newNoLevelRbac(): MainModel {
        val rbac = MutableMainModel(addressBookMetaModel)
        rbac.getOrNewRoot()
        return rbac
    }

    @Test
    fun `Fully matching non-wildcard get-model must not be permitted for no-level RBAC`() {
        val rbac = newNoLevelRbac()
        val expectedPermittedJson = null
        testPermitGet(nonWildcardGetJson, rbac, expectedPermittedJson)
    }

    @Test
    fun `Fully matching wildcard get-model must not be permitted for no-level RBAC`() {
        val rbac = newNoLevelRbac()
        val expectedPermittedJson = null
        testPermitGet(wildcardGetJson, rbac, expectedPermittedJson)
    }

    // endregion

    // region Root-level RBAC tests

    private fun newRootRbac(readScope: PermissionScope): MainModel {
        val rbac = MutableMainModel(addressBookMetaModel)
        rbac.setAux(PERMISSIONS_AUX_NAME, PermissionsAux(read = readScope))
        rbac.getOrNewRoot()
        return rbac
    }

    @Test
    fun `Fully matching non-wildcard get-model must be fully permitted for root-level sub-tree-scoped RBAC`() {
        val rbac = newRootRbac(PermissionScope.SUB_TREE)
        testPermitGet(nonWildcardGetJson, rbac, nonWildcardGetJson)
    }

    @Test
    fun `Fully matching wildcard get-model must be fully permitted for root-level sub-tree-scoped RBAC`() {
        val rbac = newRootRbac(PermissionScope.SUB_TREE)
        testPermitGet(wildcardGetJson, rbac, wildcardGetJson)
    }

    @Test
    fun `Only root of fully matching non-wildcard get-model must be permitted for root-level node-scoped RBAC`() {
        val rbac = newRootRbac(PermissionScope.NODE)

        val expectedPermittedJson = """
            |{
            |  "address_book": {}
            |}
        """.trimMargin()

        testPermitGet(nonWildcardGetJson, rbac, expectedPermittedJson)
    }

    @Test
    fun `Only root of fully matching wildcard get-model must be permitted for root-level node-scoped RBAC`() {
        val rbac = newRootRbac(PermissionScope.NODE)

        val expectedPermittedJson = """
            |{
            |  "address_book": {}
            |}
        """.trimMargin()

        testPermitGet(wildcardGetJson, rbac, expectedPermittedJson)
    }

    @Test
    fun `Fully matching non-wildcard get-model must not be permitted for root-level none-scoped RBAC`() {
        val rbac = newRootRbac(PermissionScope.NONE)
        val expectedPermittedJson = null
        testPermitGet(nonWildcardGetJson, rbac, expectedPermittedJson)
    }

    @Test
    fun `Fully matching wildcard get-model must not be permitted for root-level none-scoped RBAC`() {
        val rbac = newRootRbac(PermissionScope.NONE)
        val expectedPermittedJson = null
        testPermitGet(wildcardGetJson, rbac, expectedPermittedJson)
    }

    // NOTE: Partially matching & non-matching tests don't exist for root-level RBAC since everything is under the root.

    // endregion

    // region First-level RBAC tests

    private fun newPersonRbac(
        personId: String,
        personReadScope: PermissionScope,
        personsReadScope: PermissionScope = PermissionScope.NODE
    ): MainModel {
        val rbac = MutableMainModel(addressBookMetaModel)
        rbac.setAux(PERMISSIONS_AUX_NAME, PermissionsAux(read = PermissionScope.NODE))
        val rbacRoot = rbac.getOrNewRoot()
        val rbacPersons = getOrNewMutableSetField(rbacRoot, "person")
        rbacPersons.setAux(PERMISSIONS_AUX_NAME, PermissionsAux(read = personsReadScope))
        val rbacPerson = getNewMutableSetEntity(rbacPersons)
        setUuidSingleField(rbacPerson, "id", personId)
        rbacPersons.addValue(rbacPerson)
        rbacPerson.setAux(PERMISSIONS_AUX_NAME, PermissionsAux(read = personReadScope))
        return rbac
    }

    @Test
    fun `Fully matching non-wildcard get-model must be fully permitted for first-level sub-tree-scoped RBAC`() {
        val rbac = newPersonRbac(CLARK_KENT_ID, PermissionScope.SUB_TREE)
        testPermitGet(nonWildcardClarkKentGetJson, rbac, nonWildcardClarkKentGetJson)
    }

    @Test
    fun `Fully matching wildcard get-model must be fully permitted for first-level sub-tree-scoped RBAC`() {
        // NOTE: wildcards in the get-model do not match explicit keys in the RBAC model.
        val rbac = newPersonRbac(CLARK_KENT_ID, PermissionScope.SUB_TREE)
        testPermitGet(specificClarkKentWithWildcardChildrenGetJson, rbac, specificClarkKentWithWildcardChildrenGetJson)
    }

    @Test
    fun `Partially matching non-wildcard get-model must be partially permitted for first-level sub-tree-scoped RBAC`() {
        val rbac = newPersonRbac(CLARK_KENT_ID, PermissionScope.SUB_TREE)
        testPermitGet(nonWildcardGetJson, rbac, nonWildcardClarkKentGetJson)
    }

    @Test
    fun `Partially matching wildcard get-model must be partially permitted for first-level sub-tree-scoped RBAC`() {
        val rbac = newPersonRbac(CLARK_KENT_ID, PermissionScope.SUB_TREE)
        testPermitGet(wildcardGetJson, rbac, specificClarkKentWithWildcardChildrenGetJson)
    }

    @Test
    fun `Non-matching non-wildcard get-model must not be permitted for first-level sub-tree-scoped RBAC`() {
        val rbac = newPersonRbac(LOIS_LANE_ID, PermissionScope.SUB_TREE)
        // TODO(cleanup) enhance the permitGet operator to make the following `null`
        val expectedPermittedJson = """
            |{
            |  "address_book": {
            |    "person": []
            |  }
            |}
        """.trimMargin()
        testPermitGet(nonWildcardClarkKentGetJson, rbac, expectedPermittedJson)
    }

    @Test
    fun `Non-matching wildcard get-model must not be permitted for first-level sub-tree-scoped RBAC`() {
        val rbac = newPersonRbac(LOIS_LANE_ID, PermissionScope.SUB_TREE)
        // TODO(cleanup) enhance the permitGet operator to make the following `null`
        val expectedPermittedJson = """
            |{
            |  "address_book": {
            |    "person": []
            |  }
            |}
        """.trimMargin()
        testPermitGet(specificClarkKentWithWildcardChildrenGetJson, rbac, expectedPermittedJson)
    }

    @Test
    fun `Partially matching non-wildcard get-model must be partially permitted for first-level none-scoped RBAC`() {
        // Permit all persons except Lois Lane
        val rbac = newPersonRbac(LOIS_LANE_ID, PermissionScope.NONE, PermissionScope.SUB_TREE)
        testPermitGet(nonWildcardGetJson, rbac, nonWildcardClarkKentGetJson)
    }

    @Test
    fun `Partially matching wildcard get-model must be partially permitted for first-level none-scoped RBAC`() {
        // Permit all persons except Lois Lane
        val rbac = newPersonRbac(LOIS_LANE_ID, PermissionScope.NONE, PermissionScope.SUB_TREE)
        testPermitGet(wildcardGetJson, rbac, specificClarkKentAndWildcardPersonGetJson)
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
        // TODO(cleanup) enhance the permitGet operator to make the following `null`
        val expectedPermittedJson = """
            |{
            |  "address_book": {
            |    "person": []
            |  }
            |}
        """.trimMargin()
        testPermitGet(getJson, rbac, expectedPermittedJson)
    }

    // endregion
}