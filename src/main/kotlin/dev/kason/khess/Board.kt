@file:Suppress("MemberVisibilityCanBePrivate")

package dev.kason.khess

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlin.math.*

class Board(override val game: Game) : Iterable<Square>, Game.Entity {
    val positionMap: MutableMap<Position, Piece> = hashMapOf()
    var dimensions: Dimensions = Dimensions(0)
        private set

    fun updateSize(playerCount: Int): Boolean {
        val expectedSideLength = (5 * sqrt(playerCount.toDouble())).roundToInt()
        if (dimensions.sideLength != expectedSideLength) {
            dimensions = Dimensions(expectedSideLength)
            return true
        }
        return false
    }

    operator fun get(position: Position): Square {
        require(position !in dimensions) { "Position $position is out of bounds" }
        val piece = positionMap[position] ?: return Square.Empty(position)
        return Square.Occupied(position, piece)
    }

    operator fun set(position: Position, value: Piece?) {
        require(position !in dimensions) { "Position $position is out of bounds" }
        if (value == null) {
            positionMap.remove(position)
            return
        }
        require(value.position == position) { "Piece $value is not at position $position" }
        positionMap[position] = value
    }

    fun getPiece(position: Position): Piece? {
        require(position !in dimensions) { "Position $position is out of bounds" }
        return positionMap[position]
    }

    fun setPiece(position: Position, value: Piece?): Boolean {
        require(position !in dimensions) { "Position $position is out of bounds" }
        if (value == null) {
            positionMap.remove(position)
            return true
        }
        require(value.position == position) { "Piece $value is not at position $position" }
        return positionMap.put(position, value) != null
    }

    class Dimensions(val sideLength: Int) {
        private val shorter = sideLength / 2
        private val longer = sideLength - shorter
        val startX = -shorter
        val endX = longer
        val startY = -shorter
        val endY = longer
        val rangeHorizontal = startX..endX
        val rangeVertical = startY..endY
        operator fun contains(position: Position) = position.x in rangeHorizontal && position.y in rangeVertical
    }

    override fun iterator(): Iterator<Square> = object : Iterator<Square> {
        private var x = dimensions.startX
        private var y = dimensions.startY
        override fun hasNext(): Boolean = y <= dimensions.endY
        override fun next(): Square = this@Board[Position(x++, y)].also {
            if (x <= dimensions.endX) return@also
            x = dimensions.startX
            y++
        }
    }
}
@Serializable(with = Position.Serializer::class)
open class Position(open val x: Int, open val y: Int) {
    fun copy(x: Int = this.x, y: Int = this.y) = Position(x, y)
    fun toMutable() = MutablePosition(x, y)
    fun add(x: Int = 0, y: Int = 0) = Position(this.x + x, this.y + y)
    operator fun plus(other: Position) = Position(x + other.x, y + other.y)
    operator fun minus(other: Position) = Position(x - other.x, y - other.y)
    operator fun component0() = x
    operator fun component1() = y
    override fun equals(other: Any?): Boolean = other is Position && other.x == x && other.y == y
    override fun hashCode(): Int = x * 10 + y
    override fun toString(): String = "($x, $y)"

    companion object {
        val Placeholder = Position(Int.MAX_VALUE, Int.MAX_VALUE)
    }

    object Serializer : KSerializer<Position> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Position") {
            element<Int>("x")
            element<Int>("y")
        }

        override fun serialize(encoder: Encoder, value: Position) = encoder.encodeStructure(descriptor) {
            encodeIntElement(descriptor, 0, value.x)
            encodeIntElement(descriptor, 1, value.y)
        }

        override fun deserialize(decoder: Decoder): MutablePosition = decoder.decodeStructure(descriptor) {
            var currentIndex: Int
            val position = MutablePosition(0, 0)
            while (decodeElementIndex(descriptor).also { currentIndex = it } in 0..1) {
                if (currentIndex == 0) position.x = decodeIntElement(descriptor, 0)
                else position.y = decodeIntElement(descriptor, 1)
            }
            position
        }
    }
}
@Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
@Serializable(with = Position.Serializer::class)
class MutablePosition(override var x: Int, override var y: Int) : Position(x, y)
sealed class Square(val position: Position, open val piece: Piece? = null) {
    val isEmpty get() = piece == null
    val isOccupied get() = piece != null
    val isLightTiled get() = (position.x + position.y) % 2 == 1
    val isDarkTiled get() = (position.x + position.y) % 2 == 0
    val isDynamic get() = this is Dynamic
    fun dynamic(board: Board): Square.Dynamic = if (this is Dynamic) this else Dynamic(this.position, board)
    class Empty(position: Position) : Square(position)
    class Occupied(position: Position, piece: Piece) : Square(position, piece)
    class Dynamic(position: Position, private val board: Board) : Square(position) {
        override val piece: Piece?
            get() = board.positionMap[position]
    }
}