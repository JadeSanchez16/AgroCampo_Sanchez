package pe.upeu.agrosierra

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.ktor.client.engine.mock.MockEngine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import pe.upeu.agrosierra.data.local.AgroDb
import pe.upeu.agrosierra.data.local.BaseLocal
import pe.upeu.agrosierra.data.remote.AgroApi
import pe.upeu.agrosierra.data.remote.crearClienteHttp
import pe.upeu.agrosierra.data.repository.LoteRepositoryImpl
import kotlin.test.Test
import kotlin.test.assertEquals

class ScACommonTest {
    @Test
    fun combinaValleConSoloCampaniasAbiertas() = runTest {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        AgroDb.Schema.create(driver)
        val base = BaseLocal(driver)
        base.consultas.guardarLote(1, "MAN-001", "Abierto Mantaro", 2.0, "Papa", "Mantaro", 0, 1.5)
        base.consultas.guardarLote(2, "MAN-002", "Cerrado Mantaro", 3.0, "Maiz", "Mantaro", 1, 1.5)
        base.consultas.guardarLote(3, "URU-001", "Abierto Urubamba", 1.0, "Quinua", "Urubamba", 0, 1.5)
        val api = AgroApi(crearClienteHttp(MockEngine { error("La consulta local no debe invocar la red") }) { null })

        val resultado = LoteRepositoryImpl(base, api).observar("", "Mantaro", soloAbiertas = true).first()

        assertEquals(listOf("MAN-001"), resultado.map { it.lote.codigo })
    }
}
