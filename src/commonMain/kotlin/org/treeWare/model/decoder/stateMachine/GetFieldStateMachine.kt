package org.treeWare.model.decoder.stateMachine

import org.treeWare.metaModel.FieldType
import org.treeWare.model.core.*
import org.treeWare.model.decoder.ModelDecoderOptions

fun getFieldStateMachine(
    field: MutableFieldModel,
    errors: MutableList<String>,
    stack: DecodingStack,
    options: ModelDecoderOptions,
    multiAuxDecodingStateMachineFactory: MultiAuxDecodingStateMachineFactory
): DecodingStateMachine = when (getFieldType(field)) {
    FieldType.PASSWORD1WAY -> getPassword1wayStateMachine(field, stack, errors, multiAuxDecodingStateMachineFactory)
    FieldType.PASSWORD2WAY -> getPassword2wayStateMachine(field, stack, errors, multiAuxDecodingStateMachineFactory)
    FieldType.ASSOCIATION -> getAssociationStateMachine(
        field,
        stack,
        options,
        errors,
        multiAuxDecodingStateMachineFactory
    )
    FieldType.COMPOSITION -> getCompositionStateMachine(
        field,
        stack,
        options,
        errors,
        multiAuxDecodingStateMachineFactory
    )
    else -> getScalarStateMachine(field, stack, errors, multiAuxDecodingStateMachineFactory)
}

private fun getScalarStateMachine(
    field: MutableFieldModel,
    stack: DecodingStack,
    errors: MutableList<String>,
    multiAuxDecodingStateMachineFactory: MultiAuxDecodingStateMachineFactory
): DecodingStateMachine =
    if (isListField(field)) (field as MutableListFieldModel).let { listFieldModel ->
        val listElementStateMachine = ScalarValueModelStateMachine(
            true,
            {
                val value = newMutableValueModel(field.meta, listFieldModel) as MutableScalarValueModel
                listFieldModel.addValue(value)
                value
            },
            stack
        )
        wrapListElementStateMachine(
            field,
            listElementStateMachine,
            isWrappedElements = true,
            stack,
            errors,
            multiAuxDecodingStateMachineFactory
        )
    } else (field as MutableSingleFieldModel).let { singleFieldModel ->
        ScalarValueModelStateMachine(
            false,
            {
                val value = newMutableValueModel(field.meta, singleFieldModel) as MutableScalarValueModel
                singleFieldModel.setValue(value)
                value
            },
            stack
        )
    }

private fun getPassword1wayStateMachine(
    field: MutableFieldModel,
    stack: DecodingStack,
    errors: MutableList<String>,
    multiAuxDecodingStateMachineFactory: MultiAuxDecodingStateMachineFactory
): DecodingStateMachine =
    if (isListField(field)) (field as MutableListFieldModel).let { listFieldModel ->
        val listElementStateMachine = Password1wayModelStateMachine(
            true,
            {
                val value = newMutableValueModel(field.meta, listFieldModel) as MutablePassword1wayModel
                listFieldModel.addValue(value)
                value
            },
            stack,
            multiAuxDecodingStateMachineFactory
        )
        wrapListElementStateMachine(
            listFieldModel,
            listElementStateMachine,
            isWrappedElements = false,
            stack,
            errors,
            multiAuxDecodingStateMachineFactory
        )
    } else (field as MutableSingleFieldModel).let { singleFieldModel ->
        Password1wayModelStateMachine(
            false,
            {
                val value = newMutableValueModel(field.meta, singleFieldModel) as MutablePassword1wayModel
                singleFieldModel.setValue(value)
                value
            },
            stack,
            multiAuxDecodingStateMachineFactory
        )
    }


