package com.playwin.app.ui.screens

import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.os.Bundle
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.activity.compose.BackHandler
import com.playwin.app.data.model.FirebaseCoupon
import com.playwin.app.data.model.FirebaseRedemption
import com.playwin.app.data.model.FirebaseReferralRecord
import com.playwin.app.data.model.FirebaseTransaction
import com.playwin.app.data.model.RewardTransaction
import com.playwin.app.ui.theme.*
import com.playwin.app.ui.viewmodel.PlayWinViewModel
import com.playwin.app.ui.viewmodel.AuthState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.text.input.PasswordVisualTransformation

sealed interface AppScreen {
    object MainTabs : AppScreen
    object SpinGame : AppScreen
    object ScratchGame : AppScreen
    data class TriviaGame(val categoryId: String, val quizSetId: String) : AppScreen
    object MathGame : AppScreen
    object UPIWithdraw : AppScreen
    object Wallet : AppScreen
    object Referral : AppScreen
}

enum class AppTab {
    Home, Quiz, Tasks, Coupons, Leaderboard, Profile
}

@Composable
fun PlayWinApp(viewModel: PlayWinViewModel) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    when (val state = authState) {
        is AuthState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFF7C4DFF))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Securing Session...", color = Color.White, fontSize = 14.sp)
                }
            }
        }
        is AuthState.Unauthenticated -> {
            LoginScreen(viewModel = viewModel)
        }
        is AuthState.Authenticated -> {
            val isBlocked by viewModel.currentUserBlockedState.collectAsStateWithLifecycle()
            if (isBlocked) {
                BlockedUserScreen()
            } else {
                PlayWinMainFlow(viewModel = viewModel)
            }
        }
    }
}

data class NavEntry(val screen: AppScreen, val tab: AppTab)

val NavEntrySaver = Saver<NavEntry, Bundle>(
    save = { entry ->
        Bundle().apply {
            putString("tab", entry.tab.name)
            when (val screen = entry.screen) {
                is AppScreen.MainTabs -> putString("type", "MainTabs")
                is AppScreen.SpinGame -> putString("type", "SpinGame")
                is AppScreen.ScratchGame -> putString("type", "ScratchGame")
                is AppScreen.TriviaGame -> {
                    putString("type", "TriviaGame")
                    putString("categoryId", screen.categoryId)
                    putString("quizSetId", screen.quizSetId)
                }
                is AppScreen.MathGame -> putString("type", "MathGame")
                is AppScreen.UPIWithdraw -> putString("type", "UPIWithdraw")
                is AppScreen.Wallet -> putString("type", "Wallet")
                is AppScreen.Referral -> putString("type", "Referral")
            }
        }
    },
    restore = { bundle ->
        val tab = AppTab.valueOf(bundle.getString("tab") ?: "Home")
        val screen = when (bundle.getString("type")) {
            "MainTabs" -> AppScreen.MainTabs
            "SpinGame" -> AppScreen.SpinGame
            "ScratchGame" -> AppScreen.ScratchGame
            "TriviaGame" -> AppScreen.TriviaGame(
                categoryId = bundle.getString("categoryId") ?: "",
                quizSetId = bundle.getString("quizSetId") ?: ""
            )
            "MathGame" -> AppScreen.MathGame
            "UPIWithdraw" -> AppScreen.UPIWithdraw
            "Wallet" -> AppScreen.Wallet
            "Referral" -> AppScreen.Referral
            else -> AppScreen.MainTabs
        }
        NavEntry(screen, tab)
    }
)

val NavHistorySaver = Saver<SnapshotStateList<NavEntry>, List<Bundle>>(
    save = { list ->
        list.map { entry ->
            Bundle().apply {
                putString("tab", entry.tab.name)
                when (val screen = entry.screen) {
                    is AppScreen.MainTabs -> putString("type", "MainTabs")
                    is AppScreen.SpinGame -> putString("type", "SpinGame")
                    is AppScreen.ScratchGame -> putString("type", "ScratchGame")
                    is AppScreen.TriviaGame -> {
                        putString("type", "TriviaGame")
                        putString("categoryId", screen.categoryId)
                        putString("quizSetId", screen.quizSetId)
                    }
                    is AppScreen.MathGame -> putString("type", "MathGame")
                    is AppScreen.UPIWithdraw -> putString("type", "UPIWithdraw")
                    is AppScreen.Wallet -> putString("type", "Wallet")
                    is AppScreen.Referral -> putString("type", "Referral")
                }
            }
        }
    },
    restore = { bundles ->
        val list = mutableStateListOf<NavEntry>()
        bundles.forEach { bundle ->
            val tab = AppTab.valueOf(bundle.getString("tab") ?: "Home")
            val screen = when (bundle.getString("type")) {
                "MainTabs" -> AppScreen.MainTabs
                "SpinGame" -> AppScreen.SpinGame
                "ScratchGame" -> AppScreen.ScratchGame
                "TriviaGame" -> AppScreen.TriviaGame(
                    categoryId = bundle.getString("categoryId") ?: "",
                    quizSetId = bundle.getString("quizSetId") ?: ""
                )
                "MathGame" -> AppScreen.MathGame
                "UPIWithdraw" -> AppScreen.UPIWithdraw
                "Wallet" -> AppScreen.Wallet
                "Referral" -> AppScreen.Referral
                else -> AppScreen.MainTabs
            }
            list.add(NavEntry(screen, tab))
        }
        list
    }
)

