package com.myplaywin.app.shadowhero.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.myplaywin.app.shadowhero.data.ShadowHeroProgressionManager
import com.myplaywin.app.shadowhero.data.ShadowHeroSettings
import com.myplaywin.app.shadowhero.engine.LevelTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

/**
 * Phase 7 Complete Audio System for Shadow Hero
 * Centralized, high-performance procedural audio manager for background music, SFX,
 * ambient themes, UI sounds, and haptic feedback.
 */
object ShadowHeroAudioEngine {
    private val scope = CoroutineScope(Dispatchers.Default)

    // Audio Settings
    var musicEnabled: Boolean = true
    var soundEnabled: Boolean = true
    var hapticEnabled: Boolean = true
    var musicVolume: Float = 0.8f
    var sfxVolume: Float = 0.8f

    // Vibrator Instance
    private var vibrator: Vibrator? = null

    // Background Music Loop State
    private var currentTheme: LevelTheme? = null
    private var musicJob: Job? = null
    private var isPlayingMusic: Boolean = false
    private var musicIntensity: Float = 1.0f // 1.0 = normal, 1.3 = hazard/enemy encounter, 0.3 = death fade

    // Consecutive Crystal Pitch Counter
    private var consecutiveCrystals: Int = 0
    private var lastCrystalTimeMs: Long = 0L

    fun init(context: Context) {
        val settings = ShadowHeroProgressionManager.getSettings(context)
        musicEnabled = settings.musicEnabled
        soundEnabled = settings.soundEnabled
        hapticEnabled = settings.vibrationEnabled

        initVibrator(context)
    }

