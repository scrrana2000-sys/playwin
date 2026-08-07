package com.myplaywin.app.blockmaster.engine

import androidx.compose.ui.graphics.Color
import com.myplaywin.app.blockmaster.audio.BlockMasterAudioEngine
import com.myplaywin.app.blockmaster.blocks.BlockFactory
import com.myplaywin.app.blockmaster.blocks.TetrominoBlock
import com.myplaywin.app.blockmaster.effects.FloatingPopupData
import com.myplaywin.app.blockmaster.grid.BlockGridState
import com.myplaywin.app.blockmaster.missions.MissionEngine
import com.myplaywin.app.blockmaster.missions.MissionObjective
import com.myplaywin.app.blockmaster.powerups.PowerUpEngine
import com.myplaywin.app.blockmaster.powerups.PowerUpRegistry
import com.myplaywin.app.blockmaster.powerups.PowerUpType
import com.myplaywin.app.blockmaster.procedural.GeneratedLevelConfig
import com.myplaywin.app.blockmaster.procedural.ProceduralLevelGenerator
import com.myplaywin.app.blockmaster.special.SpecialBlockSpawnSystem
import com.myplaywin.app.blockmaster.storage.BlockMasterSaveSystem
import com.myplaywin.app.blockmaster.world.BlockWorld
import com.myplaywin.app.blockmaster.world.WorldProgressionManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class GameEngineState {
    IDLE,
    STARTING,
    PLAYING,
    PAUSED,
    GAME_OVER
}

interface GameEngineEventListener {
    fun onLineClear(linesCleared: Int, scoreEarned: Int, coinsEarned: Int)
    fun onCombo(comboCount: Int, bonusScore: Int)
    fun onBackToBack(bonusScore: Int, bonusCoins: Int)
    fun onPerfectClear(bonusScore: Int, bonusCoins: Int)
    fun onLevelUp(newLevel: Int)
    fun onWorldUnlocked(newWorld: BlockWorld)
    fun onMissionCompleted(mission: MissionObjective, xpEarned: Int, coinsEarned: Int)
    fun onLevelCompleted(level: Int, nextLevel: Int)
    fun onGameOver(finalScore: Int, coinsEarned: Int, linesCleared: Int, gameTimeSec: Long)
    fun onSpecialBlockTriggered(text: String)
}

class BlockMasterGameEngine {

    private val engineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var loopJob: Job? = null

    private val _engineState = MutableStateFlow(GameEngineState.IDLE)
    val engineState: StateFlow<GameEngineState> = _engineState.asStateFlow()

    private val _isGameOver = MutableStateFlow(false)
    val isGameOver: StateFlow<Boolean> = _isGameOver.asStateFlow()

    private val _fps = MutableStateFlow(60)
    val fps: StateFlow<Int> = _fps.asStateFlow()

    val gridState = BlockGridState()
    private val sevenBagGenerator = BlockFactory.SevenBagGenerator()

    // Phase 5 Special Block & Power-Up Engines
    val specialSpawnSystem = SpecialBlockSpawnSystem()
    val powerUpEngine = PowerUpEngine()

    // Phase 4 Engine Extensions
    val smartDifficultyEngine = SmartDifficultyEngine()
    val levelGenerator = ProceduralLevelGenerator(smartDifficultyEngine)
    val missionEngine: MissionEngine = levelGenerator.getMissionEngine()

    // Active piece state
    private val _activePiece = MutableStateFlow<TetrominoBlock?>(null)
    val activePiece: StateFlow<TetrominoBlock?> = _activePiece.asStateFlow()

    // Next piece state
    private val _nextPiece = MutableStateFlow<TetrominoBlock>(
        specialSpawnSystem.attachSpecialBlockIfEligible(
            BlockFactory.createPiece(sevenBagGenerator.nextPieceType()),
            1
        )
    )
    val nextPiece: StateFlow<TetrominoBlock> = _nextPiece.asStateFlow()

    // Hold piece state
    private val _holdPiece = MutableStateFlow<TetrominoBlock?>(null)
    val holdPiece: StateFlow<TetrominoBlock?> = _holdPiece.asStateFlow()