@Composable
fun PlayWinMainFlow(viewModel: PlayWinViewModel) {
    var currentNavEntry by rememberSaveable(stateSaver = NavEntrySaver) {
        mutableStateOf(NavEntry(AppScreen.MainTabs, AppTab.Home))
    }
    val navHistory = rememberSaveable(saver = NavHistorySaver) {
        mutableStateListOf<NavEntry>()
    }

    val currentScreen = currentNavEntry.screen
    val currentTab = currentNavEntry.tab

    var showExitDialog by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    val wallet by viewModel.walletState.collectAsStateWithLifecycle()
    val transactions by viewModel.transactionsState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Reset screens/tabs on user sign out detection
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    LaunchedEffect(authState) {
        if (authState !is AuthState.Authenticated) {
            currentNavEntry = NavEntry(AppScreen.MainTabs, AppTab.Home)
            navHistory.clear()
        }
    }

    val onTabSelected: (AppTab) -> Unit = { newTab ->
        if (newTab != currentTab) {
            val lastEntry = navHistory.lastOrNull()
            if (lastEntry == null || lastEntry != currentNavEntry) {
                navHistory.add(currentNavEntry)
            }
            currentNavEntry = NavEntry(AppScreen.MainTabs, newTab)
        }
    }

    val onNavigateToGame: (AppScreen) -> Unit = { newScreen ->
        if (newScreen != currentScreen) {
            val lastEntry = navHistory.lastOrNull()
            if (lastEntry == null || lastEntry != currentNavEntry) {
                navHistory.add(currentNavEntry)
            }
            currentNavEntry = NavEntry(newScreen, currentTab)
        }
    }

    val navigateBack: () -> Unit = {
        if (navHistory.isNotEmpty()) {
            val previousEntry = navHistory.removeAt(navHistory.size - 1)
            currentNavEntry = previousEntry
        } else {
            if (currentScreen == AppScreen.MainTabs && currentTab == AppTab.Home) {
                showExitDialog = true
            } else {
                currentNavEntry = NavEntry(AppScreen.MainTabs, AppTab.Home)
            }
        }
    }

    // Capture system Back press using Compose BackHandler
    BackHandler(enabled = true) {
        navigateBack()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                fadeIn(animationSpec = tween(250)) togetherWith fadeOut(animationSpec = tween(250))
            },
            label = "screen_transition",
            modifier = Modifier.fillMaxSize()
        ) { screen ->
            when (screen) {
                is AppScreen.MainTabs -> MainTabsScreen(
                    viewModel = viewModel,
                    currentTab = currentTab,
                    onTabSelected = onTabSelected,
                    onNavigateToGame = onNavigateToGame
                )
                is AppScreen.SpinGame -> LuckySpinScreen(
                    viewModel = viewModel,
                    onBack = navigateBack
                )
                is AppScreen.ScratchGame -> LuckyScratchScreen(
                    viewModel = viewModel,
                    onBack = navigateBack
                )
                is AppScreen.TriviaGame -> TriviaQuizScreen(
                    viewModel = viewModel,
                    categoryId = screen.categoryId,
                    quizSetId = screen.quizSetId,
                    onBack = navigateBack
                )
                is AppScreen.MathGame -> MathSolverScreen(
                    viewModel = viewModel,
                    onBack = navigateBack
                )
                is AppScreen.UPIWithdraw -> UPIWithdrawScreen(
                    viewModel = viewModel,
                    onBack = navigateBack
                )
                is AppScreen.Wallet -> WalletScreen(
                    wallet = wallet,
                    transactions = transactions,
                    viewModel = viewModel,
                    coroutineScope = coroutineScope,
                    snackbarHostState = snackbarHostState,
                    onWithdrawClick = { onNavigateToGame(AppScreen.UPIWithdraw) },
                    onBack = navigateBack
                )
                is AppScreen.Referral -> ReferralScreen(
                    wallet = wallet,
                    viewModel = viewModel,
                    coroutineScope = coroutineScope,
                    snackbarHostState = snackbarHostState,
                    onBack = navigateBack
                )
            }
        }

        // Beautiful MD3 Exit Confirmation Dialog
        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
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
                            text = "Exit PlayWin?",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                },
                text = {
                    Text(
                        text = "Are you sure you want to exit? Your progress is safely stored offline and in Firebase.",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                },
                dismissButton = {
                    TextButton(
                        onClick = { showExitDialog = false }
                    ) {
                        Text("Cancel", color = Color(0xFF7C4DFF), fontWeight = FontWeight.Bold)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showExitDialog = false
                            val activity = context as? androidx.activity.ComponentActivity
                            activity?.finish()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF5252)
                        )
                    ) {
                        Text("Exit", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = Color(0xFF121212),
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}

fun sanitizeDoubleInput(old: String, new: String): String {
    if (new.length == old.length + 2) {
        var i = 0
        while (i < old.length && i < new.length && old[i] == new[i]) {
            i++
        }
        if (i < new.length - 1 && new[i] == new[i + 1]) {
            return old.substring(0, i) + new[i] + old.substring(i)
        }
    }
    return new
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: PlayWinViewModel) {
    var isSignUp by remember { mutableStateOf(false) }
    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val initialRememberMe = remember {
        val prefs = context.getSharedPreferences("playwin_prefs", android.content.Context.MODE_PRIVATE)
        prefs.getBoolean("remember_me", true)
    }
    var rememberMe by remember { mutableStateOf(initialRememberMe) }
    var isAuthenticating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var showResendButton by remember { mutableStateOf(false) }
    var isCheckingVerification by remember { mutableStateOf(false) }
    var isResending by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorDialogMsg by remember { mutableStateOf("") }
    
    var showResetDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var resetMessage by remember { mutableStateOf<String?>(null) }
    var isResetting by remember { mutableStateOf(false) }

    val userCreatedStatus by viewModel.userCreatedStatus.collectAsStateWithLifecycle()
    val verificationEmailStatus by viewModel.verificationEmailStatus.collectAsStateWithLifecycle()
    val verificationEmailError by viewModel.verificationEmailError.collectAsStateWithLifecycle()

    LaunchedEffect(isSignUp) {
        viewModel.clearDebugStatus()
        errorMessage = null
        successMessage = null
    }

    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F0C20),
                        Color(0xFF080612)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Glowing Background Circles
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopCenter)
                .padding(top = 50.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF7C4DFF).copy(alpha = 0.2f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Trophy Emoji Badge
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF1B192E))
                    .border(2.dp, Color(0xFF7C4DFF).copy(alpha = 0.6f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🏆",
                    fontSize = 42.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Brand Typography
            Text(
                text = "PLAY WIN",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 32.sp,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Play Mini Games • Earn Coins • Redeem Vouchers",
                color = Color(0xFF00E5FF),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF2E2C3D), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF12111A).copy(alpha = 0.9f)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isSignUp) "Create Account" else "Sign In",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = if (isSignUp) 
                            "Sign up now to start playing and claiming rewards." 
                            else "To sync your earnings securely, sign in to your profile.",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                    )

                    errorMessage?.let { error ->
                        Text(
                            text = error,
                            color = Color.Red,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    successMessage?.let { success ->
                        Text(
                            text = success,
                            color = Color(0xFF00E5FF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    if (isSignUp) {
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = sanitizeDoubleInput(displayName, it) },
                            label = { Text("Display Name") },
                            placeholder = { Text("John Doe") },
                            modifier = Modifier.fillMaxWidth().testTag("displayName_field"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF7C4DFF),
                                unfocusedBorderColor = Color(0xFF2E2C3D),
                                focusedLabelColor = Color(0xFF7C4DFF),
                                unfocusedLabelColor = Color.Gray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = sanitizeDoubleInput(email, it) },
                        label = { Text("Email Address") },
                        placeholder = { Text("your.email@gmail.com") },
                        modifier = Modifier.fillMaxWidth().testTag("email_field"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF7C4DFF),
                            unfocusedBorderColor = Color(0xFF2E2C3D),
                            focusedLabelColor = Color(0xFF7C4DFF),
                            unfocusedLabelColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = sanitizeDoubleInput(password, it) },
                        label = { Text("Password") },
                        placeholder = { Text("Min. 6 characters") },
                        modifier = Modifier.fillMaxWidth().testTag("password_field"),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF7C4DFF),
                            unfocusedBorderColor = Color(0xFF2E2C3D),
                            focusedLabelColor = Color(0xFF7C4DFF),
                            unfocusedLabelColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { rememberMe = !rememberMe }
                                .testTag("remember_me_row")
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF7C4DFF),
                                    uncheckedColor = Color(0xFF2E2C3D),
                                    checkmarkColor = Color.White
                                ),
                                modifier = Modifier.testTag("remember_me_checkbox")
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Remember Me",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (!isSignUp) {
                            Text(
                                text = "Forgot Password?",
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .clickable {
                                        errorMessage = null
                                        resetEmail = email
                                        resetMessage = null
                                        showResetDialog = true
                                    }
                                    .testTag("forgot_password_button")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (isAuthenticating) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF7C4DFF), modifier = Modifier.size(32.dp))
                        }
                    } else {
                        Button(
                            onClick = {
                                if (email.isBlank() || !email.contains("@")) {
                                    errorMessage = "Please enter a valid email address."
                                    return@Button
                                }
                                if (password.length < 6) {
                                    errorMessage = "Password must be at least 6 characters."
                                    return@Button
                                }
                                if (isSignUp && displayName.isBlank()) {
                                    errorMessage = "Please enter a Display Name."
                                    return@Button
                                }

                                errorMessage = null
                                successMessage = null
                                isAuthenticating = true
                                if (isSignUp) {
                                    viewModel.signUpWithEmailAndPassword(email, password, displayName) { success, msg ->
                                        isAuthenticating = false
                                        if (success) {
                                            successMessage = msg ?: "Verification email sent. Please verify your email before signing in."
                                            isSignUp = false // Auto-switch to Sign In page
                                            showResendButton = true
                                        } else {
                                            val errorMsg = msg ?: "Sign Up failed."
                                            errorMessage = errorMsg
                                            errorDialogMsg = errorMsg
                                            showErrorDialog = true
                                        }
                                    }
                                } else {
                                    viewModel.signInWithEmailAndPassword(email, password, rememberMe) { success, err ->
                                        isAuthenticating = false
                                        if (success) {
                                            // Handled by viewmodel auth state change
                                        } else {
                                            val errorMsg = err ?: "Sign In failed."
                                            errorMessage = errorMsg
                                            errorDialogMsg = errorMsg
                                            showErrorDialog = true
                                            if (err != null && (err.contains("verify") || err.contains("verified") || err.contains("Verification") || err.contains("email"))) {
                                                showResendButton = true
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag(if (isSignUp) "create_account_button" else "sign_in_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF7C4DFF)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (isSignUp) "Create Account" else "Sign In",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    if (showResendButton && !isSignUp) {
                        Spacer(modifier = Modifier.height(12.dp))
                        if (isResending) {
                            CircularProgressIndicator(color = Color(0xFF7C4DFF), modifier = Modifier.size(24.dp))
                        } else {
                            Button(
                                onClick = {
                                    isResending = true
                                    successMessage = null
                                    errorMessage = null
                                    viewModel.resendVerificationEmail(email, password) { success, msg ->
                                        isResending = false
                                        if (success) {
                                            successMessage = msg ?: "Verification email resent successfully!"
                                        } else {
                                            val errorMsg = msg ?: "Failed to resend email."
                                            errorMessage = errorMsg
                                            errorDialogMsg = errorMsg
                                            showErrorDialog = true
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF00E5FF),
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("resend_verification_email_button")
                            ) {
                                Text(
                                    text = "Resend Verification Email",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        if (isCheckingVerification) {
                            CircularProgressIndicator(color = Color(0xFF7C4DFF), modifier = Modifier.size(24.dp))
                        } else {
                            Button(
                                onClick = {
                                    isCheckingVerification = true
                                    successMessage = null
                                    errorMessage = null
                                    viewModel.checkEmailVerification(email, password, rememberMe) { success, msg ->
                                        isCheckingVerification = false
                                        if (success) {
                                            successMessage = "Email verified successfully!"
                                        } else {
                                            val errorMsg = msg ?: "Email not verified yet."
                                            errorMessage = errorMsg
                                            errorDialogMsg = errorMsg
                                            showErrorDialog = true
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4CAF50),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("ive_verified_my_email_button")
                            ) {
                                Text(
                                    text = "I've Verified My Email",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    if (userCreatedStatus != null || verificationEmailStatus != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFF2E2C3D), RoundedCornerShape(12.dp))
                                .testTag("firebase_verification_debug_card"),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1C2E)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "FIREBASE AUTH SYSTEM LOGS",
                                    color = Color(0xFF00E5FF),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    if (userCreatedStatus == true) {
                                        Text("✓", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("User created", color = Color.White, fontSize = 12.sp)
                                    } else if (userCreatedStatus == false) {
                                        Text("✗", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("User creation failed", color = Color.Red, fontSize = 12.sp)
                                    } else {
                                        CircularProgressIndicator(color = Color(0xFF7C4DFF), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Creating user Account...", color = Color.Gray, fontSize = 12.sp)
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    if (verificationEmailStatus == true) {
                                        Text("✓", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Verification email sent", color = Color.White, fontSize = 12.sp)
                                    } else if (verificationEmailStatus == false) {
                                        Text("✗", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Verification email failed", color = Color.Red, fontSize = 12.sp)
                                    } else {
                                        if (userCreatedStatus == true) {
                                            CircularProgressIndicator(color = Color(0xFF7C4DFF), modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Sending verification email...", color = Color.Gray, fontSize = 12.sp)
                                        } else {
                                            Text("○", color = Color.Gray, fontSize = 14.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Verification email pending", color = Color.Gray, fontSize = 12.sp)
                                        }
                                    }
                                }

                                verificationEmailError?.let { err ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Firebase Error: $err",
                                        color = Color.Red,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isSignUp) "Already have an account? " else "Don't have an account? ",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                        Text(
                            text = if (isSignUp) "Sign In" else "Create Account",
                            color = Color(0xFF7C4DFF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .clickable {
                                    isSignUp = !isSignUp
                                    errorMessage = null
                                    successMessage = null
                                    showResendButton = false
                                }
                                .testTag("toggle_login_mode")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Or Divider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(Color.Gray.copy(alpha = 0.2f))
                        )
                        Text(
                            text = "OR",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(Color.Gray.copy(alpha = 0.2f))
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Disabled temporarily Google Sign In Button
                    Button(
                        onClick = {
                            // Intentionally disabled temporarily for SHA-1 setup
                        },
                        enabled = false, // Google Sign-In button should remain visible but disabled for now.
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("google_sign_in_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.15f),
                            disabledContainerColor = Color.White.copy(alpha = 0.08f),
                            contentColor = Color.White.copy(alpha = 0.4f),
                            disabledContentColor = Color.White.copy(alpha = 0.25f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("G", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Black)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Continue with Google (Disabled)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = {
                Text(
                    text = "Authentication Error",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = errorDialogMsg,
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showErrorDialog = false }
                ) {
                    Text("OK", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF1E1C30),
            titleContentColor = Color.White,
            textContentColor = Color.LightGray,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { if (!isResetting) showResetDialog = false },
            title = {
                Text("Reset Password", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column {
                    Text(
                        text = "Enter your registered Email Address below to receive a password recovery link.",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Email Address") },
                        placeholder = { Text("your.email@gmail.com") },
                        modifier = Modifier.fillMaxWidth().testTag("reset_email_field"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF7C4DFF),
                            unfocusedBorderColor = Color(0xFF2E2C3D),
                            focusedLabelColor = Color(0xFF7C4DFF),
                            unfocusedLabelColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    resetMessage?.let { msg ->
                        Text(
                            text = msg,
                            color = if (msg.contains("Success", ignoreCase = true)) Color(0xFF00E5FF) else Color.Red,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                if (isResetting) {
                    CircularProgressIndicator(color = Color(0xFF7C4DFF), modifier = Modifier.size(24.dp))
                } else {
                    Button(
                        onClick = {
                            if (resetEmail.isBlank() || !resetEmail.contains("@")) {
                                resetMessage = "Please enter a valid email address."
                                return@Button
                            }
                            isResetting = true
                            resetMessage = null
                            viewModel.sendPasswordResetEmail(resetEmail) { success, err ->
                                isResetting = false
                                if (success) {
                                    resetMessage = "Success! Password recovery email sent successfully."
                                } else {
                                    resetMessage = err ?: "Failed to send recovery email."
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
                    ) {
                        Text("Send Link", color = Color.White)
                    }
                }
            },
            dismissButton = {
                if (!isResetting) {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("Cancel", color = Color.White)
                    }
                }
            },
            containerColor = Color(0xFF13111C),
            shape = RoundedCornerShape(16.dp)
        )
    }
}
  @OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabsScreen(
    viewModel: PlayWinViewModel,
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    onNavigateToGame: (AppScreen) -> Unit
) {
    val wallet by viewModel.walletState.collectAsStateWithLifecycle()
    val transactions by viewModel.transactionsState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showWatchAdDialog by remember { mutableStateOf(false) }

    // Dialog simulators
    if (showWatchAdDialog) {
        VideoAdSimulatorDialog(
            viewModel = viewModel,
            wallet = wallet,
            onDismiss = { showWatchAdDialog = false },
            snackbarHostState = snackbarHostState
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFF090615),
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0F0C1B),
                tonalElevation = 8.dp,
                modifier = Modifier
                    .height(84.dp)
                    .border(BorderStroke(0.5.dp, Color(0xFF7C4DFF).copy(alpha = 0.15f)), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .testTag("bottom_navigation_bar")
            ) {
                val tabs = listOf(
                    PlayWinNavigationItem(AppTab.Home, "Home", Icons.Default.Home),
                    PlayWinNavigationItem(AppTab.Quiz, "Quiz", Icons.Default.HelpOutline),
                    PlayWinNavigationItem(AppTab.Tasks, "Tasks", Icons.Default.Assignment),
                    PlayWinNavigationItem(AppTab.Coupons, "Coupons", Icons.Default.LocalActivity),
                    PlayWinNavigationItem(AppTab.Leaderboard, "Leaderboard", Icons.Default.EmojiEvents),
                    PlayWinNavigationItem(AppTab.Profile, "Profile", Icons.Default.Person)
                )

                tabs.forEach { navItem ->
                    val isSelected = currentTab == navItem.tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { onTabSelected(navItem.tab) },
                        alwaysShowLabel = true,
                        icon = {
                            Icon(
                                imageVector = navItem.icon,
                                contentDescription = navItem.title,
                                tint = if (isSelected) Color(0xFFFFD700) else Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = navItem.title,
                                color = if (isSelected) Color(0xFFFFD700) else Color.White.copy(alpha = 0.6f),
                                fontSize = 8.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Clip
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color(0xFF7C4DFF).copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF090615))
        ) {
            val allUsers by viewModel.allUsersState.collectAsStateWithLifecycle()
            val currentUser by viewModel.currentUserState.collectAsStateWithLifecycle()
            
            when (currentTab) {
                AppTab.Home -> HomeScreen(
                    wallet = wallet,
                    viewModel = viewModel,
                    coroutineScope = coroutineScope,
                    snackbarHostState = snackbarHostState,
                    onWatchAdClick = { showWatchAdDialog = true },
                    onInviteClick = { onNavigateToGame(AppScreen.Referral) },
                    onViewAllTasksClick = { onTabSelected(AppTab.Tasks) },
                    onProfileClick = { onTabSelected(AppTab.Profile) },
                    onNavigateToGame = onNavigateToGame,
                    onTabSelected = onTabSelected
                )
                AppTab.Quiz -> QuizArenaScreen(
                    viewModel = viewModel,
                    onNavigateToGame = onNavigateToGame
                )
                AppTab.Tasks -> TasksScreen(
                    wallet = wallet,
                    viewModel = viewModel,
                    coroutineScope = coroutineScope,
                    snackbarHostState = snackbarHostState,
                    onNavigateToGame = onNavigateToGame,
                    onWatchAdClick = { showWatchAdDialog = true },
                    onDailyCheckIn = {
                        val success = viewModel.claimDailyReward()
                        coroutineScope.launch {
                            if (success) {
                                snackbarHostState.showSnackbar("Awesome! Daily Check-In reward added to your Wallet.")
                            } else {
                                snackbarHostState.showSnackbar("You already claimed your active Daily Check-In today!")
                            }
                        }
                    }
                )
                AppTab.Coupons -> CouponsScreen(
                    wallet = wallet,
                    viewModel = viewModel,
                    coroutineScope = coroutineScope,
                    snackbarHostState = snackbarHostState
                )
                AppTab.Leaderboard -> LeaderboardScreen(
                    allUsers = allUsers,
                    currentUser = currentUser
                )
                AppTab.Profile -> ProfileScreen(
                    wallet = wallet,
                    transactions = transactions,
                    viewModel = viewModel,
                    coroutineScope = coroutineScope,
                    snackbarHostState = snackbarHostState
                )
            }
        }
    }
}

@Composable
fun QuizArenaScreen(
    viewModel: PlayWinViewModel,
    onNavigateToGame: (AppScreen) -> Unit
) {
    val quizzes by viewModel.quizzesState.collectAsStateWithLifecycle()
    val quizProgress by viewModel.quizProgressState.collectAsStateWithLifecycle()
    val completedQuizzes by viewModel.completedQuizzesState.collectAsStateWithLifecycle()
    val weeklyQuizProgress by viewModel.weeklyQuizProgressState.collectAsStateWithLifecycle()
    val dailyQuiz by viewModel.dailyQuizState.collectAsStateWithLifecycle()
    val currentDailyQuiz = dailyQuiz
    val context = androidx.compose.ui.platform.LocalContext.current
    val todayDate = viewModel.getLocalDateString()

    // 1. Reactive Server Time & Countdown
    val currentServerTime by com.playwin.app.data.repository.DailyResetManager.currentServerTime.collectAsStateWithLifecycle()
    val nextQuizResetTimestamp by viewModel.nextQuizResetTimestampState.collectAsStateWithLifecycle()

    val nextResetVal = if (nextQuizResetTimestamp > 0L) {
        nextQuizResetTimestamp
    } else {
        com.playwin.app.data.repository.DailyResetManager.getNextResetUtc(currentServerTime)
    }

    val diffMs = nextResetVal - currentServerTime
    val countdownText = if (diffMs <= 0) {
        "00:00:00"
    } else {
        val sec = (diffMs / 1000) % 60
        val min = (diffMs / (1000 * 60)) % 60
        val hr = (diffMs / (1000 * 60 * 60))
        String.format(java.util.Locale.US, "%02d:%02d:%02d", hr, min, sec)
    }

    // Determine today's day of week name using UTC SimpleDateFormat
    val sdfDay = remember {
        java.text.SimpleDateFormat("EEEE", java.util.Locale.US).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }
    }
    val todayDayName = sdfDay.format(java.util.Date(currentServerTime))

    // Real-time synchronization & Auto reset
    LaunchedEffect(diffMs <= 0) {
        if (diffMs <= 0) {
            val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
            if (currentUserId.isNotEmpty()) {
                com.playwin.app.data.repository.DailyResetManager.performDailyReset(currentUserId)
            }
        }
    }

    // App resume handler: refresh countdown / daily reset state on resume
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                if (currentUserId.isNotEmpty()) {
                    com.playwin.app.data.repository.DailyResetManager.performDailyReset(currentUserId)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val weekdayOrder = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val activeQuizzes = quizzes
        .filter { it.dayOfWeek.trim().isNotEmpty() }
        .distinctBy { it.dayOfWeek.lowercase().trim() }
        .sortedBy { quiz ->
            val index = weekdayOrder.indexOfFirst { it.equals(quiz.dayOfWeek.trim(), ignoreCase = true) }
            if (index != -1) index else 999
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF090615), Color(0xFF020108))))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Immersive header
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF1E1A30), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("⚔️", fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "QUIZ ARENA",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Unlock live challenges & earn amazing rewards!",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        }

        // Live Reset Countdown Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF13111C)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Next Quiz Reset In",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = countdownText,
                        color = Color(0xFF4CAF50),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFF4CAF50).copy(alpha = 0.15f), CircleShape)
                        .border(1.dp, Color(0xFF4CAF50).copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Timer",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Instruction / Info Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF181524)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Play Live Quizzes & Win Coins!",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Complete newly published quizzes to earn bonus coins. Completed quizzes can be replayed in review mode.",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }

        if (activeQuizzes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 64.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text("🎮", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Active Quizzes",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "New challenges are being prepared by admin. Check back soon!",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            activeQuizzes.forEach { quiz ->
                val isTodayQuiz = quiz.dayOfWeek.trim().equals(todayDayName, ignoreCase = true)
                val isCompletedToday = currentDailyQuiz != null && currentDailyQuiz.completed && currentDailyQuiz.lastCompletedDate == todayDate

                val cardStatus = if (isTodayQuiz) {
                    if (isCompletedToday) "COMPLETED_TODAY" else "AVAILABLE"
                } else {
                    "LOCKED"
                }

                val color = if (isTodayQuiz) Color(0xFF4CAF50) else Color.Gray
                val isClickable = isTodayQuiz

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .testTag("quiz_card_${quiz.id}")
                        .then(
                            if (isClickable) {
                                Modifier.clickable {
                                    onNavigateToGame(AppScreen.TriviaGame(quiz.categoryId.ifEmpty { quiz.category }, quiz.id))
                                }
                            } else {
                                Modifier
                            }
                        ),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF13111C)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        1.dp,
                        when (cardStatus) {
                            "AVAILABLE" -> Color(0xFF7C4DFF).copy(alpha = 0.6f)
                            "COMPLETED_TODAY" -> Color(0xFF4CAF50).copy(alpha = 0.4f)
                            else -> Color.Gray.copy(alpha = 0.3f)
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Top row: Icon, Title & Badge, Action Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Icon container
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        color.copy(alpha = 0.15f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        1.dp,
                                        color.copy(alpha = 0.5f),
                                        RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                val categoryText = quiz.categoryId.ifEmpty { quiz.category }
                                val emoji = when (categoryText.lowercase()) {
                                    "gk" -> "🧠"
                                    "sports" -> "⚽"
                                    "movies", "cinema" -> "🎬"
                                    "science" -> "🧪"
                                    "history" -> "🏛️"
                                    "tech", "technology" -> "💻"
                                    else -> "🌟"
                                }
                                Text(
                                    text = if (quiz.icon.isNotEmpty() && !quiz.icon.startsWith("http")) quiz.icon else emoji,
                                    fontSize = 22.sp
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            // Title & Badge Column
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "${quiz.title} (${quiz.dayOfWeek})",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                // Status Badge
                                Box(
                                    modifier = Modifier
                                        .background(
                                            when (cardStatus) {
                                                "AVAILABLE" -> Color(0xFF9C27B0).copy(alpha = 0.15f)
                                                "COMPLETED_TODAY" -> Color(0xFF2E7D32).copy(alpha = 0.15f)
                                                "COMPLETED" -> Color(0xFF2196F3).copy(alpha = 0.15f)
                                                else -> Color.Gray.copy(alpha = 0.15f)
                                            },
                                            RoundedCornerShape(100.dp)
                                        )
                                        .border(
                                            0.5.dp,
                                            when (cardStatus) {
                                                "AVAILABLE" -> Color(0xFF9C27B0)
                                                "COMPLETED_TODAY" -> Color(0xFF4CAF50)
                                                "COMPLETED" -> Color(0xFF2196F3)
                                                else -> Color.Gray
                                            },
                                            RoundedCornerShape(100.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = when (cardStatus) {
                                            "AVAILABLE" -> "Available"
                                            "COMPLETED_TODAY" -> "Completed Today ✔"
                                            "COMPLETED" -> "Completed ✔"
                                            else -> "Locked"
                                        },
                                        color = when (cardStatus) {
                                            "AVAILABLE" -> Color(0xFFAB47BC)
                                            "COMPLETED_TODAY" -> Color(0xFF66BB6A)
                                            "COMPLETED" -> Color(0xFF42A5F5)
                                            else -> Color.Gray
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            // Play Button on the Right
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (cardStatus == "AVAILABLE") Color(0xFF9C27B0).copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.15f),
                                        CircleShape
                                    )
                                    .border(
                                        1.dp,
                                        if (cardStatus == "AVAILABLE") Color(0xFF9C27B0).copy(alpha = 0.5f) else Color.Gray.copy(alpha = 0.3f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (cardStatus == "AVAILABLE") Icons.Default.PlayArrow else Icons.Default.Lock,
                                    contentDescription = if (cardStatus == "AVAILABLE") "Play" else "Locked",
                                    tint = if (cardStatus == "AVAILABLE") Color(0xFFAB47BC) else Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Description
                        if (quiz.description.isNotEmpty()) {
                            Text(
                                text = quiz.description,
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        
                        // Metadata: Category, Timer, Difficulty
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Category badge
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF221F33), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Category: ${quiz.categoryId.ifEmpty { quiz.category }.uppercase()}",
                                    color = Color.Gray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                            
                            // Difficulty badge
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF221F33), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = quiz.difficulty.uppercase(),
                                    color = Color.Gray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                            
                            // Timer badge
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF221F33), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "⏱️ ${quiz.timerSeconds}s",
                                    color = Color.Gray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                        }
                        
                        if (cardStatus == "COMPLETED_TODAY") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF2E7D32).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .border(0.5.dp, Color(0xFF4CAF50).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "Come back tomorrow for the next quiz.",
                                    color = Color(0xFF4CAF50),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f), thickness = 1.dp)
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Reward section and Question count
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Reward section
                            Text(
                                text = "🏆 ${if (quiz.rewardPerQuestion > 0) quiz.rewardPerQuestion else quiz.rewardCoins} Coins/Correct • Perfect +${if (quiz.passBonus > 0) quiz.passBonus else quiz.completionBonus}",
                                color = if (cardStatus == "AVAILABLE") Color(0xFFAB47BC) else Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            // Question count
                            Text(
                                text = "${quiz.questions.size} Questions",
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

data class QuizSet(
    val index: Int,
    val title: String,
    val categoryId: String,
    val description: String,
    val emoji: String,
    val color: Color,
    val dayName: String
)

@Composable
fun LeaderboardScreen(
    allUsers: List<com.playwin.app.data.model.FirebaseUser>,
    currentUser: com.playwin.app.data.model.FirebaseUser?
) {
    val sortedUsers = remember(allUsers) {
        allUsers.sortedByDescending { it.coins }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF090615), Color(0xFF020108))))
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF2C220B), CircleShape)
                    .border(1.dp, Color(0xFFFFD700), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🏆", fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "GLOBAL LEADERBOARD",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "The absolute best players on Play Win",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        }

        if (sortedUsers.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFFFD700))
            }
        } else {
            Column(modifier = Modifier.weight(1f)) {
                // Top 3 Podium
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Rank 2
                    if (sortedUsers.size > 1) {
                        val user2 = sortedUsers[1]
                        PodiumColumn(user = user2, rank = 2, scale = 0.9f, color = Color(0xFFC0C0C0))
                    }
                    // Rank 1
                    if (sortedUsers.size > 0) {
                        val user1 = sortedUsers[0]
                        PodiumColumn(user = user1, rank = 1, scale = 1.1f, color = Color(0xFFFFD700))
                    }
                    // Rank 3
                    if (sortedUsers.size > 2) {
                        val user3 = sortedUsers[2]
                        PodiumColumn(user = user3, rank = 3, scale = 0.85f, color = Color(0xFFCD7F32))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // List of rest
                val listUsers = sortedUsers.drop(3)
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    itemsIndexed(listUsers) { index, user ->
                        val realRank = index + 4
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(Color(0xFF13111C), RoundedCornerShape(12.dp))
                                .border(0.5.dp, Color(0xFF7C4DFF).copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(Color(0xFF1E1C28), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "#$realRank",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFF7C4DFF).copy(alpha = 0.15f), CircleShape)
                                        .border(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.3f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val fallback = if (user.displayName.isNotEmpty()) user.displayName.first().uppercase() else "P"
                                    Text(fallback, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = user.displayName.ifEmpty { "Player" },
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        if (user.level >= 10) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Verified",
                                                tint = Color(0xFF00E5FF),
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Level ${user.level}",
                                        color = Color.Gray,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = "Coins",
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = String.format("%,d", user.coins),
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating user rank card at the bottom
        currentUser?.let { user ->
            val myRank = sortedUsers.indexOfFirst { it.uid == user.uid } + 1
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E1A30))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFFFD700).copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, Color(0xFFFFD700), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (myRank > 0) "#$myRank" else "-", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "My Position", color = Color.Gray, fontSize = 11.sp)
                        Text(text = user.displayName.ifEmpty { "Player" }, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.MonetizationOn, contentDescription = "Coins", tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = String.format("%,d", user.coins), color = Color(0xFFFFD700), fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun PodiumColumn(
    user: com.playwin.app.data.model.FirebaseUser,
    rank: Int,
    scale: Float,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.scale(scale)
    ) {
        Box(contentAlignment = Alignment.TopCenter) {
            // Crown for 1st place
            if (rank == 1) {
                Text(
                    "👑",
                    fontSize = 24.sp,
                    modifier = Modifier.offset(y = (-18).dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(Color(0xFF13111C), CircleShape)
                    .border(2.dp, color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val name = user.displayName.ifEmpty { "Player" }
                val char = if (name.isNotEmpty()) name.first().uppercase() else "P"
                Text(char, color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp)
            }
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(color, CircleShape)
                    .border(1.dp, Color.Black, CircleShape)
                    .align(Alignment.BottomCenter)
                    .offset(y = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(rank.toString(), color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = user.displayName.ifEmpty { "Player" },
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(72.dp),
            textAlign = TextAlign.Center
        )
        Text(
            text = "${user.coins} Coins",
            color = Color(0xFFFFD700),
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp
        )
    }
}

data class PlayWinNavigationItem(val tab: AppTab, val title: String, val icon: ImageVector)

// --- HOME SCREEN ---
@Composable
fun HomeScreen(
    wallet: com.playwin.app.data.model.UserWallet,
    viewModel: PlayWinViewModel,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    snackbarHostState: SnackbarHostState,
    onWatchAdClick: () -> Unit,
    onInviteClick: () -> Unit,
    onViewAllTasksClick: () -> Unit,
    onProfileClick: () -> Unit,
    onNavigateToGame: (AppScreen) -> Unit,
    onTabSelected: (AppTab) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.refreshDailyCheckInSettings()
    }
    val currentUser by viewModel.currentUserState.collectAsStateWithLifecycle()
    val scratchSettings by viewModel.scratchCardSettingsState.collectAsStateWithLifecycle()
    val scratchState by viewModel.userScratchCardStateState.collectAsStateWithLifecycle()
    val currentServerTime by com.playwin.app.data.repository.DailyResetManager.currentServerTime.collectAsStateWithLifecycle()
    
    // Dynamic Scratch card variables
    val scratchesToday = scratchState.scratchesToday
    val scratchDailyLimit = scratchSettings.dailyScratchLimit
    val remainingScratches = maxOf(0, scratchDailyLimit - scratchesToday)
    val scratchEnabled = scratchSettings.enabled
    
    // Cooldown
    val lastScratchTime = scratchState.lastScratchTimestamp
    val cooldownDurationMs = scratchSettings.rewardCooldownMinutes * 60 * 1000L
    val isCooldownActive = lastScratchTime > 0L && currentServerTime < (lastScratchTime + cooldownDurationMs)
    
    val cooldownSecondsLeft = if (isCooldownActive) {
        maxOf(0L, (lastScratchTime + cooldownDurationMs - currentServerTime) / 1000L)
    } else {
        0L
    }

    val scratchDesc = when {
        !scratchEnabled -> "Temporarily Disabled"
        isCooldownActive -> String.format("Cooldown: %02dm:%02ds", cooldownSecondsLeft / 60, cooldownSecondsLeft % 60)
        remainingScratches <= 0 -> "Daily Limit Reached ($scratchDailyLimit)"
        else -> "$remainingScratches Left • Today: $scratchesToday/$scratchDailyLimit"
    }
    
    // Dynamic user display name from Realtime Database or Auth, with Player fallback
    val displayName = currentUser?.displayName?.ifEmpty { null }
        ?: com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.displayName?.ifEmpty { null }
        ?: "Player"
    
    // Add debug log for loaded display name
    android.util.Log.d("PlayWinVM", "Display Name Loaded: $displayName")

    val coinBalance = currentUser?.coins ?: wallet.coins
    val currentLevel = currentUser?.level ?: 1
    val dailyStreak = currentUser?.streak ?: currentUser?.dailyStreak ?: wallet.dailyStreak
    val lastCheckInTime = currentUser?.lastCheckInTime ?: wallet.lastCheckInTime
    val profilePhotoUrl = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.photoUrl?.toString()

    // Dialog state variables
    var showQuizChoiceDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }

    val activity = context as? android.app.Activity

    var showRewardedAdDialog by remember { mutableStateOf(false) }
    var adLoadingState by remember { mutableStateOf(false) }
    var adFeedbackMessage by remember { mutableStateOf<String?>(null) }

    var adCardSubtitle by remember { mutableStateOf("Watch Reward Ad (+50 Coins)") }
    var adCooldownActive by remember { mutableStateOf(false) }

    LaunchedEffect(wallet.lastRewardAdTime, wallet.dailyAdsWatched) {
        while (true) {
            val now = System.currentTimeMillis()
            val elapsed = now - wallet.lastRewardAdTime
            val cooldownDuration = 1 * 60 * 1000L
            if (wallet.dailyAdsWatched >= 10) {
                adCooldownActive = false
                adCardSubtitle = "Daily Reward Ad Limit Reached. Come Back Tomorrow."
            } else if (wallet.lastRewardAdTime != 0L && elapsed < cooldownDuration) {
                adCooldownActive = true
                val remainingSec = (cooldownDuration - elapsed) / 1000
                val min = remainingSec / 60
                val sec = remainingSec % 60
                adCardSubtitle = String.format("Next Reward Ad Available In: %02d:%02d", min, sec)
            } else {
                adCooldownActive = false
                adCardSubtitle = "Watch Reward Ad (+50 Coins)"
            }
            delay(1000L)
        }
    }

    if (showRewardedAdDialog) {
        AlertDialog(
            onDismissRequest = { 
                if (!adLoadingState) showRewardedAdDialog = false 
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🎬 Watch Rewarded Ad",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (adLoadingState) {
                        CircularProgressIndicator(
                            color = Color(0xFFD32F2F),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading Ad...",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    } else if (adFeedbackMessage != null) {
                        Text(
                            text = adFeedbackMessage ?: "",
                            color = Color.White,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = "Watch a short ad completely to earn 50 coins.",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            confirmButton = {
                if (!adLoadingState) {
                    if (adFeedbackMessage != null) {
                        Button(
                            onClick = {
                                adFeedbackMessage = null
                                showRewardedAdDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD32F2F)
                            )
                        ) {
                            Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                if (activity != null) {
                                    adLoadingState = true
                                    adFeedbackMessage = null
                                    
                                    val adRequest = com.google.android.gms.ads.AdRequest.Builder().build()
                                    val adUnitId = "ca-app-pub-3940256099942544/5224354917"
                                    
                                    com.google.android.gms.ads.rewarded.RewardedAd.load(
                                        activity,
                                        adUnitId,
                                        adRequest,
                                        object : com.google.android.gms.ads.rewarded.RewardedAdLoadCallback() {
                                            override fun onAdLoaded(ad: com.google.android.gms.ads.rewarded.RewardedAd) {
                                                adLoadingState = false
                                                
                                                ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                                                    override fun onAdDismissedFullScreenContent() {
                                                        android.util.Log.d("PlayWinAds", "Ad dismissed")
                                                        if (adFeedbackMessage == null) {
                                                            showRewardedAdDialog = false
                                                        }
                                                    }
                                                    override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                                                        android.util.Log.e("PlayWinAds", "Failed to show ad: ${error.message}")
                                                        adFeedbackMessage = "Rewarded Ad failed to display.\nPlease try again later."
                                                    }
                                                }
                                                
                                                ad.show(activity, com.google.android.gms.ads.OnUserEarnedRewardListener { rewardItem ->
                                                    viewModel.claimRewardedAdReward { success, errMsg ->
                                                        if (success) {
                                                            adFeedbackMessage = "Success! 50 Coins added to your Wallet."
                                                        } else {
                                                            adFeedbackMessage = errMsg ?: "Failed to claim reward."
                                                        }
                                                    }
                                                })
                                            }

                                            override fun onAdFailedToLoad(loadAdError: com.google.android.gms.ads.LoadAdError) {
                                                adLoadingState = false
                                                android.util.Log.e("PlayWinAds", "Failed to load ad: ${loadAdError.message}")
                                                adFeedbackMessage = "Rewarded Ad not available.\nPlease try again later."
                                            }
                                        }
                                    )
                                } else {
                                    adFeedbackMessage = "Internal error. Activity context unavailable."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD32F2F)
                            )
                        ) {
                            Text("Watch Ad", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            dismissButton = {
                if (!adLoadingState && adFeedbackMessage == null) {
                    TextButton(
                        onClick = { showRewardedAdDialog = false }
                    ) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                    }
                }
            },
            containerColor = Color(0xFF16141F),
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Dialog Rendering
    if (showQuizChoiceDialog) {
        QuizChoiceDialog(
            onDismiss = { showQuizChoiceDialog = false },
            onNavigateToGame = onNavigateToGame
        )
    }

    if (showNotificationsDialog) {
        NotificationsDialog(
            onDismiss = { showNotificationsDialog = false },
            onDailyCheckInClick = {
                val success = viewModel.claimDailyReward()
                coroutineScope.launch {
                    if (success) {
                        snackbarHostState.showSnackbar("Awesome! 5 Coins added to your Wallet.")
                    } else {
                        snackbarHostState.showSnackbar("You already claimed your active Daily Check-In today!")
                    }
                }
            },
            onInviteClick = onInviteClick
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF090615), Color(0xFF020108))))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // --- HEADER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // PLAY WIN Logo with Crown
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box {
                    // Crown icon floating on top
                    Text(
                        text = "👑",
                        fontSize = 14.sp,
                        modifier = Modifier.offset(x = 64.dp, y = (-12).dp)
                    )
                    Row {
                        Text(
                            text = "PLAY ",
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "WIN",
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFD700),
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }
            }
            
            // Notification bell with red circle badge showing "3"
            IconButton(
                onClick = { showNotificationsDialog = true },
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFF13111C), CircleShape)
                            .border(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    // Red circle badge with "3"
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(Color(0xFFFF3B30), CircleShape)
                            .border(1.5.dp, Color(0xFF090615), CircleShape)
                            .align(Alignment.TopEnd)
                            .offset(x = 2.dp, y = (-2).dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "3",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // --- PROFILE SECTION ---
        HomeTopSection(
            displayName = displayName,
            coinBalance = coinBalance,
            level = currentLevel,
            streak = dailyStreak,
            profilePhotoUrl = profilePhotoUrl,
            onNotificationClick = { showNotificationsDialog = true },
            onProfileClick = onProfileClick
        )

        Spacer(modifier = Modifier.height(12.dp))

        // --- DAILY CHECK-IN (glowing purple container) ---
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isWide = maxWidth >= 600.dp
            
            val userCheckIn by viewModel.userDailyCheckInState.collectAsStateWithLifecycle()
            val checkInSettings by viewModel.dailyCheckInSettingsState.collectAsStateWithLifecycle()
            
            val currentServerTime by com.playwin.app.data.repository.DailyResetManager.currentServerTime.collectAsStateWithLifecycle()
            val remainingTime by com.playwin.app.data.repository.DailyResetManager.remainingTime.collectAsStateWithLifecycle()

            val isEligibleToClaim = remember(userCheckIn?.lastClaimTimestamp, currentServerTime) {
                val lastClaim = userCheckIn?.lastClaimTimestamp ?: 0L
                val startOfToday = com.playwin.app.data.repository.DailyResetManager.getStartOfTodayUtc(currentServerTime)
                lastClaim < startOfToday
            }
            val countdownText = if (isEligibleToClaim) "" else "$remainingTime remaining."

            val checkInLoading by viewModel.dailyCheckInLoadingState.collectAsStateWithLifecycle()
            val rewardsList = checkInSettings?.rewards
            val isConfigAvailable = checkInSettings != null && rewardsList != null && rewardsList.size >= 7

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF320854), Color(0xFF09070F))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    1.5.dp,
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFE212D1), Color(0xFF7C4DFF))
                    )
                )
            ) {
                if (checkInLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF7C4DFF))
                    }
                } else if (!isConfigAvailable) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Configuration unavailable",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Configuration unavailable",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    val dayRewards = rewardsList!!
                if (isWide) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // LEFT SIDE: Text + Button
                        Column(
                            modifier = Modifier.weight(1.1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Daily Check-in",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Clip
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isEligibleToClaim) "Claim your daily reward!" else countdownText,
                                color = if (isEligibleToClaim) Color.White.copy(alpha = 0.7f) else Color(0xFFFFEA3D),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Clip
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            val buttonBrush = if (!isEligibleToClaim) {
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFF4B5563), Color(0xFF1F2937))
                                )
                            } else {
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFFFFEA3D), Color(0xFFFFAE00))
                                )
                            }

                            Button(
                                onClick = {
                                    viewModel.claimDailyReward { success, errorMsg ->
                                        coroutineScope.launch {
                                            if (success) {
                                                snackbarHostState.showSnackbar("Daily check-in successful! Reward claimed.")
                                            } else {
                                                snackbarHostState.showSnackbar(errorMsg ?: "Failed to claim reward.")
                                            }
                                        }
                                    }
                                },
                                enabled = isEligibleToClaim,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = Color.Transparent),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(),
                                modifier = Modifier
                                    .height(44.dp)
                                    .fillMaxWidth()
                                    .background(
                                        brush = buttonBrush,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Text(
                                    text = if (!isEligibleToClaim) "CHECKED IN" else "CHECK IN NOW",
                                    color = if (!isEligibleToClaim) Color.White.copy(alpha = 0.6f) else Color.Black,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Clip
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(24.dp))
                        
                        // RIGHT SIDE: Exactly 7 reward boxes
                        Row(
                            modifier = Modifier.weight(2.5f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val days = listOf(
                                "Day 1" to dayRewards[0],
                                "Day 2" to dayRewards[1],
                                "Day 3" to dayRewards[2],
                                "Day 4" to dayRewards[3],
                                "Day 5" to dayRewards[4],
                                "Day 6" to dayRewards[5],
                                "Day 7" to dayRewards[6]
                            )
                            days.forEachIndexed { index, (dayName, reward) ->
                                val dayNum = index + 1
                                val currentDay = userCheckIn?.currentDay ?: 0
                                val lastClaim = userCheckIn?.lastClaimTimestamp ?: 0L
                                val serverTime = currentServerTime
                                val status = getNewCheckInDayStatus(dayNum, currentDay, lastClaim, serverTime)
                                DailyRewardBox(
                                    day = dayName,
                                    rewardCoins = reward,
                                    status = status,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                } else {
                    // STACKED RESPONSIVE LAYOUT FOR PHONES
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val buttonBrush = if (!isEligibleToClaim) {
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF4B5563), Color(0xFF1F2937))
                            )
                        } else {
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFFFEA3D), Color(0xFFFFAE00))
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Daily Check-in",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Clip
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isEligibleToClaim) "Claim your daily reward!" else countdownText,
                                    color = if (isEligibleToClaim) Color.White.copy(alpha = 0.7f) else Color(0xFFFFEA3D),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Clip
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Button(
                                onClick = {
                                    viewModel.claimDailyReward { success, errorMsg ->
                                        coroutineScope.launch {
                                            if (success) {
                                                snackbarHostState.showSnackbar("Daily check-in successful! Reward claimed.")
                                            } else {
                                                snackbarHostState.showSnackbar(errorMsg ?: "Failed to claim reward.")
                                            }
                                        }
                                    }
                                },
                                enabled = isEligibleToClaim,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = Color.Transparent),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp),
                                modifier = Modifier
                                    .height(38.dp)
                                    .background(
                                        brush = buttonBrush,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                            ) {
                                Text(
                                    text = if (!isEligibleToClaim) "CHECKED IN" else "CHECK IN NOW",
                                    color = if (!isEligibleToClaim) Color.White.copy(alpha = 0.6f) else Color.Black,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Clip
                                )
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val days = listOf(
                                "Day 1" to dayRewards[0],
                                "Day 2" to dayRewards[1],
                                "Day 3" to dayRewards[2],
                                "Day 4" to dayRewards[3],
                                "Day 5" to dayRewards[4],
                                "Day 6" to dayRewards[5],
                                "Day 7" to dayRewards[6]
                            )
                            days.forEachIndexed { index, (dayName, reward) ->
                                val dayNum = index + 1
                                val currentDay = userCheckIn?.currentDay ?: 0
                                val lastClaim = userCheckIn?.lastClaimTimestamp ?: 0L
                                val serverTime = currentServerTime
                                val status = getNewCheckInDayStatus(dayNum, currentDay, lastClaim, serverTime)
                                DailyRewardBox(
                                    day = dayName,
                                    rewardCoins = reward,
                                    status = status,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- FEATURE GRID (Exactly 3 columns, 3 rows) ---
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Row 1: Watch Ads, Quiz Arena, Spin Wheel
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    HomeFeatureCard(
                        title = "Watch Ads",
                        description = adCardSubtitle,
                        emoji = "🎬",
                        color = Color(0xFFD32F2F),
                        onClick = {
                            if (wallet.dailyAdsWatched >= 10) {
                                android.widget.Toast.makeText(context, "Daily Reward Ad Limit Reached. Come Back Tomorrow.", android.widget.Toast.LENGTH_SHORT).show()
                            } else if (adCooldownActive) {
                                val elapsed = System.currentTimeMillis() - wallet.lastRewardAdTime
                                val remainingSec = (1 * 60 * 1000L - elapsed) / 1000
                                val min = remainingSec / 60
                                val sec = remainingSec % 60
                                val timeStr = String.format("%02d:%02d", min, sec)
                                android.widget.Toast.makeText(context, "Next Reward Ad Available In: $timeStr", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                showRewardedAdDialog = true
                            }
                        }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    HomeFeatureCard(
                        title = "Quiz Arena",
                        description = "Play Quizzes & Earn Coins",
                        emoji = "🧠",
                        color = Color(0xFF7C4DFF),
                        onClick = { onTabSelected(AppTab.Quiz) }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    HomeFeatureCard(
                        title = "Spin Wheel",
                        description = "1 Free Spin Daily",
                        emoji = "🎡",
                        color = Color(0xFFFF9E00),
                        onClick = { onNavigateToGame(AppScreen.SpinGame) }
                    )
                }
            }

            // Row 2: Scratch Card, Coupon Store, Refer & Earn
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    HomeFeatureCard(
                        title = "Scratch Card",
                        description = scratchDesc,
                        emoji = if (!scratchEnabled) "🔒" else "🎫",
                        color = if (!scratchEnabled) Color.Gray else Color(0xFF7C4DFF),
                        onClick = { onNavigateToGame(AppScreen.ScratchGame) }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    HomeFeatureCard(
                        title = "Coupon Store",
                        description = "Redeem Coins for Exciting Coupons",
                        emoji = "🎁",
                        color = Color(0xFFEC008C),
                        onClick = { onTabSelected(AppTab.Coupons) }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    HomeFeatureCard(
                        title = "Refer & Earn",
                        description = "Invite Friends & Earn Rewards",
                        emoji = "🤝",
                        color = Color(0xFF2979FF),
                        onClick = { onNavigateToGame(AppScreen.Referral) }
                    )
                }
            }

            // Row 3: Daily Tasks, Leaderboard, Wallet
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    HomeFeatureCard(
                        title = "Daily Tasks",
                        description = "Complete Tasks & Earn Rewards",
                        emoji = "📋",
                        color = Color(0xFF00E5FF),
                        onClick = { onTabSelected(AppTab.Tasks) }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    HomeFeatureCard(
                        title = "Leaderboard",
                        description = "See Top Players & Compete",
                        emoji = "🏆",
                        color = Color(0xFF00C853),
                        onClick = { onTabSelected(AppTab.Leaderboard) }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    HomeFeatureCard(
                        title = "Wallet",
                        description = "Coins, History & Transactions",
                        emoji = "💳",
                        color = Color(0xFFFFD700),
                        onClick = { onNavigateToGame(AppScreen.Wallet) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- BOTTOM BANNER (AD) ---
        PremiumAdBanner()
    }
}

@Composable
fun PremiumAdBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13111C)),
        border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.25f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF2C1B4D), Color(0xFF13111C))
                    )
                )
        ) {
            // "AD" label top right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(bottomStart = 8.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("AD", color = Color.White.copy(alpha = 0.8f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }

            // Left Side contents
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1.2f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Play More ",
                            color = Color(0xFFEC008C),
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Earn More",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Win exciting real reward vouchers today!",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFFFDF00), Color(0xFFFFA500))
                                ),
                                RoundedCornerShape(100.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "PLAY NOW",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 9.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Right Side contents (Pile of Gold Coins & Premium Gift Box drawings via emojis)
                Box(
                    modifier = Modifier
                        .weight(0.8f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(Color(0xFF7C4DFF).copy(alpha = 0.1f), CircleShape)
                            .border(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎁", fontSize = 32.sp)
                    }
                    Text(
                        "🪙",
                        fontSize = 20.sp,
                        modifier = Modifier
                            .offset(x = (-42).dp, y = 14.dp)
                    )
                    Text(
                        "🪙",
                        fontSize = 24.sp,
                        modifier = Modifier
                            .offset(x = (-28).dp, y = 18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ActionTaskCardItem(
    title: String,
    rewardCoins: Int,
    progress: String,
    type: String,
    onGoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13111C)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, Color(0xFF7C4DFF).copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val (bgColor, iconColor, icon) = when (type.lowercase()) {
                    "profile", "task_profile" -> Triple(Color(0xFF7C4DFF).copy(alpha = 0.15f), Color(0xFF7C4DFF), Icons.Default.Assignment)
                    "share", "task_share" -> Triple(Color(0xFF00C853).copy(alpha = 0.15f), Color(0xFF00C853), Icons.Default.Share)
                    "video", "task_video", "video_ad" -> Triple(Color(0xFFFF3D00).copy(alpha = 0.15f), Color(0xFFFF3D00), Icons.Default.PlayArrow)
                    "referral", "invite", "task_invite", "invite_200" -> Triple(Color(0xFF00E5FF).copy(alpha = 0.15f), Color(0xFF00E5FF), Icons.Default.CardGiftcard)
                    "trivia", "quiz", "quizzes" -> Triple(Color(0xFF7C4DFF).copy(alpha = 0.15f), Color(0xFF7C4DFF), Icons.Default.Quiz)
                    "streak" -> Triple(Color(0xFFFFD700).copy(alpha = 0.15f), Color(0xFFFFD700), Icons.Default.DateRange)
                    else -> Triple(Color(0xFF7C4DFF).copy(alpha = 0.15f), Color(0xFF7C4DFF), Icons.Default.Assignment)
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Progress: $progress",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "+$rewardCoins",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = "Coin Icon",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Button(
                    onClick = onGoClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFFFFD700)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.6f)),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("go_button_${title.replace(" ", "_").lowercase()}")
                ) {
                    Text(
                        text = "Go",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun AutoScalingText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    maxLines: Int = 1,
    minFontSize: TextUnit = 8.sp
) {
    var scaledTextStyle by remember(text) { mutableStateOf(style) }
    var readyToDraw by remember(text) { mutableStateOf(false) }

    Text(
        text = text,
        style = scaledTextStyle,
        maxLines = maxLines,
        overflow = TextOverflow.Clip,
        modifier = modifier.drawWithContent {
            if (readyToDraw) {
                drawContent()
            }
        },
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.hasVisualOverflow && scaledTextStyle.fontSize > minFontSize) {
                scaledTextStyle = scaledTextStyle.copy(fontSize = scaledTextStyle.fontSize * 0.9f)
            } else {
                readyToDraw = true
            }
        }
    )
}

@Composable
fun HomeTopSection(
    displayName: String,
    coinBalance: Int,
    level: Int,
    streak: Int,
    profilePhotoUrl: String?,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val firstLetter = if (displayName.isNotEmpty()) displayName.first().uppercaseChar().toString() else "P"
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        // --- PROFILE SECTION ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left: Large Avatar + Username + Level/XP
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1.2f)
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp) // Large avatar as per reference
                        .clickable { onProfileClick() }
                ) {
                    // Avatar Circle with gradient background and thick purple neon border
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(BorderStroke(2.5.dp, Color(0xFF7B2CF7)), CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFF7B2CF7).copy(alpha = 0.5f), Color(0xFF0F0C1B))
                                ),
                                shape = CircleShape
                            )
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        // Illustrated character emoji (a cool boy profile)
                        Text(
                            text = "👦",
                            fontSize = 38.sp
                        )
                    }
                    
                    // Edit pen badge at bottom right
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.BottomEnd)
                            .background(Color(0xFF7B2CF7), CircleShape)
                            .border(1.dp, Color(0xFF090615), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Name and level details
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AutoScalingText(
                            text = displayName,
                            style = TextStyle(
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            ),
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        // Verified badge (purple circle with check)
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(Color(0xFF7B2CF7), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Verified Profile",
                                tint = Color.White,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Level badge & XP text in a row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF2563EB), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Level $level",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        
                        Text(
                            text = "${850 + level * 5} / ${1200 + level * 10} XP",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    // XP Progress bar
                    Box(
                        modifier = Modifier
                            .width(130.dp)
                            .height(6.dp)
                            .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(100.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = 0.71f)
                                .background(
                                    Brush.horizontalGradient(listOf(Color(0xFF7B2CF7), Color(0xFFA855F7))),
                                    RoundedCornerShape(100.dp)
                                )
                        )
                    }
                }
            }
            
            // Right: "My Coins" Wallet Card
            Card(
                modifier = Modifier
                    .weight(0.8f)
                    .clickable { onProfileClick() },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF13111C)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Gold Coin Circle
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFFFD700).copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🪙", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "My Coins",
                                color = Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = String.format("%,d", coinBalance),
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                    }
                    
                    // Plus button (purple circle)
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .background(Color(0xFF7B2CF7), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "＋",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // --- STATS CARD (One horizontal premium card with 4 metrics) ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0A1B)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFFFFFFF).copy(alpha = 0.05f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stat 1: Daily Streak
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    StatRowItem(
                        icon = "📅",
                        iconTint = Color(0xFF7B2CF7),
                        label = "Daily Streak",
                        valueNum = "$streak",
                        valueUnit = "Days"
                    )
                }
                
                // Vertical Divider
                Spacer(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.08f)))
                
                // Stat 2: Longest Streak
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    StatRowItem(
                        icon = "🔥",
                        iconTint = Color(0xFFFB923C),
                        label = "Longest Streak",
                        valueNum = "${streak + 8}",
                        valueUnit = "Days"
                    )
                }
                
                // Vertical Divider
                Spacer(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.08f)))
                
                // Stat 3: Total Quizzes
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    StatRowItem(
                        icon = "⭐",
                        iconTint = Color(0xFFA855F7),
                        label = "Total Quizzes",
                        valueNum = "${210 + level * 5}",
                        valueUnit = ""
                    )
                }
                
                // Vertical Divider
                Spacer(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.08f)))
                
                // Stat 4: Rank
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    StatRowItem(
                        icon = "🏆",
                        iconTint = Color(0xFFFFD700),
                        label = "Rank",
                        valueNum = "#${150 - streak.coerceAtMost(50)}",
                        valueUnit = ""
                    )
                }
            }
        }
    }
}

@Composable
fun StatRowItem(
    icon: String,
    iconTint: Color,
    label: String,
    valueNum: String,
    valueUnit: String = ""
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Glowing round-square background for icon
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(iconTint.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                .border(1.dp, iconTint.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 18.sp)
        }
        Column {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = valueNum,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
                if (valueUnit.isNotEmpty()) {
                    Text(
                        text = valueUnit,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Clip
                    )
                }
            }
        }
    }
}

private fun isSameDay(time1: Long, time2: Long): Boolean {
    if (time1 == 0L || time2 == 0L) return false
    val sdf = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US)
    return sdf.format(java.util.Date(time1)) == sdf.format(java.util.Date(time2))
}

