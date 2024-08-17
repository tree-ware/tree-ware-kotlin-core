package org.treeWare.model.operator

import org.treeWare.model.AddressBookMutableEntityModelFactory
import org.treeWare.model.addCity
import org.treeWare.model.assertMatchesJsonString
import org.treeWare.model.core.*
import org.treeWare.model.decodeJsonStringIntoEntity
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import org.treeWare.model.encoder.EncodePasswords
import org.treeWare.model.encoder.MultiAuxEncoder
import org.treeWare.model.operator.rbac.FullyPermitted
import org.treeWare.model.operator.rbac.NotPermitted
import org.treeWare.model.operator.rbac.PartiallyPermitted
import org.treeWare.model.operator.rbac.aux.PermissionScope
import org.treeWare.model.operator.rbac.aux.PermissionsAux
import org.treeWare.model.operator.rbac.aux.setPermissionsAux
import org.treeWare.model.operator.set.aux.SET_AUX_NAME
import org.treeWare.model.operator.set.aux.SetAux
import org.treeWare.model.operator.set.aux.SetAuxEncoder
import org.treeWare.model.operator.set.aux.SetAuxStateMachine
import org.treeWare.util.readFile
import kotlin.test.Test
import kotlin.test.assertIs

private const val CLARK_KENT_ID = "cc477201-48ec-4367-83a4-7fdbd92f8a6f"
private const val LOIS_LANE_ID = "a8aacf55-7810-4b43-afe5-4344f25435fd"

private val mixedSetJson = readFile("org/treeWare/model/operator/permitSet/set_request_mixed.json")
private val mixedClarkKentSetJson = readFile("org/treeWare/model/operator/permitSet/set_request_mixed_clark_kent.json")

private val multiAuxDecodingFactory = MultiAuxDecodingStateMachineFactory(SET_AUX_NAME to { SetAuxStateMachine(it) })
private val multiAuxEncoder = MultiAuxEncoder(SET_AUX_NAME to SetAuxEncoder())

class PermitSetTests {
    private fun testPermitSet(
        setJson: String,
        rbac: EntityModel,
        expectedPermittedJson: String?,
        isFullyPermitted: Boolean
    ) {
        val setModel = AddressBookMutableEntityModelFactory.create()
        decodeJsonStringIntoEntity(
            setJson,
            multiAuxDecodingStateMachineFactory = multiAuxDecodingFactory,
            entity = setModel
        )
        assertMatchesJsonString(setModel, setJson, EncodePasswords.ALL, multiAuxEncoder)
        val actual = permitSet(setModel, rbac, AddressBookMutableEntityModelFactory)
        if (expectedPermittedJson == null) {
            when (actual) {
                is FullyPermitted -> assertMatchesJsonString(
                    actual.permitted,
                    "NotPermitted instead of FullyPermitted",
                    EncodePasswords.ALL,
                    multiAuxEncoder
                )
                is PartiallyPermitted -> assertMatchesJsonString(
                    actual.permitted,
                    "NotPermitted instead of PartiallyPermitted",
                    EncodePasswords.ALL,
                    multiAuxEncoder
                )
                NotPermitted -> {}
            }
        } else if (isFullyPermitted) {
            assertIs<FullyPermitted>(actual)
            assertMatchesJsonString(actual.permitted, expectedPermittedJson, EncodePasswords.ALL, multiAuxEncoder)
        } else {
            assertIs<PartiallyPermitted>(actual)
            assertMatchesJsonString(actual.permitted, expectedPermittedJson, EncodePasswords.ALL, multiAuxEncoder)
        }
    }

    // region Pruning

    @Test
    fun `Empty single-compositions in set-model will be pruned by the permitSet operator`() {
        val singleCompositionSetJson = """
            |{
            |  "settings": {}
            |}
        """.trimMargin()
        val rbac = newRootRbac(PermissionsAux(crud = PermissionScope.SUB_TREE))
        testPermitSet(singleCompositionSetJson, rbac, expectedPermittedJson = null, false)
    }

    @Test
    fun `UPDATE entities with only key fields in set-model will be pruned by the permitSet operator`() {
        // Since keys cannot be updated and there is nothing else in the entity to be updated.
        val updateEntitiesSetJson = """
            |{
            |  "person": [
            |    {
            |      "set_": "update",
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f"
            |    }
            |  ],
            |  "city_info": [
            |    {
            |      "set_": "update",
            |      "city": {
            |        "name": "New York City",
            |        "state": "New York",
            |        "country": "United States of America"
            |      }
            |    }
            |  ]
            |}
        """.trimMargin()
        val rbac = newRootRbac(PermissionsAux(crud = PermissionScope.SUB_TREE))
        testPermitSet(updateEntitiesSetJson, rbac, expectedPermittedJson = null, false)
    }

