package com.example.prueba_app.network

import com.example.prueba_app.model.Plato
import com.example.prueba_app.model.Usuario
import com.google.gson.annotations.SerializedName

// --------------------
// AUTH (Login/Signup)
// --------------------
data class LoginRequest(
    @SerializedName("email") val email: String,
    // Si tu API realmente lo requiere, deja el default. Si no, quítalo.
    @SerializedName("password") val password: String = "123456"
)

data class AuthResponse(
    @SerializedName("authToken") val authToken: String,
    @SerializedName("user") val user: Usuario
)

// --------------------
// CARRITO
// --------------------
data class CarritoRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("plato_id") val platoId: Int,
    @SerializedName("cantidad") val cantidad: Int = 1
)

// --------------------
// PLATOS (API -> DTO)
// --------------------
data class PlatoApi(
    @SerializedName("id") val id: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("imagenUrl") val imagenUrl: String
)

// Mapper: API -> Entity (Room)
fun PlatoApi.toEntity(): Plato {
    return Plato(
        id = id,
        nombre = nombre,
        descripcion = "",   // API no lo trae (por ahora)
        precio = 0,         // API no lo trae (por ahora)
        imagenUrl = imagenUrl
    )
}