private fun isYesterday(lastTime: Long, now: Long): Boolean {
    if (lastTime == 0L) return false
    val sdf = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US)
    val cal = java.util.Calendar.getInstance()
    cal.timeInMillis = now
    cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
    val yesterdayStr = sdf.format(cal.time)
    val lastStr = sdf.format(java.util.Date(lastTime))
    return lastStr == yesterdayStr
}

private fun getCheckInDayStatus(d: Int, dailyStreak: Int, lastCheckInTime: Long): CheckInDayStatus {
    val now = System.currentTimeMillis()
    val isTodayClaimed = lastCheckInTime != 0L && isSameDay(lastCheckInTime, now)
    
    return if (isTodayClaimed) {
        if (d <= dailyStreak) {
            CheckInDayStatus.COMPLETED
        } else {
            CheckInDayStatus.LOCKED
        }
    } else {
        val isYest = lastCheckInTime != 0L && isYesterday(lastCheckInTime, now)
        val activeDay = when {
            lastCheckInTime == 0L -> 1
            isYest -> {
                if (dailyStreak >= 7) 1 else dailyStreak + 1
            }
            else -> 1
        }
        
        when {
            d < activeDay -> CheckInDayStatus.COMPLETED
            d == activeDay -> CheckInDayStatus.ACTIVE
            else -> CheckInDayStatus.LOCKED
        }
    }
}

