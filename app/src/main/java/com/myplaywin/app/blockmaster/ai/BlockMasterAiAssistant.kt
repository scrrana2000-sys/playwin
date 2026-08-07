package com.myplaywin.app.blockmaster.ai

import com.myplaywin.app.blockmaster.storage.BlockMasterSaveData
import kotlin.random.Random

data class AiInsight(
    val title: String,
    val description: String,
    val recommendedPowerUp: String,
    val skillGrade: String,
    val tipCategory: String
)

object BlockMasterAiAssistant {

    private val TIPS_LIST = listOf(
        "Clear double lines simultaneously to trigger 2.5x score multipliers!",
        "Save your Laser Beam power-up for when the grid is over 75% full.",
        "Clear corner blocks early to maintain flexibility for 3x3 square blocks.",
        "Chain consecutive line clears to build a massive Combo Streak multiplier!",
        "Use the Undo power-up immediately if a block drop seals off an open gap.",
        "Focus on clearing middle columns to create natural drop cascades.",
        "Maintain a low board profile to give yourself room for complex multi-piece shapes.",
        "In Time Attack mode, rapid single-line clears yield more bonus time than waiting."
    )

    fun analyzePlayerSkill(saveData: BlockMasterSaveData, gridFillPercent: Float = 0.3f): AiInsight {
        val totalGames = saveData.totalGamesPlayed.coerceAtLeast(1)
        val avgScore = saveData.lifetimeScore / totalGames
        val combo = saveData.highestComboAllTime

        val skillGrade = when {
            avgScore > 25000 || combo >= 10 -> "S+ (Grandmaster)"
            avgScore > 15000 || combo >= 7  -> "A (Elite Builder)"
            avgScore > 8000  || combo >= 4  -> "B (Skilled Stacker)"
            else                            -> "C (Rising Apprentice)"
        }

        val powerUpRec = when {
            gridFillPercent > 0.7f -> "💣 Bomb Blast (Clear Danger Area)"
            combo > 3              -> "⚡ Lightning Surge (Maintain Combo)"
            else                   -> "🔄 Block Swap (Optimize Layout)"
        }

        val randomTip = TIPS_LIST[Random.nextInt(TIPS_LIST.size)]

        return AiInsight(
            title = "AI Tactical Analysis",
            description = randomTip,
            recommendedPowerUp = powerUpRec,
            skillGrade = skillGrade,
            tipCategory = if (gridFillPercent > 0.65f) "DANGER ALERT" else "TACTICAL ADVICE"
        )
    }
}
