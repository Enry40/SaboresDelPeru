package com.example.prueba_app.ui.theme

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.prueba_app.viewmodel.ComidaViewModel

@Composable
fun AppComidaPeruana(
    viewModel: ComidaViewModel,
    onExitApp: () -> Unit
) {
    Text("App Comida Peruana OK ✅")
}
