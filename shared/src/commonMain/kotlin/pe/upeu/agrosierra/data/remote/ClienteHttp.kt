package pe.upeu.agrosierra.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun crearClienteHttp(engine: io.ktor.client.engine.HttpClientEngine, token: () -> String?): HttpClient = HttpClient(engine) {
    expectSuccess = false
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true; explicitNulls = false })
    }
    install(HttpTimeout) {
        connectTimeoutMillis = ApiConfig.CONNECT_TIMEOUT_MS
        requestTimeoutMillis = ApiConfig.REQUEST_TIMEOUT_MS
    }
    install(Logging) { level = LogLevel.INFO; sanitizeHeader { it == HttpHeaders.Authorization } }
    defaultRequest {
        url.takeFrom(ApiConfig.BASE_URL)
        url.protocol = URLProtocol.HTTP
        token()?.takeIf { it.isNotBlank() }?.let { header(HttpHeaders.Authorization, "Bearer $it") }
    }
}
