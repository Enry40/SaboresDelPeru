package com.example.prueba_app

import com.example.prueba_app.network.PlatoApi
import com.example.prueba_app.network.toEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class RepositorioComidaTest {

    @Test
    fun `PlatoApi toEntity mapea nombre e imagen`() {
        val dto = PlatoApi(id = 10, nombre = "Ceviche", imagenUrl = "https://img.com/ceviche.jpg")
        val entity = dto.toEntity()

        assertEquals(10, entity.id)
        assertEquals("Ceviche", entity.nombre)
        assertEquals("https://img.com/ceviche.jpg", entity.imagenUrl)
    }
}
