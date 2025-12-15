package com.example.prueba_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prueba_app.model.DetalleCarrito
import com.example.prueba_app.model.Plato
import com.example.prueba_app.model.Usuario
import com.example.prueba_app.repository.ComidaDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ComidaViewModel(
    private val repositorio: ComidaDataSource
) : ViewModel() {

    private val _idUsuarioSesion = MutableStateFlow(-1)

    @OptIn(ExperimentalCoroutinesApi::class)
    val usuarioActivo: StateFlow<Usuario?> = _idUsuarioSesion
        .flatMapLatest { id ->
            if (id != -1) repositorio.obtenerUsuarioActivo(id) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _consultaBusqueda = MutableStateFlow("")
    val consultaBusqueda: StateFlow<String> = _consultaBusqueda

    private val _buscadorVisible = MutableStateFlow(false)
    val buscadorVisible: StateFlow<Boolean> = _buscadorVisible

    val listaPlatos: StateFlow<List<Plato>> = repositorio.platos
        .combine(_consultaBusqueda) { platos, query ->
            if (query.isEmpty()) platos
            else platos.filter { it.nombre.contains(query, ignoreCase = true) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val carrito: StateFlow<List<DetalleCarrito>> = repositorio.carrito
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCarrito: StateFlow<Int> = carrito
        .map { items -> items.sumOf { it.plato.precio * it.cantidad } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val cantidadProductos: StateFlow<Int> = carrito
        .map { items -> items.sumOf { it.cantidad } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        viewModelScope.launch {
            repositorio.inicializarDatos()
        }
    }

    fun iniciarSesion(
        correo: String,
        contrasena: String,
        onError: () -> Unit,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val usuario = repositorio.login(correo, contrasena)
            if (usuario != null) {
                _idUsuarioSesion.value = usuario.id
                onSuccess()
            } else {
                onError()
            }
        }
    }

    fun registrarUsuario(usuario: Usuario, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val nuevoId = repositorio.registrarUsuario(usuario)
            _idUsuarioSesion.value = nuevoId.toInt()
            onSuccess()
        }
    }

    fun cerrarSesion() {
        _idUsuarioSesion.value = -1
    }

    fun eliminarUsuarioActual() {
        val usuario = usuarioActivo.value
        if (usuario != null) {
            viewModelScope.launch {
                repositorio.eliminarUsuario(usuario)
                _idUsuarioSesion.value = -1
            }
        }
    }

    fun actualizarBusqueda(query: String) {
        _consultaBusqueda.value = query
    }

    fun toggleBuscador() {
        _buscadorVisible.value = !_buscadorVisible.value
    }

    fun mostrarBuscador(mostrar: Boolean) {
        _buscadorVisible.value = mostrar
    }

    fun agregarAlCarrito(
        platoId: Int,
        onNoRegistrado: () -> Unit,
        onExito: () -> Unit
    ) {
        val usuario = usuarioActivo.value
        if (usuario == null) {
            onNoRegistrado()
        } else {
            viewModelScope.launch {
                repositorio.agregarAlCarrito(platoId, usuario.id)
                onExito()
            }
        }
    }

    fun eliminarDelCarrito(detalle: DetalleCarrito) {
        val usuario = usuarioActivo.value
        viewModelScope.launch {
            repositorio.reducirCantidadOEliminar(detalle, usuario?.id ?: -1)
        }
    }
}
