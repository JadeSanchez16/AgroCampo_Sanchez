package pe.upeu.agrosierra.data.local

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class ObservadorConexionAndroid(context: Context) : ObservadorConexion {
    private val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    override val conectado: Flow<Boolean> = callbackFlow {
        fun estado(): Boolean = manager.activeNetwork?.let { red -> manager.getNetworkCapabilities(red)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } == true
        trySend(estado())
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { trySend(true) }
            override fun onLost(network: Network) { trySend(estado()) }
        }
        manager.registerDefaultNetworkCallback(callback)
        awaitClose { manager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()
}
