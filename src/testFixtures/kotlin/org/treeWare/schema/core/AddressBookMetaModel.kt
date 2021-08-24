package org.treeWare.schema.core

import org.treeWare.model.core.*

fun newAddressBookMetaModel(): MutableModel<Resolved> {
    val model = MutableModel<Resolved>(metaModelSchema, null)
    val metaModel = model.getOrNewRoot()
    populateMetaModel(metaModel)
    return model
}

private fun populateMetaModel(metaModel: MutableRootModel<Resolved>) {
    val root = getSingleEntity(metaModel, "root")
    populateRoot(root)
    val packagesField = getListField(metaModel, "packages")
    populatePackages(packagesField)
}

private fun populateRoot(rootEntity: MutableEntityModel<Resolved>) {
    setStringField(rootEntity, "name", "address_book")
    setStringField(rootEntity, "entity", "address_book_root")
    setStringField(rootEntity, "package", "address_book.main")
}

private fun populatePackages(packages: MutableListFieldModel<Resolved>) {
    val mainPackage = getNewListEntity(packages)
    populateMainPackage(mainPackage)
    val cityPackage = getNewListEntity(packages)
    populateCityPackage(cityPackage)
}

private fun populateMainPackage(mainPackage: MutableEntityModel<Resolved>) {
    setStringField(mainPackage, "name", "address_book.main")
    setStringField(mainPackage, "info", "Schema for storing address book information")
    val entities = getListField(mainPackage, "entities")
    populateMainEntities(entities)
    val enumerations = getListField(mainPackage, "enumerations")
    populateMainEnumerations(enumerations)
}

fun populateMainEntities(entities: MutableListFieldModel<Resolved>) {
    val addressBookRoot = getNewListEntity(entities)
    populateAddressBookRoot(addressBookRoot)
    val addressBookSettings = getNewListEntity(entities)
    populateAddressBookSettings(addressBookSettings)
    val addressBookPerson = getNewListEntity(entities)
    populateAddressBookPerson(addressBookPerson)
    val addressBookRelation = getNewListEntity(entities)
    populateAddressBookRelation(addressBookRelation)
}

fun populateAddressBookRoot(root: MutableEntityModel<Resolved>) {
    setStringField(root, "name", "address_book_root")
    val fields = getListField(root, "fields")

    val nameField = getNewListEntity(fields)
    setStringField(nameField, "name", "name")
    setStringField(nameField, "info", "A name for the address book")
    setStringField(nameField, "type", "string")

    val lastUpdatedField = getNewListEntity(fields)
    setStringField(lastUpdatedField, "name", "last_updated")
    setStringField(lastUpdatedField, "type", "timestamp")

    val settingsField = getNewListEntity(fields)
    populateAsEntityField(settingsField, "settings", "address_book_settings", "address_book.main")
    setStringField(settingsField, "multiplicity", "optional")

    val personField = getNewListEntity(fields)
    populateAsEntityField(personField, "person", "address_book_person", "address_book.main")
    setStringField(personField, "multiplicity", "list")

    val cityInfoField = getNewListEntity(fields)
    populateAsEntityField(cityInfoField, "city_info", "address_book_city_info", "address_book.city")
    setStringField(cityInfoField, "multiplicity", "list")
}

fun populateAddressBookSettings(settings: MutableEntityModel<Resolved>) {
    setStringField(settings, "name", "address_book_settings")
    val fields = getListField(settings, "fields")

    val lastNameFirstField = getNewListEntity(fields)
    setStringField(lastNameFirstField, "name", "last_name_first")
    setStringField(lastNameFirstField, "type", "boolean")
    setStringField(lastNameFirstField, "multiplicity", "optional")

    val encryptHeroNameField = getNewListEntity(fields)
    setStringField(encryptHeroNameField, "name", "encrypt_hero_name")
    setStringField(encryptHeroNameField, "type", "boolean")
    setStringField(encryptHeroNameField, "multiplicity", "optional")

    val cardColorsField = getNewListEntity(fields)
    setStringField(cardColorsField, "name", "card_colors")
    setStringField(cardColorsField, "type", "enumeration")
    val cardColorsEnumeration = getSingleEntity(cardColorsField, "enumeration")
    setStringField(cardColorsEnumeration, "name", "address_book_color")
    setStringField(cardColorsEnumeration, "package", "address_book.main")
    setStringField(cardColorsField, "multiplicity", "list")
}