private fun getNewCheckInDayStatus(
    dayNum: Int,
    currentDay: Int,
    lastClaimTimestamp: Long,
    serverTime: Long
): CheckInDayStatus {
    val isEligible = lastClaimTimestamp == 0L || (serverTime - lastClaimTimestamp >= 86400000L)
    val isStreakReset = lastClaimTimestamp > 0L && (serverTime - lastClaimTimestamp >= 172800000L)

    if (isStreakReset) {
        return when (dayNum) {
            1 -> CheckInDayStatus.ACTIVE
            else -> CheckInDayStatus.LOCKED
        }
    }

    if (isEligible) {
        val nextActiveDay = if (currentDay == 7 || currentDay == 0) 1 else currentDay + 1
        return when {
            dayNum == nextActiveDay -> CheckInDayStatus.ACTIVE
            dayNum < nextActiveDay -> CheckInDayStatus.COMPLETED
            else -> CheckInDayStatus.LOCKED
        }
    } else {
        return when {
            dayNum <= currentDay -> CheckInDayStatus.COMPLETED
            else -> CheckInDayStatus.LOCKED
        }
    }
}

enum class CheckInDayStatus {
    COMPLETED,
    ACTIVE,
    LOCKED
}

@Composable
fun DailyRewardBox(
    day: String,
    rewardCoins: Int,
    status: CheckInDayStatus,
    modifier: Modifier = Modifier
) {
    val isClaimed = status == CheckInDayStatus.COMPLETED
    val isActive = status == CheckInDayStatus.ACTIVE

    val borderColor = when (status) {
        CheckInDayStatus.COMPLETED -> Color(0xFF22C55E)
        CheckInDayStatus.ACTIVE -> Color(0xFFFFC107)
        CheckInDayStatus.LOCKED -> Color(0xFF7C4DFF).copy(alpha = 0.25f)
    }

    val containerBg = if (isActive) Color(0xFF130E26) else Color(0xFF090616)

    Box(
        modifier = modifier
            .width(58.dp)
            .height(96.dp)
            .background(containerBg, RoundedCornerShape(12.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = day,
                color = when (status) {
                    CheckInDayStatus.COMPLETED -> Color(0xFF2DD4BF)
                    CheckInDayStatus.ACTIVE -> Color.White
                    CheckInDayStatus.LOCKED -> Color.White.copy(alpha = 0.4f)
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            if (isClaimed) {
                Text("🪙", fontSize = 16.sp)
            } else if (isActive) {
                Text("🪙", fontSize = 22.sp)
            } else {
                Text("🔒", fontSize = 18.sp)
            }

            if (isClaimed) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color(0xFF22C55E), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                }
            } else {
                Text(
                    text = "$rewardCoins",
                    color = if (isActive) Color(0xFFFFEA3D) else Color(0xFFFFEA3D).copy(alpha = 0.4f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun HomeFeatureCard(
    title: String,
    description: String,
    emoji: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by androidx.compose.animation.core.animateFloatAsState(if (isPressed) 0.95f else 1f)

    val gradient = when (title.lowercase()) {
        "quiz arena" -> Brush.verticalGradient(listOf(Color(0xFF5B21B6), Color(0xFF1E1B4B)))
        "daily tasks" -> Brush.verticalGradient(listOf(Color(0xFF1D4ED8), Color(0xFF1E1B4B)))
        "spin wheel" -> Brush.verticalGradient(listOf(Color(0xFFC2410C), Color(0xFF1F1D1A)))
        "coupon store" -> Brush.verticalGradient(listOf(Color(0xFFBE185D), Color(0xFF1F1625)))
        "leaderboard" -> Brush.verticalGradient(listOf(Color(0xFF047857), Color(0xFF111827)))
        "invite friends", "refer & earn" -> Brush.verticalGradient(listOf(Color(0xFF0369A1), Color(0xFF0F172A)))
        "wallet" -> Brush.verticalGradient(listOf(Color(0xFFB45309), Color(0xFF1C1917)))
        "notifications" -> Brush.verticalGradient(listOf(Color(0xFF6D28D9), Color(0xFF0F172A)))
        "scratch card" -> Brush.verticalGradient(listOf(Color(0xFF6D28D9), Color(0xFF0F172A)))
        "profile" -> Brush.verticalGradient(listOf(Color(0xFF0F766E), Color(0xFF0F172A)))
        "watch ads" -> Brush.verticalGradient(listOf(Color(0xFFD32F2F), Color(0xFF1E1111)))
        else -> Brush.verticalGradient(listOf(color, Color(0xFF111827)))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(145.dp) // Perfect AAA height for 3-column grid
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current,
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0B14)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Background gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(gradient)
            )
            
            // 2. Illustration with glow and sparkles
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.64f)
                    .align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(contentAlignment = Alignment.Center) {
                    // Subtle glowing halo behind emoji
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(color.copy(alpha = 0.22f), CircleShape)
                    )
                    // Sparkles around
                    Text(
                        text = "✨",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.TopStart).offset(x = (-12).dp, y = (-6).dp)
                    )
                    Text(
                        text = "✨",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.BottomEnd).offset(x = 12.dp, y = 6.dp)
                    )
                    // Large emoji
                    Text(
                        text = emoji,
                        fontSize = 38.sp
                    )
                }
            }
            
            // 3. Dark Semi-transparent Footer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.36f)
                    .background(Color.Black.copy(alpha = 0.45f))
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(0.85f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = description,
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 8.sp,
                            lineHeight = 9.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    // Circular arrow button
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(color, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuizChoiceDialog(
    onDismiss: () -> Unit,
    onNavigateToGame: (AppScreen) -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF13111C)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Choose Quiz Game",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Pick an arena to test your skills & earn coins!",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Math Quiz Choice
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDismiss()
                            onNavigateToGame(AppScreen.MathGame)
                        }
                        .background(Color(0xFF7C4DFF).copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00E5FF).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "➕", fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Math Challenge",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Solve arithmetic and earn rewards",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Trivia Game Choice
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDismiss()
                            onNavigateToGame(AppScreen.TriviaGame("GK", "set_1"))
                        }
                        .background(Color(0xFF7C4DFF).copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF7C4DFF).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "❓", fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Trivia Showdown",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "General knowledge quiz with rewards",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.Gray),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Cancel", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun LeaderboardDialog(
    allUsers: List<com.playwin.app.data.model.FirebaseUser>,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF13111C)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Leaderboard",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Global top players with the highest points",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                val sortedUsers = remember(allUsers) {
                    allUsers.sortedByDescending { it.coins }.take(5)
                }

                if (sortedUsers.isEmpty()) {
                    Text(
                        text = "No players found yet. Be the first!",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                } else {
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(sortedUsers) { index, user ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1C1A24), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val rankEmoji = when (index) {
                                        0 -> "🥇"
                                        1 -> "🥈"
                                        2 -> "🥉"
                                        else -> "${index + 1}."
                                    }
                                    Text(
                                        text = rankEmoji,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(28.dp)
                                    )
                                    Text(
                                        text = user.displayName.ifEmpty { "Player" },
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = String.format("%,d", user.coins),
                                        color = Color(0xFFFCD116),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp
                                    )
                                    Icon(
                                        imageVector = Icons.Default.MonetizationOn,
                                        contentDescription = null,
                                        tint = Color(0xFFFCD116),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Close", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun NotificationsDialog(
    onDismiss: () -> Unit,
    onDailyCheckInClick: () -> Unit,
    onInviteClick: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF13111C)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Notifications",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Daily Check-in Reminder Notification Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1A24)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00C853))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Daily Check-In Ready!",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Claim your free 5 Coins right now and grow your coins.",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                onDismiss()
                                onDailyCheckInClick()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(text = "Claim Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Invite Referral Promo Notification Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1A24)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF7C4DFF))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Referral Bonus Promo!",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Earn up to +200 Coins for every friend who joins Play Win using your custom link.",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                onDismiss()
                                onInviteClick()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(text = "Refer & Earn", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun NativeAdMock(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1A24)),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFCD116).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "AD",
                        color = Color(0xFFFCD116),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Text(
                    text = "Sponsored Content",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF7C4DFF).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Ad Icon",
                        tint = Color(0xFF7C4DFF),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Claim Extra Daily Free Spins!",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Install recommended apps to multiply your daily rewards instantly.",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveTaskRowItem(
    title: String,
    progress: String,
    reward: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF12111A)),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(0.5.dp, Color(0xFF2E2C3D))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF7C4DFF).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF7C4DFF))
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = progress,
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }
            Text(
                text = reward,
                color = Color(0xFFFCD116),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

// --- WALLET SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    wallet: com.playwin.app.data.model.UserWallet,
    transactions: List<com.playwin.app.data.model.FirebaseTransaction>,
    viewModel: PlayWinViewModel,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    snackbarHostState: SnackbarHostState,
    onWithdrawClick: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val hasMore by viewModel.hasMoreTransactions.collectAsStateWithLifecycle()
    
    val totalEarned = transactions.filter { it.coins > 0 }.sumOf { it.coins }
    val totalRedeemed = transactions.filter { it.coins < 0 }.sumOf { kotlin.math.abs(it.coins) }
    val lifetimeEarnings = totalEarned

    val dailyRewardsSum = transactions.filter { it.type == "daily_reward" }.sumOf { it.coins }
    val videoAdsSum = transactions.filter { it.type == "video_ad" }.sumOf { it.coins }
    val referralsSum = transactions.filter { it.type == "referral" }.sumOf { it.coins }
    val spinRewardsSum = transactions.filter { it.type == "spin_reward" }.sumOf { it.coins }
    val scratchCardsSum = wallet.totalScratchRewards

    var showExportDialog by remember { mutableStateOf(false) }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Text(
                    text = "Request Payout / Export CSV",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "The secure payout gateway structure is initialized! You can currently inspect and export your transactions or request an automated payout when meeting the minimum coin requirements.",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1A33)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("EXPORT DETAILS:", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("• Format: CSV (Comma-Separated Values)", color = Color.LightGray, fontSize = 12.sp)
                            Text("• Columns: id, title, type, coins, status, timestamp", color = Color.LightGray, fontSize = 12.sp)
                            Text("• Record Count: ${transactions.size}", color = Color.LightGray, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExportDialog = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Success! Exported ${transactions.size} records to playwin_transactions.csv")
                        }
                    }
                ) {
                    Text("EXPORT NOW", color = Color(0xFF7C4DFF), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("CLOSE", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF13111C),
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0C1B))
    ) {
        if (onBack != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = "My Wallet",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1437)),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "AVAILABLE COIN BALANCE",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "🪙 ${wallet.coins}",
                                    color = Color(0xFFFCD116),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$${String.format(java.util.Locale.US, "%.2f", wallet.coins * 0.005)} USD",
                                    color = Color(0xFF00C853),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Secure Wallet",
                                    color = Color.Gray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                                .height(1.dp)
                                .background(Color.White.copy(alpha = 0.1f))
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("💸 Total Earned", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                                Text("${totalEarned} Coins", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("💰 Total Redeemed", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                                Text("${totalRedeemed} Coins", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                Text("📈 Lifetime", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                                Text("${lifetimeEarnings} Coins", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onWithdrawClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("withdraw_via_upi_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = Color(0xFF0F0C1B),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "WITHDRAW VIA UPI",
                                color = Color(0xFF0F0C1B),
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "REWARD SUMMARY",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SummaryCard(
                            title = "Daily Rewards",
                            coins = dailyRewardsSum,
                            icon = "📅",
                            modifier = Modifier.weight(1f)
                        )
                        SummaryCard(
                            title = "Video Ads",
                            coins = videoAdsSum,
                            icon = "🎥",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SummaryCard(
                            title = "Referrals",
                            coins = referralsSum,
                            icon = "👥",
                            modifier = Modifier.weight(1f)
                        )
                        SummaryCard(
                            title = "Spin Wheel",
                            coins = spinRewardsSum,
                            icon = "🎡",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SummaryCard(
                            title = "Scratch Cards",
                            coins = scratchCardsSum,
                            icon = "🎫",
                            modifier = Modifier.weight(1f)
                        )
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TRANSACTION HISTORY",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    TextButton(
                        onClick = { showExportDialog = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF7C4DFF)),
                        modifier = Modifier.testTag("export_button")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export Payout", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(6.dp))

                TextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_field"),
                    placeholder = { Text("Search transactions...", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = null, tint = Color.Gray)
                            }
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1B1437),
                        unfocusedContainerColor = Color(0xFF1B1437),
                        disabledContainerColor = Color(0xFF1B1437),
                        focusedIndicatorColor = Color(0xFF7C4DFF),
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            item {
                val filters = listOf("All", "Rewards", "Referrals", "Ads", "Spins", "Scratch Cards", "Redeemed")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("filter_row")
                ) {
                    items(filters) { filter ->
                        val isSelected = filter == selectedFilter
                        val borderCol = if (isSelected) Color(0xFF7C4DFF) else Color.White.copy(alpha = 0.1f)
                        val bgCol = if (isSelected) Color(0xFF7C4DFF).copy(alpha = 0.15f) else Color(0xFF1B1437)
                        val textCol = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f)

                        Surface(
                            onClick = { viewModel.setFilter(filter) },
                            shape = RoundedCornerShape(50.dp),
                            color = bgCol,
                            border = BorderStroke(1.dp, borderCol),
                            modifier = Modifier
                                .testTag("filter_chip_$filter")
                                .minimumInteractiveComponentSize()
                        ) {
                            Text(
                                text = filter,
                                color = textCol,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            if (transactions.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "📭",
                            fontSize = 48.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Text(
                            text = "No Transactions Found",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Win or claim coins to create transaction history!",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(transactions) { tx ->
                    TransactionItemRow(tx = tx)
                }

                if (hasMore) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(
                                onClick = { viewModel.loadNextTransactionPage() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B1437)),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.3f)),
                                modifier = Modifier.testTag("load_more_button")
                            ) {
                                Text("Load More Transactions", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    coins: Int,
    icon: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1437)),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.White.copy(alpha = 0.05f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 18.sp)
            }
            Column {
                Text(
                    text = title,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "+$coins Coins",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun TransactionItemRow(tx: com.playwin.app.data.model.FirebaseTransaction) {
    val formattedDate = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(tx.timestamp))
    
    val icon = when {
        tx.type == "daily_reward" -> "📅"
        tx.type == "video_ad" -> "🎥"
        tx.type == "referral" -> "🤝"
        tx.type == "spin_reward" -> "🎡"
        tx.type == "scratch_reward" -> "🎁"
        tx.type == "coupon_redeemed" || tx.coins < 0 -> "🎟"
        else -> "🪙"
    }

    val amountColor = if (tx.coins >= 0) Color(0xFF00C853) else Color(0xFFE53935)
    val prefix = if (tx.coins >= 0) "+" else ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1437)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = icon, fontSize = 20.sp)
                }

                Column {
                    Text(
                        text = tx.title,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = formattedDate,
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF00C853).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = tx.status,
                                color = Color(0xFF00C853),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Text(
                text = "$prefix${tx.coins} Coins",
                color = amountColor,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp
            )
        }
    }
}

// --- TASKS SCREEN ---
@Composable
fun TasksScreen(
    wallet: com.playwin.app.data.model.UserWallet,
    viewModel: PlayWinViewModel,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    snackbarHostState: SnackbarHostState,
    onNavigateToGame: (AppScreen) -> Unit,
    onWatchAdClick: () -> Unit,
    onDailyCheckIn: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Gamified Challenges",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TaskGameItem(
                title = "Lucky Spin",
                desc = "Spin & Win Gold!",
                icon = Icons.Default.Refresh,
                color = Color(0xFF7C4DFF),
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToGame(AppScreen.SpinGame) }
            )
            TaskGameItem(
                title = "Lucky Scratch",
                desc = "Scrape for Treasures!",
                icon = Icons.Default.Star,
                color = Color(0xFF00E5FF),
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToGame(AppScreen.ScratchGame) }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TaskGameItem(
                title = "Trivia Quiz",
                desc = "Multiple Choice Trivia!",
                icon = Icons.Default.Done,
                color = Color(0xFF00C853),
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToGame(AppScreen.TriviaGame("GK", "set_1")) }
            )
            TaskGameItem(
                title = "Fast Math",
                desc = "Quick math calculations!",
                icon = Icons.Default.Add,
                color = Color(0xFFFFD700),
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToGame(AppScreen.MathGame) }
            )
        }

        Text(
            text = "Easy Instant Tasks",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Easy Instant Tasks list
        val tasks by viewModel.tasksState.collectAsStateWithLifecycle()
        val userCheckIn by viewModel.userDailyCheckInState.collectAsStateWithLifecycle()
        val currentServerTime by com.playwin.app.data.repository.DailyResetManager.currentServerTime.collectAsStateWithLifecycle()
        val remainingTime by com.playwin.app.data.repository.DailyResetManager.remainingTime.collectAsStateWithLifecycle()

        val startOfToday = remember(currentServerTime) {
            com.playwin.app.data.repository.DailyResetManager.getStartOfTodayUtc(currentServerTime)
        }
        val isTasksCheckInClaimed = remember(userCheckIn?.lastClaimTimestamp, startOfToday) {
            val lastClaim = userCheckIn?.lastClaimTimestamp ?: 0L
            lastClaim >= startOfToday
        }

        if (tasks.isEmpty()) {
            ActiveTaskRowItem(
                title = "Watch High Reward Video Ads",
                progress = "${wallet.dailyAdsWatched}/10 ads watched",
                reward = "+50 Coins",
                modifier = Modifier.clickable {
                    val now = System.currentTimeMillis()
                    val elapsed = now - wallet.lastRewardAdTime
                    val cooldownDuration = 1 * 60 * 1000L
                    if (wallet.dailyAdsWatched >= 10) {
                        android.widget.Toast.makeText(context, "Daily Reward Ad Limit Reached. Come Back Tomorrow.", android.widget.Toast.LENGTH_SHORT).show()
                    } else if (wallet.lastRewardAdTime != 0L && elapsed < cooldownDuration) {
                        val remainingSec = (cooldownDuration - elapsed) / 1000
                        val min = remainingSec / 60
                        val sec = remainingSec % 60
                        val timeStr = String.format("%02d:%02d", min, sec)
                        android.widget.Toast.makeText(context, "Next Reward Ad Available In: $timeStr", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        onWatchAdClick()
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            ActiveTaskRowItem(
                title = "Claim Today's Daily Check-In",
                progress = if (isTasksCheckInClaimed) {
                    "Claimed! Next in $remainingTime"
                } else {
                    "Available once per day"
                },
                reward = "+5 Coins",
                modifier = if (isTasksCheckInClaimed) Modifier else Modifier.clickable(onClick = onDailyCheckIn)
            )
        } else {
            tasks.forEach { task ->
                val isDailyTask = task.type == "daily"
                val isDailyClaimed = isDailyTask && isTasksCheckInClaimed
                ActiveTaskRowItem(
                    title = task.title,
                    progress = when (task.type) {
                        "daily" -> if (isDailyClaimed) {
                            "Claimed! Next in $remainingTime"
                        } else {
                            "0/1 check-in claimed"
                        }
                        "video" -> "${wallet.dailyAdsWatched}/10 ads watched"
                        "streak" -> "${wallet.dailyStreak % 7}/7 streak count"
                        else -> task.progressText
                    },
                    reward = task.rewardText,
                    modifier = if (isDailyClaimed) {
                        Modifier
                    } else {
                        Modifier.clickable {
                            when (task.type) {
                                "video" -> {
                                    val now = System.currentTimeMillis()
                                    val elapsed = now - wallet.lastRewardAdTime
                                    val cooldownDuration = 1 * 60 * 1000L
                                    if (wallet.dailyAdsWatched >= 10) {
                                        android.widget.Toast.makeText(context, "Daily Reward Ad Limit Reached. Come Back Tomorrow.", android.widget.Toast.LENGTH_SHORT).show()
                                    } else if (wallet.lastRewardAdTime != 0L && elapsed < cooldownDuration) {
                                        val remainingSec = (cooldownDuration - elapsed) / 1000
                                        val min = remainingSec / 60
                                        val sec = remainingSec % 60
                                        val timeStr = String.format("%02d:%02d", min, sec)
                                        android.widget.Toast.makeText(context, "Next Reward Ad Available In: $timeStr", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        onWatchAdClick()
                                    }
                                }
                                "daily" -> onDailyCheckIn()
                                "spin" -> onNavigateToGame(AppScreen.SpinGame)
                                "scratch" -> onNavigateToGame(AppScreen.ScratchGame)
                                "trivia" -> onNavigateToGame(AppScreen.TriviaGame("GK", "set_1"))
                                "math" -> onNavigateToGame(AppScreen.MathGame)
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun TaskGameItem(
    title: String,
    desc: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF12111A)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(desc, color = Color.Gray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

// --- COUPONS SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CouponsScreen(
    wallet: com.playwin.app.data.model.UserWallet,
    viewModel: PlayWinViewModel,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    val clipboardManager = LocalClipboardManager.current
    val allCoupons by viewModel.couponsState.collectAsStateWithLifecycle()
    val redemptions by viewModel.redemptionsState.collectAsStateWithLifecycle()
    val couponRedemptions by viewModel.couponRedemptionsState.collectAsStateWithLifecycle()
    val isRedeeming by viewModel.isRedeeming.collectAsStateWithLifecycle()

    val mappedRedemptions = remember(couponRedemptions) {
        couponRedemptions.map { r ->
            com.playwin.app.data.model.FirebaseRedemption(
                id = r.requestId,
                userId = r.userUid,
                couponId = r.requestId,
                couponName = r.couponName,
                coinsSpent = r.requiredCoins,
                status = r.status,
                timestamp = r.createdAt,
                couponCode = r.giftCardOrRechargeNumber,
                expiryDate = "N/A"
            )
        }
    }

    var activeTopTab by remember { mutableStateOf("Store") }
    var couponSearchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedMyCouponFilter by remember { mutableStateOf("Pending") }

    // Dialog/Detail states
    var selectedCouponDetails by remember { mutableStateOf<FirebaseCoupon?>(null) }
    var selectedRedemptionDetails by remember { mutableStateOf<com.playwin.app.data.model.FirebaseRedemption?>(null) }
    var successCouponAnimation by remember { mutableStateOf<FirebaseCoupon?>(null) }

    var showRedemptionFormCoupon by remember { mutableStateOf<FirebaseCoupon?>(null) }

    // Redemption Form Dialog
    showRedemptionFormCoupon?.let { coupon ->
        CouponRedemptionFormDialog(
            coupon = coupon,
            wallet = wallet,
            userEmail = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email ?: "",
            onDismiss = { showRedemptionFormCoupon = null },
            onSubmit = { fullName, mobileNumber, email, rechargeNumber, additionalNotes ->
                android.util.Log.d("PlayWin_StoreScreen", "Redemption Dialog Submitted:")
                android.util.Log.d("PlayWin_StoreScreen", "  coupon.id: ${coupon.id}")
                android.util.Log.d("PlayWin_StoreScreen", "  coupon.couponId: ${coupon.couponId}")
                android.util.Log.d("PlayWin_StoreScreen", "  Firebase node key: ${coupon.couponId}")
                android.util.Log.d("PlayWin_StoreScreen", "  Firebase path used for lookup: /coupons/${coupon.id}")

                viewModel.redeemCouponWithForm(
                    coupon = coupon,
                    fullName = fullName,
                    mobileNumber = mobileNumber,
                    email = email,
                    rechargeNumber = rechargeNumber,
                    additionalNotes = additionalNotes,
                    onSuccess = {
                        showRedemptionFormCoupon = null
                        selectedCouponDetails = null
                        successCouponAnimation = coupon
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Successfully claimed ${coupon.title}!")
                        }
                    },
                    onError = { err ->
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(err)
                        }
                    }
                )
            }
        )
    }

    // Success Animation Dialog
    successCouponAnimation?.let { coupon ->
        RedemptionSuccessDialog(
            coupon = coupon,
            onDismiss = { successCouponAnimation = null }
        )
    }

    // Coupon Details Dialog
    selectedCouponDetails?.let { coupon ->
        CouponDetailsDialog(
            coupon = coupon,
            wallet = wallet,
            isRedeeming = isRedeeming,
            onDismiss = { selectedCouponDetails = null },
            onRedeem = {
                if (wallet.coins < coupon.cost) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Insufficient Coins")
                    }
                } else {
                    showRedemptionFormCoupon = coupon
                }
            }
        )
    }

    // Redemption Details Dialog (My Coupons)
    selectedRedemptionDetails?.let { redemption ->
        RedemptionDetailsDialog(
            redemption = redemption,
            onDismiss = { selectedRedemptionDetails = null },
            onCopyCode = { code ->
                clipboardManager.setText(AnnotatedString(code))
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Coupon code copied to clipboard!")
                }
            }
        )
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0C1B))
    ) {
        // --- COIN BALANCE CARD ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1437)),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "COIN BALANCE FOR REDEEM",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🪙 ${wallet.coins}",
                        color = Color(0xFFFCD116),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF7C4DFF).copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalActivity,
                        contentDescription = null,
                        tint = Color(0xFF7C4DFF),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        // --- TOP NAVIGATION TABS ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .background(Color(0xFF12111A), RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            val tabs = listOf("Store", "My Coupons")
            tabs.forEach { tab ->
                val isSelected = tab == activeTopTab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color(0xFF7C4DFF) else Color.Transparent)
                        .clickable { activeTopTab = tab }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        color = if (isSelected) Color.White else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // --- SUB-SCREEN CONTENT ---
        when (activeTopTab) {
            "Store" -> {
                StoreTabContent(
                    allCoupons = allCoupons,
                    wallet = wallet,
                    couponSearchQuery = couponSearchQuery,
                    onSearchQueryChange = { couponSearchQuery = it },
                    selectedCategory = selectedCategory,
                    onCategoryChange = { selectedCategory = it },
                    onCouponClick = { coupon ->
                        android.util.Log.d("PlayWin_StoreScreen", "Coupon Clicked (onCouponClick):")
                        android.util.Log.d("PlayWin_StoreScreen", "  coupon.id: ${coupon.id}")
                        android.util.Log.d("PlayWin_StoreScreen", "  coupon.couponId: ${coupon.couponId}")
                        android.util.Log.d("PlayWin_StoreScreen", "  Firebase node key: ${coupon.couponId}")
                        android.util.Log.d("PlayWin_StoreScreen", "  Firebase path used for lookup: /coupons/${coupon.id}")
                        selectedCouponDetails = coupon
                    },
                    onRedeemClick = { coupon ->
                        android.util.Log.d("PlayWin_StoreScreen", "Redeem Clicked (onRedeemClick):")
                        android.util.Log.d("PlayWin_StoreScreen", "  coupon.id: ${coupon.id}")
                        android.util.Log.d("PlayWin_StoreScreen", "  coupon.couponId: ${coupon.couponId}")
                        android.util.Log.d("PlayWin_StoreScreen", "  Firebase node key: ${coupon.couponId}")
                        android.util.Log.d("PlayWin_StoreScreen", "  Firebase path used for lookup: /coupons/${coupon.id}")
                        if (wallet.coins < coupon.cost) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Insufficient Coins")
                            }
                        } else {
                            showRedemptionFormCoupon = coupon
                        }
                    }
                )
            }
            "My Coupons" -> {
                MyCouponsTabContent(
                    redemptions = mappedRedemptions,
                    selectedFilter = selectedMyCouponFilter,
                    onFilterChange = { selectedMyCouponFilter = it },
                    onRedemptionClick = { selectedRedemptionDetails = it }
                )
            }
        }
    }
}

// --- STORE CONTENT COMPOSTABLE ---
@Composable
fun StoreTabContent(
    allCoupons: List<FirebaseCoupon>,
    wallet: com.playwin.app.data.model.UserWallet,
    couponSearchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    onCouponClick: (FirebaseCoupon) -> Unit,
    onRedeemClick: (FirebaseCoupon) -> Unit
) {
    val categories = listOf("All", "Shopping", "OTT", "Recharge", "Gift Cards", "Special Offers")

    // Filter and search logic
    val filteredCoupons = allCoupons.filter { coupon ->
        val matchesCategory = selectedCategory == "All" || coupon.category == selectedCategory
        val matchesSearch = coupon.title.contains(couponSearchQuery, ignoreCase = true) ||
                coupon.description.contains(couponSearchQuery, ignoreCase = true)
        val isEnabled = coupon.isEnabled
        matchesCategory && matchesSearch && isEnabled
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 12.dp)
    ) {
        // --- SEARCH BAR ---
        TextField(
            value = couponSearchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("coupon_search_field"),
            placeholder = { Text("Search specific vouchers...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            trailingIcon = {
                if (couponSearchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = null, tint = Color.Gray)
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF12111A),
                unfocusedContainerColor = Color(0xFF12111A),
                disabledContainerColor = Color(0xFF12111A),
                focusedIndicatorColor = Color(0xFF7C4DFF),
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // --- CATEGORIES BAR ---
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                val isSelected = cat == selectedCategory
                Surface(
                    onClick = { onCategoryChange(cat) },
                    shape = RoundedCornerShape(50.dp),
                    color = if (isSelected) Color(0xFF7C4DFF).copy(alpha = 0.15f) else Color(0xFF12111A),
                    border = BorderStroke(1.dp, if (isSelected) Color(0xFF7C4DFF) else Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) Color.White else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- COUPONS GRID/LIST ---
        if (filteredCoupons.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🛍️", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No vouchers match your requirements.", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredCoupons) { coupon ->
                    CouponCardItem(
                        coupon = coupon,
                        wallet = wallet,
                        onClick = { onCouponClick(coupon) },
                        onRedeemClick = { onRedeemClick(coupon) }
                    )
                }
            }
        }
    }
}

// --- INDIVIDUAL COUPON CARD ---
@Composable
fun CouponCardItem(
    coupon: FirebaseCoupon,
    wallet: com.playwin.app.data.model.UserWallet,
    onClick: () -> Unit,
    onRedeemClick: () -> Unit
) {
    val canAfford = wallet.coins >= coupon.cost
    val isOutOfStock = coupon.stock <= 0
    val buttonEnabled = canAfford && !isOutOfStock

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF12111A)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circle Gradient Emblem (Image Slot)
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF7C4DFF).copy(alpha = 0.25f), Color.Transparent)
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = coupon.image.ifEmpty { "🎟️" },
                    fontSize = 26.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFF7C4DFF).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = coupon.category.uppercase(),
                            color = Color(0xFF7C4DFF),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Stock: ${coupon.stock}",
                        color = if (coupon.stock > 0) Color(0xFF00C853) else Color.Gray,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = coupon.title,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
                Text(
                    text = coupon.description,
                    color = Color.Gray,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "🪙 ${coupon.cost} Coins",
                    color = Color(0xFFFCD116),
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onRedeemClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (buttonEnabled) Color(0xFF7C4DFF) else Color(0xFF333045)
                ),
                shape = RoundedCornerShape(10.dp),
                enabled = buttonEnabled,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = if (isOutOfStock) "OUT OF STOCK" else if (canAfford) "REDEEM" else "LOCKED",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = if (buttonEnabled) Color.White else Color.White.copy(alpha = 0.4f)
                )
            }
        }
    }
}

