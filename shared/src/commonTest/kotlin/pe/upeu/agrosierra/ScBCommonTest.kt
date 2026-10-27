package pe.upeu.agrosierra

import pe.upeu.agrosierra.domain.model.Sesion
import pe.upeu.agrosierra.domain.model.puede
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ScBCommonTest {
    @Test
    fun supervisorMantieneGestionDeUsuariosSinHeredarCierreDeCampania() {
        val supervisor = Sesion(
            token = "jwt",
            usuario = "supervisor1",
            rol = "SUPERVISOR",
            privilegios = setOf("USUARIO_GESTIONAR", "LOTE_VER", "LABOR_VER"),
        )

        assertTrue(supervisor.puede("USUARIO_GESTIONAR"))
        assertFalse(supervisor.puede("CAMPANIA_CERRAR"))
    }
}
