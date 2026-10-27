package pe.upeu.agrosierra.data.local

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

fun crearBaseLocalAndroid(context: Context): BaseLocal =
    BaseLocal(AndroidSqliteDriver(AgroDb.Schema, context, "agrocampo.db"))