    @Test
    fun `CREATE entities with only key fields in set-model must not be pruned by the permitSet operator`() {
        val createEntitiesSetJson = """
            |{
            |  "person": [
            |    {
            |      "set_": "create",
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f"
            |    }
            |  ],
            |  "city_info": [
            |    {
            |      "set_": "create",
            |      "city": {
            |        "name": "New York City",
            |        "state": "New York",
            |        "country": "United States of America"
            |      }
            |    }
            |  ]
            |}
        """.trimMargin()
        val rbac = newRootRbac(PermissionsAux(crud = PermissionScope.SUB_TREE))
        testPermitSet(createEntitiesSetJson, rbac, expectedPermittedJson = createEntitiesSetJson, true)
    }

    @Test
    fun `DELETE entities with only key fields in set-model must not be pruned by the permitSet operator`() {
        val deleteEntitiesSetJson = """
            |{
            |  "person": [
            |    {
            |      "set_": "delete",
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f"
            |    }
            |  ],
            |  "city_info": [
            |    {
            |      "set_": "delete",
            |      "city": {
            |        "name": "New York City",
            |        "state": "New York",
            |        "country": "United States of America"
            |      }
            |    }
            |  ]
            |}
        """.trimMargin()
        val rbac = newRootRbac(PermissionsAux(crud = PermissionScope.SUB_TREE))
        testPermitSet(deleteEntitiesSetJson, rbac, expectedPermittedJson = deleteEntitiesSetJson, true)
    }

    @Test
    fun `DELETE keyless entities without any fields in set-model must not be pruned by the permitSet operator`() {
        val deleteEntitiesSetJson = """
            |{
            |  "person": [
            |    {
            |      "set_": "delete",
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "hero_details": {
            |        "set_": "delete"
            |      }
            |    }
            |  ]
            |}
        """.trimMargin()
        val rbac = newRootRbac(PermissionsAux(crud = PermissionScope.SUB_TREE))
        testPermitSet(deleteEntitiesSetJson, rbac, expectedPermittedJson = deleteEntitiesSetJson, true)
    }

    // endregion

    // region No-level RBAC tests

    private fun newNoLevelRbac(): EntityModel {
        val rbac = AddressBookMutableEntityModelFactory.create()
        return rbac
    }

    @Test
    fun `Fully matching set-model must not be permitted for no-level RBAC`() {
        val rbac = newNoLevelRbac()
        testPermitSet(mixedSetJson, rbac, expectedPermittedJson = null, false)
    }

    // endregion

    // region Root-level RBAC tests

    private fun newRootRbac(permissions: PermissionsAux): EntityModel {
        val rbac = AddressBookMutableEntityModelFactory.create()
        setPermissionsAux(rbac, permissions)
        return rbac
    }

    // region Root-level RBAC tests ALL permissions

    @Test
    fun `Fully matching set-model must be fully permitted for root-level sub-tree-scoped ALL RBAC`() {
        val rbac = newRootRbac(PermissionsAux(all = PermissionScope.SUB_TREE))
        testPermitSet(mixedSetJson, rbac, expectedPermittedJson = mixedSetJson, true)
    }

    @Test
    fun `Fully matching set-model must not be permitted for root-level node-scoped ALL RBAC`() {
        val rbac = newRootRbac(PermissionsAux(all = PermissionScope.NODE))
        testPermitSet(mixedSetJson, rbac, expectedPermittedJson = null, false)
    }

    @Test
    fun `Fully matching set-model must not be permitted for root-level none-scoped ALL RBAC`() {
        val rbac = newRootRbac(PermissionsAux(all = PermissionScope.NONE))
        testPermitSet(mixedSetJson, rbac, expectedPermittedJson = null, false)
    }

    // endregion

    // region Root-level RBAC tests CRUD permissions

    @Test
    fun `Fully matching set-model must be fully permitted for root-level sub-tree-scoped CRUD RBAC`() {
        val rbac = newRootRbac(PermissionsAux(crud = PermissionScope.SUB_TREE))
        testPermitSet(mixedSetJson, rbac, expectedPermittedJson = mixedSetJson, true)
    }

    @Test
    fun `Fully matching set-model must not be permitted for root-level node-scoped CRUD RBAC`() {
        val rbac = newRootRbac(PermissionsAux(crud = PermissionScope.NODE))
        testPermitSet(mixedSetJson, rbac, expectedPermittedJson = null, false)
    }

    @Test
    fun `Fully matching set-model must not be permitted for root-level none-scoped CRUD RBAC`() {
        val rbac = newRootRbac(PermissionsAux(crud = PermissionScope.NONE))
        testPermitSet(mixedSetJson, rbac, expectedPermittedJson = null, false)
    }

    // endregion

    // region Root-level RBAC tests CREATE permissions

