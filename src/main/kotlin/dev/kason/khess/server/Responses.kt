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
    val types: String = "possible_moves",
    val moves: MutableSet<Move>,
    val timestamp: Long = System.currentTimeMillis()
) {
    constructor(pieces: Set<Piece>) : this(moves = mutableSetOf()) {
        for (piece in pieces) {
            this.moves += Move(piece.toData(), piece.getMoves())
        }
    }
    constructor(player: Player): this(player.pieces)
    @Serializable
    class Move(
        val piece: Piece.Data,
        val positions: Set<Position>
    )
}

@Serializable
class LeaderboardResponse(
    val type: String = "leaderboard",
    val players: List<Player.Data>,
    val timestamp: Long = System.currentTimeMillis()
)