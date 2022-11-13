package dev.kason.khess

private val cardinal = listOf(Direction.Up, Direction.Down, Direction.Left, Direction.Right)
private val diagonal = listOf(Direction.UpLeft, Direction.UpRight, Direction.DownLeft, Direction.DownRight)

enum class Direction {
    Up, Down, Left, Right, // cardinal
    UpLeft, UpRight, DownLeft, DownRight; // diagonal

    val isCardinal: Boolean get() = this in cardinal
    val isDiagonal: Boolean get() = this in diagonal
    val pawnDirectionComponents: List<Direction>?
        get() = when (this) {
            Up -> listOf(UpLeft, UpRight)
            Down -> listOf(DownLeft, DownRight)
            Left -> listOf(UpLeft, DownLeft)
            Right -> listOf(UpRight, DownRight)
            else -> null
        }

    fun applyTo(x: Int, y: Int): Position = when (this) {
        Up -> Position(x, y + 1)
        Down -> Position(x, y - 1)
        Left -> Position(x - 1, y)
        Right -> Position(x + 1, y)
        UpLeft -> Position(x - 1, y + 1)
        UpRight -> Position(x + 1, y + 1)
        DownLeft -> Position(x - 1, y - 1)
        DownRight -> Position(x + 1, y - 1)
    }
}

fun Position.getDirectionTo(x: Int, y: Int): Direction? {
    val xDiff = x - this.x
    val yDiff = y - this.y
    return when {
        xDiff == 0 && yDiff > 0 -> Direction.Up
        xDiff == 0 && yDiff < 0 -> Direction.Down
        xDiff > 0 && yDiff == 0 -> Direction.Right
        xDiff < 0 && yDiff == 0 -> Direction.Left
        xDiff == yDiff && xDiff > 0 -> Direction.UpRight
        xDiff == yDiff && xDiff < 0 -> Direction.DownLeft
        xDiff == -yDiff && xDiff > 0 -> Direction.UpLeft
        xDiff == -yDiff && xDiff < 0 -> Direction.DownRight
        else -> null
    }
}