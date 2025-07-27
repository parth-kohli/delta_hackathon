package com.example.drooms_chat
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.*
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.runBlocking
import java.util.Scanner

class socketClient {

    fun main() {
        val client= HttpClient(CIO){
            install(WebSockets){
                pingIntervalMillis=20_000
            }
        }

        runBlocking {
            client.webSocket(method = HttpMethod.Get, host = "127.0.0.1", port = 5000, path = "/echo") {
                while(true) {
                    val othersMessage = incoming.receive() as? Frame.Text
                    println(othersMessage?.readText())
                    val myMessage = Scanner(System.`in`).next()
                    if(myMessage != null) {
                        send(myMessage)
                    }
                }
            }
        }
        client.close()
    }
}