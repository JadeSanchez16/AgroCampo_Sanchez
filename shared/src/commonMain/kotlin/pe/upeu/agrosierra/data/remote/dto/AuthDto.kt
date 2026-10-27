package pe.upeu.agrosierra.data.remote.dto

import kotlinx.serialization.Serializable
import pe.upeu.agrosierra.domain.model.Sesion

@Serializable data class LoginRequestDto(val usuario: String, val contrasena: String)

@Serializable
data class LoginResponseDto(
    val token: String,
    val nombre: String,
    val usuario: String,
    val rol: String,
    val privilegios: Set<String>,
)

@Serializable
data class UsuarioActualDto(
    val id: Long,
    val usuario: String,
    val nombre: String,
    val rol: String,
    val privilegios: Set<String>,
)

fun LoginResponseDto.aDominio() = Sesion(token, usuario, rol, privilegios, nombre)
fun UsuarioActualDto.aDominio(token: String) = Sesion(token, usuario, rol, privilegios, nombre)
