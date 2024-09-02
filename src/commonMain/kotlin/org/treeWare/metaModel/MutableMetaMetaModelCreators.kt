package org.treeWare.metaModel

import io.github.z4kn4fein.semver.Version
import org.treeWare.model.core.*

// Functions for creating the meta-meta-model.

fun newMetaMeta(name: String): MutableEntityModel {
    val metaMeta = MutableEntityModel(null, null)
    newStringSingleField(metaMeta, "name", name)
    return metaMeta
}

fun newVersionMetaMeta(metaMeta: MutableEntityModel, semanticVersion: Version) {
    val version = newCompositionSingleField(metaMeta, "version")
    newStringSingleField(version, "semantic", semanticVersion.toString())
}

fun newRootMetaMeta(metaMeta: MutableEntityModel, entityName: String, packageName: String) {
    val root = newCompositionSingleField(metaMeta, "root")
    newStringSingleField(root, "entity", entityName)
    newStringSingleField(root, "package", packageName)
}

fun newPackagesMetaMeta(metaMeta: MutableEntityModel): MutableSetFieldModel {
    return newCompositionSetField(metaMeta, "packages")
}

fun newPackageMetaMeta(
    packagesMeta: MutableSetFieldModel,
    name: String,
    info: String? = null
): MutableEntityModel {
    val pkg = newCompositionSetElement(packagesMeta)
    newStringSingleField(pkg, "name", name)
    info?.also { newStringSingleField(pkg, "info", it) }
    packagesMeta.addValue(pkg)
    return pkg
}

fun newEntitiesMetaMeta(packageMeta: MutableEntityModel): MutableSetFieldModel =
    newCompositionSetField(packageMeta, "entities")

fun newEntityMetaMeta(
    entitiesMeta: MutableSetFieldModel,
    name: String,
    info: String? = null
): MutableEntityModel {
    val entity = newCompositionSetElement(entitiesMeta)
    newStringSingleField(entity, "name", name)
    info?.also { newStringSingleField(entity, "info", it) }
    entitiesMeta.addValue(entity)
    return entity
}

fun newFieldsMetaMeta(entityMeta: MutableEntityModel) =
    newCompositionSetField(entityMeta, "fields")

fun newEnumerationsMetaMeta(packageMeta: MutableEntityModel): MutableSetFieldModel =
    newCompositionSetField(packageMeta, "enumerations")

data class EnumerationValueMetaMeta(val name: String, val info: String? = null)

fun newEnumerationMetaMeta(
    enumerations: MutableSetFieldModel,
    name: String,
    info: String?,
    values: List<EnumerationValueMetaMeta>
) {
    val enumeration = newCompositionSetElement(enumerations)
    newStringSingleField(enumeration, "name", name)
    info?.also { newStringSingleField(enumeration, "info", it) }
    val valueList = newCompositionSetField(enumeration, "values")
    values.forEach { (valueName, valueInfo) ->
        val value = newCompositionSetElement(valueList)
        newStringSingleField(value, "name", valueName)
        valueInfo?.also { newStringSingleField(value, "info", it) }
        valueList.addValue(value)
    }
    enumerations.addValue(enumeration)
}

fun newPrimitiveFieldMetaMeta(
    parent: MutableSetFieldModel,
    name: String,
    info: String?,
    type: String,
    multiplicity: String? = null,
    isKey: Boolean = false
): MutableEntityModel = newFieldMetaMeta(parent, name, info, type, multiplicity, isKey)

fun newEnumerationFieldMetaMeta(
    parent: MutableSetFieldModel,
    name: String,
    info: String?,
    enumerationName: String,
    packageName: String,
    multiplicity: String? = null,
    isKey: Boolean = false
): MutableEntityModel {
    val fieldMeta = newFieldMetaMeta(parent, name, info, "enumeration", multiplicity, isKey)
    val entityMeta = newCompositionSingleField(fieldMeta, "enumeration")
    newStringSingleField(entityMeta, "name", enumerationName)
    newStringSingleField(entityMeta, "package", packageName)
    return fieldMeta
}

fun newCompositionFieldMetaMeta(
    parent: MutableSetFieldModel,
    name: String,
    info: String?,
    entityName: String,
    packageName: String,
    multiplicity: String? = null,
    isKey: Boolean = false
): MutableEntityModel {
    val fieldMeta = newFieldMetaMeta(parent, name, info, "composition", multiplicity, isKey)
    val entityMeta = newCompositionSingleField(fieldMeta, "composition")
    newStringSingleField(entityMeta, "entity", entityName)
    newStringSingleField(entityMeta, "package", packageName)
    return fieldMeta
}

