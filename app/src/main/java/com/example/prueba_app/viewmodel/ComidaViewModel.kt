package com.example.prueba_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prueba_app.model.Plato
import com.example.prueba_app.repository.ComidaDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class PlatosUiState {
    data object Loading : PlatosUiState()
    data class Success(val platos: List<Plato>) : PlatosUiState()
    data class Error(val message: String) : PlatosUiState()
}

class ComidaViewModel(
    private val repo: ComidaDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow<PlatosUiState>(PlatosUiState.Loading)
    val uiState: StateFlow<PlatosUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch(dispatcher) {
            _uiState.value = PlatosUiState.Loading
            try {
                repo.inicializarDatos()

                // ✅ Espera el primer emit no vacío; si quieres permitir vacío, cambia esta línea a repo.platos.first()
                val data = repo.platos.first { it.isNotEmpty() }

                _uiState.value = PlatosUiState.Success(data)
            } catch (e: Exception) {
                _uiState.value = PlatosUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}
