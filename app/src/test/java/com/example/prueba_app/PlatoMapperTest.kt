package com.example.prueba_app

import com.example.prueba_app.network.PlatoApi
import com.example.prueba_app.network.toEntity
import org.junit.Test
import org.junit.Assert.assertEquals

class PlatoMapperTest {

    @Test
    fun `PlatoApi toEntity mapea nombre e imagenUrl`() {
        val dto = PlatoApi(
            id = 1,
            nombre = "Ceviche",
            imagenUrl = "https://img.com/ceviche.jpg"
        )

        val entity = dto.toEntity()

        assertEquals(1, entity.id)
        assertEquals("Ceviche", entity.nombre)
        assertEquals("https://img.com/ceviche.jpg", entity.imagenUrl)
    }
}
