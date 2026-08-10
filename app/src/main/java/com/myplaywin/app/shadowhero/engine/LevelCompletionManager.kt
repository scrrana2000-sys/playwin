package com.myplaywin.app.shadowhero.engine

import android.content.Context
import android.util.Log
import com.myplaywin.app.shadowhero.data.ShadowHeroProgressionManager

enum class LevelCompletionState {
    ACTIVE,
    COMPLETING,
    COMPLETED
}

/**
 * Authoritative Single Level Completion System for Shadow Hero
 */
class LevelCompletionManager(
    val stageNumber: Int,
    val requiredCrystals: Int
) {
    var state: LevelCompletionState = LevelCompletionState.ACTIVE
        private set

    var isGameplayFrozen: Boolean = false
        private set

    var completionTimeSeconds: Float = 0f
        private set

    init {
        Log.i("LEVEL", "[LEVEL] Stage Started: Stage $stageNumber | Required Objectives: $requiredCrystals 💎")
        if (requiredCrystals <= 0) {
            Log.e("LEVEL", "[LEVEL] Completion Validation FAILED: Required objective count invalid ($requiredCrystals <= 0)")
        }
    }

    fun isComplete(): Boolean = state == LevelCompletionState.COMPLETED

    fun evaluateAndCheckExit(
        context: Context,
        collectedCrystalsCount: Int,
        stageTimeSeconds: Float,
        userId: String,
        seed: Long,
        onCompletionTriggered: () -> Unit
    ): Boolean {
        if (state != LevelCompletionState.ACTIVE) {
            Log.d("LEVEL", "[LEVEL] Completion evaluation ignored: Stage already in $state state")
            return false // Duplicate completion protection
        }

        Log.i("LEVEL", "[LEVEL] Exit Reached | Objective Progress: $collectedCrystalsCount / $requiredCrystals 💎")

        if (collectedCrystalsCount < requiredCrystals) {
            Log.w("LEVEL", "[LEVEL] Completion Validation FAILED: Exit reached but objectives incomplete ($collectedCrystalsCount/$requiredCrystals 💎)")
            return false
        }

        // Objectives met AND Exit Reached! Transition state.
        state = LevelCompletionState.COMPLETING
        completionTimeSeconds = stageTimeSeconds
        isGameplayFrozen = true

        Log.i("LEVEL", "[LEVEL] Completion Validation: SUCCESS")
        Log.i("LEVEL", "[LEVEL] Level Completed: Stage $stageNumber in ${String.format("%.1f", completionTimeSeconds)}s")

        // 1. Save completion immediately to SharedPreferences & unlocked stage progression
        ShadowHeroProgressionManager.updateStatsOnStageComplete(
            context = context,
            stage = stageNumber,
            crystalsCollected = collectedCrystalsCount,
            completionTime = completionTimeSeconds,
            distanceTraveled = 0f,
            powerUpsUsed = 0,
            deathsInStage = 0
        )

        Log.i("LEVEL", "[LEVEL] Progress Saved")
        Log.i("LEVEL", "[LEVEL] Next Stage Unlocked: Stage ${stageNumber + 1}")

        state = LevelCompletionState.COMPLETED
        onCompletionTriggered()
        return true
    }

    fun reset() {
        state = LevelCompletionState.ACTIVE
        isGameplayFrozen = false
        completionTimeSeconds = 0f
    }
}
