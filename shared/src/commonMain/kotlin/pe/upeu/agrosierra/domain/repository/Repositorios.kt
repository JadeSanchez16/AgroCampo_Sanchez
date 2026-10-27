package pe.upeu.agrosierra.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.upeu.agrosierra.domain.model.Labor
import pe.upeu.agrosierra.domain.model.Lote
import pe.upeu.agrosierra.domain.model.Sesion

data class LoteConPendientes(val lote: Lote, val pendientes: Long)
data class ResumenSincronizacion(val enviados: Int, val recibidos: Int, val errores: List<String> = emptyList())

interface SesionRepository {
    val sesion: Flow<Sesion?>
    suspend fun iniciar(usuario: String, contrasena: String): Sesion
    fun restaurar(): Sesion?
    fun cerrar()
}

interface LoteRepository {
    fun observar(texto: String = "", valle: String = "", soloAbiertas: Boolean = false): Flow<List<LoteConPendientes>>
    fun observarUno(id: Long): Flow<Lote?>
    suspend fun cerrarCampania(id: Long): Lote
    fun ultimaSincronizacion(): String?
}

interface LaborRepository {
    fun observarPorLote(loteId: Long): Flow<List<Labor>>
    suspend fun guardar(labor: Labor): Labor
    suspend fun eliminar(id: Long)
    suspend fun sincronizar(): ResumenSincronizacion
}
