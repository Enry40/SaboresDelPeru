package com.example.prueba_app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.prueba_app.model.AppDatabase
import com.example.prueba_app.repository.RepositorioComida
import com.example.prueba_app.ui.theme.AppComidaPeruana
import com.example.prueba_app.viewmodel.ComidaViewModel
import com.example.prueba_app.viewmodel.ComidaViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. INSTANCIA BASE DE DATOS Y REPOSITORIO
        val database = AppDatabase.getDatabase(this)
        val repositorio = RepositorioComida(database)

        // 2. VIEWMODEL FACTORY
        val viewModelFactory = ComidaViewModelFactory(repositorio)
        val viewModel = ViewModelProvider(this, viewModelFactory)[ComidaViewModel::class.java]

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppComidaPeruana(
                        viewModel = viewModel,
                        onExitApp = {
                            Toast.makeText(this, "Gracias por visitarnos", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    )
                }
            }
        }
    }
}