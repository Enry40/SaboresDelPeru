package com.example.prueba_app.repository

import com.example.prueba_app.model.Plato
import kotlinx.coroutines.flow.Flow

interface ComidaDataSource {
    val platos: Flow<List<Plato>>
    suspend fun inicializarDatos()
}
