package com.example.prueba_app.model

import androidx.room.*
import kotlinx.coroutines.flow.Flow

//ENTIDADES

@Entity(tableName = "platos")
data class Plato(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val descripcion: String,
    val precio: Int,
    val imagenUrl: String
)

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val apellido: String,
    val direccion: String,
    val ciudad: String,
    val correo: String,
    val telefono: String
)

@Entity(tableName = "carrito")
data class ItemCarrito(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val platoId: Int,
    val cantidad: Int
)

data class DetalleCarrito(
    val idItem: Int,
    @Embedded val plato: Plato,
    val cantidad: Int
)

//DAOs (Data Access Objects)

@Dao
interface PlatoDao {
    @Query("SELECT * FROM platos")
    fun obtenerPlatos(): Flow<List<Plato>>

    // ✅ NUEVO: para tests (sin Flow)
    @Query("SELECT * FROM platos")
    suspend fun obtenerPlatosList(): List<Plato>

    @Query("SELECT * FROM platos WHERE id = :id")
    suspend fun obtenerPlatoPorId(id: Int): Plato?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarPlatos(platos: List<Plato>)

    @Query("SELECT COUNT(*) FROM platos")
    suspend fun contarPlatos(): Int
}

@Dao
interface UsuarioDao {
    //BUSCA UN USUARIO EN ESPECIFICO POR SU ID (para mantener la sesión activa)
    @Query("SELECT * FROM usuarios WHERE id = :id")
    fun obtenerUsuarioPorId(id: Int): Flow<Usuario?>

    //BUSCA POR CORREO PARA EL LOGIN
    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): Usuario?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUsuario(usuario: Usuario): Long // Devuelve el ID insertado

    @Delete
    suspend fun eliminarUsuario(usuario: Usuario)
}

@Dao
interface CarritoDao {
    @Query("SELECT c.id as idItem, c.cantidad, p.* FROM carrito c INNER JOIN platos p ON c.platoId = p.id")
    fun obtenerCarritoConDetalles(): Flow<List<DetalleCarrito>>

    @Query("SELECT * FROM carrito WHERE platoId = :platoId LIMIT 1")
    suspend fun obtenerItemPorPlato(platoId: Int): ItemCarrito?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarItem(item: ItemCarrito)

    @Update
    suspend fun actualizarItem(item: ItemCarrito)

    @Delete
    suspend fun eliminarItem(item: ItemCarrito)

    @Query("DELETE FROM carrito")
    suspend fun vaciarCarrito()
}
