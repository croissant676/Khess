package dev.kason.khess

import kotlin.math.*

abstract class Piece(val player: Player) {
    var x: Int = 0
    var y: Int = 0
    val position: Position get() = Position(x, y)

    init {
        @Suppress("LeakingThis")
        player.pieces.add(this)
    }
    abstract fun validateThenMove(x: Int, y: Int): Boolean
    fun directlyMove(x: Int, y: Int): Boolean { // assume all checks have been done
        this.x = x
        this.y = y
        Game.pieces[position] = this
        return true
    }

    open fun delete() {
        Game.pieces.remove(position)
        player.pieces.remove(this)
        this.x = Int.MIN_VALUE
        this.y = Int.MIN_VALUE
    }
}

class Pawn(player: Player) : Piece(player) {
    var currentlyFacingDirection: Direction? = null

    override fun validateThenMove(x: Int, y: Int): Boolean {
        if (this.x !in (x - 1..x + 1) || this.y !in (y - 1..y + 1)) return false
        val direction = position.getDirectionTo(x, y) ?: return false
        if (direction.isCardinal) {
            if (Game.pieces[Position(x, y)] != null) return false
            currentlyFacingDirection = direction
        } else {
            if (currentlyFacingDirection == null) return false
            if (direction !in currentlyFacingDirection!!.pawnDirectionComponents!!) return false
            if (Game.pieces[Position(x, y)] == null) return false
        }
        return directlyMove(x, y)
    }
}

class Knight(player: Player) : Piece(player) {
    override fun validateThenMove(x: Int, y: Int): Boolean {
        val xDiff = abs(x - this.x)
        val yDiff = abs(y - this.y)
        if (xDiff + yDiff != 3 || abs(xDiff - yDiff) != 1) return false
        return directlyMove(x, y)
    }
}

class Bishop(player: Player) : Piece(player) {
    override fun validateThenMove(x: Int, y: Int): Boolean {
        val direction = position.getDirectionTo(x, y) ?: return false
        if (!direction.isDiagonal) return false
        if (Game.hasPieceBetween(position, Position(x, y), direction)) return false
        return directlyMove(x, y)
    }
}

class Rook(player: Player) : Piece(player) {
    override fun validateThenMove(x: Int, y: Int): Boolean {
        val direction = position.getDirectionTo(x, y) ?: return false
        if (!direction.isCardinal) return false
        if (Game.hasPieceBetween(position, Position(x, y), direction)) return false
        return directlyMove(x, y)
    }
}

class Queen(player: Player) : Piece(player) {
    override fun validateThenMove(x: Int, y: Int): Boolean {
        val direction = position.getDirectionTo(x, y) ?: return false
        if (Game.hasPieceBetween(position, Position(x, y), direction)) return false
        return directlyMove(x, y)
    }
}

class King(player: Player) : Piece(player) {
    override fun validateThenMove(x: Int, y: Int): Boolean {
        val xDiff = abs(x - this.x)
        val yDiff = abs(y - this.y)
        if (xDiff > 1 || yDiff > 1) return false
        return directlyMove(x, y)
    }

    override fun delete() {
        super.delete()
        player.kill()
    }
}

val Piece.value: Int
    get() = when (this) {
        is Pawn -> 1
        is Knight -> 3
        is Bishop -> 3
        is Rook -> 5
        is Queen -> 9
        is King -> 0
        else -> error("Unknown piece type")
    }