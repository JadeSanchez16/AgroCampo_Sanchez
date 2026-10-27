package pe.upeu.agrosierra.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.upeu.agrosierra.data.local.BaseLocal
import pe.upeu.agrosierra.data.remote.AgroApi
import pe.upeu.agrosierra.data.remote.comoErrorApi
import pe.upeu.agrosierra.data.remote.dto.aDominio
import pe.upeu.agrosierra.data.remote.dto.aRequest
import pe.upeu.agrosierra.domain.model.Labor
import pe.upeu.agrosierra.domain.model.OperacionPendiente
import pe.upeu.agrosierra.domain.model.TipoLabor
import pe.upeu.agrosierra.domain.repository.LaborRepository
import pe.upeu.agrosierra.domain.repository.ResumenSincronizacion

class LaborRepositoryImpl(
    private val local: BaseLocal,
    private val api: AgroApi,
    private val lotes: LoteRepositoryImpl,
    private val usuarioActual: () -> String,
) : LaborRepository {
    override fun observarPorLote(loteId: Long): Flow<List<Labor>> =
        local.consultas.observarLaboresPorLote(loteId).asFlow().mapToList(Dispatchers.Default).map { filas -> filas.map(::aDominio) }

    override suspend fun guardar(labor: Labor): Labor {
        val existente = labor.id > 0
        val preparada = if (labor.id == 0L) labor.copy(
            id = minOf(local.consultas.siguienteIdLocal().executeAsOne(), 0L) - 1,
            responsable = usuarioActual(), pendiente = true, operacionPendiente = OperacionPendiente.CREAR,
        ) else labor.copy(pendiente = true, operacionPendiente = if (existente) OperacionPendiente.ACTUALIZAR else OperacionPendiente.CREAR)
        guardarLocal(preparada)
        return try {
            val remota = if (existente) api.actualizar(preparada.id, preparada.aRequest()) else api.crear(preparada.aRequest())
            val definitiva = remota.aDominio()
            local.db.transaction { if (!existente) local.consultas.eliminarLabor(preparada.id); guardarLocal(definitiva) }
            definitiva
        } catch (_: Throwable) { preparada }
    }

    override suspend fun eliminar(id: Long) {
        if (id < 0) local.consultas.eliminarLabor(id) else { api.eliminar(id); local.consultas.eliminarLabor(id) }
    }

    override suspend fun sincronizar(): ResumenSincronizacion {
        var enviados = 0; var recibidos = 0; val errores = mutableListOf<String>()
        local.consultas.seleccionarPendientes().executeAsList().forEach { fila ->
            val labor = aDominio(fila)
            try {
                val remota = if (labor.id < 0 || labor.operacionPendiente == OperacionPendiente.CREAR) api.crear(labor.aRequest()) else api.actualizar(labor.id, labor.aRequest())
                local.db.transaction { local.consultas.eliminarLabor(labor.id); guardarLocal(remota.aDominio()) }
                enviados++
            } catch (e: Throwable) { errores += e.comoErrorApi().message }
        }
        try {
            val pagina = api.lotes()
            pagina.contenido.forEach { dto ->
                val lote = dto.aDominio(); lotes.guardar(lote)
                val remotas = api.labores(lote.id).contenido.map { it.aDominio() }
                local.db.transaction {
                    local.consultas.eliminarLaboresSinPendientesPorLote(lote.id)
                    remotas.forEach(::guardarLocal)
                }
                recibidos += remotas.size
            }
            local.consultas.guardarMetadato("ultima_sincronizacion", "${kotlin.time.Clock.System.now()}")
        } catch (e: Throwable) { errores += e.comoErrorApi().message }
        return ResumenSincronizacion(enviados, recibidos, errores)
    }

    private fun guardarLocal(l: Labor) = local.consultas.guardarLabor(l.id, l.loteId, l.tipo.name, l.fecha, l.responsable, l.cantidad, l.cantidadJornales.toLong(), l.observacion, if (l.pendiente) 1 else 0, l.operacionPendiente?.name, l.creadoEn)
    private fun aDominio(f: pe.upeu.agrosierra.Labor) = Labor(f.id, f.loteId, TipoLabor.valueOf(f.tipo), f.fecha, f.responsable, f.cantidad, f.observacion, f.pendiente != 0L, f.cantidadJornales.toInt(), f.creadoEn, f.operacionPendiente?.let(OperacionPendiente::valueOf))
}
