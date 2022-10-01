package dev.kason.khess.server

import dev.kason.khess.*
import kotlinx.serialization.*

@Serializable
class ErrorResponse(
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
@Serializable
class PossibleMovesResponse(
    val moves: MutableSet<Move>,
    val timestamp: Long = System.currentTimeMillis()
) {
    constructor(pieces: Set<Piece>) : this(moves = mutableSetOf()) {
        for (piece in pieces) {
            this.moves += Move(piece.toData(), piece.getMoves())
        }
    }
    @Serializable
    class Move(
        val piece: Piece.Data,
        val positions: Set<Position>
    )
}