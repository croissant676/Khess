package dev.kason.khess

class Game {
    val board: Board = Board(this)
    val pieces: List<Piece> get() = board.positionMap.values.toList()
    val players: List<Player> = mutableListOf()

    companion object {
        var current: Game? = null
    }

    interface Entity {
        val game: Game
    }
}