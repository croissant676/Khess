package dev.kason.khess.server

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*

fun main() {
    embeddedServer(Netty, port = 8080) {
        routing {
            webSocket("/") {

            }
            post("/") {

            }
        }
    }
}