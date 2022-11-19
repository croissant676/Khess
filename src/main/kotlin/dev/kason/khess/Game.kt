package dev.kason.khess

import kotlinx.serialization.Serializable
import org.http4k.websocket.*
import java.awt.*
import kotlin.math.*
import kotlin.random.Random

@Serializable
data class Position(val x: Int, val y: Int)
object Game {
    val activePlayers: MutableSet<Player> = mutableSetOf()
    val pieces: MutableMap<Position, Piece> = mutableMapOf()
    fun hasPieceBetween(startPosition: Position, endPosition: Position, direction: Direction): Boolean {
        var currentX = startPosition.x
        var currentY = startPosition.y
        while (currentX != endPosition.x || currentY != endPosition.y) {
            val nextPosition = direction.applyTo(currentX, currentY)
            if (this.pieces[nextPosition] != null) return true
            currentX = nextPosition.x
            currentY = nextPosition.y
        }
        return false
    }

    fun addNewPlayer(newPlayer: PlayerCreateRequest, websocket: Websocket): Player {
        if (newPlayer.color != null) {
            try {
                val color = Color.decode(newPlayer.color)
                newPlayer.color = "#${Integer.toHexString(color.rgb).substring(2)}"
            } catch (_: NumberFormatException) {
                newPlayer.color = generateRandomColor()
            }
        } else {
            newPlayer.color = generateRandomColor()
        }
        val player = Player(newPlayer.name, newPlayer.color!!, websocket)
        activePlayers.add(player)
        updateBoardVariables()
        player.spawn(computeSpawnLocation(), newPlayer.startingType)

        return player
    }

    fun acceptMoveRequest(player: Player, moveRequest: PlayerMoveRequest): Boolean {
        val piece = pieces[moveRequest.from] ?: return false
        if (piece.player != player) return false
        if (moveRequest.to !in Game) return false // out of bounds
        val viewFrame = player.viewFrame ?: return false
        if (moveRequest.to !in viewFrame) return false // out of view
        if (piece.validateThenMove(moveRequest.to.x, moveRequest.to.y)) {
            player.lastMoveTimeMillis = System.currentTimeMillis()
            activePlayers.filter {
                val playerViewFrame = it.viewFrame
                playerViewFrame != null && (moveRequest.to in playerViewFrame || piece.position in playerViewFrame)
            }.forEach {
                it.sendViewFrame()
            }
            pieces[moveRequest.to]?.delete()
            return true
        }
        return false
    }

    var boardSize: Int = 0
    var minorSide = 0
    var majorSide = 0
    fun updateBoardVariables() {
        boardSize = ceil(sqrt((30 * activePlayers.size).toDouble())).toInt()
        minorSide = boardSize / 2
        majorSide = boardSize - minorSide
    }

    private fun computeSpawnLocation(): Position {
        var x: Int = Random.nextInt(-minorSide + 1, majorSide - 1)
        var y: Int = Random.nextInt(-minorSide + 1, majorSide - 1)
        fun satisfiesSpawnCondition(): Boolean {
            if (pieces[Position(x, y)] != null) return false
            for (i in -1..1) {
                for (j in -1..1) {
                    if (pieces[Position(x + i, y + j)] != null) return false
                }
            }
            return true
        }
        while (!satisfiesSpawnCondition()) {
            x = Random.nextInt(-minorSide + 1, majorSide - 1)
            y = Random.nextInt(-minorSide + 1, majorSide - 1)
        }
        return Position(x, y)
    }

    operator fun contains(position: Position): Boolean = position.x in -minorSide..majorSide && position.y in -minorSide..majorSide
    operator fun iterator(): Iterator<Position> = object : Iterator<Position> {
        var x = -minorSide
        var y = -minorSide
        override fun hasNext(): Boolean = x <= majorSide && y <= majorSide
        override fun next(): Position {
            val position = Position(x, y)
            x++
            if (x > majorSide) {
                x = -minorSide
                y++
            }
            return position
        }
    }

}

