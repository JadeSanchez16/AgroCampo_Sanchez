# AgroCampo

Cliente Android Kotlin Multiplatform del caso AgroSierra para el Examen Parcial II, Unidad 2. Consume AgroAPI, mantiene SQLDelight como fuente de verdad y permite registrar y corregir labores sin conexión mediante una cola persistente.

## Funcionalidad

- Login real con JWT y restauración de sesión cifrada con Android Keystore.
- Padrón local de lotes con búsqueda, valle, campañas abiertas y pendientes por lote.
- Detalle de lote y labores ordenadas por fecha descendente.
- Alta, corrección y eliminación de labores según privilegios.
- Cierre de campaña sujeto a `CAMPANIA_CERRAR`.
- Sincronización manual y automática al recuperar conectividad.
- IDs negativos y estado visible para labores pendientes.
- Perfil, privilegios y cierre de sesión.

## Arquitectura

El módulo `shared` contiene las capas comunes:

```text
shared/src/commonMain/kotlin/pe/upeu/agrosierra/
├── domain/          Modelos, contratos y casos de uso
├── data/            Ktor, SQLDelight y repositorios offline-first
├── presentation/    Estado, navegación y pantallas Compose
└── di/              Módulos comunes de Koin
```

Las implementaciones dependientes de Android están en `shared/src/androidMain`: SQLDelight Android, observación de conectividad y almacenamiento AES/GCM con una clave no exportable de Android Keystore. `androidApp` contiene la actividad y la clase `Application`.

## Contrato de AgroAPI

URL lógica: `http://localhost:8080/api/v1/`.

| Método | Ruta | Uso |
|---|---|---|
| POST | `/auth/login` | Autenticación |
| GET | `/lotes` | Padrón paginado |
| GET | `/lotes/{id}` | Detalle |
| GET | `/lotes/{id}/labores` | Labores paginadas |
| POST | `/labores` | Registrar |
| PUT | `/labores/{id}` | Corregir |
| DELETE | `/labores/{id}` | Eliminar |
| POST | `/lotes/{id}/cerrar-campania` | Cerrar campaña |
| GET | `/usuarios/me` | Perfil autenticado |

Los listados se deserializan como objetos con `contenido`, `pagina`, `tamanio`, `totalElementos`, `totalPaginas` y `ultima`. Los errores conservan `message` y `validationErrors`.

## Roles y privilegios

- `TECNICO`: lectura y labores según privilegios y reglas del backend.
- `JEFE_CAMPO`: administra labores y puede cerrar campañas.
- `ADMINISTRADOR`: lectura y `USUARIO_GESTIONAR`; no registra labores.
- `SUPERVISOR_CALIDAD` (SC-B): lectura y `LABOR_EDITAR`; sin crear, eliminar ni cerrar campaña.

La interfaz consulta el conjunto de privilegios de la sesión, no nombres rígidos de rol. AgroAPI conserva la autorización definitiva.

## Ejecución local

Requisitos: JDK 17 o posterior, Android SDK 37, un emulador iniciado, Docker y AgroAPI disponible en el puerto 8080.

1. Iniciar AgroAPI y PostgreSQL en su proyecto con `docker compose up --build -d`.
2. Crear el túnel al emulador: `adb reverse tcp:8080 tcp:8080`.
3. Compilar: `./gradlew :androidApp:assembleDebug` (en Windows: `.\\gradlew.bat :androidApp:assembleDebug`).
4. Instalar el APK de depuración o ejecutar `androidApp` desde Android Studio.

El túnel ADB es necesario en el entorno validado porque el cliente de desarrollo apunta a `127.0.0.1:8080`. Debe recrearse después de reiniciar el emulador. Para un dispositivo físico se debe usar HTTPS o una configuración local explícita; no se deben publicar secretos ni excepciones HTTP amplias.

## Pruebas

```powershell
.\gradlew.bat :shared:testAndroidHostTest :androidApp:assembleDebug
```

Las pruebas usan Ktor `MockEngine` y SQLite en memoria. Cubren conversión DTO, error 403, alta offline con ID negativo, orden de sincronización, filtro SC-A, privilegios SC-B, jornales SC-C y contador SC-D.

## Estrategia Git

- `main`: versión estable evaluada.
- `develop`: integración verificada.
- `feature/*`: cliente Ktor, seguridad, CRUD, persistencia, sincronización, offline-first y pruebas.
- `sc-a-sanchez` a `sc-d-sanchez`: solicitudes complementarias independientes.

No se publica la etiqueta `v1.0-unidad2` hasta completar el recorrido final y comprobar el commit exacto. La trazabilidad detallada está en [docs/MATRIZ_TRAZABILIDAD.md](docs/MATRIZ_TRAZABILIDAD.md).
