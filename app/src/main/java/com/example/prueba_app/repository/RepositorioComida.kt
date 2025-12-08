package com.example.prueba_app.repository

import android.util.Log
import com.example.prueba_app.model.*
import com.example.prueba_app.network.CarritoRequest
import com.example.prueba_app.network.ClientRetrofit
import com.example.prueba_app.network.LoginRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class RepositorioComida(private val db: AppDatabase) {

    private val api = ClientRetrofit.api

    // PLATOS
    val platos: Flow<List<Plato>> = db.platoDao().obtenerPlatos()

    suspend fun inicializarDatos() {
        try {
            val response = api.getPlatos()
            if (response.isSuccessful && response.body() != null) {
                db.platoDao().insertarPlatos(response.body()!!)
            }
        } catch (e: Exception) {
            Log.e("API", "Error platos: ${e.message}")
        }

        if (db.platoDao().contarPlatos() == 0) {
            val platosIniciales = listOf(
                Plato(nombre = "Ceviche Clásico", descripcion = "Pescado fresco marinado en limón.", precio = 12000, imagenUrl = "ceviche"),
                Plato(nombre = "Lomo Saltado", descripcion = "Trozos de carne salteados.", precio = 14500, imagenUrl = "lomo"),
                Plato(nombre = "Ají de Gallina", descripcion = "Pollo deshilachado en crema de ají.", precio = 10500, imagenUrl = "aji"),
                Plato(nombre = "Anticuchos", descripcion = "Corazón de res a la parrilla.", precio = 9000, imagenUrl = "anticuchos"),
                Plato(nombre = "Causa Limeña", descripcion = "Masa de papa con pollo.", precio = 9500, imagenUrl = "causa"),
                Plato(nombre = "Rocoto Relleno", descripcion = "Rocoto horneado relleno de carne.", precio = 11000, imagenUrl = "rocoto"),
                Plato(nombre = "Papa a la Huancaína", descripcion = "Papas con salsa de queso y ají.", precio = 8500, imagenUrl = "huancaina"),
                Plato(nombre = "Tacu Tacu con Lomo", descripcion = "Arroz y frijoles con lomo.", precio = 13500, imagenUrl = "tacutacu")
            )
            db.platoDao().insertarPlatos(platosIniciales)
        }
    }

    // USUARIO Y SESIÓN
    fun obtenerUsuarioActivo(id: Int): Flow<Usuario?> {
        return if (id != -1) db.usuarioDao().obtenerUsuarioPorId(id) else emptyFlow()
    }

    suspend fun login(correo: String): Usuario? {
        try {
            val response = api.login(LoginRequest(email = correo))
            if (response.isSuccessful && response.body() != null) {
                val usuarioApi = response.body()!!.user
                db.usuarioDao().insertarUsuario(usuarioApi)
                return usuarioApi
            }
        } catch (e: Exception) {
            Log.e("API", "Login offline: ${e.message}")
        }
        return db.usuarioDao().buscarPorCorreo(correo)
    }

    // API CREAR (POST) y ACTUALIZAR (PUT)
    suspend fun registrarUsuario(usuario: Usuario): Long {
        try {
            if (usuario.id == 0) {
                // ID es 0 -> Usuario nuevo -> POST (Signup)
                val response = api.signup(usuario)
                if (response.isSuccessful && response.body() != null) {
                    val usuarioCreado = response.body()!!.user
                    return db.usuarioDao().insertarUsuario(usuarioCreado)
                }
            } else {
                // ID existe -> Usuario existente -> PUT (Update)
                val response = api.updateUser(usuario.id, usuario)
                if (response.isSuccessful) {
                    Log.d("API", "Usuario actualizado en Xano")

                    // Si la API responde OK, actualizamos localmente
                    // (Si la API devuelve el objeto actualizado, mejor usarlo, sino usamos el local)
                    return db.usuarioDao().insertarUsuario(usuario)
                }
            }
        } catch (e: Exception) {
            Log.e("API", "Operación usuario offline: ${e.message}")
        }

        // Si falla la API o no hay internet, guardamos localmente
        return db.usuarioDao().insertarUsuario(usuario)
    }

    // API ELIMINAR (DELETE)
    suspend fun eliminarUsuario(usuario: Usuario) {
        try {
            // Intentamos borrar en la API primero
            val response = api.deleteUser(usuario.id)
            if (response.isSuccessful) {
                Log.d("API", "Usuario eliminado de Xano")
            } else {
                Log.e("API", "Error al eliminar en API: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("API", "Eliminar offline: ${e.message}")
        }

        // Siempre eliminamos localmente (Room) para limpiar el dispositivo
        db.usuarioDao().eliminarUsuario(usuario)
        db.carritoDao().vaciarCarrito()
    }

    // CARRITO (API y Local)
    val carrito: Flow<List<DetalleCarrito>> = db.carritoDao().obtenerCarritoConDetalles()

    // Modificado: Recibe userId para sincronizar con API
    suspend fun agregarAlCarrito(platoId: Int, userId: Int) {
        // 1. API: Intentamos enviar a la nube
        try {
            if (userId != -1) {
                api.addToCart(CarritoRequest(userId = userId, platoId = platoId, cantidad = 1))
            }
        } catch (e: Exception) {
            Log.e("API", "Error al agregar carrito API: ${e.message}")
        }

        // 2. LOCAL: Siempre se actualiza Room para la UI inmediata
        val itemExistente = db.carritoDao().obtenerItemPorPlato(platoId)
        if (itemExistente != null) {
            db.carritoDao().actualizarItem(itemExistente.copy(cantidad = itemExistente.cantidad + 1))
        } else {
            db.carritoDao().insertarItem(ItemCarrito(platoId = platoId, cantidad = 1))
        }
    }

    // Recibe userId para sincronizar con API
    suspend fun reducirCantidadOEliminar(detalle: DetalleCarrito, userId: Int) {
        // 1. API
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

        // 2. LOCAL
        val item = ItemCarrito(detalle.idItem, detalle.plato.id, detalle.cantidad)
        if (item.cantidad > 1) {
            db.carritoDao().actualizarItem(item.copy(cantidad = item.cantidad - 1))
        } else {
            db.carritoDao().eliminarItem(item)
        }
    }
}