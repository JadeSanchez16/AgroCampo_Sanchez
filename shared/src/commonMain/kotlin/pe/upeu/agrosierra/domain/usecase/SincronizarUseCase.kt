package pe.upeu.agrosierra.domain.usecase

import pe.upeu.agrosierra.domain.repository.LaborRepository
import pe.upeu.agrosierra.domain.repository.ResumenSincronizacion

class SincronizarUseCase(private val repositorio: LaborRepository) {
    suspend operator fun invoke(): ResumenSincronizacion = repositorio.sincronizar()
}
