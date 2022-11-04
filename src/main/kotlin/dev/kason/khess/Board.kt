package dev.kason.khess

import kotlinx.serialization.*
import kotlin.reflect.*

@Serializable
data class Position(val x: Int, val y: Int)

class PositionCreator(val x: () -> Int, val y: () -> Int) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Position = Position(x(), y())
}

class Board {

}