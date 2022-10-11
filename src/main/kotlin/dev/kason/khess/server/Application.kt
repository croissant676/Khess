package dev.kason.khess.server

import dev.kason.khess.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import org.slf4j.*
import kotlin.properties.*
import kotlin.reflect.*

val logger = LoggerFactory.getLogger("dev.kason.khess")!!
fun main(): Unit = embeddedServer(Netty, port = 8080) {
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(Json { prettyPrint = true })
    }
    install(ContentNegotiation) {
        json()
    }
    val game = Game()
    routing {
        webSocket("/") {
            var player: Player by NotNullPlayerDelegate()
            for (frame in incoming) {
                try {
                    if (frame is Frame.Close) {
                        player.kill()
                        break
                    }
                    if (frame !is Frame.Text) continue
                    val jsonString = frame.readText()
                    logger.debug("Received: $jsonString")
                    when (val request = Json.decodeFromString<Request>(jsonString)) {
                        is JoinRequest -> {
                            player = game.acceptJoinRequest(request, this@webSocket)
                            sendSerialized(player.toData())
                            sendSerialized(player.frame)
                            sendSerialized(player.getMovesData())
                        }
                        is MoveRequest -> {
                            game.acceptMoveRequest(request, player)
                            sendSerialized(player.frame)
                        }
                    }
                } catch (e: Exception) {
                    sendSerialized(ErrorResponse(e.message ?: "Unknown error"))
                    logger.error("Error while handling request", e)
                }
            }
        }
    }
}.run { start(true) }

private class NotNullPlayerDelegate : ReadWriteProperty<Any?, Player> {
    private var value: Player? = null
    override fun getValue(thisRef: Any?, property: KProperty<*>): Player {
        return value ?: throw IllegalStateException("You have not joined the game yet.")
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Player) {
        this.value = value
    }
}