    private val _canHold = MutableStateFlow(true)
    val canHold: StateFlow<Boolean> = _canHold.asStateFlow()

    // Ghost piece state
    private val _ghostY = MutableStateFlow(0)
    val ghostY: StateFlow<Int> = _ghostY.asStateFlow()

    // Stats
    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _level = MutableStateFlow(1)
    val level: StateFlow<Int> = _level.asStateFlow()

    private val _lines = MutableStateFlow(0)
    val lines: StateFlow<Int> = _lines.asStateFlow()

    private val _coinsEarned = MutableStateFlow(0)
    val coinsEarned: StateFlow<Int> = _coinsEarned.asStateFlow()

    private val _totalXpEarned = MutableStateFlow(0L)
    val totalXpEarned: StateFlow<Long> = _totalXpEarned.asStateFlow()

    private val _comboCount = MutableStateFlow(0)
    val comboCount: StateFlow<Int> = _comboCount.asStateFlow()

    private val _highestCombo = MutableStateFlow(0)
    val highestCombo: StateFlow<Int> = _highestCombo.asStateFlow()

    private val _floatingPopups = MutableStateFlow<List<FloatingPopupData>>(emptyList())
    val floatingPopups: StateFlow<List<FloatingPopupData>> = _floatingPopups.asStateFlow()

    private val _hasContinuedThisGame = MutableStateFlow(false)
    val hasContinuedThisGame: StateFlow<Boolean> = _hasContinuedThisGame.asStateFlow()

    // Phase 4 World & Mission State
    private var customWorldOverrideId: Int = 0

    private val _currentLevelConfig = MutableStateFlow(levelGenerator.generateLevel(1))
    val currentLevelConfig: StateFlow<GeneratedLevelConfig> = _currentLevelConfig.asStateFlow()

    private val _currentWorld = MutableStateFlow(_currentLevelConfig.value.world)
    val currentWorld: StateFlow<BlockWorld> = _currentWorld.asStateFlow()

    private val _activeMissions = MutableStateFlow<List<MissionObjective>>(_currentLevelConfig.value.missions)
    val activeMissions: StateFlow<List<MissionObjective>> = _activeMissions.asStateFlow()

    private val _worldUnlockEvent = MutableStateFlow<BlockWorld?>(null)
    val worldUnlockEvent: StateFlow<BlockWorld?> = _worldUnlockEvent.asStateFlow()

    private val _missionCompleteEvent = MutableStateFlow<MissionObjective?>(null)
    val missionCompleteEvent: StateFlow<MissionObjective?> = _missionCompleteEvent.asStateFlow()

    private var isPreviousTetris = false
    private var gameStartTimeMs = System.currentTimeMillis()
    private var gameTimeSec = 0L

    var eventListener: GameEngineEventListener? = null
    var audioEngine: BlockMasterAudioEngine? = null

    // Gravity timing
    private var dropIntervalMs = 800L
    private var lastDropTime = System.currentTimeMillis()
    private var isProcessingLineClear = false
    private var lineClearJob: Job? = null

    init {
        setupMissionCallbacks()
        loadLevelConfig(1)
    }

    fun setCustomWorld(worldId: Int) {
        customWorldOverrideId = worldId
        val world = if (worldId > 0) {
            WorldProgressionManager.WORLDS.find { it.id == worldId } ?: WorldProgressionManager.getWorldForLevel(_level.value)
        } else {
            WorldProgressionManager.getWorldForLevel(_level.value)
        }
        val oldWorld = _currentWorld.value
        _currentWorld.value = world
        dropIntervalMs = (_currentLevelConfig.value.dropIntervalMs * world.gameplayModifier.speedMultiplier).toLong()
        if (oldWorld.id != world.id) {
            audioEngine?.playWorldTransitionSound(world.id)
        }
    }

    private fun setupMissionCallbacks() {
        missionEngine.onMissionCompletedListener = { mission ->
            _totalXpEarned.value += mission.rewardXp
            _coinsEarned.value += mission.rewardCoins
            _missionCompleteEvent.value = mission
            triggerPopup("MISSION COMPLETE!", "${mission.title} (+${mission.rewardXp} XP)", Color(0xFF00E676))
            eventListener?.onMissionCompleted(mission, mission.rewardXp, mission.rewardCoins)
        }

        missionEngine.onAllMissionsCompletedListener = {
            engineScope.launch {
                delay(300L)
                advanceToNextInfiniteLevel()
            }
        }
    }

