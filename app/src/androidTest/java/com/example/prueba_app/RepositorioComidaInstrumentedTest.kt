package com.example.prueba_app

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.room.Room
import com.example.prueba_app.model.AppDatabase
import com.example.prueba_app.repository.RepositorioComida
import com.example.prueba_app.network.InterfazApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@RunWith(AndroidJUnit4::class)
class RepositorioComidaInstrumentedTest {

    private lateinit var db: AppDatabase
    private lateinit var server: MockWebServer
    private lateinit var api: InterfazApi

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()

        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries() // solo para tests
            .build()

        server = MockWebServer()
        server.start()

        api = Retrofit.Builder()
            .baseUrl(server.url("/")) // URL fake
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(InterfazApi::class.java)
    }

    @After
    fun tearDown() {
        db.close()
        server.shutdown()
    }

    @Test
    fun inicializarDatos_insertaPlatosDeApi_enRoom() = runTest {
        // Ajusta el JSON a tu DTO real (campos/nombres)
        val json = """
        [
          {"id": 1, "nombre": "Ceviche", "descripcion": "rico", "precio": 12000, "imagen_url": "https://img/ceviche.jpg"}
        ]
        """.trimIndent()

        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(json)
                .addHeader("Content-Type", "application/json")
        )

        val repo = RepositorioComida(
            db = db,
            api = api,
            io = UnconfinedTestDispatcher(testScheduler)
        )

        repo.inicializarDatos()

        val count = db.platoDao().contarPlatos()
        assertTrue("Debió insertar al menos 1 plato desde API", count > 0)
    }
}
