package com.myplaywin.app.blockmaster.missions

import kotlin.math.max

enum class MissionType {
    CLEAR_LINES,
    REACH_SCORE,
    CREATE_COMBOS,
    PERFORM_TETRIS,
    HARD_DROPS,
    SOFT_DROPS,
    SURVIVE_TIME,
    NO_HOLD_CLEAR
}

data class MissionObjective(
    val id: String,
    val type: MissionType,
    val title: String,
    val description: String,
    val targetAmount: Int,
    var currentAmount: Int = 0,
    var isCompleted: Boolean = false,
    val rewardXp: Int = 50,
    val rewardCoins: Int = 10
) {
    val progressFraction: Float
        get() = if (targetAmount <= 0) 1f else (currentAmount.toFloat() / targetAmount.toFloat()).coerceIn(0f, 1f)
}

class MissionEngine {

    private val _activeMissions = mutableListOf<MissionObjective>()
    val activeMissions: List<MissionObjective> get() = _activeMissions.toList()

    var onMissionCompletedListener: ((MissionObjective) -> Unit)? = null
    var onAllMissionsCompletedListener: (() -> Unit)? = null

    fun generateMissionsForLevel(level: Int, dynamicDifficultyMultiplier: Float = 1.0f) {
        _activeMissions.clear()

        val lvl = maxOf(1, level)
        val numMissions = if (lvl % 5 == 0 || lvl > 50) 2 else 1

        val possibleTypes = mutableListOf<MissionType>()
        possibleTypes.add(MissionType.CLEAR_LINES)
        if (lvl >= 2) possibleTypes.add(MissionType.REACH_SCORE)
        if (lvl >= 3) possibleTypes.add(MissionType.HARD_DROPS)
        if (lvl >= 4) possibleTypes.add(MissionType.CREATE_COMBOS)
        if (lvl >= 5) possibleTypes.add(MissionType.PERFORM_TETRIS)
        if (lvl >= 7) possibleTypes.add(MissionType.SOFT_DROPS)
        if (lvl >= 10) possibleTypes.add(MissionType.SURVIVE_TIME)

        // Deterministic pseudo-random seed per level so it is consistent on retries but dynamic across levels
        val seed = (lvl * 1337 + 42)
        val selectedTypes = possibleTypes.shuffled(kotlin.random.Random(seed)).take(numMissions)

        selectedTypes.forEachIndexed { index, type ->
            val mission = createObjective(type, lvl, index, dynamicDifficultyMultiplier)
            _activeMissions.add(mission)
        }
    }

    private fun createObjective(
        type: MissionType,
        level: Int,
        index: Int,
        diffMult: Float
    ): MissionObjective {
        val id = "mission_${level}_$index"
        val baseMultiplier = (1.0f + (level - 1) * 0.08f) * diffMult

        return when (type) {
            MissionType.CLEAR_LINES -> {
                val target = (4 + (level * 1.5f) * diffMult).toInt().coerceIn(4, 30)
                MissionObjective(
                    id = id,
                    type = type,
                    title = "Line Clearer",
                    description = "Clear $target lines",
                    targetAmount = target,
                    rewardXp = 50 + level * 10,
                    rewardCoins = 10 + level * 2
                )
            }
            MissionType.REACH_SCORE -> {
                val target = (500 + level * 350 * diffMult).toInt()
                MissionObjective(
                    id = id,
                    type = type,
                    title = "High Scorer",
                    description = "Reach $target score",
                    targetAmount = target,
                    rewardXp = 60 + level * 12,
                    rewardCoins = 15 + level * 3
                )
            }
            MissionType.CREATE_COMBOS -> {
                val target = (2 + (level / 15)).coerceIn(2, 5)
                MissionObjective(
                    id = id,
                    type = type,
                    title = "Combo Master",
                    description = "Reach a x$target combo",
                    targetAmount = target,
                    rewardXp = 75 + level * 15,
                    rewardCoins = 20 + level * 4
                )
            }
            MissionType.PERFORM_TETRIS -> {
                val target = if (level > 40) 2 else 1
                MissionObjective(
                    id = id,
                    type = type,
                    title = "Tetris Hero",
                    description = "Perform $target Tetris (4-line clear)",
                    targetAmount = target,
                    rewardXp = 100 + level * 20,
                    rewardCoins = 25 + level * 5
                )
            }
            MissionType.HARD_DROPS -> {
                val target = (8 + level * 2).coerceIn(10, 50)
                MissionObjective(
                    id = id,
                    type = type,
                    title = "Speed Dropper",
                    description = "Perform $target hard drops",
                    targetAmount = target,
                    rewardXp = 40 + level * 8,
                    rewardCoins = 8 + level * 2
                )
            }
            MissionType.SOFT_DROPS -> {
                val target = (15 + level * 3).coerceIn(15, 60)
                MissionObjective(
                    id = id,
                    type = type,
                    title = "Precision Down",
                    description = "Perform $target soft drops",
                    targetAmount = target,
                    rewardXp = 40 + level * 8,
                    rewardCoins = 8 + level * 2
                )
            }
            MissionType.SURVIVE_TIME -> {
                val target = (45 + level * 3).coerceIn(45, 180) // seconds
                MissionObjective(
                    id = id,
                    type = type,
                    title = "Surviver",
                    description = "Survive for $target seconds",
                    targetAmount = target,
                    rewardXp = 80 + level * 15,
                    rewardCoins = 18 + level * 3
                )
            }
            MissionType.NO_HOLD_CLEAR -> {
                val target = 6
                MissionObjective(
                    id = id,
                    type = type,
                    title = "Pure Hands",
                    description = "Clear $target lines without holding",
                    targetAmount = target,
                    rewardXp = 90 + level * 15,
                    rewardCoins = 20 + level * 4
                )
            }
        }
    }

