package dev.kason.khess.core

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

@Serializable(with = Position.Serializer::class)
open class Position(open val x: Int, open val y: Int) {

    override fun equals(other: Any?) = (other === this) || (other is Position && x == other.x && y == other.y)
    override fun hashCode(): Int = x * 31 + y
    override fun toString(): String = "($x, $y)"

    object Serializer: KSerializer<Position> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Position") {
            element<Int>("x")
            element<Int>("y")
        }

        override fun deserialize(decoder: Decoder): Position {
            val input = decoder.beginStructure(descriptor)
            var x = 0
            var y = 0
            loop@ while (true) {
                when (val i = input.decodeElementIndex(descriptor)) {
                    CompositeDecoder.DECODE_DONE -> break@loop
                    0 -> x = input.decodeIntElement(descriptor, i)
                    1 -> y = input.decodeIntElement(descriptor, i)
                    else -> throw SerializationException("Unknown index $i")
                }
            }
            input.endStructure(descriptor)
            return Position(x, y)
        }

        override fun serialize(encoder: Encoder, value: Position) {
            val output = encoder.beginStructure(descriptor)
            output.encodeIntElement(descriptor, 0, value.x)
            output.encodeIntElement(descriptor, 1, value.y)
            output.endStructure(descriptor)
        }
    }
}

@Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
@Serializable(with = Position.Serializer::class)
class MutablePosition(override var x: Int, override var y: Int) : Position(x, y) {

    fun immutable() = Position(x, y)

}

operator fun Position.plus(other: Position) = Position(x + other.x, y + other.y)
operator fun Position.minus(other: Position) = Position(x - other.x, y - other.y)

operator fun Position.component1() = x
operator fun Position.component2() = y

fun Position.toMutable() = MutablePosition(x, y)

val Position.isLightSquare: Boolean
    get() = (x + y) % 2 == 0
val Position.isDarkSquare: Boolean
    get() = (x + y) % 2 == 1

fun pos(x: Int, y: Int) = Position(x, y)