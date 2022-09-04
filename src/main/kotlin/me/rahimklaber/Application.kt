package me.rahimklaber

import io.ktor.server.application.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.rahimklaber.plugins.*
import org.stellar.sdk.KeyPair

//fun main(args: Array<String>): Unit =
//    io.ktor.server.netty.EngineMain.main(args)

fun main() {
    val keyPair = KeyPair.fromSecretSeed("SAIRV2AENXYLYK5BMROCBDTQAUKFOGQMQARQLJSRQ32OWA7KQRMDHXTH")
    println(keyPair.accountId)

}


@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    configureRouting()
    configureHTTP()
    configureTemplating()
    configureSerialization()
    configureSockets()
}