    fun setStartLevel(initialLevel: Int) {
        val lvl = maxOf(1, initialLevel)
        _level.value = lvl
        loadLevelConfig(lvl)
    }

    private fun loadLevelConfig(lvl: Int) {
        val config = levelGenerator.generateLevel(lvl)
        _currentLevelConfig.value = config
        _activeMissions.value = config.missions

        val oldWorld = _currentWorld.value
        val newWorld = if (customWorldOverrideId > 0) {
            WorldProgressionManager.WORLDS.find { it.id == customWorldOverrideId } ?: config.world
        } else {
            config.world
        }
        _currentWorld.value = newWorld
        dropIntervalMs = (config.dropIntervalMs * newWorld.gameplayModifier.speedMultiplier).toLong()

        if (lvl > 1 && newWorld.id != oldWorld.id && newWorld.minLevel == lvl) {
            _worldUnlockEvent.value = newWorld
            audioEngine?.playWorldTransitionSound(newWorld.id)
            eventListener?.onWorldUnlocked(newWorld)
        }
    }

    fun dismissWorldUnlockDialog() {
        _worldUnlockEvent.value = null
    }

    fun dismissMissionCompleteToast() {
        _missionCompleteEvent.value = null
    }

    private fun advanceToNextInfiniteLevel() {
        val nextLvl = _level.value + 1
        smartDifficultyEngine.recordLevelSuccess(gameTimeSec)
        _level.value = nextLvl

        loadLevelConfig(nextLvl)

        triggerPopup("LEVEL COMPLETE!", "ADVANCING TO LEVEL $nextLvl 🚀", Color(0xFF00E5FF))
        eventListener?.onLevelCompleted(nextLvl - 1, nextLvl)
        eventListener?.onLevelUp(nextLvl)
    }

    fun usePowerUp(type: PowerUpType, saveSystem: BlockMasterSaveSystem): Boolean {
        if (_engineState.value != GameEngineState.PLAYING) return false
        if (_isGameOver.value || isProcessingLineClear) return false

        val success = saveSystem.consumePowerUp(type)
        if (!success) return false

        val pu = PowerUpRegistry.getPowerUp(type)
        audioEngine?.playPowerUpSound()

        when (type) {
            PowerUpType.CLEAR_ROW -> {
                gridState.applyPowerUpClearRow()
                triggerPopup("LINE BLAST! 🧹", pu.description, pu.color)
            }
            PowerUpType.CLEAR_COLUMN -> {
                gridState.applyPowerUpClearColumn()
                triggerPopup("COLUMN BEAM! ⚡", pu.description, pu.color)
            }
            PowerUpType.DESTROY_BLOCK -> {
                gridState.applyPowerUpDestroyBlock()
                triggerPopup("BLOCK BUSTER! 💥", pu.description, pu.color)
            }
            PowerUpType.FREEZE_TIME -> {
                powerUpEngine.activatePowerUp(PowerUpType.FREEZE_TIME)
                triggerPopup("TIME FROZEN! ❄️", "15s Slow Gravity", pu.color)
            }
            PowerUpType.SCORE_BOOSTER -> {
                powerUpEngine.activatePowerUp(PowerUpType.SCORE_BOOSTER)
                triggerPopup("2x SCORE! 🚀", "20s Double Points", pu.color)
            }
            PowerUpType.COIN_BOOSTER -> {
                powerUpEngine.activatePowerUp(PowerUpType.COIN_BOOSTER)
                triggerPopup("2x COINS! 🪙", "20s Double Coins", pu.color)
            }
        }
        return true
    }

