package com.playwin.ads

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NetworkMonitor(private val context: Context) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    
    private val _isNetworkAvailable = MutableStateFlow(isNetworkAvailable())
    val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable.asStateFlow()

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    fun isNetworkAvailable(): Boolean {
        val cm = connectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    fun startMonitoring(onNetworkRestored: () -> Unit) {
        if (networkCallback != null) return

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                val wasOffline = !_isNetworkAvailable.value
                _isNetworkAvailable.value = true
                AdLogger.i("Network Monitor: Network restored / available.")
                if (wasOffline) {
                    onNetworkRestored()
                }
            }

            override fun onLost(network: Network) {
                _isNetworkAvailable.value = false
                AdLogger.w("Network Monitor: Network lost / disconnected.")
            }
        }

        networkCallback = callback
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager?.registerNetworkCallback(request, callback)
        } catch (e: Exception) {
            AdLogger.e("Failed to register network callback", e)
        }
    }

    fun stopMonitoring() {
        networkCallback?.let {
            try {
                connectivityManager?.unregisterNetworkCallback(it)
            } catch (e: Exception) {
                AdLogger.e("Failed to unregister network callback", e)
            }
            networkCallback = null
        }
    }
}
