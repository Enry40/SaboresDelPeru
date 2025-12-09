package com.example.prueba_app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.prueba_app.model.AppDatabase
import com.example.prueba_app.model.Usuario
import com.example.prueba_app.network.AuthResponse
import com.example.prueba_app.network.CarritoRequest
import com.example.prueba_app.network.InterfazApi
import com.example.prueba_app.network.LoginRequest
import com.example.prueba_app.network.PlatoApi
import com.example.prueba_app.repository.RepositorioComida
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertEquals
import retrofit2.Response

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class RepositorioComidaInstrumentedTest {

    private lateinit var db: AppDatabase
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries() // solo tests
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `inicializarDatos guarda platos desde API en Room`() = runTest(dispatcher) {
        val fakeApi = object : InterfazApi {
            override suspend fun getPlatos(): Response<List<PlatoApi>> =
                Response.success(
                    listOf(
                        PlatoApi(1, "Ceviche", "https://img.com/ceviche.jpg"),
                        PlatoApi(2, "Lomo", "https://img.com/lomo.jpg")
                    )
                )

            // No usados en este test:
            override suspend fun login(request: LoginRequest): Response<AuthResponse> =
                Response.error(501, ResponseBody.create(null, ""))

            override suspend fun signup(usuario: Usuario): Response<AuthResponse> =
                Response.error(501, ResponseBody.create(null, ""))

            override suspend fun updateUser(id: Int, usuario: Usuario): Response<Usuario> =
                Response.error(501, ResponseBody.create(null, ""))

            override suspend fun deleteUser(id: Int): Response<Unit> =
                Response.error(501, ResponseBody.create(null, ""))

            override suspend fun addToCart(request: CarritoRequest): Response<Unit> =
                Response.error(501, ResponseBody.create(null, ""))

            override suspend fun removeFromCart(request: CarritoRequest): Response<Unit> =
                Response.error(501, ResponseBody.create(null, ""))
        }

        val repo = RepositorioComida(db = db, api = fakeApi, io = dispatcher)

        repo.inicializarDatos()
        dispatcher.scheduler.advanceUntilIdle()

        val count = db.platoDao().contarPlatos()
        assertEquals(2, count)
    }
}
