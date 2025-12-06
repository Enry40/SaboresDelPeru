package com.example.prueba_app.repository

import com.example.prueba_app.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class RepositorioComida(private val db: AppDatabase) {

    //PLATOS
    val platos: Flow<List<Plato>> = db.platoDao().obtenerPlatos()

    suspend fun inicializarDatos() {
        if (db.platoDao().contarPlatos() == 0) {
            val platosIniciales = listOf(
                Plato(nombre = "Ceviche Clásico", descripcion = "Pescado fresco marinado en limón con cebolla y ají.", precio = 12000, imagenUrl = "ceviche"),
                Plato(nombre = "Lomo Saltado", descripcion = "Trozos de carne salteados con cebolla, tomate y papas fritas.", precio = 14500, imagenUrl = "lomo"),
                Plato(nombre = "Ají de Gallina", descripcion = "Pollo deshilachado en una crema de ají amarillo y nueces.", precio = 10500, imagenUrl = "aji"),
                Plato(nombre = "Anticuchos", descripcion = "Carne de res marinado y asado a la parrilla.", precio = 9000, imagenUrl = "anticuchos"),
                Plato(nombre = "Causa Limeña", descripcion = "Suave masa de papa amarilla con ají, rellena de pollo y palta.", precio = 9500, imagenUrl = "causa"),
                Plato(nombre = "Rocoto Relleno", descripcion = "Rocoto horneado relleno de carne picada, queso y especias arequipeñas.", precio = 11000, imagenUrl = "rocoto"),
                Plato(nombre = "Papa a la Huancaína", descripcion = "Papas sancochadas bañadas en una cremosa salsa de ají amarillo y queso.", precio = 8500, imagenUrl = "huancaina"),
                Plato(nombre = "Tacu Tacu con Lomo", descripcion = "Mezcla criolla de frijoles y arroz frito, montado con un jugoso lomo.", precio = 13500, imagenUrl = "tacutacu")
            )
            db.platoDao().insertarPlatos(platosIniciales)
        }
    }

    //USUARIO Y SESION
    fun obtenerUsuarioActivo(id: Int): Flow<Usuario?> {
        return if (id != -1) db.usuarioDao().obtenerUsuarioPorId(id) else emptyFlow()
    }

    suspend fun login(correo: String): Usuario? {
        return db.usuarioDao().buscarPorCorreo(correo)
    }

    suspend fun registrarUsuario(usuario: Usuario): Long {
        return db.usuarioDao().insertarUsuario(usuario)
    }

    suspend fun eliminarUsuario(usuario: Usuario) {
        db.usuarioDao().eliminarUsuario(usuario)
        db.carritoDao().vaciarCarrito()
    }

    //CARRITO
    val carrito: Flow<List<DetalleCarrito>> = db.carritoDao().obtenerCarritoConDetalles()

    suspend fun agregarAlCarrito(platoId: Int) {
        val itemExistente = db.carritoDao().obtenerItemPorPlato(platoId)
        if (itemExistente != null) {
            db.carritoDao().actualizarItem(itemExistente.copy(cantidad = itemExistente.cantidad + 1))
        } else {
            db.carritoDao().insertarItem(ItemCarrito(platoId = platoId, cantidad = 1))
        }
    }

    suspend fun reducirCantidadOEliminar(detalle: DetalleCarrito) {
        val item = ItemCarrito(detalle.idItem, detalle.plato.id, detalle.cantidad)
        if (item.cantidad > 1) {
            db.carritoDao().actualizarItem(item.copy(cantidad = item.cantidad - 1))
        } else {
            db.carritoDao().eliminarItem(item)
        }
    }
}