private var playerInc = 0

data class Player(val name: String, val color: String, val websocket: Websocket, val id: Int = playerInc++) {
    val pieces: MutableSet<Piece> = mutableSetOf()
    var currentKingPiece: King? = null
    fun determineOptimalPiecePosition(): Position {
        val kingPosition = currentKingPiece!!.position
        var distanceFromKing = 1
        val positions: MutableSet<Position> = mutableSetOf()
        while (positions.isEmpty()) {
            fun checkPosition(x: Int, y: Int) {
                val position = Position(x, y)
                if (position !in Game) return
                if (viewFrame?.contains(position) == true) return
                if (Game.pieces[position] == null) positions.add(position)
            }

            val startX = kingPosition.x - distanceFromKing
            val startY = kingPosition.y - distanceFromKing
            val endX = kingPosition.x + distanceFromKing
            val endY = kingPosition.y + distanceFromKing
            for (x in startX..endX) {
                checkPosition(x, startY)
                checkPosition(x, endY)
            }
            for (y in startY..endY) {
                checkPosition(startX, y)
                checkPosition(endX, y)
            }
            distanceFromKing++
        }
        return positions.random()
    }

    fun spawn(spawnLocation: Position, startingType: StartingType) {
        if (System.currentTimeMillis() - lastDeathTimeMillis < 5000) return // 5 second respawn cooldown
        val king = King(this)
        king.directlyMove(spawnLocation.x, spawnLocation.y)
        currentKingPiece = king
        when (startingType) {
            StartingType.Pawns -> repeat(4) {
                spawnPiece(Pawn(this))
            }
            StartingType.Knight -> {
                repeat(3) {
                    spawnPiece(Pawn(this))
                }
                spawnPiece(Knight(this))
            }
            StartingType.Bishop -> {
                repeat(3) {
                    spawnPiece(Pawn(this))
                }
                spawnPiece(Bishop(this))
            }
        }
    }

    fun spawnPiece(piece: Piece) {
        val position = determineOptimalPiecePosition()
        piece.directlyMove(position.x, position.y)
    }

    fun kill() {
        this.currentKingPiece = null
        for (piece in this.pieces) {
            if (piece !is King) piece.delete()
        }
        this.viewFrame = null
        Game.activePlayers.remove(this)
        this.lastDeathTimeMillis = System.currentTimeMillis()
    }

    var viewFrame: ViewFrame? = null
    var lastMoveTimeMillis = System.currentTimeMillis()
    var lastDeathTimeMillis = System.currentTimeMillis()

    class ViewFrame(val player: Player) {
        var sideLength = 4
        val minorSide get() = (sideLength - 1) / 2
        val majorSide get() = sideLength - minorSide
        val topCorner
            get() = Position(player.currentKingPiece!!.position.x - minorSide, player.currentKingPiece!!.position.y - minorSide)
        val bottomCorner
            get() = Position(player.currentKingPiece!!.position.x + majorSide, player.currentKingPiece!!.position.y + majorSide)

        operator fun contains(position: Position): Boolean {
            return position.x in topCorner.x..bottomCorner.x
                && position.y in topCorner.y..bottomCorner.y
        }

        operator fun iterator(): Iterator<Position> = object : Iterator<Position> {
            var x = player.currentKingPiece!!.position.x - minorSide
            var y = player.currentKingPiece!!.position.y - minorSide
            override fun hasNext(): Boolean =
                x <= player.currentKingPiece!!.position.x + majorSide && y <= player.currentKingPiece!!.position.y + majorSide

            override fun next(): Position {
                val position = Position(x, y)
                x++
                if (x > player.currentKingPiece!!.position.x + majorSide) {
                    x = player.currentKingPiece!!.position.x - minorSide
                    y++
                }
                return position
            }
        }
    }
}

enum class StartingType {
    Pawns,
    Knight,
    Bishop;
}
