package pe.upeu.agrosierra.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import pe.upeu.agrosierra.data.remote.dto.*

class AgroApi(private val client: HttpClient) {
    suspend fun login(usuario: String, contrasena: String): LoginResponseDto =
        client.post("auth/login") { contentType(ContentType.Application.Json); setBody(LoginRequestDto(usuario, contrasena)) }.exigir().body()

    suspend fun lotes(pagina: Int = 0, tamanio: Int = 100, valle: String? = null, abiertas: Boolean? = null): PaginaDto<LoteDto> =
        client.get("lotes") {
            parameter("pagina", pagina); parameter("tamanio", tamanio)
            valle?.let { parameter("valle", it) }; abiertas?.let { parameter("campaniaCerrada", !it) }
        }.exigir().body()

    suspend fun lote(id: Long): LoteDto = client.get("lotes/$id").exigir().body()
    suspend fun labores(loteId: Long, pagina: Int = 0, tamanio: Int = 100): PaginaDto<LaborDto> =
        client.get("lotes/$loteId/labores") { parameter("pagina", pagina); parameter("tamanio", tamanio) }.exigir().body()
    suspend fun crear(labor: LaborRequestDto): LaborDto = client.post("labores") { contentType(ContentType.Application.Json); setBody(labor) }.exigir(HttpStatusCode.Created).body()
    suspend fun actualizar(id: Long, labor: LaborRequestDto): LaborDto = client.put("labores/$id") { contentType(ContentType.Application.Json); setBody(labor) }.exigir().body()
    suspend fun eliminar(id: Long) { client.delete("labores/$id").exigir(HttpStatusCode.NoContent) }
    suspend fun cerrarCampania(id: Long): LoteDto = client.post("lotes/$id/cerrar-campania").exigir().body()
    suspend fun usuarioActual(): UsuarioActualDto = client.get("usuarios/me").exigir().body()
}

private suspend fun io.ktor.client.statement.HttpResponse.exigir(esperado: HttpStatusCode? = null): io.ktor.client.statement.HttpResponse {
    if ((esperado != null && status != esperado) || (esperado == null && !status.isSuccess())) throw comoError()
    return this
}
