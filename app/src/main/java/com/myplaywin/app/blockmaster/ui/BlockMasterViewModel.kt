package com.myplaywin.app.blockmaster.ui

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.myplaywin.app.blockmaster.ads.BlockMasterAdEngine
import com.myplaywin.app.blockmaster.assets.BlockMasterAssetManager
import com.myplaywin.app.blockmaster.audio.BlockMasterAudioEngine
import com.myplaywin.app.blockmaster.blocks.TetrominoBlock
import com.myplaywin.app.blockmaster.effects.FloatingPopupData
import com.myplaywin.app.blockmaster.engine.BlockMasterGameEngine
import com.myplaywin.app.blockmaster.engine.GameEngineEventListener
import com.myplaywin.app.blockmaster.engine.GameMode
import com.myplaywin.app.blockmaster.grid.BlockGridState
import com.myplaywin.app.blockmaster.liveops.*
import com.myplaywin.app.blockmaster.missions.MissionObjective
import com.myplaywin.app.blockmaster.powerups.PowerUpType
import com.myplaywin.app.blockmaster.procedural.GeneratedLevelConfig
import com.myplaywin.app.blockmaster.security.BlockMasterAntiCheat
import com.myplaywin.app.blockmaster.storage.BlockMasterSaveData
import com.myplaywin.app.blockmaster.storage.BlockMasterSaveSystem
import com.myplaywin.app.blockmaster.sync.BlockMasterSyncManager
import com.myplaywin.app.blockmaster.cosmetics.CosmeticCategory
import com.myplaywin.app.blockmaster.cosmetics.CosmeticRegistry
import com.myplaywin.app.blockmaster.luckyspin.LuckySpinEngine
import com.myplaywin.app.blockmaster.luckyspin.SpinRewardSlice
import com.myplaywin.app.blockmaster.seasons.SeasonEngine
import com.myplaywin.app.blockmaster.store.DailyStoreOffer
import com.myplaywin.app.blockmaster.world.BlockWorld
import com.myplaywin.app.data.repository.WalletService
import com.playwin.ads.RewardType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BlockMasterViewModel(context: Context) : ViewModel() {

    val gameEngine = BlockMasterGameEngine()
    val saveSystem = BlockMasterSaveSystem(context)
    val audioEngine = BlockMasterAudioEngine(context)
    val assetManager = BlockMasterAssetManager(context)
    val syncManager = BlockMasterSyncManager(context)

    val gridState: BlockGridState = gameEngine.gridState

    val activePiece: StateFlow<TetrominoBlock?> = gameEngine.activePiece
    val nextPiece: StateFlow<TetrominoBlock> = gameEngine.nextPiece
    val holdPiece: StateFlow<TetrominoBlock?> = gameEngine.holdPiece
    val canHold: StateFlow<Boolean> = gameEngine.canHold
    val ghostY: StateFlow<Int> = gameEngine.ghostY

    val score: StateFlow<Int> = gameEngine.score
    val level: StateFlow<Int> = gameEngine.level
    val lines: StateFlow<Int> = gameEngine.lines
    val coinsEarned: StateFlow<Int> = gameEngine.coinsEarned
    val totalXpEarned: StateFlow<Long> = gameEngine.totalXpEarned
    val comboCount: StateFlow<Int> = gameEngine.comboCount
    val highestCombo: StateFlow<Int> = gameEngine.highestCombo
    val isGameOver: StateFlow<Boolean> = gameEngine.isGameOver
    val floatingPopups: StateFlow<List<FloatingPopupData>> = gameEngine.floatingPopups
    val hasContinuedThisGame: StateFlow<Boolean> = gameEngine.hasContinuedThisGame

    val currentLevelConfig: StateFlow<GeneratedLevelConfig> = gameEngine.currentLevelConfig
    val currentWorld: StateFlow<BlockWorld> = gameEngine.currentWorld
    val activeMissions: StateFlow<List<MissionObjective>> = gameEngine.activeMissions
    val worldUnlockEvent: StateFlow<BlockWorld?> = gameEngine.worldUnlockEvent
    val missionCompleteEvent: StateFlow<MissionObjective?> = gameEngine.missionCompleteEvent

    // Phase 5 Power-Up States
    val freezeTimeRemaining: StateFlow<Int> = gameEngine.powerUpEngine.freezeTimeRemaining
    val scoreBoosterRemaining: StateFlow<Int> = gameEngine.powerUpEngine.scoreBoosterRemaining
    val coinBoosterRemaining: StateFlow<Int> = gameEngine.powerUpEngine.coinBoosterRemaining

    val saveData: StateFlow<BlockMasterSaveData> = saveSystem.saveData
    val fps: StateFlow<Int> = gameEngine.fps
    val engineState: StateFlow<com.myplaywin.app.blockmaster.engine.GameEngineState> = gameEngine.engineState

    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    private val _isWorldMapOpen = MutableStateFlow(false)
    val isWorldMapOpen: StateFlow<Boolean> = _isWorldMapOpen.asStateFlow()

    // --- PHASE 7 LIVEOPS STATE ---
    private val _isLiveHubOpen = MutableStateFlow(false)
    val isLiveHubOpen: StateFlow<Boolean> = _isLiveHubOpen.asStateFlow()

    private val _isAchievementsOpen = MutableStateFlow(false)
    val isAchievementsOpen: StateFlow<Boolean> = _isAchievementsOpen.asStateFlow()

    private val _isProfileStatsOpen = MutableStateFlow(false)
    val isProfileStatsOpen: StateFlow<Boolean> = _isProfileStatsOpen.asStateFlow()

    private val _chestRewardOutcome = MutableStateFlow<ChestRewardOutcome?>(null)
    val chestRewardOutcome: StateFlow<ChestRewardOutcome?> = _chestRewardOutcome.asStateFlow()

    // --- PHASE 8 REWARD & AD STATES ---
    private val _isAdRewardCenterOpen = MutableStateFlow(false)
    val isAdRewardCenterOpen: StateFlow<Boolean> = _isAdRewardCenterOpen.asStateFlow()

    private val _isCoinAnimationTriggered = MutableStateFlow(false)
    val isCoinAnimationTriggered: StateFlow<Boolean> = _isCoinAnimationTriggered.asStateFlow()

    private val _hasDoubledMatchCoins = MutableStateFlow(false)
    val hasDoubledMatchCoins: StateFlow<Boolean> = _hasDoubledMatchCoins.asStateFlow()

    // --- PHASE 9 STATES ---
    private val _isSeasonPassOpen = MutableStateFlow(false)
    val isSeasonPassOpen: StateFlow<Boolean> = _isSeasonPassOpen.asStateFlow()

    private val _isDailyStoreOpen = MutableStateFlow(false)
    val isDailyStoreOpen: StateFlow<Boolean> = _isDailyStoreOpen.asStateFlow()

    private val _isLuckySpinOpen = MutableStateFlow(false)
    val isLuckySpinOpen: StateFlow<Boolean> = _isLuckySpinOpen.asStateFlow()

    private val _isCollectionsOpen = MutableStateFlow(false)
    val isCollectionsOpen: StateFlow<Boolean> = _isCollectionsOpen.asStateFlow()

    private val _isAdvancedStatsOpen = MutableStateFlow(false)
    val isAdvancedStatsOpen: StateFlow<Boolean> = _isAdvancedStatsOpen.asStateFlow()

    private val _isLeaderboardOpen = MutableStateFlow(false)
    val isLeaderboardOpen: StateFlow<Boolean> = _isLeaderboardOpen.asStateFlow()

    private val _selectedGameMode = MutableStateFlow(GameMode.CLASSIC)
    val selectedGameMode: StateFlow<GameMode> = _selectedGameMode.asStateFlow()

    private val _isModeSelectorOpen = MutableStateFlow(false)
    val isModeSelectorOpen: StateFlow<Boolean> = _isModeSelectorOpen.asStateFlow()

    private val _isShareDialogOpen = MutableStateFlow(false)
    val isShareDialogOpen: StateFlow<Boolean> = _isShareDialogOpen.asStateFlow()

    private val _isAiAssistantOpen = MutableStateFlow(false)
    val isAiAssistantOpen: StateFlow<Boolean> = _isAiAssistantOpen.asStateFlow()

    private val _isPhotoModeActive = MutableStateFlow(false)
    val isPhotoModeActive: StateFlow<Boolean> = _isPhotoModeActive.asStateFlow()

    val dailyMissions: MutableStateFlow<List<LiveMission>> = MutableStateFlow(emptyList())
    val weeklyMissions: MutableStateFlow<List<LiveMission>> = MutableStateFlow(emptyList())
    val achievements: MutableStateFlow<List<AchievementItem>> = MutableStateFlow(emptyList())

    val activeEvent: SpecialLiveEvent = SpecialEventsManager.getActiveEvent()

    init {
        gameEngine.audioEngine = audioEngine
        val data = saveSystem.saveData.value
        audioEngine.setMusicEnabled(data.musicEnabled)
        audioEngine.setSfxEnabled(data.soundEnabled)
        audioEngine.updateVolumes(data.musicVolume, data.sfxVolume)

        // Restore saved infinite level & selected custom world
        gameEngine.setStartLevel(data.currentInfiniteLevel)
        if (data.selectedWorldId > 0) {
            gameEngine.setCustomWorld(data.selectedWorldId)
        }

        refreshLiveMissionsAndAchievements()
        setupGameEventListener()

        // Flush offline pending rewards when initialized
        syncManager.syncPendingRewards()
        syncManager.syncPlayerProfileStats(data)
    }

    private fun refreshLiveMissionsAndAchievements() {
        val data = saveSystem.saveData.value
        val dayKey = DailyWeeklyMissionEngine.getCurrentDayKey()
        val weekKey = DailyWeeklyMissionEngine.getCurrentWeekKey()

        dailyMissions.value = DailyWeeklyMissionEngine.generateDailyMissions(dayKey, data.playerLevel)
        weeklyMissions.value = DailyWeeklyMissionEngine.generateWeeklyMissions(weekKey, data.playerLevel)
        achievements.value = AchievementEngine.evaluateAchievements(data, data.claimedAchievements)
    }

    private fun setupGameEventListener() {
        gameEngine.eventListener = object : GameEngineEventListener {
            override fun onLineClear(linesCleared: Int, scoreEarned: Int, coinsEarned: Int) {
                audioEngine.playLineClearSound(linesCleared)
                if (coinsEarned > 0) {
                    audioEngine.playCoinSound()
                }
            }

            override fun onCombo(comboCount: Int, bonusScore: Int) {
                audioEngine.playComboSound(comboCount)
            }

            override fun onBackToBack(bonusScore: Int, bonusCoins: Int) {
                audioEngine.playLineClearSound(4)
            }

            override fun onPerfectClear(bonusScore: Int, bonusCoins: Int) {
                audioEngine.playPerfectClearSound()
                saveSystem.recordSpecialBlockStats(perfectClears = 1)
            }

            override fun onLevelUp(newLevel: Int) {
                audioEngine.playLevelUpSound()
                saveSystem.updateInfiniteLevel(newLevel)
            }

            override fun onWorldUnlocked(newWorld: BlockWorld) {
                audioEngine.playLevelUpSound()
            }

            override fun onMissionCompleted(mission: MissionObjective, xpEarned: Int, coinsEarned: Int) {
                audioEngine.playCoinSound()
                saveSystem.addXpAndCoins(xpEarned.toLong(), coinsEarned)
                syncManager.awardCoins(coinsEarned, "Block Master Mission: ${mission.title}", "MINI_GAME_REWARD")
            }

            override fun onLevelCompleted(level: Int, nextLevel: Int) {
                audioEngine.playLevelUpSound()
                saveSystem.updateInfiniteLevel(nextLevel)
            }

            override fun onSpecialBlockTriggered(text: String) {
                audioEngine.playPowerUpSound()
            }

            override fun onGameOver(
                finalScore: Int,
                coinsEarned: Int,
                linesCleared: Int,
                gameTimeSec: Long
            ) {
                audioEngine.playGameOverSound()

                val sanitizedCoins = BlockMasterAntiCheat.validateMatchCoins(coinsEarned, gameTimeSec, linesCleared)

                // Save game progress locally with Phase 4, Phase 5 & Phase 7 stats
                saveSystem.saveGameProgress(
                    score = finalScore,
                    addedCoins = sanitizedCoins,
                    linesCleared = linesCleared,
                    durationSec = gameTimeSec,
                    reachedLevel = level.value,
                    earnedXp = totalXpEarned.value,
                    maxCombo = highestCombo.value,
                    isWin = linesCleared >= 20
                )

                refreshLiveMissionsAndAchievements()

                // Sync with PlayWin Firebase wallet & profile node
                syncManager.awardCoins(
                    coins = sanitizedCoins,
                    source = "Block Master Match Reward",
                    type = "MINI_GAME_REWARD"
                )
                syncManager.syncPlayerProfileStats(saveSystem.saveData.value)
            }
        }
    }

    fun usePowerUp(type: PowerUpType) {
        gameEngine.usePowerUp(type, saveSystem)
        refreshLiveMissionsAndAchievements()
    }

    // --- PHASE 7 LIVEOPS HANDLERS ---

    fun toggleLiveHub(open: Boolean) {
        if (open) refreshLiveMissionsAndAchievements()
        _isLiveHubOpen.value = open
    }

    fun toggleAchievements(open: Boolean) {
        if (open) refreshLiveMissionsAndAchievements()
        _isAchievementsOpen.value = open
    }

    fun toggleProfileStats(open: Boolean) {
        _isProfileStatsOpen.value = open
    }

    fun claimDailyMission(mission: LiveMission) {
        val success = saveSystem.claimDailyMission(mission.id, mission.rewardCoins, mission.rewardXp)
        if (success) {
            audioEngine.playCoinSound()
            _isCoinAnimationTriggered.value = true
            syncManager.awardCoins(mission.rewardCoins, "Daily Mission: ${mission.title}", "MINI_GAME_REWARD")
            refreshLiveMissionsAndAchievements()
        }
    }

    fun claimWeeklyMission(mission: LiveMission) {
        val success = saveSystem.claimWeeklyMission(mission.id, mission.rewardCoins, mission.rewardXp)
        if (success) {
            audioEngine.playCoinSound()
            _isCoinAnimationTriggered.value = true
            syncManager.awardCoins(mission.rewardCoins, "Weekly Mission: ${mission.title}", "MINI_GAME_REWARD")
            refreshLiveMissionsAndAchievements()
        }
    }

    fun claimAchievement(item: AchievementItem) {
        val success = saveSystem.claimAchievement(item.id, item.rewardCoins, item.rewardXp)
        if (success) {
            audioEngine.playCoinSound()
            _isCoinAnimationTriggered.value = true
            syncManager.awardCoins(item.rewardCoins, "Achievement: ${item.title}", "MINI_GAME_REWARD")
            refreshLiveMissionsAndAchievements()
        }
    }

    fun claimLoginDay(dayReward: LoginRewardDay) {
        val success = saveSystem.claimLoginDay(
            dayNumber = dayReward.dayNumber,
            coins = dayReward.rewardCoins,
            xp = dayReward.rewardXp,
            chestRarity = dayReward.chestReward,
            powerUpReward = dayReward.powerUpReward
        )
        if (success) {
            audioEngine.playCoinSound()
            _isCoinAnimationTriggered.value = true
            syncManager.awardCoins(dayReward.rewardCoins, "Daily Calendar Day ${dayReward.dayNumber}", "MINI_GAME_REWARD")
            refreshLiveMissionsAndAchievements()
        }
    }

    fun openChest(rarity: MysteryChestRarity) {
        val outcome = MysteryChestEngine.openChest(rarity)
        saveSystem.addXpAndCoins(outcome.xp, outcome.coins)
        outcome.powerUps.forEach { (type, count) ->
            saveSystem.addPowerUpCount(type, count)
        }
        audioEngine.playPerfectClearSound()
        if (outcome.coins > 0) {
            _isCoinAnimationTriggered.value = true
            syncManager.awardCoins(outcome.coins, "Mystery Chest (${rarity.title})", "MINI_GAME_REWARD")
        }
        _chestRewardOutcome.value = outcome
        refreshLiveMissionsAndAchievements()
    }

    fun dismissChestOverlay() {
        _chestRewardOutcome.value = null
    }

    // --- PHASE 8 AD REWARD & MONEY HANDLERS ---

    fun toggleAdRewardCenter(open: Boolean) {
        _isAdRewardCenterOpen.value = open
    }

    fun dismissCoinAnimation() {
        _isCoinAnimationTriggered.value = false
    }

    fun watchAdForContinue(activity: Activity) {
        if (!BlockMasterAntiCheat.isActionAllowed()) return
        BlockMasterAdEngine.showRewardedAd(
            activity = activity,
            rewardType = RewardType.BLOCK_MASTER_CONTINUE,
            onSuccess = { _, _ ->
                audioEngine.playPowerUpSound()
                gameEngine.continueAfterGameOver()
            },
            onError = { _ -> }
        )
    }

    fun watchAdForDoubleReward(activity: Activity) {
        if (!BlockMasterAntiCheat.isActionAllowed()) return
        if (_hasDoubledMatchCoins.value) return

        val earned = coinsEarned.value
        if (earned <= 0) return

        BlockMasterAdEngine.showRewardedAd(
            activity = activity,
            rewardType = RewardType.BLOCK_MASTER_DOUBLE_REWARD,
            onSuccess = { _, _ ->
                _hasDoubledMatchCoins.value = true
                audioEngine.playPerfectClearSound()
                _isCoinAnimationTriggered.value = true
                saveSystem.addXpAndCoins(0L, earned)
                syncManager.awardCoins(earned, "Block Master 2x Match Bonus", "MINI_GAME_REWARD")
                toggleAdRewardCenter(false)
            },
            onError = { _ -> }
        )
    }

    fun watchAdForBonusCoins(activity: Activity) {
        if (!BlockMasterAntiCheat.isActionAllowed()) return
        val bonus = 100

        BlockMasterAdEngine.showRewardedAd(
            activity = activity,
            rewardType = RewardType.BLOCK_MASTER_EXTRA_COINS,
            onSuccess = { _, _ ->
                audioEngine.playCoinSound()
                _isCoinAnimationTriggered.value = true
                saveSystem.addXpAndCoins(0L, bonus)
                syncManager.awardCoins(bonus, "Block Master Rewarded Ad Bonus", "MINI_GAME_REWARD")
                toggleAdRewardCenter(false)
            },
            onError = { _ -> }
        )
    }

    fun watchAdForPowerUp(activity: Activity) {
        if (!BlockMasterAntiCheat.isActionAllowed()) return

        BlockMasterAdEngine.showRewardedAd(
            activity = activity,
            rewardType = RewardType.BLOCK_MASTER_POWERUP,
            onSuccess = { _, _ ->
                audioEngine.playPowerUpSound()
                saveSystem.addPowerUpCount(PowerUpType.CLEAR_ROW, 2)
                saveSystem.addPowerUpCount(PowerUpType.DESTROY_BLOCK, 2)
                toggleAdRewardCenter(false)
            },
            onError = { _ -> }
        )
    }

    // --- WORLD SYSTEM HANDLERS ---

    fun toggleWorldMap(open: Boolean) {
        _isWorldMapOpen.value = open
    }

    fun selectWorld(worldId: Int) {
        saveSystem.setSelectedWorldId(worldId)
        gameEngine.setCustomWorld(worldId)
    }

    fun claimWorldReward(worldId: Int) {
        val success = saveSystem.claimWorldReward(worldId)
        if (success) {
            audioEngine.playCoinSound()
            refreshLiveMissionsAndAchievements()
        }
    }

    // --- INPUT ACTION HANDLERS ---

    fun dismissWorldUnlockDialog() {
        gameEngine.dismissWorldUnlockDialog()
    }

    fun dismissMissionCompleteToast() {
        gameEngine.dismissMissionCompleteToast()
    }

    fun onMoveLeft() {
        gameEngine.moveLeft()
    }

    fun onMoveRight() {
        gameEngine.moveRight()
    }

    fun onRotate() {
        gameEngine.rotatePiece()
    }

    fun onSoftDrop() {
        gameEngine.softDrop()
    }

    fun onHardDrop() {
        gameEngine.hardDrop()
    }

    fun onHold() {
        gameEngine.holdCurrentPiece()
    }

    fun onPlayAgain() {
        gameEngine.restartEngine()
    }

    fun startGame() {
        gameEngine.startGame()
    }

    fun onContinueWithAd() {
        gameEngine.continueAfterGameOver()
    }

    fun toggleSound() {
        val newSoundState = !saveData.value.soundEnabled
        saveSystem.setSoundEnabled(newSoundState)
        audioEngine.setSfxEnabled(newSoundState)
    }

    fun toggleMusic() {
        val newMusicState = !saveData.value.musicEnabled
        saveSystem.setMusicEnabled(newMusicState)
        audioEngine.setMusicEnabled(newMusicState)
    }

    fun toggleHaptic() {
        saveSystem.toggleHaptic()
    }

    fun setGraphicsQuality(quality: String) {
        saveSystem.setGraphicsQuality(quality)
    }

    fun toggleFpsDisplay() {
        saveSystem.toggleFpsDisplay()
    }

    fun setSelectedLanguage(language: String) {
        saveSystem.setSelectedLanguage(language)
    }

    fun resetProgress() {
        saveSystem.resetProgress()
        gameEngine.restartEngine()
    }

    fun toggleSettingsDialog(open: Boolean) {
        _isSettingsOpen.value = open
    }

    // --- PHASE 9 HANDLERS ---

    fun toggleSeasonPass(open: Boolean) {
        _isSeasonPassOpen.value = open
    }

    fun toggleDailyStore(open: Boolean) {
        _isDailyStoreOpen.value = open
    }

    fun toggleLuckySpin(open: Boolean) {
        _isLuckySpinOpen.value = open
    }

    fun toggleCollections(open: Boolean) {
        _isCollectionsOpen.value = open
    }

    fun toggleAdvancedStats(open: Boolean) {
        _isAdvancedStatsOpen.value = open
    }

    fun toggleLeaderboard(open: Boolean) {
        _isLeaderboardOpen.value = open
    }

    fun setGameMode(mode: GameMode) {
        _selectedGameMode.value = mode
        gameEngine.restartEngine()
    }

    fun toggleModeSelector(open: Boolean) {
        _isModeSelectorOpen.value = open
    }

    fun toggleShareDialog(open: Boolean) {
        _isShareDialogOpen.value = open
    }

    fun toggleAiAssistant(open: Boolean) {
        _isAiAssistantOpen.value = open
    }

    fun togglePhotoMode(active: Boolean? = null) {
        _isPhotoModeActive.value = active ?: !_isPhotoModeActive.value
    }

    fun claimSeasonFreeReward(seasonId: Int, level: Int) {
        if (!BlockMasterAntiCheat.isActionAllowed()) return
        val key = "s${seasonId}_free_$level"
        val success = saveSystem.claimSeasonFreeReward(key)
        if (success) {
            audioEngine.playCoinSound()
            _isCoinAnimationTriggered.value = true
            val bonusCoins = if (level % 10 == 0) 500 else 100
            saveSystem.addXpAndCoins(0L, bonusCoins)
            syncManager.awardCoins(bonusCoins, "Season $seasonId Level $level Free Pass Reward", "MINI_GAME_REWARD")
        }
    }

    fun claimSeasonPremiumReward(seasonId: Int, level: Int) {
        if (!BlockMasterAntiCheat.isActionAllowed()) return
        val key = "s${seasonId}_prem_$level"
        val success = saveSystem.claimSeasonPremiumReward(key)
        if (success) {
            audioEngine.playPerfectClearSound()
            _isCoinAnimationTriggered.value = true
            val bonusCoins = if (level % 10 == 0) 1500 else 300
            saveSystem.addXpAndCoins(0L, bonusCoins)
            syncManager.awardCoins(bonusCoins, "Season $seasonId Level $level Premium Pass Reward", "MINI_GAME_REWARD")
        }
    }

    fun buyStoreOffer(offer: DailyStoreOffer) {
        if (!BlockMasterAntiCheat.isActionAllowed()) return
        val currentCoins = saveData.value.coins
        val price = offer.finalPriceCoins
        if (currentCoins < price) return

        if (!saveSystem.spendCoins(price)) return

        audioEngine.playCoinSound()

        when (offer.itemType) {
            "COSMETIC" -> {
                offer.cosmeticId?.let { saveSystem.unlockCosmetic(it) }
            }
            "POWERUP_PACK" -> {
                saveSystem.addPowerUpCount(PowerUpType.CLEAR_ROW, offer.count)
                saveSystem.addPowerUpCount(PowerUpType.CLEAR_COLUMN, offer.count)
                saveSystem.addPowerUpCount(PowerUpType.DESTROY_BLOCK, offer.count)
            }
            "CHEST" -> {
                saveSystem.addChestCount(MysteryChestRarity.DIAMOND, 1)
            }
        }
    }

    fun processLuckySpinReward(slice: SpinRewardSlice, wasFree: Boolean) {
        if (wasFree) {
            saveSystem.recordLuckySpinTimestamp()
        } else {
            if (!saveSystem.spendCoins(LuckySpinEngine.SPIN_COST_COINS)) return
        }

        audioEngine.playPerfectClearSound()
        _isCoinAnimationTriggered.value = true

        when (slice.rewardType) {
            "COINS", "JACKPOT" -> {
                saveSystem.addXpAndCoins(0L, slice.rewardValue)
                syncManager.awardCoins(slice.rewardValue, "Lucky Spin: ${slice.title}", "MINI_GAME_REWARD")
            }
            "XP" -> {
                saveSystem.addXpAndCoins(slice.rewardValue.toLong(), 0)
            }
            "POWERUPS" -> {
                saveSystem.addPowerUpCount(PowerUpType.DESTROY_BLOCK, slice.rewardValue)
                saveSystem.addPowerUpCount(PowerUpType.CLEAR_COLUMN, slice.rewardValue)
            }
            "CHEST" -> {
                saveSystem.addChestCount(MysteryChestRarity.DIAMOND, 1)
            }
            "TITLE" -> {
                saveSystem.unlockCosmetic("title_spin_master")
            }
        }
    }

    fun equipCosmetic(category: CosmeticCategory, id: String) {
        saveSystem.equipCosmetic(category.name, id)
        audioEngine.playClickSound()
    }

    fun onPause() {
        gameEngine.pauseEngine()
        audioEngine.pauseAll()
    }

    fun release() {
        gameEngine.release()
        audioEngine.release()
        assetManager.clearUnusedCache()
    }

    fun onResume() {
        gameEngine.resumeEngine()
        audioEngine.resumeAll()
    }

    override fun onCleared() {
        super.onCleared()
        release()
    }
}
