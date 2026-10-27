package pe.upeu.agrosierra.data.remote.dto

import kotlinx.serialization.Serializable
import pe.upeu.agrosierra.domain.model.Labor
import pe.upeu.agrosierra.domain.model.TipoLabor

@Serializable
data class LaborRequestDto(
    val loteId: Long,
    val tipo: TipoLabor,
    val fecha: String,
    val cantidad: Double,
    val cantidadJornales: Int,
    val observacion: String,
)

@Serializable
data class LaborDto(
    val id: Long,
    val loteId: Long,
    val tipo: TipoLabor,
    val fecha: String,
    val responsable: String,
    val cantidad: Double,
    val cantidadJornales: Int = 0,
    val observacion: String,
    val creadoEn: String? = null,
)

fun LaborDto.aDominio() = Labor(id, loteId, tipo, fecha, responsable, cantidad, observacion, false, cantidadJornales, creadoEn)
fun Labor.aRequest() = LaborRequestDto(loteId, tipo, fecha, cantidad, cantidadJornales, observacion)
