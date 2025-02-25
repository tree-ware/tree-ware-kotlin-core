package org.treeWare.model.cursor

import okio.Buffer
import org.treeWare.metaModel.addressBookRootEntityFactory
import org.treeWare.model.core.ElementModel
import org.treeWare.model.core.getMetaResolved
import org.treeWare.model.decodeJsonFileIntoEntity
import org.treeWare.model.encoder.EncodePasswords
import org.treeWare.model.encoder.JsonWireFormatEncoder
import org.treeWare.model.encoder.ModelEncodingVisitor
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.model.traversal.dispatchLeave
import org.treeWare.model.traversal.dispatchVisit
import org.treeWare.util.readFile
import kotlin.test.*

class FollowerModelCursorTests {
    @Test
    fun `Follower-cursor on same data-model follows leader-cursor without association traversal`() {
        testFollowerSameModelInstance("model/address_book_1.json", false)
    }

    @Test
    fun `Follower-cursor on same data-model follows leader-cursor with association traversal`() {
        testFollowerSameModelInstance("model/address_book_1.json", true)
    }

    @Test
    fun `Follower-cursor on same null-fields data-model follows leader-cursor without association traversal`() {
        testFollowerSameModelInstance(
            "org/treeWare/model/cursor/address_book_null_fields.json",
            false
        )
    }

    @Test
    fun `Follower-cursor on same null-fields data-model follows leader-cursor with association traversal`() {
        testFollowerSameModelInstance(
            "org/treeWare/model/cursor/address_book_null_fields.json",
            true
        )
    }

    @Test
    fun `Follower-cursor on different data-model follows leader-cursor without association traversal`() {
        testFollowerDifferentModelInstances("model/address_book_1.json", false)
    }

    @Test
    fun `Follower-cursor on different data-model follows leader-cursor with association traversal`() {
        testFollowerDifferentModelInstances("model/address_book_1.json", true)
    }

    @Test
    fun `Follower-cursor on wildcard model follows leader-cursor without association traversal`() {
        testFollowerWildcardModelInstance(
            "model/address_book_1.json",
            "model/address_book_empty_root.json",
            false,
            "model/address_book_1_no_associations.json"
        )
    }

    @Test
    fun `Follower-cursor on wildcard model follows leader-cursor with association traversal`() {
        testFollowerWildcardModelInstance(
            "model/address_book_1.json",
            "model/address_book_empty_root.json",
            true
        )
    }
}

private fun testFollowerSameModelInstance(inputFilePath: String, traverseAssociations: Boolean) {
    val model = addressBookRootEntityFactory(null)
    decodeJsonFileIntoEntity(inputFilePath, entity = model)

    val leaderCursor = Leader1ModelCursor(model, traverseAssociations)
    val followerCursor = FollowerModelCursor(model)

    var moveCount = 0
    val action = TraversalAction.CONTINUE
    while (true) {
        val leaderMove = leaderCursor.next(action) ?: break
        assertNotSame(leaderCursor.element, followerCursor.element)
        val followerMove = followerCursor.follow(leaderMove)
        assertNotNull(followerMove)
        assertSame(leaderMove.element, followerMove.element)
        assertSame(leaderCursor.element, followerCursor.element)
        ++moveCount
    }
    assertNotEquals(0, moveCount)
}

private fun testFollowerDifferentModelInstances(inputFilePath: String, traverseAssociations: Boolean) {
    // Create different instances of the model from the same JSON input file.
    val leaderModel = addressBookRootEntityFactory(null)
    decodeJsonFileIntoEntity(inputFilePath, entity = leaderModel)
    val followerModel = addressBookRootEntityFactory(null)
    decodeJsonFileIntoEntity(inputFilePath, entity = followerModel)

    assertNotSame(leaderModel, followerModel)

    val leaderCursor = Leader1ModelCursor(leaderModel, traverseAssociations)
    val followerCursor = FollowerModelCursor(followerModel)

    val action = TraversalAction.CONTINUE
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
private fun testFollowerWildcardModelInstance(
    leaderFilePath: String,
    wildcardFilePath: String,
    traverseAssociations: Boolean,
    expectedFilePath: String? = null
) {
    val leaderModel = addressBookRootEntityFactory(null)
    decodeJsonFileIntoEntity(leaderFilePath, entity = leaderModel)
    val followerModel = addressBookRootEntityFactory(null)
    decodeJsonFileIntoEntity(wildcardFilePath, entity = followerModel)

    val leaderCursor = Leader1ModelCursor(leaderModel, traverseAssociations)
    val followerCursor = FollowerModelCursor(followerModel)

    val jsonBuffer = Buffer()
    val wireFormatEncoder = JsonWireFormatEncoder(jsonBuffer, true)
    val encodingVisitor = ModelEncodingVisitor(wireFormatEncoder, encodePasswords = EncodePasswords.ALL)

    var action = TraversalAction.CONTINUE
    while (action != TraversalAction.ABORT_TREE) {
        val leaderMove = leaderCursor.next(action) ?: break
        val followerMove = followerCursor.follow(leaderMove)
        assertNotNull(followerMove)

        action = when (leaderMove.direction) {
            CursorMoveDirection.VISIT -> dispatchVisit(leaderMove.element, encodingVisitor)
                ?: TraversalAction.ABORT_TREE
            CursorMoveDirection.LEAVE -> {
                dispatchLeave(leaderMove.element, encodingVisitor)
                TraversalAction.CONTINUE
            }
        }
    }

    val expected = readFile(expectedFilePath ?: leaderFilePath)
    val actual = jsonBuffer.readUtf8()
    assertEquals(expected, actual)
}

// TODO(deepak-nulu): return model path instead of meta-model path
private fun getPath(element: ElementModel?): String? = element?.getMetaResolved()?.fullName