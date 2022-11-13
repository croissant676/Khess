package dev.kason.khess

import kotlinx.serialization.*
import org.http4k.websocket.*

@Serializable
sealed class Response
@Serializable
@SerialName("error")
data class ErrorResponse(val error: String) : Response()
@Serializable
@SerialName("piece")
data class PieceResponse(
    val type: PieceType,
    val color: String,
    val position: Position,
    val player: Int,
    val direction: Int? = null
) : Response()
@Serializable
enum class PieceType {
    @SerialName("pawn")
    Pawn,
    @SerialName("rook")
    Rook,
    @SerialName("knight")
    Knight,
    @SerialName("bishop")
    Bishop,
    @SerialName("queen")
    Queen,
    @SerialName("king")
    King
}

val Piece.pieceType: PieceType
    get() = when (this) {
        is Pawn -> PieceType.Pawn
        is Rook -> PieceType.Rook
        is Knight -> PieceType.Knight
        is Bishop -> PieceType.Bishop
        is Queen -> PieceType.Queen
        is King -> PieceType.King
        else -> error("Unknown piece type")
    }

fun Piece.toResponse(): PieceResponse {
    if (this is Pawn) {
        val direction = this.currentlyFacingDirection
        return PieceResponse(
            type = PieceType.Pawn,
            color = player.color,
            position = position,
            player = player.id,
            direction = direction?.ordinal
        )
    }
    return PieceResponse(
        type = pieceType,
        color = player.color,
        position = position,
        player = player.id
    )
}
@Serializable
@SerialName("viewframe")
data class ViewFrameResponse(
    val pieces: List<PieceResponse>,
    @SerialName("top_corner")
    val topCorner: Position,
    @SerialName("bottom_corner")
    val bottomCorner: Position,
    @SerialName("side_len")
    val sideLength: Int,
    @SerialName("player_data")
    val playerData: List<PlayerRenderDataResponse>
) : Response()

@Serializable
@SerialName("player_render_data")
data class PlayerRenderDataResponse(
    val id: Int,
    val color: String,
    val name: String,
    @SerialName("is_alive")
    val isAlive: Boolean
) : Response()

fun Player.toRenderData(): PlayerRenderDataResponse {
    return PlayerRenderDataResponse(
        id = id,
        color = color,
        name = name,
        isAlive = currentKingPiece != null
    )
}
@Serializable
data class PlayerDataResponse(
    val id: Int,
    val color: String,
    val name: String,
    val isAlive: Boolean,
    val pieces: List<PieceResponse>
) : Response()

fun Player.ViewFrame.toResponse(): ViewFrameResponse {
    val pieces: MutableSet<Piece> = mutableSetOf()
    val otherPlayers: MutableSet<Player> = mutableSetOf()
    for (position in this) {
        val piece = Game.pieces[position] ?: continue
        pieces.add(piece)
        if (piece.player != this.player) {
            otherPlayers.add(piece.player)
        }
    }
    return ViewFrameResponse(
        pieces = pieces.map { it.toResponse() },
        topCorner = this.topCorner,
        bottomCorner = this.bottomCorner,
        sideLength = this.sideLength,
        playerData = otherPlayers.map { it.toRenderData() }
    )
}

fun Player.sendViewFrame() = websocket.respond(viewFrame!!.toResponse())
fun Player.toData(): PlayerDataResponse {
    return PlayerDataResponse(
        id = id,
        color = color,
        name = name,
        isAlive = currentKingPiece != null,
        pieces = pieces.map { it.toResponse() }
    )
}
@Serializable
@SerialName("game_info")
class BaseDataResponse(
    @SerialName("board_size")
    val boardSize: Int,
    @SerialName("minor_side")
    val minorSide: Int,
    @SerialName("major_side")
    val majorSide: Int,
    @SerialName("active_player_count")
    val activePlayerCount: Int
) : Response()

fun Game.createBaseDataResponse(): BaseDataResponse {
    return BaseDataResponse(
        boardSize = boardSize,
        minorSide = minorSide,
        majorSide = majorSide,
        activePlayerCount = activePlayers.size
    )
}