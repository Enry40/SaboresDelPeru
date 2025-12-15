package com.example.prueba_app

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.prueba_app.model.DetalleCarrito
import com.example.prueba_app.model.Plato
import com.example.prueba_app.model.Usuario
import com.example.prueba_app.repository.ComidaDataSource
import com.example.prueba_app.ui.theme.AppComidaPeruana
import com.example.prueba_app.viewmodel.ComidaViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppComidaPeruanaUITest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun muestraTextoAppOk() {
        // Repositorio falso solo para este test de UI
        val fakeRepo = object : ComidaDataSource {

            override val platos: Flow<List<Plato>> =
                MutableStateFlow(emptyList())   // MutableStateFlow implementa Flow, sirve perfecto

            override val carrito: Flow<List<DetalleCarrito>> =
                MutableStateFlow(emptyList())

            override suspend fun inicializarDatos() {
                // no-op para el test
            }

            override suspend fun agregarAlCarrito(platoId: Int, usuarioId: Int) {
                // no-op
            }

            override suspend fun reducirCantidadOEliminar(
                detalle: DetalleCarrito,
                usuarioId: Int
            ) {
                // no-op
            }

            override suspend fun registrarUsuario(usuario: Usuario): Long = 1L

            override suspend fun login(correo: String, contrasena: String): Usuario? = null

            override fun obtenerUsuarioActivo(id: Int): Flow<Usuario?> = flowOf(null)

            override suspend fun eliminarUsuario(usuario: Usuario) {
                // no-op
            }

            // método extra que también existe en la INTERFAZ ComidaDataSource
            override suspend fun eliminarCarritoPorId(
                idDetalle: Int,
                usuarioId: Int
            ) {
                // no-op en el test
            }
        }

        val vm = ComidaViewModel(repositorio = fakeRepo)

        rule.setContent {
            AppComidaPeruana(viewModel = vm, onExitApp = {})
        }

        // Este texto es el que pusimos en AppComidaPeruana()
        rule.onNodeWithText("App Comida Peruana OK ✅")
            .assertIsDisplayed()
    }
}