    @Test
    fun `CREATE parts of fully matching set-model must be permitted for root-level sub-tree-scoped CREATE RBAC`() {
        val rbac = newRootRbac(PermissionsAux(create = PermissionScope.SUB_TREE))
        val expectedPermittedJson =
            readFile("org/treeWare/model/operator/permitSet/set_request_mixed_with_create_only.json")
        testPermitSet(mixedSetJson, rbac, expectedPermittedJson, false)
    }

    @Test
    fun `Fully matching set-model must not be permitted for root-level node-scoped CREATE RBAC`() {
        val rbac = newRootRbac(PermissionsAux(create = PermissionScope.NODE))
        testPermitSet(mixedSetJson, rbac, expectedPermittedJson = null, false)
    }

    @Test
    fun `Fully matching set-model must not be permitted for root-level none-scoped CREATE RBAC`() {
        val rbac = newRootRbac(PermissionsAux(create = PermissionScope.NONE))
        testPermitSet(mixedSetJson, rbac, expectedPermittedJson = null, false)
    }

    // endregion

    // region Root-level RBAC tests UPDATE permissions

    @Test
    fun `UPDATE parts of fully matching set-model must be permitted for root-level sub-tree-scoped UPDATE RBAC`() {
        val rbac = newRootRbac(PermissionsAux(update = PermissionScope.SUB_TREE))
        val expectedPermittedJson =
            readFile("org/treeWare/model/operator/permitSet/set_request_mixed_with_update_only.json")
        testPermitSet(mixedSetJson, rbac, expectedPermittedJson, false)
    }

    @Test
    fun `Fully matching set-model must not be permitted for root-level node-scoped UPDATE RBAC`() {
        val rbac = newRootRbac(PermissionsAux(update = PermissionScope.NODE))
        testPermitSet(mixedSetJson, rbac, expectedPermittedJson = null, false)
    }

    @Test
    fun `Fully matching set-model must not be permitted for root-level none-scoped UPDATE RBAC`() {
        val rbac = newRootRbac(PermissionsAux(update = PermissionScope.NONE))
        testPermitSet(mixedSetJson, rbac, expectedPermittedJson = null, false)
    }

    // endregion

    // region Root-level RBAC tests DELETE permissions

    @Test
    fun `DELETE parts of fully matching set-model must be permitted for root-level sub-tree-scoped DELETE RBAC`() {
        val rbac = newRootRbac(PermissionsAux(delete = PermissionScope.SUB_TREE))
        val expectedPermittedJson =
            readFile("org/treeWare/model/operator/permitSet/set_request_mixed_with_delete_only.json")
        testPermitSet(mixedSetJson, rbac, expectedPermittedJson, false)
    }

    @Test
    fun `Fully matching set-model must not be permitted for root-level node-scoped DELETE RBAC`() {
        val rbac = newRootRbac(PermissionsAux(delete = PermissionScope.NODE))
        testPermitSet(mixedSetJson, rbac, expectedPermittedJson = null, false)
    }

    @Test
    fun `Fully matching set-model must not be permitted for root-level none-scoped DELETE RBAC`() {
        val rbac = newRootRbac(PermissionsAux(delete = PermissionScope.NONE))
        testPermitSet(mixedSetJson, rbac, expectedPermittedJson = null, false)
    }

    // endregion

    // region Root-level RBAC tests CREATE & UPDATE permissions

    @Test
    fun `CREATE & UPDATE parts of fully matching set-model must be permitted for root-level sub-tree-scoped CREATE & UPDATE RBAC`() {
        val rbac = newRootRbac(PermissionsAux(create = PermissionScope.SUB_TREE, update = PermissionScope.SUB_TREE))
        val expectedPermittedJson =
            readFile("org/treeWare/model/operator/permitSet/set_request_mixed_with_create_and_update_only.json")
        testPermitSet(mixedSetJson, rbac, expectedPermittedJson, false)
    }

    @Test
    fun `Fully matching set-model must not be permitted for root-level node-scoped CREATE & UPDATE RBAC`() {
        val rbac = newRootRbac(PermissionsAux(create = PermissionScope.NODE, update = PermissionScope.NODE))
        testPermitSet(mixedSetJson, rbac, expectedPermittedJson = null, false)
    }

    @Test
    fun `Fully matching set-model must not be permitted for root-level none-scoped CREATE & UPDATE RBAC`() {
        val rbac = newRootRbac(PermissionsAux(create = PermissionScope.NONE, update = PermissionScope.NONE))
        testPermitSet(mixedSetJson, rbac, expectedPermittedJson = null, false)
    }

    // endregion

    // endregion

    // region First-level RBAC tests

    private fun newPersonRbac(
        personId: String,
        personPermissions: PermissionsAux,
        personsPermissions: PermissionsAux? = null
    ): EntityModel {
        val rbac = AddressBookMutableEntityModelFactory.create()
        val rbacPersons = getOrNewMutableSetField(rbac, "person")
        personsPermissions?.also { setPermissionsAux(rbacPersons, it) }
        val rbacPerson = getNewMutableSetEntity(rbacPersons)
        setUuidSingleField(rbacPerson, "id", personId)
        rbacPersons.addValue(rbacPerson)
        setPermissionsAux(rbacPerson, personPermissions)
        return rbac
    }

