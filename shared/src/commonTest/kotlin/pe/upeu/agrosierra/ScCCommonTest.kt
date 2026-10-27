package pe.upeu.agrosierra

import kotlinx.serialization.json.Json
import pe.upeu.agrosierra.data.remote.dto.LaborDto
import pe.upeu.agrosierra.data.remote.dto.aDominio
import kotlin.test.Test
import kotlin.test.assertEquals

class ScCCommonTest {
    @Test
    fun conservaCantidadDeJornalesDesdeLaRespuestaDelBackend() {
        val dto = Json.decodeFromString<LaborDto>(
            """{"id":91,"loteId":4,"tipo":"COSECHA","fecha":"2026-10-27","responsable":"tecnico1","cantidad":12.5,"cantidadJornales":7,"observacion":"Cosecha manual"}""",
        )

        assertEquals(7, dto.aDominio().cantidadJornales)
    }
}
