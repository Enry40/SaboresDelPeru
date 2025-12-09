package com.example.prueba_app.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ClientRetrofit {

    // ⚠️ CAMBIA ESTO:
    // - Si es servidor en tu PC: "http://10.0.2.2:PUERTO/"
    // - Debe terminar con "/"
    private const val BASE_URL = "https://TU_BASE_URL_AQUI/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: InterfazApi = retrofit.create(InterfazApi::class.java)
}