    // region First-level RBAC tests ALL permissions

    @Test
    fun `Fully matching set-model must be fully permitted for first-level sub-tree-scoped ALL RBAC`() {
        val rbac = newPersonRbac(CLARK_KENT_ID, PermissionsAux(all = PermissionScope.SUB_TREE))
        testPermitSet(mixedClarkKentSetJson, rbac, expectedPermittedJson = mixedClarkKentSetJson, true)
    }

    @Test
    fun `Partially matching set-model must be partially permitted for first-level sub-tree-scoped ALL RBAC`() {
        val rbac = newPersonRbac(CLARK_KENT_ID, PermissionsAux(all = PermissionScope.SUB_TREE))
        testPermitSet(mixedSetJson, rbac, expectedPermittedJson = mixedClarkKentSetJson, false)
    }

    @Test
    fun `Non-matching set-model must not be permitted for first-level sub-tree-scoped ALL RBAC`() {
        val rbac = newPersonRbac(LOIS_LANE_ID, PermissionsAux(all = PermissionScope.SUB_TREE))
        testPermitSet(mixedClarkKentSetJson, rbac, expectedPermittedJson = null, false)
    }

    @Test
    fun `Fully matching set-model must not be permitted for first-level node-scoped ALL RBAC`() {
        val rbac = newPersonRbac(CLARK_KENT_ID, PermissionsAux(all = PermissionScope.NODE))
        testPermitSet(mixedClarkKentSetJson, rbac, expectedPermittedJson = null, false)
    }

    @Test
    fun `Fully matching set-model must not be permitted for first-level none-scoped ALL RBAC`() {
        val rbac = newPersonRbac(CLARK_KENT_ID, PermissionsAux(all = PermissionScope.NONE))
        testPermitSet(mixedClarkKentSetJson, rbac, expectedPermittedJson = null, false)
    }

    @Test
    fun `Partially matching set-model must be partially permitted for first-level none-scoped-inside-sub-tree-scoped ALL RBAC`() {
        // Permit all persons except Lois Lane
        val rbac = newPersonRbac(
            LOIS_LANE_ID,
            PermissionsAux(all = PermissionScope.NONE),
            PermissionsAux(all = PermissionScope.SUB_TREE)
        )
        val expectedPermittedJson =
            readFile("org/treeWare/model/operator/permitSet/set_request_mixed_persons_other_than_lois_lane.json")
        testPermitSet(mixedSetJson, rbac, expectedPermittedJson, false)
    }

    // endregion

    // region First-level RBAC tests CRUD permissions

    @Test
    fun `Fully matching set-model must be fully permitted for first-level sub-tree-scoped CRUD RBAC`() {
        val rbac = newPersonRbac(CLARK_KENT_ID, PermissionsAux(crud = PermissionScope.SUB_TREE))
        testPermitSet(mixedClarkKentSetJson, rbac, expectedPermittedJson = mixedClarkKentSetJson, true)
    }

    @Test
    fun `Partially matching set-model must be partially permitted for first-level sub-tree-scoped CRUD RBAC`() {
        val rbac = newPersonRbac(CLARK_KENT_ID, PermissionsAux(crud = PermissionScope.SUB_TREE))
        testPermitSet(mixedSetJson, rbac, expectedPermittedJson = mixedClarkKentSetJson, false)
    }

    @Test
    fun `Non-matching set-model must not be permitted for first-level sub-tree-scoped CRUD RBAC`() {
        val rbac = newPersonRbac(LOIS_LANE_ID, PermissionsAux(crud = PermissionScope.SUB_TREE))
        testPermitSet(mixedClarkKentSetJson, rbac, expectedPermittedJson = null, false)
    }

    @Test
    fun `Fully matching set-model must not be permitted for first-level node-scoped CRUD RBAC`() {
        val rbac = newPersonRbac(CLARK_KENT_ID, PermissionsAux(crud = PermissionScope.NODE))
        testPermitSet(mixedClarkKentSetJson, rbac, expectedPermittedJson = null, false)
    }

    @Test
    fun `Fully matching set-model must not be permitted for first-level none-scoped CRUD RBAC`() {
        val rbac = newPersonRbac(CLARK_KENT_ID, PermissionsAux(crud = PermissionScope.NONE))
        testPermitSet(mixedClarkKentSetJson, rbac, expectedPermittedJson = null, false)
    }

