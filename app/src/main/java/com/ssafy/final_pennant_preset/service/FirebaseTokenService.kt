package com.ssafy.final_pennant_preset.service
import retrofit2.Call
import retrofit2.http.*

interface FirebaseTokenService {
    // Token정보 서버로 전송
    @POST("token/{userUID}")
    fun uploadToken(@Path("userUID") userUID: String, @Query("token") token: String): Call<String>

}