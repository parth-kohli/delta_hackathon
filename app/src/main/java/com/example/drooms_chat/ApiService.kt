package com.example.drooms_chat.com.example.drooms_chat

import androidx.compose.runtime.MutableState
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming
data class FcmTokenRequest(val fcm_token: String)
data class UserSignup(val username: String, val password: String)
data class ApiResponse(val message: String, val user_id: Int?)
data class User(val id:Int, val username: String)
data class Notes(val id: Int, val title: String, val note: String, val user_id: Int, val created_at: String)
data class TokenResponse(val access_token: String, val token_type: String, val user_id: Int, val username: String)
interface ApiService {
    @FormUrlEncoded
    @POST("register/")
    fun signupUser(
        @Field("uname") uname: String,
    ): Call<Int>


    @GET("list/")
    fun listRooms(
        @Header("value") value: String,
    ): Call<List<String>>
    @GET("history/{room_name}/")
    fun roomHistory(
        @Path("room_name") room_name: String,  @Query("skip") skip : Int, @Query("limit")limit : Int
    ): Call<List<List<String>>>
    @GET("sessions/")
    fun getSessions(
        @Query("email") email: String,
    ): Call<List<String>>
    @FormUrlEncoded
    @POST("/send/")
    fun sendMessage(
        @Field("email") email: String,
        @Field("room") room: String,
        @Field("content") content: String
    ): Call<Map<String, Any>>





}
