package dev.kason.khess.core.pieces

import dev.kason.khess.core.Game
import dev.kason.khess.core.KhessEntity
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

@Serializable(with = Piece.Serializer::class)
abstract class Piece(val type: PieceType<*>, override val game: Game): KhessEntity {



    object Serializer: KSerializer<Piece> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Khess.Piece") {
            element("type", PieceType.Serializer.descriptor)
        }

        override fun serialize(encoder: Encoder, value: Piece) {

        }

        override fun deserialize(decoder: Decoder): Piece {
            TODO()
        }
    }

}

@Serializable(with = PieceType.Serializer::class)
sealed class PieceType<T : Piece>(val type: Int) {

    object PawnType : PieceType<Pawn>(0)
    object KnightType : PieceType<Knight>(1)
    object BishopType : PieceType<Bishop>(2)
    object RookType : PieceType<Rook>(3)
    object QueenType : PieceType<Queen>(4)
    object KingType : PieceType<King>(5)

    object Serializer : KSerializer<PieceType<*>> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Khess.PieceType", PrimitiveKind.INT)

        override fun serialize(encoder: Encoder, value: PieceType<*>) {
            encoder.encodeInt(value.type)
        }

        override fun deserialize(decoder: Decoder): PieceType<*> {
            return when (decoder.decodeInt()) {
                0 -> PawnType
                1 -> KnightType
                2 -> BishopType
                3 -> RookType
                4 -> QueenType
                5 -> KingType
                else -> throw IllegalArgumentException("Invalid piece type")
            }
        }
    }
}