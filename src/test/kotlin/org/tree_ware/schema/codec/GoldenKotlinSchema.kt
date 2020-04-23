package org.tree_ware.schema.codec

import org.tree_ware.schema.core.*

fun getGoldenKotlinSchema(): MutableSchema {
    val packageA = MutablePackageSchema(
        name = "package.a",
        info = "Test package A",
        entities = listOf(
            MutableEntitySchema(
                name = "entity1",
                info = "First entity in package.a",
                fields = listOf(
                    MutablePrimitiveFieldSchema(
                        name = "primitive_string_field",
                        info = "A primitive string field",
                        primitive = MutableStringSchema(),
                        isKey = true
                    ),
                    MutableCompositionFieldSchema(
                        name = "package_b_entity2_composition_field",
                        info = "A field that composes entity 2 in package.b",
                        packageName = "package.b",
                        entityName = "entity2",
                        multiplicity = MutableMultiplicity(0, 1)
                    )
                )
            ),
            MutableEntitySchema(
                name = "entity2",
                fields = listOf(
                    MutableAssociationFieldSchema(
                        name = "package_a_association_field",
                        info = "A field that is an association to the entity at the end of the specified path",
                        entityPath = listOf(
                            "test",
                            "package_b_entity2_composition_field",
                            "package_c_entity3_composition_field"
                        ),
                        multiplicity = MutableMultiplicity(1, 10)
                    ),
                    MutableAliasFieldSchema(
                        name = "package_b_string_alias1_field",
                        packageName = "package.b",
                        aliasName = "string_alias1",
                        isKey = true
                    ),
                    MutableEnumerationFieldSchema(
                        name = "package_b_enumeration1_field",
                        packageName = "package.b",
                        enumerationName = "enumeration1",
                        isKey = true
                    ),
                    MutableCompositionFieldSchema(
                        name = "package_b_entity1_composition_field",
                        packageName = "package.b",
                        entityName = "entity1",
                        isKey = true
                    )
                )
            ),
            MutableEntitySchema(name = "entity3", fields = listOf())
        )
    )

    val packageB = MutablePackageSchema(
        name = "package.b",
        aliases = listOf(
            MutableAliasSchema(
                name = "string_alias1",
                primitive = MutableStringSchema()
            )
        ),
        enumerations = listOf(
            MutableEnumerationSchema(
                name = "enumeration1",
                values = listOf(
                    MutableEnumerationValueSchema("value1"),
                    MutableEnumerationValueSchema("value2"),
                    MutableEnumerationValueSchema("value3")
                )
            )
        ),
        entities = listOf(
            MutableEntitySchema(
                name = "entity1",
                fields = listOf(
                    MutablePrimitiveFieldSchema(
                        name = "primitive_boolean_field",
                        primitive = MutableBooleanSchema(),
                        isKey = true
                    )
                )
            ),
            MutableEntitySchema(
                name = "entity2",
                fields = listOf(
                    MutablePrimitiveFieldSchema(
                        name = "primitive_boolean_field",
                        primitive = MutableBooleanSchema(),
                        isKey = true
                    ),
                    MutableCompositionFieldSchema(
                        name = "package_c_entity3_composition_field",
                        packageName = "package.c",
                        entityName = "entity3"
                    )
                )
            )
        )
    )

    val packageC = MutablePackageSchema(
        name = "package.c",
        entities = listOf(
            MutableEntitySchema(
                name = "entity3",
                fields = listOf(
                    MutablePrimitiveFieldSchema(
                        name = "primitive_boolean_field",
                        primitive = MutableBooleanSchema(),
                        isKey = true
                    ),
                    MutablePrimitiveFieldSchema(
                        name = "primitive_byte_field",
                        primitive = MutableByteSchema()
                    ),
                    MutablePrimitiveFieldSchema(
                        name = "primitive_short_field",
                        primitive = MutableShortSchema()
                    ),
                    MutablePrimitiveFieldSchema(
                        name = "primitive_int_field",
                        primitive = MutableIntSchema()
                    ),
                    MutablePrimitiveFieldSchema(
                        name = "primitive_long_field",
                        primitive = MutableLongSchema()
                    ),
                    MutablePrimitiveFieldSchema(
                        name = "primitive_float_field",
                        primitive = MutableFloatSchema()
                    ),
                    MutablePrimitiveFieldSchema(
                        name = "primitive_double_field",
                        primitive = MutableDoubleSchema()
                    ),
                    MutablePrimitiveFieldSchema(
                        name = "primitive_string_field",
                        primitive = MutableStringSchema()
                    ),
                    MutablePrimitiveFieldSchema(
                        name = "primitive_password_1_way_field",
                        primitive = MutablePassword1WaySchema()
                    ),
                    MutablePrimitiveFieldSchema(
                        name = "primitive_password_2_way_field",
                        primitive = MutablePassword2WaySchema()
                    ),
                    MutablePrimitiveFieldSchema(
                        name = "primitive_uuid_field",
                        primitive = MutableUuidSchema(),
                        multiplicity = MutableMultiplicity(1, 0)
                    ),
                    MutablePrimitiveFieldSchema(
                        name = "primitive_blob_field",
                        primitive = MutableBlobSchema()
                    )
                )
            )
        )
    )

    return MutableSchema(
        MutableRootSchema(
            name = "test",
            info = "Test schema root",
            packageName = "package.a",
            entityName = "entity1"
        ),
        listOf(packageA, packageB, packageC)
    )
}
