package pe.upeu.agrosierra

import pe.upeu.agrosierra.domain.model.Sesion
import pe.upeu.agrosierra.domain.model.puede
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ScBCommonTest {
    @Test
    fun supervisorCalidadEditaLaboresSinHeredarCierreDeCampania() {
        val supervisor = Sesion(
            token = "jwt",
            usuario = "supervisor.calidad",
            rol = "SUPERVISOR_CALIDAD",
            privilegios = setOf("LOTE_LEER", "LABOR_LEER", "LABOR_EDITAR"),
        )

        assertTrue(supervisor.puede("LABOR_EDITAR"))
        assertFalse(supervisor.puede("CAMPANIA_CERRAR"))
        assertFalse(supervisor.puede("LABOR_CREAR"))
        assertFalse(supervisor.puede("LABOR_ELIMINAR"))
        assertFalse(supervisor.puede("USUARIO_GESTIONAR"))
    }
}
