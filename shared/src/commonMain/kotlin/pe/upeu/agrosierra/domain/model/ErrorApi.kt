package pe.upeu.agrosierra.domain.model

sealed class ErrorApi(override val message: String) : Exception(message) {
    data class Validacion(val campos: Map<String, String>, val detalle: String) : ErrorApi(detalle)
    data class NoAutorizado(val detalle: String = "La sesión venció") : ErrorApi(detalle)
    data class SinPrivilegio(val detalle: String = "No tienes permiso para esta operación") : ErrorApi(detalle)
    data class NoEncontrado(val detalle: String) : ErrorApi(detalle)
    data class Conflicto(val detalle: String) : ErrorApi(detalle)
    data class Servidor(val detalle: String) : ErrorApi(detalle)
    data class SinConexion(val detalle: String = "No hay conexión con el servicio") : ErrorApi(detalle)
    data class TiempoAgotado(val detalle: String = "El servicio tardó demasiado en responder") : ErrorApi(detalle)
    data class Desconocido(val detalle: String) : ErrorApi(detalle)
}
