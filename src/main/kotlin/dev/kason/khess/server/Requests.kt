package dev.kason.khess.server

import dev.kason.khess.*
import kotlinx.serialization.*

@Serializable
sealed class Request
@Serializable
@SerialName("join")
data class JoinRequest(
    val name: String,
    val color: Color = randomColor(),
    @SerialName("start_type")
    val initialPiecesType: InitialPiecesType = InitialPiecesType.KnightAndPawn
) : Request()
@Serializable
@SerialName("move")
data class MoveRequest(
    val piece: Piece.Data,
    val position: Position
) : Request()

