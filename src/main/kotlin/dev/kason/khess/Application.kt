package dev.kason.khess

import kotlinx.serialization.*
import kotlinx.serialization.builtins.*
import kotlinx.serialization.json.*
import org.http4k.routing.*
import org.http4k.server.*
import org.http4k.websocket.*

@OptIn(ExperimentalSerializationApi::class)
val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
    explicitNulls = false
}

object ErrorMessages {
    const val NoPlayer = "You aren't a player yet. Please join the game first."
}

fun Websocket.respond(response: Response) =
    send(WsMessage(json.encodeToString(response)))

fun app(): WsHandler {
    fun newConnection(ws: Websocket) {
        ws.respond(Game.createBaseDataResponse())
        var player: Player? = null
        ws.onMessage {
            when (val message = Json.decodeFromString(Request.serializer(), it.bodyString())) {
                is PlayerCreateRequest -> {
                    player = Game.addNewPlayer(message, ws)
                    ws.respond(player!!.toData())
                }
                is PlayerMoveRequest -> {
                    if (player != null) {
                        Game.acceptMoveRequest(player!!, message)
                    } else {
                        ws.respond(ErrorResponse(ErrorMessages.NoPlayer))
                    }
                }

            }
        }
    }
    return websockets("/game" bind ::newConnection)
}

fun main() {
    app().asServer(Jetty(8080)).start()
}
