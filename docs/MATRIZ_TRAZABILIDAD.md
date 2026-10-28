# Matriz de trazabilidad — AgroCampo

Fecha de verificación: 27 de octubre de 2026. Los estados se basan en código y ejecuciones reales, no en resultados supuestos.

## Requisitos funcionales

| Requisito | Implementación principal | Verificación/evidencia | Estado |
|---|---|---|---|
| RF-A01 Login | `SesionRepositoryImpl`, `AgroApi`, `AppViewModel`, pantalla Compose | Login real con `tecnico1` contra AgroAPI; padrón recibido en emulador | Cumplido |
| RF-A02 Sesión persistente | `AlmacenSeguroAndroid`, `SesionRepositoryImpl` | AES/GCM con Android Keystore; reapertura offline conservó sesión y padrón | Cumplido |
| RF-A03 Padrón | SQLDelight `observarLotes`, `LoteRepositoryImpl`, pantalla de lotes | Captura real con 8 lotes; reapertura sin red mostró caché | Cumplido |
| RF-A04 Detalle | `AgroCampoApp.kt`, repositorios de lote/labor | Navegación, datos, labores y acciones condicionadas por privilegio | Parcial: falta conservar captura del recorrido completo |
| RF-A05 Registro/corrección | formulario Compose, `LaborRepositoryImpl` | Validación local, responsable de sesión, alta/edición online y cola offline implementadas | Parcial: falta evidencia visual completa de edición y errores de campo |
| RF-A06 Offline-first | `AgroDb.sq`, `LaborRepositoryImpl`, `SincronizarUseCase`, observador de red | Tests de ID negativo y orden push/pull; ejecución offline real | Cumplido |
| RF-A07 Perfil | pantalla Compose y sesión observable | Usuario, rol, privilegios y cierre de sesión implementados | Parcial: falta captura final |

## Requisitos técnicos

| Requisito | Archivo o componente | Prueba/evidencia | Estado |
|---|---|---|---|
| Una instancia Ktor inyectada | `ClienteHttp.kt`, Koin | Compilación y llamadas reales | Cumplido |
| Nueve endpoints REST | `AgroApi.kt` | Login y listados comprobados; rutas restantes compiladas | Parcial: no se ejecutaron las nueve rutas en un recorrido final |
| Paginación de objetos | DTO comunes y `AgroApi.kt` | Respuesta real de lotes procesada | Cumplido |
| Errores tipados | `MapeoErrores.kt`, `ErrorApi.kt` | Test 403; integración comprobó 401 y 403 | Parcial: faltan tests separados 400, 401 y 409 |
| JWT cifrado y expiración | `AlmacenSeguroAndroid.kt`, `SesionRepositoryImpl.kt` | Restauración real de sesión | Cumplido |
| SQLDelight como fuente de verdad | `AgroDb.sq`, repositorios | Vista offline real y tests en memoria | Cumplido |
| Cola e IDs negativos | tabla `labor`, `LaborRepositoryImpl` | `registroSinConexionUsaIdNegativoYPendiente` | Cumplido |
| Push antes de pull y transacciones | `LaborRepositoryImpl.sincronizar` | `sincronizacionSubeAntesDeDescargar` | Cumplido |
| Sincronización al recuperar red | `ObservadorConexionAndroid`, `AppViewModel` | Compilación; falta prueba instrumental de reconexión | Parcial |
| Privilegios desde sesión | `Sesion.puede`, UI Compose | Test SC-B y 403 real al cerrar campaña como técnico | Cumplido |
| Compilación Android | Gradle | `:androidApp:assembleDebug`, BUILD SUCCESSFUL | Cumplido |
| Pruebas comunes | 13 pruebas entre suites comunes y host | `:shared:testAndroidHostTest`, 0 fallos y 0 errores | Cumplido |
| Publicación GitHub | `origin` y 13 ramas remotas | Push confirmado sin force push; `main`, `develop`, siete `feature/*` y cuatro `sc-*` | Cumplido |
| Pull requests | Comparaciones GitHub preparadas | GitHub CLI ausente y sesión web no autenticada | Bloqueado |

## Solicitudes complementarias

| Solicitud | Rama | Implementación y prueba | Estado |
|---|---|---|---|
| SC-A Solo campañas abiertas combinado con valle | `sc-a-sanchez` | Condición SQL observable; `ScACommonTest` | Cumplido |
| SC-B SUPERVISOR_CALIDAD sin privilegios extra | `sc-b-sanchez` | UI genérica por privilegios; `ScBCommonTest` | Cliente cumplido; E2E bloqueado porque el backend inspeccionado no expuso ese usuario |
| SC-C cantidad de jornales | `sc-c-sanchez` | dominio, DTO, request, SQLDelight, formulario y detalle; `ScCCommonTest` | Cumplido para instalación nueva; migración previa no verificada |
| SC-D pendientes por lote | `sc-d-sanchez` | `COUNT` observable en SQLDelight y distintivo Compose; `ScDCommonTest` | Cumplido |

## Evidencia real disponible

- `build/agrocampo_login4.png`: pantalla de login ejecutada.
- `build/agrocampo_lotes_real.png`: login real y padrón descargado desde AgroAPI.
- `build/agrocampo_offline.png`: sesión y padrón restaurados sin red.
- Gradle final: `BUILD SUCCESSFUL` en 1 min 25 s para 13 pruebas y APK.
- Integración directa: 401 sin JWT y 403 al intentar cerrar campaña con `tecnico1`.

La ausencia de una captura o prueba explícita mantiene el requisito en parcial aunque exista implementación. No se publica etiqueta estable hasta cerrar esas verificaciones.
