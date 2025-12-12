package com.example.prueba_app.network

import com.example.prueba_app.model.Usuario
import com.example.prueba_app.model.Plato
import retrofit2.Response
import retrofit2.http.*

interface PruebaApi {

    @GET("platos")
    suspend fun getPlatos(): Response<List<Plato>>

    // 2. LOGIN (POST)
    @POST("auth/login")
    suspend fun login(@Body credenciales: Map<String, String>): Response<AuthResponse>

    // 3. REGISTRO (POST)
    @POST("auth/signup")
    suspend fun signup(@Body usuario: Usuario): Response<AuthResponse>

    // 4. ACTUALIZAR (PUT)
    @PUT("user/{id}")
    suspend fun updateUser(@Path("id") id: Int, @Body usuario: Usuario): Response<Usuario>

    // 5. ELIMINAR (DELETE)
    @DELETE("user/{id}")
    suspend fun deleteUser(@Path("id") id: Int): Response<Unit>

    // 6. CARRITO (POST)
    @POST("carrito")
    suspend fun addToCart(@Body request: CarritoRequest): Response<Unit>

    // 7. ELIMINAR DEL CARRITO (POST)
    @POST("carrito/remove")
    suspend fun removeFromCart(@Body request: CarritoRequest): Response<Unit>
}