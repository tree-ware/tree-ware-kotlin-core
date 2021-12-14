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

    override suspend fun visitMain(
        responseMain: MainModel,
        requestMain: MainModel?,
        mappingMain: MainModel?
    ) = TraversalAction.CONTINUE

    override suspend fun leaveMain(
        responseMain: MainModel,
        requestMain: MainModel?,
        mappingMain: MainModel?
    ) {
    }

    override suspend fun visitRoot(
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

    override suspend fun leaveRoot(
        responseRoot: RootModel,
        requestRoot: RootModel?,
        mappingRoot: RootModel?
    ) {
        delegate.popPathEntity()
    }

    override suspend fun visitEntity(
        responseEntity: EntityModel,
        requestEntity: EntityModel?,
        mappingEntity: EntityModel?
    ) = TraversalAction.CONTINUE

    override suspend fun leaveEntity(
        responseEntity: EntityModel,
        requestEntity: EntityModel?,
        mappingEntity: EntityModel?
    ) {
    }

    // Fields

    override suspend fun visitSingleField(
        responseField: SingleFieldModel,
        requestField: SingleFieldModel?,
        mappingField: SingleFieldModel?
    ) = TraversalAction.CONTINUE

    override suspend fun leaveSingleField(
        responseField: SingleFieldModel,
        requestField: SingleFieldModel?,
        mappingField: SingleFieldModel?
    ) {
    }

    override suspend fun visitListField(
        responseField: ListFieldModel,
        requestField: ListFieldModel?,
        mappingField: ListFieldModel?
    ) = TraversalAction.CONTINUE

    override suspend fun leaveListField(
        responseField: ListFieldModel,
        requestField: ListFieldModel?,
        mappingField: ListFieldModel?
    ) {
    }

    override suspend fun visitSetField(
        responseField: SetFieldModel,
        requestField: SetFieldModel?,
        mappingField: SetFieldModel?
    ) = if (isCompositionSetField(responseField)) visitCompositionSet(
        responseField,
        requestField,
        mappingField
    ) else TraversalAction.CONTINUE

    override suspend fun leaveSetField(
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

    override suspend fun visitPrimitive(
        responseField: PrimitiveModel,
        requestField: PrimitiveModel?,
        mappingField: PrimitiveModel?
    ) = TraversalAction.CONTINUE

    override suspend fun leavePrimitive(
        responseField: PrimitiveModel,
        requestField: PrimitiveModel?,
        mappingField: PrimitiveModel?
    ) {
    }

    override suspend fun visitAlias(
        responseField: AliasModel,
        requestField: AliasModel?,
        mappingField: AliasModel?
    ) = TraversalAction.CONTINUE

    override suspend fun leaveAlias(
        responseField: AliasModel,
        requestField: AliasModel?,
        mappingField: AliasModel?
    ) {
    }

    override suspend fun visitPassword1way(
        leaderValue1: Password1wayModel,
        followerValue1: Password1wayModel?,
        followerValue2: Password1wayModel?
    ) = TraversalAction.CONTINUE

    override suspend fun leavePassword1way(
        leaderValue1: Password1wayModel,
        followerValue1: Password1wayModel?,
        followerValue2: Password1wayModel?
    ) {
    }

    override suspend fun visitPassword2way(
        leaderValue1: Password2wayModel,
        followerValue1: Password2wayModel?,
        followerValue2: Password2wayModel?
    ) = TraversalAction.CONTINUE

    override suspend fun leavePassword2way(
        leaderValue1: Password2wayModel,
        followerValue1: Password2wayModel?,
        followerValue2: Password2wayModel?
    ) {
    }

    override suspend fun visitEnumeration(
        responseField: EnumerationModel,
        requestField: EnumerationModel?,
        mappingField: EnumerationModel?
    ) = TraversalAction.CONTINUE

    override suspend fun leaveEnumeration(
        responseField: EnumerationModel,
        requestField: EnumerationModel?,
        mappingField: EnumerationModel?
    ) {
    }

    override suspend fun visitAssociation(
        responseField: AssociationModel,
        requestField: AssociationModel?,
        mappingField: AssociationModel?
    ) = TraversalAction.CONTINUE

    override suspend fun leaveAssociation(
        responseField: AssociationModel,
        requestField: AssociationModel?,
        mappingField: AssociationModel?
    ) {
    }

    // Sub-values

    override suspend fun visitEntityKeys(
        leaderField1: EntityKeysModel,
        followerField1: EntityKeysModel?,
        followerField2: EntityKeysModel?
    ): TraversalAction = TraversalAction.CONTINUE

    override suspend fun leaveEntityKeys(
        leaderField1: EntityKeysModel,
        followerField1: EntityKeysModel?,
        followerField2: EntityKeysModel?
    ) {
    }
}
