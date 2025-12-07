package com.example.prueba_app.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.navigation.NavBackStackEntry

object TransicionesApp {

    //Camara a las rutas principales
    private val rutasPrincipales = setOf("home", "carrito", "usuario", "camara")
    private const val DURACION = 400

    // Entrada normal (Hacia adelante)
    fun enter(scope: AnimatedContentTransitionScope<NavBackStackEntry>): EnterTransition {
        val from = scope.initialState.destination.route
        val to = scope.targetState.destination.route

        if (esNavegacionPrincipal(from, to)) {
            return fadeIn(animationSpec = tween(DURACION))
        }

        return slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(DURACION, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(DURACION))
    }

    // Salida normal (La pantalla que se va)
    fun exit(scope: AnimatedContentTransitionScope<NavBackStackEntry>): ExitTransition {
        val from = scope.initialState.destination.route
        val to = scope.targetState.destination.route

        if (esNavegacionPrincipal(from, to)) {
            return fadeOut(animationSpec = tween(DURACION))
        }

        return slideOutHorizontally(
            targetOffsetX = { -it / 3 },
            animationSpec = tween(DURACION, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(DURACION))
    }

    // Entrada al volver
    fun popEnter(scope: AnimatedContentTransitionScope<NavBackStackEntry>): EnterTransition {
        return slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = tween(DURACION, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(DURACION))
    }

    // Salida al volver
    fun popExit(scope: AnimatedContentTransitionScope<NavBackStackEntry>): ExitTransition {
        return slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(DURACION, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(DURACION))
    }

    private fun esNavegacionPrincipal(from: String?, to: String?): Boolean {
        if (from == "splash") return false
        return (from in rutasPrincipales && to in rutasPrincipales)
    }
}