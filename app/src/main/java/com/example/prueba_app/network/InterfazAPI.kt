package com.example.prueba_app.network

import com.example.prueba_app.model.Usuario
import com.example.prueba_app.model.Plato
import retrofit2.Response
import retrofit2.http.*

interface PruebaApi {
    // 1. Obtener Platos
    @GET("platos")
    suspend fun getPlatos(): Response<List<Plato>>

    // 2. Login
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    // 3. Registro
    @POST("auth/signup")
    suspend fun signup(@Body usuario: Usuario): Response<AuthResponse>

    // 4. Modificar Usuario (PUT)
    @PUT("user/{id}")
    suspend fun updateUser(@Path("id") id: Int, @Body usuario: Usuario): Response<Usuario>

    // 5. Eliminar Usuario (DELETE)
    @DELETE("user/{id}")
    suspend fun deleteUser(@Path("id") id: Int): Response<Unit>
}