package pe.upeu.agrosierra.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import pe.upeu.agrosierra.data.local.AlmacenSeguro
import pe.upeu.agrosierra.data.local.tokenSigueVigente
import pe.upeu.agrosierra.data.remote.AgroApi
import pe.upeu.agrosierra.data.remote.TokenProvider
import pe.upeu.agrosierra.data.remote.comoErrorApi
import pe.upeu.agrosierra.data.remote.dto.aDominio
import pe.upeu.agrosierra.domain.model.Sesion
import pe.upeu.agrosierra.domain.repository.SesionRepository

class SesionRepositoryImpl(private val api: AgroApi, private val almacen: AlmacenSeguro, private val tokenProvider: TokenProvider) : SesionRepository {
    private val json = Json
    private val estado = MutableStateFlow<Sesion?>(null)
    override val sesion = estado.asStateFlow()

    override suspend fun iniciar(usuario: String, contrasena: String): Sesion = try {
        api.login(usuario.trim(), contrasena).aDominio().also(::persistir)
    } catch (error: Throwable) { throw error.comoErrorApi() }

    override fun restaurar(): Sesion? {
        val raw = almacen.leer(CLAVE) ?: return null
        val sesion = runCatching { json.decodeFromString<SesionGuardada>(raw).aDominio() }.getOrNull()
        if (sesion == null || !tokenSigueVigente(sesion.token)) { cerrar(); return null }
        estado.value = sesion
        tokenProvider.token = sesion.token
        return sesion
    }

    override fun cerrar() { almacen.eliminar(CLAVE); estado.value = null; tokenProvider.token = null }

    private fun persistir(sesion: Sesion) {
        almacen.guardar(CLAVE, json.encodeToString(SesionGuardada.desde(sesion)))
        estado.value = sesion
        tokenProvider.token = sesion.token
    }

    private companion object { const val CLAVE = "sesion" }
}

@Serializable
private data class SesionGuardada(val token: String, val usuario: String, val rol: String, val privilegios: Set<String>, val nombre: String) {
    fun aDominio() = Sesion(token, usuario, rol, privilegios, nombre)
    companion object { fun desde(s: Sesion) = SesionGuardada(s.token, s.usuario, s.rol, s.privilegios, s.nombre) }
}
