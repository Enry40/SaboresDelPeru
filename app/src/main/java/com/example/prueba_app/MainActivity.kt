package com.example.prueba_app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.example.prueba_app.model.AppDatabase          // 👈 IMPORT CORRECTO
import com.example.prueba_app.repository.RepositorioComida
import com.example.prueba_app.ui.theme.Prueba_appTheme
import com.example.prueba_app.ui.theme.AppComidaPeruana
import com.example.prueba_app.viewmodel.ComidaViewModel
import com.example.prueba_app.viewmodel.ComidaViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Base de datos REAL
        val database = AppDatabase.getDatabase(this)

        // 2. Repositorio REAL
        val repositorio = RepositorioComida(db = database)

        // 3. ViewModel + Factory
        val viewModelFactory = ComidaViewModelFactory(repositorio)
        val viewModel = ViewModelProvider(
            this,
            viewModelFactory
        )[ComidaViewModel::class.java]

        // 4. Contenido de la app
        setContent {
            Prueba_appTheme {
                AppComidaPeruana(
                    viewModel = viewModel,
                    onExitApp = {
                        Toast.makeText(
                            this,
                            "Gracias por visitarnos",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                )
            }
        }
    }
}
