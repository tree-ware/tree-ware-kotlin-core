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
): DecodingStateMachine {
    val singleFieldModel = field as MutableSingleFieldModel
    return ScalarValueModelStateMachine(
        false,
        { singleFieldModel.getOrNewValue() as MutableScalarValueModel },
        stack
    )
}

private fun getPassword1wayStateMachine(
    field: MutableFieldModel,
    stack: DecodingStack,
    errors: MutableList<String>,
    multiAuxDecodingStateMachineFactory: MultiAuxDecodingStateMachineFactory
): DecodingStateMachine {
    val singleFieldModel = field as MutableSingleFieldModel
    return Password1wayModelStateMachine(
        false,
        { singleFieldModel.getOrNewValue() as MutablePassword1wayModel },
        stack,
        multiAuxDecodingStateMachineFactory
    )
}


private fun getPassword2wayStateMachine(
    field: MutableFieldModel,
    stack: DecodingStack,
    errors: MutableList<String>,
    multiAuxDecodingStateMachineFactory: MultiAuxDecodingStateMachineFactory
): DecodingStateMachine {
    val singleFieldModel = field as MutableSingleFieldModel
    return Password2wayModelStateMachine(
        false,
        { singleFieldModel.getOrNewValue() as MutablePassword2wayModel },
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
): DecodingStateMachine {
    val singleFieldModel = field as MutableSingleFieldModel
    return BaseEntityStateMachine(
        null,
        {
            val association = singleFieldModel.getOrNewValue() as MutableAssociationModel
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
            { setFieldModel.getOrNewValue() as MutableEntityModel },
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
            { singleFieldModel.getOrNewValue() as MutableEntityModel },
            stack,
            options,
            errors,
            multiAuxDecodingStateMachineFactory,
            "Entities must not be null; use empty object {} instead"
        )
    }

private fun wrapSetElementStateMachine(
    setFieldModel: MutableSetFieldModel,
    setElementStateMachine: ValueDecodingStateMachine,
    stack: DecodingStack,
    errors: MutableList<String>
): DecodingStateMachine = CollectionFieldModelStateMachine(setFieldModel, setElementStateMachine, stack, errors)