package org.treeWare.model.action

import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.treeWare.metaModel.newAddressBookMetaModel
import org.treeWare.model.assertMatchesJson
import org.treeWare.model.core.*
import org.treeWare.model.decoder.stateMachine.StringAuxStateMachine
import org.treeWare.model.encoder.EncodePasswords
import org.treeWare.model.getMainModel
import kotlin.test.Test

class GetTests {
    @Test
    fun `get() returns the requested data`() = runBlocking {
        val metaModel = newAddressBookMetaModel(null, null)

        val request = getMainModel<Unit>(metaModel, "model/address_book_get_person_request.json")
        val mapping =
            getMainModel(
                metaModel,
                "model/address_book_mapping_model.json",
                expectedModelType = "mapping"
            ) { StringAuxStateMachine(it) }

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
            delegate.fetchCompositionList(ofType(), listOf("id", "first_name", "email"), "person_mapping")
        } answers {
            val setField = arg<MutableSetFieldModel<Unit>>(0)

            val entity1 = setField.getNewValue() as MutableEntityModel
            val id1 = entity1.getOrNewField("id") as MutableSingleFieldModel
            (id1.getOrNewValue() as MutablePrimitiveModel).setValue("cc477201-48ec-4367-83a4-7fdbd92f8a6f")
            val firstNameField1 = entity1.getOrNewField("first_name") as MutableSingleFieldModel
            (firstNameField1.getOrNewValue() as MutablePrimitiveModel).setValue("Clark")
            val emailListField1 = entity1.getOrNewField("email") as MutableListFieldModel
            (emailListField1.getNewValue() as MutablePrimitiveModel).setValue("clark.kent@dailyplanet.com")
            (emailListField1.getNewValue() as MutablePrimitiveModel).setValue("superman@dc.com")
            setField.addValue(entity1)

            val entity2 = setField.getNewValue() as MutableEntityModel
            val id2 = entity2.getOrNewField("id") as MutableSingleFieldModel
            (id2.getOrNewValue() as MutablePrimitiveModel).setValue("a8aacf55-7810-4b43-afe5-4344f25435fd")
            val firstNameField2 = entity2.getOrNewField("first_name") as MutableSingleFieldModel
            (firstNameField2.getOrNewValue() as MutablePrimitiveModel).setValue("Lois")
            val emailListField2 = entity2.getOrNewField("email") as MutableListFieldModel
            (emailListField2.getNewValue() as MutablePrimitiveModel).setValue("lois.lane@dailyplanet.com")
            setField.addValue(entity2)
        }

        // Test the get() method.
        val visitor = CompositionTableGetVisitor(delegate)
        val response = get(request, mapping, visitor)

        coVerifySequence {
            delegate.pushPathEntity(ofType())
            delegate.fetchRoot(
                ofType(),
                listOf("name", "last_updated", "settings/last_name_first", "settings/card_colors"),
                "root_mapping"
            )
            delegate.fetchCompositionList(ofType(), listOf("id", "first_name", "email"), "person_mapping")
            // TODO(deepak-nulu): the following stopped getting called when compositions were changed from lists to sets.
            // delegate.fetchCompositionList(ofType(), listOf(), "relation_mapping")
            // delegate.fetchCompositionList(ofType(), listOf(), "relation_mapping")
            delegate.popPathEntity()
        }
        assertMatchesJson(response, null, "model/address_book_get_person_response.json", EncodePasswords.NONE)
    }
}
