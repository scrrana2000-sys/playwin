package com.myplaywin.app.blockmaster.procedural

import com.myplaywin.app.blockmaster.engine.SmartDifficultyEngine
import com.myplaywin.app.blockmaster.missions.MissionEngine
import com.myplaywin.app.blockmaster.missions.MissionObjective
import com.myplaywin.app.blockmaster.world.BlockWorld
import com.myplaywin.app.blockmaster.world.WorldProgressionManager

data class GeneratedLevelConfig(
    val level: Int,
    val world: BlockWorld,
    val dropIntervalMs: Long,
    val scoreMultiplier: Float,
    val coinRewardMultiplier: Float,
    val missions: List<MissionObjective>,
    val levelTitle: String,
    val specialRuleDescription: String? = null
)

class ProceduralLevelGenerator(
    private val smartDifficultyEngine: SmartDifficultyEngine = SmartDifficultyEngine()
) {
    private val missionEngine = MissionEngine()

    fun generateLevel(level: Int): GeneratedLevelConfig {
        val lvl = maxOf(1, level)
        val world = WorldProgressionManager.getWorldForLevel(lvl)

        val diffMult = smartDifficultyEngine.getDifficultyMultiplier()
        val dropInterval = smartDifficultyEngine.calculateGravityDropInterval(lvl)
        val scoreMult = smartDifficultyEngine.calculateScoreMultiplier(lvl)
        val coinMult = smartDifficultyEngine.calculateCoinRewardMultiplier(lvl)

        missionEngine.generateMissionsForLevel(lvl, diffMult)
        val missions = missionEngine.activeMissions

        val levelTitle = "Level $lvl • ${world.name}"

        val specialRule = when {
            lvl % 10 == 0 -> "🌟 MILESTONE LEVEL: +50% Coin & XP Bonus!"
            lvl % 5 == 0 -> "⚡ SPEED CHALLENGE: Rapid Drop Speed!"
            diffMult > 1.1f -> "🔥 HIGH STAKES: Score Boost active!"
            diffMult < 0.9f -> "🛡️ ASSIST MODE: Relaxed drop speed"
            else -> null
        }

        return GeneratedLevelConfig(
            level = lvl,
            world = world,
            dropIntervalMs = dropInterval,
            scoreMultiplier = scoreMult,
            coinRewardMultiplier = coinMult,
            missions = missions,
            levelTitle = levelTitle,
            specialRuleDescription = specialRule
        )
    }

    fun getMissionEngine(): MissionEngine = missionEngine
}
