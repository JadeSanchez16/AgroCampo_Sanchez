package pe.upeu.agrosierra

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import pe.upeu.agrosierra.domain.model.*
import pe.upeu.agrosierra.presentation.*

@Composable fun AgroCampoRoot(vm: AppViewModel = koinInject()) {
    val e by vm.estado.collectAsState()
    MaterialTheme { Scaffold(topBar = { if (e.pantalla != Pantalla.LOGIN) Barra(e, vm) }) { p ->
        Box(Modifier.fillMaxSize().padding(p)) {
            when (e.pantalla) { Pantalla.LOGIN -> Login(e, vm); Pantalla.LOTES -> Lotes(e, vm); Pantalla.DETALLE -> Detalle(e, vm); Pantalla.PERFIL -> Perfil(e, vm); Pantalla.FORMULARIO -> Formulario(e, vm) }
            if (e.cargando) CircularProgressIndicator(Modifier.align(Alignment.Center))
            e.mensaje?.let { Surface(Modifier.align(Alignment.BottomCenter).fillMaxWidth(), color = MaterialTheme.colorScheme.inverseSurface) { Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) { Text(it, Modifier.weight(1f), color = MaterialTheme.colorScheme.inverseOnSurface); TextButton(vm::limpiarMensaje) { Text("OK") } } } }
        }
    } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun Barra(e: EstadoApp, vm: AppViewModel) = TopAppBar(title = { Text("AgroCampo") }, navigationIcon = { if (e.pantalla in listOf(Pantalla.DETALLE, Pantalla.PERFIL, Pantalla.FORMULARIO)) TextButton({ vm.irA(if (e.pantalla == Pantalla.FORMULARIO) Pantalla.DETALLE else Pantalla.LOTES) }) { Text("Atrás") } }, actions = { TextButton(vm::sincronizar) { Text("Sincronizar") }; TextButton({ vm.irA(Pantalla.PERFIL) }) { Text("Perfil") } })

@Composable private fun Login(e: EstadoApp, vm: AppViewModel) { var u by remember { mutableStateOf("") }; var c by remember { mutableStateOf("") }; Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) { Text("AgroSierra", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold); Text("Labores de campo offline-first", Modifier.padding(bottom = 24.dp)); OutlinedTextField(u, { u = it }, label = { Text("Usuario") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(c, { c = it }, label = { Text("Contraseña") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth()); Button({ vm.iniciarSesion(u, c) }, enabled = u.isNotBlank() && c.isNotBlank() && !e.cargando, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) { Text("Ingresar") }; e.mensaje?.let { Text(it, color = MaterialTheme.colorScheme.error) } } }

@Composable private fun Lotes(e: EstadoApp, vm: AppViewModel) { Column(Modifier.fillMaxSize().padding(16.dp)) { OutlinedTextField(e.filtros.texto, { vm.filtrar(texto = it) }, label = { Text("Buscar por nombre o código") }, modifier = Modifier.fillMaxWidth()); Row(verticalAlignment = Alignment.CenterVertically) { OutlinedTextField(e.filtros.valle, { vm.filtrar(valle = it) }, label = { Text("Valle") }, modifier = Modifier.weight(1f)); Checkbox(e.filtros.soloAbiertas, { vm.filtrar(soloAbiertas = it) }); Text("Solo abiertas") }; Text("Última sincronización: ${e.ultimaSincronizacion ?: "aún no realizada"}", style = MaterialTheme.typography.bodySmall); if (e.lotes.isEmpty() && !e.cargando) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No hay lotes para mostrar") } else LazyColumn { items(e.lotes, key = { it.lote.id }) { item -> Card({ vm.abrirLote(item.lote.id) }, Modifier.fillMaxWidth().padding(vertical = 5.dp)) { Column(Modifier.padding(14.dp)) { Text("${item.lote.codigo} · ${item.lote.nombre}", fontWeight = FontWeight.Bold); Text("${item.lote.cultivo} · ${item.lote.hectareas} ha · ${item.lote.valle}"); Text(if (item.lote.campaniaCerrada) "Campaña cerrada" else "Campaña abierta"); if (item.pendientes > 0) Text("${item.pendientes} pendiente(s)", color = MaterialTheme.colorScheme.tertiary) } } } } } }

@Composable private fun Detalle(e: EstadoApp, vm: AppViewModel) { val lote = e.loteSeleccionado ?: return; val s = e.sesion ?: return; Column(Modifier.fillMaxSize().padding(16.dp)) { Text(lote.nombre, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text("${lote.codigo} · ${lote.cultivo} · ${lote.hectareas} ha · ${lote.valle}"); Row { if (s.puede("LABOR_CREAR") && !lote.campaniaCerrada) Button(vm::nuevoRegistro) { Text("Registrar labor") }; if (s.puede("CAMPANIA_CERRAR") && !lote.campaniaCerrada) OutlinedButton(vm::cerrarCampania, Modifier.padding(start = 8.dp)) { Text("Cerrar campaña") } }; Text("Labores", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 16.dp)); if (e.labores.isEmpty()) Text("Este lote todavía no tiene labores"); LazyColumn { items(e.labores, key = { it.id }) { l -> Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) { Column(Modifier.padding(12.dp)) { Text("${l.tipo} · ${l.fecha}", fontWeight = FontWeight.Bold); Text("Cantidad: ${l.cantidad} · Jornales: ${l.cantidadJornales}"); Text(l.observacion); Text("Responsable: ${l.responsable}"); if (l.pendiente) Text("Pendiente de sincronización", color = MaterialTheme.colorScheme.tertiary); Row { if (s.puede("LABOR_EDITAR")) TextButton({ vm.editar(l) }) { Text("Editar") }; if (s.puede("LABOR_ELIMINAR")) TextButton({ vm.eliminar(l) }) { Text("Eliminar") } } } } } } } }

@Composable private fun Formulario(e: EstadoApp, vm: AppViewModel) { val f = e.formulario; Column(Modifier.fillMaxSize().padding(16.dp)) { Text(if (f.id == 0L) "Registrar labor" else "Corregir labor", style = MaterialTheme.typography.headlineSmall); Row { TipoLabor.entries.forEach { t -> FilterChip(f.tipo == t, { vm.actualizarFormulario(f.copy(tipo = t)) }, { Text(t.name) }, Modifier.padding(end = 4.dp)) } }; Campo("Fecha (AAAA-MM-DD)", f.fecha, { vm.actualizarFormulario(f.copy(fecha = it)) }, f.errores["fecha"]); Campo("Cantidad", f.cantidad, { vm.actualizarFormulario(f.copy(cantidad = it)) }, f.errores["cantidad"]); Campo("Cantidad de jornales", f.cantidadJornales, { vm.actualizarFormulario(f.copy(cantidadJornales = it)) }, f.errores["cantidadJornales"]); Campo("Observación", f.observacion, { vm.actualizarFormulario(f.copy(observacion = it)) }, f.errores["observacion"]); Button(vm::guardarLabor, Modifier.fillMaxWidth().padding(top = 12.dp)) { Text("Guardar") } } }
@Composable private fun Campo(etiqueta: String, valor: String, cambio: (String) -> Unit, error: String?) = OutlinedTextField(valor, cambio, label = { Text(etiqueta) }, isError = error != null, supportingText = { error?.let { Text(it) } }, modifier = Modifier.fillMaxWidth())
@Composable private fun Perfil(e: EstadoApp, vm: AppViewModel) { val s = e.sesion ?: return; Column(Modifier.fillMaxSize().padding(24.dp)) { Text(s.nombre, style = MaterialTheme.typography.headlineSmall); Text("Usuario: ${s.usuario}"); Text("Rol: ${s.rol}"); Text("Privilegios:"); s.privilegios.sorted().forEach { Text("• $it") }; Button(vm::cerrarSesion, Modifier.padding(top = 24.dp)) { Text("Cerrar sesión") } } }
