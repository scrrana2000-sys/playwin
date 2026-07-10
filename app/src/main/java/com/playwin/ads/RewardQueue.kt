package com.playwin.ads

import java.util.concurrent.ConcurrentHashMap
import java.util.UUID

object RewardQueue {
    private val activeTokens = ConcurrentHashMap.newKeySet<String>()
    private var lastRequestTime = 0L
    private const val THROTTLE_DURATION_MS = 1500L

    fun generateUniqueToken(): String {
        val token = UUID.randomUUID().toString()
        activeTokens.add(token)
        return token
    }

    fun isTokenActive(token: String): Boolean {
        return activeTokens.contains(token)
    }

    fun consumeToken(token: String): Boolean {
        return activeTokens.remove(token)
    }

    fun canProcessRequest(): Boolean {
        val now = System.currentTimeMillis()
        if (now - lastRequestTime < THROTTLE_DURATION_MS) {
            AdLogger.w("Throttled rapid click attempt.")
            return false
        }
        lastRequestTime = now
        return true
    }

    fun clearAll() {
        activeTokens.clear()
    }
}
