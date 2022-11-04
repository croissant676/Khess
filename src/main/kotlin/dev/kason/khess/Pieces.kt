package dev.kason.khess

import kotlinx.serialization.*
import java.util.UUID

abstract class Piece(val player: UUID, val type: Type) {
    // position of piece
    var x: Int = 0
        private set
    var y: Int = 0
        private set
    val position by PositionCreator(::x, ::y)

    @Serializable
    enum class Type {
        @SerialName("p")
        Pawn,
        @SerialName("r")
        Rook,
        @SerialName("n")
        Knight,
        @SerialName("b")
        Bishop,
        @SerialName("q")
        Queen,
        @SerialName("k")
        King
    }

    @Serializable
    data class Representation(
        val type: Type,
        val position: Position,
        val player: String // player uuid
    )

    abstract fun moves(): List<Position>

}