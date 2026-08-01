package com.myplaywin.app.data.auth

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.myplaywin.app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    suspend fun getGoogleIdToken(context: Context): GoogleSignInResult {
        return withContext(Dispatchers.IO) {
            if (!isNetworkAvailable(context)) {
                return@withContext GoogleSignInResult.Error("No internet connection. Please check your network and try again.")
            }

            try {
                val webClientId = try {
                    context.getString(R.string.default_web_client_id)
                } catch (e: Exception) {
                    "228349425977-e2mjc70g7lp8qj4r8rkvm0cu46odohf8.apps.googleusercontent.com"
                }

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val credentialManager = CredentialManager.create(context)
                
                val result = credentialManager.getCredential(
                    context = context,
                    request = request
                )

                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    GoogleSignInResult.Success(
                        idToken = googleIdTokenCredential.idToken,
                        displayName = googleIdTokenCredential.displayName ?: "",
                        email = googleIdTokenCredential.id ?: "",
                        profilePictureUrl = googleIdTokenCredential.profilePictureUri?.toString() ?: ""
                    )
                } else {
                    GoogleSignInResult.Error("Failed to retrieve Google credentials.")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.d(TAG, "User cancelled Google Sign-In")
                GoogleSignInResult.Cancelled
            } catch (e: GetCredentialException) {
                Log.e(TAG, "Credential Manager Exception", e)
                val isNoCredentials = e is androidx.credentials.exceptions.NoCredentialException || 
                        e.message?.contains("No credentials available", ignoreCase = true) == true ||
                        e.message?.contains("No accounts", ignoreCase = true) == true

                if (isNoCredentials) {
                    try {
                        val intent = android.content.Intent(android.provider.Settings.ACTION_ADD_ACCOUNT).apply {
                            putExtra(android.provider.Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
                            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                        GoogleSignInResult.Cancelled
                    } catch (activityException: Exception) {
                        Log.e(TAG, "Failed to start Add Account activity", activityException)
                        GoogleSignInResult.Error("No Google account found on this device. Failed to open system settings to add one.")
                    }
                } else {
                    val msg = e.localizedMessage ?: "Google Sign-In failed. Please try again."
                    GoogleSignInResult.Error(msg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected Google Sign-In Exception", e)
                GoogleSignInResult.Error(e.localizedMessage ?: "An unexpected error occurred during Google Sign-In.")
            }
        }
    }
}
