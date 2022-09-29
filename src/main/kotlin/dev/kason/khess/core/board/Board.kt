package dev.kason.khess.core.board

import dev.kason.khess.core.*
import dev.kason.khess.core.pieces.Piece

class Board(override val game: Game): KhessEntity {

    val pieces: MutableMap<Position, Piece> = mutableMapOf()


}

@JvmInline
value class BoardDimensions(val sideLength: Int) {



}