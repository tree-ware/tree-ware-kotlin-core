package org.treeWare.schema.core

import org.treeWare.metaModel.*
import org.treeWare.model.core.MutableEntityModel
import org.treeWare.model.core.MutableListFieldModel
import org.treeWare.model.core.MutableModel
import org.treeWare.model.core.Resolved

fun newAddressBookMetaModel(): MutableModel<Resolved> {
    val mainMeta = newMainMetaMeta()
    newRootMetaMeta(mainMeta, "address_book", "address_book_root", "address_book.main")
    val packagesMeta = newPackagesMetaMeta(mainMeta)
    populatePackages(packagesMeta)
    return mainMeta
}

private fun populatePackages(packages: MutableListFieldModel<Resolved>) {
    val mainPackage = newPackageMetaMeta(packages, "address_book.main", "Schema for storing address book information")
    populateMainPackage(mainPackage)
    val cityPackage = newPackageMetaMeta(packages, "address_book.city", "Schema for storing city information")
    populateCityPackage(cityPackage)
}

private fun populateMainPackage(mainPackage: MutableEntityModel<Resolved>) {
    val entities = newEntitiesMetaMeta(mainPackage)
    populateMainEntities(entities)
    val enumerations = newEnumerationsMetaMeta(mainPackage)
    populateMainEnumerations(enumerations)
}

private fun populateMainEntities(entities: MutableListFieldModel<Resolved>) {
    val addressBookRoot = newEntityMetaMeta(entities, "address_book_root")
    populateAddressBookRoot(addressBookRoot)
    val addressBookSettings = newEntityMetaMeta(entities, "address_book_settings")
    populateAddressBookSettings(addressBookSettings)
    val addressBookPerson = newEntityMetaMeta(entities, "address_book_person")
    populateAddressBookPerson(addressBookPerson)
    val addressBookRelation = newEntityMetaMeta(entities, "address_book_relation")
    populateAddressBookRelation(addressBookRelation)
}

private fun populateAddressBookRoot(root: MutableEntityModel<Resolved>) {
    val fields = newFieldsMetaMeta(root)
    newPrimitiveFieldMetaMeta(fields, "name", "A name for the address book", "string")
    newPrimitiveFieldMetaMeta(fields, "last_updated", null, "timestamp")
    newEntityFieldMetaMeta(fields, "settings", null, "address_book_settings", "address_book.main", "optional")
    newEntityFieldMetaMeta(fields, "person", null, "address_book_person", "address_book.main", "list")
    newEntityFieldMetaMeta(fields, "city_info", null, "address_book_city_info", "address_book.city", "list")
}

private fun populateAddressBookSettings(settings: MutableEntityModel<Resolved>) {
    val fields = newFieldsMetaMeta(settings)
    newPrimitiveFieldMetaMeta(fields, "last_name_first", null, "boolean", "optional")
    newPrimitiveFieldMetaMeta(fields, "encrypt_hero_name", null, "boolean", "optional")
    newEnumerationFieldMetaMeta(fields, "card_colors", null, "address_book_color", "address_book.main", "list")
}

private fun populateAddressBookPerson(person: MutableEntityModel<Resolved>) {
    val fields = newFieldsMetaMeta(person)
    newPrimitiveFieldMetaMeta(fields, "id", null, "uuid", null, true)
    newPrimitiveFieldMetaMeta(fields, "first_name", null, "string")
    newPrimitiveFieldMetaMeta(fields, "last_name", null, "string")
    newPrimitiveFieldMetaMeta(fields, "hero_name", null, "string", "optional")
    newPrimitiveFieldMetaMeta(fields, "email", null, "string", "list")
    newEntityFieldMetaMeta(fields, "relation", null, "address_book_relation", "address_book.main", "list")
}

private fun populateAddressBookRelation(relation: MutableEntityModel<Resolved>) {
    val fields = newFieldsMetaMeta(relation)
    newPrimitiveFieldMetaMeta(fields, "id", null, "uuid", null, true)
    newEnumerationFieldMetaMeta(fields, "relationship", null, "address_book_relationship", "address_book.main")
    newAssociationFieldMetaMeta(fields, "person", null, listOf("address_book", "person"))
}

private fun populateMainEnumerations(enumerations: MutableListFieldModel<Resolved>) {
    newEnumerationMetaMeta(
        enumerations, "address_book_color", null, listOf(
            EnumerationValueMetaMeta("violet"),
            EnumerationValueMetaMeta("indigo"),
            EnumerationValueMetaMeta("blue"),
            EnumerationValueMetaMeta("green"),
            EnumerationValueMetaMeta("yellow"),
            EnumerationValueMetaMeta("orange"),
            EnumerationValueMetaMeta("red"),
        )
    )

    newEnumerationMetaMeta(
        enumerations, "address_book_relationship", null, listOf(
            EnumerationValueMetaMeta("parent"),
            EnumerationValueMetaMeta("child"),
            EnumerationValueMetaMeta("spouse"),
            EnumerationValueMetaMeta("sibling"),
            EnumerationValueMetaMeta("family"),
            EnumerationValueMetaMeta("friend"),
            EnumerationValueMetaMeta("colleague"),
        )
    )
}

private fun populateCityPackage(cityPackage: MutableEntityModel<Resolved>) {
    val entities = newEntitiesMetaMeta(cityPackage)
    populateCityEntities(entities)
}

private fun populateCityEntities(entities: MutableListFieldModel<Resolved>) {
    val addressBookCity = newEntityMetaMeta(entities, "address_book_city")
    populateAddressBookCity(addressBookCity)
    val addressBookCityInfo = newEntityMetaMeta(entities, "address_book_city_info")
    populateAddressBookCityInfo(addressBookCityInfo)
}

private fun populateAddressBookCity(city: MutableEntityModel<Resolved>) {
    val fields = newFieldsMetaMeta(city)
    newPrimitiveFieldMetaMeta(fields, "name", "City name", "string", null, true)
    newPrimitiveFieldMetaMeta(fields, "state", "Name of the state in which the city is", "string", null, true)
    newPrimitiveFieldMetaMeta(fields, "country", "Name of the country in which the city is", "string", null, true)
}

private fun populateAddressBookCityInfo(cityInfo: MutableEntityModel<Resolved>) {
    val fields = newFieldsMetaMeta(cityInfo)
    newEntityFieldMetaMeta(fields, "city", null, "address_book_city", "address_book.city", null, true)
    newPrimitiveFieldMetaMeta(fields, "info", "Information about the city", "string")
    newAssociationFieldMetaMeta(fields, "related_city_info", null, listOf("address_book", "city_info"), "list")
}
