package com.example.prueba_app.repository

import com.example.prueba_app.model.DetalleCarrito
import com.example.prueba_app.model.Plato
import com.example.prueba_app.model.Usuario
import kotlinx.coroutines.flow.Flow

interface ComidaDataSource {

    // --- PLATOS ---
    val platos: Flow<List<Plato>>
    suspend fun inicializarDatos()

    // --- USUARIO ---
    fun obtenerUsuarioActivo(id: Int): Flow<Usuario?>
    suspend fun login(correo: String, contrasena: String): Usuario?
    suspend fun registrarUsuario(usuario: Usuario): Long
    suspend fun eliminarUsuario(usuario: Usuario)

    // --- CARRITO ---
    val carrito: Flow<List<DetalleCarrito>>
    suspend fun agregarAlCarrito(platoId: Int, userId: Int)
    suspend fun reducirCantidadOEliminar(detalle: DetalleCarrito, userId: Int)
    suspend fun eliminarItemCarritoPorId(idItem: Int)
}
