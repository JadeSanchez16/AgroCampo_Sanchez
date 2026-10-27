package pe.upeu.agrosierra

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform