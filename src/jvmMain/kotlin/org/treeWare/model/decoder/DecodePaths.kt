package org.treeWare.model.decoder

import org.treeWare.metaModel.*
import org.treeWare.model.core.*
import org.treeWare.util.assertInDevMode
import java.io.Reader

// TODO(deepak-nulu): support aux-data

private const val PATH_SEPARATOR = "/"

fun decodePaths(reader: Reader, mainModel: MutableMainModel, pathValueSeparator: String = " = "): List<String> {
    val mainName = getMainName(mainModel)
    val errors = mutableListOf<String>()
    reader.forEachLine label@{ line ->
        val (path, value) = line.split(pathValueSeparator, limit = 2)
        val error = decodePath(path, value, mainModel, mainName)
        error?.also { errors.add(it) }
    }
    return errors
}

private fun decodePath(path: String, value: String, mainModel: MutableMainModel, mainName: String?): String? {
    if (!path.startsWith(PATH_SEPARATOR)) return "$path must be an absolute path"
    val pathParts = path.split(PATH_SEPARATOR) // Drop the empty string before the first separator
    val firstPart = pathParts[1] // pathParts[0] is the empty string before the first separator
    if (firstPart != mainName) return "$path does not start with $PATH_SEPARATOR$mainName"
    val lastPartIndex = pathParts.size - 1
    return decodePath(path, pathParts, 2, lastPartIndex, value, mainModel.getOrNewRoot())
}

private fun decodePath(
    path: String,
    pathParts: List<String>,
    partIndex: Int,
    lastPartIndex: Int,
    value: String,
    entity: MutableBaseEntityModel
): String? {
    assertInDevMode(partIndex <= lastPartIndex)
    val fieldName = pathParts[partIndex]
    val fieldId = "$fieldName at index $partIndex in $path"

    // TODO(deepak-nulu): change getOrNewField() to return null instead of throwing if a field is not found.
    val field = entity.getOrNewField(fieldName)
    val fieldType = getFieldType(field)
    val fieldMeta = field.meta ?: throw IllegalStateException("Meta-model is missing for field $fieldId")
    val multiplicity = getMultiplicityMeta(fieldMeta)
    // TODO(deepak-nulu): support list fields; without gaps in the index(?); with aux for each element
    if (multiplicity == Multiplicity.LIST) return "Field $fieldId is a list; lists are not yet supported"

    return if (partIndex < lastPartIndex) {
        if (fieldType != FieldType.COMPOSITION) "Intermediate field $fieldId must be a composition"
        else if (multiplicity == Multiplicity.SET) {
            val setField = field as MutableSetFieldModel
            decodeSetField(path, pathParts, partIndex + 1, lastPartIndex, value, fieldId, setField)
        } else {
            val singleField = field as MutableSingleFieldModel
            val nextEntity = singleField.getOrNewValue() as MutableEntityModel
            decodePath(path, pathParts, partIndex + 1, lastPartIndex, value, nextEntity)
        }
    } else {
        if (fieldType == FieldType.COMPOSITION) return "Last field $fieldId must not be a composition"
        val singleField = field as MutableSingleFieldModel
        // TODO(deepak-nulu): support all field types
        val primitive = newMutableValueModel(fieldMeta, singleField) as MutablePrimitiveModel
        singleField.setValue(primitive)
        primitive.setValue(value)
        null
    }
}

fun decodeSetField(
    path: String,
    pathParts: List<String>,
    partIndex: Int,
    lastPartIndex: Int,
    value: String,
    fieldId: String,
    setField: MutableSetFieldModel
): String? {
    val availablePartsCount = lastPartIndex - partIndex + 1
    val newEntity = setField.getNewValue() as MutableEntityModel
    val entityMeta = newEntity.meta
        ?: throw IllegalStateException("Meta-model is missing for entity of field $fieldId")
    val keyFieldsMeta = getKeyFieldsMeta(entityMeta)
    val keyFieldsCount = keyFieldsMeta.size
    if (keyFieldsCount > availablePartsCount) return "Found only $availablePartsCount of $keyFieldsCount keys for field $fieldId"

    // Populate the key fields in `newEntity`
    keyFieldsMeta.forEachIndexed { keyIndex, keyFieldMeta ->
        val keyFieldName = getMetaName(keyFieldMeta)
        val keyField = newEntity.getOrNewField(keyFieldName) as MutableSingleFieldModel
        val keyFieldType = getFieldType(keyField)
        if (keyFieldType == FieldType.COMPOSITION) return "Field $fieldId has composition keys; composition keys are not yet supported"
        val keyPrimitive = newMutableValueModel(keyFieldMeta, keyField) as MutablePrimitiveModel
        keyField.setValue(keyPrimitive)
        val keyValue = pathParts[partIndex + keyIndex]
        keyPrimitive.setValue(keyValue)
    }

    // Use `newEntity` if there is no matching entity in the set already; else use the matching entity.
    val existingEntity = setField.getValueMatching(newEntity) as MutableEntityModel?
    val nextEntity = if (existingEntity != null) existingEntity else {
        setField.addValue(newEntity)
        newEntity
    }
    return decodePath(path, pathParts, partIndex + keyFieldsCount, lastPartIndex, value, nextEntity)
}