    @Test
    fun `Partially matching set-model must be partially permitted for first-level none-scoped-inside-sub-tree-scoped CRUD RBAC`() {
        // Permit all persons except Lois Lane
        val rbac = newPersonRbac(
            LOIS_LANE_ID,
            PermissionsAux(crud = PermissionScope.NONE),
            PermissionsAux(crud = PermissionScope.SUB_TREE)
        )
        val expectedPermittedJson =
            readFile("org/treeWare/model/operator/permitSet/set_request_mixed_persons_other_than_lois_lane.json")
        testPermitSet(mixedSetJson, rbac, expectedPermittedJson, false)
    }

    // endregion

    // region First-level RBAC tests CREATE permissions

    @Test
    fun `Fully matching mixed set-model must be partially permitted for first-level sub-tree-scoped CREATE RBAC`() {
        val rbac = newPersonRbac(CLARK_KENT_ID, PermissionsAux(create = PermissionScope.SUB_TREE))
        val expectedPermittedJson =
            readFile("org/treeWare/model/operator/permitSet/set_request_mixed_clark_kent_with_create_only.json")
        testPermitSet(mixedClarkKentSetJson, rbac, expectedPermittedJson, false)
    }

    @Test
    fun `Fully matching CREATE set-model must be fully permitted for first-level sub-tree-scoped CREATE RBAC`() {
        val rbac = newPersonRbac(CLARK_KENT_ID, PermissionsAux(create = PermissionScope.SUB_TREE))
        val expectedPermittedJson =
            readFile("org/treeWare/model/operator/permitSet/set_request_mixed_clark_kent_with_create_only.json")
        testPermitSet(expectedPermittedJson, rbac, expectedPermittedJson, true)
    }

    @Test
    fun `Partially matching set-model must be partially permitted for first-level sub-tree-scoped CREATE RBAC`() {
        val rbac = newPersonRbac(CLARK_KENT_ID, PermissionsAux(create = PermissionScope.SUB_TREE))
        val expectedPermittedJson =
            readFile("org/treeWare/model/operator/permitSet/set_request_mixed_clark_kent_with_create_only.json")
        testPermitSet(mixedSetJson, rbac, expectedPermittedJson, false)
    }

    @Test
    fun `Non-matching set-model must not be permitted for first-level sub-tree-scoped CREATE RBAC`() {
        val rbac = newPersonRbac(LOIS_LANE_ID, PermissionsAux(create = PermissionScope.SUB_TREE))
        testPermitSet(mixedClarkKentSetJson, rbac, expectedPermittedJson = null, false)
    }

    @Test
    fun `Fully matching set-model must not be permitted for first-level node-scoped CREATE RBAC`() {
        val rbac = newPersonRbac(CLARK_KENT_ID, PermissionsAux(create = PermissionScope.NODE))
        testPermitSet(mixedClarkKentSetJson, rbac, expectedPermittedJson = null, false)
    }

    @Test
    fun `Fully matching set-model must not be permitted for first-level none-scoped CREATE RBAC`() {
        val rbac = newPersonRbac(CLARK_KENT_ID, PermissionsAux(create = PermissionScope.NONE))
        testPermitSet(mixedClarkKentSetJson, rbac, expectedPermittedJson = null, false)
    }

    @Test
    fun `Partially matching set-model must be partially permitted for first-level none-scoped-inside-sub-tree-scoped CREATE RBAC`() {
        // Permit all persons except Lois Lane
        val rbac = newPersonRbac(
            LOIS_LANE_ID,
            PermissionsAux(create = PermissionScope.NONE),
            PermissionsAux(create = PermissionScope.SUB_TREE)
        )
        val expectedPermittedJson =
            readFile("org/treeWare/model/operator/permitSet/set_request_mixed_clark_kent_with_create_only.json")
        testPermitSet(mixedSetJson, rbac, expectedPermittedJson, false)
    }

    // endregion

    // region First-level RBAC tests UPDATE permissions

    @Test
    fun `Fully matching mixed set-model must be partially permitted for first-level sub-tree-scoped UPDATE RBAC`() {
        val rbac = newPersonRbac(CLARK_KENT_ID, PermissionsAux(update = PermissionScope.SUB_TREE))
        val expectedPermittedJson =
            readFile("org/treeWare/model/operator/permitSet/set_request_mixed_clark_kent_with_update_only.json")
        testPermitSet(mixedClarkKentSetJson, rbac, expectedPermittedJson, false)
    }

    @Test
    fun `Fully matching UPDATE set-model must be fully permitted for first-level sub-tree-scoped UPDATE RBAC`() {
        val rbac = newPersonRbac(CLARK_KENT_ID, PermissionsAux(update = PermissionScope.SUB_TREE))
        val expectedPermittedJson =
            readFile("org/treeWare/model/operator/permitSet/set_request_mixed_clark_kent_with_update_only.json")
        testPermitSet(expectedPermittedJson, rbac, expectedPermittedJson, true)
    }

