package com.example.prueba_app.network

import com.example.prueba_app.model.Usuario
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface InterfazApi {

    // ---------
    // PLATOS
    // ---------
    @GET("platos")
    suspend fun getPlatos(): Response<List<PlatoApi>>

    // ---------
    // AUTH
    // ---------
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("signup")
    suspend fun signup(@Body usuario: Usuario): Response<AuthResponse>

    // ---------
    // USUARIO
    // ---------
    @PUT("users/{id}")
    suspend fun updateUser(@Path("id") id: Int, @Body usuario: Usuario): Response<Usuario>

    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") id: Int): Response<Unit>

    // ---------
    // CARRITO
    // ---------
    @POST("cart/add")
    suspend fun addToCart(@Body request: CarritoRequest): Response<Unit>

    @POST("cart/remove")
    suspend fun removeFromCart(@Body request: CarritoRequest): Response<Unit>
}
