package com.myplaywin.app.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import android.app.Activity
import android.content.ContextWrapper
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.awaitFirstDown
import com.myplaywin.app.ui.viewmodel.PlayWinViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.abs
import kotlin.random.Random
import android.os.Vibrator
import android.os.VibrationEffect
import android.os.Build
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.graphics.StrokeJoin

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}

class SnakeSoundManager(context: Context) {
    private val prefs = context.getSharedPreferences("snake_sound_prefs", Context.MODE_PRIVATE)
    var isSoundEnabled by mutableStateOf(prefs.getBoolean("sound_enabled", true))
        private set

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var bgmJob: Job? = null
    private var isBgmPlaying = false
    var isFruitFrenzyPlaying by mutableStateOf(false)

    // Dynamic states for background music
    var currentThemeId by mutableStateOf("NEON_CITY")
    var isBossLevel by mutableStateOf(false)
    var isBonusLevel by mutableStateOf(false)

    fun toggleSoundEnabled(enabled: Boolean) {
        isSoundEnabled = enabled
        prefs.edit().putBoolean("sound_enabled", enabled).apply()
        if (!enabled) {
            stopBgm()
        }
    }

    fun startBgm() {
        if (!isSoundEnabled) return
        stopBgm()
        isBgmPlaying = true
        bgmJob = scope.launch {
            playBgmLoop()
        }
    }

    fun pauseBgm() {
        isBgmPlaying = false
        bgmJob?.cancel()
        bgmJob = null
    }

    fun resumeBgm() {
        if (!isSoundEnabled) return
        if (bgmJob == null) {
            isBgmPlaying = true
            bgmJob = scope.launch {
                playBgmLoop()
            }
        }
    }

    fun stopBgm() {
        isBgmPlaying = false
        bgmJob?.cancel()
        bgmJob = null
    }

    fun cleanup() {
        stopBgm()
        scope.cancel()
    }

