package com.example.prueba_app.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.navigation.NavBackStackEntry

object TransicionesApp {
    // Definimos las rutas principales para usar una transición diferente (Fade) entre ellas
    private val rutasPrincipales = setOf("home", "carrito", "usuario")
    private const val DURACION = 400 // Duración en milisegundos

    //Entrada normal (Hacia adelante)
    fun enter(scope: AnimatedContentTransitionScope<NavBackStackEntry>): EnterTransition {
        val from = scope.initialState.destination.route
        val to = scope.targetState.destination.route

        //Si navegamos entre pestañas principales, solo hacemos Fade
        if (esNavegacionPrincipal(from, to)) {
            return fadeIn(animationSpec = tween(DURACION))
        }

        //Si vamos a un detalle, deslizamos desde la derecha
        return slideInHorizontally(
            initialOffsetX = { it }, // Desde la derecha completa
            animationSpec = tween(DURACION, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(DURACION))
    }

    //Salida normal (La pantalla que se va)
    fun exit(scope: AnimatedContentTransitionScope<NavBackStackEntry>): ExitTransition {
        val from = scope.initialState.destination.route
        val to = scope.targetState.destination.route

        if (esNavegacionPrincipal(from, to)) {
            return fadeOut(animationSpec = tween(DURACION))
        }

        //La pantalla se va hacia la izquierda un poco más lento
        return slideOutHorizontally(
            targetOffsetX = { -it / 3 },
            animationSpec = tween(DURACION, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(DURACION))
    }

    //Entrada al volver (Botón atrás)
    fun popEnter(scope: AnimatedContentTransitionScope<NavBackStackEntry>): EnterTransition {
        // Al volver, la pantalla entra desde la izquierda
        return slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = tween(DURACION, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(DURACION))
    }

    //Salida al volver (La pantalla actual se va)
    fun popExit(scope: AnimatedContentTransitionScope<NavBackStackEntry>): ExitTransition {
        // Al volver, la pantalla actual se va hacia la derecha
        return slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(DURACION, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(DURACION))
    }

    private fun esNavegacionPrincipal(from: String?, to: String?): Boolean {
        // Ignoramos splash para que tenga su propia entrada
        if (from == "splash") return false
        return (from in rutasPrincipales && to in rutasPrincipales)
    }
}