    @Test
    fun `Partially matching set-model must be partially permitted for first-level sub-tree-scoped UPDATE RBAC`() {
        val rbac = newPersonRbac(CLARK_KENT_ID, PermissionsAux(update = PermissionScope.SUB_TREE))
        val expectedPermittedJson =
            readFile("org/treeWare/model/operator/permitSet/set_request_mixed_clark_kent_with_update_only.json")
        testPermitSet(mixedSetJson, rbac, expectedPermittedJson, false)
    }

    @Test
    fun `Non-matching set-model must not be permitted for first-level sub-tree-scoped UPDATE RBAC`() {
        val rbac = newPersonRbac(LOIS_LANE_ID, PermissionsAux(update = PermissionScope.SUB_TREE))
        testPermitSet(mixedClarkKentSetJson, rbac, expectedPermittedJson = null, false)
    }

    @Test
    fun `Fully matching set-model must not be permitted for first-level node-scoped UPDATE RBAC`() {
        val rbac = newPersonRbac(CLARK_KENT_ID, PermissionsAux(update = PermissionScope.NODE))
        testPermitSet(mixedClarkKentSetJson, rbac, expectedPermittedJson = null, false)
    }

    @Test
    fun `Fully matching set-model must not be permitted for first-level none-scoped UPDATE RBAC`() {
        val rbac = newPersonRbac(CLARK_KENT_ID, PermissionsAux(update = PermissionScope.NONE))
        testPermitSet(mixedClarkKentSetJson, rbac, expectedPermittedJson = null, false)
    }

    @Test
    fun `Partially matching set-model must be partially permitted for first-level none-scoped-inside-sub-tree-scoped UPDATE RBAC`() {
        // Permit all persons except Lois Lane
        val rbac = newPersonRbac(
            LOIS_LANE_ID,
            PermissionsAux(update = PermissionScope.NONE),
            PermissionsAux(update = PermissionScope.SUB_TREE)
        )
        val expectedPermittedJson =
            readFile("org/treeWare/model/operator/permitSet/set_request_mixed_clark_kent_with_update_only.json")
        testPermitSet(mixedSetJson, rbac, expectedPermittedJson, false)
    }

    // endregion

    // region First-level RBAC tests DELETE permissions

    @Test
    fun `Fully matching mixed set-model must be partially permitted for first-level sub-tree-scoped DELETE RBAC`() {
        val rbac = newPersonRbac(CLARK_KENT_ID, PermissionsAux(delete = PermissionScope.SUB_TREE))
        val expectedPermittedJson =
            readFile("org/treeWare/model/operator/permitSet/set_request_mixed_clark_kent_with_delete_only.json")
        testPermitSet(mixedClarkKentSetJson, rbac, expectedPermittedJson, false)
    }

    @Test
    fun `Fully matching DELETE set-model must be fully permitted for first-level sub-tree-scoped DELETE RBAC`() {
        val rbac = newPersonRbac(CLARK_KENT_ID, PermissionsAux(delete = PermissionScope.SUB_TREE))
        val expectedPermittedJson =
            readFile("org/treeWare/model/operator/permitSet/set_request_mixed_clark_kent_with_delete_only.json")
        testPermitSet(expectedPermittedJson, rbac, expectedPermittedJson, true)
    }

    @Test
    fun `Partially matching set-model must be partially permitted for first-level sub-tree-scoped DELETE RBAC`() {
        val rbac = newPersonRbac(CLARK_KENT_ID, PermissionsAux(delete = PermissionScope.SUB_TREE))
        val expectedPermittedJson =
            readFile("org/treeWare/model/operator/permitSet/set_request_mixed_clark_kent_with_delete_only.json")
        testPermitSet(mixedSetJson, rbac, expectedPermittedJson, false)
    }

    @Test
    fun `Non-matching set-model must not be permitted for first-level sub-tree-scoped DELETE RBAC`() {
        val rbac = newPersonRbac(LOIS_LANE_ID, PermissionsAux(delete = PermissionScope.SUB_TREE))
        testPermitSet(mixedClarkKentSetJson, rbac, expectedPermittedJson = null, false)
    }

    @Test
    fun `Fully matching set-model must not be permitted for first-level node-scoped DELETE RBAC`() {
        val rbac = newPersonRbac(CLARK_KENT_ID, PermissionsAux(delete = PermissionScope.NODE))
        testPermitSet(mixedClarkKentSetJson, rbac, expectedPermittedJson = null, false)
    }

    @Test
    fun `Fully matching set-model must not be permitted for first-level none-scoped DELETE RBAC`() {
        val rbac = newPersonRbac(CLARK_KENT_ID, PermissionsAux(delete = PermissionScope.NONE))
        testPermitSet(mixedClarkKentSetJson, rbac, expectedPermittedJson = null, false)
    }

