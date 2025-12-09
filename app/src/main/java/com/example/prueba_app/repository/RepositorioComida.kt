package com.example.prueba_app.repository

import android.util.Log
import com.example.prueba_app.model.*
import com.example.prueba_app.network.CarritoRequest
import com.example.prueba_app.network.ClientRetrofit
import com.example.prueba_app.network.InterfazApi
import com.example.prueba_app.network.LoginRequest
import com.example.prueba_app.network.toEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.withContext

class RepositorioComida(
    private val db: AppDatabase,
    private val api: InterfazApi = ClientRetrofit.api,
    private val io: CoroutineDispatcher = Dispatchers.IO
) : ComidaDataSource {

    // PLATOS (UI siempre lee desde Room)
    override val platos: Flow<List<Plato>> = db.platoDao().obtenerPlatos()

    override suspend fun inicializarDatos() = withContext(io) {
        // 1) Intentar API -> guardar en Room
        try {
            val response = api.getPlatos()
            val body = response.body()

            if (response.isSuccessful && !body.isNullOrEmpty()) {
                val entidades = body.map { it.toEntity() }
                db.platoDao().insertarPlatos(entidades)
            }
        } catch (e: Exception) {
            Log.e("API", "Error platos: ${e.message}")
        }

        // 2) Fallback si sigue vacío
        if (db.platoDao().contarPlatos() == 0) {
            db.platoDao().insertarPlatos(platosIniciales())
        }
    }

    private fun platosIniciales(): List<Plato> = listOf(
        Plato(nombre = "Ceviche Clásico", descripcion = "Pescado fresco marinado en limón.", precio = 12000, imagenUrl = "ceviche"),
        Plato(nombre = "Lomo Saltado", descripcion = "Trozos de carne salteados.", precio = 14500, imagenUrl = "lomo"),
        Plato(nombre = "Ají de Gallina", descripcion = "Pollo deshilachado en crema de ají.", precio = 10500, imagenUrl = "aji"),
        Plato(nombre = "Anticuchos", descripcion = "Corazón de res a la parrilla.", precio = 9000, imagenUrl = "anticuchos"),
        Plato(nombre = "Causa Limeña", descripcion = "Masa de papa con pollo.", precio = 9500, imagenUrl = "causa"),
        Plato(nombre = "Rocoto Relleno", descripcion = "Rocoto horneado relleno de carne.", precio = 11000, imagenUrl = "rocoto"),
        Plato(nombre = "Papa a la Huancaína", descripcion = "Papas con salsa de queso y ají.", precio = 8500, imagenUrl = "huancaina"),
        Plato(nombre = "Tacu Tacu con Lomo", descripcion = "Arroz y frijoles con lomo.", precio = 13500, imagenUrl = "tacutacu")
    )

    // USUARIO Y SESIÓN
    fun obtenerUsuarioActivo(id: Int): Flow<Usuario?> {
        return if (id != -1) db.usuarioDao().obtenerUsuarioPorId(id) else emptyFlow()
    }

    suspend fun login(correo: String): Usuario? = withContext(io) {
        try {
            val response = api.login(LoginRequest(email = correo))
            if (response.isSuccessful && response.body() != null) {
                val usuarioApi = response.body()!!.user
                db.usuarioDao().insertarUsuario(usuarioApi)
                return@withContext usuarioApi
            }
        } catch (e: Exception) {
            Log.e("API", "Login offline: ${e.message}")
        }
        return@withContext db.usuarioDao().buscarPorCorreo(correo)
    }

    // API CREAR (POST) y ACTUALIZAR (PUT)
    suspend fun registrarUsuario(usuario: Usuario): Long = withContext(io) {
        try {
            if (usuario.id == 0) {
                val response = api.signup(usuario)
                if (response.isSuccessful && response.body() != null) {
                    val usuarioCreado = response.body()!!.user
                    return@withContext db.usuarioDao().insertarUsuario(usuarioCreado)
                }
            } else {
                val response = api.updateUser(usuario.id, usuario)
                if (response.isSuccessful) {
                    Log.d("API", "Usuario actualizado en API")
                    return@withContext db.usuarioDao().insertarUsuario(usuario)
                }
            }
        } catch (e: Exception) {
            Log.e("API", "Operación usuario offline: ${e.message}")
        }
        return@withContext db.usuarioDao().insertarUsuario(usuario)
    }

    // API ELIMINAR (DELETE)
    suspend fun eliminarUsuario(usuario: Usuario) = withContext(io) {
        try {
            val response = api.deleteUser(usuario.id)
            if (response.isSuccessful) {
                Log.d("API", "Usuario eliminado de API")
            } else {
                Log.e("API", "Error al eliminar en API: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("API", "Eliminar offline: ${e.message}")
        }

        db.usuarioDao().eliminarUsuario(usuario)
        db.carritoDao().vaciarCarrito()
    }

    // CARRITO (API y Local)
    val carrito: Flow<List<DetalleCarrito>> = db.carritoDao().obtenerCarritoConDetalles()

    suspend fun agregarAlCarrito(platoId: Int, userId: Int) = withContext(io) {
        // 1) API
        try {
            if (userId != -1) {
                api.addToCart(CarritoRequest(userId = userId, platoId = platoId, cantidad = 1))
            }
        } catch (e: Exception) {
            Log.e("API", "Error al agregar carrito API: ${e.message}")
        }

        // 2) Local (Room)
        val itemExistente = db.carritoDao().obtenerItemPorPlato(platoId)
        if (itemExistente != null) {
            db.carritoDao().actualizarItem(itemExistente.copy(cantidad = itemExistente.cantidad + 1))
        } else {
            db.carritoDao().insertarItem(ItemCarrito(platoId = platoId, cantidad = 1))
        }
    }

    suspend fun reducirCantidadOEliminar(detalle: DetalleCarrito, userId: Int) = withContext(io) {
        // 1) API
        try {
            if (userId != -1) {
                api.removeFromCart(
                    CarritoRequest(
                        userId = userId,
                        platoId = detalle.plato.id,
                        cantidad = 1
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("API", "Error al eliminar carrito API: ${e.message}")
        }

        // 2) Local
        val item = ItemCarrito(detalle.idItem, detalle.plato.id, detalle.cantidad)
        if (item.cantidad > 1) {
            db.carritoDao().actualizarItem(item.copy(cantidad = item.cantidad - 1))
        } else {
            db.carritoDao().eliminarItem(item)
        }
    }
}
