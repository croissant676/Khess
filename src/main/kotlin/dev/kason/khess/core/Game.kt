package dev.kason.khess.core

import dev.kason.khess.core.board.*
import kotlinx.serialization.Serializable

class Game : KhessEntity {
    override val game: Game get() = this

    val board = Board(this)
    val boardSizeService = BoardSizeService(this)
}

interface KhessEntity {
    val game: Game
}