    @Test
    fun `Partially matching set-model must be partially permitted for first-level none-scoped-inside-sub-tree-scoped DELETE RBAC`() {
        // Permit all persons except Lois Lane
        val rbac = newPersonRbac(
            LOIS_LANE_ID,
            PermissionsAux(delete = PermissionScope.NONE),
            PermissionsAux(delete = PermissionScope.SUB_TREE)
        )
        val expectedPermittedJson =
            readFile("org/treeWare/model/operator/permitSet/set_request_mixed_persons_other_than_lois_lane_with_delete_only.json")
        testPermitSet(mixedSetJson, rbac, expectedPermittedJson, false)
    }

    // endregion

    // region First-level RBAC tests CREATE & UPDATE permissions

    @Test
    fun `Fully matching mixed set-model must be partially permitted for first-level sub-tree-scoped CREATE & UPDATE RBAC`() {
        val rbac = newPersonRbac(
            CLARK_KENT_ID,
            PermissionsAux(create = PermissionScope.SUB_TREE, update = PermissionScope.SUB_TREE)
        )
        val expectedPermittedJson =
            readFile("org/treeWare/model/operator/permitSet/set_request_mixed_clark_kent_with_create_and_update_only.json")
        testPermitSet(mixedClarkKentSetJson, rbac, expectedPermittedJson, false)
    }

    @Test
    fun `Fully matching CREATE & UPDATE set-model must be fully permitted for first-level sub-tree-scoped CREATE & UPDATE RBAC`() {
        val rbac = newPersonRbac(
            CLARK_KENT_ID,
            PermissionsAux(create = PermissionScope.SUB_TREE, update = PermissionScope.SUB_TREE)
        )
        val expectedPermittedJson =
            readFile("org/treeWare/model/operator/permitSet/set_request_mixed_clark_kent_with_create_and_update_only.json")
        testPermitSet(expectedPermittedJson, rbac, expectedPermittedJson, true)
    }

    @Test
    fun `Partially matching set-model must be partially permitted for first-level sub-tree-scoped CREATE & UPDATE RBAC`() {
        val rbac = newPersonRbac(
            CLARK_KENT_ID,
            PermissionsAux(create = PermissionScope.SUB_TREE, update = PermissionScope.SUB_TREE)
        )
        val expectedPermittedJson =
            readFile("org/treeWare/model/operator/permitSet/set_request_mixed_clark_kent_with_create_and_update_only.json")
        testPermitSet(mixedSetJson, rbac, expectedPermittedJson, false)
    }

    @Test
    fun `Non-matching set-model must not be permitted for first-level sub-tree-scoped CREATE & UPDATE RBAC`() {
        val rbac = newPersonRbac(
            LOIS_LANE_ID,
            PermissionsAux(create = PermissionScope.SUB_TREE, update = PermissionScope.SUB_TREE)
        )
        testPermitSet(mixedClarkKentSetJson, rbac, expectedPermittedJson = null, false)
    }

    @Test
    fun `Fully matching set-model must not be permitted for first-level node-scoped CREATE & UPDATE RBAC`() {
        val rbac =
            newPersonRbac(CLARK_KENT_ID, PermissionsAux(create = PermissionScope.NODE, update = PermissionScope.NODE))
        testPermitSet(mixedClarkKentSetJson, rbac, expectedPermittedJson = null, false)
    }

    @Test
    fun `Fully matching set-model must not be permitted for first-level none-scoped CREATE & UPDATE RBAC`() {
        val rbac =
            newPersonRbac(CLARK_KENT_ID, PermissionsAux(create = PermissionScope.NONE, update = PermissionScope.NONE))
        testPermitSet(mixedClarkKentSetJson, rbac, expectedPermittedJson = null, false)
    }

    @Test
    fun `Partially matching set-model must be partially permitted for first-level none-scoped-inside-sub-tree-scoped CREATE & UPDATE RBAC`() {
        // Permit all persons except Lois Lane
        val rbac = newPersonRbac(
            LOIS_LANE_ID,
            PermissionsAux(create = PermissionScope.NONE, update = PermissionScope.NONE),
            PermissionsAux(create = PermissionScope.SUB_TREE, update = PermissionScope.SUB_TREE)
        )
        val expectedPermittedJson =
            readFile("org/treeWare/model/operator/permitSet/set_request_mixed_clark_kent_with_create_and_update_only.json")
        testPermitSet(mixedSetJson, rbac, expectedPermittedJson, false)
    }

    // endregion

    // endregion

    // region Associations

    private fun newAssociationSetJson(setAux: SetAux): String = """
        |{
        |  "set_": "${setAux.name.lowercase()}",
        |  "person": [
        |    {
        |      "id": "$CLARK_KENT_ID",
        |      "group": {
        |        "groups": [
        |          {
        |            "name": "DC"
        |          }
        |        ]
        |      }
        |    }
        |  ],
        |  "city_info": [
        |    {
        |      "city": {
        |        "name": "Albany",
        |        "state": "New York",
        |        "country": "United States of America"
        |      },
        |      "related_city_info": [
        |        {
        |          "city_info": [
        |            {
        |              "city": {
        |                "name": "New York City",
        |                "state": "New York",
        |                "country": "United States of America"
        |              }
        |            }
        |          ]
        |        }
        |      ]
        |    }
        |  ]
        |}
    """.trimMargin()

