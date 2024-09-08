package org.treeWare.model.decoder

import okio.BufferedSource
import org.treeWare.metaModel.*
import org.treeWare.model.core.*

// TODO(deepak-nulu): support aux-data

private const val PATH_SEPARATOR = '/'
private const val ESCAPE_CHARACTER = '\\'

private const val WILDCARD = "*"
private const val SUB_TREE_WILDCARD = "**"

fun decodePaths(source: BufferedSource, model: MutableEntityModel, pathValueSeparator: String = " = "): List<String> {
    val errors = mutableListOf<String>()
    var line = source.readUtf8Line()
    while (line != null) {
        val (path, value) = line.split(pathValueSeparator, limit = 2)
        when (val result = decodePath(path, value, model)) {
            is DecodePathResult.Error -> errors.add(result.error)
            else -> {}
        }
        line = source.readUtf8Line()
    }
    return errors
}

sealed interface DecodePathResult {
    interface Element : DecodePathResult {
        val element: MutableElementModel
        val trailingWildcards: Int
    }

    /** A field pointed to by the path. */
    data class Field(override val element: MutableFieldModel, override val trailingWildcards: Int = 0) : Element

    /** A set-field entity pointed to by the path. */
    data class Entity(override val element: MutableEntityModel, override val trailingWildcards: Int = 0) : Element

    data class Error(val error: String) : DecodePathResult
}

fun decodePath(path: String, value: String?, model: MutableEntityModel): DecodePathResult {
    if (!path.startsWith(PATH_SEPARATOR)) return DecodePathResult.Error("$path must be an absolute path")
    if (path.length == 1) return DecodePathResult.Entity(model, 0) // path is "/"
    if (path.endsWith(PATH_SEPARATOR)) return DecodePathResult.Error("`$path` must not end with $PATH_SEPARATOR")
    val pathParts = splitEscapedPath(path)
    val firstPartIndex = 1  // index 0 is the empty string before the first separator
    val lastPartIndex = pathParts.size - 1
    val trailingWildcards = getTrailingWildcards(pathParts, 0, lastPartIndex)
    return if (trailingWildcards != null) DecodePathResult.Entity(model, trailingWildcards)
    else decodePath(path, pathParts, firstPartIndex, lastPartIndex, value, model)
}

private fun decodePath(
    path: String,
    pathParts: List<String>,
    partIndex: Int,
    lastPartIndex: Int,
    value: String?,
    entity: MutableEntityModel
): DecodePathResult {
    val fieldName = pathParts[partIndex]
    val fieldId = getFieldId(fieldName, partIndex, path)

    val field = runCatching { entity.getOrNewField(fieldName) }.getOrNull()
        ?: return DecodePathResult.Error("Unknown field $fieldId")
    val fieldType = getFieldType(field)
    if (partIndex < lastPartIndex && fieldType != FieldType.COMPOSITION) return DecodePathResult.Error("Intermediate field $fieldId must be a composition")

    val fieldMeta = field.meta ?: throw IllegalStateException("Meta-model is missing for field $fieldId")
    return when (getMultiplicityMeta(fieldMeta)) {
        Multiplicity.REQUIRED,
        Multiplicity.OPTIONAL -> decodeSingleField(
            path,
            pathParts,
            partIndex,
            lastPartIndex,
            value,
            fieldId,
            field as MutableSingleFieldModel,
            fieldType
        )
        Multiplicity.SET -> decodeSetField(
            path,
            pathParts,
            partIndex,
            lastPartIndex,
            value,
            fieldId,
            field as MutableSetFieldModel
        )
    }
}

// TODO(deepak-nulu): support all field types
private fun decodeSingleField(
    path: String,
    pathParts: List<String>,
    partIndex: Int,
    lastPartIndex: Int,
    value: String?,
    fieldId: String,
    singleField: MutableSingleFieldModel,
    fieldType: FieldType,
): DecodePathResult = when (fieldType) {
    FieldType.BOOLEAN,
    FieldType.UINT8,
    FieldType.UINT16,
    FieldType.UINT32,
    FieldType.UINT64,
    FieldType.INT8,
    FieldType.INT16,
    FieldType.INT32,
    FieldType.INT64,
    FieldType.FLOAT,
    FieldType.DOUBLE,
    FieldType.BIG_INTEGER,
    FieldType.BIG_DECIMAL,
    FieldType.TIMESTAMP,
    FieldType.STRING,
    FieldType.UUID,
    FieldType.BLOB -> setPrimitive(singleField, value)
    FieldType.PASSWORD1WAY -> TODO()
    FieldType.PASSWORD2WAY -> TODO()
    FieldType.ALIAS -> TODO()
    FieldType.ENUMERATION -> setEnumeration(singleField, value)
    FieldType.ASSOCIATION -> TODO()
    FieldType.COMPOSITION -> {
        val trailingWildcards = getTrailingWildcards(pathParts, partIndex, lastPartIndex)
        if (trailingWildcards != null) {
            if (value == null) DecodePathResult.Field(singleField, trailingWildcards)
            else DecodePathResult.Error("Cannot assign a value to composition $fieldId")
        } else {
            val nextEntity = singleField.getOrNewValue() as MutableEntityModel
            decodePath(path, pathParts, partIndex + 1, lastPartIndex, value, nextEntity)
        }
    }
}

