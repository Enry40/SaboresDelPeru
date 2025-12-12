package com.example.prueba_app

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.prueba_app.repository.ComidaDataSource
import com.example.prueba_app.ui.theme.AppComidaPeruana
import com.example.prueba_app.viewmodel.ComidaViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Rule
import org.junit.Test

class AppComidaPeruanaUiTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun muestraTextoAppOk() {
        val fakeRepo = object : ComidaDataSource {
            private val _platos = MutableStateFlow(emptyList<com.example.prueba_app.model.Plato>())
            override val platos = _platos.asStateFlow()
            override suspend fun inicializarDatos() { /* no-op */ }
        }

        val vm = ComidaViewModel(fakeRepo)

        rule.setContent {
            AppComidaPeruana(viewModel = vm, onExitApp = {})
        }

        rule.onNodeWithText("App Comida Peruana OK").assertIsDisplayed()
    }
}
