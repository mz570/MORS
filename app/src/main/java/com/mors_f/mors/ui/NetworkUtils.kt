package com.mors_f.mors.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

sealed class ConnectionState {
    object Available : ConnectionState()
    object Unavailable : ConnectionState()
}

@Composable
fun connectivityState(): State<ConnectionState> {
    val context = LocalContext.current
    return produceState(initialValue = context.currentConnectionState) {
        context.observeConnectivity().collect { value = it }
    }
}

private val Context.currentConnectionState: ConnectionState
    get() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (connectivityManager.isCurrentlyConnected()) ConnectionState.Available else ConnectionState.Unavailable
    }

private fun ConnectivityManager.isCurrentlyConnected() =
    activeNetwork?.let { getNetworkCapabilities(it) }
        ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false

private fun Context.observeConnectivity() = callbackFlow {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            trySend(ConnectionState.Available)
        }

        override fun onLost(network: Network) {
            trySend(ConnectionState.Unavailable)
        }
    }

    val request = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()
    connectivityManager.registerNetworkCallback(request, callback)

    awaitClose {
        connectivityManager.unregisterNetworkCallback(callback)
    }
}
