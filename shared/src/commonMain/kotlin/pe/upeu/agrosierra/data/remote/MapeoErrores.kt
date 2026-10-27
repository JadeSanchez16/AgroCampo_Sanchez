package pe.upeu.agrosierra.data.remote

import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.errors.IOException
import pe.upeu.agrosierra.data.remote.dto.ErrorDto
import pe.upeu.agrosierra.domain.model.ErrorApi

suspend fun HttpResponse.comoError(): ErrorApi {
    val error = runCatching { body<ErrorDto>() }.getOrNull()
    val detalle = error?.message ?: "Error HTTP ${status.value}"
    return when (status) {
        HttpStatusCode.BadRequest -> ErrorApi.Validacion(error?.validationErrors.orEmpty(), detalle)
        HttpStatusCode.Unauthorized -> ErrorApi.NoAutorizado(detalle)
        HttpStatusCode.Forbidden -> ErrorApi.SinPrivilegio(detalle)
        HttpStatusCode.NotFound -> ErrorApi.NoEncontrado(detalle)
        HttpStatusCode.Conflict -> ErrorApi.Conflicto(detalle)
        else -> ErrorApi.Servidor(detalle)
    }
}

fun Throwable.comoErrorApi(): ErrorApi = when (this) {
    is ErrorApi -> this
    is ConnectTimeoutException, is SocketTimeoutException -> ErrorApi.TiempoAgotado()
    is IOException -> ErrorApi.SinConexion()
    else -> ErrorApi.Desconocido(message ?: "Error inesperado")
}