    private fun initVibrator(context: Context) {
        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateSettings(context: Context, newSettings: ShadowHeroSettings) {
        musicEnabled = newSettings.musicEnabled
        soundEnabled = newSettings.soundEnabled
        hapticEnabled = newSettings.vibrationEnabled
        ShadowHeroProgressionManager.saveSettings(context, newSettings)

        if (!musicEnabled) {
            stopBackgroundMusic()
        } else if (currentTheme != null && !isPlayingMusic) {
            startBackgroundMusic(currentTheme!!)
        }
    }

    fun triggerHaptic(durationMs: Long = 30L) {
        if (!hapticEnabled) return
        try {
            vibrator?.let { v ->
                if (v.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        v.vibrate(durationMs)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- BACKGROUND MUSIC & ADAPTIVE MOODS ---

    fun startBackgroundMusic(theme: LevelTheme) {
        currentTheme = theme
        if (!musicEnabled) return

        musicJob?.cancel()
        isPlayingMusic = true
        musicIntensity = 1.0f

        musicJob = scope.launch {
            val sampleRate = 22050
            val freqs = getThemeMelodyNotes(theme)

            var noteIdx = 0
            while (isActive && isPlayingMusic && musicEnabled) {
                val noteFreq = freqs[noteIdx % freqs.size].toFloat()
                val durationMs = (180 / musicIntensity).toInt().coerceAtLeast(100)

                playSynthToneInternal(
                    startFreq = noteFreq,
                    endFreq = noteFreq * 1.01f,
                    durationMs = durationMs,
                    volume = 0.22f * musicVolume,
                    waveformType = when (theme) {
                        LevelTheme.CYBER_FACTORY -> 1 // Sawtooth
                        LevelTheme.LAVA_CORE -> 2 // Triangle/Square
                        else -> 0 // Sine
                    }
                )

                noteIdx++
                delay(durationMs.toLong() + 30L)
            }
        }
    }

    fun setMusicIntensity(intensity: Float) {
        musicIntensity = intensity.coerceIn(0.5f, 1.6f)
    }

    fun stopBackgroundMusic() {
        isPlayingMusic = false
        musicJob?.cancel()
        musicJob = null
    }

    private fun getThemeMelodyNotes(theme: LevelTheme): IntArray {
        return when (theme) {
            LevelTheme.NEON_CAVES -> intArrayOf(164, 196, 220, 246, 196, 164) // E minor ambient pulse
            LevelTheme.CYBER_FACTORY -> intArrayOf(220, 261, 329, 392, 440, 329, 261, 220) // Fast cyber A minor
            LevelTheme.FROZEN_TEMPLE -> intArrayOf(523, 659, 783, 1046, 783, 659) // Cold crystalline high sine
            LevelTheme.LAVA_CORE -> intArrayOf(110, 130, 146, 164, 130, 110) // Low magma minor bass
            LevelTheme.SKY_RUINS -> intArrayOf(196, 246, 293, 392, 493, 392) // Mystical G major pad
            LevelTheme.SHADOW_CASTLE -> intArrayOf(130, 155, 196, 233, 196, 155) // Gothic C minor pulse
            LevelTheme.VOID_DIMENSION -> intArrayOf(146, 155, 185, 220, 185, 155) // Deep Phrygian space ambient
        }
    }

    // --- PLAYER MOVEMENT SFX ---

    fun playFootstep() {
        if (!soundEnabled) return
        scope.launch {
            val freq = 120f + (Math.random().toFloat() * 30f)
            playSynthToneInternal(startFreq = freq, endFreq = 60f, durationMs = 35, volume = 0.15f * sfxVolume)
        }
    }

    fun playJump() {
        if (!soundEnabled) return
        triggerHaptic(20)
        scope.launch {
            playSynthToneInternal(startFreq = 280f, endFreq = 680f, durationMs = 90, volume = 0.35f * sfxVolume)
        }
    }

    fun playDoubleJump() {
        if (!soundEnabled) return
        triggerHaptic(25)
        scope.launch {
            playChimeSequenceInternal(intArrayOf(523, 880, 1174), stepMs = 40, volume = 0.4f * sfxVolume)
        }
    }

    fun playLanding() {
        if (!soundEnabled) return
        triggerHaptic(25)
        scope.launch {
            playNoiseThumpInternal(durationMs = 60, volume = 0.3f * sfxVolume)
        }
    }

    fun playWallSlide() {
        if (!soundEnabled) return
        scope.launch {
            playSynthToneInternal(startFreq = 200f, endFreq = 180f, durationMs = 40, volume = 0.12f * sfxVolume)
        }
    }

    fun playWallJump() {
        if (!soundEnabled) return
        triggerHaptic(30)
        scope.launch {
            playSynthToneInternal(startFreq = 350f, endFreq = 750f, durationMs = 85, volume = 0.35f * sfxVolume)
        }
    }

    fun playDash() {
        if (!soundEnabled) return
        triggerHaptic(40)
        scope.launch {
            playSynthToneInternal(startFreq = 600f, endFreq = 1200f, durationMs = 120, volume = 0.5f * sfxVolume, waveformType = 1)
        }
    }

    // --- COLLECTIBLES & POWER-UPS ---

    fun playCrystalCollect() {
        if (!soundEnabled) return
        triggerHaptic(20)

        val now = System.currentTimeMillis()
        if (now - lastCrystalTimeMs < 1200) {
            consecutiveCrystals = (consecutiveCrystals + 1) % 7
        } else {
            consecutiveCrystals = 0
        }
        lastCrystalTimeMs = now

        val scale = intArrayOf(523, 587, 659, 698, 783, 880, 988, 1046) // C Major scale
        val pitch = scale[consecutiveCrystals % scale.size]

        scope.launch {
            playChimeSequenceInternal(intArrayOf(pitch, pitch + 200), stepMs = 50, volume = 0.45f * sfxVolume)
        }
    }

    fun playPowerUpCollect() {
        if (!soundEnabled) return
        triggerHaptic(45)
        scope.launch {
            playChimeSequenceInternal(intArrayOf(440, 554, 659, 880, 1108), stepMs = 50, volume = 0.55f * sfxVolume)
        }
    }

    fun playShieldBreak() {
        if (!soundEnabled) return
        triggerHaptic(60)
        scope.launch {
            playNoiseThumpInternal(durationMs = 120, volume = 0.6f * sfxVolume)
        }
    }

    // --- CHECKPOINTS & HAZARDS ---

    fun playCheckpointActivate() {
        if (!soundEnabled) return
        triggerHaptic(50)
        scope.launch {
            playChimeSequenceInternal(intArrayOf(392, 493, 587, 783, 988), stepMs = 70, volume = 0.55f * sfxVolume)
        }
    }

    fun playHazardWarning() {
        if (!soundEnabled) return
        scope.launch {
            playSynthToneInternal(startFreq = 880f, endFreq = 880f, durationMs = 40, volume = 0.25f * sfxVolume)
        }
    }

    fun playLaserShoot() {
        if (!soundEnabled) return
        scope.launch {
            playSynthToneInternal(startFreq = 1100f, endFreq = 220f, durationMs = 90, volume = 0.35f * sfxVolume)
        }
    }

    // --- PLAYER DEATH & STAGE COMPLETE ---

    fun playPlayerDeath() {
        if (!soundEnabled) return
        triggerHaptic(80)
        musicIntensity = 0.3f
        scope.launch {
            playNoiseThumpInternal(durationMs = 180, volume = 0.65f * sfxVolume)
            delay(100)
            playChimeSequenceInternal(intArrayOf(330, 293, 261, 220, 164), stepMs = 80, volume = 0.5f * sfxVolume)
        }
    }

    fun playSecondChance() {
        if (!soundEnabled) return
        triggerHaptic(60)
        musicIntensity = 1.0f
        scope.launch {
            playChimeSequenceInternal(intArrayOf(261, 329, 392, 523, 659, 783), stepMs = 60, volume = 0.6f * sfxVolume)
        }
    }

    fun playStageComplete() {
        if (!soundEnabled) return
        triggerHaptic(70)
        scope.launch {
            playChimeSequenceInternal(intArrayOf(523, 659, 783, 1046, 1318, 1568), stepMs = 90, volume = 0.65f * sfxVolume)
        }
    }

    fun playCountdownBeep(count: Int) {
        if (!soundEnabled) return
        triggerHaptic(20)
        scope.launch {
            val freq = if (count == 0) 1046f else 523f
            playSynthToneInternal(startFreq = freq, endFreq = freq, durationMs = 80, volume = 0.45f * sfxVolume)
        }
    }

    fun playCountdownBeep(isGo: Boolean) {
        playCountdownBeep(if (isGo) 0 else 1)
    }

    // --- UI SOUNDS ---

    fun playButtonClick() {
        if (!soundEnabled) return
        triggerHaptic(15)
        scope.launch {
            playSynthToneInternal(startFreq = 600f, endFreq = 800f, durationMs = 35, volume = 0.3f * sfxVolume)
        }
    }

    fun playToggleSwitch() {
        if (!soundEnabled) return
        triggerHaptic(15)
        scope.launch {
            playSynthToneInternal(startFreq = 400f, endFreq = 650f, durationMs = 40, volume = 0.3f * sfxVolume)
        }
    }

    // --- INTERNAL PCM AUDIO GENERATORS ---

    private fun playSynthToneInternal(
        startFreq: Float,
        endFreq: Float,
        durationMs: Int,
        volume: Float,
        waveformType: Int = 0
    ) {
        val sampleRate = 22050
        val numSamples = (sampleRate * durationMs / 1000).coerceAtLeast(1)
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val freq = startFreq + (endFreq - startFreq) * progress
            val t = i.toDouble() / sampleRate
            val angle = 2.0 * Math.PI * freq * t
            val envelope = (1.0 - progress).coerceIn(0.0, 1.0)

            val rawWave = when (waveformType) {
                1 -> (2.0 * (angle / (2.0 * Math.PI) - Math.floor(0.5 + angle / (2.0 * Math.PI)))) // Sawtooth
                2 -> if (sin(angle) >= 0) 0.8 else -0.8 // Square wave
                else -> sin(angle) // Sine wave
            }

            val sample = (rawWave * 18000 * volume * envelope).toInt().coerceIn(-32768, 32767).toShort()
            buffer[i] = sample
        }

        playPcmBuffer(buffer, sampleRate)
    }

    private fun playChimeSequenceInternal(freqs: IntArray, stepMs: Int, volume: Float) {
        val sampleRate = 22050
        val totalSamples = (sampleRate * stepMs * freqs.size / 1000).coerceAtLeast(1)
        val buffer = ShortArray(totalSamples)

        var idx = 0
        for (f in freqs) {
            val samplesPerTone = sampleRate * stepMs / 1000
            for (i in 0 until samplesPerTone) {
                val t = i.toDouble() / sampleRate
                val angle = 2.0 * Math.PI * f * t
                val envelope = 1.0 - (i.toDouble() / samplesPerTone)
                val sample = (sin(angle) * 18000 * volume * envelope).toInt().coerceIn(-32768, 32767).toShort()
                if (idx < buffer.size) {
                    buffer[idx++] = sample
                }
            }
        }

        playPcmBuffer(buffer, sampleRate)
    }

    private fun playNoiseThumpInternal(durationMs: Int, volume: Float) {
        val sampleRate = 22050
        val numSamples = (sampleRate * durationMs / 1000).coerceAtLeast(1)
        val buffer = ShortArray(numSamples)
        val random = java.util.Random()

        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val envelope = (1.0 - progress).coerceIn(0.0, 1.0)
            val noise = (random.nextFloat() * 2f - 1f)
            val sample = (noise * 16000 * volume * envelope).toInt().coerceIn(-32768, 32767).toShort()
            buffer[i] = sample
        }

        playPcmBuffer(buffer, sampleRate)
    }

    private fun playPcmBuffer(buffer: ShortArray, sampleRate: Int) {
        try {
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()

            scope.launch {
                delay((buffer.size * 1000L / sampleRate) + 80)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun pauseAllAudio() {
        stopBackgroundMusic()
    }

    fun resumeAudio() {
        if (musicEnabled && currentTheme != null) {
            startBackgroundMusic(currentTheme!!)
        }
    }

    fun releaseAudio() {
        stopBackgroundMusic()
        vibrator = null
    }
}
