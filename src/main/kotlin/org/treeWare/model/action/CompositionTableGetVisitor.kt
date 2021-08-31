package org.treeWare.model.action

import org.treeWare.common.traversal.TraversalAction
import org.treeWare.metaModel.getMetaName
import org.treeWare.model.core.*
import org.treeWare.model.operator.dispatchVisit

// IMPLEMENTATION: ./Get.md

// NOTE: this class uses typecasts.
// Not a fan of typecasting, but using it inside the framework to avoid
// code duplication. The framework will be well tested, so making an
// exception for the framework, at least for now.

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
class CompositionTableGetVisitor<MappingAux>(
    private val delegate: CompositionTableGetVisitorDelegate<MappingAux>
) : GetVisitor<MappingAux> {
    private val fieldNameVisitor = CompositionTableFieldNameVisitor()

    private fun cloneCompositionListFields(
        compositionListFields: List<FieldModel<Unit>>,
        parentEntity: MutableBaseEntityModel<Unit>
    ) {
        compositionListFields.forEach {
            parentEntity.getOrNewField(getMetaName(it.meta))
        }
    }

    override suspend fun visit(
        responseModel: Model<Unit>,
        requestModel: Model<Unit>?,
        mappingModel: Model<MappingAux>?
    ) = TraversalAction.CONTINUE

    override suspend fun leave(
        responseModel: Model<Unit>,
        requestModel: Model<Unit>?,
        mappingModel: Model<MappingAux>?
    ) {
    }

    override suspend fun visit(
        responseRoot: RootModel<Unit>,
        requestRoot: RootModel<Unit>?,
        mappingRoot: RootModel<MappingAux>?
    ): TraversalAction {
        if (requestRoot == null) return TraversalAction.ABORT_SUB_TREE

        val mappingAux = mappingRoot?.aux
        assert(mappingAux != null)
        if (mappingAux == null) return TraversalAction.ABORT_SUB_TREE

        delegate.pushPathEntity(responseRoot)
        val (compositionListFields, fields) = requestRoot.fields.values.partition { isCompositionListField(it) }
        val mutableResponseRoot = responseRoot as MutableRootModel<Unit>
        val fieldNames = fields.flatMap { dispatchVisit(it, fieldNameVisitor) ?: listOf() }
        delegate.fetchRoot(mutableResponseRoot, fieldNames, mappingAux)
        cloneCompositionListFields(compositionListFields, mutableResponseRoot)
        return TraversalAction.CONTINUE
    }

    override suspend fun leave(
        responseRoot: RootModel<Unit>,
        requestRoot: RootModel<Unit>?,
        mappingRoot: RootModel<MappingAux>?
    ) {
        delegate.popPathEntity()
    }

    override suspend fun visit(
        responseEntity: EntityModel<Unit>,
        requestEntity: EntityModel<Unit>?,
        mappingEntity: EntityModel<MappingAux>?
    ) = TraversalAction.CONTINUE

    override suspend fun leave(
        responseEntity: EntityModel<Unit>,
        requestEntity: EntityModel<Unit>?,
        mappingEntity: EntityModel<MappingAux>?
    ) {
    }

    // Fields

    override suspend fun visit(
        responseField: SingleFieldModel<Unit>,
        requestField: SingleFieldModel<Unit>?,
        mappingField: SingleFieldModel<MappingAux>?
    ) = TraversalAction.CONTINUE

    override suspend fun leave(
        responseField: SingleFieldModel<Unit>,
        requestField: SingleFieldModel<Unit>?,
        mappingField: SingleFieldModel<MappingAux>?
    ) {
    }

    override suspend fun visit(
        responseField: ListFieldModel<Unit>,
        requestField: ListFieldModel<Unit>?,
        mappingField: ListFieldModel<MappingAux>?
    ) = if (isCompositionListField(responseField)) visitCompositionList(
        responseField,
        requestField,
        mappingField
    ) else TraversalAction.CONTINUE

    override suspend fun leave(
        responseField: ListFieldModel<Unit>,
        requestField: ListFieldModel<Unit>?,
        mappingField: ListFieldModel<MappingAux>?
    ) {
    }

    private suspend fun visitCompositionList(
        responseListField: ListFieldModel<Unit>,
        requestListField: ListFieldModel<Unit>?,
        mappingListField: ListFieldModel<MappingAux>?
    ): TraversalAction {
        if (requestListField == null) return TraversalAction.ABORT_SUB_TREE

        val mappingAux = mappingListField?.aux
        assert(mappingAux != null)
        if (mappingAux == null) return TraversalAction.ABORT_SUB_TREE

        val requestEntityFields = (requestListField.firstValue() as? EntityModel<Unit>)?.fields ?: LinkedHashMap()
        val (compositionListFields, fields) = requestEntityFields.values.partition { isCompositionListField(it) }
        val mutableResponseListField = responseListField as MutableListFieldModel<Unit>
        val fieldNames = fields.flatMap { dispatchVisit(it, fieldNameVisitor) ?: listOf() }
        delegate.fetchCompositionList(mutableResponseListField, fieldNames, mappingAux)
        mutableResponseListField.values.forEach {
            cloneCompositionListFields(
                compositionListFields,
                it as MutableEntityModel<Unit>
            )
        }
        return TraversalAction.CONTINUE
    }

    // Values

    override suspend fun visit(
        responseField: PrimitiveModel<Unit>,
        requestField: PrimitiveModel<Unit>?,
        mappingField: PrimitiveModel<MappingAux>?
    ) = TraversalAction.CONTINUE

    override suspend fun leave(
        responseField: PrimitiveModel<Unit>,
        requestField: PrimitiveModel<Unit>?,
        mappingField: PrimitiveModel<MappingAux>?
    ) {
    }

    override suspend fun visit(
        responseField: AliasModel<Unit>,
        requestField: AliasModel<Unit>?,
        mappingField: AliasModel<MappingAux>?
    ) = TraversalAction.CONTINUE

    override suspend fun leave(
        responseField: AliasModel<Unit>,
        requestField: AliasModel<Unit>?,
        mappingField: AliasModel<MappingAux>?
    ) {
    }

    override suspend fun visit(
        responseField: EnumerationModel<Unit>,
        requestField: EnumerationModel<Unit>?,
        mappingField: EnumerationModel<MappingAux>?
    ) = TraversalAction.CONTINUE

    override suspend fun leave(
        responseField: EnumerationModel<Unit>,
        requestField: EnumerationModel<Unit>?,
        mappingField: EnumerationModel<MappingAux>?
    ) {
    }

    override suspend fun visit(
        responseField: AssociationModel<Unit>,
        requestField: AssociationModel<Unit>?,
        mappingField: AssociationModel<MappingAux>?
    ) = TraversalAction.CONTINUE

    override suspend fun leave(
        responseField: AssociationModel<Unit>,
        requestField: AssociationModel<Unit>?,
        mappingField: AssociationModel<MappingAux>?
    ) {
    }

    // Sub-values

    override suspend fun visit(
        leaderField1: EntityKeysModel<Unit>,
        followerField1: EntityKeysModel<Unit>?,
        followerField2: EntityKeysModel<MappingAux>?
    ): TraversalAction = TraversalAction.CONTINUE

    override suspend fun leave(
        leaderField1: EntityKeysModel<Unit>,
        followerField1: EntityKeysModel<Unit>?,
        followerField2: EntityKeysModel<MappingAux>?
    ) {
    }
}
