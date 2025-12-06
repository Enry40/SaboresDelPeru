package com.example.prueba_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.example.prueba_app.model.DetalleCarrito
import com.example.prueba_app.model.Plato
import com.example.prueba_app.model.Usuario
import com.example.prueba_app.repository.RepositorioComida
import kotlinx.coroutines.ExperimentalCoroutinesApi

class ComidaViewModel(private val repositorio: RepositorioComida) : ViewModel() {

    //GESTION DE SESION
    private val _idUsuarioSesion = MutableStateFlow<Int>(-1) // -1 indica no logueado

    @OptIn(ExperimentalCoroutinesApi::class)
    val usuarioActivo: StateFlow<Usuario?> = _idUsuarioSesion
        .flatMapLatest { id ->
            if (id != -1) repositorio.obtenerUsuarioActivo(id) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    //LOS PLATOS Y BUSQUEDA
    private val _consultaBusqueda = MutableStateFlow("")
    val consultaBusqueda = _consultaBusqueda.asStateFlow()

    val listaPlatos: StateFlow<List<Plato>> = repositorio.platos
        .combine(_consultaBusqueda) { platos, query ->
            if (query.isEmpty()) platos else platos.filter { it.nombre.contains(query, ignoreCase = true) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    //EL CARRITO
    val carrito: StateFlow<List<DetalleCarrito>> = repositorio.carrito
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCarrito: StateFlow<Int> = carrito.map { items ->
        items.sumOf { it.plato.precio * it.cantidad }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val cantidadProductos: StateFlow<Int> = carrito.map { items ->
        items.sumOf { it.cantidad }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        viewModelScope.launch {
            repositorio.inicializarDatos()
        }
    }

    //FUNCIONES DE USUARIO

    fun iniciarSesion(correo: String, onError: () -> Unit, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val usuario = repositorio.login(correo)
            if (usuario != null) {
                _idUsuarioSesion.value = usuario.id
                onSuccess()
            } else {
                onError()
            }
        }
    }

    // onSuccess PARA CONFIRMAR EL GUARDADO
    fun registrarUsuario(usuario: Usuario, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val nuevoId = repositorio.registrarUsuario(usuario)
            _idUsuarioSesion.value = nuevoId.toInt()
            onSuccess() // Ejecutamos la acción (navegación/toast) solo al terminar
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

    //FUNCIONES VARIAS

    fun actualizarBusqueda(query: String) {
        _consultaBusqueda.value = query
    }

    fun agregarAlCarrito(platoId: Int, onNoRegistrado: () -> Unit, onExito: () -> Unit) {
        if (usuarioActivo.value == null) {
            onNoRegistrado()
        } else {
            viewModelScope.launch {
                repositorio.agregarAlCarrito(platoId)
                onExito()
            }
        }
    }

    fun eliminarDelCarrito(detalle: DetalleCarrito) {
        viewModelScope.launch {
            repositorio.reducirCantidadOEliminar(detalle)
        }
    }
}

class ComidaViewModelFactory(private val repositorio: RepositorioComida) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ComidaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ComidaViewModel(repositorio) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}