// --- MY COUPONS TAB CONTENT ---
@Composable
fun MyCouponsTabContent(
    redemptions: List<com.playwin.app.data.model.FirebaseRedemption>,
    selectedFilter: String,
    onFilterChange: (String) -> Unit,
    onRedemptionClick: (com.playwin.app.data.model.FirebaseRedemption) -> Unit
) {
    val filters = listOf("Pending", "Approved", "Rejected", "Completed")
    val filteredHistory = redemptions.filter { it.status.trim().equals(selectedFilter, ignoreCase = true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 12.dp)
    ) {
        // Filter Chips row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filters) { f ->
                val isSelected = f == selectedFilter
                val chipColor = when (f) {
                    "Approved" -> Color(0xFF00C853)
                    "Rejected" -> Color(0xFFD50000)
                    "Completed" -> Color(0xFF757575)
                    else -> Color(0xFF7C4DFF)
                }

                Surface(
                    onClick = { onFilterChange(f) },
                    shape = RoundedCornerShape(50.dp),
                    color = if (isSelected) chipColor.copy(alpha = 0.15f) else Color(0xFF12111A),
                    border = BorderStroke(1.dp, if (isSelected) chipColor else Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text(
                        text = f,
                        color = if (isSelected) Color.White else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredHistory.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎟️", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No redemptions found in $selectedFilter.", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredHistory) { r ->
                    RedemptionRowItem(
                        redemption = r,
                        onClick = { onRedemptionClick(r) }
                    )
                }
            }
        }
    }
}