fun populateAddressBookPerson(person: MutableEntityModel<Resolved>) {
    setStringField(person, "name", "address_book_person")
    val fields = getListField(person, "fields")

    val idField = getNewListEntity(fields)
    setStringField(idField, "name", "id")
    setStringField(idField, "type", "uuid")
    setBooleanField(idField, "is_key", true)

    val firstNameField = getNewListEntity(fields)
    setStringField(firstNameField, "name", "first_name")
    setStringField(firstNameField, "type", "string")

    val lastNameField = getNewListEntity(fields)
    setStringField(lastNameField, "name", "last_name")
    setStringField(lastNameField, "type", "string")

    val heroNameField = getNewListEntity(fields)
    setStringField(heroNameField, "name", "hero_name")
    setStringField(heroNameField, "type", "string")
    setStringField(heroNameField, "multiplicity", "optional")

    val emailField = getNewListEntity(fields)
    setStringField(emailField, "name", "email")
    setStringField(emailField, "type", "string")
    setStringField(emailField, "multiplicity", "list")

    val relationField = getNewListEntity(fields)
    populateAsEntityField(relationField, "relation", "address_book_relation", "address_book.main")
    setStringField(relationField, "multiplicity", "list")
}

fun populateAddressBookRelation(relation: MutableEntityModel<Resolved>) {
    setStringField(relation, "name", "address_book_relation")
    val fields = getListField(relation, "fields")

    val idField = getNewListEntity(fields)
    setStringField(idField, "name", "id")
    setStringField(idField, "type", "uuid")
    setBooleanField(idField, "is_key", true)

    val relationshipField = getNewListEntity(fields)
    setStringField(relationshipField, "name", "relationship")
    setStringField(relationshipField, "type", "enumeration")
    val relationshipEnumeration = getSingleEntity(relationshipField, "enumeration")
    setStringField(relationshipEnumeration, "name", "address_book_relationship")
    setStringField(relationshipEnumeration, "package", "address_book.main")

    val personField = getNewListEntity(fields)
    populateAsAssociationField(personField, "person", listOf("address_book", "person"))
}

fun populateMainEnumerations(enumerations: MutableListFieldModel<Resolved>) {
    val colorEnumeration = getNewListEntity(enumerations)
    populateEnumeration(
        colorEnumeration, "address_book_color", listOf(
            "violet",
            "indigo",
            "blue",
            "green",
            "yellow",
            "orange",
            "red"
        )
    )

    val relationshipEnumeration = getNewListEntity(enumerations)
    populateEnumeration(
        relationshipEnumeration, "address_book_relationship", listOf(
            "parent",
            "child",
            "spouse",
            "sibling",
            "family",
            "friend",
            "colleague"
        )
    )
}

private fun populateCityPackage(cityPackage: MutableEntityModel<Resolved>) {
    setStringField(cityPackage, "name", "address_book.city")
    setStringField(cityPackage, "info", "Schema for storing city information")
    val entities = getListField(cityPackage, "entities")
    populateCityEntities(entities)
}

fun populateCityEntities(entities: MutableListFieldModel<Resolved>) {
    val addressBookCity = getNewListEntity(entities)
    populateAddressBookCity(addressBookCity)
    val addressBookCityInfo = getNewListEntity(entities)
    populateAddressBookCityInfo(addressBookCityInfo)
}

fun populateAddressBookCity(city: MutableEntityModel<Resolved>) {
    setStringField(city, "name", "address_book_city")
    val fields = getListField(city, "fields")

    val nameField = getNewListEntity(fields)
    setStringField(nameField, "name", "name")
    setStringField(nameField, "info", "City name")
    setStringField(nameField, "type", "string")
    setBooleanField(nameField, "is_key", true)

    val stateField = getNewListEntity(fields)
    setStringField(stateField, "name", "state")
    setStringField(stateField, "info", "Name of the state in which the city is")
    setStringField(stateField, "type", "string")
    setBooleanField(stateField, "is_key", true)

    val countryField = getNewListEntity(fields)
    setStringField(countryField, "name", "country")
    setStringField(countryField, "info", "Name of the country in which the city is")
    setStringField(countryField, "type", "string")
    setBooleanField(countryField, "is_key", true)
}

