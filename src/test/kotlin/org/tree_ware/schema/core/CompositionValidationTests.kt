package org.tree_ware.schema.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CompositionValidationTests {
    @Test
    fun `Composition key target entity must have keys`() {
        val testPackage = MutablePackageSchema(
            name = "test.package",
            entities = listOf(
                MutableEntitySchema(
                    name = "test_entity",
                    fields = listOf(
                        MutableCompositionFieldSchema(
                            name = "test_field",
                            packageName = "helper.package",
                            entityName = "entity_with_no_keys",
                            isKey = true
                        )
                    )
                )
            )
        )
        val schema = MutableSchema(
            newLocalHelperRoot(),
            listOf(testPackage, newLocalHelperPackage())
        )
        val errors = validate(schema)

        val expectedErrors = listOf(
            "Target of composition key does not have only primitive keys: /test.package/test_entity/test_field"
        )

        assertEquals(expectedErrors.toString(), errors.toString())
    }

    @Test
    fun `Composition key target entity must not have composition keys`() {
        val testPackage = MutablePackageSchema(
            name = "test.package",
            entities = listOf(
                MutableEntitySchema(
                    name = "test_entity",
                    fields = listOf(
                        MutableCompositionFieldSchema(
                            name = "test_field_1",
                            packageName = "helper.package",
                            entityName = "entity_with_composition_key",
                            isKey = true
                        ),
                        MutableCompositionFieldSchema(
                            name = "test_field_2",
                            packageName = "helper.package",
                            entityName = "entity_with_primitive_and_composition_keys",
                            isKey = true
                        )
                    )
                )
            )
        )
        val schema = MutableSchema(
            newLocalHelperRoot(),
            listOf(testPackage, newLocalHelperPackage())
        )
        val errors = validate(schema)

        val expectedErrors = listOf(
            "Target of composition key does not have only primitive keys: /test.package/test_entity/test_field_1",
            "Target of composition key does not have only primitive keys: /test.package/test_entity/test_field_2"
        )

        assertEquals(expectedErrors.toString(), errors.toString())
    }

    @Test
    fun `Composition key is valid if target entity has only primitive keys`() {
        val testPackage = MutablePackageSchema(
            name = "test.package",
            entities = listOf(
                MutableEntitySchema(
                    name = "test_entity",
                    fields = listOf(
                        MutableCompositionFieldSchema(
                            name = "test_field",
                            packageName = "helper.package",
                            entityName = "entity_with_only_primitive_keys",
                            isKey = true
                        )
                    )
                )
            )
        )
        val schema = MutableSchema(
            newLocalHelperRoot(),
            listOf(testPackage, newLocalHelperPackage())
        )
        val errors = validate(schema)

        assertTrue(errors.isEmpty())
    }

    @Test
    fun `Composition list target entity must have keys`() {
        val testPackage = MutablePackageSchema(
            name = "test.package",
            entities = listOf(
                MutableEntitySchema(
                    name = "test_entity",
                    fields = listOf(
                        MutableCompositionFieldSchema(
                            name = "test_field1",
                            packageName = "helper.package",
                            entityName = "entity_with_no_keys",
                            multiplicity = MutableMultiplicity(1, 2)
                        ),
                        MutableCompositionFieldSchema(
                            name = "test_field2",
                            packageName = "helper.package",
                            entityName = "entity_with_no_keys",
                            multiplicity = MutableMultiplicity(0, 0)
                        )
                    )
                )
            )
        )
        val schema = MutableSchema(
            newLocalHelperRoot(),
            listOf(testPackage, newLocalHelperPackage())
        )
        val errors = validate(schema)

        val expectedErrors = listOf(
            "Target of composition list does not have keys: /test.package/test_entity/test_field1",
            "Target of composition list does not have keys: /test.package/test_entity/test_field2"
        )

        assertEquals(expectedErrors.toString(), errors.toString())
    }

    @Test
    fun `Composition list is valid if target entity has keys`() {
        val testPackage = MutablePackageSchema(
            name = "test.package",
            entities = listOf(
                MutableEntitySchema(
                    name = "test_entity",
                    fields = listOf(
                        MutableCompositionFieldSchema(
                            name = "test_field1",
                            packageName = "helper.package",
                            entityName = "entity_with_primitive_and_composition_keys",
                            multiplicity = MutableMultiplicity(1, 2)
                        ),
                        MutableCompositionFieldSchema(
                            name = "test_field2",
                            packageName = "helper.package",
                            entityName = "entity_with_primitive_and_composition_keys",
                            multiplicity = MutableMultiplicity(0, 0)
                        )
                    )
                )
            )
        )
        val schema = MutableSchema(
            newLocalHelperRoot(),
            listOf(testPackage, newLocalHelperPackage())
        )
        val errors = validate(schema)

        assertTrue(errors.isEmpty())
    }
}

private fun newLocalHelperRoot() = MutableRootSchema(
    name = "root",
    packageName = "helper.package",
    entityName = "entity_with_no_keys"
)

private fun newLocalHelperPackage() = MutablePackageSchema(
    name = "helper.package",
    entities = listOf(
        MutableEntitySchema(
            name = "entity_with_no_keys",
            fields = listOf(
                MutablePrimitiveFieldSchema(
                    name = "boolean_field",
                    primitive = MutableBooleanSchema()
                )
            )
        ),
        MutableEntitySchema(
            name = "entity_with_composition_key",
            fields = listOf(
                MutableCompositionFieldSchema(
                    name = "composition_field",
                    packageName = "helper.package",
                    entityName = "entity_with_only_primitive_keys",
                    isKey = true
                )
            )
        ),
        MutableEntitySchema(
            name = "entity_with_primitive_and_composition_keys",
            fields = listOf(
                MutablePrimitiveFieldSchema(
                    name = "boolean_field",
                    primitive = MutableBooleanSchema(),
                    isKey = true
                ),
                MutableCompositionFieldSchema(
                    name = "composition_field",
                    packageName = "helper.package",
                    entityName = "entity_with_only_primitive_keys",
                    isKey = true
                )
            )
        ),
        MutableEntitySchema(
            name = "entity_with_only_primitive_keys",
            fields = listOf(
                MutablePrimitiveFieldSchema(
                    name = "key_string_field",
                    primitive = MutableStringSchema(),
                    isKey = true
                ),
                MutablePrimitiveFieldSchema(
                    name = "non_key_string_field",
                    primitive = MutableStringSchema()
                ),
                MutablePrimitiveFieldSchema(
                    name = "non_key_boolean_field",
                    primitive = MutableBooleanSchema()
                )
            )
        )
    )
)
