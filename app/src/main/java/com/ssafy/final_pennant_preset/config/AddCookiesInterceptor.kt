package com.ssafy.final_pennant_preset.config

import android.util.Log
import com.ssafy.final_pennant_preset.config.ApplicationClass.Companion.sSharedPreferences
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class AddCookiesInterceptor : Interceptor{
    private val TAG = "AddCookiesInterceptor_ssafy"

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder: Request.Builder = chain.request().newBuilder()

        // cookie 가져오기
        val getCookies = sSharedPreferences.getUserCookie()
        for (cookie in getCookies!!) {
            builder.addHeader("Cookie", cookie)
            Log.v(
                TAG,
                "Adding Header: $cookie"
            ) //This is done so I know which headers are being added; this interceptor is used after the normal logging of OkHttp
        }
        return chain.proceed(builder.build())
    }
}