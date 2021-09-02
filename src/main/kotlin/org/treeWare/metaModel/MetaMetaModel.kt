package org.treeWare.metaModel

import org.treeWare.metaModel.validation.validate
import org.treeWare.model.core.MutableEntityModel
import org.treeWare.model.core.MutableListFieldModel
import org.treeWare.model.core.MutableModel
import org.treeWare.model.core.Resolved

// TODO(self-hosting): make this an enum
val multiplicityValues = listOf(
    "required",
    "optional",
    "list",
)

// TODO(self-hosting): make this an enum
val fieldTypeValues = listOf(
    "boolean",
    "byte",
    "short",
    "int",
    "long",
    "float",
    "double",
    "string",
    "password1way",
    "password2way",
    "uuid",
    "blob",
    "timestamp",
    "enumeration",
    "association",
    "entity",
)

private const val META_MODEL_MAIN_PACKAGE = "tree_ware_meta_model.main"

fun newMetaMetaModel(): MutableModel<Resolved> {
    val mainMeta = newMainMetaMeta()
    populateMain(mainMeta)
    val errors = validate(mainMeta)
    if (errors.isNotEmpty()) throw IllegalStateException("Meta-meta-model is not valid")
    return mainMeta
}

private fun populateMain(mainMeta: MutableModel<Resolved>) {
    newRootMetaMeta(mainMeta, "meta_model", "meta_model", "tree_ware_meta_model.main")
    val packagesMeta = newPackagesMetaMeta(mainMeta)
    populatePackages(packagesMeta)
}

private fun populatePackages(packagesMeta: MutableListFieldModel<Resolved>) {
    val mainPackage = newPackageMetaMeta(packagesMeta, META_MODEL_MAIN_PACKAGE)
    populateMainPackage(mainPackage)
}

private fun populateMainPackage(mainPackage: MutableEntityModel<Resolved>) {
    val entitiesMeta = newEntitiesMetaMeta(mainPackage)
    populateMainEntities(entitiesMeta)
    val enumerationsMeta = newEnumerationsMetaMeta(mainPackage)
    populateMainEnumerations(enumerationsMeta)
}

private fun populateMainEntities(entitiesMeta: MutableListFieldModel<Resolved>) {
    val metaModelEntityMeta = newEntityMetaMeta(entitiesMeta, "meta_model")
    populateMetaModelEntity(metaModelEntityMeta)
    val rootEntityMeta = newEntityMetaMeta(entitiesMeta, "root")
    populateRootEntity(rootEntityMeta)
    val packageEntityMeta = newEntityMetaMeta(entitiesMeta, "package")
    populatePackageEntity(packageEntityMeta)
    val enumerationEntityMeta = newEntityMetaMeta(entitiesMeta, "enumeration")
    populateEnumerationEntity(enumerationEntityMeta)
    val enumerationValueEntityMeta = newEntityMetaMeta(entitiesMeta, "enumeration_value")
    populateEnumerationValueEntity(enumerationValueEntityMeta)
    val entityEntityMeta = newEntityMetaMeta(entitiesMeta, "entity")
    populateEntityEntity(entityEntityMeta)
    val fieldEntityMeta = newEntityMetaMeta(entitiesMeta, "field")
    populateFieldEntity(fieldEntityMeta)
    val enumerationInfoEntityMeta = newEntityMetaMeta(entitiesMeta, "enumeration_info")
    populateEnumerationInfoEntity(enumerationInfoEntityMeta)
    val entityInfoEntityMeta = newEntityMetaMeta(entitiesMeta, "entity_info")
    populateEntityInfoEntity(entityInfoEntityMeta)
}

private fun populateMetaModelEntity(metaModelEntityMeta: MutableEntityModel<Resolved>) {
    val fields = newFieldsMetaMeta(metaModelEntityMeta)
    newEntityFieldMetaMeta(fields, "root", null, "root", META_MODEL_MAIN_PACKAGE)
    newEntityFieldMetaMeta(fields, "packages", null, "package", META_MODEL_MAIN_PACKAGE, "list")
}

private fun populateRootEntity(rootEntityMeta: MutableEntityModel<Resolved>) {
    val fields = newFieldsMetaMeta(rootEntityMeta)
    newPrimitiveFieldMetaMeta(fields, "name", null, "string")
    newPrimitiveFieldMetaMeta(fields, "info", null, "string", "optional")
    newPrimitiveFieldMetaMeta(fields, "entity", null, "string")
    newPrimitiveFieldMetaMeta(fields, "package", null, "string")
}

