@file:Suppress("MemberVisibilityCanBePrivate")

package dev.kason.khess

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
    override val game: Game
) : Game.Entity {
    var kingPiece: Piece? = null
    val pieces: MutableSet<Piece> = mutableSetOf()
    var lastMoveTimestamp: Long = 0
    var respawnCountdown: Int = -1
    var moveInterval: Int = 1000
    val frame: ViewFrame = ViewFrame(this)
    fun spawn() {
        game.board
    }

    fun movePiece(piece: Piece, to: Position) {
        require(System.currentTimeMillis() - lastMoveTimestamp >= moveInterval) {
            "You cannot move yet."
        }
        require(piece.player == this) { "You can only move your own pieces." }
        require(to in piece.getMoves()) { "You can't move to $to" }
        piece.move(to)
    }

    fun kill() {

    }
    @Serializable
    class Data(
        val name: String,
        val color: Color,
        val id: String
    ) {
        fun toPlayer(game: Game) = game.players.first { it.sessionId == id }
    }

    fun toData(): Data = Data(name, color, sessionId)
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

        override fun serialize(encoder: Encoder, value: ViewFrame) {
            return encoder.encodeStructure(descriptor) {
                encodeIntElement(descriptor, 0, value.startX)
                encodeIntElement(descriptor, 1, value.startY)
                encodeIntElement(descriptor, 2, value.endX)
                encodeIntElement(descriptor, 3, value.endY)
                encodeIntElement(descriptor, 4, value.stage)
                val pieceData = value.piecesInRange.map { it.toData() }
                encodeSerializableElement(descriptor, 5, ListSerializer(Piece.Data.serializer()), pieceData)
            }
        }
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ViewFrame") {
            element<Int>("startX")
            element<Int>("startY")
            element<Int>("endX")
            element<Int>("endY")
            element<Int>("stage")
            element<List<Piece.Data>>("pieces")
        }

        override fun deserialize(decoder: Decoder): ViewFrame =
            throw UnsupportedOperationException("ViewFrame cannot be deserialized.")
    }
}

fun generateSessionId(): String = Random.nextBytes(16).encodeBase64()