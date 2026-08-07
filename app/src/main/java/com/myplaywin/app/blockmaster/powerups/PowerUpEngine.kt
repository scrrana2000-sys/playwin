package com.myplaywin.app.blockmaster.powerups

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PowerUpEngine {

    private val _freezeTimeRemaining = MutableStateFlow(0)
    val freezeTimeRemaining: StateFlow<Int> = _freezeTimeRemaining.asStateFlow()

    private val _scoreBoosterRemaining = MutableStateFlow(0)
    val scoreBoosterRemaining: StateFlow<Int> = _scoreBoosterRemaining.asStateFlow()

    private val _coinBoosterRemaining = MutableStateFlow(0)
    val coinBoosterRemaining: StateFlow<Int> = _coinBoosterRemaining.asStateFlow()

    fun activatePowerUp(type: PowerUpType) {
        val powerUp = PowerUpRegistry.getPowerUp(type)
        when (type) {
            PowerUpType.FREEZE_TIME -> _freezeTimeRemaining.value = powerUp.durationSec
            PowerUpType.SCORE_BOOSTER -> _scoreBoosterRemaining.value = powerUp.durationSec
            PowerUpType.COIN_BOOSTER -> _coinBoosterRemaining.value = powerUp.durationSec
            else -> {} // Instant powerups executed directly on grid
        }
    }

    fun onSecondPassed() {
        if (_freezeTimeRemaining.value > 0) {
            _freezeTimeRemaining.value = _freezeTimeRemaining.value - 1
        }
        if (_scoreBoosterRemaining.value > 0) {
            _scoreBoosterRemaining.value = _scoreBoosterRemaining.value - 1
        }
        if (_coinBoosterRemaining.value > 0) {
            _coinBoosterRemaining.value = _coinBoosterRemaining.value - 1
        }
    }

    fun isTimeFrozen(): Boolean = _freezeTimeRemaining.value > 0

    fun getActiveScoreMultiplier(): Float = if (_scoreBoosterRemaining.value > 0) 2.0f else 1.0f

    fun getActiveCoinMultiplier(): Float = if (_coinBoosterRemaining.value > 0) 2.0f else 1.0f

    fun resetAll() {
        _freezeTimeRemaining.value = 0
        _scoreBoosterRemaining.value = 0
        _coinBoosterRemaining.value = 0
    }
}
