package pe.upeu.agrosierra.domain.model

data class Sesion(
    val token: String,
    val usuario: String,
    val rol: String,
    val privilegios: Set<String>,
    val nombre: String = usuario,
)

fun Sesion.puede(privilegio: String): Boolean = privilegio in privilegios