fun populateAddressBookCityInfo(cityInfo: MutableEntityModel<Resolved>) {
    setStringField(cityInfo, "name", "address_book_city_info")
    val fields = getListField(cityInfo, "fields")

    val cityField = getNewListEntity(fields)
    populateAsEntityField(cityField, "city", "address_book_city", "address_book.city")
    setBooleanField(cityField, "is_key", true)

    val infoField = getNewListEntity(fields)
    setStringField(infoField, "name", "info")
    setStringField(infoField, "info", "Information about the city")
    setStringField(infoField, "type", "string")

    val relatedCityInfoField = getNewListEntity(fields)
    populateAsAssociationField(relatedCityInfoField, "related_city_info", listOf("address_book", "city_info"))
    setStringField(relatedCityInfoField, "multiplicity", "list")
}

// Helpers

private fun setStringField(entity: MutableBaseEntityModel<Resolved>, name: String, value: String) {
    val fieldModel = entity.getOrNewField(name) as? MutableSingleFieldModel ?: throw IllegalStateException()
    val valueModel = fieldModel.getOrNewValue() as? MutableScalarValueModel ?: throw IllegalStateException()
    valueModel.setValue(value)
}

private fun setBooleanField(entity: MutableBaseEntityModel<Resolved>, name: String, value: Boolean) {
    val fieldModel = entity.getOrNewField(name) as? MutableSingleFieldModel ?: throw IllegalStateException()
    val valueModel = fieldModel.getOrNewValue() as? MutableScalarValueModel ?: throw IllegalStateException()
    valueModel.setValue(value)
}

private fun getSingleEntity(
    entity: MutableBaseEntityModel<Resolved>,
    name: String
): MutableEntityModel<Resolved> {
    val singleField =
        entity.getOrNewField(name) as? MutableSingleFieldModel<Resolved> ?: throw IllegalStateException()
    return singleField.getOrNewValue() as? MutableEntityModel<Resolved> ?: throw IllegalStateException()
}

private fun getListField(
    entity: MutableBaseEntityModel<Resolved>,
    name: String
): MutableListFieldModel<Resolved> =
    entity.getOrNewField(name) as? MutableListFieldModel<Resolved> ?: throw IllegalStateException()

private fun getNewListEntity(listField: MutableListFieldModel<Resolved>): MutableEntityModel<Resolved> =
    listField.getNewValue() as? MutableEntityModel<Resolved> ?: throw IllegalStateException()

private fun addListString(listField: MutableListFieldModel<Resolved>, value: String) {
    val valueModel = listField.getNewValue() as? MutableScalarValueModel<Resolved> ?: throw IllegalStateException()
    valueModel.setValue(value)
}

// Meta-model specific helpers

private fun populateEnumeration(enumeration: MutableEntityModel<Resolved>, name: String, values: List<String>) {
    setStringField(enumeration, "name", name)
    val valuesField = getListField(enumeration, "values")
    values.forEach { value ->
        val valueEntity = getNewListEntity(valuesField)
        setStringField(valueEntity, "name", value)
    }
}

private fun populateAsAssociationField(
    field: MutableEntityModel<Resolved>,
    fieldName: String,
    path: List<String>
) {
    setStringField(field, "name", fieldName)
    setStringField(field, "type", "association")
    val associationField = getListField(field, "association")
    path.forEach { element -> addListString(associationField, element) }
}

private fun populateAsEntityField(
    field: MutableEntityModel<Resolved>,
    fieldName: String,
    entityName: String,
    entityPackageName: String
) {
    setStringField(field, "name", fieldName)
    setStringField(field, "type", "entity")
    val entity = getSingleEntity(field, "entity")
    setStringField(entity, "name", entityName)
    setStringField(entity, "package", entityPackageName)
}
