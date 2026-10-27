package pe.upeu.agrosierra.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PaginaDto<T>(
    val contenido: List<T>,
    val pagina: Int,
    val tamanio: Int,
    val totalElementos: Long,
    val totalPaginas: Int,
    val ultima: Boolean,
)

@Serializable
data class ErrorDto(
    val timestamp: String? = null,
    val status: Int,
    val error: String = "",
    val message: String = "Error del servicio",
    val path: String = "",
    val validationErrors: Map<String, String>? = null,
)
