package org.treeWare.model

import org.treeWare.metaModel.addressBookMetaModel
import org.treeWare.model.core.*

fun newAddressBook(auxName: String): MainModel {
    val main = MutableMainModel(addressBookMetaModel)
    main.setAux(auxName, "Aux at address_book level")
    val root = main.getOrNewRoot()
    setStringSingleField(root, "name", "Address Book")
    setTimestampSingleField(root, "last_updated", 1587147731L)

    val settingsField = getOrNewMutableSingleField(root, "settings")
    settingsField.setAux(auxName, "Aux for settings")
    val settings = getOrNewMutableSingleEntity(root, "settings")
    setBooleanSingleField(settings, "last_name_first", true)
    setBooleanSingleField(settings, "encrypt_hero_name", false)
    val cardColors = getOrNewMutableListField(settings, "card_colors")
    addEnumerationListFieldElement(cardColors, "orange").setAux(auxName, "Aux for scalar element in list")
    addEnumerationListFieldElement(cardColors, "green").setAux(auxName, "Aux for list element green")
    addEnumerationListFieldElement(cardColors, "blue").setAux(auxName, "Aux for another scalar list element")

    val groups = getOrNewMutableSetField(root, "groups")
    val dc = getNewMutableSetEntity(groups)
    setStringSingleField(dc, "name", "DC")
    groups.addValue(dc)
    val dcSubGroups = getOrNewMutableSetField(dc, "sub_groups")
    val superman = getNewMutableSetEntity(dcSubGroups)
    setStringSingleField(superman, "name", "Superman")
    dcSubGroups.addValue(superman)

    val marvel = getNewMutableSetEntity(groups)
    setStringSingleField(marvel, "name", "Marvel")
    groups.addValue(marvel)

    val persons = getOrNewMutableSetField(root, "person")
    val clark = getNewMutableSetEntity(persons)
    clark.setAux(auxName, "Aux for Clark")
    setUuidSingleField(clark, "id", "cc477201-48ec-4367-83a4-7fdbd92f8a6f")
    setStringSingleField(clark, "first_name", "Clark")
    setStringSingleField(clark, "last_name", "Kent")
    setStringSingleField(clark, "hero_name", "Superman")
    val clarkRelations = getOrNewMutableSetField(clark, "relation")
    clarkRelations.setAux(auxName, "Aux for Clark's relation list")
    val clarkRelationToLois = addRelation(
        clarkRelations,
        "05ade278-4b44-43da-a0cc-14463854e397",
        "colleague",
        "a8aacf55-7810-4b43-afe5-4344f25435fd",
        auxName,
        "Aux for Clark's colleague Lois",
        "Some aux for relationship field",
        "Aux for association to Clark's colleague Lois"
    )
    clarkRelations.addValue(clarkRelationToLois)

    setGroup(clark, auxName, "Aux for Clark's group", "DC", "Superman")
    persons.addValue(clark)

    val lois = getNewMutableSetEntity(persons)
    setUuidSingleField(lois, "id", "a8aacf55-7810-4b43-afe5-4344f25435fd")
    setStringSingleField(lois, "first_name", "Lois")
    setStringSingleField(lois, "last_name", "Lane")
    val loisRelations = getOrNewMutableSetField(lois, "relation")
    val loisRelationToClark = addRelation(
        loisRelations,
        "16634916-8f83-4376-ad42-37038e108a0b",
        "colleague",
        "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
        auxName,
        null,
        null,
        "Aux for association to Lois' colleague Clark"
    )
    loisRelations.addValue(loisRelationToClark)
    setGroup(lois, auxName, "Aux for Lois' group", "DC", "Superman")
    persons.addValue(lois)

    val cityInfoSet = getOrNewMutableSetField(root, "city_info")
    cityInfoSet.setAux(auxName, "Aux for city info list")

    val newYorkCityInfo = getNewMutableSetEntity(cityInfoSet)
    val newYorkCityField = getOrNewMutableSingleField(newYorkCityInfo, "city")
    newYorkCityField.setAux(auxName, "Aux for 1:1 composition key inside a list")
    addCity(newYorkCityInfo, "New York City", "New York", "United States of America")
    setStringSingleField(
        newYorkCityInfo,
        "info",
        "One of the most populous and most densely populated major city in USA"
    ).setAux(auxName, "Aux for info")
    val newYorkRelated = getOrNewMutableListField(newYorkCityInfo, "related_city_info")
    newYorkRelated.setAux(auxName, "Aux for related city list")
    addRelatedCity(
        newYorkRelated,
        "Albany",
        "New York",
        "United States of America",
        auxName,
        "Aux for association element in list"
    )
    addRelatedCity(newYorkRelated, "Princeton", "New Jersey", "United States of America", auxName)
    addRelatedCity(
        newYorkRelated,
        "San Francisco",
        "California",
        "United States of America",
        auxName,
        "Aux for association element in list after an element without aux"
    )
    cityInfoSet.addValue(newYorkCityInfo)

    val albanyCityInfo = getNewMutableSetEntity(cityInfoSet)
    setStringSingleField(albanyCityInfo, "info", "Capital of New York state")
    addCity(albanyCityInfo, "Albany", "New York", "United States of America")
    val albanyRelated = getOrNewMutableListField(albanyCityInfo, "related_city_info")
    addRelatedCity(albanyRelated, "New York City", "New York", "United States of America", auxName)
    cityInfoSet.addValue(albanyCityInfo)

    val princetonCityInfo = getNewMutableSetEntity(cityInfoSet)
    setStringSingleField(princetonCityInfo, "info", "Home of Princeton University")
    getOrNewMutableListField(princetonCityInfo, "related_city_info")
    addCity(princetonCityInfo, "Princeton", "New Jersey", "United States of America")
    cityInfoSet.addValue(princetonCityInfo)

    val sanFranciscoCityInfo = getNewMutableSetEntity(cityInfoSet)
    setStringSingleField(sanFranciscoCityInfo, "info", "The cultural and financial center of Northern California")
    addCity(sanFranciscoCityInfo, "San Francisco", "California", "United States of America")
    cityInfoSet.addValue(sanFranciscoCityInfo)

    return main
}

