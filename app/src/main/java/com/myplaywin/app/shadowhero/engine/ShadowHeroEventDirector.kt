package com.myplaywin.app.shadowhero.engine

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import com.myplaywin.app.shadowhero.audio.ShadowHeroAudioEngine
import java.util.Random

enum class WorldEventType(
    val eventName: String,
    val telegraphTitle: String,
    val durationSeconds: Float,
    val icon: String,
    val primaryColor: Color
) {
    NONE("Normal Operations", "", 0f, "", Color.Unspecified),
    SHADOW_STORM("Shadow Storm", "⚡ SHADOW STORM APPROACHING!", 12f, "⚡", Color(0xFFA855F7)),
    CRYSTAL_RAIN("Crystal Rain", "🌧️ CRYSTAL RAIN DETECTED!", 14f, "💎", Color(0xFF38BDF8)),
    DARKNESS("Darkness Fall", "🌑 DARKNESS COVER FALLING!", 10f, "🌑", Color(0xFF818CF8)),
    ENERGY_SURGE("Energy Surge", "⚡ ENERGY SURGE OVERLOAD!", 12f, "⚡", Color(0xFFC084FC)),
    METEOR_FALL("Meteor Shower", "☄ METEOR BOMBARDMENT INCOMING!", 11f, "☄", Color(0xFFEF4444)),
    ENEMY_HUNT("Enemy Hunt", "🐺 ENEMY HUNT SWARM DETECTED!", 12f, "🐺", Color(0xFFF59E0B)),
    LOW_GRAVITY("Low Gravity", "🌌 LOW GRAVITY FIELD ACTIVE!", 14f, "🌌", Color(0xFF34D399)),
    SPEED_SURGE("Speed Surge", "⏩ SPEED SURGE INITIATED!", 12f, "⏩", Color(0xFF06B6D4)),
    VOID_DISTORTION("Void Distortion", "🌀 VOID DISTORTION UNLEASHED!", 10f, "🌀", Color(0xFFFB7185))
}

data class MeteorTarget(
    val id: String,
    val targetX: Float,
    val targetY: Float,
    var telegraphTimer: Float = 1.3f, // Time before strike
    val totalTelegraphTime: Float = 1.3f,
    var isImpacted: Boolean = false,
    var impactDurationTimer: Float = 0.6f
) {
    val impactBounds: Rect
        get() = Rect(targetX - 32f, targetY - 20f, targetX + 32f, targetY + 10f)
}

data class FallingCrystal(
    val id: String,
    var x: Float,
    var y: Float,
    val speedY: Float = 180f,
    val groundY: Float,
    var isCollected: Boolean = false,
    var lifeTimer: Float = 8f
) {
    val bounds: Rect
        get() = Rect(x - 12f, y - 12f, x + 12f, y + 12f)
}

/**
 * Event Director (Phase 11B Section 16)
 * Manages procedural, rare dynamic world events, telegraph banners,
 * active physics adjustments, and safe cleanup.
 */
