package com.ssafy.final_pennant_preset.util

import com.ssafy.final_pennant_preset.config.ApplicationClass
import com.ssafy.final_pennant_preset.service.FirebaseTokenService
import com.ssafy.final_pennant_preset.service.MusicService
import retrofit2.create

class RetrofitUtil {
    companion object{
        val musicService = ApplicationClass.sRetrofit.create(MusicService::class.java)
        val firebaseTokenService = ApplicationClass.sRetrofit.create(FirebaseTokenService::class.java)
    }
}