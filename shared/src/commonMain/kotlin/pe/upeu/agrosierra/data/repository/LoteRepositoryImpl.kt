package pe.upeu.agrosierra.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.upeu.agrosierra.data.local.BaseLocal
import pe.upeu.agrosierra.data.remote.AgroApi
import pe.upeu.agrosierra.data.remote.dto.aDominio
import pe.upeu.agrosierra.domain.model.Lote
import pe.upeu.agrosierra.domain.repository.LoteConPendientes
import pe.upeu.agrosierra.domain.repository.LoteRepository

class LoteRepositoryImpl(private val local: BaseLocal, private val api: AgroApi) : LoteRepository {
    override fun observar(texto: String, valle: String, soloAbiertas: Boolean): Flow<List<LoteConPendientes>> =
        local.consultas.observarLotes(texto, valle, if (soloAbiertas) 1 else 0).asFlow().mapToList(Dispatchers.Default).map { filas ->
            filas.map { LoteConPendientes(Lote(it.id, it.codigo, it.nombre, it.hectareas, it.cultivo, it.valle, it.campaniaCerrada != 0L, it.dosisMaxima), it.pendientes) }
        }

    override fun observarUno(id: Long): Flow<Lote?> = local.consultas.seleccionarLote(id).asFlow().mapToOneOrNull(Dispatchers.Default).map {
        it?.let { f -> Lote(f.id, f.codigo, f.nombre, f.hectareas, f.cultivo, f.valle, f.campaniaCerrada != 0L, f.dosisMaxima) }
    }

    override suspend fun cerrarCampania(id: Long): Lote = api.cerrarCampania(id).aDominio().also(::guardar)
    override fun ultimaSincronizacion(): String? = local.consultas.leerMetadato("ultima_sincronizacion").executeAsOneOrNull()

    fun guardar(lote: Lote) = local.consultas.guardarLote(lote.id, lote.codigo, lote.nombre, lote.hectareas, lote.cultivo, lote.valle, if (lote.campaniaCerrada) 1 else 0, lote.dosisMaximaPorHectarea)
}