// --- REDEMPTION LIST ROW ---
@Composable
fun RedemptionRowItem(
    redemption: com.playwin.app.data.model.FirebaseRedemption,
    onClick: () -> Unit
) {
    val dateText = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(redemption.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF12111A)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.04f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎁", fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = redemption.couponName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Claimed on $dateText",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "🪙 -${redemption.coinsSpent} Coins",
                    color = Color(0xFFE53935),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Surface(
                    color = when (redemption.status) {
                        "Approved" -> Color(0xFF00C853).copy(alpha = 0.15f)
                        "Rejected" -> Color(0xFFD50000).copy(alpha = 0.15f)
                        "Completed" -> Color(0xFF757575).copy(alpha = 0.15f)
                        else -> Color(0xFF7C4DFF).copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = redemption.status.uppercase(),
                        color = when (redemption.status) {
                            "Approved" -> Color(0xFF00C853)
                            "Rejected" -> Color(0xFFD50000)
                            "Completed" -> Color(0xFF757575)
                            else -> Color(0xFF7C4DFF)
                        },
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

/*
// --- ADMIN PANEL CONTENT ---
@Composable
fun AdminTabContent(
    viewModel: PlayWinViewModel,
    allCoupons: List<FirebaseCoupon>,
    onAddClick: () -> Unit,
    onEditClick: (FirebaseCoupon) -> Unit,
    onEnableToggle: (FirebaseCoupon) -> Unit,
    onCostChangeSubmit: (FirebaseCoupon, Int) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    var adminSubSection by remember { mutableStateOf("Coupons") } // "Coupons", "Claims", "Withdraws", "Users"

    // Collect global streams
    val allClaims by viewModel.allRedemptionsState.collectAsStateWithLifecycle()
    val allCouponRedemptions by viewModel.allCouponRedemptionsState.collectAsStateWithLifecycle()
    val allWithdraws by viewModel.allWithdrawRequestsState.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsersState.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()

    // Dialog state for UPI Withdraw approval/rejection
    var activeWithdrawRequestForAction by remember { mutableStateOf<com.playwin.app.data.model.FirebaseWithdrawRequest?>(null) }
    var withdrawActionType by remember { mutableStateOf("") } // "Approve" or "Reject"
    var withdrawRemarksText by remember { mutableStateOf("") }

    // Dialog state for User coin manual modifications
    var activeUserForCoinsMod by remember { mutableStateOf<com.playwin.app.data.model.FirebaseUser?>(null) }
    var modificationCoinAmount by remember { mutableStateOf("") }
    var isAdditionAction by remember { mutableStateOf(true) }

    // User Search Query
    var userSearchQuery by remember { mutableStateOf("") }

    if (activeWithdrawRequestForAction != null) {
        val request = activeWithdrawRequestForAction!!
        AlertDialog(
            onDismissRequest = { activeWithdrawRequestForAction = null },
            title = {
                Text(
                    text = "${withdrawActionType.uppercase(Locale.US)} WITHDRAWAL REQUEST",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Recipient: ${request.userName}\nUPI ID: ${request.upiId}\nAmount: ₹${request.amount} (${request.coinsSpent} Coins)",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = withdrawRemarksText,
                        onValueChange = { withdrawRemarksText = it },
                        label = { Text("Trans. Ref / Admin Remark (Optional)") },
                        textStyle = TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF7C4DFF),
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = Color(0xFF7C4DFF),
                            unfocusedLabelColor = Color.Gray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val remark = withdrawRemarksText.trim()
                        if (withdrawActionType == "Approve") {
                            viewModel.adminApproveWithdraw(request.id, request.uid, request.transactionId, remark) { _ -> }
                        } else {
                            viewModel.adminRejectWithdraw(request.id, request.uid, request.transactionId, request.coinsSpent, remark) { _ -> }
                        }
                        activeWithdrawRequestForAction = null
                        withdrawRemarksText = ""
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (withdrawActionType == "Approve") Color(0xFF00C853) else Color(0xFFFF3D00)
                    )
                ) {
                    Text("SUBMIT", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    activeWithdrawRequestForAction = null
                    withdrawRemarksText = ""
                }) {
                    Text("CANCEL", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF13111C),
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (activeUserForCoinsMod != null) {
        val userItem = activeUserForCoinsMod!!
        AlertDialog(
            onDismissRequest = { activeUserForCoinsMod = null },
            title = {
                Text(
                    text = "MANUALLY MODIFY COINS",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "User Name: ${userItem.displayName}\nEmail: ${userItem.email}\nCurrent Balance: 🪙 ${userItem.coins}",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = modificationCoinAmount,
                        onValueChange = { modificationCoinAmount = it },
                        label = { Text("Coin Amount") },
                        textStyle = TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF7C4DFF),
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = Color(0xFF7C4DFF),
                            unfocusedLabelColor = Color.Gray
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { isAdditionAction = true }) {
                            RadioButton(selected = isAdditionAction, onClick = { isAdditionAction = true }, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF7C4DFF)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Coins", color = Color.White, fontSize = 13.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { isAdditionAction = false }) {
                            RadioButton(selected = !isAdditionAction, onClick = { isAdditionAction = false }, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF7C4DFF)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Deduct Coins", color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parseAmt = modificationCoinAmount.toIntOrNull()
                        if (parseAmt != null && parseAmt > 0) {
                            viewModel.adminModifyCoinsManually(userItem.uid, if (isAdditionAction) parseAmt else -parseAmt, "Admin Coins Adjustment") { _ -> }
                            activeUserForCoinsMod = null
                            modificationCoinAmount = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
                ) {
                    Text("APPLY", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    activeUserForCoinsMod = null
                    modificationCoinAmount = ""
                }) {
                    Text("CANCEL", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF13111C),
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Scrollable Row for sections to fit all screen sizes beautifully
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val sections = listOf("Coupons", "Claims", "Withdraws", "Users", "Daily Check-In")
            items(sections) { sec ->
                Button(
                    onClick = { adminSubSection = sec },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (adminSubSection == sec) Color(0xFF7C4DFF) else Color(0xFF12111A)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = when (sec) {
                            "Coupons" -> "Coupons"
                            "Claims" -> "Claims"
                            "Withdraws" -> "UPI Requests"
                            "Users" -> "Users Control"
                            else -> "Daily Check-In"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (adminSubSection) {
            "Daily Check-In" -> {
                AdminDailyCheckInSection(viewModel = viewModel)
            }
            "Coupons" -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ALL COUPONS IN SERVER", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Button(
                        onClick = onAddClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("+ ADD COUPON", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(allCoupons) { coupon ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1437))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(coupon.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Category: ${coupon.category} | Cost: ${coupon.cost} coins", color = Color.Gray, fontSize = 10.sp)
                                    }
                                    Surface(
                                        color = if (coupon.isEnabled) Color(0xFF00C853).copy(alpha = 0.1f) else Color(0xFFE53935).copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = if (coupon.isEnabled) "ACTIVE" else "DISABLED",
                                            color = if (coupon.isEnabled) Color(0xFF00C853) else Color(0xFFE53935),
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    TextButton(
                                        onClick = { onEditClick(coupon) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("EDIT", color = Color.White, fontSize = 10.sp)
                                    }
                                    TextButton(
                                        onClick = { onEnableToggle(coupon) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(if (coupon.isEnabled) "DISABLE" else "ENABLE", color = Color(0xFFFCD116), fontSize = 10.sp)
                                    }
                                    TextButton(
                                        onClick = { onDeleteClick(coupon.id) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("DELETE", color = Color(0xFFE53935), fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "Claims" -> {
                Text("GLOBAL REDEMPTION CLAIMS", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))

                if (allCouponRedemptions.isEmpty()) {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f), contentAlignment = Alignment.Center) {
                        Text("No claims submitted yet.", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(allCouponRedemptions) { r ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1437))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(r.couponName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("Name: ${r.displayName}", color = Color.LightGray, fontSize = 11.sp)
                                            Text("Phone: ${r.mobileNumber} | Email: ${r.email}", color = Color.LightGray, fontSize = 11.sp)
                                            Text("Target Account: ${r.giftCardOrRechargeNumber}", color = Color(0xFFFCD116), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            if (r.additionalNotes.isNotEmpty()) {
                                                Text("Notes: ${r.additionalNotes}", color = Color.Gray, fontSize = 10.sp)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("Cost: ${r.requiredCoins} coins | Status: ${r.status}", color = Color.LightGray, fontSize = 11.sp)
                                        }
                                        Text(
                                            text = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(r.createdAt)),
                                            color = Color.Gray,
                                            fontSize = 10.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.adminChangeRedemptionStatus(r.userUid, r.requestId, "Approved") },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text("APPROVE", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Button(
                                            onClick = { viewModel.adminChangeRedemptionStatus(r.userUid, r.requestId, "Rejected") },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD50000)),
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text("REJECT", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Button(
                                            onClick = { viewModel.adminChangeRedemptionStatus(r.userUid, r.requestId, "Completed") },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF757575)),
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text("COMPLETE", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "Withdraws" -> {
                Text("GLOBAL UPI WITHDRAWAL REQUESTS", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))

                if (allWithdraws.isEmpty()) {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f), contentAlignment = Alignment.Center) {
                        Text("No withdraw requests submitted yet.", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(allWithdraws) { wd ->
                            val isPending = wd.status == "Pending"
                            val statusColor = when (wd.status) {
                                "Approved" -> Color(0xFF00C853)
                                "Rejected" -> Color(0xFFFF3D00)
                                else -> Color(0xFFFFD600)
                            }
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1437))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Amount: ₹${wd.amount}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            Text("Coins Spent: 🪙 ${wd.coinsSpent}", color = Color(0xFFFCD116), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(wd.status.uppercase(Locale.US), color = statusColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Holder: ${wd.userName}", color = Color.LightGray, fontSize = 12.sp)
                                    Text("UPI ID: ${wd.upiId}", color = Color.LightGray, fontSize = 12.sp)
                                    Text("UserId: ${wd.uid}", color = Color.Gray, fontSize = 10.sp)
                                    Text("Submitted: ${SimpleDateFormat("dd/MM/yyyy h:mm a", Locale.US).format(Date(wd.timestamp))}", color = Color.Gray, fontSize = 11.sp)
                                    
                                    if (wd.remarks.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("Remark: ${wd.remarks}", color = Color.LightGray, fontSize = 11.sp, fontStyle = FontStyle.Italic)
                                    }

                                    if (isPending) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    activeWithdrawRequestForAction = wd
                                                    withdrawActionType = "Approve"
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("APPROVE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Button(
                                                onClick = {
                                                    activeWithdrawRequestForAction = wd
                                                    withdrawActionType = "Reject"
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3D00)),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("REJECT", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else -> { // "Users"
                Text("USER ACCOUNT SECURITY CONTROL", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = userSearchQuery,
                    onValueChange = { userSearchQuery = it },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    placeholder = { Text("Search by email or name...", color = Color.Gray) },
                    textStyle = TextStyle(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF7C4DFF),
                        unfocusedBorderColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                val filteredUsers = allUsers.filter {
                    it.displayName.contains(userSearchQuery, ignoreCase = true) ||
                    it.email.contains(userSearchQuery, ignoreCase = true) ||
                    it.uid.contains(userSearchQuery, ignoreCase = true)
                }

                if (filteredUsers.isEmpty()) {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f), contentAlignment = Alignment.Center) {
                        Text("No matching user found on server.", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(filteredUsers) { user ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1437))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(user.displayName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(user.email, color = Color.Gray, fontSize = 11.sp)
                                            Text("Balance: 🪙 ${user.coins} Coins", color = Color(0xFFFCD116), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        }
                                        if (user.isBlocked) {
                                            Box(
                                                modifier = Modifier
                                                    .background(Color.Red.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("SUSPENDED", color = Color.Red, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { activeUserForCoinsMod = user },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                                            modifier = Modifier.weight(1.3f),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text("MANAGE COINS", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = {
                                                viewModel.adminSetUserBlockStatus(user.uid, !user.isBlocked) { _ -> }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (user.isBlocked) Color(0xFF00C853) else Color(0xFFFF3D00)
                                            ),
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text(
                                                text = if (user.isBlocked) "UNSUSPEND" else "SUSPEND",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
*/

// --- REDEMPTION SUCCESS DIALOG ---
@Composable
fun RedemptionSuccessDialog(
    coupon: FirebaseCoupon,
    onDismiss: () -> Unit
) {
    var checkmarkVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        checkmarkVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (checkmarkVisible) 1f else 0.2f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val rotation by animateFloatAsState(
        targetValue = if (checkmarkVisible) 360f else 0f,
        animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing),
        label = "rotation"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = null,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(scale)
                        .background(Color(0xFF00C853).copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = Color(0xFF00C853),
                        modifier = Modifier
                            .size(72.dp)
                            .rotate(rotation)
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "REDEEMED SUCCESSFULLY!",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "You successfully redeemed ${coupon.title} for ${coupon.cost} Coins.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1437)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "COUPON CLAIM CODE:",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = coupon.code,
                            color = Color(0xFFFCD116),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Expires: ${coupon.expiryDate.ifEmpty { "No expiry" }}",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("DONE", fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = Color(0xFF13111C),
        shape = RoundedCornerShape(20.dp)
    )
}

// --- COUPON DETAILS CONFIG DIALOG ---
@Composable
fun CouponDetailsDialog(
    coupon: FirebaseCoupon,
    wallet: com.playwin.app.data.model.UserWallet,
    isRedeeming: Boolean,
    onDismiss: () -> Unit,
    onRedeem: () -> Unit
) {
    val canAfford = wallet.coins >= coupon.cost

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                coupon.title,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(coupon.image.ifEmpty { "🎟️" }, fontSize = 32.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Surface(
                            color = Color(0xFF7C4DFF).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = coupon.category.uppercase(),
                                color = Color(0xFF7C4DFF),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = coupon.availability,
                            color = if (coupon.availability == "In Stock") Color(0xFF00C853) else Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "VOUCHER DESCRIPTION:",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = coupon.description,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("REDEEM COST", color = Color.Gray, fontSize = 11.sp)
                        Text("🪙 ${coupon.cost} Coins", color = Color(0xFFFCD116), fontWeight = FontWeight.Black, fontSize = 15.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("VALID UNTIL", color = Color.Gray, fontSize = 11.sp)
                        Text(coupon.expiryDate.ifEmpty { "Lifetime" }, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onRedeem,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                shape = RoundedCornerShape(10.dp),
                enabled = canAfford && !isRedeeming
            ) {
                Text(
                    text = if (isRedeeming) "CLAIMING..." else if (canAfford) "REDEEM NOW" else "INSUFFICIENT COINS",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", color = Color.Gray)
            }
        },
        containerColor = Color(0xFF13111C),
        shape = RoundedCornerShape(16.dp)
    )
}

// --- PREMIUM REDEMPTION FORM DIALOG ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CouponRedemptionFormDialog(
    coupon: FirebaseCoupon,
    wallet: com.playwin.app.data.model.UserWallet,
    userEmail: String,
    onDismiss: () -> Unit,
    onSubmit: (fullName: String, mobileNumber: String, email: String, rechargeNumber: String, additionalNotes: String) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(userEmail) }
    var rechargeNumber by remember { mutableStateOf("") }
    var additionalNotes by remember { mutableStateOf("") }

    var fullNameError by remember { mutableStateOf(false) }
    var mobileNumberError by remember { mutableStateOf(false) }
    var rechargeNumberError by remember { mutableStateOf(false) }

    var showConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(
                    text = "Confirm Redemption",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to redeem this coupon?\n\nThis action will deduct your coins and create a redemption request.",
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        onSubmit(fullName.trim(), mobileNumber.trim(), email.trim(), rechargeNumber.trim(), additionalNotes.trim())
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
                ) {
                    Text("Confirm Redeem", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF13111C),
            shape = RoundedCornerShape(16.dp)
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Coupon Redemption Request",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Read-Only stats
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1437)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Coupon Name:", color = Color.Gray, fontSize = 12.sp)
                            Text(coupon.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Required Coins:", color = Color.Gray, fontSize = 12.sp)
                            Text("🪙 ${coupon.cost}", color = Color(0xFFFCD116), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Your Coin Balance:", color = Color.Gray, fontSize = 12.sp)
                            Text("🪙 ${wallet.coins}", color = Color(0xFFFCD116), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                // Full Name
                OutlinedTextField(
                    value = fullName,
                    onValueChange = {
                        fullName = it
                        fullNameError = it.trim().isEmpty()
                    },
                    label = { Text("Full Name *") },
                    isError = fullNameError,
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF7C4DFF),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFF7C4DFF),
                        unfocusedLabelColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (fullNameError) {
                    Text("Full Name is required.", color = Color.Red, fontSize = 10.sp)
                }

                // Mobile Number
                OutlinedTextField(
                    value = mobileNumber,
                    onValueChange = {
                        mobileNumber = it
                        mobileNumberError = it.trim().length != 10 || !it.all { char -> char.isDigit() }
                    },
                    label = { Text("Mobile Number (10 digits) *") },
                    isError = mobileNumberError,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF7C4DFF),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFF7C4DFF),
                        unfocusedLabelColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (mobileNumberError) {
                    Text("Mobile Number must be exactly 10 digits.", color = Color.Red, fontSize = 10.sp)
                }

                // Email Address
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address *") },
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF7C4DFF),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFF7C4DFF),
                        unfocusedLabelColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Recharge/Gift Card Number
                OutlinedTextField(
                    value = rechargeNumber,
                    onValueChange = {
                        rechargeNumber = it
                        rechargeNumberError = it.trim().isEmpty()
                    },
                    label = { Text("Preferred Recharge / Gift Card No. *") },
                    isError = rechargeNumberError,
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF7C4DFF),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFF7C4DFF),
                        unfocusedLabelColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (rechargeNumberError) {
                    Text("This field is required.", color = Color.Red, fontSize = 10.sp)
                }

                // Additional Notes
                OutlinedTextField(
                    value = additionalNotes,
                    onValueChange = { additionalNotes = it },
                    label = { Text("Additional Notes (Optional)") },
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF7C4DFF),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFF7C4DFF),
                        unfocusedLabelColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val isNameValid = fullName.trim().isNotEmpty()
                    val isMobileValid = mobileNumber.trim().length == 10 && mobileNumber.all { it.isDigit() }
                    val isRechargeValid = rechargeNumber.trim().isNotEmpty()

                    fullNameError = !isNameValid
                    mobileNumberError = !isMobileValid
                    rechargeNumberError = !isRechargeValid

                    if (isNameValid && isMobileValid && isRechargeValid) {
                        showConfirmDialog = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
            ) {
                Text("Submit", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        containerColor = Color(0xFF13111C),
        shape = RoundedCornerShape(16.dp)
    )
}

// --- REDEMPTION DETAILS SCREEN ---
@Composable
fun RedemptionDetailsDialog(
    redemption: com.playwin.app.data.model.FirebaseRedemption,
    onDismiss: () -> Unit,
    onCopyCode: (String) -> Unit
) {
    val dateText = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(redemption.timestamp))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Redemption Details",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                Text("VOUCHER CLAIMED:", color = Color.Gray, fontSize = 11.sp)
                Text(redemption.couponName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("COINS DECUCTED", color = Color.Gray, fontSize = 11.sp)
                        Text("🪙 -${redemption.coinsSpent} Coins", color = Color(0xFFE53935), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("CLAIM DATE", color = Color.Gray, fontSize = 11.sp)
                        Text(dateText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("REDEEM STATUS", color = Color.Gray, fontSize = 11.sp)
                Surface(
                    color = when (redemption.status) {
                        "Approved" -> Color(0xFF00C853).copy(alpha = 0.15f)
                        "Rejected" -> Color(0xFFD50000).copy(alpha = 0.15f)
                        "Used" -> Color(0xFF757575).copy(alpha = 0.15f)
                        else -> Color(0xFF7C4DFF).copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = redemption.status.uppercase(),
                        color = when (redemption.status) {
                            "Approved" -> Color(0xFF00C853)
                            "Rejected" -> Color(0xFFD50000)
                            "Used" -> Color(0xFF757575)
                            else -> Color(0xFF7C4DFF)
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (redemption.status == "Approved") {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1437)),
                        border = BorderStroke(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("YOUR REDEEMABLE COUPON CODE", color = Color.Gray, fontSize = 10.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = redemption.couponCode,
                                color = Color(0xFFFCD116),
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { onCopyCode(redemption.couponCode) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Copy Coupon Code", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else if (redemption.status == "Pending") {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF221F2D))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "🔒 COUPON PIN / CODE LOCKED",
                                color = Color(0xFFFCD116),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "To maintain maximum security, your voucher pin will unlock instantly here once approved by system security audit. Please wait slightly.",
                                color = Color.LightGray,
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
            ) {
                Text("DONE", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color(0xFF13111C),
        shape = RoundedCornerShape(16.dp)
    )
}

/*
// --- ADMIN CREATE/EDIT FORM DIALOG ---
@Composable
fun AdminCouponFormDialog(
    couponToEdit: FirebaseCoupon? = null,
    onDismiss: () -> Unit,
    onSave: (FirebaseCoupon) -> Unit
) {
    var id by remember { mutableStateOf(couponToEdit?.id ?: "") }
    var title by remember { mutableStateOf(couponToEdit?.title ?: "") }
    var costText by remember { mutableStateOf(couponToEdit?.cost?.toString() ?: "") }
    var code by remember { mutableStateOf(couponToEdit?.code ?: "") }
    var description by remember { mutableStateOf(couponToEdit?.description ?: "") }
    var category by remember { mutableStateOf(couponToEdit?.category ?: "Shopping") }
    var availability by remember { mutableStateOf(couponToEdit?.availability ?: "In Stock") }
    var expiryDate by remember { mutableStateOf(couponToEdit?.expiryDate ?: "") }
    var image by remember { mutableStateOf(couponToEdit?.image ?: "🎟️") }
    var isEnabled by remember { mutableStateOf(couponToEdit?.isEnabled ?: true) }

    val categories = listOf("Shopping", "OTT", "Recharge", "Gift Cards", "Special Offers")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (couponToEdit == null) "Create New Coupon" else "Edit Existing Coupon",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (couponToEdit == null) {
                    OutlinedTextField(
                        value = id,
                        onValueChange = { id = it },
                        label = { Text("Coupon ID (e.g. coupon_steam)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Coupon Title") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                OutlinedTextField(
                    value = costText,
                    onValueChange = { costText = it },
                    label = { Text("Coin Cost (integer)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Coupon Redemption Code") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Short Description") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                OutlinedTextField(
                    value = availability,
                    onValueChange = { availability = it },
                    label = { Text("Availability (e.g. In Stock, 5 Left)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                OutlinedTextField(
                    value = expiryDate,
                    onValueChange = { expiryDate = it },
                    label = { Text("Expiry Date (e.g. 31 Dec 2026)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                OutlinedTextField(
                    value = image,
                    onValueChange = { image = it },
                    label = { Text("Emoji icon (e.g. 🎒, 🎮, ⚡)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                // Category Selection Spinner row
                Text("SELECT CATEGORY:", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        val isSel = cat == category
                        Surface(
                            onClick = { category = cat },
                            color = if (isSel) Color(0xFF7C4DFF) else Color(0xFF12111A),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = cat,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF7C4DFF))
                    )
                    Text("Is Coupon Active/Enabled", color = Color.White, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalId = id.ifEmpty { "coupon_" + System.currentTimeMillis() }
                    val finalCost = costText.toIntOrNull() ?: 0
                    val coupon = FirebaseCoupon(
                        couponId = finalId,
                        couponName = title,
                        coinCost = finalCost,
                        requiredCoins = finalCost,
                        code = code,
                        description = description,
                        category = category,
                        status = availability,
                        expiryDate = expiryDate,
                        couponImage = image,
                        enabled = isEnabled,
                        remainingStock = 50
                    )
                    onSave(coupon)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                enabled = title.isNotEmpty() && costText.isNotEmpty()
            ) {
                Text("SAVE", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = Color.Gray)
            }
        },
        containerColor = Color(0xFF13111C),
        shape = RoundedCornerShape(16.dp)
    )
}
*/

// --- REFERRAL SCREEN ---
@Composable
fun ReferralScreen(
    wallet: com.playwin.app.data.model.UserWallet,
    viewModel: PlayWinViewModel,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    snackbarHostState: SnackbarHostState,
    onBack: (() -> Unit)? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val clipboard = androidx.compose.ui.platform.LocalClipboardManager.current

    val referralCode = if (wallet.userId.isNotEmpty()) {
        "PLAYWIN_${wallet.userId.take(6).uppercase()}"
    } else {
        "PLAYWIN99"
    }
    val appLink = "https://play.google.com/store/apps/details?id=com.playwin.app"
    val shareMessage = """
🎮 Join Play Win and earn rewards!
Use my referral code:
$referralCode

Download now:
$appLink

Get +50 coins instantly after signup.
""".trimIndent()

    var enteredCode by remember { mutableStateOf("") }
    val hasClaimed = wallet.hasUsedReferralCode
    val isProcessingReferral by viewModel.isProcessingReferral.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.refreshUserData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (onBack != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Refer & Earn",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        // Hero Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF13111C)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Circle gift card icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFF7C4DFF).copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = "Refer & Earn",
                        tint = Color(0xFF7C4DFF),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Refer & Earn",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Invite Friends & Earn Rewards",
                    color = Color(0xFF7C4DFF),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Share your code, your friend receives 50 coins instantly. You get 100 coins once they setup the application!",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Custom dotted referral promo display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1A1824), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "YOUR REFERRAL CODE",
                                color = Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = referralCode,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("referral_code_text")
                            )
                        }

                        IconButton(
                            onClick = {
                                clipboard.setText(androidx.compose.ui.text.AnnotatedString(referralCode))
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Referral code copied to clipboard!")
                                }
                            },
                            modifier = Modifier.testTag("copy_referral_code_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy code",
                                tint = Color(0xFF7C4DFF)
                            )
                        }
                    }
                }
            }
        }

        // Statistics section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .background(Color(0xFF12111A), RoundedCornerShape(12.dp))
                .border(0.5.dp, Color(0xFF2E2C3D), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${wallet.totalReferrals}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.testTag("total_referrals_stats")
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Total Referrals",
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }
            
            Box(
                modifier = Modifier
                    .width(0.5.dp)
                    .height(24.dp)
                    .background(Color.Gray.copy(alpha = 0.3f))
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${wallet.referralsCoinsEarned}",
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.testTag("coins_earned_stats")
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Coins Earned",
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }

            Box(
                modifier = Modifier
                    .width(0.5.dp)
                    .height(24.dp)
                    .background(Color.Gray.copy(alpha = 0.3f))
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${wallet.pendingRewards}",
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.testTag("pending_rewards_stats")
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Pending Rewards",
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }
        }

        // Share Section Box Title
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Share your referral code",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // WhatsApp
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        onClick = {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    `package` = "com.whatsapp"
                                    putExtra(android.content.Intent.EXTRA_TEXT, shareMessage)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val uri = android.net.Uri.parse("https://api.whatsapp.com/send?text=" + android.net.Uri.encode(shareMessage))
                                context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, uri))
                            }
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .testTag("whatsapp_share_button"),
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF25D366))
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("WA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("WhatsApp", color = Color.Gray, fontSize = 11.sp)
                }

                // Instagram
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        onClick = {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    `package` = "com.instagram.android"
                                    putExtra(android.content.Intent.EXTRA_TEXT, shareMessage)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(android.content.Intent.EXTRA_TEXT, shareMessage)
                                }
                                context.startActivity(android.content.Intent.createChooser(intent, "Share on Instagram"))
                            }
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .testTag("instagram_share_button"),
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE1306C))
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("IG", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Instagram", color = Color.Gray, fontSize = 11.sp)
                }

                // Facebook
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        onClick = {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    `package` = "com.facebook.katana"
                                    putExtra(android.content.Intent.EXTRA_TEXT, shareMessage)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val uri = android.net.Uri.parse("https://www.facebook.com/sharer/sharer.php?u=" + android.net.Uri.encode(appLink) + "&quote=" + android.net.Uri.encode(shareMessage))
                                context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, uri))
                            }
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .testTag("facebook_share_button"),
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1877F2))
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("FB", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Facebook", color = Color.Gray, fontSize = 11.sp)
                }

                // More
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        onClick = {
                            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_TEXT, shareMessage)
                            }
                            context.startActivity(android.content.Intent.createChooser(intent, "Share Referral Code"))
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .testTag("more_share_button"),
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF7C4DFF))
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "More", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("More", color = Color.Gray, fontSize = 11.sp)
                }
            }
        }

        // How it works Card Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF12111A)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(0.5.dp, Color(0xFF2E2C3D))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "How it works?",
                    color = Color(0xFF7C4DFF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF00C853),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Friend installs, registers and links your code",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF00C853),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Friend completes their FIRST QUIZ",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF00C853),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Friend receives +100 Coins, Referrer receives +500 Coins",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Referral History Card Section
        val referralHistory by viewModel.referralHistoryState.collectAsStateWithLifecycle()

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF12111A)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(0.5.dp, Color(0xFF2E2C3D))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Referral History",
                    color = Color(0xFF7C4DFF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (referralHistory.isEmpty()) {
                    Text(
                        text = "No referrals yet. Refer your friends to start earning!",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    )
                } else {
                    referralHistory.forEachIndexed { index, record ->
                        Column(modifier = Modifier.fillMaxWidth().testTag("referral_history_item_$index")) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = record.friendName,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Joined: " + java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(record.joinDate)),
                                        color = Color.Gray,
                                        fontSize = 10.sp
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    val isRewarded = record.status == "Rewarded"
                                    Surface(
                                        color = if (isRewarded) Color(0xFF00C853).copy(alpha = 0.15f) else Color(0xFFFF9100).copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = record.status,
                                            color = if (isRewarded) Color(0xFF00C853) else Color(0xFFFF9100),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (isRewarded) "+${record.coinsEarned} Coins" else "Pending First Quiz",
                                        color = if (isRewarded) Color(0xFFFFD700) else Color.Gray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            if (index < referralHistory.lastIndex) {
                                Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(Color.Gray.copy(alpha = 0.15f)))
                            }
                        }
                    }
                }
            }
        }

        // OR entered code entry
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(0.5.dp)
                    .background(Color.Gray.copy(alpha = 0.3f))
            )
            Text(
                text = "OR",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(0.5.dp)
                    .background(Color.Gray.copy(alpha = 0.3f))
            )
        }

        Text(
            text = "Enter Friends Referral Code",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = if (hasClaimed) "Referral bonus already claimed." else enteredCode,
            onValueChange = { if (!hasClaimed) enteredCode = it },
            enabled = !hasClaimed && !isProcessingReferral,
            placeholder = { Text("Example: PLAYWIN_XXXXXX", color = Color.Gray) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF12111A),
                unfocusedContainerColor = Color(0xFF12111A),
                disabledContainerColor = Color(0xFF1A1824),
                focusedBorderColor = Color(0xFF7C4DFF),
                unfocusedBorderColor = Color(0xFF2E2C3D),
                disabledBorderColor = Color(0xFF2E2C3D).copy(alpha = 0.5f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                disabledTextColor = Color.Gray
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("referral_code_input")
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = {
                if (enteredCode.isNotEmpty()) {
                    viewModel.claimFriendReferral(enteredCode) { success, errorMsg ->
                        if (success) {
                            enteredCode = ""
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Referral Linked Successfully")
                            }
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(errorMsg ?: "Failed to apply referral code.")
                            }
                        }
                    }
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Please enter a friend's referral code!")
                    }
                }
            },
            enabled = !hasClaimed && !isProcessingReferral,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF7C4DFF),
                disabledContainerColor = Color(0xFF4C3D82)
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("claim_referral_bonus_button")
        ) {
            if (isProcessingReferral) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.5.dp
                )
            } else {
                Text("CLAIM FRIEND BONUS", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

// --- PROFILE SCREEN ---
@Composable
fun ProfileScreen(
    wallet: com.playwin.app.data.model.UserWallet,
    transactions: List<com.playwin.app.data.model.FirebaseTransaction>,
    viewModel: PlayWinViewModel,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    var showClearProgressConfirm by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    val currentUser by viewModel.currentUserState.collectAsStateWithLifecycle()
    val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
    val userEmail = firebaseUser?.email ?: "scrrana2000@gmail.com"
    
    // Dynamic user display name from Realtime Database or Auth, with Player fallback
    val userName = currentUser?.displayName?.ifEmpty { null }
        ?: firebaseUser?.displayName?.ifEmpty { null }
        ?: "Player"

    // Add debug log for loaded display name
    android.util.Log.d("PlayWinVM", "Display Name Loaded: $userName")

    if (showClearProgressConfirm) {
        AlertDialog(
            onDismissRequest = { showClearProgressConfirm = false },
            title = { Text("Reset Progress?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to reset your coins, daily streak, task progress, and reward history? This action is irreversible, but your Google authentication session will remain active.", color = Color.White.copy(alpha = 0.8f)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearCurrentAccountProgress()
                        showClearProgressConfirm = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Success! Account progress reset successfully.")
                        }
                    }
                ) {
                    Text("Reset", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearProgressConfirm = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = Color(0xFF13111C),
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Logout", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to logout?", color = Color.White.copy(alpha = 0.8f)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.logout()
                        showLogoutConfirm = false
                    }
                ) {
                    Text("Logout", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = Color(0xFF13111C),
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar circle
        Box(
            modifier = Modifier
                .padding(top = 16.dp, bottom = 12.dp)
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(0xFF7C4DFF).copy(alpha = 0.15f))
                .border(2.dp, Color(0xFF7C4DFF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(userName.first().uppercaseChar().toString(), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        }

        Text(userName, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        Text(userEmail, color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(bottom = 24.dp))

        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF12111A))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("COINS", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("${wallet.coins}", color = Color(0xFFFCD116), fontSize = 18.sp, fontWeight = FontWeight.Black)
                }
                Box(modifier = Modifier.width(0.5.dp).height(24.dp).background(Color.Gray.copy(alpha = 0.5f)))
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("STREAK", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("${wallet.dailyStreak} Days", color = Color(0xFF00E5FF), fontSize = 18.sp, fontWeight = FontWeight.Black)
                }
                Box(modifier = Modifier.width(0.5.dp).height(24.dp).background(Color.Gray.copy(alpha = 0.5f)))
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("RECORDS", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("${transactions.size}", color = Color(0xFF00C853), fontSize = 18.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        // Profile Menu settings
        ProfileMenuRowItem(
            title = "Help & Application Guidelines",
            icon = Icons.Default.Info,
            onClick = {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Guidelines: Play games, complete daily checks to earn coins, exchange them for vouchers instantly!")
                }
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        ProfileMenuRowItem(
            title = "Clear Current Account Progress",
            icon = Icons.Default.Refresh,
            color = Color.Red,
            onClick = {
                showClearProgressConfirm = true
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        ProfileMenuRowItem(
            title = "Logout",
            icon = Icons.Default.ExitToApp,
            color = Color.Red,
            onClick = {
                showLogoutConfirm = true
            }
        )
    }
}

@Composable
fun ProfileMenuRowItem(
    title: String,
    icon: ImageVector,
    color: Color = Color.White,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF12111A)),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(0.5.dp, Color(0xFF2E2C3D))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = if (color == Color.White) Color(0xFF7C4DFF) else color, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, color = color, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
        }
    }
}

// --- POPUP DIALOGS INTERACTIVE SIMULATORS ---

@Composable
fun VideoAdSimulatorDialog(
    viewModel: com.playwin.app.ui.viewmodel.PlayWinViewModel,
    wallet: com.playwin.app.data.model.UserWallet,
    onDismiss: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val coroutineScope = rememberCoroutineScope()
    var stage by remember { mutableStateOf("LOADING") } // "LOADING", "PLAYING", "ERROR", "REWARDED"
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var countSeconds by remember { mutableIntStateOf(5) }

    // Check reset of ad watch count to make sure our UI limit values are fresh
    LaunchedEffect(Unit) {
        viewModel.checkAdCountReset()
    }

    // 1. Loading screen simulation (1.5 seconds) with a beautiful progress loader
    LaunchedEffect(Unit) {
        delay(1500)
        // Check Daily Limit
        val todayWatched = wallet.dailyAdsWatched
        if (todayWatched >= 10) {
            errorMessage = "Daily Reward Ad Limit Reached. Come back tomorrow."
            stage = "ERROR"
        } else {
            // Simulated 10% chance of "No ad availability" to test error screen properly
            val isAdAvailable = (1..100).random() > 10
            if (!isAdAvailable) {
                errorMessage = "No ads available at the moment. Please try again later!"
                stage = "ERROR"
            } else {
                stage = "PLAYING"
            }
        }
    }

    // 2. Video Playing screen simulation with countdown and close button
    LaunchedEffect(stage) {
        if (stage == "PLAYING") {
            while (countSeconds > 0) {
                delay(1000)
                countSeconds--
            }
            // Once watching is fully completed, claim reward automatically in Firebase via Transaction
            viewModel.claimVideoAdReward { success, error ->
                if (success) {
                    stage = "REWARDED"
                } else {
                    errorMessage = error ?: "Network error. Failed to add reward."
                    stage = "ERROR"
                }
            }
        }
    }

    LaunchedEffect(stage) {
        if (stage == "REWARDED") {
            delay(1500)
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (stage == "ERROR" || stage == "REWARDED") {
                onDismiss()
            }
        },
        confirmButton = {},
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PLAYWIN VIDEO ADS",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Close (X) button is visible during video playback, allowing user to cancel/early close
                if (stage == "PLAYING" || stage == "LOADING") {
                    IconButton(
                        onClick = {
                            if (stage == "PLAYING") {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Ad closed early. No reward earned.")
                                }
                            }
                            onDismiss()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close ad",
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (stage) {
                    "LOADING" -> {
                        Text(
                            text = "Loading sponsored rewarded ad...",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        CircularProgressIndicator(
                            color = Color(0xFF00E5FF),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Ads loaded today: ${wallet.dailyAdsWatched}/10",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    }
                    "PLAYING" -> {
                        Text(
                            text = "Sponsor advertisement playback in progress...",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E5FF).copy(alpha = 0.1f))
                                .border(1.5.dp, Color(0xFF00E5FF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$countSeconds s",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Watch till end for +50 Coins reward!",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    "ERROR" -> {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error icon",
                            tint = Color(0xFFFF3D00),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = errorMessage ?: "No ad is available at the moment.",
                            color = Color.White,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
                        ) {
                            Text("Dismiss", color = Color.White)
                        }
                    }
                    "REWARDED" -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success icon",
                            tint = Color(0xFF00C853),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Reward Earned! +50 Coins",
                            color = Color(0xFF00C853),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Your coins updated instantly!",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFF13111C),
        shape = RoundedCornerShape(16.dp)
    )
}

// --- BLOCKED USER OVERLAY SCREEN ---
@Composable
fun BlockedUserScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0E17))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161522)),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Block,
                    contentDescription = "Blocked Account",
                    tint = Color.Red,
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "ACCOUNT SUSPENDED",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Your account has been suspended by the administrator due to violating terms of service or detected suspicious activities.",
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Contact Support:",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                Text(
                    text = "support@playwinapp.com",
                    color = Color(0xFF7C4DFF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

// --- UPI WITHDRAW SCREEN ---
data class WithdrawOption(val amount: Int, val coins: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UPIWithdrawScreen(
    viewModel: PlayWinViewModel,
    onBack: () -> Unit
) {
    val wallet by viewModel.walletState.collectAsStateWithLifecycle()
    val withdrawRequests by viewModel.withdrawRequestsState.collectAsStateWithLifecycle()
    val isSubmitting by viewModel.isSubmittingWithdraw.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var holderName by remember { mutableStateOf("") }
    var upiId by remember { mutableStateOf("") }
    
    val options = listOf(
        WithdrawOption(10, 1000),
        WithdrawOption(20, 2000),
        WithdrawOption(50, 5000),
        WithdrawOption(100, 10000)
    )
    
    var selectedOption by remember { mutableStateOf<WithdrawOption?>(options[0]) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val totalWithdrawnCoins = withdrawRequests.filter { it.status == "Approved" }.sumOf { it.coinsSpent }

    // Today's requests to handle limit check
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    val startOfToday = calendar.timeInMillis
    val todayRequestsCount = withdrawRequests.filter { it.timestamp >= startOfToday }.size

    fun isValidUpi(upi: String): Boolean {
        val pattern = "^[a-zA-Z0-9.\\-_]{2,256}@[a-zA-Z]{2,64}$".toRegex()
        return pattern.matches(upi.trim())
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSuccessDialog = false
                onBack()
            },
            title = {
                Text(
                    text = "Withdrawal Submitted",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success icon",
                        tint = Color(0xFF00C853),
                        modifier = Modifier
                            .size(54.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your withdrawal request of ₹${selectedOption?.amount} (${selectedOption?.coins} Coins) is submitted successfully and is currently under pending admin review.",
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        onBack()
                    }
                ) {
                    Text("OK", color = Color(0xFF7C4DFF), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF13111C),
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFF0F0C1B),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Withdraw via UPI",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Balance details
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1437)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "CURRENT COINS",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "🪙 ${wallet.coins}",
                                color = Color(0xFFFCD116),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "TOTAL WITHDRAWN",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "🪙 $totalWithdrawnCoins",
                                color = Color(0xFF00C853),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Specs Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Min. Withdraw", color = Color.Gray, fontSize = 11.sp)
                        Text("1,000 Coins (₹10)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Today's Requests Limit", color = Color.Gray, fontSize = 11.sp)
                        Text("$todayRequestsCount / 3 Limits", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Selection options
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "SELECT WITHDRAWAL OPTION",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf(options[0], options[1]).forEach { option ->
                            val isSelected = selectedOption == option
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedOption = option },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFF7C4DFF).copy(alpha = 0.2f) else Color(0xFF161522)
                                ),
                                border = BorderStroke(
                                    width = 1.5.dp,
                                    color = if (isSelected) Color(0xFF7C4DFF) else Color.Transparent
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(14.dp)
                                        .fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("₹${option.amount}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("🪙 ${option.coins} Coins", color = Color(0xFFFCD116), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf(options[2], options[3]).forEach { option ->
                            val isSelected = selectedOption == option
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedOption = option },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFF7C4DFF).copy(alpha = 0.2f) else Color(0xFF161522)
                                ),
                                border = BorderStroke(
                                    width = 1.5.dp,
                                    color = if (isSelected) Color(0xFF7C4DFF) else Color.Transparent
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(14.dp)
                                        .fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("₹${option.amount}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("🪙 ${option.coins} Coins", color = Color(0xFFFCD116), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Input details
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF14121F)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "RECEIPIENT INFORMATION",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = holderName,
                            onValueChange = { holderName = it },
                            label = { Text("Account Holder Name") },
                            textStyle = TextStyle(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF7C4DFF),
                                unfocusedBorderColor = Color.Gray,
                                focusedLabelColor = Color(0xFF7C4DFF),
                                unfocusedLabelColor = Color.Gray
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("holder_name_input")
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = upiId,
                            onValueChange = { upiId = it },
                            label = { Text("UPI ID (e.g., recipient@upi)") },
                            textStyle = TextStyle(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF7C4DFF),
                                unfocusedBorderColor = Color.Gray,
                                focusedLabelColor = Color(0xFF7C4DFF),
                                unfocusedLabelColor = Color.Gray
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("upi_id_input")
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (selectedOption == null) {
                                    coroutineScope.launch { snackbarHostState.showSnackbar("Please select a withdrawal amount.") }
                                    return@Button
                                }
                                val option = selectedOption!!
                                if (holderName.trim().isEmpty()) {
                                    coroutineScope.launch { snackbarHostState.showSnackbar("Please enter account holder name.") }
                                    return@Button
                                }
                                if (upiId.trim().isEmpty()) {
                                    coroutineScope.launch { snackbarHostState.showSnackbar("Please enter your UPI ID.") }
                                    return@Button
                                }
                                if (!isValidUpi(upiId)) {
                                    coroutineScope.launch { snackbarHostState.showSnackbar("Invalid UPI ID format. Must match name@bank pattern.") }
                                    return@Button
                                }
                                if (wallet.coins < option.coins) {
                                    coroutineScope.launch { snackbarHostState.showSnackbar("Insufficient Coins") }
                                    return@Button
                                }
                                if (todayRequestsCount >= 3) {
                                    coroutineScope.launch { snackbarHostState.showSnackbar("Limit Exceeded: Maximum 3 requests per day.") }
                                    return@Button
                                }

                                viewModel.submitWithdraw(
                                    upiId = upiId.trim(),
                                    holderName = holderName.trim(),
                                    coinAmount = option.coins,
                                    rupeeAmount = option.amount,
                                    onSuccess = {
                                        showSuccessDialog = true
                                    },
                                    onError = { errorText ->
                                        coroutineScope.launch { snackbarHostState.showSnackbar(errorText) }
                                    }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isSubmitting,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("submit_withdraw_button")
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text(
                                    "SUBMIT WITHDRAW REQUEST",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // Withdraw requests history section
            item {
                Text(
                    text = "WITHDRAW HISTORY",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            if (withdrawRequests.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF14121F))
                    ) {
                        Text(
                            "No withdrawal history found. Submit your first request above!",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(24.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(
                    count = withdrawRequests.size,
                    key = { index -> withdrawRequests[index].id }
                ) { index ->
                    val request = withdrawRequests[index]
                    
                    val statusColor = when (request.status) {
                        "Approved" -> Color(0xFF00C853)
                        "Rejected" -> Color(0xFFFF3D00)
                        else -> Color(0xFFFFD600)
                    }

                    val sdf = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US) }
                    val dateStr = sdf.format(Date(request.timestamp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161522)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Amount: ₹${request.amount}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("Spent: 🪙 ${request.coinsSpent} Coins", color = Color(0xFFFCD116), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Box(
                                    modifier = Modifier
                                        .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = request.status.uppercase(Locale.US),
                                        color = statusColor,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("UPI ID: ${request.upiId}", color = Color.LightGray, fontSize = 12.sp)
                            Text("Account: ${request.userName}", color = Color.LightGray, fontSize = 12.sp)
                            Text("Date: $dateStr", color = Color.Gray, fontSize = 11.sp)
                            
                            if (request.remarks.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = "Remark: ${request.remarks}",
                                        color = Color.LightGray,
                                        fontSize = 11.sp,
                                        fontStyle = FontStyle.Italic
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminDailyCheckInSection(
    viewModel: com.playwin.app.ui.viewmodel.PlayWinViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.dailyCheckInSettingsState.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsersState.collectAsStateWithLifecycle()
    val serverTimeOffset = viewModel.serverTimeOffset

    var localEnabled by remember { mutableStateOf(settings?.enabled ?: true) }
    var localDay1 by remember { mutableStateOf((settings?.rewards?.getOrNull(0) ?: 20).toString()) }
    var localDay2 by remember { mutableStateOf((settings?.rewards?.getOrNull(1) ?: 30).toString()) }
    var localDay3 by remember { mutableStateOf((settings?.rewards?.getOrNull(2) ?: 40).toString()) }
    var localDay4 by remember { mutableStateOf((settings?.rewards?.getOrNull(3) ?: 50).toString()) }
    var localDay5 by remember { mutableStateOf((settings?.rewards?.getOrNull(4) ?: 60).toString()) }
    var localDay6 by remember { mutableStateOf((settings?.rewards?.getOrNull(5) ?: 80).toString()) }
    var localDay7 by remember { mutableStateOf((settings?.rewards?.getOrNull(6) ?: 120).toString()) }
    var localMaxLimit by remember { mutableStateOf((settings?.maxRewardLimit ?: 500).toString()) }

    var showResetConfirmation by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Sync from database if it changes
    LaunchedEffect(settings) {
        settings?.let { s ->
            localEnabled = s.enabled
            val r = s.rewards ?: listOf(20, 30, 40, 50, 60, 80, 120)
            localDay1 = (r.getOrNull(0) ?: 20).toString()
            localDay2 = (r.getOrNull(1) ?: 30).toString()
            localDay3 = (r.getOrNull(2) ?: 40).toString()
            localDay4 = (r.getOrNull(3) ?: 50).toString()
            localDay5 = (r.getOrNull(4) ?: 60).toString()
            localDay6 = (r.getOrNull(5) ?: 80).toString()
            localDay7 = (r.getOrNull(6) ?: 120).toString()
            localMaxLimit = s.maxRewardLimit.toString()
        }
    }

    // Calculations for Stats
    val now = System.currentTimeMillis() + serverTimeOffset
    val totalClaimsCount = allUsers.sumOf { it.dailyCheckIn?.totalClaims ?: 0 }
    val todayClaimsCount = allUsers.count { (it.dailyCheckIn?.lastClaimTimestamp ?: 0L) >= now - 86400000L }
    val completionCount = allUsers.count { (it.dailyCheckIn?.streak ?: 0) >= 7 }
    val activeCount = allUsers.count { it.lastActiveTime >= now - 7 * 86400000L }
    val inactiveCount = allUsers.size - activeCount

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- STATISTICS CARD ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF130E26)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DAILY CHECK-IN STATS",
                        color = Color(0xFFE212D1),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Total Claims", color = Color.Gray, fontSize = 11.sp)
                            Text("$totalClaimsCount", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Today's Claims", color = Color.Gray, fontSize = 11.sp)
                            Text("$todayClaimsCount", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("7-Day Comps", color = Color.Gray, fontSize = 11.sp)
                            Text("$completionCount", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Active (7 Days)", color = Color.Gray, fontSize = 11.sp)
                            Text("$activeCount Users", color = Color(0xFF22C55E), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Inactive Users", color = Color.Gray, fontSize = 11.sp)
                            Text("$inactiveCount Users", color = Color(0xFFEF4444), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- ENABLE/DISABLE SWITCH ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF130E26), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Daily Check-In System", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Turn on/off check-in globally", color = Color.Gray, fontSize = 11.sp)
                }
                Switch(
                    checked = localEnabled,
                    onCheckedChange = { localEnabled = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF7C4DFF))
                )
            }
        }

        // --- REWARD VALUE FIELDS ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF130E26)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "REWARD CONFIGURATION (COINS)",
                        color = Color(0xFFE212D1),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val dayFields = listOf(
                        "Day 1" to localDay1,
                        "Day 2" to localDay2,
                        "Day 3" to localDay3,
                        "Day 4" to localDay4,
                        "Day 5" to localDay5,
                        "Day 6" to localDay6,
                        "Day 7" to localDay7
                    )

                    dayFields.forEachIndexed { index, (label, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            OutlinedTextField(
                                value = value,
                                onValueChange = { newVal ->
                                    val sanitized = newVal.filter { it.isDigit() }
                                    when (index) {
                                        0 -> localDay1 = sanitized
                                        1 -> localDay2 = sanitized
                                        2 -> localDay3 = sanitized
                                        3 -> localDay4 = sanitized
                                        4 -> localDay5 = sanitized
                                        5 -> localDay6 = sanitized
                                        6 -> localDay7 = sanitized
                                    }
                                },
                                singleLine = true,
                                textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                                modifier = Modifier.width(100.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Max Limit (Cap)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = localMaxLimit,
                            onValueChange = { localMaxLimit = it.filter { c -> c.isDigit() } },
                            singleLine = true,
                            textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                            modifier = Modifier.width(100.dp)
                        )
                    }
                }
            }
        }

        // --- PREVIEW SECTION ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF090616)),
                border = BorderStroke(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "LIVE USER PREVIEW",
                        color = Color(0xFFFFEA3D),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val previewRewards = listOf(
                            localDay1.toIntOrNull() ?: 0,
                            localDay2.toIntOrNull() ?: 0,
                            localDay3.toIntOrNull() ?: 0,
                            localDay4.toIntOrNull() ?: 0,
                            localDay5.toIntOrNull() ?: 0,
                            localDay6.toIntOrNull() ?: 0,
                            localDay7.toIntOrNull() ?: 0
                        )
                        previewRewards.forEachIndexed { index, coins ->
                            DailyRewardBox(
                                day = "Day ${index + 1}",
                                rewardCoins = coins,
                                status = if (index == 2) CheckInDayStatus.ACTIVE else if (index < 2) CheckInDayStatus.COMPLETED else CheckInDayStatus.LOCKED,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // --- SAVE / ACTION BUTTONS ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        val finalSettings = com.playwin.app.data.model.FirebaseDailyCheckInSettings(
                            enabled = localEnabled,
                            rewards = listOf(
                                localDay1.toIntOrNull() ?: 20,
                                localDay2.toIntOrNull() ?: 30,
                                localDay3.toIntOrNull() ?: 40,
                                localDay4.toIntOrNull() ?: 50,
                                localDay5.toIntOrNull() ?: 60,
                                localDay6.toIntOrNull() ?: 80,
                                localDay7.toIntOrNull() ?: 120
                            ),
                            maxRewardLimit = localMaxLimit.toIntOrNull() ?: 500
                        )
                        viewModel.adminUpdateDailyCheckInSettings(finalSettings) { success ->
                            statusMessage = if (success) "Settings Saved Successfully!" else "Failed to save settings."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("SAVE SETTINGS", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { showResetConfirmation = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("RESET ALL USERS' STREAKS", fontWeight = FontWeight.Bold)
                }
            }
        }

        statusMessage?.let { msg ->
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1B0F33), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(msg, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // --- RESET CONFIRMATION DIALOG ---
    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetConfirmation = false },
            title = { Text("Reset All Streaks?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to reset all users' streaks back to Day 1? This action is irreversible.", color = Color.LightGray) },
            containerColor = Color(0xFF130E26),
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirmation = false
                        viewModel.adminResetAllUsersDailyCheckIn { success ->
                            statusMessage = if (success) "All users' streaks have been reset!" else "Failed to reset streaks."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("YES, RESET", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmation = false }) {
                    Text("CANCEL", color = Color.Gray)
                }
            }
        )
    }
}
