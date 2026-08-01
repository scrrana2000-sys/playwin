package com.myplaywin.app.data.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Base64
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import androidx.credentials.CredentialOption
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.myplaywin.app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.SecureRandom

sealed class GoogleSignInResult {
    data class Success(
        val idToken: String,
        val displayName: String,
        val email: String,
        val profilePictureUrl: String
    ) : GoogleSignInResult()

    object Cancelled : GoogleSignInResult()
    data class Error(val message: String) : GoogleSignInResult()
}

object GoogleAuthManager {
    private const val TAG = "GoogleAuthManager"

    private fun findActivity(context: Context): Activity? {
        var currentContext = context
        while (currentContext is ContextWrapper) {
            if (currentContext is Activity) return currentContext
            currentContext = currentContext.baseContext
        }
        return null
    }

    private fun generateSecureRandomNonce(byteLength: Int = 32): String {
        val randomBytes = ByteArray(byteLength)
        SecureRandom().nextBytes(randomBytes)
        return Base64.encodeToString(randomBytes, Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING)
    }

    fun isNetworkAvailable(context: Context): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            true
        }
    }

    fun isGooglePlayServicesAvailable(context: Context): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
        return resultCode == ConnectionResult.SUCCESS
    }

    suspend fun getGoogleIdToken(context: Context): GoogleSignInResult {
        return withContext(Dispatchers.IO) {
            Log.d(TAG, "Starting getGoogleIdToken (Multi-Step Flow)")
            if (!isNetworkAvailable(context)) {
                return@withContext GoogleSignInResult.Error("No internet connection. Please check your network and try again.")
            }

            if (!isGooglePlayServicesAvailable(context)) {
                return@withContext GoogleSignInResult.Error("Google Play Services is not available or needs to be updated.")
            }

            val webClientId = try {
                context.getString(R.string.default_web_client_id)
            } catch (e: Exception) {
                "228349425977-e2mjc70g7lp8qj4r8rkvm0cu46odohf8.apps.googleusercontent.com"
            }
            Log.d(TAG, "Using Web Client ID: ${webClientId.take(10)}...")

            // Step 1: Try to get an authorized account (silent/fast)
            Log.d(TAG, "Step 1: Attempting silent sign-in with GetGoogleIdOption(filterByAuthorizedAccounts=true)")
            val silentOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(true)
                .build()

            val silentResult = tryGetCredential(context, silentOption)
            
            if (silentResult is GoogleSignInResult.Success) {
                Log.d(TAG, "Step 1 Success!")
                return@withContext silentResult
            }
            
            if (silentResult is GoogleSignInResult.Cancelled) {
                Log.d(TAG, "Step 1 Cancelled by user")
                return@withContext silentResult
            }

            // Step 2: Full interactive button flow (GetSignInWithGoogleOption)
            // This is designed to show the picker and handle "Add Account" scenarios.
            Log.d(TAG, "Step 1 failed or returned No Credentials. Step 2: Attempting full interactive flow with GetSignInWithGoogleOption")
            val nonce = generateSecureRandomNonce()
            val interactiveOption = GetSignInWithGoogleOption.Builder(webClientId)
                .setNonce(nonce)
                .build()

            val interactiveResult = tryGetCredential(context, interactiveOption)
            
            if (interactiveResult is GoogleSignInResult.Error && interactiveResult.message == "No credentials available") {
                Log.e(TAG, "Step 2 also returned No Credentials. This usually means NO Google accounts are on the device.")
                return@withContext GoogleSignInResult.Error("No Google accounts found. Please add a Google account in your device Settings.")
            }

            interactiveResult
        }
    }

    private suspend fun tryGetCredential(
        context: Context,
        option: CredentialOption
    ): GoogleSignInResult {
        return try {
            val optionType = option.javaClass.simpleName
            Log.d(TAG, "Requesting credential with option: $optionType")
            
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(option)
                .build()

            val credentialManager = CredentialManager.create(context)
            
            // Critical for reliable UI: Find Activity context
            val activityContext = findActivity(context) ?: context
            if (activityContext !is Activity) {
                Log.w(TAG, "No Activity context found. UI picker might not appear correctly.")
            }

            val result = credentialManager.getCredential(
                context = activityContext,
                request = request
            )

            val credential = result.credential
            Log.d(TAG, "getCredential returned successfully. Type: ${credential.type}")
            
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                Log.d(TAG, "Successfully retrieved ID Token for ${googleIdTokenCredential.id}. Token length: ${googleIdTokenCredential.idToken.length}")
                GoogleSignInResult.Success(
                    idToken = googleIdTokenCredential.idToken,
                    displayName = googleIdTokenCredential.displayName ?: "",
                    email = googleIdTokenCredential.id ?: "",
                    profilePictureUrl = googleIdTokenCredential.profilePictureUri?.toString() ?: ""
                )
            } else {
                Log.e(TAG, "Unexpected credential type: ${credential.type}")
                GoogleSignInResult.Error("Failed to retrieve Google credentials (Unexpected type: ${credential.type}).")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "User cancelled the sign-in bottom sheet/picker (GetCredentialCancellationException).")
            GoogleSignInResult.Cancelled
        } catch (e: NoCredentialException) {
            Log.d(TAG, "NoCredentialException: No matching credentials found for this request. Message: ${e.message}")
            GoogleSignInResult.Error("No credentials available")
        } catch (e: GetCredentialException) {
            Log.e(TAG, "GetCredentialException [Type: ${e.type}]: ${e.message}", e)
            val msg = "Sign-In Failed: ${e.localizedMessage ?: "Unknown Error"} (Type: ${e.type})"
            GoogleSignInResult.Error(msg)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during credential retrieval [${e.javaClass.simpleName}]: ${e.message}", e)
            GoogleSignInResult.Error("Unexpected Error: ${e.localizedMessage ?: "An error occurred."} [${e.javaClass.simpleName}]")
        }
    }
}
