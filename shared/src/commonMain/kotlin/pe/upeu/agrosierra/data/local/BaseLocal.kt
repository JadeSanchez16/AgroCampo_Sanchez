package pe.upeu.agrosierra.data.local

import app.cash.sqldelight.db.SqlDriver

class BaseLocal(driver: SqlDriver) {
    val db = AgroDb(driver)
    val consultas get() = db.agroDbQueries
}