    private suspend fun playBgmLoop() {
        val sampleRate = 22050
        var bgmTrack: AudioTrack? = null
        try {
            val minBufSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            bgmTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufSize.coerceAtLeast(4096),
                AudioTrack.MODE_STREAM
            )
            bgmTrack.play()

            // Soft cyber pentatonic melody
            val melodyNeon = doubleArrayOf(329.63, 392.00, 440.00, 392.00, 329.63, 293.66, 261.63, 0.0)
            val durationsNeon = intArrayOf(400, 400, 400, 400, 400, 400, 600, 200)

            // Forest: deep natural slow pentatonic scale
            val melodyForest = doubleArrayOf(261.63, 293.66, 329.63, 392.00, 329.63, 293.66, 261.63, 0.0)
            val durationsForest = intArrayOf(600, 600, 600, 600, 600, 600, 800, 400)

            // Snow World: crisp ice high chime
            val melodySnow = doubleArrayOf(523.25, 587.33, 659.25, 783.99, 659.25, 587.33, 523.25, 0.0)
            val durationsSnow = intArrayOf(250, 250, 250, 250, 250, 250, 400, 150)

            // Volcano: low dark dramatic warning scale
            val melodyVolcano = doubleArrayOf(130.81, 138.59, 146.83, 138.59, 130.81, 123.47, 110.00, 0.0)
            val durationsVolcano = intArrayOf(500, 500, 500, 500, 500, 500, 700, 300)

            // Desert: mystic Arabian scale
            val melodyDesert = doubleArrayOf(293.66, 311.13, 369.99, 392.00, 369.99, 311.13, 293.66, 0.0)
            val durationsDesert = intArrayOf(450, 450, 450, 450, 450, 450, 600, 200)

            // Ocean: slow floating ambient swells
            val melodyOcean = doubleArrayOf(349.23, 440.00, 523.25, 440.00, 349.23, 293.66, 329.63, 0.0)
            val durationsOcean = intArrayOf(800, 800, 800, 800, 800, 800, 1200, 400)

            // Space: drifting sci-fi tritone
            val melodySpace = doubleArrayOf(493.88, 698.46, 587.33, 493.88, 392.00, 554.37, 440.00, 0.0)
            val durationsSpace = intArrayOf(350, 350, 350, 350, 350, 350, 500, 150)

            // Cyber Grid: fast retro 8-bit chip tunes
            val melodyCyber = doubleArrayOf(523.25, 659.25, 587.33, 698.46, 659.25, 783.99, 1046.50, 0.0)
            val durationsCyber = intArrayOf(200, 200, 200, 200, 200, 200, 300, 100)

            // Ancient Temple: slow meditative octaves
            val melodyAncient = doubleArrayOf(220.00, 330.00, 440.00, 330.00, 220.00, 165.00, 196.00, 0.0)
            val durationsAncient = intArrayOf(700, 700, 700, 700, 700, 700, 900, 300)

            // Dragon Kingdom: bold royal marching theme
            val melodyDragon = doubleArrayOf(261.63, 329.63, 392.00, 523.25, 440.00, 392.00, 440.00, 0.0)
            val durationsDragon = intArrayOf(300, 300, 300, 500, 300, 300, 600, 200)

            // Boss Theme: intense urgent chromatic diminished scale
            val melodyBoss = doubleArrayOf(146.83, 155.56, 164.81, 155.56, 146.83, 138.59, 130.81, 123.47)
            val durationsBoss = intArrayOf(220, 220, 220, 220, 220, 220, 220, 220)

            // Bonus Theme: super cheerful, rapid major arpeggios
            val melodyBonus = doubleArrayOf(523.25, 659.25, 783.99, 1046.50, 783.99, 659.25, 523.25, 783.99)
            val durationsBonus = intArrayOf(160, 160, 160, 160, 160, 160, 160, 160)

            var index = 0
            while (isBgmPlaying && isSoundEnabled) {
                val currentMelody = when {
                    isBossLevel -> melodyBoss
                    isBonusLevel -> melodyBonus
                    currentThemeId == "FOREST" -> melodyForest
                    currentThemeId == "SNOW_WORLD" -> melodySnow
                    currentThemeId == "VOLCANO" -> melodyVolcano
                    currentThemeId == "DESERT" -> melodyDesert
                    currentThemeId == "OCEAN" -> melodyOcean
                    currentThemeId == "SPACE" -> melodySpace
                    currentThemeId == "CYBER_GRID" -> melodyCyber
                    currentThemeId == "ANCIENT_TEMPLE" -> melodyAncient
                    currentThemeId == "DRAGON_KINGDOM" -> melodyDragon
                    else -> melodyNeon
                }
                
                val currentDurations = when {
                    isBossLevel -> durationsBoss
                    isBonusLevel -> durationsBonus
                    currentThemeId == "FOREST" -> durationsForest
                    currentThemeId == "SNOW_WORLD" -> durationsSnow
                    currentThemeId == "VOLCANO" -> durationsVolcano
                    currentThemeId == "DESERT" -> durationsDesert
                    currentThemeId == "OCEAN" -> durationsOcean
                    currentThemeId == "SPACE" -> durationsSpace
                    currentThemeId == "CYBER_GRID" -> durationsCyber
                    currentThemeId == "ANCIENT_TEMPLE" -> durationsAncient
                    currentThemeId == "DRAGON_KINGDOM" -> durationsDragon
                    else -> durationsNeon
                }

                var freq = currentMelody[index % currentMelody.size]
                var duration = currentDurations[index % currentDurations.size]
                if (isFruitFrenzyPlaying) {
                    freq *= 1.25
                    duration = (duration * 0.75).toInt()
                }

                val volume = when {
                    isBossLevel -> 0.05f
                    isBonusLevel -> 0.04f
                    currentThemeId == "VOLCANO" -> 0.06f
                    else -> 0.035f
                }
                
                val samples = generateNoteSamples(freq, duration, volume = volume, sampleRate = sampleRate)

                bgmTrack.write(samples, 0, samples.size)
                index = (index + 1) % currentMelody.size
                delay(10)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                bgmTrack?.stop()
                bgmTrack?.release()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private fun generateNoteSamples(frequency: Double, durationMs: Int, volume: Float, sampleRate: Int = 22050): ShortArray {
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val samples = ShortArray(numSamples)
        if (frequency == 0.0) return samples

        val attackSamples = (numSamples * 0.1).toInt()
        val releaseSamples = (numSamples * 0.15).toInt()

        for (i in 0 until numSamples) {
            val time = i.toDouble() / sampleRate
            val wave = sin(2.0 * PI * frequency * time)

            val envelope: Double = when {
                i < attackSamples -> i.toDouble() / attackSamples
                i > numSamples - releaseSamples -> (numSamples - i).toDouble() / releaseSamples
                else -> 1.0
            }

            val value = (wave * Short.MAX_VALUE * volume * envelope).toInt()
            samples[i] = value.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return samples
    }

    fun playTeleport() {
        playSfx(
            freqs = doubleArrayOf(587.33, 880.00, 1174.66),
            durations = intArrayOf(60, 60, 120),
            volume = 0.15f,
            waveType = "SINE"
        )
    }

    fun playWallBreak() {
        playSfx(
            freqs = doubleArrayOf(220.0, 165.0, 110.0),
            durations = intArrayOf(80, 80, 120),
            volume = 0.28f,
            waveType = "SQUARE"
        )
    }

    private fun playSfx(
        freqs: DoubleArray,
        durations: IntArray,
        volume: Float = 0.3f,
        waveType: String = "SINE"
    ) {
        if (!isSoundEnabled) return
        scope.launch {
            var audioTrack: AudioTrack? = null
            try {
                val sampleRate = 22050
                var totalSamples = 0
                for (d in durations) {
                    totalSamples += (sampleRate * (d / 1000.0)).toInt()
                }
                val buffer = ShortArray(totalSamples)
                var offset = 0

                for (i in freqs.indices) {
                    val f = freqs[i]
                    val d = durations[i]
                    val numSamples = (sampleRate * (d / 1000.0)).toInt()

                    for (j in 0 until numSamples) {
                        val time = j.toDouble() / sampleRate
                        val wave = when (waveType) {
                            "SINE" -> sin(2.0 * PI * f * time)
                            "SQUARE" -> if (sin(2.0 * PI * f * time) >= 0.0) 1.0 else -1.0
                            else -> sin(2.0 * PI * f * time)
                        }

                        val decay = (numSamples - j).toDouble() / numSamples
                        val value = (wave * Short.MAX_VALUE * volume * decay).toInt()
                        if (offset + j < buffer.size) {
                            buffer[offset + j] = value.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                        }
                    }
                    offset += numSamples
                }

                audioTrack = AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    buffer.size * 2,
                    AudioTrack.MODE_STATIC
                )
                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()

                val totalDurationMs = durations.sum()
                delay(totalDurationMs.toLong() + 50)

                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playGameStart() {
        playSfx(
            freqs = doubleArrayOf(523.25, 659.25, 783.99, 1046.50),
            durations = intArrayOf(80, 80, 80, 150),
            volume = 0.2f,
            waveType = "SINE"
        )
    }

    fun playCountdown(number: Int) {
        if (number == 0) {
            playSfx(
                freqs = doubleArrayOf(880.0, 1000.0),
                durations = intArrayOf(100, 150),
                volume = 0.25f,
                waveType = "SINE"
            )
        } else {
            playSfx(
                freqs = doubleArrayOf(440.0),
                durations = intArrayOf(120),
                volume = 0.2f,
                waveType = "SINE"
            )
        }
    }

    fun playSwipe() {
        playSfx(
            freqs = doubleArrayOf(350.0),
            durations = intArrayOf(30),
            volume = 0.08f,
            waveType = "SINE"
        )
    }

    fun playFoodCollected() {
        playSfx(
            freqs = doubleArrayOf(659.25, 987.77),
            durations = intArrayOf(50, 120),
            volume = 0.25f,
            waveType = "SINE"
        )
    }

    fun playGrowth() {
        playSfx(
            freqs = doubleArrayOf(400.0, 200.0),
            durations = intArrayOf(30, 40),
            volume = 0.18f,
            waveType = "SINE"
        )
    }

    fun playLevelUp() {
        playSfx(
            freqs = doubleArrayOf(523.25, 659.25, 783.99, 1046.50, 1318.51),
            durations = intArrayOf(60, 60, 60, 60, 250),
            volume = 0.3f,
            waveType = "SINE"
        )
    }

    fun playCoinReward() {
        playSfx(
            freqs = doubleArrayOf(987.77, 1318.51),
            durations = intArrayOf(70, 250),
            volume = 0.3f,
            waveType = "SINE"
        )
    }

    fun playButtonClick() {
        playSfx(
            freqs = doubleArrayOf(220.0),
            durations = intArrayOf(20),
            volume = 0.12f,
            waveType = "SINE"
        )
    }

    fun playPause() {
        playSfx(
            freqs = doubleArrayOf(400.0, 300.0, 200.0),
            durations = intArrayOf(50, 50, 80),
            volume = 0.18f,
            waveType = "SINE"
        )
    }

    fun playResume() {
        playSfx(
            freqs = doubleArrayOf(200.0, 300.0, 400.0),
            durations = intArrayOf(50, 50, 80),
            volume = 0.18f,
            waveType = "SINE"
        )
    }

    fun playGameOver() {
        playSfx(
            freqs = doubleArrayOf(196.0, 155.56, 130.81),
            durations = intArrayOf(120, 120, 300),
            volume = 0.3f,
            waveType = "SINE"
        )
    }

    fun playNewBestScore() {
        playSfx(
            freqs = doubleArrayOf(523.25, 783.99, 1046.50, 1567.98, 1046.50),
            durations = intArrayOf(80, 80, 80, 80, 400),
            volume = 0.35f,
            waveType = "SINE"
        )
    }
}

class SnakeHapticManager(context: Context) {
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    fun vibrateLight() {
        try {
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(15)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun vibrateMedium() {
        try {
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(45)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun vibrateSuccess() {
        try {
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 50, 50, 80), intArrayOf(0, 180, 0, 255), -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 50, 50, 80), -1)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun vibrateGameOver() {
        try {
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 150, 80, 250), intArrayOf(0, 200, 0, 150), -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 150, 80, 250), -1)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

enum class SnakeDirection { UP, DOWN, LEFT, RIGHT }
data class SnakePoint(val x: Int, val y: Int)

data class SnakeGameHistoryEntry(
    val date: String,
    val score: Int,
    val coins: Int
)

enum class SnakeEntityType {
    SHIELD, MAGNET, SLOW, DOUBLE_COINS, CHEST
}

data class SnakeEntity(
    val point: SnakePoint,
    val type: SnakeEntityType,
    val ageMs: Long = 0L
)

data class SnakeParticle(
    var x: Float,
    var y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    var alpha: Float,
    var size: Float,
    val maxLife: Int,
    var currentLife: Int = 0
)

data class FloatingText(
    val text: String,
    var x: Float,
    var y: Float,
    var alpha: Float,
    val maxLife: Int,
    var currentLife: Int = 0
)

data class SnakeGameStateSnapshot(
    val snake: List<SnakePoint>,
    val previousSnake: List<SnakePoint>,
    val direction: SnakeDirection,
    val previousDirection: SnakeDirection,
    val food: SnakePoint,
    val score: Int,
    val levelFruitsCollected: Int,
    val fruitsCollectedThisGame: Int,
    val currentLevelNumber: Int,
    val isAdventureMode: Boolean,
    val activeLevel: SnakeLevel?,
    val breakableWalls: List<SnakePoint>,
    val crystalPoint: SnakePoint?,
    val bossPosition: SnakePoint,
    val bossDirection: SnakeDirection,
    val bossTickCounter: Int,
    val activeTraps: List<SnakePoint>,
    val bonusTimeLeftSeconds: Int,
    val levelTimeElapsedSeconds: Int,
    val hasShield: Boolean,
    val shieldTimeLeft: Int,
    val isDoubleCoinsActive: Boolean,
    val doubleCoinsTimeLeft: Int,
    val magnetTimeLeft: Int,
    val slowMotionTimeLeft: Int,
    val fruitFrenzyTimeLeft: Int,
    val extraFruits: List<SnakePoint>,
    val activePowerUpOnBoard: SnakeEntity?
)

@Composable
fun SnakeClassicScreen(
    viewModel: PlayWinViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Instantiate and clean up sound manager
    val soundManager = remember { SnakeSoundManager(context) }
    DisposableEffect(Unit) {
        onDispose {
            soundManager.cleanup()
        }
    }

    // Haptic feedback manager
    val hapticManager = remember { SnakeHapticManager(context) }

    // Screen Shake Animatable offsets
    val shakeOffsetX = remember { Animatable(0f) }
    val shakeOffsetY = remember { Animatable(0f) }

    // Glow Flash alpha
    val boardFlashAlpha = remember { Animatable(0f) }

    // Particles and floating texts
    val particles = remember { mutableStateListOf<SnakeParticle>() }
    val floatingTexts = remember { mutableStateListOf<FloatingText>() }

    // Snake eyes blinking state
    var isBlinking by remember { mutableStateOf(false) }

    // Death animation states
    var isDeadAnimating by remember { mutableStateOf(false) }
    var isDeadEyeClosed by remember { mutableStateOf(false) }
    val snakeDeathShake = remember { Animatable(0f) }
    val snakeDeathAlpha = remember { Animatable(1f) }

    // Save previous movement direction for turning interpolation
    var previousDirection by remember { mutableStateOf(SnakeDirection.UP) }

    // Preferences for score & history
    val prefs = remember { context.getSharedPreferences("snake_game_prefs", Context.MODE_PRIVATE) }
    var highScore by remember { mutableStateOf(prefs.getInt("high_score", 0)) }
    var highestLevel by remember { mutableStateOf(prefs.getInt("highest_level", 1)) }
    var highestSpeed by remember { mutableStateOf(prefs.getFloat("highest_speed", 1.0f)) }
    var showLevelUpOverlay by remember { mutableStateOf(false) }
    
    // Game history parsing from simple semi-colon separated string (Date|Score|Coins)
    var historyList by remember {
        mutableStateOf(parseHistory(prefs.getString("game_history", "") ?: ""))
    }

    // --- Progression & Achievements States ---
    var currentScreenState by remember { mutableStateOf("LOBBY") }
    val view = LocalView.current

    // Immersive Sticky Full-Screen Management for Gameplay
    DisposableEffect(currentScreenState) {
        val activity = context.findActivity()
        if (activity != null) {
            val window = activity.window
            val insetsController = WindowCompat.getInsetsController(window, view)
            if (currentScreenState == "GAMEPLAY") {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                insetsController.hide(
                    WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars()
                )
            } else {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                insetsController.show(
                    WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars()
                )
            }
        }
        onDispose {
            val activity = context.findActivity()
            if (activity != null) {
                val window = activity.window
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.show(
                    WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars()
                )
            }
        }
    }
    var dailyMissions by remember { mutableStateOf(SnakeProgressionManager.loadMissions(context)) }
    var achievements by remember { mutableStateOf(SnakeProgressionManager.loadAchievements(context)) }
    var stats by remember { mutableStateOf(SnakeProgressionManager.loadStats(context)) }

    // --- Adventure Mode & Level States ---
    var isAdventureMode by remember { mutableStateOf(false) }
    var currentLevelNumber by remember { mutableStateOf(1) }
    var levelFruitsCollected by remember { mutableStateOf(0) }
    var levelTimeElapsedSeconds by remember { mutableStateOf(0) }
    var activeLevel by remember { mutableStateOf<SnakeLevel?>(null) }
    var unlockedLevel by remember { mutableStateOf(SnakeProgressionManager.loadUnlockedLevel(context)) }
    val currentBreakableWalls = remember { mutableStateListOf<SnakePoint>() }
    var crystalPoint by remember { mutableStateOf<SnakePoint?>(null) }
    var isLevelCompleted by remember { mutableStateOf(false) }
    var isSlidingIce by remember { mutableStateOf(false) }
    var windMoveCounter by remember { mutableStateOf(0) }
    
    // Boss States
    var bossPosition by remember { mutableStateOf(SnakePoint(10, 5)) }
    var bossDirection by remember { mutableStateOf(SnakeDirection.RIGHT) }
    var bossTickCounter by remember { mutableStateOf(0) }
    val activeTraps = remember { mutableStateListOf<SnakePoint>() }
    var bonusTimeLeftSeconds by remember { mutableStateOf(30) }

    var fruitsCollectedThisGame by remember { mutableStateOf(0) }
    var wasPausedThisGame by remember { mutableStateOf(false) }
    var gameStartTimeMs by remember { mutableStateOf(0L) }
    var pausedDurationMs by remember { mutableStateOf(0L) }
    var lastPauseTimestamp by remember { mutableStateOf(0L) }

    LaunchedEffect(currentScreenState) {
        if (currentScreenState == "LOBBY") {
            unlockedLevel = SnakeProgressionManager.loadUnlockedLevel(context)
            stats = SnakeProgressionManager.loadStats(context)
            achievements = SnakeProgressionManager.loadAchievements(context)
            dailyMissions = SnakeProgressionManager.loadMissions(context)
        }
    }

    LaunchedEffect(showLevelUpOverlay) {
        if (showLevelUpOverlay) {
            soundManager.pauseBgm()
            soundManager.playLevelUp()
        }
    }

    LaunchedEffect(isLevelCompleted) {
        if (isLevelCompleted) {
            soundManager.pauseBgm()
        }
    }

    var hasShield by remember { mutableStateOf(false) }
    var isDoubleCoinsActive by remember { mutableStateOf(false) }
    var bonusFood by remember { mutableStateOf<SnakePoint?>(null) }

    // Phase 3 States
    var shieldTimeLeft by remember { mutableStateOf(0) }
    var magnetTimeLeft by remember { mutableStateOf(0) }
    var slowMotionTimeLeft by remember { mutableStateOf(0) }
    var doubleCoinsTimeLeft by remember { mutableStateOf(0) }
    var fruitFrenzyTimeLeft by remember { mutableStateOf(0) }

    val extraFruits = remember { mutableStateListOf<SnakePoint>() }
    var activePowerUpOnBoard by remember { mutableStateOf<SnakeEntity?>(null) }
    var timeSinceLastSpawnSeconds by remember { mutableStateOf(0) }
    var nextSpawnIntervalSeconds by remember { mutableStateOf(Random.nextInt(30, 46)) }

    var showTreasureChestOverlay by remember { mutableStateOf(false) }
    var isTreasureChestOpened by remember { mutableStateOf(false) }
    var treasureChestRewardText by remember { mutableStateOf("") }
    var treasureChestRewardType by remember { mutableStateOf("") }

    var showMysteryBoxOverlay by remember { mutableStateOf(false) }
    var mysteryBoxRewardText by remember { mutableStateOf("") }
    var isMysteryBoxOpened by remember { mutableStateOf(false) }
    var activeCompletionNotification by remember { mutableStateOf<String?>(null) }

    // Grid Dimensions - Perfect vertical layout optimized for mobile screens
    val gridWidth = 20
    val gridHeight = 26

    // Game loop states
    var snake by remember {
        mutableStateOf(
            listOf(
                SnakePoint(10, 12),
                SnakePoint(10, 13),
                SnakePoint(10, 14)
            )
        )
    }
    
    // Previous snake state for smooth 60 FPS linear interpolation
    var previousSnake by remember {
        mutableStateOf(
            listOf(
                SnakePoint(10, 12),
                SnakePoint(10, 13),
                SnakePoint(10, 14)
            )
        )
    }

    // Animatable progress for grid movement interpolation
    val moveProgress = remember { Animatable(1f) }

    var direction by remember { mutableStateOf(SnakeDirection.UP) }
    val directionQueue = remember { mutableStateListOf<SnakeDirection>() }
    var food by remember { mutableStateOf(SnakePoint(5, 5)) }
    var isPaused by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }
    
    // Countdown State: 3, 2, 1, 0 (Go), -1 (Playing)
    var countdownState by remember { mutableStateOf(3) }
    
    // Reward claiming
    var rewardClaimed by remember { mutableStateOf(false) }

    // Rewarded Ad Extra Life States
    var hasUsedContinueThisGame by remember { mutableStateOf(false) }
    var showSecondChanceText by remember { mutableStateOf(false) }
    var isAdShieldActive by remember { mutableStateOf(false) }
    var lastDeathSnapshot by remember { mutableStateOf<SnakeGameStateSnapshot?>(null) }

    // Preload Rewarded Ad for Extra Life
    LaunchedEffect(Unit) {
        com.playwin.ads.RewardedManager.preload(context)
    }

    // Pulsing Food Glow Animation
    val infiniteTransition = rememberInfiniteTransition(label = "food_glow")
    val foodPulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Tongue protrusion animation
    val tongueProgress by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(280, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tongue"
    )

    // Speed calculation - comfortable Level 1 starting delay (450ms) decreasing smoothly by 8% speed multiplier per level
    val classicLevel = (fruitsCollectedThisGame / 5) + 1
    val speedMultiplier = 1.0f + (classicLevel - 1) * 0.08f
    val baseClassicDelay = (450f / speedMultiplier).toLong().coerceAtLeast(110L)
    
    val speedLevel = if (isAdventureMode) (score / 50) + 1 else classicLevel
    val speedProgress = if (isAdventureMode) {
        ((score / 10) % 5) / 5f
    } else {
        (fruitsCollectedThisGame % 5) / 5f
    }
    
    val currentDelay = if (isAdventureMode) {
        val baseVal = activeLevel?.baseDelay ?: 250L
        (baseVal * (if (slowMotionTimeLeft > 0) 1.67f else 1f)).toLong()
    } else {
        (baseClassicDelay * (if (slowMotionTimeLeft > 0) 1.67f else 1f)).toLong()
    }

    // Helper to check if direction turn is valid (not reverse and not same)
    fun isValidTurn(current: SnakeDirection, next: SnakeDirection): Boolean {
        return when (current) {
            SnakeDirection.UP -> next != SnakeDirection.DOWN && next != SnakeDirection.UP
            SnakeDirection.DOWN -> next != SnakeDirection.UP && next != SnakeDirection.DOWN
            SnakeDirection.LEFT -> next != SnakeDirection.RIGHT && next != SnakeDirection.LEFT
            SnakeDirection.RIGHT -> next != SnakeDirection.LEFT && next != SnakeDirection.RIGHT
        }
    }

    // Queue upcoming direction with a 1-move buffer
    fun tryQueueDirection(newDir: SnakeDirection) {
        if (currentScreenState != "GAMEPLAY" || isPaused || isGameOver || countdownState != -1) return
        val lastDir = directionQueue.lastOrNull() ?: direction
        if (isValidTurn(lastDir, newDir)) {
            if (directionQueue.size < 1) {
                directionQueue.add(newDir)
                soundManager.playSwipe()
            }
        }
    }

    var isNavigatingHome by remember { mutableStateOf(false) }

    fun exitGameToHome() {
        if (isNavigatingHome) return
        isNavigatingHome = true

        soundManager.playButtonClick()
        hapticManager.vibrateLight()
        soundManager.stopBgm()

        // Stop active power-up timers & states
        shieldTimeLeft = 0
        magnetTimeLeft = 0
        slowMotionTimeLeft = 0
        doubleCoinsTimeLeft = 0
        fruitFrenzyTimeLeft = 0
        extraFruits.clear()
        activePowerUpOnBoard = null
        hasShield = false
        isDoubleCoinsActive = false
        bonusFood = null
        soundManager.isFruitFrenzyPlaying = false

        // Clear temporary overlays & game state
        showMysteryBoxOverlay = false
        showTreasureChestOverlay = false
        showLevelUpOverlay = false
        particles.clear()
        floatingTexts.clear()
        directionQueue.clear()
        isDeadAnimating = false
        isDeadEyeClosed = false

        // Close game over and transition state to LOBBY
        rewardClaimed = false
        isGameOver = false
        isPaused = true
        currentScreenState = "LOBBY"

        // Refresh Home screen data
        unlockedLevel = SnakeProgressionManager.loadUnlockedLevel(context)
        stats = SnakeProgressionManager.loadStats(context)
        achievements = SnakeProgressionManager.loadAchievements(context)
        dailyMissions = SnakeProgressionManager.loadMissions(context)
        highScore = prefs.getInt("high_score", 0)
        highestLevel = prefs.getInt("highest_level", 1)
        historyList = parseHistory(prefs.getString("game_history", "") ?: "")

        isNavigatingHome = false
    }

    BackHandler(enabled = true) {
        if (isGameOver) {
            exitGameToHome()
        } else if (showMysteryBoxOverlay) {
            showMysteryBoxOverlay = false
            isPaused = false
        } else if (showLevelUpOverlay) {
            showLevelUpOverlay = false
        } else if (currentScreenState == "GAMEPLAY") {
            exitGameToHome()
        } else {
            onBack()
        }
    }

    // Pause gameplay when the app loses focus
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_PAUSE) {
                if (!isGameOver && countdownState == -1) {
                    isPaused = true
                    soundManager.pauseBgm()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    fun isPointBlocked(pt: SnakePoint): Boolean {
        // 1. Grid boundary check
        if (pt.x < 0 || pt.x >= gridWidth || pt.y < 0 || pt.y >= gridHeight) return true

        // 2. Snake head & body
        if (snake.contains(pt)) return true

        // 3. Main food
        if (pt == food) return true

        // 4. Bonus food
        if (bonusFood != null && pt == bonusFood) return true

        // 5. Extra fruits
        if (extraFruits.contains(pt)) return true

        // 6. Active power up
        if (activePowerUpOnBoard != null && activePowerUpOnBoard?.point == pt) return true

        // Adventure mode checks
        if (isAdventureMode && activeLevel != null) {
            val lvl = activeLevel!!
            // 7. Static obstacles
            if (lvl.obstacles.contains(pt)) return true

            // 8. Breakable walls (currently active breakable walls)
            if (currentBreakableWalls.contains(pt)) return true

            // 9. Lava tiles
            if (lvl.lavaTiles.contains(pt)) return true

            // 10. Portals (p1 and p2)
            for (pair in lvl.portals) {
                if (pt == pair.first || pt == pair.second) return true
            }

            // 11. Active traps (spawned by Boss)
            if (activeTraps.contains(pt)) return true

            // 12. Boss position
            if (lvl.isBoss && bossPosition == pt) return true
        }

        return false
    }

    fun getReachablePoints(start: SnakePoint): Set<SnakePoint> {
        val reachable = mutableSetOf<SnakePoint>()
        val queue = java.util.ArrayDeque<SnakePoint>()
        
        queue.add(start)
        reachable.add(start)
        
        val blockers = mutableSetOf<SnakePoint>()
        if (isAdventureMode && activeLevel != null) {
            val lvl = activeLevel!!
            blockers.addAll(lvl.obstacles)
            blockers.addAll(currentBreakableWalls)
            blockers.addAll(lvl.lavaTiles)
            blockers.addAll(activeTraps)
            if (lvl.isBoss) {
                blockers.add(bossPosition)
            }
        }
        
        val portalsMap = mutableMapOf<SnakePoint, SnakePoint>()
        if (isAdventureMode && activeLevel != null) {
            activeLevel!!.portals.forEach { (p1, p2) ->
                portalsMap[p1] = p2
                portalsMap[p2] = p1
            }
        }
        
        var steps = 0
        while (queue.isNotEmpty() && steps < 1000) {
            val curr = queue.poll() ?: break
            steps++
            
            val warpDest = portalsMap[curr]
            if (warpDest != null && !reachable.contains(warpDest)) {
                reachable.add(warpDest)
                queue.add(warpDest)
                continue
            }
            
            val directions = listOf(
                SnakePoint(curr.x, curr.y - 1),
                SnakePoint(curr.x, curr.y + 1),
                SnakePoint(curr.x - 1, curr.y),
                SnakePoint(curr.x + 1, curr.y)
            )
            
            for (nextPt in directions) {
                var actualNextPt = nextPt
                if (isAdventureMode && activeLevel?.isBonus == true) {
                    actualNextPt = SnakePoint(
                        (nextPt.x + gridWidth) % gridWidth,
                        (nextPt.y + gridHeight) % gridHeight
                    )
                } else {
                    if (nextPt.x < 0 || nextPt.x >= gridWidth || nextPt.y < 0 || nextPt.y >= gridHeight) {
                        continue
                    }
                }
                
                if (!blockers.contains(actualNextPt) && !reachable.contains(actualNextPt)) {
                    reachable.add(actualNextPt)
                    queue.add(actualNextPt)
                }
            }
        }
        
        return reachable
    }

    fun findSafeEmptyPoint(): SnakePoint? {
        val startPt = snake.firstOrNull() ?: SnakePoint(gridWidth / 2, gridHeight / 2)
        val reachableSet = getReachablePoints(startPt)
        
        val allPoints = mutableListOf<SnakePoint>()
        for (x in 0 until gridWidth) {
            for (y in 0 until gridHeight) {
                allPoints.add(SnakePoint(x, y))
            }
        }
        allPoints.shuffle()
        
        val obstaclesSet = mutableSetOf<SnakePoint>()
        val portalsSet = mutableSetOf<SnakePoint>()
        if (isAdventureMode && activeLevel != null) {
            val lvl = activeLevel!!
            obstaclesSet.addAll(lvl.obstacles)
            obstaclesSet.addAll(currentBreakableWalls)
            obstaclesSet.addAll(lvl.lavaTiles)
            obstaclesSet.addAll(activeTraps)
            if (lvl.isBoss) {
                obstaclesSet.add(bossPosition)
            }
            lvl.portals.forEach { (p1, p2) ->
                portalsSet.add(p1)
                portalsSet.add(p2)
            }
        }
        
        fun isAdjacentToRestricted(pt: SnakePoint): Boolean {
            if (pt.x == 0 || pt.x == gridWidth - 1 || pt.y == 0 || pt.y == gridHeight - 1) return true
            
            val adjacents = listOf(
                SnakePoint(pt.x, pt.y - 1),
                SnakePoint(pt.x, pt.y + 1),
                SnakePoint(pt.x - 1, pt.y),
                SnakePoint(pt.x + 1, pt.y)
            )
            for (adj in adjacents) {
                if (obstaclesSet.contains(adj) || portalsSet.contains(adj)) return true
            }
            return false
        }
        
        // PASS 1: Try to find a point that is NOT blocked, NOT adjacent to restricted areas, and reachable
        for (pt in allPoints) {
            if (!isPointBlocked(pt) && !isAdjacentToRestricted(pt) && reachableSet.contains(pt)) {
                return pt
            }
        }
        
        // PASS 2: If no candidate found, try to find any point that is NOT blocked and is reachable
        for (pt in allPoints) {
            if (!isPointBlocked(pt) && reachableSet.contains(pt)) {
                return pt
            }
        }
        
        // PASS 3: Fallback - if still nothing is reachable, just find any non-blocked point
        for (pt in allPoints) {
            if (!isPointBlocked(pt)) {
                return pt
            }
        }
        
        return null
    }

    // Helper to spawn food safely
    fun spawnFood() {
        findSafeEmptyPoint()?.let {
            food = it
        }
    }

    // Phase 3 Helpers
    fun findRandomEmptyPoint(): SnakePoint? {
        return findSafeEmptyPoint()
    }

    fun spawnRandomPowerUpOrChest() {
        if (currentScreenState != "GAMEPLAY" || isPaused || isGameOver || countdownState != -1) return
        
        // Either trigger Fruit Frenzy directly (15% chance) or spawn a physical power-up / chest on the board (85% chance)
        if (Random.nextFloat() < 0.15f) {
            // Start Fruit Frenzy!
            fruitFrenzyTimeLeft = 10
            soundManager.isFruitFrenzyPlaying = true
            
            extraFruits.clear()
            repeat(5) {
                findRandomEmptyPoint()?.let { extraFruits.add(it) }
            }
            
            soundManager.playLevelUp()
            hapticManager.vibrateSuccess()
            
            coroutineScope.launch {
                boardFlashAlpha.snapTo(0.55f)
                boardFlashAlpha.animateTo(0f, tween(350))
            }
            
            floatingTexts.add(
                FloatingText(
                    text = "FRUIT FRENZY! ⚡🍇",
                    x = gridWidth / 2f,
                    y = gridHeight / 2f,
                    alpha = 1f,
                    maxLife = 1800
                )
            )
        } else {
            val emptyPoint = findRandomEmptyPoint() ?: return
            
            val type = when (Random.nextInt(5)) {
                0 -> SnakeEntityType.SHIELD
                1 -> SnakeEntityType.MAGNET
                2 -> SnakeEntityType.SLOW
                3 -> SnakeEntityType.DOUBLE_COINS
                else -> SnakeEntityType.CHEST
            }
            
            // Spawn only one power-up at a time
            activePowerUpOnBoard = SnakeEntity(emptyPoint, type)
            
            soundManager.playGrowth()
            hapticManager.vibrateLight()
            
            floatingTexts.add(
                FloatingText(
                    text = "POWER-UP SPAWNED! ✨",
                    x = emptyPoint.x + 0.5f,
                    y = emptyPoint.y + 0.3f,
                    alpha = 1f,
                    maxLife = 1200
                )
            )
        }
    }

    fun collectPowerUp(powerUp: SnakeEntity) {
        soundManager.playCoinReward()
        hapticManager.vibrateSuccess()
        
        val pColor = when (powerUp.type) {
            SnakeEntityType.SHIELD -> Color(0xFF00E5FF)
            SnakeEntityType.MAGNET -> Color(0xFFE040FB)
            SnakeEntityType.SLOW -> Color(0xFF29B6F6)
            SnakeEntityType.DOUBLE_COINS -> Color(0xFFFFD700)
            SnakeEntityType.CHEST -> Color(0xFFFF9100)
        }
        
        // Beautiful glow splash particles on pick-up
        repeat(15) {
            val angle = Random.nextFloat() * 2f * PI.toFloat()
            val speed = Random.nextFloat() * 0.12f + 0.05f
            particles.add(
                SnakeParticle(
                    x = powerUp.point.x + 0.5f,
                    y = powerUp.point.y + 0.5f,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = pColor,
                    alpha = 1f,
                    size = Random.nextFloat() * 0.12f + 0.05f,
                    maxLife = Random.nextInt(350) + 250
                )
            )
        }
        
        when (powerUp.type) {
            SnakeEntityType.SHIELD -> {
                hasShield = true
                shieldTimeLeft = 30
                floatingTexts.add(
                    FloatingText(
                        text = "SHIELD ACTIVE! 🛡️",
                        x = powerUp.point.x + 0.5f,
                        y = powerUp.point.y + 0.3f,
                        alpha = 1f,
                        maxLife = 1200
                    )
                )
            }
            SnakeEntityType.MAGNET -> {
                magnetTimeLeft = 10
                floatingTexts.add(
                    FloatingText(
                        text = "MAGNET ACTIVE! 🧲",
                        x = powerUp.point.x + 0.5f,
                        y = powerUp.point.y + 0.3f,
                        alpha = 1f,
                        maxLife = 1200
                    )
                )
            }
            SnakeEntityType.SLOW -> {
                slowMotionTimeLeft = 8
                floatingTexts.add(
                    FloatingText(
                        text = "SLOW MOTION! ⏱️",
                        x = powerUp.point.x + 0.5f,
                        y = powerUp.point.y + 0.3f,
                        alpha = 1f,
                        maxLife = 1200
                    )
                )
            }
            SnakeEntityType.DOUBLE_COINS -> {
                doubleCoinsTimeLeft = 20
                floatingTexts.add(
                    FloatingText(
                        text = "2X COINS ACTIVE! 🪙",
                        x = powerUp.point.x + 0.5f,
                        y = powerUp.point.y + 0.3f,
                        alpha = 1f,
                        maxLife = 1200
                    )
                )
            }
            SnakeEntityType.CHEST -> {
                isPaused = true
                showTreasureChestOverlay = true
                isTreasureChestOpened = false
                treasureChestRewardText = ""
                treasureChestRewardType = ""
            }
        }
    }

    // Reset game state
    fun resetGame(levelToPlay: Int? = null) {
        val targetLvl = levelToPlay ?: SnakeProgressionManager.loadUnlockedLevel(context)
        soundManager.stopBgm()
        isDeadAnimating = false
        isDeadEyeClosed = false
        particles.clear()
        floatingTexts.clear()
        coroutineScope.launch {
            snakeDeathAlpha.snapTo(1f)
            snakeDeathShake.snapTo(0f)
            moveProgress.snapTo(1f)
            shakeOffsetX.snapTo(0f)
            shakeOffsetY.snapTo(0f)
            boardFlashAlpha.snapTo(0f)
        }
        
        isAdventureMode = true
        currentLevelNumber = targetLvl
        val lvl = generateLevelData(targetLvl)
        activeLevel = lvl
        
        // Set dynamic sound manager states
        soundManager.currentThemeId = lvl.theme.id
        soundManager.isBossLevel = lvl.isBoss
        soundManager.isBonusLevel = lvl.isBonus
        
        levelFruitsCollected = 0
        levelTimeElapsedSeconds = 0
        isLevelCompleted = false
        isSlidingIce = false
        windMoveCounter = 0
        
        // Set up breakable walls
        currentBreakableWalls.clear()
        currentBreakableWalls.addAll(lvl.breakableWalls)
        
        // Spawn crystal if breakable walls exist
        if (lvl.breakableWalls.isNotEmpty()) {
            crystalPoint = SnakePoint(Random.nextInt(2, 18), Random.nextInt(3, 23))
        } else {
            crystalPoint = null
        }
        
        // Setup Boss
        if (lvl.isBoss) {
            bossPosition = SnakePoint(10, 5)
            bossDirection = SnakeDirection.RIGHT
            bossTickCounter = 0
            activeTraps.clear()
        }
        
        // Setup Bonus timer
        if (lvl.isBonus) {
            bonusTimeLeftSeconds = 30
        }

        snake = listOf(
            SnakePoint(10, 12),
            SnakePoint(10, 13),
            SnakePoint(10, 14)
        )
        previousSnake = listOf(
            SnakePoint(10, 12),
            SnakePoint(10, 13),
            SnakePoint(10, 14)
        )
        direction = SnakeDirection.UP
        previousDirection = SnakeDirection.UP
        directionQueue.clear()
        score = 0
        isPaused = false
        isGameOver = false
        rewardClaimed = false
        countdownState = 3
        
        // Reset Phase 3 States
        shieldTimeLeft = 0
        magnetTimeLeft = 0
        slowMotionTimeLeft = 0
        doubleCoinsTimeLeft = 0
        fruitFrenzyTimeLeft = 0
        extraFruits.clear()
        activePowerUpOnBoard = null
        timeSinceLastSpawnSeconds = 0
        nextSpawnIntervalSeconds = Random.nextInt(30, 46)
        soundManager.isFruitFrenzyPlaying = false

        spawnFood()

        // Reset progression session states
        fruitsCollectedThisGame = 0
        wasPausedThisGame = false
        gameStartTimeMs = System.currentTimeMillis()
        pausedDurationMs = 0L
        lastPauseTimestamp = 0L
        hasShield = false
        isDoubleCoinsActive = false
        bonusFood = null
        hasUsedContinueThisGame = false
        showSecondChanceText = false
        isAdShieldActive = false
        lastDeathSnapshot = null
    }

    // Natural eye blinking loop
    LaunchedEffect(Unit) {
        while (true) {
            delay(Random.nextLong(2000, 5000))
            isBlinking = true
            delay(150)
            isBlinking = false
        }
    }

    // Particle and Floating Text 60 FPS update loop
    LaunchedEffect(Unit) {
        while (true) {
            delay(16)
            if (particles.isNotEmpty()) {
                val toRemove = mutableListOf<SnakeParticle>()
                particles.forEach { p ->
                    p.x += p.vx
                    p.y += p.vy
                    p.currentLife += 16
                    p.alpha = (1f - p.currentLife.toFloat() / p.maxLife).coerceIn(0f, 1f)
                    if (p.currentLife >= p.maxLife) {
                        toRemove.add(p)
                    }
                }
                if (toRemove.isNotEmpty()) {
                    particles.removeAll(toRemove)
                }
            }
            if (floatingTexts.isNotEmpty()) {
                val toRemove = mutableListOf<FloatingText>()
                floatingTexts.forEach { ft ->
                    ft.y -= 0.008f // Float upwards slowly in grid units
                    ft.currentLife += 16
                    ft.alpha = (1f - ft.currentLife.toFloat() / ft.maxLife).coerceIn(0f, 1f)
                    if (ft.currentLife >= ft.maxLife) {
                        toRemove.add(ft)
                    }
                }
                if (toRemove.isNotEmpty()) {
                    floatingTexts.removeAll(toRemove)
                }
            }
        }
    }

    // Countdown Effect with light tick haptics
    LaunchedEffect(countdownState) {
        if (countdownState > 0) {
            soundManager.playCountdown(countdownState)
            hapticManager.vibrateLight()
            delay(1000)
            countdownState -= 1
        } else if (countdownState == 0) {
            soundManager.playCountdown(0)
            soundManager.playGameStart()
            soundManager.startBgm()
            hapticManager.vibrateMedium()
            delay(800)
            countdownState = -1
        }
    }

    // 1-second background countdown timer for active power-ups and spawn timers
    LaunchedEffect(isPaused, isGameOver, countdownState, currentScreenState, isLevelCompleted, showLevelUpOverlay) {
        if (isPaused || isGameOver || countdownState != -1 || currentScreenState != "GAMEPLAY" || showLevelUpOverlay) return@LaunchedEffect
        while (!isLevelCompleted && !showLevelUpOverlay) {
            delay(1000)
            
            // Adventure Mode Tracking
            if (isAdventureMode) {
                levelTimeElapsedSeconds += 1
                if (activeLevel?.isBonus == true) {
                    bonusTimeLeftSeconds = (bonusTimeLeftSeconds - 1).coerceAtLeast(0)
                    if (bonusTimeLeftSeconds <= 0) {
                        // Bonus level rewards: 2 coins per fruit collected!
                        val stars = 3
                        val coinsReward = levelFruitsCollected * 2
                        SnakeProgressionManager.saveLevelStars(context, currentLevelNumber, stars)
                        SnakeProgressionManager.saveLevelHighScore(context, currentLevelNumber, score)
                        
                        if (currentLevelNumber < 100) {
                            val nextLvl = currentLevelNumber + 1
                            SnakeProgressionManager.saveUnlockedLevel(context, nextLvl)
                            unlockedLevel = SnakeProgressionManager.loadUnlockedLevel(context)
                        }
                        viewModel.addCoins(coinsReward, "Bonus Level $currentLevelNumber Complete")
                        stats = SnakeProgressionManager.loadStats(context)
                        soundManager.playNewBestScore()
                        hapticManager.vibrateSuccess()
                        
                        // Set level completed state at the very end to safely trigger any keys/re-evaluations
                        isLevelCompleted = true
                        break
                    }
                }
            }
            
            // Decrement active power-up timers
            if (shieldTimeLeft > 0) {
                shieldTimeLeft -= 1
                if (shieldTimeLeft == 0) {
                    hasShield = false
                }
            }
            if (magnetTimeLeft > 0) {
                magnetTimeLeft -= 1
            }
            if (slowMotionTimeLeft > 0) {
                slowMotionTimeLeft -= 1
            }
            if (doubleCoinsTimeLeft > 0) {
                doubleCoinsTimeLeft -= 1
            }
            if (fruitFrenzyTimeLeft > 0) {
                fruitFrenzyTimeLeft -= 1
                if (fruitFrenzyTimeLeft == 0) {
                    soundManager.isFruitFrenzyPlaying = false
                    extraFruits.clear()
                }
            }
            
            // Spawn random power-up / chest on average every 30-45 seconds
            timeSinceLastSpawnSeconds += 1
            if (timeSinceLastSpawnSeconds >= nextSpawnIntervalSeconds) {
                timeSinceLastSpawnSeconds = 0
                nextSpawnIntervalSeconds = Random.nextInt(30, 46)
                spawnRandomPowerUpOrChest()
            }
        }
    }

    // Intercept and animate snake moves continuously inside the Game Loop
    LaunchedEffect(isPaused, isGameOver, countdownState, currentScreenState, isLevelCompleted, showLevelUpOverlay) {
        if (isPaused || isGameOver || countdownState != -1 || isDeadAnimating || currentScreenState != "GAMEPLAY" || isLevelCompleted || showLevelUpOverlay) return@LaunchedEffect

        while (!isLevelCompleted && !showLevelUpOverlay) {
            previousDirection = direction
            // Dequeue next direction if available
            if (directionQueue.isNotEmpty()) {
                direction = directionQueue.removeAt(0)
            }
            
            val head = snake.first()

            // Magnet Attraction Logic: Attract nearby fruits closer by 1 cell on each tick
            if (magnetTimeLeft > 0) {
                // Attract standard food
                val fDx = head.x - food.x
                val fDy = head.y - food.y
                if (maxOf(abs(fDx), abs(fDy)) <= 4) {
                    val stepX = if (fDx > 0) 1 else if (fDx < 0) -1 else 0
                    val stepY = if (fDy > 0) 1 else if (fDy < 0) -1 else 0
                    val targetFood = SnakePoint(food.x + stepX, food.y + stepY)
                    if (!snake.drop(1).contains(targetFood)) {
                        food = targetFood
                    }
                }
                
                // Attract bonus food
                bonusFood?.let { bf ->
                    val bDx = head.x - bf.x
                    val bDy = head.y - bf.y
                    if (maxOf(abs(bDx), abs(bDy)) <= 4) {
                        val stepX = if (bDx > 0) 1 else if (bDx < 0) -1 else 0
                        val stepY = if (bDy > 0) 1 else if (bDy < 0) -1 else 0
                        val targetBf = SnakePoint(bf.x + stepX, bf.y + stepY)
                        if (!snake.drop(1).contains(targetBf)) {
                            bonusFood = targetBf
                        }
                    }
                }
                
                // Attract extra fruits
                for (i in extraFruits.indices) {
                    val ef = extraFruits[i]
                    val eDx = head.x - ef.x
                    val eDy = head.y - ef.y
                    if (maxOf(abs(eDx), abs(eDy)) <= 4) {
                        val stepX = if (eDx > 0) 1 else if (eDx < 0) -1 else 0
                        val stepY = if (eDy > 0) 1 else if (eDy < 0) -1 else 0
                        val targetEf = SnakePoint(ef.x + stepX, ef.y + stepY)
                        if (!snake.drop(1).contains(targetEf)) {
                            extraFruits[i] = targetEf
                        }
                    }
                }
            }

            // Slide on Ice mechanic
            var directionToUse = direction
            if (isAdventureMode && activeLevel?.specialMechanic == "ICE") {
                if (direction != previousDirection && !isSlidingIce) {
                    directionToUse = previousDirection
                    isSlidingIce = true
                    floatingTexts.add(FloatingText("SLIDE! ❄️", head.x + 0.5f, head.y + 0.3f, 1f, 500))
                } else {
                    isSlidingIce = false
                }
            }

            var actualNextHead = when (directionToUse) {
                SnakeDirection.UP -> SnakePoint(head.x, head.y - 1)
                SnakeDirection.DOWN -> SnakePoint(head.x, head.y + 1)
                SnakeDirection.LEFT -> SnakePoint(head.x - 1, head.y)
                SnakeDirection.RIGHT -> SnakePoint(head.x + 1, head.y)
            }

            // Teleport Gates / Portals Teleportation logic
            if (isAdventureMode) {
                activeLevel?.portals?.forEach { (p1, p2) ->
                    if (actualNextHead == p1) {
                        actualNextHead = p2
                        soundManager.playTeleport()
                        repeat(12) {
                            val angle = Random.nextFloat() * 2f * PI.toFloat()
                            val speed = Random.nextFloat() * 0.12f + 0.06f
                            particles.add(
                                SnakeParticle(
                                    x = p1.x + 0.5f, y = p1.y + 0.5f,
                                    vx = cos(angle) * speed, vy = sin(angle) * speed,
                                    color = Color(0xFF00E5FF), alpha = 1f,
                                    size = Random.nextFloat() * 0.1f + 0.05f, maxLife = Random.nextInt(400) + 200
                                )
                            )
                            particles.add(
                                SnakeParticle(
                                    x = p2.x + 0.5f, y = p2.y + 0.5f,
                                    vx = cos(angle) * speed, vy = sin(angle) * speed,
                                    color = Color(0xFF00E5FF), alpha = 1f,
                                    size = Random.nextFloat() * 0.1f + 0.05f, maxLife = Random.nextInt(400) + 200
                                )
                            )
                        }
                        floatingTexts.add(FloatingText("WARP! 🌀", p2.x + 0.5f, p2.y + 0.3f, 1f, 800))
                    } else if (actualNextHead == p2) {
                        actualNextHead = p1
                        soundManager.playTeleport()
                        repeat(12) {
                            val angle = Random.nextFloat() * 2f * PI.toFloat()
                            val speed = Random.nextFloat() * 0.12f + 0.06f
                            particles.add(
                                SnakeParticle(
                                    x = p1.x + 0.5f, y = p1.y + 0.5f,
                                    vx = cos(angle) * speed, vy = sin(angle) * speed,
                                    color = Color(0xFF00E5FF), alpha = 1f,
                                    size = Random.nextFloat() * 0.1f + 0.05f, maxLife = Random.nextInt(400) + 200
                                )
                            )
                            particles.add(
                                SnakeParticle(
                                    x = p2.x + 0.5f, y = p2.y + 0.5f,
                                    vx = cos(angle) * speed, vy = sin(angle) * speed,
                                    color = Color(0xFF00E5FF), alpha = 1f,
                                    size = Random.nextFloat() * 0.1f + 0.05f, maxLife = Random.nextInt(400) + 200
                                )
                            )
                        }
                        floatingTexts.add(FloatingText("WARP! 🌀", p1.x + 0.5f, p1.y + 0.3f, 1f, 800))
                    }
                }
            }

            // Wind drift push mechanic
            if (isAdventureMode && activeLevel?.specialMechanic == "WIND") {
                windMoveCounter++
                if (windMoveCounter >= 5) {
                    windMoveCounter = 0
                    val windOffset = if (currentLevelNumber % 2 == 0) 1 else -1
                    val driftedHead = SnakePoint(actualNextHead.x + windOffset, actualNextHead.y)
                    val hitWallOrObstacle = driftedHead.x < 0 || driftedHead.x >= gridWidth || driftedHead.y < 0 || driftedHead.y >= gridHeight ||
                            (activeLevel?.obstacles?.contains(driftedHead) == true) ||
                            currentBreakableWalls.contains(driftedHead)
                    if (!hitWallOrObstacle) {
                        actualNextHead = driftedHead
                        floatingTexts.add(FloatingText(if (windOffset > 0) "WIND PUSH ➡️" else "WIND PUSH ⬅️", head.x + 0.5f, head.y + 0.3f, 1f, 600))
                    }
                }
            }

            // Check collision with walls, self, static obstacles, breakable walls, lava, traps, or boss
            var actualHitWall = actualNextHead.x < 0 || actualNextHead.x >= gridWidth || actualNextHead.y < 0 || actualNextHead.y >= gridHeight
            var actualHitObstacle = isAdventureMode && activeLevel != null && (
                activeLevel!!.obstacles.contains(actualNextHead) ||
                currentBreakableWalls.contains(actualNextHead) ||
                activeLevel!!.lavaTiles.contains(actualNextHead) ||
                activeTraps.contains(actualNextHead) ||
                (activeLevel!!.isBoss && bossPosition == actualNextHead)
            )
            
            // Self-collision: Ignore head (index 0) and check all other segments
            var actualHitSelf = false
            if (snake.size > 1) {
                for (i in 1 until snake.size) {
                    if (snake[i] == actualNextHead) {
                        actualHitSelf = true
                        break
                    }
                }
            }

            // Wrap and protect on Bonus Level (no death, wraps around walls)
            if (isAdventureMode && activeLevel?.isBonus == true) {
                actualHitWall = false
                actualHitSelf = false
                actualHitObstacle = false
                actualNextHead = SnakePoint(
                    (actualNextHead.x + gridWidth) % gridWidth,
                    (actualNextHead.y + gridHeight) % gridHeight
                )
            }

            // Temporary Ad Shield protection (3 seconds after Rewarded Ad continue)
            if (isAdShieldActive) {
                if (actualHitWall) {
                    actualNextHead = SnakePoint(
                        (actualNextHead.x + gridWidth) % gridWidth,
                        (actualNextHead.y + gridHeight) % gridHeight
                    )
                }
                actualHitWall = false
                actualHitSelf = false
                actualHitObstacle = false
            }

            if (actualHitWall || actualHitSelf || actualHitObstacle) {
                if (hasShield || shieldTimeLeft > 0) {
                    hasShield = false
                    shieldTimeLeft = 0
                    soundManager.playLevelUp() // Play beautiful shield break sound
                    hapticManager.vibrateSuccess()
                    
                    // Spawn beautiful explosive shield breaking particle effects!
                    repeat(15) {
                        val angle = Random.nextFloat() * 2f * PI.toFloat()
                        val speed = Random.nextFloat() * 0.15f + 0.05f
                        particles.add(
                            SnakeParticle(
                                x = head.x + 0.5f, y = head.y + 0.5f,
                                vx = cos(angle) * speed, vy = sin(angle) * speed,
                                color = Color(0xFF00E5FF), alpha = 1f,
                                size = Random.nextFloat() * 0.1f + 0.05f, maxLife = Random.nextInt(500) + 300
                            )
                        )
                    }

                    floatingTexts.add(
                        FloatingText(
                            text = "SHIELD BROKEN! 🛡️💥",
                            x = head.x + 0.5f,
                            y = head.y + 0.3f,
                            alpha = 1f,
                            maxLife = 1500
                        )
                    )
                    
                    if (actualHitWall) {
                        // Wrap around walls safely so the snake survives and continues
                        actualNextHead = SnakePoint(
                            (actualNextHead.x + gridWidth) % gridWidth,
                            (actualNextHead.y + gridHeight) % gridHeight
                        )
                    }
                    actualHitWall = false
                    actualHitSelf = false
                    actualHitObstacle = false
                }
            }

            if (actualHitWall || actualHitSelf || actualHitObstacle) {
                // Save snapshot containing exact game state immediately before death animation
                lastDeathSnapshot = SnakeGameStateSnapshot(
                    snake = snake.toList(),
                    previousSnake = previousSnake.toList(),
                    direction = direction,
                    previousDirection = previousDirection,
                    food = food,
                    score = score,
                    levelFruitsCollected = levelFruitsCollected,
                    fruitsCollectedThisGame = fruitsCollectedThisGame,
                    currentLevelNumber = currentLevelNumber,
                    isAdventureMode = isAdventureMode,
                    activeLevel = activeLevel,
                    breakableWalls = currentBreakableWalls.toList(),
                    crystalPoint = crystalPoint,
                    bossPosition = bossPosition,
                    bossDirection = bossDirection,
                    bossTickCounter = bossTickCounter,
                    activeTraps = activeTraps.toList(),
                    bonusTimeLeftSeconds = bonusTimeLeftSeconds,
                    levelTimeElapsedSeconds = levelTimeElapsedSeconds,
                    hasShield = hasShield,
                    shieldTimeLeft = shieldTimeLeft,
                    isDoubleCoinsActive = isDoubleCoinsActive,
                    doubleCoinsTimeLeft = doubleCoinsTimeLeft,
                    magnetTimeLeft = magnetTimeLeft,
                    slowMotionTimeLeft = slowMotionTimeLeft,
                    fruitFrenzyTimeLeft = fruitFrenzyTimeLeft,
                    extraFruits = extraFruits.toList(),
                    activePowerUpOnBoard = activePowerUpOnBoard
                )

                // PREMIUM DEATH SEQUENCE
                isDeadAnimating = true
                isDeadEyeClosed = true
                soundManager.stopBgm()
                hapticManager.vibrateGameOver()

                // Shake snake head/body sequentially
                repeat(8) { i ->
                    val intensity = 12f / (i + 1)
                    snakeDeathShake.animateTo(if (i % 2 == 0) intensity else -intensity, tween(50))
                }
                snakeDeathShake.animateTo(0f, tween(50))

                // Smooth fade out of body segments sequentially
                snakeDeathAlpha.animateTo(0f, tween(800))

                isDeadAnimating = false
                
                // Update high score, highest level, and highest speed locally if higher (Endless mode only)
                if (!isAdventureMode) {
                    val finalLvl = (fruitsCollectedThisGame / 5) + 1
                    val finalSpeedMult = 1.0f + (finalLvl - 1) * 0.08f
                    
                    var newBest = false
                    if (score > highScore) {
                        highScore = score
                        prefs.edit().putInt("high_score", score).apply()
                        newBest = true
                    }
                    if (finalLvl > highestLevel) {
                        highestLevel = finalLvl
                        prefs.edit().putInt("highest_level", finalLvl).apply()
                        newBest = true
                    }
                    if (finalSpeedMult > highestSpeed) {
                        highestSpeed = finalSpeedMult
                        prefs.edit().putFloat("highest_speed", finalSpeedMult).apply()
                        newBest = true
                    }
                    
                    if (newBest) {
                        soundManager.playNewBestScore()
                    } else {
                        soundManager.playGameOver()
                    }
                } else {
                    soundManager.playGameOver()
                }

                // Calculate final survival time
                val rawDuration = if (gameStartTimeMs > 0) (System.currentTimeMillis() - gameStartTimeMs) else 0L
                val pausedTime = if (lastPauseTimestamp > 0) (System.currentTimeMillis() - lastPauseTimestamp + pausedDurationMs) else pausedDurationMs
                val survivalSeconds = ((rawDuration - pausedTime) / 1000).coerceAtLeast(0).toInt()

                // Update Statistics
                val oldStats = SnakeProgressionManager.loadStats(context)
                val newStats = oldStats.copy(
                    gamesPlayed = oldStats.gamesPlayed + 1,
                    totalScore = oldStats.totalScore + score,
                    longestSurvivalTime = maxOf(oldStats.longestSurvivalTime, survivalSeconds)
                )
                SnakeProgressionManager.saveStats(context, newStats)
                stats = newStats

                // Update game-end missions (Play 3 Games, Survive 2 Min, No Pause)
                val endMissions = dailyMissions.map { m ->
                    if (m.type == "PLAY_3_GAMES") {
                        val newProgress = (m.progress + 1).coerceAtMost(m.target)
                        if (newProgress == m.target && m.progress < m.target) {
                            activeCompletionNotification = "🎉 Daily Mission Completed: ${m.title}!"
                            hapticManager.vibrateSuccess()
                        }
                        m.copy(progress = newProgress)
                    } else if (m.type == "SURVIVE_2_MIN") {
                        val newProgress = maxOf(m.progress, survivalSeconds).coerceAtMost(m.target)
                        if (newProgress == m.target && m.progress < m.target) {
                            activeCompletionNotification = "🎉 Daily Mission Completed: ${m.title}!"
                            hapticManager.vibrateSuccess()
                        }
                        m.copy(progress = newProgress)
                    } else if (m.type == "NO_PAUSE") {
                        if (!wasPausedThisGame && score > 0) {
                            val newProgress = 1
                            if (m.progress == 0) {
                                activeCompletionNotification = "🎉 Daily Mission Completed: ${m.title}!"
                                hapticManager.vibrateSuccess()
                            }
                            m.copy(progress = newProgress)
                        } else {
                            m
                        }
                    } else {
                        m
                    }
                }
                dailyMissions = endMissions
                SnakeProgressionManager.saveMissions(context, endMissions)

                // Update game-end achievements (First Snake, Survivor)
                val endAchievements = achievements.map { a ->
                    if (a.id == "a1") { // First Snake
                        val newProgress = newStats.gamesPlayed
                        val nowUnlocked = newProgress >= a.target
                        if (nowUnlocked && !a.unlocked) {
                            activeCompletionNotification = "🏆 Achievement Unlocked: ${a.name}!"
                            hapticManager.vibrateSuccess()
                        }
                        a.copy(progress = newProgress, unlocked = nowUnlocked)
                    } else if (a.id == "a6") { // Survivor
                        val newProgress = newStats.longestSurvivalTime
                        val nowUnlocked = newProgress >= a.target
                        if (nowUnlocked && !a.unlocked) {
                            activeCompletionNotification = "🏆 Achievement Unlocked: ${a.name}!"
                            hapticManager.vibrateSuccess()
                        }
                        a.copy(progress = newProgress, unlocked = nowUnlocked)
                    } else {
                        a
                    }
                }
                achievements = endAchievements
                SnakeProgressionManager.saveAchievements(context, endAchievements)

                // Auto-save history on Game Over with 2X Coins scaling if active (Endless only)
                if (!isAdventureMode) {
                    val baseCoins = (score / 10 * 5).coerceAtMost(100)
                    val coinsEarned = if (isDoubleCoinsActive || doubleCoinsTimeLeft > 0) baseCoins * 2 else baseCoins
                    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                    val currentDateStr = sdf.format(Date())
                    val newEntry = SnakeGameHistoryEntry(currentDateStr, score, coinsEarned)
                    
                    val updatedList = listOf(newEntry) + historyList.take(19)
                    historyList = updatedList
                    prefs.edit().putString("game_history", serializeHistory(updatedList)).apply()
                }

                // Set isGameOver = true at the very end to safely trigger dialog popping up after all animation and data saving finishes
                isGameOver = true
                break
            } else {
                val newSnake = mutableListOf(actualNextHead)
                val ateBonus = (bonusFood != null && actualNextHead == bonusFood)
                val ateExtraIndex = extraFruits.indexOf(actualNextHead)
                val ateExtra = ateExtraIndex != -1

                // Check Crystal Pickup for breaking walls
                crystalPoint?.let { cp ->
                    if (actualNextHead == cp) {
                        crystalPoint = null
                        soundManager.playWallBreak()
                        hapticManager.vibrateSuccess()
                        floatingTexts.add(FloatingText("WALLS BROKEN! 🧱💥", cp.x + 0.5f, cp.y + 0.3f, 1f, 1500))
                        
                        // Explosive wall particles
                        currentBreakableWalls.forEach { wallPt ->
                            repeat(6) {
                                val angle = Random.nextFloat() * 2f * PI.toFloat()
                                val speed = Random.nextFloat() * 0.12f + 0.05f
                                particles.add(
                                    SnakeParticle(
                                        x = wallPt.x + 0.5f, y = wallPt.y + 0.5f,
                                        vx = cos(angle) * speed, vy = sin(angle) * speed,
                                        color = Color(0xFF8D6E63), alpha = 1f,
                                        size = Random.nextFloat() * 0.12f + 0.06f, maxLife = Random.nextInt(300) + 200
                                    )
                                )
                            }
                        }
                        currentBreakableWalls.clear()
                    }
                }

                // Boss movement AI updates
                if (isAdventureMode && activeLevel?.isBoss == true) {
                    bossTickCounter++
                    if (bossTickCounter >= 2) {
                        bossTickCounter = 0
                        val bX = bossPosition.x
                        val bY = bossPosition.y
                        val sX = head.x
                        val sY = head.y
                        val dx = sX - bX
                        val dy = sY - bY
                        
                        var nextBossPt = bossPosition
                        if (abs(dx) > abs(dy)) {
                            val stepX = if (dx > 0) 1 else -1
                            nextBossPt = SnakePoint(bX + stepX, bY)
                        } else if (abs(dy) > 0) {
                            val stepY = if (dy > 0) 1 else -1
                            nextBossPt = SnakePoint(bX, bY + stepY)
                        }
                        
                        val isSafeForBoss = nextBossPt.x in 0 until gridWidth && 
                                           nextBossPt.y in 0 until gridHeight &&
                                           !(activeLevel?.obstacles?.contains(nextBossPt) ?: false) &&
                                           !currentBreakableWalls.contains(nextBossPt)
                                           
                        if (isSafeForBoss) {
                            bossDirection = when {
                                nextBossPt.x > bX -> SnakeDirection.RIGHT
                                nextBossPt.x < bX -> SnakeDirection.LEFT
                                nextBossPt.y > bY -> SnakeDirection.DOWN
                                else -> SnakeDirection.UP
                            }
                            bossPosition = nextBossPt
                            
                            if (Random.nextFloat() < 0.35f) {
                                activeTraps.add(SnakePoint(bX, bY))
                                if (activeTraps.size > 5) {
                                    activeTraps.removeAt(0)
                                }
                            }
                        }
                    }
                }

                // Check active powerup collision
                activePowerUpOnBoard?.let { powerUp ->
                    if (actualNextHead == powerUp.point) {
                        collectPowerUp(powerUp)
                        activePowerUpOnBoard = null
                    }
                }

                if (actualNextHead == food || ateBonus || ateExtra) {
                    // Ate food!
                    val oldScore = score
                    val isFrenzy = fruitFrenzyTimeLeft > 0
                    val pointsAdded = if (ateBonus) {
                        30
                    } else if (isFrenzy) {
                        20
                    } else {
                        10
                    }
                    score += pointsAdded
                    
                    if (ateBonus) {
                        bonusFood = null
                        val baseCoins = 5
                        val coinsBonus = if (doubleCoinsTimeLeft > 0 || isDoubleCoinsActive) baseCoins * 2 else baseCoins
                        viewModel.addCoins(coinsBonus, "Snake Bonus Fruit")
                        val oldStats = SnakeProgressionManager.loadStats(context)
                        val newStats = oldStats.copy(totalCoinsEarned = oldStats.totalCoinsEarned + coinsBonus)
                        SnakeProgressionManager.saveStats(context, newStats)
                        stats = newStats
                        
                        floatingTexts.add(
                            FloatingText(
                                text = if (doubleCoinsTimeLeft > 0 || isDoubleCoinsActive) "BONUS +30! (2X 🪙)" else "BONUS +30!",
                                x = actualNextHead.x + 0.5f,
                                y = actualNextHead.y + 0.3f,
                                alpha = 1f,
                                maxLife = 1000
                            )
                        )
                    } else if (ateExtra) {
                        extraFruits.removeAt(ateExtraIndex)
                        if (isFrenzy) {
                            findRandomEmptyPoint()?.let { extraFruits.add(it) }
                        }
                        
                        floatingTexts.add(
                            FloatingText(
                                text = if (doubleCoinsTimeLeft > 0 || isDoubleCoinsActive) "FRENZY +20! (2X 🪙)" else "FRENZY +20! ⚡",
                                x = actualNextHead.x + 0.5f,
                                y = actualNextHead.y + 0.3f,
                                alpha = 1f,
                                maxLife = 1000
                            )
                        )
                    }

                    val oldLevel = (oldScore / 50) + 1
                    val newLevel = (score / 50) + 1
                    if (newLevel > oldLevel) {
                        soundManager.playLevelUp()
                        hapticManager.vibrateSuccess()
                    } else {
                        soundManager.playFoodCollected()
                        soundManager.playGrowth()
                        hapticManager.vibrateMedium()
                    }

                    // Spark Particle Burst in grid coordinates centered on food
                    repeat(18) {
                        val angle = Random.nextFloat() * 2f * PI.toFloat()
                        val speed = Random.nextFloat() * 0.15f + 0.08f // Grid units per 16ms
                        val pColor = when {
                            ateBonus -> Color(0xFFFFF59D)
                            ateExtra -> Color(0xFFE040FB)
                            else -> {
                                when (Random.nextInt(3)) {
                                    0 -> Color(0xFFFFB74D) // Peach light
                                    1 -> Color(0xFFF4511E) // Peach dark
                                    else -> Color(0xFF00E5FF) // Neon Cyan
                                }
                            }
                        }
                        particles.add(
                            SnakeParticle(
                                x = actualNextHead.x + 0.5f,
                                y = actualNextHead.y + 0.5f,
                                vx = cos(angle) * speed,
                                vy = sin(angle) * speed,
                                color = pColor,
                                alpha = 1f,
                                size = Random.nextFloat() * 0.15f + 0.08f, // size in grid units
                                maxLife = Random.nextInt(400) + 300
                            )
                        )
                    }

                    // Floating text
                    floatingTexts.add(
                        FloatingText(
                            text = if (ateBonus) "+30" else if (ateExtra) "+20" else "+10",
                            x = actualNextHead.x + 0.5f,
                            y = actualNextHead.y + 0.3f,
                            alpha = 1f,
                            maxLife = 850
                        )
                    )

                    // Glow Flash animation on food collection
                    coroutineScope.launch {
                        boardFlashAlpha.snapTo(0.45f)
                        boardFlashAlpha.animateTo(0f, tween(320))
                    }

                    // Screen Shake
                    coroutineScope.launch {
                        repeat(5) { i ->
                            val intensity = 8f / (i + 1)
                            shakeOffsetX.animateTo(if (i % 2 == 0) intensity else -intensity, tween(40))
                            shakeOffsetY.animateTo(if (i % 2 == 1) intensity else -intensity, tween(40))
                        }
                        shakeOffsetX.animateTo(0f, tween(30))
                        shakeOffsetY.animateTo(0f, tween(30))
                    }

                    newSnake.addAll(snake)
                    
                    if (!ateBonus) {
                        fruitsCollectedThisGame += 1
                        
                        if (!isAdventureMode) {
                            val oldClassicLvl = (fruitsCollectedThisGame - 1) / 5 + 1
                            val newClassicLvl = fruitsCollectedThisGame / 5 + 1
                            if (newClassicLvl > oldClassicLvl) {
                                showLevelUpOverlay = true
                                soundManager.playLevelUp()
                                hapticManager.vibrateSuccess()
                                
                                val cX = gridWidth / 2f
                                val cY = gridHeight / 2f
                                val colors = listOf(Color(0xFF00E5FF), Color(0xFF7C4DFF), Color(0xFFFF007F), Color(0xFFFFD700))
                                repeat(50) {
                                    val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
                                    val spd = Random.nextFloat() * 0.15f + 0.05f
                                    particles.add(
                                        SnakeParticle(
                                            x = cX,
                                            y = cY,
                                            vx = (Math.cos(angle.toDouble()).toFloat() * spd),
                                            vy = (Math.sin(angle.toDouble()).toFloat() * spd),
                                            color = colors.random(),
                                            alpha = 1f,
                                            size = Random.nextFloat() * 0.2f + 0.1f,
                                            maxLife = Random.nextInt(800, 1500)
                                        )
                                    )
                                }
                                
                                floatingTexts.add(
                                    FloatingText(
                                        text = "LEVEL UP! 🌟",
                                        x = cX,
                                        y = cY - 2f,
                                        alpha = 1f,
                                        maxLife = 2000
                                    )
                                )
                                
                                // Auto-save highest level and speed immediately
                                if (newClassicLvl > highestLevel) {
                                    highestLevel = newClassicLvl
                                    prefs.edit().putInt("highest_level", newClassicLvl).apply()
                                }
                                val mult = 1.0f + (newClassicLvl - 1) * 0.08f
                                if (mult > highestSpeed) {
                                    highestSpeed = mult
                                    prefs.edit().putFloat("highest_speed", mult).apply()
                                }
                            }
                        }
                        
                        if (isAdventureMode) {
                            levelFruitsCollected += 1
                            val target = activeLevel?.targetFruits ?: 999
                            if (levelFruitsCollected >= target && activeLevel?.isBonus == false) {
                                soundManager.playNewBestScore()
                                hapticManager.vibrateSuccess()
                                
                                val stars = when {
                                    levelTimeElapsedSeconds <= 35 -> 3
                                    levelTimeElapsedSeconds <= 60 -> 2
                                    else -> 1
                                }
                                val coinsReward = 10 + (stars * 1)
                                SnakeProgressionManager.saveLevelStars(context, currentLevelNumber, stars)
                                SnakeProgressionManager.saveLevelHighScore(context, currentLevelNumber, score)
                                SnakeProgressionManager.saveLevelBestTime(context, currentLevelNumber, levelTimeElapsedSeconds)
                                
                                if (currentLevelNumber < 100) {
                                    val nextLvl = currentLevelNumber + 1
                                    SnakeProgressionManager.saveUnlockedLevel(context, nextLvl)
                                    unlockedLevel = SnakeProgressionManager.loadUnlockedLevel(context)
                                }
                                viewModel.addCoins(coinsReward, "Level $currentLevelNumber Complete")
                                stats = SnakeProgressionManager.loadStats(context)
                                
                                // Set level completed state at the very end to safely trigger completed dialog without interrupting preceding logic
                                isLevelCompleted = true
                            }
                        }
                        
                        // Trigger Mystery Box every 10 fruits (Endless only)
                        if (!isAdventureMode && fruitsCollectedThisGame > 0 && fruitsCollectedThisGame % 10 == 0) {
                            isPaused = true
                            showMysteryBoxOverlay = true
                            isMysteryBoxOpened = false
                            mysteryBoxRewardText = ""
                            soundManager.playLevelUp()
                        }
                        
                        // Update stats
                        val oldStats = SnakeProgressionManager.loadStats(context)
                        val newStats = oldStats.copy(totalFruits = oldStats.totalFruits + 1)
                        SnakeProgressionManager.saveStats(context, newStats)
                        stats = newStats
                        
                        // Update real-time missions & achievements
                        val missionsList = dailyMissions.map { m ->
                            if (m.type == "EAT_FRUITS" || m.type == "COLLECT_50_FRUITS") {
                                val newProgress = (m.progress + 1).coerceAtMost(m.target)
                                if (newProgress == m.target && m.progress < m.target) {
                                    activeCompletionNotification = "🎉 Daily Mission Completed: ${m.title}!"
                                    hapticManager.vibrateSuccess()
                                }
                                m.copy(progress = newProgress)
                            } else if (m.type == "REACH_SCORE_100" || m.type == "REACH_SCORE_200") {
                                val newProgress = maxOf(m.progress, score).coerceAtMost(m.target)
                                if (newProgress == m.target && m.progress < m.target) {
                                    activeCompletionNotification = "🎉 Daily Mission Completed: ${m.title}!"
                                    hapticManager.vibrateSuccess()
                                }
                                m.copy(progress = newProgress)
                            } else if (m.type == "COMPLETE_5_LEVELS") {
                                val speedLvl = (score / 50) + 1
                                val newProgress = maxOf(m.progress, speedLvl).coerceAtMost(m.target)
                                if (newProgress == m.target && m.progress < m.target) {
                                    activeCompletionNotification = "🎉 Daily Mission Completed: ${m.title}!"
                                    hapticManager.vibrateSuccess()
                                }
                                m.copy(progress = newProgress)
                            } else {
                                m
                            }
                        }
                        dailyMissions = missionsList
                        SnakeProgressionManager.saveMissions(context, missionsList)
                        
                        val achievementsList = achievements.map { a ->
                            if (a.id == "a2") { // Fruit Collector
                                val newProgress = (a.progress + 1).coerceAtMost(a.target)
                                val wasUnlocked = a.unlocked
                                val nowUnlocked = newProgress >= a.target
                                if (nowUnlocked && !wasUnlocked) {
                                    activeCompletionNotification = "🏆 Achievement Unlocked: ${a.name}!"
                                    hapticManager.vibrateSuccess()
                                }
                                a.copy(progress = newProgress, unlocked = nowUnlocked)
                            } else if (a.id == "a3") { // Speed Master
                                val speedLvl = (score / 50) + 1
                                val newProgress = maxOf(a.progress, speedLvl).coerceAtMost(a.target)
                                val wasUnlocked = a.unlocked
                                val nowUnlocked = newProgress >= a.target
                                if (nowUnlocked && !wasUnlocked) {
                                    activeCompletionNotification = "🏆 Achievement Unlocked: ${a.name}!"
                                    hapticManager.vibrateSuccess()
                                }
                                a.copy(progress = newProgress, unlocked = nowUnlocked)
                            } else if (a.id == "a4" || a.id == "a5") { // Score Hunter, Snake King
                                val newProgress = maxOf(a.progress, score).coerceAtMost(a.target)
                                val wasUnlocked = a.unlocked
                                val nowUnlocked = newProgress >= a.target
                                if (nowUnlocked && !wasUnlocked) {
                                    activeCompletionNotification = "🏆 Achievement Unlocked: ${a.name}!"
                                    hapticManager.vibrateSuccess()
                                }
                                a.copy(progress = newProgress, unlocked = nowUnlocked)
                            } else {
                                a
                            }
                        }
                        achievements = achievementsList
                        SnakeProgressionManager.saveAchievements(context, achievementsList)
                    }
                    if (actualNextHead == food) {
                        spawnFood()
                    }
                } else {
                    // Regular movement
                    newSnake.addAll(snake.dropLast(1))
                }
                
                previousSnake = snake
                snake = newSnake
                
                // Animate progress smoothly over the duration of currentDelay
                moveProgress.snapTo(0f)
                moveProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = currentDelay.toInt(),
                        easing = LinearEasing
                    )
                )
            }
        }
    }

    // Main layout
    Scaffold(
        containerColor = Color(0xFF090615),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D0A1B))
                    .statusBarsPadding()
                    .displayCutoutPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        if (currentScreenState == "GAMEPLAY" || isGameOver) {
                            exitGameToHome()
                        } else {
                            soundManager.playButtonClick()
                            onBack()
                        }
                    },
                    modifier = Modifier.testTag("back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Go Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "SNAKE CLASSIC",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 1.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Sound Toggle Button
                    IconButton(
                        onClick = {
                            val nextState = !soundManager.isSoundEnabled
                            soundManager.toggleSoundEnabled(nextState)
                            if (nextState) {
                                soundManager.playButtonClick()
                                if (!isPaused && !isGameOver && countdownState == -1) {
                                    soundManager.startBgm()
                                }
                            }
                        },
                        modifier = Modifier.testTag("sound_toggle_button")
                    ) {
                        AnimatedContent(
                            targetState = soundManager.isSoundEnabled,
                            transitionSpec = {
                                scaleIn(animationSpec = tween(200)) + fadeIn(animationSpec = tween(150)) togetherWith
                                scaleOut(animationSpec = tween(200)) + fadeOut(animationSpec = tween(150))
                            },
                            label = "sound_icon"
                        ) { isEnabled ->
                            Text(
                                text = if (isEnabled) "🔊" else "🔇",
                                fontSize = 20.sp
                            )
                        }
                    }

                    // Pause Button
                    if (currentScreenState == "GAMEPLAY") {
                        IconButton(
                            onClick = {
                                soundManager.playButtonClick()
                                if (!isGameOver && countdownState == -1) {
                                    if (isPaused) {
                                        isPaused = false
                                        soundManager.playResume()
                                        soundManager.resumeBgm()
                                        if (lastPauseTimestamp > 0L) {
                                            pausedDurationMs += System.currentTimeMillis() - lastPauseTimestamp
                                            lastPauseTimestamp = 0L
                                        }
                                    } else {
                                        isPaused = true
                                        wasPausedThisGame = true
                                        soundManager.playPause()
                                        soundManager.pauseBgm()
                                        lastPauseTimestamp = System.currentTimeMillis()
                                    }
                                } else {
                                    isPaused = !isPaused
                                }
                            },
                            modifier = Modifier.testTag("pause_button")
                        ) {
                            Icon(
                                imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = "Pause",
                                tint = Color(0xFF00E5FF)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 10.dp)
                // Ultra-low latency, responsive gesture swipe detector
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val startPos = down.position
                            var directionTriggered = false
                            
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id }
                                if (change == null || !change.pressed) {
                                    break
                                }
                                
                                if (!directionTriggered) {
                                    val currentPos = change.position
                                    val diffX = currentPos.x - startPos.x
                                    val diffY = currentPos.y - startPos.y
                                    
                                    val swipeThreshold = 30f // Pixels (extremely responsive & fast)
                                    val absX = kotlin.math.abs(diffX)
                                    val absY = kotlin.math.abs(diffY)
                                    
                                    if (absX > swipeThreshold || absY > swipeThreshold) {
                                        if (absX > absY) {
                                            if (diffX > 0) {
                                                tryQueueDirection(SnakeDirection.RIGHT)
                                            } else {
                                                tryQueueDirection(SnakeDirection.LEFT)
                                            }
                                        } else {
                                            if (diffY > 0) {
                                                tryQueueDirection(SnakeDirection.DOWN)
                                            } else {
                                                tryQueueDirection(SnakeDirection.UP)
                                            }
                                        }
                                        directionTriggered = true
                                        change.consume()
                                    }
                                }
                            }
                        }
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (currentScreenState == "LOBBY") {
                var lobbyTabState by remember { mutableStateOf("LEVELS") }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Welcome Header
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF13111C)),
                        border = BorderStroke(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(Color(0xFFC084FC), Color(0xFF7C3AED))
                                        ),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🐍", fontSize = 28.sp)
                            }
                            Column {
                                Text(
                                    text = "Snake Champion",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "High Score: $highScore • Rank: Elite",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // Big Start Button
                    Button(
                        onClick = {
                            soundManager.playButtonClick()
                            val targetLvl = SnakeProgressionManager.loadUnlockedLevel(context)
                            resetGame(targetLvl)
                            currentScreenState = "GAMEPLAY"
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .testTag("start_game_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF7C3AED), Color(0xFF00E5FF))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                                Text(
                                    text = "PLAY SNAKE CLASSIC",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }

                    // Tab Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF13111C), RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("LEVELS", "MISSIONS", "AWARDS", "STATS").forEach { tab ->
                            val isActive = lobbyTabState == tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isActive) Color(0xFF7C4DFF) else Color.Transparent,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        soundManager.playButtonClick()
                                        lobbyTabState = tab
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tab,
                                    color = if (isActive) Color.White else Color.Gray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    when (lobbyTabState) {
                        "LEVELS" -> {
                            val activeUnlockedLvl = SnakeProgressionManager.loadUnlockedLevel(context)
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "ADVENTURE (100 LEVELS)",
                                        color = Color.Gray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    // Total stars overall
                                    val totalStarsEarned = (1..100).sumOf { lvlNum ->
                                        SnakeProgressionManager.loadLevelStars(context, lvlNum)
                                    }
                                    Text(
                                        text = "🏆 TOTAL: ⭐ $totalStarsEarned / 300",
                                        color = Color(0xFFFFD700),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                for (worldIdx in 0..9) {
                                    val startLvl = worldIdx * 10 + 1
                                    val endLvl = startLvl + 9
                                    val theme = SnakeTheme.values()[worldIdx]
                                    
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF13111C)),
                                        border = BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.25f)),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text(theme.emoji, fontSize = 18.sp)
                                                    Text(
                                                        text = "Ch. ${worldIdx + 1}: ${theme.title}",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp
                                                    )
                                                }
                                                val worldStars = (startLvl..endLvl).sumOf { lvlNum ->
                                                    SnakeProgressionManager.loadLevelStars(context, lvlNum)
                                                }
                                                Text(
                                                    text = "⭐ $worldStars / 30",
                                                    color = Color(0xFFFFD700),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            
                                            Spacer(modifier = Modifier.height(10.dp))
                                            
                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                for (row in 0..1) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        for (col in 0..4) {
                                                            val localLvlIdx = row * 5 + col
                                                            val lvlNum = startLvl + localLvlIdx
                                                            val isUnlocked = lvlNum <= maxOf(unlockedLevel, activeUnlockedLvl)
                                                            val stars = SnakeProgressionManager.loadLevelStars(context, lvlNum)
                                                            val isBoss = lvlNum % 10 == 0
                                                            val isBonus = lvlNum % 10 == 5
                                                            
                                                            Box(
                                                                modifier = Modifier
                                                                    .weight(1f)
                                                                    .aspectRatio(1f)
                                                                    .clip(RoundedCornerShape(12.dp))
                                                                    .background(
                                                                        if (isUnlocked) {
                                                                            if (isBoss) Color(0xFFFF1744).copy(alpha = 0.15f)
                                                                            else if (isBonus) Color(0xFFE040FB).copy(alpha = 0.15f)
                                                                            else theme.primaryColor.copy(alpha = 0.08f)
                                                                        } else Color.White.copy(alpha = 0.03f)
                                                                    )
                                                                    .border(
                                                                        width = 1.dp,
                                                                        color = if (isUnlocked) {
                                                                            if (isBoss) Color(0xFFFF1744)
                                                                            else if (isBonus) Color(0xFFE040FB)
                                                                            else theme.primaryColor.copy(alpha = 0.4f)
                                                                        } else Color.White.copy(alpha = 0.08f),
                                                                        shape = RoundedCornerShape(12.dp)
                                                                    )
                                                                    .clickable(enabled = isUnlocked) {
                                                                        soundManager.playButtonClick()
                                                                        resetGame(lvlNum)
                                                                        currentScreenState = "GAMEPLAY"
                                                                    },
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                if (!isUnlocked) {
                                                                    Text("🔒", fontSize = 12.sp)
                                                                } else {
                                                                    Column(
                                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                                        verticalArrangement = Arrangement.Center
                                                                    ) {
                                                                        Text(
                                                                            text = "$lvlNum",
                                                                            color = if (isBoss) Color(0xFFFF1744) else if (isBonus) Color(0xFFE040FB) else Color.White,
                                                                            fontWeight = FontWeight.Black,
                                                                            fontSize = 12.sp
                                                                        )
                                                                        
                                                                        if (!isBonus) {
                                                                            Row(
                                                                                horizontalArrangement = Arrangement.Center,
                                                                                verticalAlignment = Alignment.CenterVertically
                                                                            ) {
                                                                                repeat(3) { starIdx ->
                                                                                    Text(
                                                                                        text = "★",
                                                                                        color = if (starIdx < stars) Color(0xFFFFD700) else Color.Gray.copy(alpha = 0.4f),
                                                                                        fontSize = 8.sp
                                                                                    )
                                                                                }
                                                                            }
                                                                        } else {
                                                                            Text("🎁", fontSize = 9.sp)
                                                                        }
                                                                    }

                                                                    if (stars > 0) {
                                                                        Box(
                                                                            modifier = Modifier
                                                                                .align(Alignment.TopEnd)
                                                                                .padding(4.dp)
                                                                                .size(12.dp)
                                                                                .background(Color(0xFF00E676), CircleShape),
                                                                            contentAlignment = Alignment.Center
                                                                        ) {
                                                                            Text(
                                                                                text = "✓",
                                                                                color = Color.Black,
                                                                                fontSize = 8.sp,
                                                                                fontWeight = FontWeight.Black
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
                            }
                        }

                        "MISSIONS" -> {
                            // Mission List
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "DAILY MISSIONS (RESETS DAILY)",
                                    color = Color.Gray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )

                                dailyMissions.forEach { m ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF13111C)),
                                        border = BorderStroke(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.15f)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = m.title,
                                                    color = Color.White,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Text("🪙", fontSize = 12.sp)
                                                    Text(
                                                        text = "${m.reward} Coins",
                                                        color = Color(0xFFFFD700),
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                LinearProgressIndicator(
                                                    progress = { (m.progress.toFloat() / m.target).coerceIn(0f, 1f) },
                                                    color = Color(0xFF00E5FF),
                                                    trackColor = Color.Black.copy(alpha = 0.4f),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(6.dp)
                                                        .clip(CircleShape)
                                                )
                                                Text(
                                                    text = "${m.progress}/${m.target}",
                                                    color = Color.Gray,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            if (m.progress >= m.target) {
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Button(
                                                    onClick = {
                                                        if (!m.claimed) {
                                                            soundManager.playCoinReward()
                                                            viewModel.addCoins(m.reward, "Snake Daily Mission: ${m.title}")
                                                            hapticManager.vibrateSuccess()
                                                            
                                                            val updatedMissions = dailyMissions.map { dm ->
                                                                if (dm.id == m.id) dm.copy(claimed = true) else dm
                                                            }
                                                            dailyMissions = updatedMissions
                                                            SnakeProgressionManager.saveMissions(context, updatedMissions)
                                                            
                                                            android.widget.Toast.makeText(
                                                                context,
                                                                "Claimed ${m.reward} Coins!",
                                                                android.widget.Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    },
                                                    enabled = !m.claimed,
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = Color(0xFFFFD700),
                                                        disabledContainerColor = Color.Gray.copy(alpha = 0.2f)
                                                    ),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.fillMaxWidth().height(36.dp)
                                                ) {
                                                    Text(
                                                        text = if (m.claimed) "CLAIMED ✓" else "CLAIM REWARD 🪙",
                                                        color = if (m.claimed) Color.White.copy(alpha = 0.5f) else Color.Black,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        "AWARDS" -> {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "PERMANENT ACHIEVEMENTS",
                                    color = Color.Gray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )

                                achievements.forEach { a ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF13111C)),
                                        border = BorderStroke(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.15f)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text(a.icon, fontSize = 20.sp)
                                                    Column {
                                                        Text(
                                                            text = a.name,
                                                            color = Color.White,
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Text(
                                                            text = a.description,
                                                            color = Color.Gray,
                                                            fontSize = 11.sp
                                                        )
                                                    }
                                                }
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Text("🪙", fontSize = 11.sp)
                                                    Text(
                                                        text = "${a.reward}",
                                                        color = Color(0xFFFFD700),
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                LinearProgressIndicator(
                                                    progress = { (a.progress.toFloat() / a.target).coerceIn(0f, 1f) },
                                                    color = Color(0xFF7C4DFF),
                                                    trackColor = Color.Black.copy(alpha = 0.4f),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(6.dp)
                                                        .clip(CircleShape)
                                                )
                                                Text(
                                                    text = "${a.progress}/${a.target}",
                                                    color = Color.Gray,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            if (a.unlocked) {
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Button(
                                                    onClick = {
                                                        if (!a.claimed) {
                                                            soundManager.playCoinReward()
                                                            viewModel.addCoins(a.reward, "Snake Achievement: ${a.name}")
                                                            hapticManager.vibrateSuccess()
                                                            
                                                            val updatedAchievements = achievements.map { ac ->
                                                                if (ac.id == a.id) ac.copy(claimed = true) else ac
                                                            }
                                                            achievements = updatedAchievements
                                                            SnakeProgressionManager.saveAchievements(context, updatedAchievements)
                                                            
                                                            android.widget.Toast.makeText(
                                                                context,
                                                                "Claimed ${a.reward} Coins!",
                                                                android.widget.Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    },
                                                    enabled = !a.claimed,
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = Color(0xFFFFD700),
                                                        disabledContainerColor = Color.Gray.copy(alpha = 0.2f)
                                                    ),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.fillMaxWidth().height(36.dp)
                                                ) {
                                                    Text(
                                                        text = if (a.claimed) "CLAIMED ✓" else "CLAIM ACHIEVEMENT 🏆",
                                                        color = if (a.claimed) Color.White.copy(alpha = 0.5f) else Color.Black,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        "STATS" -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF13111C)),
                                border = BorderStroke(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.15f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Text(
                                        text = "LIFETIME STATISTICS",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    val statsList = listOf(
                                        "Games Played" to "${stats.gamesPlayed} matches",
                                        "All-Time High Score" to "$highScore pts",
                                        "Total Fruits Eaten" to "${stats.totalFruits} fruits",
                                        "Longest Survival" to "${stats.longestSurvivalTime}s",
                                        "Extra Coins Earned" to "${stats.totalCoinsEarned} 🪙"
                                    )

                                    statsList.forEach { (label, value) ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(label, color = Color.Gray, fontSize = 13.sp)
                                            Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = {
                                            soundManager.playButtonClick()
                                            SnakeProgressionManager.resetProgress(context)
                                            unlockedLevel = SnakeProgressionManager.loadUnlockedLevel(context)
                                            android.widget.Toast.makeText(context, "Level Progress Reset to Level 1", android.widget.Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744).copy(alpha = 0.2f)),
                                        border = BorderStroke(1.dp, Color(0xFFFF1744)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth().height(38.dp)
                                    ) {
                                        Text("RESET LEVEL PROGRESS 🔄", color = Color(0xFFFF1744), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Stats Panel
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isAdventureMode) {
                    val remainingFoods = 5 - (fruitsCollectedThisGame % 5)
                    val classicProgress = (fruitsCollectedThisGame % 5) / 5f
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF13111C), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("SCORE", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "$score",
                                color = Color(0xFF00E5FF),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(24.dp)
                                .background(Color.Gray.copy(alpha = 0.2f))
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1.2f)) {
                            Text("BEST SCORE", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "$highScore",
                                color = Color(0xFFFFD700),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(24.dp)
                                .background(Color.Gray.copy(alpha = 0.2f))
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("LEVEL", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "Level $classicLevel",
                                color = Color(0xFF00FFCC),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(24.dp)
                                .background(Color.Gray.copy(alpha = 0.2f))
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1.3f)) {
                            Text("SPEED", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "x${"%.2f".format(speedMultiplier)}",
                                color = Color(0xFF7C4DFF),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress Indicator
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Next Level: $remainingFoods Foods Remaining",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        LinearProgressIndicator(
                            progress = { classicProgress },
                            color = Color(0xFF7C4DFF),
                            trackColor = Color(0xFF13111C),
                            modifier = Modifier
                                .width(100.dp)
                                .height(6.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${fruitsCollectedThisGame % 5} / 5",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF13111C), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("SCORE", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "$score",
                                color = Color(0xFF00E5FF),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(24.dp)
                                .background(Color.Gray.copy(alpha = 0.2f))
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1.2f)) {
                            Text("TARGET", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "$levelFruitsCollected / ${activeLevel?.targetFruits ?: 0}",
                                color = Color(0xFFFFD700),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(24.dp)
                                .background(Color.Gray.copy(alpha = 0.2f))
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("LEVEL", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "Lvl $currentLevelNumber",
                                color = Color(0xFF00FFCC),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(24.dp)
                                .background(Color.Gray.copy(alpha = 0.2f))
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("SPEED", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "Lvl $speedLevel",
                                color = Color(0xFF7C4DFF),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Next Level",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        LinearProgressIndicator(
                            progress = { speedProgress },
                            color = Color(0xFF7C4DFF),
                            trackColor = Color(0xFF13111C),
                            modifier = Modifier
                                .width(100.dp)
                                .height(6.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${(score / 10) % 5} / 5",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Power-up Countdown Status Bar
                val activePowerUps = listOf(
                    Triple("🛡️ Shield", shieldTimeLeft, Color(0xFF00E5FF)),
                    Triple("🧲 Magnet", magnetTimeLeft, Color(0xFFE040FB)),
                    Triple("⏱️ Slow Mo", slowMotionTimeLeft, Color(0xFF29B6F6)),
                    Triple("🪙 2X Coins", doubleCoinsTimeLeft, Color(0xFFFFD700)),
                    Triple("⚡ Frenzy", fruitFrenzyTimeLeft, Color(0xFFE040FB))
                ).filter { it.second > 0 }

                if (activePowerUps.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        activePowerUps.forEach { (name, secondsLeft, color) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF13111C))
                                    .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.size(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            progress = { secondsLeft.toFloat() / 30f },
                                            modifier = Modifier.fillMaxSize(),
                                            color = color,
                                            trackColor = Color.White.copy(alpha = 0.1f),
                                            strokeWidth = 2.dp
                                        )
                                        Text(
                                            text = "$secondsLeft",
                                            color = Color.White,
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = name,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Massive Expanded Game Board (incorporating decorative outer stone walls)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Stone grid dimensions including borders
                val visualWidth = gridWidth + 2
                val visualHeight = gridHeight + 2

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(visualWidth.toFloat() / visualHeight.toFloat())
                        .offset(x = shakeOffsetX.value.dp, y = shakeOffsetY.value.dp)
                        .shadow(16.dp, RoundedCornerShape(16.dp))
                        .border(1.5.dp, Brush.horizontalGradient(listOf(Color(0xFF7C4DFF).copy(alpha = 0.5f), Color(0xFF00E5FF).copy(alpha = 0.5f))), RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0F0C1B))
                        .testTag("game_board")
                ) {
                    val boardSizePxWidth = constraints.maxWidth
                    val boardSizePxHeight = constraints.maxHeight
                    val cellSize = minOf(boardSizePxWidth.toFloat() / visualWidth, boardSizePxHeight.toFloat() / visualHeight)

                    val finalWidthDp = with(LocalDensity.current) { (cellSize * visualWidth).toDp() }
                    val finalHeightDp = with(LocalDensity.current) { (cellSize * visualHeight).toDp() }

                    Box(
                        modifier = Modifier.size(finalWidthDp, finalHeightDp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize().testTag("snake_canvas")) {
                            // Calculate interpolated positions of all segments
                            val progress = moveProgress.value
                            val points = snake.mapIndexed { idx, pt ->
                                val prevPt = previousSnake.getOrNull(idx) ?: pt
                                val lx = prevPt.x + (pt.x - prevPt.x) * progress
                                val ly = prevPt.y + (pt.y - prevPt.y) * progress
                                Offset((lx + 1) * cellSize + cellSize / 2f, (ly + 1) * cellSize + cellSize / 2f)
                            }
                            val headCenterRaw = points.firstOrNull() ?: Offset(0f, 0f)
                            val headCenter = if (isDeadAnimating) headCenterRaw + Offset(snakeDeathShake.value, snakeDeathShake.value) else headCenterRaw

                            // 1. Draw Playable Area inner background with subtle grid lines
                            drawRect(
                                color = Color(0xFF0A0714),
                                topLeft = Offset(cellSize, cellSize),
                                size = Size(gridWidth * cellSize, gridHeight * cellSize)
                            )

                            // Dynamic board head aura lighting (moving radial glow)
                            if (points.isNotEmpty() && !isGameOver) {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(Color(0xFF7C4DFF).copy(alpha = 0.08f), Color.Transparent),
                                        center = headCenter,
                                        radius = cellSize * 5.2f
                                    ),
                                    radius = cellSize * 5.2f,
                                    center = headCenter
                                )
                            }

                            // Subtle, neon-tinted inner playable grid lines
                            val linePaint = Color(0xFF7C4DFF).copy(alpha = 0.08f)
                            for (i in 1 until gridWidth) {
                                drawLine(
                                    color = linePaint,
                                    start = Offset((i + 1) * cellSize, cellSize),
                                    end = Offset((i + 1) * cellSize, (gridHeight + 1) * cellSize),
                                    strokeWidth = 1f
                                )
                            }
                            for (i in 1 until gridHeight) {
                                drawLine(
                                    color = linePaint,
                                    start = Offset(cellSize, (i + 1) * cellSize),
                                    end = Offset((gridWidth + 1) * cellSize, (i + 1) * cellSize),
                                    strokeWidth = 1f
                                )
                            }

                            // 2. Draw Decorative Stone Walls (Stay completely outside the playable grid)
                            val wallPrimary = Color(0xFF1D1A30)
                            val wallHighlight = Color(0xFF4C3E75)
                            val wallNeonShadow = Color(0xFF7C4DFF).copy(alpha = 0.35f)
                            val brickCornerRadius = 3.dp.toPx()

                            fun drawStoneBrick(vx: Int, vy: Int) {
                                val tx = vx * cellSize
                                val ty = vy * cellSize
                                val margin = 1f

                                // Outer glowing stroke for wall definition
                                drawRoundRect(
                                    color = wallNeonShadow,
                                    topLeft = Offset(tx + margin, ty + margin),
                                    size = Size(cellSize - margin * 2f, cellSize - margin * 2f),
                                    cornerRadius = CornerRadius(brickCornerRadius),
                                    style = Stroke(width = 1.dp.toPx())
                                )

                                // Brick Body Fill
                                drawRoundRect(
                                    color = wallPrimary,
                                    topLeft = Offset(tx + margin + 1f, ty + margin + 1f),
                                    size = Size(cellSize - margin * 2f - 2f, cellSize - margin * 2f - 2f),
                                    cornerRadius = CornerRadius(brickCornerRadius)
                                )

                                // Beveled light highlight (Top and Left inner edge)
                                drawLine(
                                    color = wallHighlight,
                                    start = Offset(tx + margin + 2f, ty + margin + 2f),
                                    end = Offset(tx + cellSize - margin - 2f, ty + margin + 2f),
                                    strokeWidth = 1.5f
                                )
                                drawLine(
                                    color = wallHighlight,
                                    start = Offset(tx + margin + 2f, ty + margin + 2f),
                                    end = Offset(tx + margin + 2f, ty + cellSize - margin - 2f),
                                    strokeWidth = 1.5f
                                )

                                // Small decorative detail lines on stone
                                drawLine(
                                    color = Color.Black.copy(alpha = 0.4f),
                                    start = Offset(tx + cellSize * 0.3f, ty + cellSize * 0.4f),
                                    end = Offset(tx + cellSize * 0.7f, ty + cellSize * 0.4f),
                                    strokeWidth = 1f
                                )
                                drawLine(
                                    color = Color.Black.copy(alpha = 0.4f),
                                    start = Offset(tx + cellSize * 0.5f, ty + cellSize * 0.4f),
                                    end = Offset(tx + cellSize * 0.5f, ty + cellSize * 0.7f),
                                    strokeWidth = 1f
                                )
                            }

                            // Draw walls horizontally (top & bottom border)
                            for (vx in 0 until visualWidth) {
                                drawStoneBrick(vx, 0)
                                drawStoneBrick(vx, visualHeight - 1)
                            }
                            // Draw walls vertically (left & right border)
                            for (vy in 1 until visualHeight - 1) {
                                drawStoneBrick(0, vy)
                                drawStoneBrick(visualWidth - 1, vy)
                            }

                            // Adventure Mode Hazards, Elements and Characters
                            if (isAdventureMode) {
                                // A. Lava Tiles (animated molten blocks)
                                activeLevel?.lavaTiles?.forEach { lavaPt ->
                                    val lx = (lavaPt.x + 1) * cellSize
                                    val ly = (lavaPt.y + 1) * cellSize
                                    val margin = 1f
                                    drawRect(
                                        color = Color(0xFFFF3D00).copy(alpha = 0.35f + 0.15f * foodPulseScale),
                                        topLeft = Offset(lx + margin, ly + margin),
                                        size = Size(cellSize - margin * 2, cellSize - margin * 2)
                                    )
                                    drawCircle(
                                        color = Color(0xFFFFD600).copy(alpha = 0.7f),
                                        radius = cellSize * 0.25f,
                                        center = Offset(lx + cellSize / 2f, ly + cellSize / 2f)
                                    )
                                }

                                // B. Static Concrete Obstacles
                                activeLevel?.obstacles?.forEach { obsPt ->
                                    val ox = (obsPt.x + 1) * cellSize
                                    val oy = (obsPt.y + 1) * cellSize
                                    val margin = 1f
                                    drawRoundRect(
                                        color = Color(0xFF37474F),
                                        topLeft = Offset(ox + margin, oy + margin),
                                        size = Size(cellSize - margin * 2, cellSize - margin * 2),
                                        cornerRadius = CornerRadius(4.dp.toPx())
                                    )
                                    drawRoundRect(
                                        color = Color(0xFF78909C),
                                        topLeft = Offset(ox + margin, oy + margin),
                                        size = Size(cellSize - margin * 2, cellSize - margin * 2),
                                        cornerRadius = CornerRadius(4.dp.toPx()),
                                        style = Stroke(width = 1.5.dp.toPx())
                                    )
                                    drawLine(
                                        color = Color(0xFF263238),
                                        start = Offset(ox + cellSize * 0.2f, oy + cellSize * 0.8f),
                                        end = Offset(ox + cellSize * 0.8f, oy + cellSize * 0.2f),
                                        strokeWidth = 2.dp.toPx()
                                    )
                                }

                                // C. Breakable Walls
                                currentBreakableWalls.forEach { wallPt ->
                                    val wx = (wallPt.x + 1) * cellSize
                                    val wy = (wallPt.y + 1) * cellSize
                                    val margin = 1f
                                    drawRoundRect(
                                        color = Color(0xFF8D6E63),
                                        topLeft = Offset(wx + margin, wy + margin),
                                        size = Size(cellSize - margin * 2, cellSize - margin * 2),
                                        cornerRadius = CornerRadius(3.dp.toPx())
                                    )
                                    drawRoundRect(
                                        color = Color(0xFFD7CCC8),
                                        topLeft = Offset(wx + margin, wy + margin),
                                        size = Size(cellSize - margin * 2, cellSize - margin * 2),
                                        cornerRadius = CornerRadius(3.dp.toPx()),
                                        style = Stroke(width = 1.dp.toPx())
                                    )
                                    drawLine(
                                        color = Color(0xFF4E342E),
                                        start = Offset(wx + cellSize * 0.3f, wy + cellSize * 0.3f),
                                        end = Offset(wx + cellSize * 0.7f, wy + cellSize * 0.7f),
                                        strokeWidth = 1.5.dp.toPx()
                                    )
                                }

                                // D. Warp Portals (neon spinning vortex rings)
                                activeLevel?.portals?.forEach { (p1, p2) ->
                                    listOf(p1, p2).forEach { portalPt ->
                                        val px = (portalPt.x + 1) * cellSize + cellSize / 2f
                                        val py = (portalPt.y + 1) * cellSize + cellSize / 2f
                                        val pRadius = cellSize * 0.45f
                                        drawCircle(
                                            color = Color(0xFF00E5FF).copy(alpha = 0.4f * foodPulseScale),
                                            radius = pRadius * 1.3f,
                                            center = Offset(px, py),
                                            style = Stroke(width = 3.dp.toPx())
                                        )
                                        drawCircle(
                                            brush = Brush.sweepGradient(
                                                colors = listOf(Color(0xFF00E5FF), Color(0xFF00B0FF), Color.Transparent, Color(0xFF00E5FF)),
                                                center = Offset(px, py)
                                            ),
                                            radius = pRadius,
                                            center = Offset(px, py)
                                        )
                                        drawCircle(
                                            color = Color(0xFF0F0C1B),
                                            radius = pRadius * 0.4f,
                                            center = Offset(px, py)
                                        )
                                    }
                                }

                                // E. Wall-breaking Crystals
                                crystalPoint?.let { cp ->
                                    val cx = (cp.x + 1) * cellSize + cellSize / 2f
                                    val cy = (cp.y + 1) * cellSize + cellSize / 2f
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(Color(0xFFE040FB).copy(alpha = 0.45f * foodPulseScale), Color.Transparent),
                                            center = Offset(cx, cy),
                                            radius = cellSize * 2f
                                        ),
                                        radius = cellSize * 2f,
                                        center = Offset(cx, cy)
                                    )
                                    drawContext.canvas.nativeCanvas.drawText(
                                        "💎",
                                        cx,
                                        cy + cellSize * 0.18f,
                                        Paint().apply {
                                            textSize = cellSize * 0.65f
                                            textAlign = Paint.Align.CENTER
                                        }
                                    )
                                }

                                // F. Boss Monster Character
                                if (activeLevel?.isBoss == true) {
                                    val bx = (bossPosition.x + 1) * cellSize + cellSize / 2f
                                    val by = (bossPosition.y + 1) * cellSize + cellSize / 2f
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(Color(0xFFFF1744).copy(alpha = 0.5f + 0.15f * foodPulseScale), Color.Transparent),
                                            center = Offset(bx, by),
                                            radius = cellSize * 3.5f
                                        ),
                                        radius = cellSize * 3.5f,
                                        center = Offset(bx, by)
                                    )
                                    drawContext.canvas.nativeCanvas.drawText(
                                        "💀",
                                        bx,
                                        by + cellSize * 0.24f,
                                        Paint().apply {
                                            textSize = cellSize * 0.95f
                                            textAlign = Paint.Align.CENTER
                                        }
                                    )
                                }

                                // G. Boss Flame Traps
                                activeTraps.forEach { trapPt ->
                                    val tx = (trapPt.x + 1) * cellSize + cellSize / 2f
                                    val ty = (trapPt.y + 1) * cellSize + cellSize / 2f
                                    drawCircle(
                                        color = Color(0xFFFF9100).copy(alpha = 0.5f + 0.2f * foodPulseScale),
                                        radius = cellSize * 0.4f,
                                        center = Offset(tx, ty),
                                        style = Stroke(width = 2.dp.toPx())
                                    )
                                    drawCircle(
                                        color = Color(0xFFFF3D00).copy(alpha = 0.8f),
                                        radius = cellSize * 0.2f,
                                        center = Offset(tx, ty)
                                    )
                                }
                            }

                            // 3. Draw Active Food Collection Glow Flash (Over the background)
                            if (boardFlashAlpha.value > 0f) {
                                drawRect(
                                    color = Color.White.copy(alpha = boardFlashAlpha.value),
                                    topLeft = Offset(cellSize, cellSize),
                                    size = Size(gridWidth * cellSize, gridHeight * cellSize)
                                )
                            }

                            // 4. Draw Particles (Food explosion burst)
                            particles.forEach { p ->
                                val px = (p.x + 1) * cellSize
                                val py = (p.y + 1) * cellSize
                                val pSize = p.size * cellSize
                                drawCircle(
                                    color = p.color.copy(alpha = p.alpha),
                                    radius = pSize,
                                    center = Offset(px, py)
                                )
                            }

                            // 5. Draw Soft Glow & Glossy Peach Food
                            val foodX = (food.x + 1) * cellSize + cellSize / 2f
                            val foodY = (food.y + 1) * cellSize + cellSize / 2f
                            val foodSize = cellSize * 0.38f

                            // Pulsing bottom warm peach glow
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFFFF5722).copy(alpha = 0.35f * foodPulseScale), Color.Transparent),
                                    center = Offset(foodX, foodY),
                                    radius = cellSize * 2.5f * foodPulseScale
                                ),
                                radius = cellSize * 2.5f * foodPulseScale,
                                center = Offset(foodX, foodY)
                            )
                            drawCircle(
                                color = Color(0xFFFF5722).copy(alpha = 0.35f),
                                radius = foodSize * 1.1f * (0.9f + foodPulseScale * 0.1f),
                                center = Offset(foodX, foodY)
                            )

                            // Peach main gradient body
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFFFFB74D), Color(0xFFF4511E)),
                                    center = Offset(foodX - foodSize * 0.2f, foodY - foodSize * 0.2f),
                                    radius = foodSize
                                ),
                                radius = foodSize,
                                center = Offset(foodX, foodY)
                            )

                            // 5b. Draw Golden Bonus Food if active (pulsing bright gold color)
                            bonusFood?.let { bf ->
                                val bX = (bf.x + 1) * cellSize + cellSize / 2f
                                val bY = (bf.y + 1) * cellSize + cellSize / 2f
                                val bSize = cellSize * 0.44f
                                val pulseVal = foodPulseScale

                                // Golden radial glow
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(Color(0xFFFBC02D).copy(alpha = 0.45f * pulseVal), Color.Transparent),
                                        center = Offset(bX, bY),
                                        radius = cellSize * 3.2f * pulseVal
                                    ),
                                    radius = cellSize * 3.2f * pulseVal,
                                    center = Offset(bX, bY)
                                )

                                // Golden body
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(Color(0xFFFFF59D), Color(0xFFFBC02D)),
                                        center = Offset(bX - bSize * 0.2f, bY - bSize * 0.2f),
                                        radius = bSize
                                    ),
                                    radius = bSize,
                                    center = Offset(bX, bY)
                                )

                                // Crown stem
                                drawLine(
                                    color = Color(0xFF8D6E63),
                                    start = Offset(bX, bY - bSize * 0.7f),
                                    end = Offset(bX + bSize * 0.2f, bY - bSize * 1.1f),
                                    strokeWidth = 3.dp.toPx(),
                                    cap = StrokeCap.Round
                                )

                                // Sparkle gloss
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.9f),
                                    radius = bSize * 0.28f,
                                    center = Offset(bX - bSize * 0.35f, bY - bSize * 0.35f)
                                )
                            }

                            // Leaf stem
                            drawLine(
                                color = Color(0xFF8D6E63),
                                start = Offset(foodX, foodY - foodSize * 0.7f),
                                end = Offset(foodX + foodSize * 0.3f, foodY - foodSize * 1.2f),
                                strokeWidth = 2.dp.toPx(),
                                cap = StrokeCap.Round
                            )

                            // Green Leaf
                            val leafPath = Path().apply {
                                moveTo(foodX + foodSize * 0.2f, foodY - foodSize * 0.9f)
                                quadraticTo(
                                    foodX + foodSize * 0.8f, foodY - foodSize * 1.3f,
                                    foodX + foodSize * 0.9f, foodY - foodSize * 0.7f
                                )
                                quadraticTo(
                                    foodX + foodSize * 0.5f, foodY - foodSize * 0.6f,
                                    foodX + foodSize * 0.2f, foodY - foodSize * 0.9f
                                )
                            }
                            drawPath(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF81C784), Color(0xFF2E7D32))
                                ),
                                path = leafPath
                            )

                            // Peach crease indentation line
                            drawArc(
                                color = Color(0xFFD84315).copy(alpha = 0.6f),
                                startAngle = 90f,
                                sweepAngle = 180f,
                                useCenter = false,
                                topLeft = Offset(foodX - foodSize * 0.15f, foodY - foodSize),
                                size = Size(foodSize * 0.3f, foodSize * 2f),
                                style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                            )

                            // Glossy Reflection Highlight
                            drawCircle(
                                color = Color.White.copy(alpha = 0.85f),
                                radius = foodSize * 0.24f,
                                center = Offset(foodX - foodSize * 0.38f, foodY - foodSize * 0.38f)
                            )
                            drawCircle(
                                color = Color.White.copy(alpha = 0.4f),
                                radius = foodSize * 0.12f,
                                center = Offset(foodX - foodSize * 0.25f, foodY - foodSize * 0.25f)
                            )

                            // 6. Draw Soft Neon Trails behind the snake body
                            if (points.size > 1 && snakeDeathAlpha.value > 0.05f) {
                                val trailPath = Path().apply {
                                    points.forEachIndexed { idx, offset ->
                                        val targetOffset = if (idx == 0) headCenter else offset
                                        if (idx == 0) moveTo(targetOffset.x, targetOffset.y) else lineTo(targetOffset.x, targetOffset.y)
                                    }
                                }
                                drawPath(
                                    path = trailPath,
                                    color = Color(0xFFA855F7).copy(alpha = 0.15f * snakeDeathAlpha.value),
                                    style = Stroke(width = cellSize * 0.95f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )
                                drawPath(
                                    path = trailPath,
                                    color = Color(0xFF00E5FF).copy(alpha = 0.25f * snakeDeathAlpha.value),
                                    style = Stroke(width = cellSize * 0.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )
                            }

                            // 7. Draw Animated Cartoon Snake
                            if (snakeDeathAlpha.value > 0.01f) {
                                points.forEachIndexed { index, segmentCenter ->
                                    val currentPoint = snake.getOrElse(index) { SnakePoint(0, 0) }
                                    val previousPoint = previousSnake.getOrNull(index) ?: currentPoint

                                    // Calculate tail-thinning scale down factor towards end of body
                                    val tailScale = (1.0f - (index.toFloat() / snake.size) * 0.45f).coerceAtLeast(0.55f)

                                    if (index == 0) {
                                        // Identify current head heading direction
                                        val dx = currentPoint.x - previousPoint.x
                                        val dy = currentPoint.y - previousPoint.y
                                        val heading = if (dx != 0 || dy != 0) {
                                            if (dx > 0) SnakeDirection.RIGHT
                                            else if (dx < 0) SnakeDirection.LEFT
                                            else if (dy > 0) SnakeDirection.DOWN
                                            else SnakeDirection.UP
                                        } else {
                                            direction
                                        }

                                        // Calculate exact interpolated rotation angle following direction
                                        fun getAngle(dir: SnakeDirection): Float {
                                            return when (dir) {
                                                SnakeDirection.UP -> -90f
                                                SnakeDirection.DOWN -> 90f
                                                SnakeDirection.LEFT -> 180f
                                                SnakeDirection.RIGHT -> 0f
                                            }
                                        }
                                        val prevAngle = getAngle(previousDirection)
                                        val targetAngle = getAngle(direction)
                                        var diff = targetAngle - prevAngle
                                        while (diff < -180f) diff += 360f
                                        while (diff > 180f) diff -= 360f
                                        val interpolatedAngle = prevAngle + diff * progress

                                        // Add active turning tilt wobble
                                        val tiltAngle = if (direction != previousDirection) {
                                            sin(progress * PI.toFloat()) * 16f
                                        } else 0f

                                        val finalHeadAngle = interpolatedAngle + tiltAngle

                                        // Interactive squash/stretch animations
                                        val squashFactor = 0.14f * sin(progress * PI.toFloat())
                                        val (scaleX, scaleY) = when (heading) {
                                            SnakeDirection.UP, SnakeDirection.DOWN -> Pair(1f - squashFactor, 1f + squashFactor)
                                            SnakeDirection.LEFT, SnakeDirection.RIGHT -> Pair(1f + squashFactor, 1f - squashFactor)
                                        }

                                        val headWidth = (cellSize * 0.95f) * scaleX
                                        val headHeight = (cellSize * 0.95f) * scaleY

                                        withTransform({
                                            rotate(degrees = finalHeadAngle, pivot = headCenter)
                                        }) {
                                            // A. Forked Tongue Animation
                                            val tongueColor = Color(0xFFFF1744).copy(alpha = snakeDeathAlpha.value)
                                            val tongueLength = cellSize * 0.35f * tongueProgress
                                            val frontPoint = Offset(headCenter.x + headWidth / 2f, headCenter.y)

                                            val forkDist = 4.dp.toPx()
                                            val tonguePath = Path().apply {
                                                moveTo(frontPoint.x, frontPoint.y)
                                                lineTo(frontPoint.x + tongueLength, frontPoint.y)
                                                moveTo(frontPoint.x + tongueLength, frontPoint.y)
                                                lineTo(frontPoint.x + tongueLength + forkDist, frontPoint.y - forkDist)
                                                moveTo(frontPoint.x + tongueLength, frontPoint.y)
                                                lineTo(frontPoint.x + tongueLength + forkDist, frontPoint.y + forkDist)
                                            }
                                            drawPath(
                                                path = tonguePath,
                                                color = tongueColor,
                                                style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
                                            )

                                            // B. Head outer soft neon glow / Shield halo
                                            if (hasShield) {
                                                drawCircle(
                                                    color = Color(0xFF00E5FF).copy(alpha = 0.55f * snakeDeathAlpha.value),
                                                    radius = cellSize * 0.9f,
                                                    center = headCenter,
                                                    style = Stroke(width = 3.dp.toPx())
                                                )
                                                drawCircle(
                                                    brush = Brush.radialGradient(
                                                        colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.25f), Color.Transparent),
                                                        center = headCenter,
                                                        radius = cellSize * 1.4f
                                                    ),
                                                    radius = cellSize * 1.4f,
                                                    center = headCenter
                                                )
                                            } else {
                                                drawCircle(
                                                    color = Color(0xFFA855F7).copy(alpha = 0.28f * snakeDeathAlpha.value),
                                                    radius = cellSize * 0.72f,
                                                    center = headCenter
                                                )
                                            }

                                            // C. Head Body shape (gorgeous cartoon purple gradient look)
                                            drawRoundRect(
                                                brush = Brush.radialGradient(
                                                    colors = listOf(Color(0xFFC084FC), Color(0xFF7C3AED)),
                                                    center = Offset(headCenter.x - headWidth * 0.15f, headCenter.y - headHeight * 0.15f),
                                                    radius = cellSize * 0.65f
                                                ),
                                                topLeft = Offset(headCenter.x - headWidth / 2f, headCenter.y - headHeight / 2f),
                                                size = Size(headWidth, headHeight),
                                                cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                                                alpha = snakeDeathAlpha.value
                                            )

                                            // D. Cute Expressive Eyes
                                            val eyeRadius = cellSize * 0.16f
                                            val pupilRadius = cellSize * 0.08f
                                            val glossRadius = cellSize * 0.03f

                                            val eyeOffset1 = Offset(cellSize * 0.15f, -cellSize * 0.22f)
                                            val eyeOffset2 = Offset(cellSize * 0.15f, cellSize * 0.22f)

                                            if (isDeadEyeClosed) {
                                                // Crossed (X) Dead Eyes
                                                val c1 = Offset(headCenter.x + eyeOffset1.x, headCenter.y + eyeOffset1.y)
                                                val crossSize = eyeRadius * 0.7f
                                                drawLine(color = Color.White.copy(alpha = snakeDeathAlpha.value), start = Offset(c1.x - crossSize, c1.y - crossSize), end = Offset(c1.x + crossSize, c1.y + crossSize), strokeWidth = 2.5.dp.toPx(), cap = StrokeCap.Round)
                                                drawLine(color = Color.White.copy(alpha = snakeDeathAlpha.value), start = Offset(c1.x - crossSize, c1.y + crossSize), end = Offset(c1.x + crossSize, c1.y - crossSize), strokeWidth = 2.5.dp.toPx(), cap = StrokeCap.Round)

                                                val c2 = Offset(headCenter.x + eyeOffset2.x, headCenter.y + eyeOffset2.y)
                                                drawLine(color = Color.White.copy(alpha = snakeDeathAlpha.value), start = Offset(c2.x - crossSize, c2.y - crossSize), end = Offset(c2.x + crossSize, c2.y + crossSize), strokeWidth = 2.5.dp.toPx(), cap = StrokeCap.Round)
                                                drawLine(color = Color.White.copy(alpha = snakeDeathAlpha.value), start = Offset(c2.x - crossSize, c2.y + crossSize), end = Offset(c2.x + crossSize, c2.y - crossSize), strokeWidth = 2.5.dp.toPx(), cap = StrokeCap.Round)
                                            } else if (isBlinking) {
                                                // Blink flat eyelids
                                                val c1 = Offset(headCenter.x + eyeOffset1.x, headCenter.y + eyeOffset1.y)
                                                drawLine(color = Color.White.copy(alpha = snakeDeathAlpha.value), start = Offset(c1.x - eyeRadius, c1.y), end = Offset(c1.x + eyeRadius, c1.y), strokeWidth = 2.5.dp.toPx(), cap = StrokeCap.Round)

                                                val c2 = Offset(headCenter.x + eyeOffset2.x, headCenter.y + eyeOffset2.y)
                                                drawLine(color = Color.White.copy(alpha = snakeDeathAlpha.value), start = Offset(c2.x - eyeRadius, c2.y), end = Offset(c2.x + eyeRadius, c2.y), strokeWidth = 2.5.dp.toPx(), cap = StrokeCap.Round)
                                            } else {
                                                // Normal Expressive Eyes
                                                val c1 = Offset(headCenter.x + eyeOffset1.x, headCenter.y + eyeOffset1.y)
                                                val c2 = Offset(headCenter.x + eyeOffset2.x, headCenter.y + eyeOffset2.y)

                                                drawCircle(color = Color.White.copy(alpha = snakeDeathAlpha.value), radius = eyeRadius, center = c1)
                                                drawCircle(color = Color.White.copy(alpha = snakeDeathAlpha.value), radius = eyeRadius, center = c2)

                                                // Pupil looking forward
                                                val pupilLook = Offset(1.5f, 0f)
                                                val p1 = Offset(c1.x + pupilLook.x, c1.y + pupilLook.y)
                                                val p2 = Offset(c2.x + pupilLook.x, c2.y + pupilLook.y)

                                                drawCircle(color = Color(0xFF111111).copy(alpha = snakeDeathAlpha.value), radius = pupilRadius, center = p1)
                                                drawCircle(color = Color(0xFF111111).copy(alpha = snakeDeathAlpha.value), radius = pupilRadius, center = p2)

                                                // Pupil gloss reflection
                                                drawCircle(color = Color.White.copy(alpha = snakeDeathAlpha.value), radius = glossRadius, center = Offset(p1.x - 1.5f, p1.y - 1.5f))
                                                drawCircle(color = Color.White.copy(alpha = snakeDeathAlpha.value), radius = glossRadius, center = Offset(p2.x - 1.5f, p2.y - 1.5f))
                                            }
                                        }
                                    } else if (index == snake.lastIndex) {
                                        // TAIL SEGMENT following body curves smoothly
                                        val segSize = cellSize * 0.82f * tailScale
                                        val segmentAhead = points.getOrNull(index - 1) ?: headCenter

                                        // Rotate tail to align exactly with the curve direction
                                        val dx = segmentAhead.x - segmentCenter.x
                                        val dy = segmentAhead.y - segmentCenter.y
                                        val segAngle = Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble())).toFloat()

                                        withTransform({
                                            rotate(degrees = segAngle, pivot = segmentCenter)
                                        }) {
                                            // Soft neon tail segment glow
                                            drawCircle(
                                                color = Color(0xFF7C3AED).copy(alpha = 0.12f * snakeDeathAlpha.value),
                                                radius = segSize * 0.7f,
                                                center = segmentCenter
                                            )

                                            // Draw tapered pointed tail path
                                            val tailPath = Path().apply {
                                                moveTo(segmentCenter.x - segSize / 2f, segmentCenter.y - segSize / 2f)
                                                lineTo(segmentCenter.x + segSize * 0.45f, segmentCenter.y)
                                                lineTo(segmentCenter.x - segSize / 2f, segmentCenter.y + segSize / 2f)
                                                close()
                                            }
                                            drawPath(
                                                path = tailPath,
                                                brush = Brush.linearGradient(
                                                    colors = listOf(Color(0xFF7C3AED), Color(0xFFA855F7))
                                                ),
                                                alpha = snakeDeathAlpha.value
                                            )
                                        }
                                    } else {
                                        // regular body segment
                                        val segSize = cellSize * 0.82f * tailScale

                                        // A. Soft body segment glow
                                        drawCircle(
                                            color = Color(0xFF7C3AED).copy(alpha = 0.12f * snakeDeathAlpha.value),
                                            radius = segSize * 0.7f,
                                            center = segmentCenter
                                        )

                                        // B. Body Segment gradient block
                                        drawRoundRect(
                                            brush = Brush.linearGradient(
                                                colors = listOf(Color(0xFF7C3AED), Color(0xFFA855F7))
                                            ),
                                            topLeft = Offset(segmentCenter.x - segSize / 2f, segmentCenter.y - segSize / 2f),
                                            size = Size(segSize, segSize),
                                            cornerRadius = CornerRadius(segSize * 0.45f),
                                            alpha = snakeDeathAlpha.value
                                        )

                                        // C. Subtle Cartoon Scale Texture
                                        val scaleStroke = 1.dp.toPx()
                                        val scalePaint = Color(0xFFE9D5FF).copy(alpha = 0.35f * snakeDeathAlpha.value)
                                        drawArc(
                                            color = scalePaint,
                                            startAngle = 0f,
                                            sweepAngle = 180f,
                                            useCenter = false,
                                            topLeft = Offset(segmentCenter.x - segSize * 0.22f, segmentCenter.y - segSize * 0.18f),
                                            size = Size(segSize * 0.44f, segSize * 0.33f),
                                            style = Stroke(width = scaleStroke)
                                        )
                                        drawArc(
                                            color = scalePaint,
                                            startAngle = 0f,
                                            sweepAngle = 180f,
                                            useCenter = false,
                                            topLeft = Offset(segmentCenter.x - segSize * 0.35f, segmentCenter.y + segSize * 0.08f),
                                            size = Size(segSize * 0.35f, segSize * 0.28f),
                                            style = Stroke(width = scaleStroke)
                                        )
                                        drawArc(
                                            color = scalePaint,
                                            startAngle = 0f,
                                            sweepAngle = 180f,
                                            useCenter = false,
                                            topLeft = Offset(segmentCenter.x, segmentCenter.y + segSize * 0.08f),
                                            size = Size(segSize * 0.35f, segSize * 0.28f),
                                            style = Stroke(width = scaleStroke)
                                        )
                                    }
                                }
                            }

                            // 7b. Draw Extra Fruits if Fruit Frenzy is active (pulsing magenta/purple glow)
                            extraFruits.forEach { ef ->
                                val efX = (ef.x + 1) * cellSize + cellSize / 2f
                                val efY = (ef.y + 1) * cellSize + cellSize / 2f
                                val efSize = cellSize * 0.38f

                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(Color(0xFFE040FB).copy(alpha = 0.45f * foodPulseScale), Color.Transparent),
                                        center = Offset(efX, efY),
                                        radius = cellSize * 2.8f * foodPulseScale
                                    ),
                                    radius = cellSize * 2.8f * foodPulseScale,
                                    center = Offset(efX, efY)
                                )

                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(Color(0xFFF50057), Color(0xFFD500F9)),
                                        center = Offset(efX - efSize * 0.2f, efY - efSize * 0.2f),
                                        radius = efSize
                                    ),
                                    radius = efSize,
                                    center = Offset(efX, efY)
                                )

                                drawCircle(
                                    color = Color.White.copy(alpha = 0.85f),
                                    radius = efSize * 0.24f,
                                    center = Offset(efX - efSize * 0.38f, efY - efSize * 0.38f)
                                )
                            }

