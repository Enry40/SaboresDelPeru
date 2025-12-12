package com.example.prueba_app.model

import androidx.room.*
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.flow.Flow

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
    @SerializedName("email") val correo: String,
    val telefono: String,
    // NUEVO CAMPO:
    val password: String
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

// DAOs

@Dao
interface PlatoDao {
    @Query("SELECT * FROM platos")
    fun obtenerPlatos(): Flow<List<Plato>>

    @Query("SELECT * FROM platos WHERE id = :id")
    suspend fun obtenerPlatoPorId(id: Int): Plato?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarPlatos(platos: List<Plato>)

    @Query("SELECT COUNT(*) FROM platos")
    suspend fun contarPlatos(): Int
}

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUsuario(usuario: Usuario): Long

    @Query("SELECT * FROM usuarios WHERE id = :id")
    fun obtenerUsuarioPorId(id: Int): Flow<Usuario?>

    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): Usuario?

    @Query("SELECT * FROM usuarios")
    fun obtenerTodos(): Flow<List<Usuario>>

    @Update
    suspend fun actualizarUsuario(usuario: Usuario): Int

    @Delete
    suspend fun eliminarUsuario(usuario: Usuario)

    @Query("DELETE FROM usuarios WHERE id = :id")
    suspend fun eliminarUsuarioPorId(id: Int)
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

    @Query("DELETE FROM carrito WHERE id = :idItem")
    suspend fun eliminarItemPorId(idItem: Int)
}