package dev.kason.khess

import org.http4k.routing.*
import org.http4k.server.*
import org.http4k.websocket.*

fun app(): WsHandler {
    fun newConnection(ws: Websocket) {
        ws.onMessage {
            println(it.body)
        }
    }
    return websockets("/game" bind ::newConnection)
}

fun main() {
    app().asServer(Jetty(8080)).start()
}