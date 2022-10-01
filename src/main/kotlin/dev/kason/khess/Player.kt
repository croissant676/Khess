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

    var kingPiece: Piece? = null
    val pieces: MutableSet<Piece> = mutableSetOf()
    var lastMoveTimestamp: Long = 0
    var lastDeathTimestamp: Long = 0
    var moveInterval: Int = 1000
    val frame: ViewFrame = ViewFrame(this)
    var pointCount: Int = 4
    fun spawn() {
        require(System.currentTimeMillis() - lastDeathTimestamp >= DeathInterval) {
            "Player $name is still in death state"
        }
    }

    // find a place on the board that is 3 squares away from any other player
    fun calculateSuitableLocation(): Position {
        val occupied = game.players.flatMap { it.pieces }.map { it.position }.toSet()
        TODO()
    }

    fun spawnPiece(piece: Piece) {
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
            encodeIntElement(descriptor, 0, value.startX)
            encodeIntElement(descriptor, 1, value.startY)
            encodeIntElement(descriptor, 2, value.endX)
            encodeIntElement(descriptor, 3, value.endY)
            encodeIntElement(descriptor, 4, value.stage)
            val pieceData = value.piecesInRange.map { it.toData() }
            encodeSerializableElement(descriptor, 5, ListSerializer(Piece.Data.serializer()), pieceData)
        }

        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ViewFrame") {
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