class ShadowHeroEventDirector(
    val seed: Long = 133777L
) {
    var activeEvent: WorldEventType = WorldEventType.NONE
        private set

    var eventTimer: Float = 0f
        private set

    var telegraphBannerTitle: String = ""
        private set

    var telegraphTimer: Float = 0f
        private set

    var cooldownTimer: Float = 25f // Cooldown before next potential event

    val activeMeteors = mutableListOf<MeteorTarget>()
    val fallingCrystals = mutableListOf<FallingCrystal>()

    private var meteorSpawnTimer: Float = 0f
    private var crystalRainSpawnTimer: Float = 0f
    private val random = Random(seed)

    /**
     * Ticks the Event Director every engine frame.
     */
    fun update(
        dt: Float,
        playerX: Float,
        playerY: Float,
        stageNumber: Int,
        difficultyDirector: ShadowHeroDifficultyDirector,
        activePlatforms: List<LevelPlatform>
    ) {
        val safeDt = dt.coerceIn(0.001f, 0.1f)

        // 1. Tick Telegraph Phase
        if (telegraphTimer > 0f) {
            telegraphTimer -= safeDt
            if (telegraphTimer <= 0f) {
                // Telegraph finished, start active event!
                telegraphBannerTitle = ""
            }
        }

        // 2. Tick Active Event
        if (activeEvent != WorldEventType.NONE && telegraphTimer <= 0f) {
            eventTimer -= safeDt
            if (eventTimer <= 0f) {
                endActiveEvent()
            } else {
                updateActiveEventLogic(safeDt, playerX, playerY, activePlatforms)
            }
        } else if (activeEvent == WorldEventType.NONE) {
            // 3. Tick Cooldown & Roll for New Event
            cooldownTimer -= safeDt
            if (cooldownTimer <= 0f) {
                tryTriggerRandomEvent(stageNumber, difficultyDirector, playerX)
            }
        }

        // 4. Update Meteor Targets & Impact Timers
        val mIterator = activeMeteors.iterator()
        while (mIterator.hasNext()) {
            val m = mIterator.next()
            if (!m.isImpacted) {
                m.telegraphTimer -= safeDt
                if (m.telegraphTimer <= 0f) {
                    m.isImpacted = true
                    ShadowHeroAudioEngine.playShieldBreak()
                }
            } else {
                m.impactDurationTimer -= safeDt
                if (m.impactDurationTimer <= 0f) {
                    mIterator.remove()
                }
            }
        }

        // 5. Update Falling Crystals
        val cIterator = fallingCrystals.iterator()
        while (cIterator.hasNext()) {
            val fc = cIterator.next()
            if (!fc.isCollected) {
                if (fc.y < fc.groundY) {
                    fc.y += fc.speedY * safeDt
                }
                fc.lifeTimer -= safeDt
                if (fc.lifeTimer <= 0f) {
                    cIterator.remove()
                }
            } else {
                cIterator.remove()
            }
        }
    }

    private fun updateActiveEventLogic(
        safeDt: Float,
        playerX: Float,
        playerY: Float,
        activePlatforms: List<LevelPlatform>
    ) {
        when (activeEvent) {
            WorldEventType.METEOR_FALL -> {
                meteorSpawnTimer -= safeDt
                if (meteorSpawnTimer <= 0f) {
                    meteorSpawnTimer = 1.8f
                    // Spawn telegraphed meteor target ahead or near player (NEVER directly centered on player)
                    val offset = if (random.nextBoolean()) (120f + random.nextFloat() * 180f) else (-140f - random.nextFloat() * 120f)
                    val targetX = playerX + offset
                    // Find nearest ground platform below targetX
                    val plat = activePlatforms.filter { it.bounds.left <= targetX + 40f && it.bounds.right >= targetX - 40f }.minByOrNull { Math.abs(it.bounds.top - playerY) }
                    val targetY = plat?.bounds?.top ?: (playerY + 80f)

                    activeMeteors.add(
                        MeteorTarget(
                            id = "meteor_${System.currentTimeMillis()}_${random.nextInt(1000)}",
                            targetX = targetX,
                            targetY = targetY
                        )
                    )
                }
            }
            WorldEventType.CRYSTAL_RAIN -> {
                crystalRainSpawnTimer -= safeDt
                if (crystalRainSpawnTimer <= 0f) {
                    crystalRainSpawnTimer = 0.6f
                    val dropX = playerX + (random.nextFloat() * 600f - 300f)
                    val dropYStart = playerY - 350f
                    val plat = activePlatforms.filter { it.bounds.left <= dropX && it.bounds.right >= dropX }.minByOrNull { Math.abs(it.bounds.top - playerY) }
                    val groundY = plat?.bounds?.top?.minus(12f) ?: (playerY + 120f)

                    fallingCrystals.add(
                        FallingCrystal(
                            id = "fc_${System.currentTimeMillis()}_${random.nextInt(1000)}",
                            x = dropX,
                            y = dropYStart,
                            groundY = groundY
                        )
                    )
                }
            }
            else -> {}
        }
    }

    private fun tryTriggerRandomEvent(stageNumber: Int, difficultyDirector: ShadowHeroDifficultyDirector, playerX: Float) {
        // Roll probability: 35% chance to trigger event when cooldown expires
        if (random.nextFloat() > 0.35f) {
            cooldownTimer = 15f
            return
        }

        // Candidate pool based on stage progression
        val candidateEvents = mutableListOf<WorldEventType>()
        // Early Stages (1-3): Gentle events
        candidateEvents.add(WorldEventType.CRYSTAL_RAIN)
        candidateEvents.add(WorldEventType.ENERGY_SURGE)

        if (stageNumber >= 2) {
            candidateEvents.add(WorldEventType.SHADOW_STORM)
            candidateEvents.add(WorldEventType.LOW_GRAVITY)
            candidateEvents.add(WorldEventType.SPEED_SURGE)
        }
        if (stageNumber >= 3) {
            candidateEvents.add(WorldEventType.ENEMY_HUNT)
            candidateEvents.add(WorldEventType.DARKNESS)
        }
        if (stageNumber >= 4) {
            candidateEvents.add(WorldEventType.METEOR_FALL)
            candidateEvents.add(WorldEventType.VOID_DISTORTION)
        }

        val chosen = candidateEvents[random.nextInt(candidateEvents.size)]
        triggerEvent(chosen)
    }

    fun triggerEvent(event: WorldEventType) {
        if (event == WorldEventType.NONE) return

        activeEvent = event
        eventTimer = event.durationSeconds
        telegraphBannerTitle = event.telegraphTitle
        telegraphTimer = 2.5f // 2.5 seconds telegraph banner
        cooldownTimer = event.durationSeconds + 30f // Cooldown after event completes

        ShadowHeroAudioEngine.playPowerUpCollect()
    }

    fun endActiveEvent() {
        activeEvent = WorldEventType.NONE
        eventTimer = 0f
        telegraphBannerTitle = ""
        telegraphTimer = 0f
        activeMeteors.clear()
        fallingCrystals.clear()
    }

    // Helper getters for active gameplay modifiers
    val isLowGravityActive: Boolean
        get() = activeEvent == WorldEventType.LOW_GRAVITY && telegraphTimer <= 0f

    val isSpeedSurgeActive: Boolean
        get() = activeEvent == WorldEventType.SPEED_SURGE && telegraphTimer <= 0f

    val isEnergySurgeActive: Boolean
        get() = activeEvent == WorldEventType.ENERGY_SURGE && telegraphTimer <= 0f

    val isDarknessActive: Boolean
        get() = activeEvent == WorldEventType.DARKNESS && telegraphTimer <= 0f

    val isShadowStormActive: Boolean
        get() = activeEvent == WorldEventType.SHADOW_STORM && telegraphTimer <= 0f

    val isVoidDistortionActive: Boolean
        get() = activeEvent == WorldEventType.VOID_DISTORTION && telegraphTimer <= 0f

    val isEnemyHuntActive: Boolean
        get() = activeEvent == WorldEventType.ENEMY_HUNT && telegraphTimer <= 0f
}
