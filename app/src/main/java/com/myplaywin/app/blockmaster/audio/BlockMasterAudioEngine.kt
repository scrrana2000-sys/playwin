package com.myplaywin.app.blockmaster.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.media.ToneGenerator
import android.util.Log

class BlockMasterAudioEngine(private val context: Context) {

    private var soundPool: SoundPool? = null
    private var toneGenerator: ToneGenerator? = null

    private var isSfxEnabled = true
    private var isMusicEnabled = true
    private var sfxVolume = 1.0f

    init {
        initAudioEngine()
    }

    private fun initAudioEngine() {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(8)
                .setAudioAttributes(audioAttributes)
                .build()

            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
        } catch (e: Exception) {
            Log.e("BlockMasterAudioEngine", "Error initializing audio engine: ${e.message}")
        }
    }

    fun setSfxEnabled(enabled: Boolean) {
        isSfxEnabled = enabled
    }

    fun setMusicEnabled(enabled: Boolean) {
        isMusicEnabled = enabled
    }

    fun updateVolumes(musicVol: Float, sfxVol: Float) {
        sfxVolume = sfxVol
    }

    fun playClickSound() {
        if (!isSfxEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 40)
        } catch (e: Exception) {
            Log.e("BlockMasterAudioEngine", "Error playing click sound: ${e.message}")
        }
    }

    fun playLineClearSound(lines: Int) {
        if (!isSfxEnabled) return
        try {
            val toneType = when (lines) {
                1 -> ToneGenerator.TONE_PROP_BEEP
                2 -> ToneGenerator.TONE_PROP_BEEP2
                3 -> ToneGenerator.TONE_PROP_ACK
                4 -> ToneGenerator.TONE_CDMA_HIGH_L
                else -> ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD
            }
            val duration = if (lines >= 4) 250 else 100
            toneGenerator?.startTone(toneType, duration)
        } catch (e: Exception) {
            Log.e("BlockMasterAudioEngine", "Error playing line clear sound: ${e.message}")
        }
    }

    fun playComboSound(combo: Int) {
        if (!isSfxEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_DTMF_A, 120)
        } catch (e: Exception) {
            Log.e("BlockMasterAudioEngine", "Error playing combo sound: ${e.message}")
        }
    }

    fun playPerfectClearSound() {
        if (!isSfxEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 350)
        } catch (e: Exception) {
            Log.e("BlockMasterAudioEngine", "Error playing perfect clear sound: ${e.message}")
        }
    }

    fun playLevelUpSound() {
        if (!isSfxEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 300)
        } catch (e: Exception) {
            Log.e("BlockMasterAudioEngine", "Error playing level up sound: ${e.message}")
        }
    }

    fun playCoinSound() {
        if (!isSfxEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 80)
        } catch (e: Exception) {
            Log.e("BlockMasterAudioEngine", "Error playing coin sound: ${e.message}")
        }
    }

    fun playExplosionSound() {
        if (!isSfxEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_SUP_ERROR, 250)
        } catch (e: Exception) {
            Log.e("BlockMasterAudioEngine", "Error playing explosion sound: ${e.message}")
        }
    }

    fun playIceCrackSound() {
        if (!isSfxEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_SUP_CONGESTION, 100)
        } catch (e: Exception) {
            Log.e("BlockMasterAudioEngine", "Error playing ice crack sound: ${e.message}")
        }
    }

    fun playElectricSound() {
        if (!isSfxEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200)
        } catch (e: Exception) {
            Log.e("BlockMasterAudioEngine", "Error playing electric sound: ${e.message}")
        }
    }

    fun playSteelSound() {
        if (!isSfxEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
        } catch (e: Exception) {
            Log.e("BlockMasterAudioEngine", "Error playing steel sound: ${e.message}")
        }
    }

    fun playPowerUpSound() {
        if (!isSfxEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_DTMF_C, 250)
        } catch (e: Exception) {
            Log.e("BlockMasterAudioEngine", "Error playing power-up sound: ${e.message}")
        }
    }

    fun playGameOverSound() {
        if (!isSfxEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_SUP_ERROR, 400)
        } catch (e: Exception) {
            Log.e("BlockMasterAudioEngine", "Error playing game over sound: ${e.message}")
        }
    }

    fun playWorldTransitionSound(worldId: Int) {
        if (!isSfxEnabled) return
        try {
            val toneType = when (worldId) {
                1 -> ToneGenerator.TONE_PROP_BEEP2
                2 -> ToneGenerator.TONE_PROP_ACK
                3 -> ToneGenerator.TONE_CDMA_HIGH_L
                4 -> ToneGenerator.TONE_SUP_ERROR
                5 -> ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD
                6 -> ToneGenerator.TONE_DTMF_C
                else -> ToneGenerator.TONE_DTMF_A
            }
            toneGenerator?.startTone(toneType, 300)
        } catch (e: Exception) {
            Log.e("BlockMasterAudioEngine", "Error playing world transition sound: ${e.message}")
        }
    }

    fun pauseAll() {
        soundPool?.autoPause()
    }

    fun resumeAll() {
        soundPool?.autoResume()
    }

    fun release() {
        try {
            soundPool?.release()
            soundPool = null
            toneGenerator?.release()
            toneGenerator = null
        } catch (e: Exception) {
            Log.e("BlockMasterAudioEngine", "Error releasing sound engine: ${e.message}")
        }
    }
}