                            // 7c. Draw Active Power-up or Treasure Chest on Board with neon color halo rings and colorful centered emoji symbol
                            activePowerUpOnBoard?.let { powerUp ->
                                val pX = (powerUp.point.x + 1) * cellSize + cellSize / 2f
                                val pY = (powerUp.point.y + 1) * cellSize + cellSize / 2f
                                val pRadius = cellSize * 0.52f

                                val glowColor = when (powerUp.type) {
                                    SnakeEntityType.SHIELD -> Color(0xFF00E5FF)
                                    SnakeEntityType.MAGNET -> Color(0xFFE040FB)
                                    SnakeEntityType.SLOW -> Color(0xFF29B6F6)
                                    SnakeEntityType.DOUBLE_COINS -> Color(0xFFFFD700)
                                    SnakeEntityType.CHEST -> Color(0xFFFF9100)
                                }

                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(glowColor.copy(alpha = 0.45f * foodPulseScale), Color.Transparent),
                                        center = Offset(pX, pY),
                                        radius = cellSize * 3f * foodPulseScale
                                    ),
                                    radius = cellSize * 3f * foodPulseScale,
                                    center = Offset(pX, pY)
                                )

                                drawCircle(
                                    color = Color(0xFF13111C),
                                    radius = pRadius,
                                    center = Offset(pX, pY)
                                )

                                drawCircle(
                                    color = glowColor,
                                    radius = pRadius * 1.05f,
                                    center = Offset(pX, pY),
                                    style = Stroke(width = 2.dp.toPx())
                                )

                                val emoji = when (powerUp.type) {
                                    SnakeEntityType.SHIELD -> "🛡️"
                                    SnakeEntityType.MAGNET -> "🧲"
                                    SnakeEntityType.SLOW -> "⏱️"
                                    SnakeEntityType.DOUBLE_COINS -> "🪙"
                                    SnakeEntityType.CHEST -> "🎁"
                                }
                                
                                drawContext.canvas.nativeCanvas.drawText(
                                    emoji,
                                    pX,
                                    pY + cellSize * 0.22f,
                                    Paint().apply {
                                        textSize = cellSize * 0.72f
                                        textAlign = Paint.Align.CENTER
                                    }
                                )
                            }

                            // 8. Draw Floating "+10" Score Text
                            floatingTexts.forEach { ft ->
                                val tx = (ft.x + 1) * cellSize
                                val ty = (ft.y + 1) * cellSize
                                drawContext.canvas.nativeCanvas.drawText(
                                    ft.text,
                                    tx,
                                    ty,
                                    Paint().apply {
                                        color = android.graphics.Color.argb((ft.alpha * 255).toInt(), 0, 229, 255) // Neon Cyan Color(0xFF00E5FF)
                                        textSize = cellSize * 0.58f
                                        textAlign = Paint.Align.CENTER
                                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                                    }
                                )
                            }
                        }

                        // Countdown overlay
                        if (countdownState != -1) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .testTag("countdown_overlay"),
                                contentAlignment = Alignment.Center
                            ) {
                                AnimatedContent(
                                    targetState = countdownState,
                                    transitionSpec = {
                                        scaleIn(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)) togetherWith
                                        scaleOut(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                                    },
                                    label = "countdown"
                                ) { targetCount ->
                                    val text = if (targetCount == 0) "GO!" else "$targetCount"
                                    val color = if (targetCount == 0) Color(0xFF00E5FF) else Color(0xFF7C4DFF)
                                    Text(
                                        text = text,
                                        color = color,
                                        fontSize = 72.sp,
                                        fontWeight = FontWeight.Black,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        // Paused overlay state
                        if (isPaused && !isGameOver && countdownState == -1) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.7f))
                                    .testTag("pause_overlay"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Pause,
                                        contentDescription = "Paused",
                                        tint = Color(0xFF00E5FF),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "GAME PAUSED",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = {
                                            soundManager.playButtonClick()
                                            isPaused = false
                                            soundManager.playResume()
                                            soundManager.resumeBgm()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                                        modifier = Modifier.testTag("resume_button")
                                    ) {
                                        Text("Resume Game", color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                    // Second Chance! Banner Overlay Animation
                    if (showSecondChanceText) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(2.dp, Color(0xFF00E5FF)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF19122A).copy(alpha = 0.95f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF7C4DFF).copy(alpha = 0.35f), Color(0xFF00E5FF).copy(alpha = 0.35f))
                                        )
                                    )
                                    .padding(horizontal = 24.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("🛡️", fontSize = 28.sp)
                                Column {
                                    Text(
                                        text = "⭐ SECOND CHANCE ⭐",
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "Temporary Shield Active for 3s",
                                        color = Color(0xFF00E5FF),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Fixed Banner Ad (AdMob)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(top = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                com.playwin.ads.BannerManager.BannerAd()
            }
            }
        }
    }

    // Level Complete Dialog
    if (isLevelCompleted) {
        val isBonus = activeLevel?.isBonus == true
        val stars = if (isBonus) 3 else when {
            levelTimeElapsedSeconds <= 35 -> 3
            levelTimeElapsedSeconds <= 60 -> 2
            else -> 1
        }
        val coinsReward = if (isBonus) levelFruitsCollected * 2 else (10 + (stars * 1))

        AlertDialog(
            onDismissRequest = {},
            containerColor = Color(0xFF13111C),
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 8.dp,
            title = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isBonus) "🎁 BONUS STAGE FINISHED" else "🎉 LEVEL $currentLevelNumber COMPLETED!",
                        color = Color(0xFF00E5FF),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isBonus) "Phenomenal fruit collection!" else activeLevel?.theme?.let { "Theme: ${it.title}" } ?: "",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Star Rating Animation / Display
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(3) { index ->
                            val active = index < stars
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Star ${index + 1}",
                                tint = if (active) Color(0xFFFFD700) else Color(0xFF2D2B3D),
                                modifier = Modifier
                                    .size(44.dp)
                                    .shadow(if (active) 4.dp else 0.dp, CircleShape)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Performance details card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B182B)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Fruits Collected:", color = Color.Gray, fontSize = 13.sp)
                                Text(
                                    text = if (isBonus) "$levelFruitsCollected Eaten" else "$levelFruitsCollected / ${activeLevel?.targetFruits}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(if (isBonus) "Time Allowed:" else "Time Elapsed:", color = Color.Gray, fontSize = 13.sp)
                                Text(
                                    text = if (isBonus) "30 Seconds ⏱️" else "${levelTimeElapsedSeconds}s ⏱️",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Divider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Coins Earned:", color = Color.Gray, fontSize = 13.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "+$coinsReward",
                                        color = Color(0xFFFFD700),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("🪙", fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Next Level Button (if unlocked level <= 100)
                    if (currentLevelNumber < 100) {
                        Button(
                            onClick = {
                                soundManager.playButtonClick()
                                currentLevelNumber += 1
                                resetGame(currentLevelNumber)
                                isLevelCompleted = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF7C4DFF)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("next_level_button")
                        ) {
                            Text(
                                text = "NEXT LEVEL ➡️",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF00E5FF).copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🏆 CONGRATS! YOU BEAT ALL 100 LEVELS!",
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                soundManager.playButtonClick()
                                resetGame(currentLevelNumber)
                                isLevelCompleted = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B182B)),
                            border = BorderStroke(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("replay_level_button")
                        ) {
                            Text("Replay", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                soundManager.playButtonClick()
                                isLevelCompleted = false
                                isPaused = true
                                currentScreenState = "LOBBY"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B182B)),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("level_select_button")
                        ) {
                            Text("Levels Select", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        )
    }

    // Game Over dialog with rewards
    if (isGameOver) {
        val coinsEarned = (score / 10 * 1).coerceAtMost(25)

        AlertDialog(
            onDismissRequest = {
                exitGameToHome()
            },
            containerColor = Color(0xFF13111C),
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp,
            title = {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🎮 GAME OVER",
                        color = Color(0xFFFF1744),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    // Circular Close (X) Button matching PlayWin dark purple premium theme
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(32.dp)
                            .background(Color(0xFF1E1B2C), CircleShape)
                            .border(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.4f), CircleShape)
                            .clip(CircleShape)
                            .clickable {
                                exitGameToHome()
                            }
                            .testTag("game_over_close_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Game Over",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Splendid attempt! Snake collided.",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("YOUR SCORE", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("$score", color = Color(0xFF00E5FF), fontSize = 24.sp, fontWeight = FontWeight.Black)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("BEST SCORE", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("$highScore", color = Color(0xFFFFD700), fontSize = 24.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Coins reward banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFF7C4DFF).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (coinsEarned > 0) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "+$coinsEarned Coins",
                                        color = Color(0xFFFFD700),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("🪙", fontSize = 18.sp)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Claim your reward instantly!",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                            } else {
                                Text(
                                    text = "No Coins Earned",
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Score 10+ points to earn coins next time!",
                                    color = Color.Gray,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (coinsEarned > 0) {
                        Button(
                            onClick = {
                                if (!rewardClaimed) {
                                    soundManager.playCoinReward()
                                    viewModel.addCoins(coinsEarned, "Snake Classic Game")
                                    rewardClaimed = true
                                    android.widget.Toast.makeText(
                                        context,
                                        "Claimed $coinsEarned PlayWin Coins successfully!",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            enabled = !rewardClaimed,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFD700),
                                disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("claim_reward_button")
                        ) {
                            Text(
                                text = if (rewardClaimed) "REWARD CLAIMED ✓" else "CLAIM REWARD 🪙",
                                color = if (rewardClaimed) Color.White.copy(alpha = 0.6f) else Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Rewarded Ad Extra Life Button (Allowed only once per game)
                    if (!hasUsedContinueThisGame) {
                        Button(
                            onClick = {
                                val activity = context as? android.app.Activity
                                if (activity != null && com.playwin.ads.RewardedManager.isAdReady(activity)) {
                                    soundManager.stopBgm()
                                    com.playwin.ads.RewardedManager.showAd(
                                        activity = activity,
                                        rewardType = com.playwin.ads.RewardType.QUIZ_LIFELINE,
                                        callbacks = object : com.playwin.ads.RewardCallback {
                                            override fun onRewardEarned(rewardType: com.playwin.ads.RewardType, amount: Int, token: String) {
                                                // Reward earned
                                            }

                                            override fun onAdFailedToLoad(errorCode: Int, errorMessage: String) {
                                                soundManager.startBgm()
                                                android.widget.Toast.makeText(
                                                    context,
                                                    "Rewarded ad is currently unavailable. Please try again.",
                                                    android.widget.Toast.LENGTH_SHORT
                                                ).show()
                                            }

                                            override fun onAdFailedToShow(errorMessage: String) {
                                                soundManager.startBgm()
                                                android.widget.Toast.makeText(
                                                    context,
                                                    "Rewarded ad is currently unavailable. Please try again.",
                                                    android.widget.Toast.LENGTH_SHORT
                                                ).show()
                                            }

                                            override fun onAdClosed(userEarnedReward: Boolean) {
                                                if (userEarnedReward) {
                                                    // Restore game state from snapshot
                                                    val snapshot = lastDeathSnapshot
                                                    if (snapshot != null) {
                                                        snake = snapshot.snake
                                                        previousSnake = snapshot.previousSnake
                                                        direction = snapshot.direction
                                                        previousDirection = snapshot.previousDirection
                                                        food = snapshot.food
                                                        score = snapshot.score
                                                        levelFruitsCollected = snapshot.levelFruitsCollected
                                                        fruitsCollectedThisGame = snapshot.fruitsCollectedThisGame
                                                        currentLevelNumber = snapshot.currentLevelNumber
                                                        isAdventureMode = snapshot.isAdventureMode
                                                        activeLevel = snapshot.activeLevel
                                                        currentBreakableWalls.clear()
                                                        currentBreakableWalls.addAll(snapshot.breakableWalls)
                                                        crystalPoint = snapshot.crystalPoint
                                                        bossPosition = snapshot.bossPosition
                                                        bossDirection = snapshot.bossDirection
                                                        bossTickCounter = snapshot.bossTickCounter
                                                        activeTraps.clear()
                                                        activeTraps.addAll(snapshot.activeTraps)
                                                        bonusTimeLeftSeconds = snapshot.bonusTimeLeftSeconds
                                                        levelTimeElapsedSeconds = snapshot.levelTimeElapsedSeconds

                                                        isDoubleCoinsActive = snapshot.isDoubleCoinsActive
                                                        doubleCoinsTimeLeft = snapshot.doubleCoinsTimeLeft
                                                        magnetTimeLeft = snapshot.magnetTimeLeft
                                                        slowMotionTimeLeft = snapshot.slowMotionTimeLeft
                                                        fruitFrenzyTimeLeft = snapshot.fruitFrenzyTimeLeft
                                                        extraFruits.clear()
                                                        extraFruits.addAll(snapshot.extraFruits)
                                                        activePowerUpOnBoard = snapshot.activePowerUpOnBoard
                                                    }

                                                    isDeadAnimating = false
                                                    isDeadEyeClosed = false

                                                    coroutineScope.launch {
                                                        snakeDeathAlpha.snapTo(1f)
                                                        snakeDeathShake.snapTo(0f)
                                                        moveProgress.snapTo(1f)
                                                    }

                                                    hasShield = true
                                                    shieldTimeLeft = 3
                                                    isAdShieldActive = true

                                                    hasUsedContinueThisGame = true
                                                    isGameOver = false
                                                    isPaused = false
                                                    countdownState = -1

                                                    soundManager.startBgm()

                                                    coroutineScope.launch {
                                                        showSecondChanceText = true
                                                        delay(3000)
                                                        showSecondChanceText = false
                                                        isAdShieldActive = false
                                                    }
                                                } else {
                                                    soundManager.startBgm()
                                                    android.widget.Toast.makeText(
                                                        context,
                                                        "Watch full ad to continue. Try again!",
                                                        android.widget.Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        }
                                    )
                                } else {
                                    com.playwin.ads.RewardedManager.preload(context)
                                    android.widget.Toast.makeText(
                                        context,
                                        "Rewarded ad is currently unavailable. Please try again.",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF7C4DFF), Color(0xFF9D4EDD))
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .testTag("watch_ad_continue_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text("🛡️ ", fontSize = 16.sp)
                                Text(
                                    text = "Watch Ad & Continue",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                soundManager.playButtonClick()
                                hapticManager.vibrateLight()
                                if (isAdventureMode) {
                                    resetGame(currentLevelNumber)
                                } else {
                                    resetGame()
                                }
                                isGameOver = false
                                currentScreenState = "GAMEPLAY"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("play_again_button")
                        ) {
                            Text("Play Again", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                exitGameToHome()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1B2C)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("home_button")
                        ) {
                            Text("Home", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        )
    }

    // Mystery Box Reward Overlay Dialog
    if (showMysteryBoxOverlay) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = Color(0xFF13111C),
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 8.dp,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎁 MYSTERY REWARD",
                        color = Color(0xFF00E5FF),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "You collected 10 Fruits! Open the box to reveal your secret bonus prize.",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (!isMysteryBoxOpened) {
                        // closed box animation
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(Color(0xFF1E1B2C), CircleShape)
                                .clickable {
                                    // Open Box
                                    isMysteryBoxOpened = true
                                    soundManager.playCoinReward()
                                    hapticManager.vibrateSuccess()
                                    
                                    val rewards = listOf(
                                        "COINS_15" to "15 Coins!",
                                        "COINS_30" to "30 Coins!",
                                        "SHIELD" to "Active Collision Shield!",
                                        "DOUBLE" to "Double Coins Bonus!"
                                    )
                                    val chosen = rewards.random()
                                    mysteryBoxRewardText = chosen.second
                                    
                                    when (chosen.first) {
                                        "COINS_15" -> {
                                            viewModel.addCoins(5, "Snake Mystery Box")
                                            val oldStats = SnakeProgressionManager.loadStats(context)
                                            SnakeProgressionManager.saveStats(context, oldStats.copy(totalCoinsEarned = oldStats.totalCoinsEarned + 5))
                                            stats = SnakeProgressionManager.loadStats(context)
                                        }
                                        "COINS_30" -> {
                                            viewModel.addCoins(10, "Snake Mystery Box")
                                            val oldStats = SnakeProgressionManager.loadStats(context)
                                            SnakeProgressionManager.saveStats(context, oldStats.copy(totalCoinsEarned = oldStats.totalCoinsEarned + 10))
                                            stats = SnakeProgressionManager.loadStats(context)
                                        }
                                        "SHIELD" -> {
                                            hasShield = true
                                        }
                                        "DOUBLE" -> {
                                            isDoubleCoinsActive = true
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎁", fontSize = 52.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "TAP TO OPEN",
                            color = Color(0xFF7C4DFF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp
                        )
                    } else {
                        // opened box reveal
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(Color(0xFF1E1B2C), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✨", fontSize = 52.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "YOU REVEALED:",
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = mysteryBoxRewardText,
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            confirmButton = {
                if (isMysteryBoxOpened) {
                    Button(
                        onClick = {
                            soundManager.playButtonClick()
                            showMysteryBoxOverlay = false
                            isPaused = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("RESUME GAME 🎮", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }

    // Real-Time Notification Floating Banner
    activeCompletionNotification?.let { msg ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF13111C)),
                border = BorderStroke(1.2.dp, Color(0xFF00E5FF)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = msg,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            activeCompletionNotification = null
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Text("✕", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        }
        
        // Auto-dismiss after 3.5 seconds
        LaunchedEffect(msg) {
            delay(3500)
            if (activeCompletionNotification == msg) {
                activeCompletionNotification = null
            }
        }
    }

    if (showLevelUpOverlay) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .pointerInput(Unit) {}, // Consume all input/clicks
            contentAlignment = Alignment.Center
        ) {
            val scaleAnim = remember { Animatable(0.8f) }
            LaunchedEffect(Unit) {
                scaleAnim.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
            
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .scale(scaleAnim.value)
                    .shadow(24.dp, RoundedCornerShape(24.dp), ambientColor = Color(0xFF7C4DFF), spotColor = Color(0xFF00E5FF)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0A1B)),
                border = BorderStroke(2.dp, Brush.horizontalGradient(listOf(Color(0xFF7C4DFF), Color(0xFF00E5FF)))),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "⭐ LEVEL UP ⭐",
                        color = Color(0xFFFFD700),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFF7C4DFF).copy(alpha = 0.3f), Color.Transparent)
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⚡",
                            fontSize = 40.sp,
                            modifier = Modifier.scale(scaleAnim.value)
                        )
                    }
                    
                    Text(
                        text = "Level $classicLevel",
                        color = Color(0xFF00E5FF),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                    
                    Text(
                        text = "Speed increased to x${"%.2f".format(speedMultiplier)}! 🚀",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = {
                            soundManager.playButtonClick()
                            showLevelUpOverlay = false
                            countdownState = 3
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7C4DFF)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = "CONTINUE",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

// History Serializer / Deserializer using simple delimited string for extreme crash-proofness
private fun parseHistory(historyStr: String): List<SnakeGameHistoryEntry> {
    if (historyStr.isEmpty()) return emptyList()
    val list = mutableListOf<SnakeGameHistoryEntry>()
    try {
        val entries = historyStr.split(";")
        for (entry in entries) {
            if (entry.isEmpty()) continue
            val parts = entry.split("|")
            if (parts.size == 3) {
                list.add(
                    SnakeGameHistoryEntry(
                        date = parts[0],
                        score = parts[1].toIntOrNull() ?: 0,
                        coins = parts[2].toIntOrNull() ?: 0
                    )
                )
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return list
}

private fun serializeHistory(history: List<SnakeGameHistoryEntry>): String {
    val sb = StringBuilder()
    for (i in history.indices) {
        val entry = history[i]
        sb.append(entry.date).append("|").append(entry.score).append("|").append(entry.coins)
        if (i < history.size - 1) {
            sb.append(";")
        }
    }
    return sb.toString()
}
