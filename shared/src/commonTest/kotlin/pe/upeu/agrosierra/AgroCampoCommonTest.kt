package pe.upeu.agrosierra

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.*
import pe.upeu.agrosierra.data.local.AgroDb
import pe.upeu.agrosierra.data.local.BaseLocal
import pe.upeu.agrosierra.data.remote.*
import pe.upeu.agrosierra.data.remote.dto.LoteDto
import pe.upeu.agrosierra.data.remote.dto.aDominio
import pe.upeu.agrosierra.data.repository.LaborRepositoryImpl
import pe.upeu.agrosierra.data.repository.LoteRepositoryImpl
import pe.upeu.agrosierra.domain.model.*

class AgroCampoCommonTest {
    @Test fun convierteDtoAlDominio() {
        val lote = LoteDto(3, "LOT-003", "Quebrada Alta", 3.2, "Quinua", "Urubamba", false, 1.5).aDominio()
        assertEquals("Quebrada Alta", lote.nombre); assertFalse(lote.campaniaCerrada); assertEquals(1.5, lote.dosisMaximaPorHectarea)
    }
    @Test fun traduce403ASinPrivilegio() = runTest {
        val engine = MockEngine { respond("""{"status":403,"message":"Operación prohibida"}""", HttpStatusCode.Forbidden, jsonHeaders) }
        assertFailsWith<ErrorApi.SinPrivilegio> { AgroApi(crearClienteHttp(engine) { "token" }).lotes() }
    }
    @Test fun traduce400YConservaErroresDeCampo() = runTest {
        val engine = MockEngine { respond("""{"status":400,"message":"Datos inválidos","validationErrors":{"cantidad":"Debe ser mayor que cero"}}""", HttpStatusCode.BadRequest, jsonHeaders) }
        val error = assertFailsWith<ErrorApi.Validacion> { AgroApi(crearClienteHttp(engine) { "token" }).lotes() }
        assertEquals("Debe ser mayor que cero", error.campos["cantidad"])
    }
    @Test fun traduce401ASesionVencida() = runTest {
        val engine = MockEngine { respond("""{"status":401,"message":"Token vencido"}""", HttpStatusCode.Unauthorized, jsonHeaders) }
        assertFailsWith<ErrorApi.NoAutorizado> { AgroApi(crearClienteHttp(engine) { "token" }).lotes() }
    }
    @Test fun traduce409AConflicto() = runTest {
        val engine = MockEngine { respond("""{"status":409,"message":"La campaña está cerrada"}""", HttpStatusCode.Conflict, jsonHeaders) }
        assertFailsWith<ErrorApi.Conflicto> { AgroApi(crearClienteHttp(engine) { "token" }).lotes() }
    }
    @Test fun registroSinConexionUsaIdNegativoYPendiente() = runTest {
        val base = base(); base.consultas.guardarLote(1, "L-1", "Lote", 1.0, "Papa", "Mantaro", 0, 2.0)
        val api = AgroApi(crearClienteHttp(MockEngine { respondError(HttpStatusCode.ServiceUnavailable) }) { "token" })
        val repo = LaborRepositoryImpl(base, api, LoteRepositoryImpl(base, api)) { "tecnico1" }
        val guardada = repo.guardar(Labor(0, 1, TipoLabor.RIEGO, "2026-10-27", "", 2.0, "Prueba offline", false, 1))
        assertTrue(guardada.id < 0); assertTrue(guardada.pendiente); assertEquals(guardada.id, repo.observarPorLote(1).first().single().id)
    }
    @Test fun sincronizacionSubeAntesDeDescargar() = runTest {
        val orden = mutableListOf<String>()
        val engine = MockEngine { request -> val ruta = request.url.encodedPath; orden += "${request.method.value} $ruta"; when {
            request.method == HttpMethod.Post && ruta.endsWith("/labores") -> respond("""{"id":21,"loteId":1,"tipo":"RIEGO","fecha":"2026-10-27","responsable":"tecnico1","cantidad":2.0,"cantidadJornales":1,"observacion":"Offline","creadoEn":"2026-10-27T15:00:00Z"}""", HttpStatusCode.Created, jsonHeaders)
            ruta.endsWith("/lotes") -> respond("""{"contenido":[],"pagina":0,"tamanio":100,"totalElementos":0,"totalPaginas":0,"ultima":true}""", HttpStatusCode.OK, jsonHeaders)
            else -> respond("{}", HttpStatusCode.OK, jsonHeaders) } }
        val base = base(); base.consultas.guardarLote(1, "L-1", "Lote", 1.0, "Papa", "Mantaro", 0, 2.0); base.consultas.guardarLabor(-1, 1, "RIEGO", "2026-10-27", "tecnico1", 2.0, 1, "Offline", 1, "CREAR", null)
        val api = AgroApi(crearClienteHttp(engine) { "token" }); val resumen = LaborRepositoryImpl(base, api, LoteRepositoryImpl(base, api)) { "tecnico1" }.sincronizar()
        assertEquals(1, resumen.enviados); assertTrue(orden.indexOfFirst { it.startsWith("POST") } < orden.indexOfFirst { it.startsWith("GET") })
    }
    private fun base(): BaseLocal { val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY); AgroDb.Schema.create(driver); return BaseLocal(driver) }
    private companion object { val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json") }
}
