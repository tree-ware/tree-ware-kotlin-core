package org.tree_ware.core.codec.json

import org.tree_ware.core.schema.*

fun getGoldenKotlinPackages(): List<MutablePackageSchema> {
    val packageA = MutablePackageSchema(
        name = "package.a",
        root = MutableCompositionFieldSchema(
            name = "test",
            packageName = "package.a",
            entityName = "entity1"
        ),
        entities = listOf(
            MutableEntitySchema(
                name = "entity1",
                fields = listOf(
                    MutablePrimitiveFieldSchema(
                        name = "primitive_string_field",
                        primitive = MutableStringSchema(),
                        isKey = true
                    ),
                    MutableCompositionFieldSchema(
                        name = "package_b_entity2_composition_field",
                        packageName = "package.b",
                        entityName = "entity2"
                    )
                )
            ),
            MutableEntitySchema(
                name = "entity2",
                fields = listOf(
                    MutableAssociationFieldSchema(
                        name = "package_a_association_field",
                        entityPath = listOf(
                            "test",
                            "package_b_entity2_composition_field",
                            "package_c_entity3_composition_field"
                        )
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
                    )
                )
            )
        )
    )

    return listOf(packageA, packageB, packageC)
}
