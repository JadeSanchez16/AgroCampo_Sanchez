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

class ScDCommonTest {
    @Test
    fun cuentaPendientesPorLoteSinIncluirLaboresSincronizadas() = runTest {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        AgroDb.Schema.create(driver)
        val base = BaseLocal(driver)
        base.consultas.guardarLote(1, "LOT-001", "Lote con cola", 2.0, "Papa", "Mantaro", 0, 1.5)
        base.consultas.guardarLabor(-1, 1, "RIEGO", "2026-10-27", "tecnico1", 1.0, 1, "Pendiente", 1, "CREAR", null)
        base.consultas.guardarLabor(8, 1, "COSECHA", "2026-10-26", "tecnico1", 3.0, 4, "Sincronizada", 0, null, null)
        val api = AgroApi(crearClienteHttp(MockEngine { error("La consulta local no debe invocar la red") }) { null })

        val lote = LoteRepositoryImpl(base, api).observar("", "", soloAbiertas = false).first().single()

        assertEquals(1L, lote.pendientes)
    }
}
