package dev.kason.khess

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.*
import io.ktor.websocket.*
import kotlinx.serialization.json.*

suspend fun main() {
    val client = HttpClient(CIO) {
        WebSockets {
            pingInterval = 1000
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
    }
    client.webSocket("ws://localhost:8080/") {
        send(
            buildJsonObject {
                put("type", "join")
                put("name", "test")
            }.toString()
        )
        repeat(3) {
            println((incoming.receive() as Frame.Text).readText())
        }
        while (true) {
            send(readln())
            println((incoming.receive() as Frame.Text).readText())
        }
    }
}