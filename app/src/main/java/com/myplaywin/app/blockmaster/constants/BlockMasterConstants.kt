package com.myplaywin.app.blockmaster.constants

import androidx.compose.ui.graphics.Color

object BlockMasterConstants {
    const val GAME_TITLE = "BLOCK MASTER"
    const val GAME_SUBTITLE = "Stack blocks, clear lines & earn coins."
    
    // Grid Dimensions
    const val GRID_COLUMNS = 10
    const val GRID_ROWS = 20
    
    // Target Performance
    const val TARGET_FPS = 60
    const val FRAME_TIME_MS = 1000L / TARGET_FPS // ~16.66ms
    
    // Theme Colors
    val DarkBackground = Color(0xFF0D0B14)
    val CardBackground = Color(0xFF14111F)
    val CardBorder = Color(0xFFA855F7)
    val NeonCyan = Color(0xFF00E5FF)
    val NeonPurple = Color(0xFFA855F7)
    val NeonYellow = Color(0xFFFFD700)
    val NeonGreen = Color(0xFF00E676)
    val CellEmptyFill = Color(0xFF1A162B)
    val CellBorderColor = Color(0xFF2A233C)
    val CellGlowColor = Color(0x33A855F7)
    
    // Storage Keys
    const val PREFS_NAME = "block_master_prefs"
    const val KEY_HIGH_SCORE = "bm_high_score"
    const val KEY_COINS = "bm_coins"
    const val KEY_LEVEL = "bm_level"
    const val KEY_SOUND_ENABLED = "bm_sound_enabled"
    const val KEY_MUSIC_ENABLED = "bm_music_enabled"
    const val KEY_MUSIC_VOLUME = "bm_music_volume"
    const val KEY_SFX_VOLUME = "bm_sfx_volume"
    const val KEY_TOTAL_GAMES = "bm_total_games"
    const val KEY_LINES_CLEARED = "bm_lines_cleared"
    const val KEY_TIME_PLAYED_SEC = "bm_time_played_sec"
    const val KEY_UNLOCKED_WORLDS = "bm_unlocked_worlds"
    const val KEY_PLAYER_NAME = "bm_player_name"
    
    // Phase 4 Storage Keys
    const val KEY_PLAYER_XP = "bm_player_xp"
    const val KEY_CURRENT_INFINITE_LEVEL = "bm_current_infinite_level"
    const val KEY_HIGHEST_LEVEL = "bm_highest_level"
    const val KEY_HIGHEST_COMBO = "bm_highest_combo"
}
