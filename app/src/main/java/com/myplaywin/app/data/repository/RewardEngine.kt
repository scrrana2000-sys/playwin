package com.myplaywin.app.data.repository

import com.myplaywin.app.data.model.FirebaseSpinReward
import com.myplaywin.app.data.model.FirebaseScratchCardReward
import kotlin.random.Random

object RewardEngine {

    /**
     * Centralized function to roll a Spin Wheel reward based strictly on Firebase probability weight.
     * Prevents client-side hardcoding or guessing.
     */
    fun rollSpinReward(rewards: List<FirebaseSpinReward>): FirebaseSpinReward? {
        val activeRewards = rewards.filter { it.active }.sortedBy { it.displayOrder }
        if (activeRewards.isEmpty()) return null

        val totalWeight = activeRewards.sumOf { it.probabilityWeight }
        if (totalWeight <= 0) return activeRewards.random()

        val roll = Random.nextInt(totalWeight)
        var cumulative = 0
        for (reward in activeRewards) {
            cumulative += reward.probabilityWeight
            if (roll < cumulative) {
                return reward
            }
        }
        return activeRewards.lastOrNull()
    }

    /**
     * Centralized function to roll a Scratch Card reward based strictly on Firebase probability weight.
     * Does NOT use a hardcoded "Better Luck Next Time" fallback unless the Firebase configuration is empty.
     */
    fun rollScratchReward(rewards: List<FirebaseScratchCardReward>): FirebaseScratchCardReward? {
        val activeRewards = rewards.filter { it.active }.sortedBy { it.displayOrder }
        if (activeRewards.isEmpty()) return null

        val totalWeight = activeRewards.sumOf { it.probabilityWeight }
        if (totalWeight <= 0) return activeRewards.random()

        val roll = Random.nextInt(totalWeight)
        var cumulative = 0
        for (reward in activeRewards) {
            cumulative += reward.probabilityWeight
            if (roll < cumulative) {
                return reward
            }
        }
        return activeRewards.lastOrNull()
    }

    /**
     * Centralized function to calculate Quiz completion rewards based on correct answers,
     * reward-per-question, and completion bonus coefficients.
     */
    fun calculateQuizReward(
        score: Int,
        totalQuestions: Int,
        rewardPerCorrect: Int,
        completionBonus: Int
    ): Int {
        val baseReward = score * rewardPerCorrect
        val bonus = if (score == totalQuestions) completionBonus else 0
        return baseReward + bonus
    }
}