    fun startGame() {
        if (_engineState.value != GameEngineState.IDLE) return
        _engineState.value = GameEngineState.STARTING
        
        // Reset state & board
        gridState.resetGrid()
        powerUpEngine.resetAll()
        _score.value = 0
        _lines.value = 0
        _coinsEarned.value = 0
        _totalXpEarned.value = 0L
        _comboCount.value = 0
        _highestCombo.value = 0
        gameTimeSec = 0L
        _hasContinuedThisGame.value = false
        _isGameOver.value = false
        _activePiece.value = null
        _holdPiece.value = null
        _canHold.value = true
        isPreviousTetris = false

        // Generate clean next piece
        val rawNextType = sevenBagGenerator.nextPieceType()
        _nextPiece.value = specialSpawnSystem.attachSpecialBlockIfEligible(
            BlockFactory.createPiece(rawNextType),
            _level.value
        )
        loadLevelConfig(_level.value)

        // Spawn first active piece
        spawnNextPiece()

        _engineState.value = GameEngineState.PLAYING
        startEngine()
    }

    fun startEngine() {
        if (_engineState.value == GameEngineState.PLAYING && loopJob != null) return
        _engineState.value = GameEngineState.PLAYING
        _isGameOver.value = false

        if (_activePiece.value == null) {
            spawnNextPiece()
        }

        loopJob?.cancel()
        loopJob = engineScope.launch {
            var lastTime = System.nanoTime()
            var frames = 0
            var fpsTimer = System.currentTimeMillis()
            lastDropTime = System.currentTimeMillis()

            while (isActive && _engineState.value == GameEngineState.PLAYING) {
                val now = System.nanoTime()
                val deltaMs = (now - lastTime) / 1_000_000L
                lastTime = now

                onEngineUpdate(deltaMs)

                frames++
                val currentTime = System.currentTimeMillis()
                if (currentTime - fpsTimer >= 1000) {
                    _fps.value = frames
                    frames = 0
                    fpsTimer = currentTime
                    if (!_isGameOver.value) {
                        gameTimeSec++
                        powerUpEngine.onSecondPassed()
                        missionEngine.onSecondPassed(gameTimeSec)
                        _activeMissions.value = missionEngine.activeMissions
                    }
                }

                delay(16L) // ~60 FPS
            }
        }
    }

    private fun onEngineUpdate(deltaMs: Long) {
        if (isProcessingLineClear || _isGameOver.value) return

        val piece = _activePiece.value ?: return

        val now = System.currentTimeMillis()
        val effectiveInterval = if (powerUpEngine.isTimeFrozen()) dropIntervalMs * 2 else dropIntervalMs

        if (now - lastDropTime >= effectiveInterval) {
            lastDropTime = now
            tickGravity()
        }

        updateGhostPosition()
    }

    private fun tickGravity() {
        val piece = _activePiece.value ?: return
        if (BlockMasterCollisionEngine.isValidPosition(piece, piece.x, piece.y + 1, piece.matrix, gridState)) {
            _activePiece.value = piece.copy(y = piece.y + 1)
        } else {
            lockCurrentPiece()
        }
    }

    private fun lockCurrentPiece() {
        val piece = _activePiece.value ?: return
        gridState.lockPiece(piece)
        _activePiece.value = null

        // Check Completed Lines & Special Block Triggers
        lineClearJob?.cancel()
        lineClearJob = engineScope.launch {
            processLineClearsAndLock()
        }
    }

