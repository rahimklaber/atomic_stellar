package me.rahimklaber.plugins

import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.time.Duration
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.rahimklaber.swap.ClientMessage
import me.rahimklaber.swap.StellarToPolygonSettings
import me.rahimklaber.swap.stellarToPolygonServer

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        webSocket("/{token}/{from}/{to}") { // websocketSession
            println("connected to client")
            val token = call.parameters["token"] ?: error("no token supplied")
            val from = call.parameters["from"] ?: error("no from chain supplied")
            val to = call.parameters["to"] ?: error("no to chain supplied")

            require(token == "usdc"){
                "Only allow usdc for now"
            }
            require(from == "stellar"){
                "Only allow from stellar for now"
            }
            require(to == "polygon"){
                "Only allow to polygon for now"
            }

            val clientMessageChannel = Channel<ClientMessage>(2)
            launch {
                for (message in incoming){
                    require(message is Frame.Text){"message should be a stringified json"}
                    val decoded = Json.decodeFromString<ClientMessage>(message.readText())
                    clientMessageChannel.send(decoded)
                }
            }

//            val handler = stellarToPolygonServer(StellarToPolygonSettings(64),clientMessageChannel)
//
//            for (response in handler){
//                outgoing.send(Frame.Text(Json.encodeToString(response)))
//            }
        }
    }
}