fun addRelation(
    relations: MutableSetFieldModel,
    relationId: String,
    relationship: String,
    personId: String?,
    auxName: String,
    relationAux: String? = null,
    relationshipAux: String? = null,
    personAux: String? = null
): MutableEntityModel {
    val relation = getNewMutableSetEntity(relations)
    relationAux?.also { relation.setAux(auxName, it) }
    setUuidSingleField(relation, "id", relationId)
    val relationshipField = setEnumerationSingleField(relation, "relationship", relationship)
    relationshipAux?.also { relationshipField.setAux(auxName, it) }
    personId?.also {
        val association = getOrNewMutableSingleAssociation(relation, "person")
        personAux?.also { association.setAux(auxName, it) }
        val associationPersons = getOrNewMutableSetField(association.value, "person")
        val associationPersonsPerson = getNewMutableSetEntity(associationPersons)
        setUuidSingleField(associationPersonsPerson, "id", personId)
        associationPersons.addValue(associationPersonsPerson)
    }
    return relation
}

fun setGroup(person: MutableEntityModel, auxName: String, aux: String?, vararg groupNames: String) {
    if (groupNames.isEmpty()) return
    val association = getOrNewMutableSingleAssociation(person, "group")
    aux?.also { association.setAux(auxName, it) }
    var entity = association.value
    groupNames.forEachIndexed { index, groupName ->
        val fieldName = if (index == 0) "groups" else "sub_groups"
        val field = getOrNewMutableSetField(entity, fieldName)
        entity = getNewMutableSetEntity(field)
        setStringSingleField(entity, "name", groupName)
        field.addValue(entity)
    }
}

fun addCity(parentEntity: MutableEntityModel, name: String, state: String, country: String) {
    val city = getOrNewMutableSingleEntity(parentEntity, "city")
    setStringSingleField(city, "name", name)
    setStringSingleField(city, "state", state)
    setStringSingleField(city, "country", country)
}

fun addRelatedCity(
    relatedList: MutableListFieldModel,
    name: String,
    state: String,
    country: String,
    auxName: String,
    aux: String? = null
) {
    val relatedAssociation = relatedList.getNewValue() as MutableAssociationModel
    aux?.also { relatedAssociation.setAux(auxName, it) }
    val cityInfoSet = getOrNewMutableSetField(relatedAssociation.value, "city_info")
    val cityInfo = getNewMutableSetEntity(cityInfoSet)
    addCity(cityInfo, name, state, country)
    cityInfoSet.addValue(cityInfo)
}