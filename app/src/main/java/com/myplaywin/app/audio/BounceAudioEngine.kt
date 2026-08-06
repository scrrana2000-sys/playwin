package com.myplaywin.app.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.AudioManager
import android.media.ToneGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

object BounceAudioEngine {
    private var toneGenerator: ToneGenerator? = null
    private val scope = CoroutineScope(Dispatchers.Default)
    var isMuted: Boolean = false

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playJump() {
        if (isMuted) return
        scope.launch {
            playSynthTone(startFreq = 300f, endFreq = 650f, durationMs = 100)
        }
    }

    fun playLanding() {
        if (isMuted) return
        scope.launch {
            playSynthTone(startFreq = 150f, endFreq = 80f, durationMs = 80)
        }
    }

    fun playCoin() {
        if (isMuted) return
        scope.launch {
            playChimeSequence(intArrayOf(987, 1318), 70) // B5, E6
        }
    }

    fun playStar() {
        if (isMuted) return
        scope.launch {
            playChimeSequence(intArrayOf(523, 659, 783, 1046), 60) // C5, E5, G5, C6
        }
    }

    fun playCheckpoint() {
        if (isMuted) return
        scope.launch {
            playChimeSequence(intArrayOf(440, 554, 659, 880), 80) // A4, C#5, E5, A5
        }
    }

    fun playEnemyHit() {
        if (isMuted) return
        scope.launch {
            playNoiseThump(durationMs = 150)
        }
    }

    fun playDoorUnlock() {
        if (isMuted) return
        scope.launch {
            playChimeSequence(intArrayOf(600, 800, 1200), 90)
        }
    }

    fun playWaterSplash() {
        if (isMuted) return
        scope.launch {
            playSynthTone(startFreq = 220f, endFreq = 120f, durationMs = 120)
        }
    }

    fun playVictory() {
        if (isMuted) return
        scope.launch {
            playChimeSequence(intArrayOf(523, 659, 783, 1046, 1318, 1568), 100)
        }
    }

    fun playGameOver() {
        if (isMuted) return
        scope.launch {
            playChimeSequence(intArrayOf(400, 350, 300, 220), 120)
        }
    }

    fun playThemeBackgroundMelody(themeId: Int) {
        if (isMuted) return
        scope.launch {
            when (themeId) {
                1 -> playChimeSequence(intArrayOf(261, 329, 392), 180) // Forest: C Major chord (C4, E4, G4)
                2 -> playChimeSequence(intArrayOf(523, 659, 783), 120) // Ice: Glistening high-pitched C5, E5, G5
                3 -> playChimeSequence(intArrayOf(110, 130, 146), 250) // Lava: Low, deep magma rumblings
                4 -> playChimeSequence(intArrayOf(220, 220, 330, 330), 150) // Factory: Rhythmic industrial warning notes
                5 -> playChimeSequence(intArrayOf(196, 246, 293, 392), 160) // Castle: Majestic G Major chord
                6 -> playChimeSequence(intArrayOf(293, 349, 440), 200) // Underwater: Flowing minor chord Dm
                7 -> playChimeSequence(intArrayOf(392, 493, 587, 783), 140) // Sky: Airy G Major 7th
                8 -> playChimeSequence(intArrayOf(261, 349, 392), 180) // Jungle: Energetic notes (C4, F4, G4)
                9 -> playChimeSequence(intArrayOf(293, 311, 440), 220) // Desert: Phrygian mystery notes
                10 -> playChimeSequence(intArrayOf(440, 554, 659, 880), 130) // Crystal Cave: Sparkly A Major
            }
        }
    }

    private fun playChimeSequence(freqs: IntArray, stepMs: Int) {
        val sampleRate = 22050
        val totalSamples = sampleRate * stepMs * freqs.size / 1000
        val buffer = ShortArray(totalSamples)

        var idx = 0
        for (f in freqs) {
            val samplesPerTone = sampleRate * stepMs / 1000
            for (i in 0 until samplesPerTone) {
                val t = i.toDouble() / sampleRate
                val angle = 2.0 * Math.PI * f * t
                val envelope = 1.0 - (i.toDouble() / samplesPerTone)
                val sample = (sin(angle) * 20000 * envelope).toInt().toShort()
                if (idx < buffer.size) {
                    buffer[idx++] = sample
                }
            }
        }

        playPcmBuffer(buffer, sampleRate)
    }

    private fun playSynthTone(startFreq: Float, endFreq: Float, durationMs: Int) {
        val sampleRate = 22050
        val numSamples = sampleRate * durationMs / 1000
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val freq = startFreq + (endFreq - startFreq) * progress
            val t = i.toDouble() / sampleRate
            val angle = 2.0 * Math.PI * freq * t
            val envelope = (1.0 - progress).coerceIn(0.0, 1.0)
            val sample = (sin(angle) * 18000 * envelope).toInt().toShort()
            buffer[i] = sample
        }

        playPcmBuffer(buffer, sampleRate)
    }

    private fun playNoiseThump(durationMs: Int) {
        val sampleRate = 22050
        val numSamples = sampleRate * durationMs / 1000
        val buffer = ShortArray(numSamples)
        val random = java.util.Random()

        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val envelope = (1.0 - progress).coerceIn(0.0, 1.0)
            val noise = (random.nextFloat() * 2f - 1f)
            val sample = (noise * 15000 * envelope).toInt().toShort()
            buffer[i] = sample
        }

        playPcmBuffer(buffer, sampleRate)
    }

    private fun playPcmBuffer(buffer: ShortArray, sampleRate: Int) {
        try {
            val minBufSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
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
                kotlinx.coroutines.delay((buffer.size * 1000L / sampleRate) + 100)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (e: Exception) {}
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
