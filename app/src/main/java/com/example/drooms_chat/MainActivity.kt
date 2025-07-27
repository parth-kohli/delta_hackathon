package com.example.drooms_chat

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.drooms_chat.ui.theme.DRooms_chatTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private var activity=this
    override fun onCreate(savedInstanceState: Bundle?) {
        val oidcPage = oidcClass().apply {
            init(FirebaseAuth.getInstance(), this@MainActivity)
        }
        oidcPage.getPendingAuthResult { email ->
            if (email != null) {
                println("Signed in as: $email")
            } else {
                println("Sign-in failed or cancelled")
            }
        }
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        enableEdgeToEdge()
        setContent {
            DRooms_chatTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = auth, activity = activity,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: FirebaseAuth,activity: Activity, modifier: Modifier = Modifier) {
    val socket= socketClient()
    socket.main()
}

