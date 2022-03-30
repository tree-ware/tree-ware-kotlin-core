package org.treeWare.model.operator

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

private interface TestOperator1Delegate {
    fun getName(): String
}

private class DefaultTestOperator1Delegate(private val name: String) : TestOperator1Delegate {
    override fun getName(): String = name
}

private object TestOperator1Id : OperatorId<TestOperator1Delegate>

private interface TestOperator2Delegate {
    fun getName(): String
}

private class DefaultTestOperator2Delegate(private val name: String) : TestOperator2Delegate {
    override fun getName(): String = name
}

private object TestOperator2Id : OperatorId<TestOperator2Delegate>

private const val ENTITY_1_FULL_NAME = "/package_name/entity_1_name"
private const val ENTITY_2_FULL_NAME = "/package_name/entity_2_name"

class OperatorDelegateRegistryTests {
    private val operatorEntityDelegateRegistry = OperatorEntityDelegateRegistry()

    @Test
    fun `OperatorDelegateRegistry must return the correct registry for an operator`() {
        val operator1Entity1DelegateName = "operator1Entity1DelegateName"
        val operator1Entity2DelegateName = "operator1Entity2DelegateName"
        val operator2Entity1DelegateName = "operator2Entity1DelegateName"

        operatorEntityDelegateRegistry.add(
            ENTITY_1_FULL_NAME,
            TestOperator1Id,
            DefaultTestOperator1Delegate(operator1Entity1DelegateName)
        )
        operatorEntityDelegateRegistry.add(
            ENTITY_2_FULL_NAME,
            TestOperator1Id,
            DefaultTestOperator1Delegate(operator1Entity2DelegateName)
        )
        operatorEntityDelegateRegistry.add(
            ENTITY_1_FULL_NAME,
            TestOperator2Id,
            DefaultTestOperator2Delegate(operator2Entity1DelegateName)
        )

        val entityDelegateRegistry1: EntityDelegateRegistry<TestOperator1Delegate>? =
            operatorEntityDelegateRegistry.get(TestOperator1Id)
        assertNotNull(entityDelegateRegistry1)
        assertEquals(operator1Entity1DelegateName, entityDelegateRegistry1[ENTITY_1_FULL_NAME]?.getName())
        assertEquals(operator1Entity2DelegateName, entityDelegateRegistry1[ENTITY_2_FULL_NAME]?.getName())

        val entityDelegateRegistry2: EntityDelegateRegistry<TestOperator2Delegate>? =
            operatorEntityDelegateRegistry.get(TestOperator2Id)
        assertNotNull(entityDelegateRegistry2)
        assertEquals(operator2Entity1DelegateName, entityDelegateRegistry2[ENTITY_1_FULL_NAME]?.getName())
        assertNull(entityDelegateRegistry2[ENTITY_2_FULL_NAME]?.getName())
    }

    @Test
    fun `OperatorDelegateRegistry must return null if delegates for an operator were not registered`() {
        val entityDelegateRegistry1: EntityDelegateRegistry<TestOperator1Delegate>? =
            operatorEntityDelegateRegistry.get(TestOperator1Id)
        assertNull(entityDelegateRegistry1)

        val entityDelegateRegistry2: EntityDelegateRegistry<TestOperator2Delegate>? =
            operatorEntityDelegateRegistry.get(TestOperator2Id)
        assertNull(entityDelegateRegistry2)
    }

    @Test
    fun `OperatorDelegateRegistry must replace old delegate if a new delegate is registered`() {
        val operator1Entity1Delegate1Name = "operator1Entity1Delegate1Name"
        val operator1Entity1Delegate2Name = "operator1Entity1Delegate2Name"
        val operator2Entity1DelegateName = "operator2Entity1DelegateName"

        operatorEntityDelegateRegistry.add(
            ENTITY_1_FULL_NAME,
            TestOperator1Id,
            DefaultTestOperator1Delegate(operator1Entity1Delegate1Name)
        )
        operatorEntityDelegateRegistry.add(
            ENTITY_1_FULL_NAME,
            TestOperator1Id,
            DefaultTestOperator1Delegate(operator1Entity1Delegate2Name)
        )
        operatorEntityDelegateRegistry.add(
            ENTITY_1_FULL_NAME,
            TestOperator2Id,
            DefaultTestOperator2Delegate(operator2Entity1DelegateName)
        )

        val entityDelegateRegistry1: EntityDelegateRegistry<TestOperator1Delegate>? =
            operatorEntityDelegateRegistry.get(TestOperator1Id)
        assertNotNull(entityDelegateRegistry1)
        assertEquals(operator1Entity1Delegate2Name, entityDelegateRegistry1[ENTITY_1_FULL_NAME]?.getName())
        assertNull(entityDelegateRegistry1[ENTITY_2_FULL_NAME]?.getName())

        val entityDelegateRegistry2: EntityDelegateRegistry<TestOperator2Delegate>? =
            operatorEntityDelegateRegistry.get(TestOperator2Id)
        assertNotNull(entityDelegateRegistry2)
        assertEquals(operator2Entity1DelegateName, entityDelegateRegistry2[ENTITY_1_FULL_NAME]?.getName())
        assertNull(entityDelegateRegistry2[ENTITY_2_FULL_NAME]?.getName())
    }
}