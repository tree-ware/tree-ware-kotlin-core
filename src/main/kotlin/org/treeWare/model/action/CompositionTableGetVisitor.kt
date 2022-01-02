package org.treeWare.model.action

import kotlinx.coroutines.runBlocking
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

    override fun visitMain(
        responseMain: MainModel,
        requestMain: MainModel?,
        mappingMain: MainModel?
    ) = TraversalAction.CONTINUE

    override fun leaveMain(
        responseMain: MainModel,
        requestMain: MainModel?,
        mappingMain: MainModel?
    ) {
    }

// TODO(deepak-nulu): move this to visitEntity()
//
//    override fun visitRoot(
//        responseRoot: RootModel,
//        requestRoot: RootModel?,
//        mappingRoot: RootModel?
//    ): TraversalAction {
//        if (requestRoot == null) return TraversalAction.ABORT_SUB_TREE
//
//        val mappingAux = mappingRoot?.getAux<Any>("mapping") as MappingAux?
//        assert(mappingAux != null)
//        if (mappingAux == null) return TraversalAction.ABORT_SUB_TREE
//
//        delegate.pushPathEntity(responseRoot)
//        val (compositionListFields, fields) = requestRoot.fields.values.partition { isCompositionSetField(it) }
//        val mutableResponseRoot = responseRoot as MutableRootModel
//        val fieldNames = fields.flatMap { dispatchVisit(it, fieldNameVisitor) ?: listOf() }
//        runBlocking { delegate.fetchRoot(mutableResponseRoot, fieldNames, mappingAux) }
//        cloneCompositionListFields(compositionListFields, mutableResponseRoot)
//        return TraversalAction.CONTINUE
//    }
//
//    override fun leaveRoot(
//        responseRoot: RootModel,
//        requestRoot: RootModel?,
//        mappingRoot: RootModel?
//    ) {
//        delegate.popPathEntity()
//    }

    override fun visitEntity(
        responseEntity: EntityModel,
        requestEntity: EntityModel?,
        mappingEntity: EntityModel?
    ) = TraversalAction.CONTINUE

    override fun leaveEntity(
        responseEntity: EntityModel,
        requestEntity: EntityModel?,
        mappingEntity: EntityModel?
    ) {
    }

    // Fields

    override fun visitSingleField(
        responseField: SingleFieldModel,
        requestField: SingleFieldModel?,
        mappingField: SingleFieldModel?
    ) = TraversalAction.CONTINUE

    override fun leaveSingleField(
        responseField: SingleFieldModel,
        requestField: SingleFieldModel?,
        mappingField: SingleFieldModel?
    ) {
    }

    override fun visitListField(
        responseField: ListFieldModel,
        requestField: ListFieldModel?,
        mappingField: ListFieldModel?
    ) = TraversalAction.CONTINUE

    override fun leaveListField(
        responseField: ListFieldModel,
        requestField: ListFieldModel?,
        mappingField: ListFieldModel?
    ) {
    }

    override fun visitSetField(
        responseField: SetFieldModel,
        requestField: SetFieldModel?,
        mappingField: SetFieldModel?
    ) = if (isCompositionSetField(responseField)) visitCompositionSet(
        responseField,
        requestField,
        mappingField
    ) else TraversalAction.CONTINUE

    override fun leaveSetField(
        responseField: SetFieldModel,
        requestField: SetFieldModel?,
        mappingField: SetFieldModel?
    ) {
    }

    private fun visitCompositionSet(
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
        runBlocking { delegate.fetchCompositionList(mutableResponseListField, fieldNames, mappingAux) }
        mutableResponseListField.values.forEach {
            cloneCompositionListFields(
                compositionListFields,
                it as MutableEntityModel
            )
        }
        return TraversalAction.CONTINUE
    }

    // Values

    override fun visitPrimitive(
        responseField: PrimitiveModel,
        requestField: PrimitiveModel?,
        mappingField: PrimitiveModel?
    ) = TraversalAction.CONTINUE

    override fun leavePrimitive(
        responseField: PrimitiveModel,
        requestField: PrimitiveModel?,
        mappingField: PrimitiveModel?
    ) {
    }

    override fun visitAlias(
        responseField: AliasModel,
        requestField: AliasModel?,
        mappingField: AliasModel?
    ) = TraversalAction.CONTINUE

    override fun leaveAlias(
        responseField: AliasModel,
        requestField: AliasModel?,
        mappingField: AliasModel?
    ) {
    }

    override fun visitPassword1way(
        leaderValue1: Password1wayModel,
        followerValue1: Password1wayModel?,
        followerValue2: Password1wayModel?
    ) = TraversalAction.CONTINUE

    override fun leavePassword1way(
        leaderValue1: Password1wayModel,
        followerValue1: Password1wayModel?,
        followerValue2: Password1wayModel?
    ) {
    }

    override fun visitPassword2way(
        leaderValue1: Password2wayModel,
        followerValue1: Password2wayModel?,
        followerValue2: Password2wayModel?
    ) = TraversalAction.CONTINUE

    override fun leavePassword2way(
        leaderValue1: Password2wayModel,
        followerValue1: Password2wayModel?,
        followerValue2: Password2wayModel?
    ) {
    }

    override fun visitEnumeration(
        responseField: EnumerationModel,
        requestField: EnumerationModel?,
        mappingField: EnumerationModel?
    ) = TraversalAction.CONTINUE

    override fun leaveEnumeration(
        responseField: EnumerationModel,
        requestField: EnumerationModel?,
        mappingField: EnumerationModel?
    ) {
    }

    override fun visitAssociation(
        responseField: AssociationModel,
        requestField: AssociationModel?,
        mappingField: AssociationModel?
    ) = TraversalAction.CONTINUE

    override fun leaveAssociation(
        responseField: AssociationModel,
        requestField: AssociationModel?,
        mappingField: AssociationModel?
    ) {
    }

    // Sub-values

    override fun visitEntityKeys(
        leaderField1: EntityKeysModel,
        followerField1: EntityKeysModel?,
        followerField2: EntityKeysModel?
    ): TraversalAction = TraversalAction.CONTINUE

    override fun leaveEntityKeys(
        leaderField1: EntityKeysModel,
        followerField1: EntityKeysModel?,
        followerField2: EntityKeysModel?
    ) {
    }
}