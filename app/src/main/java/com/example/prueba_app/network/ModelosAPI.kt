package com.example.prueba_app.network

import com.example.prueba_app.model.Usuario
import com.google.gson.annotations.SerializedName

// Lo que se envia para loguear
data class LoginRequest(
    val email: String, // Xano usa 'email' usualmente, mapealo si es necesario
    val password: String = "123456" // Default si no usas pass en tu app aun
)

// Lo que Xano responde al loguear/registrar
data class AuthResponse(
    @SerializedName("authToken") val authToken: String,
    @SerializedName("user") val user: Usuario
)

// Modelo para enviar items del carrito a la API
data class CarritoRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("plato_id") val platoId: Int,
    @SerializedName("cantidad") val cantidad: Int = 1
)