private fun setPrimitive(
    singleField: MutableSingleFieldModel,
    value: String?
): DecodePathResult {
    if (value != null) {
        val primitive = singleField.getOrNewValue() as MutablePrimitiveModel
        primitive.setValue(value)
    }
    return DecodePathResult.Field(singleField)
}

private fun setEnumeration(
    singleField: MutableSingleFieldModel,
    value: String?
): DecodePathResult {
    if (value != null) {
        val enumeration = singleField.getOrNewValue() as MutableEnumerationModel
        enumeration.setValue(value)
    }
    return DecodePathResult.Field(singleField)
}

private fun decodeSetField(
    path: String,
    pathParts: List<String>,
    partIndex: Int,
    lastPartIndex: Int,
    value: String?,
    fieldId: String,
    setField: MutableSetFieldModel
): DecodePathResult {
    val trailingWildcards = getTrailingWildcards(pathParts, partIndex, lastPartIndex)
    if (trailingWildcards != null) return DecodePathResult.Field(setField, trailingWildcards)

    val availablePartsCount = lastPartIndex - partIndex
    val newEntity = setField.getOrNewValue() as MutableEntityModel
    val entityMeta = newEntity.meta ?: throw IllegalStateException("Meta-model is missing for entity of field $fieldId")
    val keyFieldsMeta = getKeyFieldsMeta(entityMeta)
    val keyFieldsCount = keyFieldsMeta.size
    if (keyFieldsCount > availablePartsCount) return DecodePathResult.Error("Found only $availablePartsCount of $keyFieldsCount keys for field $fieldId")

    // Populate the key fields in `newEntity`
    val firstKeyIndex = partIndex + 1
    keyFieldsMeta.forEachIndexed { keyIndex, keyFieldMeta ->
        val keyFieldName = getMetaName(keyFieldMeta)
        val keyField = newEntity.getOrNewField(keyFieldName) as MutableSingleFieldModel
        val keyFieldType = getFieldType(keyField)
        if (keyFieldType == FieldType.COMPOSITION) return DecodePathResult.Error("Field $fieldId has composition keys; composition keys are not yet supported")
        val keyPartIndex = firstKeyIndex + keyIndex
        val keyValue = pathParts[keyPartIndex]
        if (keyValue == SUB_TREE_WILDCARD) {
            val keyId = getFieldId(SUB_TREE_WILDCARD, keyPartIndex, path)
            return DecodePathResult.Error("Sub-tree wildcard $keyId is invalid in the middle of a path")
        }
        if (keyValue == WILDCARD) return@forEachIndexed
        val keyPrimitive = keyField.getOrNewValue() as MutablePrimitiveModel
        keyPrimitive.setValue(unescapeKey(keyValue))
    }

    // Use `newEntity` if there is no matching entity in the set already; else use the matching entity.
    // TODO(cleanup): need a getOrNew() method with key values as parameters.
    val existingEntity = setField.getValueMatching(newEntity) as MutableEntityModel?
    val nextEntity = if (existingEntity != null) existingEntity else {
        setField.addValue(newEntity)
        newEntity
    }
    val lastKeyIndex = partIndex + keyFieldsCount
    val nextTrailingWildcards = getTrailingWildcards(pathParts, lastKeyIndex, lastPartIndex)
    return if (nextTrailingWildcards != null) DecodePathResult.Entity(nextEntity, nextTrailingWildcards)
    else decodePath(path, pathParts, lastKeyIndex + 1, lastPartIndex, value, nextEntity)
}

/** Split a path with escaped keys without unescaping the keys. */
private fun splitEscapedPath(escapedPath: String): List<String> {
    val splits = mutableListOf<String>()
    val builder = StringBuilder()
    var i = 0
    while (i < escapedPath.length) {
        when (val character = escapedPath[i]) {
            ESCAPE_CHARACTER -> {
                // Add this character and the next since the next is escaped.
                builder.append(character)
                ++i
                builder.append(escapedPath[i])
            }
            PATH_SEPARATOR -> {
                // Add the current split and clear the builder for the next split.
                splits.add(builder.toString())
                builder.clear()
            }
            else -> builder.append(character)
        }
        ++i
    }
    splits.add(builder.toString())
    return splits
}

private fun unescapeKey(escapedKey: String): String {
    val builder = StringBuilder()
    var i = 0
    while (i < escapedKey.length) {
        when (val character = escapedKey[i]) {
            ESCAPE_CHARACTER -> {
                // Drop the escape character and add the next character.
                ++i
                builder.append(escapedKey[i])
            }
            else -> builder.append(character)
        }
        ++i
    }
    return builder.toString()
}

private fun getTrailingWildcards(pathParts: List<String>, partIndex: Int, lastPartIndex: Int): Int? {
    return when (val remainingPartCount = lastPartIndex - partIndex) {
        0 -> 0
        1 -> when (pathParts[lastPartIndex]) {
            WILDCARD -> 1
            SUB_TREE_WILDCARD -> 2
            else -> null
        }
        else -> null
    }
}

private fun getFieldId(fieldName: String, partIndex: Int, path: String) = "`$fieldName` at index $partIndex in `$path`"