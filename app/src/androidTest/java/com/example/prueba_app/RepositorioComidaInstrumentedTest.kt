package com.example.prueba_app

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.prueba_app.model.AppDatabase
import com.example.prueba_app.network.InterfazApi
import com.example.prueba_app.repository.RepositorioComida
import com.google.gson.Gson
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Instrumented tests:
 * - Room inMemory
 * - MockWebServer simulando API
 */
@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class RepositorioComidaInstrumentedTest {

    private lateinit var db: AppDatabase
    private lateinit var server: MockWebServer
    private lateinit var api: InterfazApi
    private lateinit var repo: RepositorioComida

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()

        // Room in-memory
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries() // OK solo para tests
            .build()

        // MockWebServer
        server = MockWebServer()
        server.start()

        // Retrofit apuntando al MockWebServer
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(InterfazApi::class.java)

        // Repo con API + dispatcher inyectados
        repo = RepositorioComida(
            db = db,
            api = api,
            io = testDispatcher
        )
    }

    @After
    fun tearDown() {
        db.close()
        server.shutdown()
    }

    @Test
    fun inicializarDatos_cuandoApiFalla_insertaPlatosIniciales() = runTest(testDispatcher) {
        // API responde 500
        server.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody("""{ "error": "server error" }""")
        )

        repo.inicializarDatos()

        val platos = db.platoDao().obtenerPlatosList()
        assertTrue(platos.isNotEmpty())

        // Validamos que entró el fallback (uno de los platos iniciales)
        assertTrue(platos.any { it.nombre == "Ceviche Clásico" })
    }

    @Test
    fun inicializarDatos_cuandoApiRetornaLista_guardaEnRoom() = runTest(testDispatcher) {
        /**
         * Ajusta este JSON al formato real de tu API si tu DTO PlatoApi usa otros nombres.
         * Este test asume que tu endpoint retorna un JSON array:
         * [
         *   {"id":101,"nombre":"Plato API Test","descripcion":"desc","precio":9990,"imagenUrl":"img"}
         * ]
         */
        val bodyJson = Gson().toJson(
            listOf(
                mapOf(
                    "id" to 101,
                    "nombre" to "Plato API Test",
                    "descripcion" to "desc",
                    "precio" to 9990,
                    "imagenUrl" to "img"
                )
            )
        )

        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(bodyJson)
        )

        repo.inicializarDatos()

        val platos = db.platoDao().obtenerPlatosList()
        assertTrue(platos.isNotEmpty())

        // Si tu mapeo toEntity() está OK, debería guardarse este nombre
        assertTrue(platos.any { it.nombre == "Plato API Test" })
    }
}