    fun onLinesCleared(lines: Int, currentTotalScore: Int) {
        _activeMissions.forEach { mission ->
            if (mission.isCompleted) return@forEach
            when (mission.type) {
                MissionType.CLEAR_LINES -> {
                    mission.currentAmount += lines
                    checkCompletion(mission)
                }
                MissionType.PERFORM_TETRIS -> {
                    if (lines >= 4) {
                        mission.currentAmount += 1
                        checkCompletion(mission)
                    }
                }
                MissionType.REACH_SCORE -> {
                    mission.currentAmount = currentTotalScore
                    checkCompletion(mission)
                }
                else -> {}
            }
        }
    }

    fun onScoreUpdated(newScore: Int) {
        _activeMissions.forEach { mission ->
            if (mission.isCompleted) return@forEach
            if (mission.type == MissionType.REACH_SCORE) {
                mission.currentAmount = newScore
                checkCompletion(mission)
            }
        }
    }

    fun onComboReached(combo: Int) {
        _activeMissions.forEach { mission ->
            if (mission.isCompleted) return@forEach
            if (mission.type == MissionType.CREATE_COMBOS) {
                if (combo >= mission.targetAmount) {
                    mission.currentAmount = mission.targetAmount
                    checkCompletion(mission)
                }
            }
        }
    }

    fun onHardDropExecuted() {
        _activeMissions.forEach { mission ->
            if (mission.isCompleted) return@forEach
            if (mission.type == MissionType.HARD_DROPS) {
                mission.currentAmount += 1
                checkCompletion(mission)
            }
        }
    }

    fun onSoftDropExecuted() {
        _activeMissions.forEach { mission ->
            if (mission.isCompleted) return@forEach
            if (mission.type == MissionType.SOFT_DROPS) {
                mission.currentAmount += 1
                checkCompletion(mission)
            }
        }
    }

    fun onSecondPassed(gameTimeSec: Long) {
        _activeMissions.forEach { mission ->
            if (mission.isCompleted) return@forEach
            if (mission.type == MissionType.SURVIVE_TIME) {
                mission.currentAmount = gameTimeSec.toInt()
                checkCompletion(mission)
            }
        }
    }

    private fun checkCompletion(mission: MissionObjective) {
        if (!mission.isCompleted && mission.currentAmount >= mission.targetAmount) {
            mission.isCompleted = true
            onMissionCompletedListener?.invoke(mission)

            if (_activeMissions.all { it.isCompleted }) {
                onAllMissionsCompletedListener?.invoke()
            }
        }
    }

    fun isAllMissionsCompleted(): Boolean {
        return _activeMissions.isNotEmpty() && _activeMissions.all { it.isCompleted }
    }
}
