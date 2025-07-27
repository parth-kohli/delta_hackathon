package com.example.drooms_chat.com.example.drooms_chat


import android.R
import androidx.navigation.NavController
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.submitForm
import io.ktor.serialization.kotlinx.json.json
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiFunctions {
    val retrofit = Retrofit.Builder()
        .baseUrl("http://127.0.0.1:8000/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val apiService = retrofit.create(ApiService::class.java)
    fun signupUser(uname: String, onSignUp: ()->Unit){
        val call = apiService.signupUser(uname)
        call.enqueue(object : Callback<Int>{
            override fun onResponse(call: Call<Int>, response: Response<Int>) {
                println(1)
                onSignUp()
            }
            override fun onFailure(call: Call<Int>, t: Throwable) {
                println(t)
            }

        })
    }
    fun listRooms( onList: (List<String>?) -> Unit){
        println(SecurePrefs.getEmail().toString())
        val call = apiService.listRooms(SecurePrefs.getEmail().toString())

        call.enqueue(object : Callback<List<String>>{
            override fun onResponse(
                call: Call<List<String>?>,
                response: Response<List<String>?>
            ) {
                println(response)
                onList(response.body())
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {

            }

        })
    }
    fun getSessions(email: String, callback: (List<String>?) -> Unit) {
        val call = apiService.getSessions(email)
        call.enqueue(object : Callback<List<String>> {
            override fun onResponse(
                call: Call<List<String>>,
                response: Response<List<String>>
            ) {
                if (response.isSuccessful) {
                    callback(response.body())
                } else {
                    println("Session failed: ${response.errorBody()?.string()}")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                println("Session error: ${t.message}")
                callback(null)
            }
        })
    }

    fun sendMessage(email: String, room: String, message: String, callback: (Boolean) -> Unit) {
        val call = apiService.sendMessage(email, room, message)
        call.enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(
                call: Call<Map<String, Any>>,
                response: Response<Map<String, Any>>
            ) {
                if (response.isSuccessful && response.body()?.get("success") == true) {
                    callback(true)
                } else {
                    println("Message failed: ${response.errorBody()?.string()}")
                    callback(false)
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                println("Message error: ${t.message}")
                callback(false)
            }
        })
    }


    fun roomHistory(room_name:String, skip: Int, limit: Int, onList: (List<List<String>>?) -> Unit){
        println(SecurePrefs.getEmail().toString())
        val call = apiService.roomHistory(room_name, skip, limit)

        call.enqueue(object : Callback<List<List<String>>>{
            override fun onResponse(
                call: Call<List<List<String>>?>,
                response: Response<List<List<String>>?>
            ) {
                println(response)
                onList(response.body())
            }

            override fun onFailure(call: Call<List<List<String>>>, t: Throwable) {

            }

        })
    }


}