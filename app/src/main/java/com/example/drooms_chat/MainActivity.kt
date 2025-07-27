package com.example.drooms_chat

import android.app.Activity
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.drooms_chat.com.example.drooms_chat.ApiFunctions
import com.example.drooms_chat.com.example.drooms_chat.SecurePrefs
import com.example.drooms_chat.ui.theme.DRooms_chatTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private var activity=this
    override fun onCreate(savedInstanceState: Bundle?) {
        val oidcPage = oidcClass().apply {
            init(FirebaseAuth.getInstance(), this@MainActivity)
        }

        super.onCreate(savedInstanceState)
        SecurePrefs.init(this)
        auth = Firebase.auth
        enableEdgeToEdge()
        setContent {
            DRooms_chatTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = auth, activity = activity, oidcClass = oidcPage,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
val api = ApiFunctions()
@Composable
fun Greeting(name: FirebaseAuth,activity: Activity, oidcClass: oidcClass, modifier: Modifier = Modifier) {
    val text= remember { mutableStateOf("") }
    val ouput= remember { mutableStateOf("") }
    val textbox= remember { mutableStateOf("") }
    val socket= socketClient()

    val navController= rememberNavController()
    /*LaunchedEffect(Unit) {
        withContext(Dispatchers.IO){
            socket.main(text, output = ouput)
        }
    }*/
    LaunchedEffect(ouput.value) {
        println(ouput.value)
    }
    val room = remember { mutableStateOf("")}
    NavHost(navController, startDestination = "signup") {
        composable("signup") { signUpScreen(name, activity, oidcClass,ouput, text, modifier, navController) }
        composable("rooms") { roomList(text,ouput,modifier,navController){room.value=it; navController.navigate("room")} }
        composable("room") { roomContent(room.value, modifier) }
    }


}
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun roomContent(room: String, modifier: Modifier) {
    val roomhistory = remember { mutableStateListOf<List<String>>() }
    val scrollState = rememberScrollState()
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val bubbleMaxWidth = 0.75f * screenWidth - 100.dp
    var newAnswer by remember { mutableStateOf("") }
    val scrollState2 = rememberScrollState()
    val CleanBlack = Color.Black
    val CleanWhite = Color.White
    val Uncleanwhite = Color.LightGray
    val EnergeticTeal = Color.Cyan
    var int by remember { mutableStateOf(0) }
    val sessions = remember { mutableStateListOf<String>() }

    val api = remember { ApiFunctions() } // instantiate Retrofit wrapper

    // Load chat history & sessions only once
    LaunchedEffect(Unit) {
        api.roomHistory(room, 0, 50) {
            if (it != null) {
                roomhistory.addAll(it)
            }
            api.getSessions(SecurePrefs.getEmail().toString()) {
                if (it != null) {
                    sessions.addAll(it)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.padding(2.dp)) {
                Spacer(modifier = Modifier.height(16.dp))
                FlowRow(verticalArrangement = Arrangement.Center) {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CleanWhite)
                    }
                    Text(room, style = MaterialTheme.typography.titleLarge, color = Uncleanwhite, fontWeight = FontWeight.Bold)
                }
            }
        },
        bottomBar = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(Color.DarkGray)
            ) {
                TextField(
                    value = newAnswer,
                    onValueChange = { newAnswer = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    placeholder = { Text("Type a message...") },
                    colors = TextFieldDefaults.colors()
                )
                IconButton(
                    onClick = {
                        val trimmed = newAnswer.trim()
                        if (trimmed.isNotEmpty()) {
                            api.sendMessage(sessions.firstOrNull() ?: "You", room, trimmed) { success ->
                                if (success) {
                                    roomhistory.add(listOf(sessions.firstOrNull() ?: "You", trimmed))
                                    newAnswer = ""
                                }
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = EnergeticTeal)
                }
            }
        },
        modifier = modifier.background(CleanBlack).fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .background(CleanBlack)
                .fillMaxWidth()
                .padding(innerPadding)
        ) {
            if (roomhistory.isNotEmpty()) {
                items(roomhistory.size) { answer ->
                    val poster = roomhistory[answer][0]
                    val isUser = sessions.contains(poster)
                    val alignment = remember { mutableStateOf(Alignment.End) }
                    alignment.value=if (isUser) Alignment.End else Alignment.Start
                    val color = if (isUser) Color.DarkGray else Color.Blue

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = alignment.value
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth(0.75f),
                            horizontalAlignment = alignment.value
                        ) {
                            Row(horizontalArrangement = Arrangement.End) {
                                Card(
                                    modifier = modifier.width(250.dp),
                                    colors = CardDefaults.cardColors(containerColor = color)
                                ) {
                                    Text(
                                        text = roomhistory[answer][1],
                                        modifier = Modifier.padding(8.dp),
                                        color = CleanWhite
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                "By: $poster",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                modifier = Modifier.padding(end = 50.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun roomList(text: MutableState<String>, output: MutableState<String>, modifier: Modifier = Modifier, navController: NavController, naviagte:(String)->Unit){

    val roomlist = remember { mutableStateOf(listOf<String>()) }
    LaunchedEffect(Unit) {
        delay(500)
        api.listRooms(){
            if (!it.isNullOrEmpty()){
                roomlist.value=it!!.toList()
            }
        }
    }
    val scrollState= rememberScrollState()
    Column (modifier=Modifier.fillMaxSize().verticalScroll(scrollState), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center ) {
        Spacer(modifier.height(20.dp))
        Text("CHOOSE YOUR ROOM")
        for (i in roomlist.value){
            Spacer(modifier.height(20.dp))
            listItem(i) {naviagte(it) }
        }
    }
}
@Composable
fun listItem(room: String, naviagte: (String) -> Unit){
    IconButton ({naviagte("$room")}, modifier=Modifier.clip(RoundedCornerShape(10.dp)).background(Color.Blue).width(300.dp).height(80.dp)){
        Text(text = room)
    }
}
@Composable
fun  signUpScreen(auth: FirebaseAuth,activity: Activity, oidcClass: oidcClass, output:MutableState<String>,text:MutableState<String>, modifier: Modifier = Modifier, navController: NavController){
    val isLoading = remember { mutableStateOf(false) }

    Column(modifier=modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center ) {

        Button(onClick = {
            isLoading.value=true

            oidcClass.getPendingAuthResult { email ->
                if (email != null) {
                    api.signupUser(email){
                        SecurePrefs.saveEmail(email)
                        navController.navigate("rooms")
                    }
                    println("Signed in as: $email")
                   /*
                    text.value = "/register $email"
                    SecurePrefs.saveEmail(email)*/

                    isLoading.value=false
                } else {
                    println("Sign-in failed or cancelled")
                }
            }
        }, enabled = !isLoading.value, modifier = Modifier.width(300.dp).height(60.dp)) {
            if (isLoading.value){
                CircularProgressIndicator(modifier=Modifier.height(40.dp), color = Color.White)
            }
            else {
                Text(text = "Sign in with google ")
            }
        }
        IconButton (onClick = {
            isLoading.value=true
            oidcClass.getPendingAuthResult { email ->
                if (email != null) {
                    println("Signed in as: $email")
                    text.value = "/login $email"

                } else {
                    println("Sign-in failed or cancelled")
                }
            }
        }, enabled = !isLoading.value, modifier = Modifier.width(300.dp).height(60.dp)) {
            if (isLoading.value){
                CircularProgressIndicator(modifier=Modifier.height(40.dp), color = Color.White)
            }
            else {
                Text(text = "Login as admin ")
            }
        }
        Text(" Hi ${output.value}", color = Color.Red)
        if (output.value.isNotBlank() && output.value!="True") {
            Text(output.value, color = Color.Red)
        }
    }
}

