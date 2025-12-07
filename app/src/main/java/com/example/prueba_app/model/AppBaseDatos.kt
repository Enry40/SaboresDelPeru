package com.example.prueba_app.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Plato::class, Usuario::class, ItemCarrito::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun platoDao(): PlatoDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun carritoDao(): CarritoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "saboresdelperu_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}