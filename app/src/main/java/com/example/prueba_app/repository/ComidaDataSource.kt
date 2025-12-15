package com.example.prueba_app.repository

import com.example.prueba_app.model.DetalleCarrito
import com.example.prueba_app.model.Plato
import com.example.prueba_app.model.Usuario
import kotlinx.coroutines.flow.Flow

// OJO: interface, no class
interface ComidaDataSource {

    // Flujos FRÍOS, normales
    val platos: Flow<List<Plato>>
    val carrito: Flow<List<DetalleCarrito>>

    suspend fun inicializarDatos()

    suspend fun agregarAlCarrito(platoId: Int, usuarioId: Int)

    suspend fun reducirCantidadOEliminar(
        detalle: DetalleCarrito,
        usuarioId: Int
    )

    suspend fun registrarUsuario(usuario: Usuario): Long

    suspend fun login(correo: String, contrasena: String): Usuario?

    fun obtenerUsuarioActivo(id: Int): Flow<Usuario?>

    suspend fun eliminarUsuario(usuario: Usuario)

    // NUEVO: firma única y consistente en todos lados
    suspend fun eliminarCarritoPorId(
        idDetalle: Int,
        usuarioId: Int
    )
}
