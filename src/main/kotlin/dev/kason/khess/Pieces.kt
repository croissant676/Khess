@file:Suppress("MemberVisibilityCanBePrivate", "DuplicatedCode")

package dev.kason.khess

import kotlinx.serialization.*

abstract class Piece(
    val player: Player,
    val position: MutablePosition,
    override val game: Game,
) : Game.Entity {
    val pieceType: PieceType by lazy { determinePieceType() }
    private fun determinePieceType() = when (this) {
        is Pawn -> PieceType.Pawn
        is Knight -> PieceType.Knight
        is Bishop -> PieceType.Bishop
        is Rook -> PieceType.Rook
        is Queen -> PieceType.Queen
        is King -> PieceType.King
        else -> error("Unknown piece type")
    }

    open fun onDelete() {}
    fun delete() {
        onDelete()
        player.pieces -= this
        game.board.positionMap.remove(this.position)
    }

    private fun moveToInternal(position: Position) {
        game.board[this.position] = null
        this.position.x = position.x
        this.position.y = position.y
        game.board[position] = this
    }

    open fun onMove(position: Position) {}
    fun move(position: Position) {
        onMove(position)
        moveToInternal(position)
    }

    open fun onCapture(capturedPiece: Piece) {}
    fun capture(position: Position) {
        val piece = game.board.getPiece(position)
        checkNotNull(piece) { "No piece at $position to capture." }
        onCapture(piece)
        piece.delete()
        moveToInternal(position)
    }

    abstract fun getMoves(): Set<Position>
    fun Set<Position>.filterInFrame() = filter { it in player.frame && it in game.board.dimensions }.toSet()
    @Serializable
    class Data(
        val type: PieceType,
        val player: Player.Data,
        val position: Position
    ) {
        fun toPiece(game: Game): Piece? = game.board.getPiece(position)
    }

    fun toData() = Data(pieceType, player.toData(), position.copy())
}

class Pawn(player: Player, position: Position, game: Game) : Piece(player, position.toMutable(), game) {
    var currentlyFacingDirection = -1
    var capturedPieceCount = 0
    override fun onCapture(capturedPiece: Piece) {
        capturedPieceCount++
        if (capturedPieceCount == 4) {
            val newQueenPiece = Queen(player, Position.Placeholder, game)
            delete()
            newQueenPiece.move(position)
        }
    }

    override fun onMove(position: Position) {
        when {
            position.x > this.position.x -> currentlyFacingDirection = 1 // right
            position.x < this.position.x -> currentlyFacingDirection = 2 // left
            position.y > this.position.y -> currentlyFacingDirection = 3 // up
            position.y < this.position.y -> currentlyFacingDirection = 4 // down
        }
    }

    override fun getMoves(): Set<Position> {
        val position = mutableSetOf<Position>()
        position += setOf(
            Position(this.position.x, this.position.y + 1),
            Position(this.position.x, this.position.y - 1),
            Position(this.position.x + 1, this.position.y),
            Position(this.position.x - 1, this.position.y),
        ).filter { game.board[it].isEmpty }
        position += when (currentlyFacingDirection) {
            1 -> setOf(
                Position(this.position.x + 1, this.position.y + 1),
                Position(this.position.x + 1, this.position.y - 1),
            )
            2 -> setOf(
                Position(this.position.x - 1, this.position.y + 1),
                Position(this.position.x - 1, this.position.y - 1),
            )
            3 -> setOf(
                Position(this.position.x + 1, this.position.y + 1),
                Position(this.position.x - 1, this.position.y + 1),
            )
            4 -> setOf(
                Position(this.position.x + 1, this.position.y - 1),
                Position(this.position.x - 1, this.position.y - 1),
            )
            else -> emptySet()
        }.filter { game.board[it].isOccupied }
        return position.filterInFrame()
    }
}

class Knight(player: Player, position: Position, game: Game) : Piece(player, position.toMutable(), game) {
    override fun getMoves(): Set<Position> = setOf(
        Position(position.x + 1, position.y + 2),
        Position(position.x + 2, position.y + 1),
        Position(position.x + 2, position.y - 1),
        Position(position.x + 1, position.y - 2),
        Position(position.x - 1, position.y - 2),
        Position(position.x - 2, position.y - 1),
        Position(position.x - 2, position.y + 1),
        Position(position.x - 1, position.y + 2),
    ).filterInFrame()
}

class Bishop(player: Player, position: Position, game: Game) : Piece(player, position.toMutable(), game) {
    override fun getMoves(): Set<Position> {
        val set = mutableSetOf<Position>()
        repeat(4) {
            val position = this.position.toMutable()
            var shouldStopNextIter = false
            while (position in player.frame && position in game.board.dimensions && !shouldStopNextIter) {
                shouldStopNextIter = game.board[position].isOccupied
                position.x += if (it == 0 || it == 3) 1 else -1
                position.y += if (it == 0 || it == 1) 1 else -1
                set += position
            }
        }
        return set
    }
}

class Rook(player: Player, position: Position, game: Game) : Piece(player, position.toMutable(), game) {
    override fun getMoves(): Set<Position> {
        val set = mutableSetOf<Position>()
        repeat(4) {
            val position = this.position.toMutable()
            var shouldStopNextIter = false
            while (position in player.frame && position in game.board.dimensions && !shouldStopNextIter) {
                shouldStopNextIter = game.board[position].isOccupied
                when (it) {
                    0 -> position.x++
                    1 -> position.y++
                    2 -> position.x--
                    3 -> position.y--
                }
                set += position
            }
        }
        return set
    }
}

class Queen(player: Player, position: Position, game: Game) : Piece(player, position.toMutable(), game) {
    override fun getMoves(): Set<Position> {
        val set = mutableSetOf<Position>()
        repeat(4) {
            val position = this.position.toMutable()
            var shouldStopNextIter = false
            while (position in player.frame && position in game.board.dimensions && !shouldStopNextIter) {
                shouldStopNextIter = game.board[position].isOccupied
                when (it) {
                    0 -> position.x++
                    1 -> position.y++
                    2 -> position.x--
                    3 -> position.y--
                }
                set += position
            }
        }
        repeat(4) {
            val position = this.position.toMutable()
            var shouldStopNextIter = false
            while (position in player.frame && position in game.board.dimensions && !shouldStopNextIter) {
                shouldStopNextIter = game.board[position].isOccupied
                position.x += if (it == 0 || it == 3) 1 else -1
                position.y += if (it == 0 || it == 1) 1 else -1
                set += position
            }
        }
        return set
    }
}

class King(player: Player, position: Position, game: Game) : Piece(player, position.toMutable(), game) {
    override fun getMoves(): Set<Position> = setOf(
        Position(position.x + 1, position.y + 1),
        Position(position.x + 1, position.y),
        Position(position.x + 1, position.y - 1),
        Position(position.x, position.y + 1),
        Position(position.x, position.y - 1),
        Position(position.x - 1, position.y + 1),
        Position(position.x - 1, position.y),
        Position(position.x - 1, position.y - 1),
    ).filterInFrame()

    override fun onDelete() {
        player.kill()
    }
}
@Serializable
enum class PieceType(val value: Int) {
    @SerialName("pawn")
    Pawn(1),
    @SerialName("knight")
    Knight(3),
    @SerialName("bishop")
    Bishop(3),
    @SerialName("rook")
    Rook(5),
    @SerialName("queen")
    Queen(9),
    @SerialName("king")
    King(15);
}