    private suspend fun processLineClearsAndLock() {
        isProcessingLineClear = true

        val completedRows = gridState.getCompletedRows()

        if (completedRows.isNotEmpty()) {
            val count = completedRows.size

            // 1. Line Clear Visual Flash (while rows are still filled)
            gridState.clearingRows = completedRows.toSet()
            delay(220L) // Flash animation delay

            // 2. Actually process and clear the rows
            val specialResult = gridState.processAndClearRows(completedRows)

            // Audio & Special Block Popups
            if (specialResult.bombsTriggered.isNotEmpty()) {
                audioEngine?.playExplosionSound()
                triggerPopup("BOOM! 💥", "Bomb Cleared Surrounding 3x3!", Color(0xFFFF1744))
            }
            if (specialResult.lightningTriggered) {
                audioEngine?.playElectricSound()
                triggerPopup("LIGHTNING! ⚡", "Vaporized Row / Column!", Color(0xFFFFEA00))
            }
            if (specialResult.timeSlowTriggered) {
                powerUpEngine.activatePowerUp(PowerUpType.FREEZE_TIME)
                triggerPopup("TIME SLOW ⏳", "Falling Speed Reduced!", Color(0xFF29B6F6))
            }
            if (specialResult.iceShatteredCount > 0) {
                audioEngine?.playIceCrackSound()
            }

            // 3. Base Line Clear Scoring & Coins scaled by procedural + power-up + world multipliers
            val worldMod = _currentWorld.value.gameplayModifier
            val scoreMult = _currentLevelConfig.value.scoreMultiplier * powerUpEngine.getActiveScoreMultiplier() * worldMod.scoreMultiplierBonus
            val coinMult = _currentLevelConfig.value.coinRewardMultiplier * powerUpEngine.getActiveCoinMultiplier()

            val basePoints = when (count) {
                1 -> (100 * scoreMult).toInt()
                2 -> (300 * scoreMult).toInt()
                3 -> (500 * scoreMult).toInt()
                4 -> (800 * scoreMult).toInt()
                else -> (100 * count * scoreMult).toInt()
            } + (specialResult.scoreBonus * scoreMult).toInt()

            val baseCoins = when (count) {
                1 -> (5 * coinMult).toInt().coerceAtLeast(1)
                2 -> (10 * coinMult).toInt().coerceAtLeast(2)
                3 -> (15 * coinMult).toInt().coerceAtLeast(3)
                4 -> (25 * coinMult).toInt().coerceAtLeast(5)
                else -> (5 * count * coinMult).toInt().coerceAtLeast(1)
            } + (specialResult.coinsEarned * coinMult).toInt()

            val earnedXp = (count * 15 * scoreMult).toLong()

            var currentEarnedScore = basePoints
            var currentEarnedCoins = baseCoins

            // 4. Combo System
            val newCombo = _comboCount.value + 1
            _comboCount.value = newCombo
            if (newCombo > _highestCombo.value) {
                _highestCombo.value = newCombo
            }

            var comboBonusScore = 0
            if (newCombo >= 2) {
                comboBonusScore = ((newCombo - 1) * 50 * scoreMult * worldMod.comboMultiplier).toInt()
                currentEarnedScore += comboBonusScore
                triggerPopup("COMBO x$newCombo!", "+$comboBonusScore PTS", Color(0xFFFFD700))
                eventListener?.onCombo(newCombo, comboBonusScore)
            }

            // 5. Back to Back Tetris Bonus
            var b2bBonusScore = 0
            var b2bBonusCoins = 0
            if (count == 4 && isPreviousTetris) {
                b2bBonusScore = (400 * scoreMult).toInt()
                b2bBonusCoins = (15 * coinMult).toInt()
                currentEarnedScore += b2bBonusScore
                currentEarnedCoins += b2bBonusCoins
                triggerPopup("BACK TO BACK!", "+$b2bBonusScore PTS | +$b2bBonusCoins 🪙", Color(0xFF00E5FF))
                eventListener?.onBackToBack(b2bBonusScore, b2bBonusCoins)
            }
            isPreviousTetris = (count == 4)

            // 6. Perfect Clear Check
            if (gridState.isBoardEmpty()) {
                val pcBonusScore = (1000 * scoreMult).toInt()
                val pcBonusCoins = (50 * coinMult).toInt()
                currentEarnedScore += pcBonusScore
                currentEarnedCoins += pcBonusCoins
                triggerPopup("PERFECT CLEAR!", "+$pcBonusScore PTS | +$pcBonusCoins 🪙", Color(0xFFFF1744))
                eventListener?.onPerfectClear(pcBonusScore, pcBonusCoins)
            }

            // Update Total States
            _lines.value += count
            _score.value += currentEarnedScore
            _coinsEarned.value += currentEarnedCoins
            _totalXpEarned.value += earnedXp

            // Mission Engine Events
            missionEngine.onLinesCleared(count, _score.value)
            missionEngine.onScoreUpdated(_score.value)
            if (newCombo >= 2) missionEngine.onComboReached(newCombo)
            _activeMissions.value = missionEngine.activeMissions

            // Line Clear Floating Popup & Audio
            val clearLabel = when (count) {
                1 -> "SINGLE!"
                2 -> "DOUBLE!"
                3 -> "TRIPLE!"
                4 -> "TETRIS! 🚀"
                else -> "$count LINES!"
            }
            triggerPopup(clearLabel, "+$currentEarnedScore PTS | +$currentEarnedCoins 🪙", Color(0xFFA855F7))
            audioEngine?.playLineClearSound(count)
            eventListener?.onLineClear(count, currentEarnedScore, currentEarnedCoins)

            // Dynamic Level Up Check
            if (missionEngine.activeMissions.isEmpty()) {
                val calculatedLevel = 1 + (_lines.value / 10)
                if (calculatedLevel > _level.value) {
                    advanceToNextInfiniteLevel()
                }
            }
        } else {
            // Reset Combo if no lines cleared
            _comboCount.value = 0
        }

        _canHold.value = true
        isProcessingLineClear = false

        // Spawn next block or Game Over
        spawnNextPiece()
    }

