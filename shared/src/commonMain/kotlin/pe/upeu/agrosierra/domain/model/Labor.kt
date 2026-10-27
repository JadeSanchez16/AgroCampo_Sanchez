package pe.upeu.agrosierra.domain.model

data class Labor(
    val id: Long,
    val loteId: Long,
    val tipo: TipoLabor,
    val fecha: String,
    val responsable: String,
    val cantidad: Double,
    val observacion: String,
    val pendiente: Boolean,
    val cantidadJornales: Int = 0,
    val creadoEn: String? = null,
    val operacionPendiente: OperacionPendiente? = if (pendiente) OperacionPendiente.CREAR else null,
)

enum class TipoLabor { RIEGO, FUMIGACION, COSECHA }

enum class OperacionPendiente { CREAR, ACTUALIZAR }