private fun getPassword2wayStateMachine(
    field: MutableFieldModel,
    stack: DecodingStack,
    errors: MutableList<String>,
    multiAuxDecodingStateMachineFactory: MultiAuxDecodingStateMachineFactory
): DecodingStateMachine =
    if (isListField(field)) (field as MutableListFieldModel).let { listFieldModel ->
        val listElementStateMachine = Password2wayModelStateMachine(
            true,
            {
                val value = newMutableValueModel(field.meta, listFieldModel) as MutablePassword2wayModel
                listFieldModel.addValue(value)
                value
            },
            stack,
            multiAuxDecodingStateMachineFactory
        )
        wrapListElementStateMachine(
            listFieldModel,
            listElementStateMachine,
            isWrappedElements = false,
            stack,
            errors,
            multiAuxDecodingStateMachineFactory
        )
    } else (field as MutableSingleFieldModel).let { singleFieldModel ->
        Password2wayModelStateMachine(
            false,
            {
                val value = newMutableValueModel(field.meta, singleFieldModel) as MutablePassword2wayModel
                singleFieldModel.setValue(value)
                value
            },
            stack,
            multiAuxDecodingStateMachineFactory
        )
    }

private fun getAssociationStateMachine(
    field: MutableFieldModel,
    stack: DecodingStack,
    options: ModelDecoderOptions,
    errors: MutableList<String>,
    multiAuxDecodingStateMachineFactory: MultiAuxDecodingStateMachineFactory
): DecodingStateMachine =
    if (isListField(field)) (field as MutableListFieldModel).let { listFieldModel ->
        val listElementStateMachine = BaseEntityStateMachine(
            listFieldModel,
            {
                val association = newMutableValueModel(field.meta, listFieldModel) as MutableAssociationModel
                listFieldModel.addValue(association)
                association.value
            },
            stack,
            options,
            errors,
            multiAuxDecodingStateMachineFactory,
            null
        )
        wrapListElementStateMachine(
            listFieldModel,
            listElementStateMachine,
            isWrappedElements = false,
            stack,
            errors,
            multiAuxDecodingStateMachineFactory
        )
    } else (field as MutableSingleFieldModel).let { singleFieldModel ->
        BaseEntityStateMachine(
            null,
            {
                val association = newMutableValueModel(field.meta, singleFieldModel) as MutableAssociationModel
                singleFieldModel.setValue(association)
                association.value
            },
            stack,
            options,
            errors,
            multiAuxDecodingStateMachineFactory,
            null
        )
    }

private fun getCompositionStateMachine(
    field: MutableFieldModel,
    stack: DecodingStack,
    options: ModelDecoderOptions,
    errors: MutableList<String>,
    multiAuxDecodingStateMachineFactory: MultiAuxDecodingStateMachineFactory
): DecodingStateMachine =
    if (isSetField(field)) (field as MutableSetFieldModel).let { setFieldModel ->
        val setElementStateMachine = BaseEntityStateMachine(
            setFieldModel,
            { newMutableValueModel(field.meta, setFieldModel) as MutableEntityModel },
            stack,
            options,
            errors,
            multiAuxDecodingStateMachineFactory,
            "Entities must not be null; use empty object {} instead"
        )
        wrapSetElementStateMachine(setFieldModel, setElementStateMachine, stack, errors)
    } else (field as MutableSingleFieldModel).let { singleFieldModel ->
        BaseEntityStateMachine(
            null,
            {
                val value = newMutableValueModel(field.meta, singleFieldModel) as MutableEntityModel
                singleFieldModel.setValue(value)
                value
            },
            stack,
            options,
            errors,
            multiAuxDecodingStateMachineFactory,
            "Entities must not be null; use empty object {} instead"
        )
    }

private fun wrapListElementStateMachine(
    listFieldModel: MutableListFieldModel,
    listElementStateMachine: ValueDecodingStateMachine,
    isWrappedElements: Boolean,
    stack: DecodingStack,
    errors: MutableList<String>,
    multiAuxDecodingStateMachineFactory: MultiAuxDecodingStateMachineFactory
): DecodingStateMachine {
    val elementStateMachine =
        if (!isWrappedElements) listElementStateMachine
        else ValueAndAuxStateMachine(true, listElementStateMachine, stack, multiAuxDecodingStateMachineFactory)
    return CollectionFieldModelStateMachine(listFieldModel, elementStateMachine, stack, errors)
}

private fun wrapSetElementStateMachine(
    setFieldModel: MutableSetFieldModel,
    setElementStateMachine: ValueDecodingStateMachine,
    stack: DecodingStack,
    errors: MutableList<String>
): DecodingStateMachine = CollectionFieldModelStateMachine(setFieldModel, setElementStateMachine, stack, errors)