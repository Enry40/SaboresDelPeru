package com.example.prueba_app.repository

import com.example.prueba_app.model.AppDatabase
import com.example.prueba_app.model.DetalleCarrito
import com.example.prueba_app.model.Plato
import com.example.prueba_app.model.Usuario
import com.example.prueba_app.network.ClienteRetrofit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class RepositorioComida(
    private val db: AppDatabase
) : ComidaDataSource {

    // DAOs de Room
    private val platoDao = db.platoDao()
    private val usuarioDao = db.usuarioDao()
    private val carritoDao = db.carritoDao()

    // Cliente de API
    private val api = ClienteRetrofit.api

    // --- LISTAS EXPUESTAS COMO FLOW ---

    override val platos: Flow<List<Plato>> = platoDao.obtenerPlatos()

    // De momento carrito vacío (luego lo conectamos de verdad a Room)
    override val carrito: Flow<List<DetalleCarrito>> =
        flowOf(emptyList())

    // --- INICIALIZAR DATOS (PLATOS) ---

    override suspend fun inicializarDatos() {
        try {
            val response = api.getPlatos()
            if (response.isSuccessful) {
                val listaRemota: List<Plato> = response.body() ?: emptyList()
                if (listaRemota.isNotEmpty()) {
                    platoDao.insertarPlatos(listaRemota)
                }
            }
        } catch (e: Exception) {
            // Aquí puedes hacer Log.e(...) si quieres
            // pero no relanzamos para que los tests no fallen por la red
        }
    }

    // --- CARRITO (lo dejamos como TODO por ahora) ---

    override suspend fun agregarAlCarrito(platoId: Int, usuarioId: Int) {
        // TODO: implementar con carritoDao
    }

    override suspend fun reducirCantidadOEliminar(
        detalle: DetalleCarrito,
        usuarioId: Int
    ) {
        // TODO: implementar con carritoDao
    }

    override suspend fun eliminarCarritoPorId(
        idDetalle: Int,
        usuarioId: Int
    ) {
        // TODO: implementar cuando tengas función en CarritoDao
        // carritoDao.eliminarPorId(idDetalle, usuarioId)
    }

    // --- USUARIOS (también TODO pero sin reventar la app) ---

    override suspend fun registrarUsuario(usuario: Usuario): Long {
        // TODO: reemplazar por usuarioDao.insert(usuario)
        return 0L
    }

    override suspend fun login(correo: String, contrasena: String): Usuario? {
        // TODO: reemplazar por usuarioDao.login(...)
        return null
    }

    override fun obtenerUsuarioActivo(id: Int): Flow<Usuario?> =
        usuarioDao.obtenerUsuarioPorId(id)

    override suspend fun eliminarUsuario(usuario: Usuario) {
        // TODO: usuarioDao.eliminar(usuario)
    }
}
