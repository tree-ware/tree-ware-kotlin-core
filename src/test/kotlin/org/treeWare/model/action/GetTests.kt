package org.treeWare.model.action

import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.treeWare.model.assertMatchesJson
import org.treeWare.model.codec.decoder.stateMachine.StringAuxStateMachine
import org.treeWare.model.core.*
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
            val nameField = root.getOrNewField("name") as MutableSingleFieldModel
            (nameField.getOrNewValue() as MutablePrimitiveModel).setValue("Super Heroes")
            val lastUpdatedField = root.getOrNewField("last_updated") as MutableSingleFieldModel
            (lastUpdatedField.getOrNewValue() as MutablePrimitiveModel).setValue("1587147731")
            val settingsField = root.getOrNewField("settings") as MutableSingleFieldModel
            val settings = settingsField.getOrNewValue() as MutableEntityModel
            val lastNameFirstField = settings.getOrNewField("last_name_first") as MutableSingleFieldModel
            (lastNameFirstField.getOrNewValue() as MutablePrimitiveModel).setValue(true)
            val colorsField = settings.getOrNewField("card_colors") as MutableListFieldModel
            (colorsField.getNewValue() as MutableEnumerationModel).setValue("orange")
            (colorsField.getNewValue() as MutableEnumerationModel).setValue("green")
            (colorsField.getNewValue() as MutableEnumerationModel).setValue("blue")
        }
        // Fetch person list.
        coEvery {
            delegate.fetchCompositionList(ofType(), listOf("first_name", "email"), "person_mapping")
        } answers {
            val listField = arg<MutableListFieldModel<Unit>>(0)

            val entity1 = listField.getNewValue() as MutableEntityModel
            val firstNameField1 = entity1.getOrNewField("first_name") as MutableSingleFieldModel
            (firstNameField1.getOrNewValue() as MutablePrimitiveModel).setValue("Clark")
            val emailListField1 = entity1.getOrNewField("email") as MutableListFieldModel
            (emailListField1.getNewValue() as MutablePrimitiveModel).setValue("clark.kent@dailyplanet.com")
            (emailListField1.getNewValue() as MutablePrimitiveModel).setValue("superman@dc.com")

            val entity2 = listField.getNewValue() as MutableEntityModel
            val firstNameField2 = entity2.getOrNewField("first_name") as MutableSingleFieldModel
            (firstNameField2.getOrNewValue() as MutablePrimitiveModel).setValue("Lois")
            val emailListField2 = entity2.getOrNewField("email") as MutableListFieldModel
            (emailListField2.getNewValue() as MutablePrimitiveModel).setValue("lois.lane@dailyplanet.com")
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
