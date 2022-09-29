package dev.kason.khess.core.board

import dev.kason.khess.core.*
import dev.kason.khess.core.pieces.Piece
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

@Serializable(with = Square.Serializer::class)
sealed class Square {

    abstract val piece: Piece?
    abstract val position: Position

    val isEmpty get() = piece == null
    val isOccupied get() = piece != null
    val isLightSquare get() = position.isLightSquare
    val isDarkSquare get() = position.isDarkSquare

    class Empty internal constructor(override val position: Position) : Square() {

        override val piece = null

        override fun equals(other: Any?): Boolean = other is Empty && other.position == position
        override fun hashCode(): Int = position.hashCode()
        override fun toString() = "Empty($position)"
    }

    class Occupied internal constructor(override val piece: Piece, override val position: Position) : Square() {

        override fun equals(other: Any?): Boolean =
            other is Occupied && other.piece == piece && other.position == position

        override fun hashCode(): Int = piece.hashCode() * 31 + position.hashCode()
        override fun toString() = "Occupied($piece, $position)"
    }

    object Serializer : KSerializer<Square> {

        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Square", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Square {
            TODO("Not yet implemented")
        }

        override fun serialize(encoder: Encoder, value: Square) {
            TODO("Not yet implemented")
        }

    }

}