package pe.upeu.agrosierra.data.local

interface AlmacenSeguro {
    fun guardar(clave: String, valor: String)
    fun leer(clave: String): String?
    fun eliminar(clave: String)
}

expect fun tokenSigueVigente(token: String): Boolean