    private fun spawnNextPiece() {
        val pieceToSpawn = _nextPiece.value
        val rawNextType = sevenBagGenerator.nextPieceType()
        _nextPiece.value = specialSpawnSystem.attachSpecialBlockIfEligible(
            BlockFactory.createPiece(rawNextType),
            _level.value
        )

        if (BlockMasterCollisionEngine.isValidPosition(pieceToSpawn, pieceToSpawn.x, pieceToSpawn.y, pieceToSpawn.matrix, gridState)) {
            _activePiece.value = pieceToSpawn
            updateGhostPosition()
        } else {
            // Spawn blocked -> GAME OVER!
            triggerGameOver()
        }
    }

    private fun triggerGameOver() {
        smartDifficultyEngine.recordLevelFailure()
        _activePiece.value = null
        _isGameOver.value = true
        _engineState.value = GameEngineState.GAME_OVER
        loopJob?.cancel()

        audioEngine?.playGameOverSound()

        eventListener?.onGameOver(
            finalScore = _score.value,
            coinsEarned = _coinsEarned.value,
            linesCleared = _lines.value,
            gameTimeSec = gameTimeSec
        )
    }

    fun continueAfterGameOver() {
        if (_hasContinuedThisGame.value) return
        _hasContinuedThisGame.value = true
        _isGameOver.value = false

        // Clear bottom 6 rows to free board space
        gridState.clearBottomRows(6)

        // Spawn piece again and resume engine
        startEngine()
    }

    fun restartEngine() {
        stopEngine()
        _engineState.value = GameEngineState.STARTING
        gridState.resetGrid()
        powerUpEngine.resetAll()
        _score.value = 0
        _lines.value = 0
        _coinsEarned.value = 0
        _totalXpEarned.value = 0L
        _comboCount.value = 0
        _highestCombo.value = 0
        gameTimeSec = 0L
        _hasContinuedThisGame.value = false
        _isGameOver.value = false
        _activePiece.value = null
        _holdPiece.value = null
        _canHold.value = true
        isPreviousTetris = false

        // Generate a clean next piece preview
        val rawNextType = sevenBagGenerator.nextPieceType()
        _nextPiece.value = specialSpawnSystem.attachSpecialBlockIfEligible(
            BlockFactory.createPiece(rawNextType),
            _level.value
        )

        loadLevelConfig(_level.value)
        spawnNextPiece()
        _engineState.value = GameEngineState.PLAYING
        startEngine()
    }

    private fun updateGhostPosition() {
        val piece = _activePiece.value ?: return
        _ghostY.value = BlockMasterCollisionEngine.calculateGhostY(piece, gridState)
    }

    fun triggerPopup(text: String, subText: String? = null, color: Color = Color(0xFF00E5FF)) {
        val popup = FloatingPopupData(text = text, subText = subText, color = color)
        val currentList = _floatingPopups.value.toMutableList()
        currentList.add(popup)
        if (currentList.size > 5) {
            currentList.removeAt(0)
        }
        _floatingPopups.value = currentList

        // Auto remove after 1.2s
        engineScope.launch {
            delay(1200L)
            val updated = _floatingPopups.value.toMutableList()
            updated.remove(popup)
            _floatingPopups.value = updated
        }
    }

