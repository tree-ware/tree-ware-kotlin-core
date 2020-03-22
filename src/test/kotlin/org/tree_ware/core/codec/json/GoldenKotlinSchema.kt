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
                    )
                )
            ),
            MutableEntitySchema(
                name = "entity2",
                fields = listOf(
                    MutableAssociationFieldSchema(
                        name = "package_a_entity1_association_field",
                        entityPath = listOf("entity1")
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
                    MutableAssociationFieldSchema(
                        name = "package_a_entity3_association_field",
                        entityPath = listOf("entity3")
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
                        primitive = MutableBooleanSchema()
                    )
                )
            )
        )
    )

    return listOf(packageA, packageB)
}
