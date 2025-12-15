package com.example.prueba_app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.prueba_app.model.AppDatabase
import com.example.prueba_app.repository.RepositorioComida
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RepositorioComidaInstrumentedTest {

    private lateinit var db: AppDatabase
    private lateinit var repo: RepositorioComida

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // BD en memoria SOLO para tests
        db = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .allowMainThreadQueries()   // OK en tests
            .build()

        repo = RepositorioComida(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun inicializarDatos_insertaPlatosEnBD() = runTest {
        // 1. Ejecutar la lógica real de inicialización
        repo.inicializarDatos()

        // 2. Leer desde Room lo que quedó guardado
        val platosGuardados = db.platoDao().obtenerPlatos().first()

        // 3. Afirmar algo CONCRETO: que haya al menos un plato
        assertTrue(
            "La lista de platos debería tener datos después de inicializarDatos()",
            platosGuardados.isNotEmpty()
        )
    }
}