    // --- CONTROLS ---

    fun moveLeft() {
        if (_engineState.value != GameEngineState.PLAYING) return
        if (_isGameOver.value || isProcessingLineClear) return
        val piece = _activePiece.value ?: return
        if (BlockMasterCollisionEngine.isValidPosition(piece, piece.x - 1, piece.y, piece.matrix, gridState)) {
            _activePiece.value = piece.copy(x = piece.x - 1)
            audioEngine?.playClickSound()
            updateGhostPosition()
        }
    }

    fun moveRight() {
        if (_engineState.value != GameEngineState.PLAYING) return
        if (_isGameOver.value || isProcessingLineClear) return
        val piece = _activePiece.value ?: return
        if (BlockMasterCollisionEngine.isValidPosition(piece, piece.x + 1, piece.y, piece.matrix, gridState)) {
            _activePiece.value = piece.copy(x = piece.x + 1)
            audioEngine?.playClickSound()
            updateGhostPosition()
        }
    }

    fun rotatePiece() {
        if (_engineState.value != GameEngineState.PLAYING) return
        if (_isGameOver.value || isProcessingLineClear) return
        val piece = _activePiece.value ?: return
        val rotatedPiece = BlockMasterCollisionEngine.tryRotationWithWallKick(piece, gridState)
        if (rotatedPiece != null) {
            _activePiece.value = rotatedPiece
            audioEngine?.playClickSound()
            updateGhostPosition()
        }
    }

    fun softDrop() {
        if (_engineState.value != GameEngineState.PLAYING) return
        if (_isGameOver.value || isProcessingLineClear) return
        val piece = _activePiece.value ?: return
        if (BlockMasterCollisionEngine.isValidPosition(piece, piece.x, piece.y + 1, piece.matrix, gridState)) {
            _activePiece.value = piece.copy(y = piece.y + 1)
            _score.value += 1 // Soft drop point
            lastDropTime = System.currentTimeMillis()
            updateGhostPosition()

            missionEngine.onSoftDropExecuted()
            _activeMissions.value = missionEngine.activeMissions
        } else {
            lockCurrentPiece()
        }
    }

    fun hardDrop() {
        if (_engineState.value != GameEngineState.PLAYING) return
        if (_isGameOver.value || isProcessingLineClear) return
        val piece = _activePiece.value ?: return
        val targetY = BlockMasterCollisionEngine.calculateGhostY(piece, gridState)
        val dropDistance = targetY - piece.y
        _score.value += dropDistance * 2 // Hard drop points

        val droppedPiece = piece.copy(y = targetY)
        _activePiece.value = droppedPiece

        missionEngine.onHardDropExecuted()
        _activeMissions.value = missionEngine.activeMissions

        lockCurrentPiece()
    }

    fun holdCurrentPiece() {
        if (_engineState.value != GameEngineState.PLAYING) return
        if (!_canHold.value || _isGameOver.value || isProcessingLineClear) return
        val piece = _activePiece.value ?: return

        _canHold.value = false
        val currentHold = _holdPiece.value

        _holdPiece.value = BlockFactory.createPiece(piece.type)

        if (currentHold == null) {
            spawnNextPiece()
        } else {
            val swappedPiece = BlockFactory.createPiece(currentHold.type)
            if (BlockMasterCollisionEngine.isValidPosition(swappedPiece, swappedPiece.x, swappedPiece.y, swappedPiece.matrix, gridState)) {
                _activePiece.value = swappedPiece
                updateGhostPosition()
            }
        }
    }

    fun pauseEngine() {
        if (_engineState.value == GameEngineState.PLAYING) {
            _engineState.value = GameEngineState.PAUSED
        }
    }

    fun resumeEngine() {
        if (_engineState.value == GameEngineState.PAUSED && !_isGameOver.value) {
            _engineState.value = GameEngineState.PLAYING
            startEngine()
        }
    }

    fun stopEngine() {
        _engineState.value = GameEngineState.IDLE
        loopJob?.cancel()
        loopJob = null
        lineClearJob?.cancel()
        lineClearJob = null
        isProcessingLineClear = false
    }

    fun release() {
        stopEngine()
        engineScope.cancel()
    }
}
