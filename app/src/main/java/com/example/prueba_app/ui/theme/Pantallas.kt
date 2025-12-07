package com.example.prueba_app.ui.theme

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import java.text.NumberFormat
import java.util.Locale
import com.example.prueba_app.model.DetalleCarrito
import com.example.prueba_app.model.Plato
import com.example.prueba_app.model.Usuario
import com.example.prueba_app.viewmodel.ComidaViewModel
import com.example.prueba_app.ui.theme.camara.PantallaCamara

// Utilidad para moneda CLP
fun formatoCLP(monto: Int): String {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
    return format.format(monto)
}

// Barra Superior con Menú
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperior(onExit: () -> Unit) {
    var mostrarMenu by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        title = {
            Text(
                "MENU DE SELECCIÓN",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        ),
        actions = {
            IconButton(onClick = { mostrarMenu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
            }
            DropdownMenu(
                expanded = mostrarMenu,
                onDismissRequest = { mostrarMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Salir") },
                    onClick = {
                        mostrarMenu = false
                        onExit()
                    },
                    leadingIcon = {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.Red)
                    }
                )
            }
        }
    )
}

// Pantalla Principal (Scaffold con Navegación)
@Composable
fun AppComidaPeruana(viewModel: ComidaViewModel, onExitApp: () -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val cantidadCarrito by viewModel.cantidadProductos.collectAsState()

    // Estado de visibilidad del buscador
    val esBuscadorVisible by viewModel.buscadorVisible.collectAsState()

    Scaffold(
        bottomBar = {
            if (currentRoute != "splash") {
                NavigationBar {
                    // 1. BUSCAR
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                        label = { Text("Buscar") },
                        // Seleccionado solo si estamos en home Y el buscador es visible
                        selected = currentRoute == "home" && esBuscadorVisible,
                        onClick = {
                            if (currentRoute == "home") {
                                // Si ya estamos en home, alternamos (mostrar/esconder)
                                viewModel.toggleBuscador()
                            } else {
                                // Si venimos de otra pantalla, vamos a home y forzamos mostrar
                                viewModel.mostrarBuscador(true)
                                navController.navigate("home") {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )

                    // 2. CARRITO
                    NavigationBarItem(
                        icon = {
                            BadgedBox(badge = {
                                if (cantidadCarrito > 0) Badge { Text("$cantidadCarrito") }
                            }) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito")
                            }
                        },
                        label = { Text("Carrito") },
                        selected = currentRoute == "carrito",
                        onClick = {
                            if (currentRoute != "carrito") {
                                navController.navigate("carrito") {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )

                    // 3. MENÚ (Oculta buscador)
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.RestaurantMenu, contentDescription = "Menú") },
                        label = { Text("Menú") },
                        selected = currentRoute == "home" && !esBuscadorVisible,
                        onClick = {
                            // Ocultamos el buscador para ver solo la lista
                            viewModel.mostrarBuscador(false)
                            if (currentRoute != "home") {
                                navController.navigate("home") {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )

                    // 4. CÁMARA
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.CameraAlt, contentDescription = "Cámara") },
                        label = { Text("Cámara") },
                        selected = currentRoute == "camara",
                        onClick = {
                            if (currentRoute != "camara") {
                                navController.navigate("camara") {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )

                    // 5. USUARIO
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = "Usuario") },
                        label = { Text("Usuario") },
                        selected = currentRoute == "usuario",
                        onClick = {
                            if (currentRoute != "usuario") {
                                navController.navigate("usuario") {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(innerPadding),
            enterTransition = { TransicionesApp.enter(this) },
            exitTransition = { TransicionesApp.exit(this) },
            popEnterTransition = { TransicionesApp.popEnter(this) },
            popExitTransition = { TransicionesApp.popExit(this) }
        ) {
            composable("splash") {
                PantallaBienvenida(onTimeout = {
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                })
            }
            composable("home") {
                PantallaListaPlatos(viewModel, onExit = onExitApp) { platoId ->
                    navController.navigate("detalle/$platoId")
                }
            }
            composable("detalle/{platoId}") { backStackEntry ->
                val platoId = backStackEntry.arguments?.getString("platoId")?.toIntOrNull()
                PantallaDetallePlato(platoId, viewModel)
            }
            composable("carrito") {
                PantallaCarrito(viewModel)
            }
            composable("camara") {
                PantallaCamara(onExit = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                })
            }
            composable("usuario") {
                PantallaUsuario(viewModel)
            }
        }
    }

    if (currentRoute == "home") {
        BackHandler { onExitApp() }
    }
}

// Pantallas Básicas
@Composable
fun PantallaBienvenida(onTimeout: () -> Unit) {
    LaunchedEffect(true) {
        kotlinx.coroutines.delay(2000)
        onTimeout()
    }
    Box(
        modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFD32F2F), Color(0xFFEF5350)))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.RestaurantMenu, contentDescription = null, modifier = Modifier.size(100.dp), tint = Color.White)
            Text("Sabores del Perú", style = MaterialTheme.typography.headlineLarge, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PantallaListaPlatos(viewModel: ComidaViewModel, onExit: () -> Unit, onPlatoClick: (Int) -> Unit) {
    val platos by viewModel.listaPlatos.collectAsState()
    val busqueda by viewModel.consultaBusqueda.collectAsState()
    val esVisible by viewModel.buscadorVisible.collectAsState()

    Scaffold(
        topBar = {
            BarraSuperior(onExit = onExit)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .animateContentSize(animationSpec = tween(500))
        ) {

            AnimatedVisibility(
                visible = esVisible,
                enter = expandVertically(animationSpec = tween(500)) + slideInVertically(
                    initialOffsetY = { it * 2 },
                    animationSpec = tween(500)
                ) + fadeIn(animationSpec = tween(500)),

                exit = shrinkVertically(animationSpec = tween(500)) + slideOutVertically(
                    targetOffsetY = { it * 2 },
                    animationSpec = tween(500)
                ) + fadeOut(animationSpec = tween(500))
            ) {
                Column {
                    OutlinedTextField(
                        value = busqueda,
                        onValueChange = { viewModel.actualizarBusqueda(it) },
                        label = { Text("Buscar plato...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(platos) { plato ->
                    Card(modifier = Modifier.fillMaxWidth().clickable { onPlatoClick(plato.id) }, elevation = CardDefaults.cardElevation(4.dp)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Fastfood, contentDescription = null, modifier = Modifier.size(60.dp), tint = Color(0xFFD32F2F))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(plato.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(formatoCLP(plato.precio), color = Color(0xFF388E3C))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PantallaDetallePlato(platoId: Int?, viewModel: ComidaViewModel) {
    val platos by viewModel.listaPlatos.collectAsState()
    val plato = platos.find { it.id == platoId }
    val context = LocalContext.current

    if (plato != null) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp)).background(Color.LightGray), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.Gray)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(plato.nombre, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(formatoCLP(plato.precio), style = MaterialTheme.typography.headlineSmall, color = Color(0xFF388E3C))
            Spacer(modifier = Modifier.height(16.dp))
            Text(plato.descripcion)
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    viewModel.agregarAlCarrito(
                        platoId = plato.id,
                        onNoRegistrado = { Toast.makeText(context, "Debe estar registrado para agregar productos al carrito de compras", Toast.LENGTH_LONG).show() },
                        onExito = { Toast.makeText(context, "Agregado al carrito", Toast.LENGTH_SHORT).show() }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) { Text("AGREGAR") }
        }
    }
}

@Composable
fun PantallaCarrito(viewModel: ComidaViewModel) {
    val carrito by viewModel.carrito.collectAsState()
    val total by viewModel.totalCarrito.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Tu Pedido", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(carrito) { detalle ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(detalle.plato.nombre, fontWeight = FontWeight.Bold)
                        Text("${detalle.cantidad} x ${formatoCLP(detalle.plato.precio)}")
                    }
                    Text(formatoCLP(detalle.plato.precio * detalle.cantidad), fontWeight = FontWeight.Bold)
                    IconButton(onClick = { viewModel.eliminarDelCarrito(detalle) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                    }
                }
                Divider()
            }
        }
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total:", style = MaterialTheme.typography.titleLarge)
                Text(formatoCLP(total), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Pantalla de Usuario
@Composable
fun PantallaUsuario(viewModel: ComidaViewModel) {
    val usuario by viewModel.usuarioActivo.collectAsState()
    val context = LocalContext.current

    var vistaActual by remember { mutableStateOf("menu") }
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var ciudad by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }

    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
    var mostrarDialogoDatos by remember { mutableStateOf(false) }

    LaunchedEffect(usuario) {
        usuario?.let {
            nombre = it.nombre
            apellido = it.apellido
            direccion = it.direccion
            ciudad = it.ciudad
            correo = it.correo
            telefono = it.telefono
            if (vistaActual == "menu" || vistaActual == "login" || vistaActual == "registro") {
                vistaActual = "perfil_menu"
            }
        } ?: run {
            if (vistaActual == "perfil_menu" || vistaActual == "perfil_modificar") {
                vistaActual = "menu"
            }
        }
    }

    BackHandler(enabled = vistaActual != "menu" && vistaActual != "perfil_menu") {
        if (usuario == null) {
            vistaActual = "menu"
        } else {
            vistaActual = "perfil_menu"
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (usuario == null) {
            when (vistaActual) {
                "menu" -> {
                    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(120.dp), tint = Color.Gray)
                        Text("Bienvenido", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 16.dp))
                        Spacer(modifier = Modifier.weight(1f))
                        Button(onClick = { vistaActual = "login" }, modifier = Modifier.fillMaxWidth().height(50.dp)) { Text("Iniciar Sesión") }
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(onClick = { vistaActual = "registro" }, modifier = Modifier.fillMaxWidth().height(50.dp)) { Text("Registrarse") }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                "login" -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        IconButton(onClick = { vistaActual = "menu" }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") }
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Text("Bienvenido de nuevo", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(24.dp))
                                OutlinedTextField(value = correo, onValueChange = { correo = it }, label = { Text("Correo electrónico") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), singleLine = true)
                                Spacer(modifier = Modifier.height(32.dp))
                                Button(onClick = {
                                    if (correo.isNotEmpty()) {
                                        viewModel.iniciarSesion(correo = correo, onError = { Toast.makeText(context, "Usuario no registrado, debe registrarse", Toast.LENGTH_LONG).show() }, onSuccess = {
                                            Toast.makeText(context, "Bienvenido", Toast.LENGTH_SHORT).show()
                                            correo = ""
                                        })
                                    } else {
                                        Toast.makeText(context, "Ingrese su correo", Toast.LENGTH_SHORT).show()
                                    }
                                }, modifier = Modifier.fillMaxWidth().height(50.dp)) { Text("INGRESAR") }
                            }
                        }
                    }
                }
                "registro" -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { vistaActual = "menu" }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") }
                            Text("Crear cuenta", style = MaterialTheme.typography.titleLarge)
                        }
                        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(top = 16.dp)) {
                            OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = apellido, onValueChange = { apellido = it }, label = { Text("Apellido") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = direccion, onValueChange = { direccion = it }, label = { Text("Dirección") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = ciudad, onValueChange = { ciudad = it }, label = { Text("Ciudad") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = correo, onValueChange = { correo = it }, label = { Text("Correo (ej: @)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = telefono, onValueChange = { telefono = it }, label = { Text("Teléfono") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = {
                                if (nombre.isEmpty() || apellido.isEmpty() || direccion.isEmpty() || ciudad.isEmpty() || correo.isEmpty() || telefono.isEmpty()) {
                                    Toast.makeText(context, "Debe completar todos los campos para registrar sus datos", Toast.LENGTH_LONG).show()
                                } else if (!correo.contains("@")) {
                                    Toast.makeText(context, "El correo debe contener un @", Toast.LENGTH_LONG).show()
                                } else {
                                    viewModel.registrarUsuario(Usuario(nombre = nombre, apellido = apellido, direccion = direccion, ciudad = ciudad, correo = correo, telefono = telefono), onSuccess = {
                                        Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()
                                        nombre = ""; apellido = ""; direccion = ""; ciudad = ""; correo = ""; telefono = ""
                                    })
                                }
                            }, modifier = Modifier.fillMaxWidth().height(50.dp)) { Text("REGISTRARME") }
                            Spacer(modifier = Modifier.height(200.dp))
                        }
                    }
                }
            }
        } else {
            val usuarioLogueado = usuario!!
            when (vistaActual) {
                "perfil_menu" -> {
                    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(modifier = Modifier.height(40.dp))
                        Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(120.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Hola, ${usuarioLogueado.nombre}", style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.weight(1f))
                        Button(onClick = { mostrarDialogoDatos = true }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                            Icon(Icons.Default.Visibility, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("MOSTRAR DATOS")
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { vistaActual = "perfil_modificar" }, modifier = Modifier.fillMaxWidth().height(50.dp)) { Text("MODIFICAR DATOS") }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(onClick = { viewModel.cerrarSesion() }, modifier = Modifier.fillMaxWidth().height(50.dp)) { Text("CERRAR SESIÓN") }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(onClick = { mostrarDialogoEliminar = true }, colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red), modifier = Modifier.fillMaxWidth().height(50.dp)) { Text("ELIMINAR USUARIO") }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                "perfil_modificar" -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { vistaActual = "perfil_menu" }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") }
                            Text("Modificar Mis Datos", style = MaterialTheme.typography.titleLarge)
                        }
                        Column(modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()).padding(top = 16.dp)) {
                            OutlinedTextField(value = direccion, onValueChange = { direccion = it }, label = { Text("Dirección") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = ciudad, onValueChange = { ciudad = it }, label = { Text("Ciudad") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = correo, onValueChange = { correo = it }, label = { Text("Correo (ej: @)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = telefono, onValueChange = { telefono = it }, label = { Text("Teléfono") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
                        }
                        Button(onClick = {
                            if (direccion.isEmpty() || ciudad.isEmpty() || correo.isEmpty() || telefono.isEmpty()) {
                                Toast.makeText(context, "Debe completar todos los campos para modificar sus datos", Toast.LENGTH_LONG).show()
                            } else if (!correo.contains("@")) {
                                Toast.makeText(context, "El correo debe contener un @", Toast.LENGTH_LONG).show()
                            } else {
                                viewModel.registrarUsuario(
                                    Usuario(id = usuarioLogueado.id, nombre = usuarioLogueado.nombre, apellido = usuarioLogueado.apellido, direccion = direccion, ciudad = ciudad, correo = correo, telefono = telefono),
                                    onSuccess = {
                                        Toast.makeText(context, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show()
                                        vistaActual = "perfil_menu"
                                    }
                                )
                            }
                        }, modifier = Modifier.fillMaxWidth().height(50.dp)) { Text("GUARDAR CAMBIOS") }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    if (mostrarDialogoDatos && usuario != null) {
        val usuarioDatos = usuario!!
        AlertDialog(
            onDismissRequest = { mostrarDialogoDatos = false },
            title = { Text(text = "Mis Datos Personales", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
            text = { Column(modifier = Modifier.fillMaxWidth()) { Divider(); Spacer(modifier = Modifier.height(8.dp)); ItemDato("Nombre:", "${usuarioDatos.nombre} ${usuarioDatos.apellido}"); ItemDato("Dirección:", usuarioDatos.direccion); ItemDato("Ciudad:", usuarioDatos.ciudad); ItemDato("Correo:", usuarioDatos.correo); ItemDato("Teléfono:", usuarioDatos.telefono); Spacer(modifier = Modifier.height(8.dp)); Divider() } },
            confirmButton = { Button(onClick = { mostrarDialogoDatos = false }, modifier = Modifier.fillMaxWidth()) { Text("Cerrar") } }
        )
    }

    if (mostrarDialogoEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminar = false },
            title = { Text("Eliminar cuenta") },
            text = { Text("¿Está seguro de eliminar usuario?") },
            confirmButton = { TextButton(onClick = { viewModel.eliminarUsuarioActual(); mostrarDialogoEliminar = false; vistaActual = "menu" }) { Text("Sí, eliminar", color = Color.Red) } },
            dismissButton = { TextButton(onClick = { mostrarDialogoEliminar = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
fun ItemDato(titulo: String, valor: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(titulo, fontWeight = FontWeight.SemiBold, color = Color.Gray)
        Text(valor, fontWeight = FontWeight.Normal, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
    }
}