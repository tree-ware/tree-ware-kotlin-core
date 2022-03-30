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
    private val operatorDelegateRegistry = OperatorDelegateRegistry()

    @Test
    fun `OperatorDelegateRegistry must return the correct registry for an operator`() {
        val operator1Entity1DelegateName = "operator1Entity1DelegateName"
        val operator1Entity2DelegateName = "operator1Entity2DelegateName"
        val operator2Entity1DelegateName = "operator2Entity1DelegateName"

        operatorDelegateRegistry.add(
            ENTITY_1_FULL_NAME,
            TestOperator1Id,
            DefaultTestOperator1Delegate(operator1Entity1DelegateName)
        )
        operatorDelegateRegistry.add(
            ENTITY_2_FULL_NAME,
            TestOperator1Id,
            DefaultTestOperator1Delegate(operator1Entity2DelegateName)
        )
        operatorDelegateRegistry.add(
            ENTITY_1_FULL_NAME,
            TestOperator2Id,
            DefaultTestOperator2Delegate(operator2Entity1DelegateName)
        )

        val delegateRegistry1: DelegateRegistry<TestOperator1Delegate>? = operatorDelegateRegistry.get(TestOperator1Id)
        assertNotNull(delegateRegistry1)
        assertEquals(operator1Entity1DelegateName, delegateRegistry1[ENTITY_1_FULL_NAME]?.getName())
        assertEquals(operator1Entity2DelegateName, delegateRegistry1[ENTITY_2_FULL_NAME]?.getName())

        val delegateRegistry2: DelegateRegistry<TestOperator2Delegate>? = operatorDelegateRegistry.get(TestOperator2Id)
        assertNotNull(delegateRegistry2)
        assertEquals(operator2Entity1DelegateName, delegateRegistry2[ENTITY_1_FULL_NAME]?.getName())
        assertNull(delegateRegistry2[ENTITY_2_FULL_NAME]?.getName())
    }

    @Test
    fun `OperatorDelegateRegistry must return null if delegates for an operator were not registered`() {
        val delegateRegistry1: DelegateRegistry<TestOperator1Delegate>? = operatorDelegateRegistry.get(TestOperator1Id)
        assertNull(delegateRegistry1)

        val delegateRegistry2: DelegateRegistry<TestOperator2Delegate>? = operatorDelegateRegistry.get(TestOperator2Id)
        assertNull(delegateRegistry2)
    }

    @Test
    fun `OperatorDelegateRegistry must replace old delegate if a new delegate is registered`() {
        val operator1Entity1Delegate1Name = "operator1Entity1Delegate1Name"
        val operator1Entity1Delegate2Name = "operator1Entity1Delegate2Name"
        val operator2Entity1DelegateName = "operator2Entity1DelegateName"

        operatorDelegateRegistry.add(
            ENTITY_1_FULL_NAME,
            TestOperator1Id,
            DefaultTestOperator1Delegate(operator1Entity1Delegate1Name)
        )
        operatorDelegateRegistry.add(
            ENTITY_1_FULL_NAME,
            TestOperator1Id,
            DefaultTestOperator1Delegate(operator1Entity1Delegate2Name)
        )
        operatorDelegateRegistry.add(
            ENTITY_1_FULL_NAME,
            TestOperator2Id,
            DefaultTestOperator2Delegate(operator2Entity1DelegateName)
        )

        val delegateRegistry1: DelegateRegistry<TestOperator1Delegate>? = operatorDelegateRegistry.get(TestOperator1Id)
        assertNotNull(delegateRegistry1)
        assertEquals(operator1Entity1Delegate2Name, delegateRegistry1[ENTITY_1_FULL_NAME]?.getName())
        assertNull(delegateRegistry1[ENTITY_2_FULL_NAME]?.getName())

        val delegateRegistry2: DelegateRegistry<TestOperator2Delegate>? = operatorDelegateRegistry.get(TestOperator2Id)
        assertNotNull(delegateRegistry2)
        assertEquals(operator2Entity1DelegateName, delegateRegistry2[ENTITY_1_FULL_NAME]?.getName())
        assertNull(delegateRegistry2[ENTITY_2_FULL_NAME]?.getName())
    }
}