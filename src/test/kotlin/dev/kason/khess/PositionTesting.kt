package dev.kason.khess

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.random.Random
import kotlin.test.assertEquals

class PositionTesting {

    @Test
    fun ensureThatJsonIsEqual() {
        val testingX = Random.nextInt()
        val testingY = Random.nextInt()
        val mutablePosition = MutablePosition(testingX, testingY)
        val position = Position(testingX, testingY)
        assertEquals(Json.encodeToString(mutablePosition), Json.encodeToString(position))
    }

    @Test
    fun ensureThatPositionIsEqual() {
        val testingX = Random.nextInt()
        val testingY = Random.nextInt()
        val mutablePosition = MutablePosition(testingX, testingY)
        val position = Position(testingX, testingY)
        assertEquals(mutablePosition, position)
    }

}