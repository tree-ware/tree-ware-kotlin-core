package org.tree_ware.core.codec.json

import org.tree_ware.core.schema.*

fun getGoldenKotlinPackages(): List<MutablePackageSchema> {
    val packageA = MutablePackageSchema(
            name = "package.a",
            entities = listOf(
                    MutableEntitySchema(
                            name = "entity1",
                            fields = listOf(
                                    MutablePrimitiveFieldSchema(
                                            name = "primitive_string_field",
                                            primitive = MutableStringSchema()
                                    )
                            )
                    ),
                    MutableEntitySchema(
                            name = "entity2",
                            fields = listOf(
                                    MutableEntityFieldSchema(
                                            name = "package_a_entity1_field",
                                            packageName = "package.a",
                                            entityName = "entity1"
                                    ),
                                    MutableAliasFieldSchema(
                                            name = "package_b_string_alias1_field",
                                            packageName = "package.b",
                                            aliasName = "string_alias1"
                                    ),
                                    MutableEntityFieldSchema(
                                            name = "package_a_entity3_field",
                                            packageName = "package.a",
                                            entityName = "entity3"
                                    ),
                                    MutableEntityFieldSchema(
                                            name = "package_b_entity1_field",
                                            packageName = "package.b",
                                            entityName = "entity1"
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
