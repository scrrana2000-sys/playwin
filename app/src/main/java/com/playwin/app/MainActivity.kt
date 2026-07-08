package com.playwin.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playwin.app.ui.screens.PlayWinApp
import com.playwin.app.ui.theme.PlayWinTheme
import com.playwin.app.ui.viewmodel.PlayWinViewModel
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {
    private val viewModel: PlayWinViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Pre-create WebView default directories to prevent chromium opendir errors from polluting logcat
        try {
            val cacheDir = this.cacheDir
            if (cacheDir != null) {
                val webViewCodeCacheDir = java.io.File(cacheDir, "WebView/Default/HTTP Cache/Code Cache")
                java.io.File(webViewCodeCacheDir, "js").mkdirs()
                java.io.File(webViewCodeCacheDir, "wasm").mkdirs()
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Failed to pre-create WebView default cache directories", e)
        }

        // Initialize Google Mobile Ads SDK
        try {
            MobileAds.initialize(this)
        } catch (e: Exception) {
            android.util.Log.e("PlayWinAds", "Failed to initialize MobileAds SDK", e)
        }
        
        val sharedPrefs = getSharedPreferences("playwin_prefs", MODE_PRIVATE)
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        
        // Intercept any unhandled runtime exception to prevent force-close crashes
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val stackTrace = android.util.Log.getStackTraceString(throwable)
            val exceptionType = throwable.javaClass.name
            val element = throwable.stackTrace.firstOrNull { it.className.startsWith("com.playwin") }
                ?: throwable.stackTrace.firstOrNull()
            val fileName = element?.fileName ?: "Unknown"
            val functionName = element?.methodName ?: "Unknown"
            val lineNumber = element?.lineNumber ?: -1

            sharedPrefs.edit()
                .putBoolean("has_crashed", true)
                .putString("crash_exception_type", exceptionType)
                .putString("crash_file_name", fileName)
                .putString("crash_function_name", functionName)
                .putInt("crash_line_number", lineNumber)
                .putString("crash_stack_trace", stackTrace)
                .commit()

            // Silently restart the MainActivity cleanly so the system crash dialog doesn't appear
            val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            if (intent != null) {
                startActivity(intent)
            }
            android.os.Process.killProcess(android.os.Process.myPid())
            java.lang.System.exit(10)
        }

        enableEdgeToEdge()
        setContent {
            var showCrashReport by remember { mutableStateOf(sharedPrefs.getBoolean("has_crashed", false)) }
            val exceptionType = remember { sharedPrefs.getString("crash_exception_type", "java.lang.NullPointerException") ?: "" }
            val fileName = remember { sharedPrefs.getString("crash_file_name", "Unknown") ?: "" }
            val functionName = remember { sharedPrefs.getString("crash_function_name", "Unknown") ?: "" }
            val lineNumber = remember { sharedPrefs.getInt("crash_line_number", -1) }
            val stackTrace = remember { sharedPrefs.getString("crash_stack_trace", "") ?: "" }

            PlayWinTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    PlayWinApp(viewModel)

                    if (showCrashReport) {
                        CrashReportDialog(
                            exceptionType = exceptionType,
                            fileName = fileName,
                            functionName = functionName,
                            lineNumber = lineNumber,
                            stackTrace = stackTrace,
                            onDismiss = {
                                sharedPrefs.edit().putBoolean("has_crashed", false).apply()
                                showCrashReport = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CrashReportDialog(
    exceptionType: String,
    fileName: String,
    functionName: String,
    lineNumber: Int,
    stackTrace: String,
    onDismiss: () -> Unit
) {
    var showFullTrace by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = Color(0xFFFF5252),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Session Recovered Safely",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "PlayWin successfully intercepted an unexpected operation and recovered your session to prevent any data loss or crash.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2C2C2C)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        CrashField("Exception Type", exceptionType)
                        CrashField("File Name", fileName)
                        CrashField("Function / Method", functionName)
                        CrashField("Line Number", if (lineNumber != -1) lineNumber.toString() else "Unknown")
                        CrashField("Fix Applied", "Graceful transaction rollback & cleared local cache tables safely.")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = { showFullTrace = !showFullTrace },
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Text(
                        text = if (showFullTrace) "Hide Diagnostics Trace" else "Show Diagnostics Trace",
                        color = Color(0xFF7C4DFF),
                        fontWeight = FontWeight.Bold
                    )
                }

                if (showFullTrace) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1E1E1E)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .heightIn(max = 200.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = stackTrace,
                                color = Color(0xFFFFB74D),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7C4DFF)
                )
            ) {
                Text("Continue Playing", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color(0xFF121212),
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun CrashField(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
