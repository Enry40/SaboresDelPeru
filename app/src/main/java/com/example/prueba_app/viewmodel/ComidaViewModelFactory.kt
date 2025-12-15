package com.example.prueba_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.prueba_app.repository.ComidaDataSource

class ComidaViewModelFactory(
    private val repositorio: ComidaDataSource
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ComidaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ComidaViewModel(repositorio) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
