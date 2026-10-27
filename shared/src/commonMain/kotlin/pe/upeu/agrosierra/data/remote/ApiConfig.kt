package pe.upeu.agrosierra.data.remote

object ApiConfig {
    // En desarrollo se ejecuta: adb reverse tcp:8080 tcp:8080.
    const val BASE_URL = "http://127.0.0.1:8080/api/v1/"
    const val CONNECT_TIMEOUT_MS = 10_000L
    const val REQUEST_TIMEOUT_MS = 20_000L
}
