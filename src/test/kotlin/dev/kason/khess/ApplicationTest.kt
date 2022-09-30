package dev.kason.khess

import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test

class ApplicationTest {

    private fun test(playerCount: Int) = when {
        playerCount == 0 -> 0
        playerCount <= 2 -> 8
        playerCount <= 6 -> 4 + 2 * playerCount
        playerCount <= 10 -> 9 + playerCount
        playerCount <= 16 -> 14 + playerCount / 2
        playerCount <= 36 -> 18 + playerCount / 4
        else -> 27 + playerCount / 8
    }
}
fun main() {
    for (i in 1..100) {
        println(test(i))
    }
}
private fun test(playerCount: Int) = when {
    playerCount == 0 -> 0
    playerCount <= 2 -> 8
    playerCount <= 6 -> 4 + 2 * playerCount
    playerCount <= 12 -> 10 + playerCount
    playerCount <= 24 -> 16 + playerCount / 2
    playerCount <= 64 -> 22 + playerCount / 4
    else -> 30 + playerCount / 8
}