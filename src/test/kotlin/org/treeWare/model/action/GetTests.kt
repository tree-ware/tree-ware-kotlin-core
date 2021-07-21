package org.treeWare.model.action

import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.treeWare.model.assertMatchesJson
import org.treeWare.model.codec.decoding_state_machine.StringAuxStateMachine
import org.treeWare.model.core.MutableCompositionListFieldModel
import org.treeWare.model.core.MutableRootModel
import org.treeWare.model.core.MutableScalarListFieldModel
import org.treeWare.model.getModel
import org.treeWare.schema.core.newAddressBookSchema
import org.treeWare.schema.core.validate
import kotlin.test.Test
import kotlin.test.assertTrue

class GetTests {
    @Test
    fun `get() returns the requested data`() = runBlocking {
        val schema = newAddressBookSchema()
        val errors = validate(schema)
        assertTrue(errors.isEmpty())

        val request = getModel<Unit>(schema, "model/address_book_get_person_request.json")
        val mapping = getModel(schema, "model/address_book_mapping_model.json", "mapping") { StringAuxStateMachine(it) }

        val delegate = mockk<CompositionTableGetVisitorDelegate<String>>(relaxUnitFun = true)

        // Set up the delegate mock.

        // Fetch root.
        coEvery {
            delegate.fetchRoot(
                ofType(),
                listOf("name", "last_updated", "settings/last_name_first", "settings/card_colors"),
                "root_mapping"
            )
        } answers {
            val root = arg<MutableRootModel<Unit>>(0)
            root.getOrNewScalarField("name")?.setValue("Super Heroes")
            root.getOrNewScalarField("last_updated")?.setValue("1587147731")
            val settings = root.getOrNewCompositionField("settings")?.value
            settings?.getOrNewScalarField("last_name_first")?.setValue(true)
            val colors = settings?.getOrNewListField("card_colors") as MutableScalarListFieldModel<Unit>
            colors.addElement().setValue("orange")
            colors.addElement().setValue("green")
            colors.addElement().setValue("blue")
        }
        // Fetch person list.
        coEvery {
            delegate.fetchCompositionList(ofType(), listOf("first_name", "email"), "person_mapping")
        } answers {
            val listField = arg<MutableCompositionListFieldModel<Unit>>(0)

            val entity1 = listField.addEntity()
            entity1.getOrNewScalarField("first_name")?.setValue("Clark")
            val emailList1 = entity1.getOrNewListField("email") as MutableScalarListFieldModel<Unit>
            emailList1.addElement().setValue("clark.kent@dailyplanet.com")
            emailList1.addElement().setValue("superman@dc.com")

            val entity2 = listField.addEntity()
            entity2.getOrNewScalarField("first_name")?.setValue("Lois")
            val emailList2 = entity2.getOrNewListField("email") as MutableScalarListFieldModel<Unit>
            emailList2.addElement().setValue("lois.lane@dailyplanet.com")
        }

        // Test the get() method.
        val visitor = CompositionTableGetVisitor(delegate)
        val response = get(request, mapping, visitor)

        coVerifySequence {
            delegate.pushPathEntity(ofType(), ofType())
            delegate.fetchRoot(
                ofType(),
                listOf("name", "last_updated", "settings/last_name_first", "settings/card_colors"),
                "root_mapping"
            )
            delegate.fetchCompositionList(ofType(), listOf("first_name", "email"), "person_mapping")
            delegate.fetchCompositionList(ofType(), listOf(), "relation_mapping")
            delegate.fetchCompositionList(ofType(), listOf(), "relation_mapping")
            delegate.popPathEntity()
        }
        assertMatchesJson(response, null, "model/address_book_get_person_response.json")
    }
}
