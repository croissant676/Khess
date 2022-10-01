package dev.kason.khess

import dev.kason.khess.server.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.*
import java.util.TreeSet

class Game {
    val board: Board = Board(this)
    val pieces: List<Piece> get() = board.positionMap.values.toList()
    val players: MutableSet<Player> = TreeSet(Comparator.comparing { it.pointCount })
    fun acceptJoinRequest(joinRequest: JoinRequest, socket: WebSocketServerSession): Player {
        logger.debug("Accepting join request from ${joinRequest.name}")
        val player = createPlayer(joinRequest, socket)
        this.players += player
        onJoin(player)
        return player
    }

    private fun createPlayer(joinRequest: JoinRequest, socket: WebSocketServerSession): Player {
        val sessionId = generateSessionId()
        var newColor: Color? = null
        if (players.any { it.color == joinRequest.color && it.name == joinRequest.name }) {
            newColor = randomColor()
        }
        logger.debug("Creating player with name ${joinRequest.name} and color ${newColor ?: joinRequest.color}: $sessionId")
        return Player(
            joinRequest.name,
            newColor ?: joinRequest.color,
            sessionId,
            socket,
            this
        )
    }

    private fun onJoin(player: Player) {
        players += player
        board.updateSize(players.size)
        logger.info("Player ${player.name} joined the game ${player.toData()}")
    }

    suspend fun acceptMoveRequest(request: MoveRequest, player: Player) {
        logger.debug("Accepting move request from ${player.name}")
        val piece = request.piece.toPiece(this)
        require(piece != null) { "Invalid piece data" }
        require(piece.player == player) { "You can't move a piece that you don't own" }
        val position = request.position
        val startPosition = piece.position
        player.movePiece(piece, position)
        updateViewFrameForOthers(startPosition, position, player)
    }

    suspend fun updateViewFrameForOthers(startPosition: Position, endPosition: Position, player: Player) {
        players.filter { it != player && (startPosition in player.frame || endPosition in player.frame) }
            .forEach { it.updateViewFrame() }
    }

    interface Entity {
        val game: Game
    }
}
@Serializable
enum class InitialPiecesType {
    @SerialName("pawns")
    FourPawns,
    @SerialName("bishop")
    BishopAndPawn,
    @SerialName("knight")
    KnightAndPawn;
}