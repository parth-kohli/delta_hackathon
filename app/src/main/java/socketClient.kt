package com.example.drooms_chat
import androidx.compose.runtime.MutableState
import com.example.drooms_chat.com.example.drooms_chat.SecurePrefs
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
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class socketClient {

    suspend fun main(text:MutableState<String>, output: MutableState<String>) {
        val client = HttpClient(OkHttp) {
            install(WebSockets)
        }

        runBlocking {
            try {
                client.webSocket(method = HttpMethod.Get, host = "127.0.0.1", port = 5000, path = "/echo") {
                    println("Connected to server")
                    launch {
                        try {
                            for (message in incoming) {
                                val text = (message as? Frame.Text)?.readText()
                                println(text)
                                if (text != null) output.value=text;

                            }
                        } catch (e: Exception) {
                            println("Error receiving: ${e.localizedMessage}")
                        }
                    }

                    while (true) {
                        if (text.value.isNotBlank()) {
                            println("${text.value}")
                            send(text.value)
                            text.value=""
                        }
                    }
                }
            } catch (e: Exception) {
                println("WebSocket error: ${e.localizedMessage}")
            }
        }

        client.close()
        println("Client closed")
    }
}
