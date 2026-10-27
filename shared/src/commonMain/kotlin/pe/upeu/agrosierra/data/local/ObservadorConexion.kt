package pe.upeu.agrosierra.data.local

import kotlinx.coroutines.flow.Flow

interface ObservadorConexion { val conectado: Flow<Boolean> }
