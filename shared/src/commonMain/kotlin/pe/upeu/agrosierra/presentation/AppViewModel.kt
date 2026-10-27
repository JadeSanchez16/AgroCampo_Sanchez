package pe.upeu.agrosierra.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pe.upeu.agrosierra.domain.model.*
import pe.upeu.agrosierra.domain.repository.*

enum class Pantalla { LOGIN, LOTES, DETALLE, PERFIL, FORMULARIO }
data class FiltrosLote(val texto: String = "", val valle: String = "", val soloAbiertas: Boolean = false)
data class FormularioLabor(val id: Long = 0, val loteId: Long = 0, val tipo: TipoLabor = TipoLabor.RIEGO, val fecha: String = "", val cantidad: String = "", val cantidadJornales: String = "0", val observacion: String = "", val errores: Map<String, String> = emptyMap())
data class EstadoApp(val pantalla: Pantalla = Pantalla.LOGIN, val sesion: Sesion? = null, val cargando: Boolean = false, val lotes: List<LoteConPendientes> = emptyList(), val loteSeleccionado: Lote? = null, val labores: List<Labor> = emptyList(), val filtros: FiltrosLote = FiltrosLote(), val formulario: FormularioLabor = FormularioLabor(), val mensaje: String? = null, val ultimaSincronizacion: String? = null)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AppViewModel(private val sesiones: SesionRepository, private val lotesRepo: LoteRepository, private val laboresRepo: LaborRepository) : ViewModel() {
    private val mutable = MutableStateFlow(EstadoApp())
    val estado = mutable.asStateFlow()
    private val filtros = MutableStateFlow(FiltrosLote())
    private var detalleJob: Job? = null
    init {
        val sesion = sesiones.restaurar()
        mutable.update { it.copy(sesion = sesion, pantalla = if (sesion == null) Pantalla.LOGIN else Pantalla.LOTES) }
        viewModelScope.launch { filtros.flatMapLatest { lotesRepo.observar(it.texto, it.valle, it.soloAbiertas) }.collect { lista -> mutable.update { it.copy(lotes = lista, filtros = filtros.value, ultimaSincronizacion = lotesRepo.ultimaSincronizacion()) } } }
        if (sesion != null) sincronizar()
    }
    fun iniciarSesion(usuario: String, contrasena: String) = viewModelScope.launch {
        mutable.update { it.copy(cargando = true, mensaje = null) }
        runCatching { sesiones.iniciar(usuario, contrasena) }.onSuccess { s -> mutable.update { it.copy(cargando = false, sesion = s, pantalla = Pantalla.LOTES) }; sincronizar() }.onFailure(::mostrarError)
    }
    fun cerrarSesion() { sesiones.cerrar(); mutable.value = EstadoApp() }
    fun irA(pantalla: Pantalla) = mutable.update { it.copy(pantalla = pantalla, mensaje = null) }
    fun filtrar(texto: String = filtros.value.texto, valle: String = filtros.value.valle, soloAbiertas: Boolean = filtros.value.soloAbiertas) { filtros.value = FiltrosLote(texto, valle, soloAbiertas) }
    fun abrirLote(id: Long) {
        detalleJob?.cancel(); mutable.update { it.copy(pantalla = Pantalla.DETALLE) }
        detalleJob = viewModelScope.launch { combine(lotesRepo.observarUno(id), laboresRepo.observarPorLote(id)) { lote, labores -> lote to labores }.collect { (lote, labores) -> mutable.update { it.copy(loteSeleccionado = lote, labores = labores) } } }
    }
    fun nuevoRegistro() { estado.value.loteSeleccionado?.let { lote -> mutable.update { it.copy(pantalla = Pantalla.FORMULARIO, formulario = FormularioLabor(loteId = lote.id)) } } }
    fun editar(l: Labor) = mutable.update { it.copy(pantalla = Pantalla.FORMULARIO, formulario = FormularioLabor(l.id, l.loteId, l.tipo, l.fecha, l.cantidad.toString(), l.cantidadJornales.toString(), l.observacion)) }
    fun actualizarFormulario(f: FormularioLabor) = mutable.update { it.copy(formulario = f.copy(errores = emptyMap())) }
    fun guardarLabor() = viewModelScope.launch {
        val f = estado.value.formulario
        val errores = buildMap { if (f.fecha.isBlank()) put("fecha", "La fecha es obligatoria"); if ((f.cantidad.toDoubleOrNull() ?: 0.0) <= 0) put("cantidad", "Ingresa una cantidad mayor que cero"); if ((f.cantidadJornales.toIntOrNull() ?: -1) < 0) put("cantidadJornales", "No puede ser negativo"); if (f.observacion.isBlank()) put("observacion", "La observación es obligatoria") }
        if (errores.isNotEmpty()) { mutable.update { it.copy(formulario = f.copy(errores = errores)) }; return@launch }
        val labor = Labor(f.id, f.loteId, f.tipo, f.fecha, estado.value.sesion?.usuario.orEmpty(), f.cantidad.toDouble(), f.observacion, true, f.cantidadJornales.toInt())
        mutable.update { it.copy(cargando = true) }
        runCatching { laboresRepo.guardar(labor) }.onSuccess { guardada -> mutable.update { it.copy(cargando = false, pantalla = Pantalla.DETALLE, mensaje = if (guardada.pendiente) "Guardado localmente; pendiente de sincronización" else "Labor guardada") } }.onFailure(::mostrarError)
    }
    fun eliminar(l: Labor) = viewModelScope.launch { runCatching { laboresRepo.eliminar(l.id) }.onSuccess { mutable.update { it.copy(mensaje = "Labor eliminada") } }.onFailure(::mostrarError) }
    fun cerrarCampania() = viewModelScope.launch { estado.value.loteSeleccionado?.let { lote -> runCatching { lotesRepo.cerrarCampania(lote.id) }.onSuccess { mutable.update { it.copy(mensaje = "Campaña cerrada") } }.onFailure(::mostrarError) } }
    fun sincronizar() = viewModelScope.launch {
        mutable.update { it.copy(cargando = true, mensaje = "Sincronizando…") }; val r = laboresRepo.sincronizar()
        mutable.update { it.copy(cargando = false, mensaje = "Sincronización: ${r.enviados} enviados, ${r.recibidos} recibidos" + if (r.errores.isEmpty()) "" else ". ${r.errores.joinToString()}", ultimaSincronizacion = lotesRepo.ultimaSincronizacion()) }
    }
    fun limpiarMensaje() = mutable.update { it.copy(mensaje = null) }
    private fun mostrarError(error: Throwable) = mutable.update { actual -> val campos = (error as? ErrorApi.Validacion)?.campos; actual.copy(cargando = false, mensaje = error.message, formulario = if (campos == null) actual.formulario else actual.formulario.copy(errores = campos)) }
}
