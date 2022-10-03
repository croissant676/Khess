@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package dev.kason.khess

import io.ktor.server.websocket.*
import io.ktor.util.*
import kotlinx.serialization.*
import kotlinx.serialization.builtins.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.encoding.Encoder
import kotlin.random.*

class Player(
    val name: String,
    val color: Color,
    val sessionId: String,
    val session: WebSocketServerSession,
    override val game: Game
) : Game.Entity {
    companion object {
        const val DeathInterval = 5000L
    }

    var kingPiece: King? = null
    val pieces: MutableSet<Piece> = mutableSetOf()
    var lastMoveTimestamp: Long = 0
    var lastDeathTimestamp: Long = 0
    var moveInterval: Int = 1000
    val frame: ViewFrame = ViewFrame(this)
    var pointCount: Int = 4
    fun spawn(initialPiecesType: InitialPiecesType) {
        require(System.currentTimeMillis() - lastDeathTimestamp >= DeathInterval) {
            "Player $name is still in death state"
        }
        var position = game.board.dimensions.random()
        while (pieces.any { (it.position.x - position.x) < 3 && (it.position.y - position.y) < 3 }) {
            position = game.board.dimensions.random()
        }
        kingPiece = King(this, position, game)
        spawnPiece(kingPiece!!)
        when (initialPiecesType) {
            InitialPiecesType.FourPawns -> repeat(4) {
                val pawn = Pawn(this, chooseLocationAsCloseAsPossible(), game)
                spawnPiece(pawn)
            }
            InitialPiecesType.BishopAndPawn -> {
                val bishop = Bishop(this, chooseLocationAsCloseAsPossible(), game)
                spawnPiece(bishop)
                val pawn = Pawn(this, chooseLocationAsCloseAsPossible(), game)
                spawnPiece(pawn)
            }
            InitialPiecesType.KnightAndPawn -> {
                val knight = Knight(this, chooseLocationAsCloseAsPossible(), game)
                spawnPiece(knight)
                val pawn = Pawn(this, chooseLocationAsCloseAsPossible(), game)
                spawnPiece(pawn)
            }
        }
    }

    fun spawnPiece(piece: Piece) {
        this.pieces += piece
        game.board[piece.position] = piece
    }

    fun chooseLocationAsCloseAsPossible(): Position {
        val kingPosition = kingPiece!!.position
        var radius = 1
        while (true) {
            for (x in -radius..radius) {
                for (y in -radius..radius) {
                    val position = Position(kingPosition.x + x, kingPosition.y + y)
                    if (game.board[position].isEmpty) {
                        return position
                    }
                }
            }
            radius++
        }
    }

    fun movePiece(piece: Piece, to: Position) {
        require(System.currentTimeMillis() - lastMoveTimestamp >= moveInterval) { "You cannot move yet." }
        require(to in piece.getMoves()) { "You can't move to $to" } // also checks whether position is in the board & frame.
        require(piece.position in frame) { "Piece is out of frame" }
        require(piece.player == this) { "You can only move your own pieces." }
        piece.move(to)
    }

    fun kill() {
        lastDeathTimestamp = System.currentTimeMillis()
        pieces.forEach { it.delete() }
        kingPiece = null
        pointCount = 4
        moveInterval = 1000
    }

    override fun equals(other: Any?): Boolean = other is Player && other.sessionId == sessionId
    override fun hashCode(): Int = sessionId.hashCode()
    override fun toString(): String = "Player($name, $color, $sessionId)"
    @Serializable
    class Data(
        val name: String,
        val color: Color,
        val id: String,
        @SerialName("point_count")
        val pointCount: Int
    ) {
        fun toPlayer(game: Game) = game.players.first { it.sessionId == id }
    }

    fun toData(): Data = Data(name, color, sessionId, pointCount)
    suspend fun updateViewFrame() {
        session.sendSerialized(frame)
    }
}
@Serializable(with = ViewFrame.Serializer::class)
class ViewFrame(val player: Player) {
    var startX: Int = 0
    var startY: Int = 0
    var endX: Int = 0
    var endY: Int = 0
    var stage: Int = 0
    val width: Int
        get() = endX - startX
    val height: Int
        get() = endY - startY
    val horizontalRange: IntRange
        get() = startX..endX
    val verticalRange: IntRange
        get() = startY..endY
    val piecesInRange: Set<Piece>
        get() = player.game.board.positionMap.values.filter { it.position in this }.toSet()

    operator fun contains(position: Position): Boolean {
        return position.x in horizontalRange && position.y in verticalRange
    }

    object Serializer : KSerializer<ViewFrame> {
        override fun serialize(encoder: Encoder, value: ViewFrame) = encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, "view_frame")
            encodeIntElement(descriptor, 1, value.startX)
            encodeIntElement(descriptor, 2, value.startY)
            encodeIntElement(descriptor, 3, value.endX)
            encodeIntElement(descriptor, 4, value.endY)
            encodeIntElement(descriptor, 5, value.stage)
            val pieceData = value.piecesInRange.map { it.toData() }
            encodeSerializableElement(descriptor, 6, ListSerializer(Piece.Data.serializer()), pieceData)
        }

        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ViewFrame") {
            element<String>("type")
            element<Int>("start_x")
            element<Int>("start_x")
            element<Int>("end_x")
            element<Int>("end_x")
            element<Int>("stage")
            element<List<Piece.Data>>("pieces")
        }

        override fun deserialize(decoder: Decoder): ViewFrame =
            throw UnsupportedOperationException("ViewFrame cannot be deserialized.")
    }
}

fun generateSessionId(): String = Random.nextBytes(15).encodeBase64()