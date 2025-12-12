package com.example.prueba_app.repository

import android.util.Log
import com.example.prueba_app.model.*
import com.example.prueba_app.network.CarritoRequest
import com.example.prueba_app.network.ClientRetrofit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class RepositorioComida(private val db: AppDatabase) {

    // CORREGIDO: Usamos el nombre correcto del objeto Singleton
    private val api = ClientRetrofit.api

    // --- PLATOS ---
    val platos: Flow<List<Plato>> = db.platoDao().obtenerPlatos()

    suspend fun inicializarDatos() {
        try {
            Log.d("API", "Intentando descargar platos...")
            val response = api.getPlatos()
            if (response.isSuccessful && response.body() != null) {
                Log.d("API", "¡Platos descargados! Cantidad: ${response.body()!!.size}")
                db.platoDao().insertarPlatos(response.body()!!)
            } else {
                // AQUÍ VERÁS EL ERROR SI FALLA
                Log.e("API", "Error Platos: ${response.code()} - ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("API", "Excepción Platos: ${e.message}")
        }

        // Si no hay datos, cargamos los locales por defecto
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

    // --- USUARIO ---
    fun obtenerUsuarioActivo(id: Int): Flow<Usuario?> {
        return if (id != -1) db.usuarioDao().obtenerUsuarioPorId(id) else emptyFlow()
    }

    suspend fun login(correo: String, contrasena: String): Usuario? {
        try {
            // Creamos el mapa simple: Clave "email" y "password" (lo que espera Xano)
            val credenciales = mapOf(
                "email" to correo,
                "password" to contrasena
            )

            // Enviamos el mapa
            val response = api.login(credenciales)

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

    suspend fun registrarUsuario(usuario: Usuario): Long {
        try {
            if (usuario.id == 0) {
                Log.d("API", "Registrando usuario nuevo...")
                val response = api.signup(usuario)

                if (response.isSuccessful && response.body() != null) {
                    Log.d("API", "¡Registro Exitoso!")
                    val usuarioCreado = response.body()!!.user
                    return db.usuarioDao().insertarUsuario(usuarioCreado)
                } else {
                    Log.e("API", "Error Signup: ${response.code()} - ${response.errorBody()?.string()}")
                }
            } else {
                Log.d("API", "Actualizando usuario ${usuario.id}...")
                val response = api.updateUser(usuario.id, usuario)
                if (response.isSuccessful) {
                    Log.d("API", "Actualización Exitosa")
                    return db.usuarioDao().insertarUsuario(usuario)
                } else {
                    Log.e("API", "Error Update: ${response.code()} - ${response.errorBody()?.string()}")
                }
            }
        } catch (e: Exception) {
            Log.e("API", "Excepción Registro: ${e.message}")
        }
        return db.usuarioDao().insertarUsuario(usuario)
    }

    suspend fun eliminarUsuario(usuario: Usuario) {
        try {
            val response = api.deleteUser(usuario.id)
            if (!response.isSuccessful) Log.e("API", "Error Delete: ${response.code()}")
        } catch (e: Exception) {
            Log.e("API", "Excepción Delete: ${e.message}")
        }
        db.usuarioDao().eliminarUsuario(usuario)
        db.carritoDao().vaciarCarrito()
    }

    // --- CARRITO ---
    val carrito: Flow<List<DetalleCarrito>> = db.carritoDao().obtenerCarritoConDetalles()

    suspend fun agregarAlCarrito(platoId: Int, userId: Int) {
        try {
            if (userId != -1) {
                val response = api.addToCart(CarritoRequest(userId = userId, platoId = platoId, cantidad = 1))
                if (!response.isSuccessful) Log.e("API", "Error AddCart: ${response.code()}")
            }
        } catch (e: Exception) { Log.e("API", "Ex AddCart: ${e.message}") }

        val itemExistente = db.carritoDao().obtenerItemPorPlato(platoId)
        if (itemExistente != null) {
            db.carritoDao().actualizarItem(itemExistente.copy(cantidad = itemExistente.cantidad + 1))
        } else {
            db.carritoDao().insertarItem(ItemCarrito(platoId = platoId, cantidad = 1))
        }
    }

    suspend fun reducirCantidadOEliminar(detalle: DetalleCarrito, userId: Int) {
        try {
            if (userId != -1) {
                val response = api.removeFromCart(CarritoRequest(userId = userId, platoId = detalle.plato.id, cantidad = 1))
                if (!response.isSuccessful) Log.e("API", "Error RemoveCart: ${response.code()}")
            }
        } catch (e: Exception) { Log.e("API", "Ex RemoveCart: ${e.message}") }

        val item = ItemCarrito(detalle.idItem, detalle.plato.id, detalle.cantidad)
        if (item.cantidad > 1) {
            db.carritoDao().actualizarItem(item.copy(cantidad = item.cantidad - 1))
        } else {
            db.carritoDao().eliminarItem(item)
        }
    }

    suspend fun eliminarItemCarritoPorId(idItem: Int) {
        db.carritoDao().eliminarItemPorId(idItem)
    }
}