private fun newFieldMetaMeta(
    parent: MutableSetFieldModel,
    name: String,
    info: String?,
    type: String,
    multiplicity: String?,
    isKey: Boolean
): MutableEntityModel {
    val fieldMeta = MutableEntityModel(namedEntityMeta, parent)
    newStringSingleField(fieldMeta, "name", name)
    info?.also { newStringSingleField(fieldMeta, "info", it) }
    newEnumerationSingleField(fieldMeta, "type", type)
    multiplicity?.also { newEnumerationSingleField(fieldMeta, "multiplicity", it) }
    newBooleanSingleField(fieldMeta, "is_key", isKey)
    parent.addValue(fieldMeta)
    return fieldMeta
}

// Meta-meta-meta-models for field types.
//
// While a full-blown meta-meta-meta-model is not needed, there is still a need
// for meta-meta-meta-models for primitive field types.
//
// To create meta-meta-meta-model instances for field types, parent instances
// need to be passed to the model constructors. So dummy parent instances are
// created and used.

private val dummyRoot = MutableEntityModel(null, null)
private val dummyField = MutableSingleFieldModel(null, dummyRoot, MutablePrimitiveModel.fieldValueFactory)
private val dummySetField = MutableSetFieldModel(null, dummyRoot, MutableEntityModel.fieldValueFactory)

private val stringFieldMeta = getFieldTypeMeta("string")
private val booleanFieldMeta = getFieldTypeMeta("boolean")

private fun getFieldTypeMeta(type: String): MutableEntityModel {
    val fieldMeta = MutableEntityModel(null, dummyField)
    newEnumerationSingleField(fieldMeta, "type", type)
    return fieldMeta
}

/**
 * Meta-meta-meta-model for package, entity, and enumeration meta-meta-models.
 * It is needed for indicating that the "name" field in these meta-meta-models is a key field.
 * See getKeyFieldsMeta() in MetaModelGetters.kt for how key fields are determined from a meta.
 */
private val namedEntityMeta = getNamedEntityMeta()

private fun getNamedEntityMeta(): MutableEntityModel {
    val entityMeta = newEntityMetaMeta(dummySetField, "namedEntity")
    val fieldsMeta = newFieldsMetaMeta(entityMeta)
    val fieldMeta = newFieldMetaMeta(fieldsMeta, "name", null, "string", null, true)
    // Resolved aux must be added to `meta` as expected in getKeyFieldsMeta() in MetaModelGetters.kt.
    val resolvedAux = Resolved("/tree_ware_meta_model.main/entity")
    resolvedAux.sortedKeyFieldsMetaInternal.add(fieldMeta)
    entityMeta.setAux(RESOLVED_AUX, resolvedAux)
    return entityMeta
}

// Helpers
// NOTE: model setters require meta-models. Since we don't have
// meta-meta-meta-models, we cannot use model setters to create
// meta-meta-models.

private fun newCompositionSingleField(entity: MutableBaseEntityModel, name: String): MutableEntityModel {
    val field = MutableSingleFieldModel(null, entity, MutableEntityModel.fieldValueFactory)
    entity.fields[name] = field
    val value = MutableEntityModel(null, field)
    field.value = value
    return value
}

private fun newStringSingleField(entity: MutableBaseEntityModel, name: String, value: String) {
    val field = MutableSingleFieldModel(stringFieldMeta, entity, MutablePrimitiveModel.fieldValueFactory)
    entity.fields[name] = field
    val primitive = MutablePrimitiveModel(field, value)
    field.value = primitive
}

private fun newBooleanSingleField(entity: MutableBaseEntityModel, name: String, value: Boolean) {
    val field = MutableSingleFieldModel(booleanFieldMeta, entity, MutablePrimitiveModel.fieldValueFactory)
    entity.fields[name] = field
    val primitive = MutablePrimitiveModel(field, value)
    field.value = primitive
}

private fun newEnumerationSingleField(entity: MutableBaseEntityModel, name: String, value: String) {
    val field = MutableSingleFieldModel(null, entity, MutableEnumerationModel.fieldValueFactory)
    entity.fields[name] = field
    val enumeration = MutableEnumerationModel(field, value)
    field.value = enumeration
}

private fun newCompositionSetField(entity: MutableBaseEntityModel, name: String): MutableSetFieldModel {
    val field = MutableSetFieldModel(null, entity, MutableEntityModel.fieldValueFactory)
    entity.fields[name] = field
    return field
}

private fun newCompositionSetElement(setField: MutableSetFieldModel): MutableEntityModel {
    return MutableEntityModel(namedEntityMeta, setField)
}