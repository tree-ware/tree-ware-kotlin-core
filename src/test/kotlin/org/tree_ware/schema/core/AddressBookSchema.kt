package org.tree_ware.schema.core

import org.tree_ware.schema.core.*

fun getAddressBookPackage() = MutablePackageSchema(
    name = "address_book",
    info = "Schema for storing address book information",
    root = MutableRootSchema(
        name = "address_book",
        packageName = "address_book",
        entityName = "address_book_root"
    ),
    entities = listOf(
        MutableEntitySchema(
            name = "address_book_root",
            fields = listOf(
                MutablePrimitiveFieldSchema(
                    name = "name",
                    info = "A name for the address book",
                    primitive = MutableStringSchema()
                ),
                MutablePrimitiveFieldSchema(
                    name = "last_updated",
                    primitive = MutableTimestampSchema()
                ),
                MutableCompositionFieldSchema(
                    name = "settings",
                    packageName = "address_book",
                    entityName = "address_book_settings",
                    multiplicity = MutableMultiplicity(0, 1)
                ),
                MutableCompositionFieldSchema(
                    name = "person",
                    packageName = "address_book",
                    entityName = "address_book_person",
                    multiplicity = MutableMultiplicity(0, 0)
                )
            )
        ),
        MutableEntitySchema(
            name = "address_book_settings",
            fields = listOf(
                MutablePrimitiveFieldSchema(
                    name = "last_name_first",
                    primitive = MutableBooleanSchema(),
                    multiplicity = MutableMultiplicity(0, 1)
                ),
                MutablePrimitiveFieldSchema(
                    name = "encrypt_hero_name",
                    primitive = MutableBooleanSchema(),
                    multiplicity = MutableMultiplicity(0, 1)
                ),
                MutableEnumerationFieldSchema(
                    name = "card_colors",
                    packageName = "address_book",
                    enumerationName = "address_book_color",
                    multiplicity = MutableMultiplicity(0, 5)
                )
            )
        ),
        MutableEntitySchema(
            name = "address_book_person",
            fields = listOf(
                MutablePrimitiveFieldSchema(
                    name = "id",
                    primitive = MutableUuidSchema(),
                    isKey = true
                ),
                MutablePrimitiveFieldSchema(
                    name = "first_name",
                    primitive = MutableStringSchema()
                ),
                MutablePrimitiveFieldSchema(
                    name = "last_name",
                    primitive = MutableStringSchema()
                ),
                MutablePrimitiveFieldSchema(
                    name = "hero_name",
                    primitive = MutableStringSchema(),
                    multiplicity = MutableMultiplicity(0, 1)
                ),
                MutablePrimitiveFieldSchema(
                    name = "email",
                    primitive = MutableStringSchema(),
                    multiplicity = MutableMultiplicity(0, 0)
                ),
                MutableCompositionFieldSchema(
                    name = "relation",
                    packageName = "address_book",
                    entityName = "address_book_relation",
                    multiplicity = MutableMultiplicity(0, 0)
                )
            )
        ),
        MutableEntitySchema(
            name = "address_book_relation",
            fields = listOf(
                MutablePrimitiveFieldSchema(
                    name = "id",
                    primitive = MutableUuidSchema(),
                    isKey = true
                ),
                MutableEnumerationFieldSchema(
                    name = "relationship",
                    packageName = "address_book",
                    enumerationName = "address_book_relationship"
                ),
                MutableAssociationFieldSchema(
                    name = "person",
                    entityPath = listOf(
                        "address_book",
                        "person"
                    )
                )
            )
        )
    ),
    enumerations = listOf(
        MutableEnumerationSchema(
            name = "address_book_color",
            values = listOf(
                MutableEnumerationValueSchema(
                    name = "violet"
                ),
                MutableEnumerationValueSchema(
                    name = "indigo"
                ),
                MutableEnumerationValueSchema(
                    name = "blue"
                ),
                MutableEnumerationValueSchema(
                    name = "green"
                ),
                MutableEnumerationValueSchema(
                    name = "yellow"
                ),
                MutableEnumerationValueSchema(
                    name = "orange"
                ),
                MutableEnumerationValueSchema(
                    name = "red"
                )
            )
        ),
        MutableEnumerationSchema(
            name = "address_book_relationship",
            values = listOf(
                MutableEnumerationValueSchema(
                    name = "parent"
                ),
                MutableEnumerationValueSchema(
                    name = "child"
                ),
                MutableEnumerationValueSchema(
                    name = "spouse"
                ),
                MutableEnumerationValueSchema(
                    name = "sibling"
                ),
                MutableEnumerationValueSchema(
                    name = "family"
                ),
                MutableEnumerationValueSchema(
                    name = "friend"
                ),
                MutableEnumerationValueSchema(
                    name = "colleague"
                )
            )
        )
    )
)