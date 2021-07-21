package org.treeWare.model.action

import org.apache.logging.log4j.LogManager
import org.treeWare.model.core.*
import org.treeWare.common.traversal.TraversalAction

// IMPLEMENTATION: ./Get.md

// NOTE: this class uses typecasts.
// Not a fan of typecasting, but using it inside the framework to avoid
// code duplication. The framework will be well tested, so making an
// exception for the framework, at least for now.

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
class CompositionTableGetVisitor<MappingAux>(
    private val delegate: CompositionTableGetVisitorDelegate<MappingAux>
) : GetVisitor<MappingAux> {
    private val logger = LogManager.getLogger()

    private val fieldNameVisitor = CompositionTableFieldNameVisitor()

    private fun cloneCompositionListFields(
        compositionListFields: List<FieldModel<Unit>>,
        parentEntity: MutableBaseEntityModel<Unit>
    ) {
        compositionListFields.forEach {
            parentEntity.getOrNewListField(it.schema.name)
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

        delegate.pushPathEntity(responseRoot, responseRoot.schema.resolvedEntity)
        val (compositionListFields, fields) = requestRoot.fields.partition { it is CompositionListFieldModel<*> }
        val mutableResponseRoot = responseRoot as MutableRootModel<Unit>
        val fieldNames = fields.flatMap { it.dispatch(fieldNameVisitor) }
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

    override suspend fun visit(
        responseField: PrimitiveFieldModel<Unit>,
        requestField: PrimitiveFieldModel<Unit>?,
        mappingField: PrimitiveFieldModel<MappingAux>?
    ) = TraversalAction.CONTINUE

    override suspend fun leave(
        responseField: PrimitiveFieldModel<Unit>,
        requestField: PrimitiveFieldModel<Unit>?,
        mappingField: PrimitiveFieldModel<MappingAux>?
    ) {
    }

    override suspend fun visit(
        responseField: AliasFieldModel<Unit>,
        requestField: AliasFieldModel<Unit>?,
        mappingField: AliasFieldModel<MappingAux>?
    ) = TraversalAction.CONTINUE

    override suspend fun leave(
        responseField: AliasFieldModel<Unit>,
        requestField: AliasFieldModel<Unit>?,
        mappingField: AliasFieldModel<MappingAux>?
    ) {
    }

    override suspend fun visit(
        responseField: EnumerationFieldModel<Unit>,
        requestField: EnumerationFieldModel<Unit>?,
        mappingField: EnumerationFieldModel<MappingAux>?
    ) = TraversalAction.CONTINUE

    override suspend fun leave(
        responseField: EnumerationFieldModel<Unit>,
        requestField: EnumerationFieldModel<Unit>?,
        mappingField: EnumerationFieldModel<MappingAux>?
    ) {
    }

    override suspend fun visit(
        responseField: AssociationFieldModel<Unit>,
        requestField: AssociationFieldModel<Unit>?,
        mappingField: AssociationFieldModel<MappingAux>?
    ) = TraversalAction.CONTINUE

    override suspend fun leave(
        responseField: AssociationFieldModel<Unit>,
        requestField: AssociationFieldModel<Unit>?,
        mappingField: AssociationFieldModel<MappingAux>?
    ) {
    }

    override suspend fun visit(
        responseField: CompositionFieldModel<Unit>,
        requestField: CompositionFieldModel<Unit>?,
        mappingField: CompositionFieldModel<MappingAux>?
    ) = TraversalAction.CONTINUE

    override suspend fun leave(
        responseField: CompositionFieldModel<Unit>,
        requestField: CompositionFieldModel<Unit>?,
        mappingField: CompositionFieldModel<MappingAux>?
    ) {
    }

    override suspend fun visit(
        responseField: PrimitiveListFieldModel<Unit>,
        requestField: PrimitiveListFieldModel<Unit>?,
        mappingField: PrimitiveListFieldModel<MappingAux>?
    ) = TraversalAction.CONTINUE

    override suspend fun leave(
        responseField: PrimitiveListFieldModel<Unit>,
        requestField: PrimitiveListFieldModel<Unit>?,
        mappingField: PrimitiveListFieldModel<MappingAux>?
    ) {
    }

    override suspend fun visit(
        responseField: AliasListFieldModel<Unit>,
        requestField: AliasListFieldModel<Unit>?,
        mappingField: AliasListFieldModel<MappingAux>?
    ) = TraversalAction.CONTINUE

    override suspend fun leave(
        responseField: AliasListFieldModel<Unit>,
        requestField: AliasListFieldModel<Unit>?,
        mappingField: AliasListFieldModel<MappingAux>?
    ) {
    }

    override suspend fun visit(
        responseField: EnumerationListFieldModel<Unit>,
        requestField: EnumerationListFieldModel<Unit>?,
        mappingField: EnumerationListFieldModel<MappingAux>?
    ) = TraversalAction.CONTINUE

    override suspend fun leave(
        responseField: EnumerationListFieldModel<Unit>,
        requestField: EnumerationListFieldModel<Unit>?,
        mappingField: EnumerationListFieldModel<MappingAux>?
    ) {
    }

    override suspend fun visit(
        responseField: AssociationListFieldModel<Unit>,
        requestField: AssociationListFieldModel<Unit>?,
        mappingField: AssociationListFieldModel<MappingAux>?
    ) = TraversalAction.CONTINUE

    override suspend fun leave(
        responseField: AssociationListFieldModel<Unit>,
        requestField: AssociationListFieldModel<Unit>?,
        mappingField: AssociationListFieldModel<MappingAux>?
    ) {
    }

    override suspend fun visit(
        responseListField: CompositionListFieldModel<Unit>,
        requestListField: CompositionListFieldModel<Unit>?,
        mappingListField: CompositionListFieldModel<MappingAux>?
    ): TraversalAction {
        if (requestListField == null) return TraversalAction.ABORT_SUB_TREE

        val mappingAux = mappingListField?.aux
        assert(mappingAux != null)
        if (mappingAux == null) return TraversalAction.ABORT_SUB_TREE

        val requestEntityFields = requestListField.entities.elementAtOrNull(0)?.fields ?: listOf()
        val (compositionListFields, fields) = requestEntityFields.partition { it is CompositionListFieldModel<*> }
        val mutableResponseListField = responseListField as MutableCompositionListFieldModel<Unit>
        val fieldNames = fields.flatMap { it.dispatch(fieldNameVisitor) }
        delegate.fetchCompositionList(mutableResponseListField, fieldNames, mappingAux)
        mutableResponseListField.entities.forEach { cloneCompositionListFields(compositionListFields, it) }
        return TraversalAction.CONTINUE
    }

    override suspend fun leave(
        responseField: CompositionListFieldModel<Unit>,
        requestField: CompositionListFieldModel<Unit>?,
        mappingField: CompositionListFieldModel<MappingAux>?
    ) {
    }
}
