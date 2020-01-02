package org.tree_ware.core.codec.json

import org.tree_ware.core.schema.*

fun getGoldenKotlinSchema(): PackageSchema {
    return MutablePackageSchema(
            name = "test_package",
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
                    MutableEntitySchema(name = "entity2", fields = listOf()),
                    MutableEntitySchema(name = "entity3", fields = listOf())
            )
    )
}
