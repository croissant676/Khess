package dev.kason.khess

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlin.random.Random

@Serializable(with = Color.Serializer::class)
data class Color(val red: UByte, val green: UByte, val blue: UByte) {
    override fun toString(): String = "#" + red.toString(16).padStart(2, '0') +
            green.toString(16).padStart(2, '0') + blue.toString(16).padStart(2, '0')

    object Serializer : KSerializer<Color> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Color", PrimitiveKind.STRING)
        override fun serialize(encoder: Encoder, value: Color) = encoder.encodeString(value.toString())
        override fun deserialize(decoder: Decoder): Color = decodeColor(decoder.decodeString())
    }
}

private const val GoldenRatioConjugate = 0.618033988749895

fun decodeColor(web: String): Color {
    val red = web.substring(1, 3).toInt(16).toUByte()
    val green = web.substring(3, 5).toInt(16).toUByte()
    val blue = web.substring(5, 7).toInt(16).toUByte()
    return Color(red, green, blue)
}

fun randomColor(): Color {
    val hue = ((Random.nextDouble() + GoldenRatioConjugate) % 1.0).toFloat()
    val jColor = java.awt.Color.HSBtoRGB(hue, 0.3f, Random.nextDouble(0.8, 1.0).toFloat())
    return Color(
            ((jColor shr 16) and 0xFF).toUByte(), ((jColor shr 8) and 0xFF).toUByte(), (jColor and 0xFF).toUByte()
    )
}