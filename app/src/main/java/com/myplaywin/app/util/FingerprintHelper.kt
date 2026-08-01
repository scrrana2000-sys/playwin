package com.myplaywin.app.util

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import java.security.MessageDigest
import java.util.Locale

object FingerprintHelper {
    private const val TAG = "FingerprintHelper"

    fun logAppSignature(context: Context) {
        try {
            @Suppress("DEPRECATION")
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNATURES
            )
            val signatures = packageInfo.signatures
            if (signatures != null) {
                for (signature in signatures) {
                    val md = MessageDigest.getInstance("SHA-1")
                    md.update(signature.toByteArray())
                    val digest = md.digest()
                    val hexString = StringBuilder()
                    for (i in digest.indices) {
                        val hex = Integer.toHexString(0xFF and digest[i].toInt())
                        if (hex.length == 1) hexString.append('0')
                        hexString.append(hex.uppercase(Locale.ROOT))
                        if (i < digest.size - 1) hexString.append(':')
                    }
                    Log.d(TAG, "App SHA-1 Fingerprint: $hexString")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting SHA-1 fingerprint", e)
        }
    }
}
