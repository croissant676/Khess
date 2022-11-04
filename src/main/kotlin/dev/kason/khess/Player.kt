package dev.kason.khess

import kotlinx.serialization.Serializable
import java.util.UUID

class Player(
    val name: String,
    val color: String,
    val uuid: UUID
) {
    // sent to the client
    @Serializable
    data class Representation(
        val name: String,
        val color: String,
        val id: String, // UUID is not serializable
    )

    @Serializable
    data class SelfRepresentation(
        val name: String,
        val color: String,
        val id: String, // UUID is not serializable
        val viewFrame: ViewFrame.Representation,
        val otherPieces: List<Piece.Representation>
    )

    fun representation() = Representation(name, color, uuid.toString())
}

class ViewFrame(val player: Player) {
    // serialized pieces
    @Serializable
    class Representation(
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int
    )
}