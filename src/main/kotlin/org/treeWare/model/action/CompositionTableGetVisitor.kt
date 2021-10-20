package org.treeWare.model.action

import org.treeWare.metaModel.getMetaName
import org.treeWare.model.core.*
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.model.traversal.dispatchVisit

// IMPLEMENTATION: ./Get.md

// NOTE: this class uses typecasts.
// Not a fan of typecasting, but using it inside the framework to avoid
// code duplication. The framework will be well tested, so making an
// exception for the framework, at least for now.

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
class CompositionTableGetVisitor<MappingAux>(
    private val delegate: CompositionTableGetVisitorDelegate<MappingAux>
) : GetVisitor {
    private val fieldNameVisitor = CompositionTableFieldNameVisitor()

    private fun cloneCompositionListFields(
        compositionListFields: List<FieldModel>,
        parentEntity: MutableBaseEntityModel
    ) {
        compositionListFields.forEach {
            parentEntity.getOrNewField(getMetaName(it.meta))
        }
    }

    override suspend fun visit(
        responseMain: MainModel,
        requestMain: MainModel?,
        mappingMain: MainModel?
    ) = TraversalAction.CONTINUE

    override suspend fun leave(
        responseMain: MainModel,
        requestMain: MainModel?,
        mappingMain: MainModel?
    ) {
    }

    override suspend fun visit(
        responseRoot: RootModel,
        requestRoot: RootModel?,
        mappingRoot: RootModel?
    ): TraversalAction {
        if (requestRoot == null) return TraversalAction.ABORT_SUB_TREE

        val mappingAux = mappingRoot?.getAux<Any>("mapping") as MappingAux?
        assert(mappingAux != null)
        if (mappingAux == null) return TraversalAction.ABORT_SUB_TREE

        delegate.pushPathEntity(responseRoot)
        val (compositionListFields, fields) = requestRoot.fields.values.partition { isCompositionSetField(it) }
        val mutableResponseRoot = responseRoot as MutableRootModel
        val fieldNames = fields.flatMap { dispatchVisit(it, fieldNameVisitor) ?: listOf() }
        delegate.fetchRoot(mutableResponseRoot, fieldNames, mappingAux)
        cloneCompositionListFields(compositionListFields, mutableResponseRoot)
        return TraversalAction.CONTINUE
    }

    override suspend fun leave(
        responseRoot: RootModel,
        requestRoot: RootModel?,
        mappingRoot: RootModel?
    ) {
        delegate.popPathEntity()
    }

    override suspend fun visit(
        responseEntity: EntityModel,
        requestEntity: EntityModel?,
        mappingEntity: EntityModel?
    ) = TraversalAction.CONTINUE

    override suspend fun leave(
        responseEntity: EntityModel,
        requestEntity: EntityModel?,
        mappingEntity: EntityModel?
    ) {
    }

    // Fields

    override suspend fun visit(
        responseField: SingleFieldModel,
        requestField: SingleFieldModel?,
        mappingField: SingleFieldModel?
    ) = TraversalAction.CONTINUE

    override suspend fun leave(
        responseField: SingleFieldModel,
        requestField: SingleFieldModel?,
        mappingField: SingleFieldModel?
    ) {
    }

    override suspend fun visit(
        responseField: ListFieldModel,
        requestField: ListFieldModel?,
        mappingField: ListFieldModel?
    ) = TraversalAction.CONTINUE

    override suspend fun leave(
        responseField: ListFieldModel,
        requestField: ListFieldModel?,
        mappingField: ListFieldModel?
    ) {
    }

    override suspend fun visit(
        responseField: SetFieldModel,
        requestField: SetFieldModel?,
        mappingField: SetFieldModel?
    ) = if (isCompositionSetField(responseField)) visitCompositionSet(
        responseField,
        requestField,
        mappingField
    ) else TraversalAction.CONTINUE

    override suspend fun leave(
        responseField: SetFieldModel,
        requestField: SetFieldModel?,
        mappingField: SetFieldModel?
    ) {
    }

    private suspend fun visitCompositionSet(
        responseListField: SetFieldModel,
        requestListField: SetFieldModel?,
        mappingListField: SetFieldModel?
    ): TraversalAction {
        if (requestListField == null) return TraversalAction.ABORT_SUB_TREE

        val mappingAux = mappingListField?.getAux<Any>("mapping") as MappingAux?
        assert(mappingAux != null)
        if (mappingAux == null) return TraversalAction.ABORT_SUB_TREE

        val requestEntityFields = (requestListField.firstValue() as? EntityModel)?.fields ?: LinkedHashMap()
        val (compositionListFields, fields) = requestEntityFields.values.partition { isCompositionSetField(it) }
        val mutableResponseListField = responseListField as MutableSetFieldModel
        val fieldNames = fields.flatMap { dispatchVisit(it, fieldNameVisitor) ?: listOf() }
        delegate.fetchCompositionList(mutableResponseListField, fieldNames, mappingAux)
        mutableResponseListField.values.forEach {
            cloneCompositionListFields(
                compositionListFields,
                it as MutableEntityModel
            )
        }
        return TraversalAction.CONTINUE
    }

    // Values

    override suspend fun visit(
        responseField: PrimitiveModel,
        requestField: PrimitiveModel?,
        mappingField: PrimitiveModel?
    ) = TraversalAction.CONTINUE

    override suspend fun leave(
        responseField: PrimitiveModel,
        requestField: PrimitiveModel?,
        mappingField: PrimitiveModel?
    ) {
    }

    override suspend fun visit(
        responseField: AliasModel,
        requestField: AliasModel?,
        mappingField: AliasModel?
    ) = TraversalAction.CONTINUE

    override suspend fun leave(
        responseField: AliasModel,
        requestField: AliasModel?,
        mappingField: AliasModel?
    ) {
    }

    override suspend fun visit(
        leaderValue1: Password1wayModel,
        followerValue1: Password1wayModel?,
        followerValue2: Password1wayModel?
    ) = TraversalAction.CONTINUE

    override suspend fun leave(
        leaderValue1: Password1wayModel,
        followerValue1: Password1wayModel?,
        followerValue2: Password1wayModel?
    ) {
    }

    override suspend fun visit(
        leaderValue1: Password2wayModel,
        followerValue1: Password2wayModel?,
        followerValue2: Password2wayModel?
    ) = TraversalAction.CONTINUE

    override suspend fun leave(
        leaderValue1: Password2wayModel,
        followerValue1: Password2wayModel?,
        followerValue2: Password2wayModel?
    ) {
    }

    override suspend fun visit(
        responseField: EnumerationModel,
        requestField: EnumerationModel?,
        mappingField: EnumerationModel?
    ) = TraversalAction.CONTINUE

    override suspend fun leave(
        responseField: EnumerationModel,
        requestField: EnumerationModel?,
        mappingField: EnumerationModel?
    ) {
    }

    override suspend fun visit(
        responseField: AssociationModel,
        requestField: AssociationModel?,
        mappingField: AssociationModel?
    ) = TraversalAction.CONTINUE

    override suspend fun leave(
        responseField: AssociationModel,
        requestField: AssociationModel?,
        mappingField: AssociationModel?
    ) {
    }

    // Sub-values

    override suspend fun visit(
        leaderField1: EntityKeysModel,
        followerField1: EntityKeysModel?,
        followerField2: EntityKeysModel?
    ): TraversalAction = TraversalAction.CONTINUE

    override suspend fun leave(
        leaderField1: EntityKeysModel,
        followerField1: EntityKeysModel?,
        followerField2: EntityKeysModel?
    ) {
    }
}
