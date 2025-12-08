package com.example.prueba_app.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ClientRetrofit {

    // ¡IMPORTANTE! Reemplazar URL con la base de Xano y debe terminar con un '/'

    private const val BASE_URL = "https://x8ki-letl-twmt.n7.xano.io/api:TU_GRUPO_API/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val api: PruebaApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
            .create(PruebaApi::class.java)
    }
}
