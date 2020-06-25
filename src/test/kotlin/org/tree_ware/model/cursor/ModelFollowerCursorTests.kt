package org.tree_ware.model.cursor

import org.tree_ware.model.core.Model
import org.tree_ware.model.getModel
import org.tree_ware.schema.core.SchemaTraversalAction
import kotlin.test.Test
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class ModelFollowerCursorTests {
    @Test
    fun `Follower-cursor follows data-model leader-cursor`() {
        val model1 = getModel("src/test/resources/model/address_book_1.json")
        testFollowerSameModelInstance(model1)
    }
}

private fun <Aux> testFollowerSameModelInstance(model: Model<Aux>) {
    val leader = ModelLeaderCursor(model)
    val follower = ModelFollowerCursor(model)

    val action = SchemaTraversalAction.CONTINUE
    while (true) {
        val leaderMove = leader.next(action) ?: break
        assertNotSame(leader.element, follower.element)
        follower.follow(leaderMove)
        assertSame(leader.element, follower.element)
    }
}
