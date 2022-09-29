package dev.kason.khess.core.board

import dev.kason.khess.core.*

class BoardSizeService(override val game: Game) : KhessEntity {

    val board = game.board

    fun refitBoard() {

    }
}

fun Board.size(): BoardDimensions {
    TODO()
}