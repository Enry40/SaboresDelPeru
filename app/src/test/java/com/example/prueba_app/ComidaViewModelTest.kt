package com.example.prueba_app

import app.cash.turbine.test
import com.example.prueba_app.model.Plato
import com.example.prueba_app.repository.ComidaDataSource
import com.example.prueba_app.viewmodel.ComidaViewModel
import com.example.prueba_app.viewmodel.PlatosUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ComidaViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    @Test
    fun `init refresh emite Loading y luego Success cuando repo responde`() = runTest(mainRule.dispatcher) {
        val platosFake = listOf(
            Plato(
                id = 1,
                nombre = "Ceviche",
                descripcion = "",
                precio = 0,
                imagenUrl = "https://img.com/ceviche.jpg"
            )
        )

        val repoFake = object : ComidaDataSource {
            override val platos = flowOf(platosFake)
            override suspend fun inicializarDatos() { /* OK */ }
        }

        val vm = ComidaViewModel(repo = repoFake, dispatcher = mainRule.dispatcher)

        vm.uiState.test {
            // 1) Estado inicial
            assertTrue(awaitItem() is PlatosUiState.Loading)

            // 2) Avanza coroutines
            mainRule.dispatcher.scheduler.advanceUntilIdle()

            // 3) Debe llegar Success
            val next = awaitItem()
            assertTrue(next is PlatosUiState.Success)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refresh emite Loading y luego Error cuando repo lanza exception`() = runTest(mainRule.dispatcher) {
        val repoFake = object : ComidaDataSource {
            override val platos = flowOf(emptyList<Plato>())
            override suspend fun inicializarDatos() { throw RuntimeException("API caída") }
        }

        val vm = ComidaViewModel(repo = repoFake, dispatcher = mainRule.dispatcher)

        vm.uiState.test {
            assertTrue(awaitItem() is PlatosUiState.Loading)

            mainRule.dispatcher.scheduler.advanceUntilIdle()

            val next = awaitItem()
            assertTrue(next is PlatosUiState.Error)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
