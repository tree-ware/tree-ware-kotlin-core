package org.tree_ware.model.cursor

import org.tree_ware.common.codec.JsonWireFormatEncoder
import org.tree_ware.model.codec.ModelEncodingVisitor
import org.tree_ware.model.core.ElementModel
import org.tree_ware.model.getFileReader
import org.tree_ware.model.getModel
import org.tree_ware.schema.core.NamedElementSchema
import org.tree_ware.schema.core.SchemaTraversalAction
import org.tree_ware.schema.core.newAddressBookSchema
import org.tree_ware.schema.core.validate
import java.io.InputStreamReader
import java.io.StringWriter
import kotlin.test.*

class FollowerModelCursorTests {
    @Test
    fun `Follower-cursor on same data-model follows leader-cursor`() {
        testFollowerSameModelInstance("model/address_book_1.json")
    }

    @Test
    fun `Follower-cursor on different data-model follows leader-cursor`() {
        testFollowerDifferentModelInstances("model/address_book_1.json")
    }

    @Test
    fun `Follower-cursor on wildcard model follows leader-cursor`() {
        testFollowerWildcardModelInstance(
            "model/address_book_1.json",
            "model/address_book_filter_all_model.json"
        )
    }
}

private fun testFollowerSameModelInstance(inputFilePath: String) {
    val schema = newAddressBookSchema()
    val errors = validate(schema)
    assertTrue(errors.isEmpty())

    val model = getModel<Unit>(schema, inputFilePath)

    val leaderCursor = LeaderModelCursor(model)
    val followerCursor = FollowerModelCursor<Unit, Unit>(model)

    val action = SchemaTraversalAction.CONTINUE
    while (true) {
        val leaderMove = leaderCursor.next(action) ?: break
        assertNotSame(leaderCursor.element, followerCursor.element)
        val followerMove = followerCursor.follow(leaderMove)
        assertNotNull(followerMove)
        assertSame(leaderMove.element, followerMove.element)
        assertSame(leaderCursor.element, followerCursor.element)
    }
}

private fun testFollowerDifferentModelInstances(inputFilePath: String) {
    val schema = newAddressBookSchema()
    val errors = validate(schema)
    assertTrue(errors.isEmpty())

    // Create different instances of the model from the same JSON input file.
    val leaderModel = getModel<Unit>(schema, inputFilePath)
    val followerModel = getModel<Unit>(schema, inputFilePath)

    assertNotSame(leaderModel, followerModel)

    val leaderCursor = LeaderModelCursor(leaderModel)
    val followerCursor = FollowerModelCursor<Unit, Unit>(followerModel)

    val action = SchemaTraversalAction.CONTINUE
    while (true) {
        val leaderMove = leaderCursor.next(action) ?: break
        // TODO(deepak-nulu): once getPath() returns model-paths, verify that
        // the follower element path is not the same as the leader element path
        val followerMove = followerCursor.follow(leaderMove)
        assertNotNull(followerMove)
        assertEquals(getPath(leaderMove.element), getPath(followerMove.element))
        assertEquals(getPath(leaderCursor.element), getPath(followerCursor.element))
    }
}

/**
 * Verifies that the wildcard follower allows every leader element to be visited.
 * To verify this, JSON is generated from the leader cursor and compared against
 * the original JSON file (leaderFilePath).
 */
private fun testFollowerWildcardModelInstance(leaderFilePath: String, wildcardFilePath: String) {
    val schema = newAddressBookSchema()
    val errors = validate(schema)
    assertTrue(errors.isEmpty())

    val leaderModel = getModel<Unit>(schema, leaderFilePath)
    val followerModel = getModel<Unit>(schema, wildcardFilePath)

    val leaderCursor = LeaderModelCursor(leaderModel)
    val followerCursor = FollowerModelCursor<Unit, Unit>(followerModel)

    val jsonWriter = StringWriter()
    val wireFormatEncoder = JsonWireFormatEncoder(jsonWriter, true)
    val encodingVisitor = ModelEncodingVisitor<Unit>(wireFormatEncoder, null)

    var action = SchemaTraversalAction.CONTINUE
    while (action != SchemaTraversalAction.ABORT_TREE) {
        val leaderMove = leaderCursor.next(action) ?: break
        val followerMove = followerCursor.follow(leaderMove)
        assertNotNull(followerMove)

        when (leaderMove.direction) {
            CursorMoveDirection.Visit -> action = leaderMove.element.visitSelf(encodingVisitor)
            CursorMoveDirection.Leave -> leaderMove.element.leaveSelf(encodingVisitor)
        }
    }

    val fileReader = getFileReader(leaderFilePath)
    assertNotNull(fileReader)
    val expected = fileReader.readText()
    fileReader.close()
    val actual = jsonWriter.toString()
    assertEquals(expected, actual)
}

// TODO(deepak-nulu): return model path instead of schema path
private fun <Aux> getPath(element: ElementModel<Aux>?): String? {
    if (element == null) return null
    val schema = element.schema
    return if (schema is NamedElementSchema) schema.fullName else "/"
}
