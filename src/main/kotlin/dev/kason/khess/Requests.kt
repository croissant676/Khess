package dev.kason.khess

import kotlinx.serialization.Serializable
import java.awt.*
import kotlin.random.*

@Serializable
sealed class Request
@Serializable
data class PlayerCreateRequest(
    val name: String,
    var color: String?,
    val startingType: StartingType = StartingType.Pawns
) : Request()

const val GoldenRatioConjugate = 0.618033988749895
fun generateRandomColor(): String {
    val h = (Random.nextDouble() + GoldenRatioConjugate) % 1.0
    val s = Random.nextDouble(0.4, 0.6)
    val v = Random.nextDouble(0.95, 1.0)
    val c = Color.HSBtoRGB(h.toFloat(), s.toFloat(), v.toFloat())
    return "#${Integer.toHexString(c).substring(2)}"
}

@Serializable
data class PlayerMoveRequest(
    val from: Position,
    val to: Position
) : Request()