    /**
     * Create an RBAC model with CRUD permissions for the association sources in the above set-request.
     * If `permitTargets` is `true`, also grant permissions for the association targets.
     */
    private fun newAssociationRbac(permitTargets: Boolean): EntityModel {
        val rbac = AddressBookMutableEntityModelFactory.create()

        val personSet = getOrNewMutableSetField(rbac, "person")
        val clarkKent = getNewMutableSetEntity(personSet)
        setUuidSingleField(clarkKent, "id", CLARK_KENT_ID)
        setPermissionsAux(clarkKent, PermissionsAux(crud = PermissionScope.SUB_TREE))
        personSet.addValue(clarkKent)

        val cityInfoSet = getOrNewMutableSetField(rbac, "city_info")
        val albanyCityInfo = getNewMutableSetEntity(cityInfoSet)
        addCity(albanyCityInfo, "Albany", "New York", "United States of America")
        setPermissionsAux(albanyCityInfo, PermissionsAux(crud = PermissionScope.SUB_TREE))
        cityInfoSet.addValue(albanyCityInfo)

        if (permitTargets) {
            val groups = getOrNewMutableSetField(rbac, "groups")
            val dc = getNewMutableSetEntity(groups)
            setStringSingleField(dc, "name", "DC")
            setPermissionsAux(dc, PermissionsAux(read = PermissionScope.SUB_TREE))
            groups.addValue(dc)

            val newYorkCityInfo = getNewMutableSetEntity(cityInfoSet)
            addCity(newYorkCityInfo, "New York City", "New York", "United States of America")
            setPermissionsAux(newYorkCityInfo, PermissionsAux(read = PermissionScope.SUB_TREE))
            cityInfoSet.addValue(newYorkCityInfo)
        }

        return rbac
    }

    @Test
    fun `CREATE association must fail if user does not have read permission for target`() {
        val createAssociationJson = newAssociationSetJson(SetAux.CREATE)
        val rbac = newAssociationRbac(false)
        // The `person` entity is expected along with its ID because it is a create request.
        // `related_city_info` is expected as an empty list since empty lists are meaningful and are not pruned.
        val expectedPermittedJson = """
            |{
            |  "set_": "create",
            |  "person": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f"
            |    }
            |  ],
            |  "city_info": [
            |    {
            |      "city": {
            |        "name": "Albany",
            |        "state": "New York",
            |        "country": "United States of America"
            |      },
            |      "related_city_info": []
            |    }
            |  ]
            |}
        """.trimMargin()
        testPermitSet(createAssociationJson, rbac, expectedPermittedJson, false)
    }

    @Test
    fun `CREATE association must succeed if user has read permission for target`() {
        val createAssociationJson = newAssociationSetJson(SetAux.CREATE)
        val rbac = newAssociationRbac(true)
        testPermitSet(createAssociationJson, rbac, expectedPermittedJson = createAssociationJson, true)
    }

    @Test
    fun `UPDATE association must fail if user does not have read permission for target`() {
        val updateAssociationJson = newAssociationSetJson(SetAux.UPDATE)
        val rbac = newAssociationRbac(false)
        // `related_city_info` is expected as an empty list since empty lists are meaningful and are not pruned.
        val expectedPermittedJson = """
            |{
            |  "set_": "update",
            |  "city_info": [
            |    {
            |      "city": {
            |        "name": "Albany",
            |        "state": "New York",
            |        "country": "United States of America"
            |      },
            |      "related_city_info": []
            |    }
            |  ]
            |}
        """.trimMargin()
        testPermitSet(updateAssociationJson, rbac, expectedPermittedJson, false)
    }

    @Test
    fun `UPDATE association must succeed if user has read permission for target`() {
        val updateAssociationJson = newAssociationSetJson(SetAux.UPDATE)
        val rbac = newAssociationRbac(true)
        testPermitSet(updateAssociationJson, rbac, expectedPermittedJson = updateAssociationJson, true)
    }

    @Test
    fun `DELETE association must succeed if user does not have read permission for target`() {
        val deleteAssociationJson = newAssociationSetJson(SetAux.DELETE)
        val rbac = newAssociationRbac(false)
        testPermitSet(deleteAssociationJson, rbac, expectedPermittedJson = deleteAssociationJson, true)
    }

    @Test
    fun `DELETE association must succeed if user has read permission for target`() {
        val deleteAssociationJson = newAssociationSetJson(SetAux.DELETE)
        val rbac = newAssociationRbac(true)
        testPermitSet(deleteAssociationJson, rbac, expectedPermittedJson = deleteAssociationJson, true)
    }

    // endregion
}