private fun populatePackageEntity(packageEntityMeta: MutableEntityModel<Resolved>) {
    val fields = newFieldsMetaMeta(packageEntityMeta)
    newPrimitiveFieldMetaMeta(fields, "name", null, "string", null, true)
    newPrimitiveFieldMetaMeta(fields, "info", null, "string", "optional")
    newEntityFieldMetaMeta(fields, "enumerations", null, "enumeration", META_MODEL_MAIN_PACKAGE, "list")
    newEntityFieldMetaMeta(fields, "entities", null, "entity", META_MODEL_MAIN_PACKAGE, "list")
}

private fun populateEnumerationEntity(enumerationEntityMeta: MutableEntityModel<Resolved>) {
    val fields = newFieldsMetaMeta(enumerationEntityMeta)
    newPrimitiveFieldMetaMeta(fields, "name", null, "string", null, true)
    newPrimitiveFieldMetaMeta(fields, "info", null, "string", "optional")
    newEntityFieldMetaMeta(fields, "values", null, "enumeration_value", META_MODEL_MAIN_PACKAGE, "list")
}

private fun populateEnumerationValueEntity(enumerationValueEntityMeta: MutableEntityModel<Resolved>) {
    val fields = newFieldsMetaMeta(enumerationValueEntityMeta)
    newPrimitiveFieldMetaMeta(fields, "name", null, "string", null, true)
    newPrimitiveFieldMetaMeta(fields, "info", null, "string", "optional")
}

private fun populateEntityEntity(entityEntityMeta: MutableEntityModel<Resolved>) {
    val fields = newFieldsMetaMeta(entityEntityMeta)
    newPrimitiveFieldMetaMeta(fields, "name", null, "string", null, true)
    newPrimitiveFieldMetaMeta(fields, "info", null, "string", "optional")
    newEntityFieldMetaMeta(fields, "fields", null, "field", META_MODEL_MAIN_PACKAGE, "list")
}

private fun populateFieldEntity(fieldEntityMeta: MutableEntityModel<Resolved>) {
    val fields = newFieldsMetaMeta(fieldEntityMeta)
    newPrimitiveFieldMetaMeta(fields, "name", null, "string", null, true)
    newPrimitiveFieldMetaMeta(fields, "info", null, "string", "optional")
    newEnumerationFieldMetaMeta(fields, "type", null, "field_type", META_MODEL_MAIN_PACKAGE)
    newEntityFieldMetaMeta(fields, "enumeration", null, "enumeration_info", META_MODEL_MAIN_PACKAGE, "optional")
    newPrimitiveFieldMetaMeta(fields, "association", null, "string", "list")
    newEntityFieldMetaMeta(fields, "entity", null, "entity_info", META_MODEL_MAIN_PACKAGE, "optional")
    newPrimitiveFieldMetaMeta(fields, "is_key", null, "boolean", "optional")
    newEnumerationFieldMetaMeta(fields, "multiplicity", null, "multiplicity", META_MODEL_MAIN_PACKAGE, "optional")

}

private fun populateEnumerationInfoEntity(enumerationInfoEntityMeta: MutableEntityModel<Resolved>) {
    val fields = newFieldsMetaMeta(enumerationInfoEntityMeta)
    newPrimitiveFieldMetaMeta(fields, "name", null, "string")
    newPrimitiveFieldMetaMeta(fields, "package", null, "string")
}

private fun populateEntityInfoEntity(entityInfoEntityMeta: MutableEntityModel<Resolved>) {
    val fields = newFieldsMetaMeta(entityInfoEntityMeta)
    newPrimitiveFieldMetaMeta(fields, "name", null, "string")
    newPrimitiveFieldMetaMeta(fields, "package", null, "string")
}

private fun populateMainEnumerations(enumerationsMeta: MutableListFieldModel<Resolved>) {
    populateFieldTypeEnumeration(enumerationsMeta)
    populateMultiplicityEnumeration(enumerationsMeta)
}

fun populateFieldTypeEnumeration(enumerationsMeta: MutableListFieldModel<Resolved>) {
    newEnumerationMetaMeta(
        enumerationsMeta,
        "field_type",
        null,
        fieldTypeValues.map { EnumerationValueMetaMeta(it) })
}

private fun populateMultiplicityEnumeration(enumerationsMeta: MutableListFieldModel<Resolved>) {
    newEnumerationMetaMeta(
        enumerationsMeta,
        "multiplicity",
        null,
        multiplicityValues.map { EnumerationValueMetaMeta(it) })
}