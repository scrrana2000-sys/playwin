package com.myplaywin.app.ui.screens

import android.content.Context
import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.*

data class DailyMission(
    val id: String,
    val title: String,
    val description: String,
    val type: String, // "EAT_FRUITS", "REACH_SCORE_100", "REACH_SCORE_200", "PLAY_3_GAMES", "SURVIVE_2_MIN", "COMPLETE_5_LEVELS", "COLLECT_50_FRUITS", "NO_PAUSE"
    val target: Int,
    var progress: Int,
    val difficulty: String, // "Easy", "Medium", "Hard"
    val reward: Int,
    var claimed: Boolean = false
)

data class Achievement(
    val id: String,
    val icon: String,
    val name: String,
    val description: String,
    val target: Int,
    var progress: Int,
    val reward: Int,
    var unlocked: Boolean = false,
    var claimed: Boolean = false
)

data class SnakeStats(
    val gamesPlayed: Int,
    val highestScore: Int,
    val totalFruits: Int,
    val totalCoinsEarned: Int,
    val longestSurvivalTime: Int, // in seconds
    val totalScore: Int
) {
    val averageScore: Int
        get() = if (gamesPlayed > 0) totalScore / gamesPlayed else 0
}

object SnakeProgressionManager {
    
    // Delimiter-based serialization to avoid any external JSON library overhead or crash-prone parsing
    fun serializeMissions(missions: List<DailyMission>): String {
        return missions.joinToString(";") { m ->
            "${m.id}|${m.title}|${m.description}|${m.type}|${m.target}|${m.progress}|${m.difficulty}|${m.reward}|${if (m.claimed) 1 else 0}"
        }
    }

    fun deserializeMissions(str: String): List<DailyMission> {
        if (str.isEmpty()) return emptyList()
        val list = mutableListOf<DailyMission>()
        try {
            val parts = str.split(";")
            for (p in parts) {
                if (p.isEmpty()) continue
                val fields = p.split("|")
                if (fields.size >= 9) {
                    list.add(
                        DailyMission(
                            id = fields[0],
                            title = fields[1],
                            description = fields[2],
                            type = fields[3],
                            target = fields[4].toIntOrNull() ?: 0,
                            progress = fields[5].toIntOrNull() ?: 0,
                            difficulty = fields[6],
                            reward = fields[7].toIntOrNull() ?: 0,
                            claimed = fields[8] == "1"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun serializeAchievements(achievements: List<Achievement>): String {
        return achievements.joinToString(";") { a ->
            "${a.id}|${a.icon}|${a.name}|${a.description}|${a.target}|${a.progress}|${a.reward}|${if (a.unlocked) 1 else 0}|${if (a.claimed) 1 else 0}"
        }
    }

    fun deserializeAchievements(str: String): List<Achievement> {
        if (str.isEmpty()) return emptyList()
        val list = mutableListOf<Achievement>()
        try {
            val parts = str.split(";")
            for (p in parts) {
                if (p.isEmpty()) continue
                val fields = p.split("|")
                if (fields.size >= 9) {
                    list.add(
                        Achievement(
                            id = fields[0],
                            icon = fields[1],
                            name = fields[2],
                            description = fields[3],
                            target = fields[4].toIntOrNull() ?: 0,
                            progress = fields[5].toIntOrNull() ?: 0,
                            reward = fields[6].toIntOrNull() ?: 0,
                            unlocked = fields[7] == "1",
                            claimed = fields[8] == "1"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    // Load or generate Daily Missions (guaranteed to be 3)
    fun loadMissions(context: Context): List<DailyMission> {
        val prefs = context.getSharedPreferences("snake_progression_prefs", Context.MODE_PRIVATE)
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastResetDay = prefs.getString("last_reset_day", "") ?: ""
        
        if (todayStr != lastResetDay) {
            // Create 3 brand new random missions
            val pool = listOf(
                DailyMission("m1", "Eat 20 Fruits", "Eat 20 fruits today", "EAT_FRUITS", 20, 0, "Easy", 20),
                DailyMission("m2", "Reach Score 100", "Reach a score of 100 in a single game", "REACH_SCORE_100", 100, 0, "Medium", 40),
                DailyMission("m3", "Reach Score 200", "Reach a score of 200 in a single game", "REACH_SCORE_200", 200, 0, "Hard", 75),
                DailyMission("m4", "Play 3 Games", "Play 3 games of Snake", "PLAY_3_GAMES", 3, 0, "Easy", 20),
                DailyMission("m5", "Survive for 2 Minutes", "Survive for 2 minutes (120s) in a single game", "SURVIVE_2_MIN", 120, 0, "Medium", 40),
                DailyMission("m6", "Complete 5 Levels", "Reach Speed Level 5 in a single game", "COMPLETE_5_LEVELS", 5, 0, "Medium", 40),
                DailyMission("m7", "Collect 50 Fruits", "Collect 50 fruits today", "COLLECT_50_FRUITS", 50, 0, "Hard", 75),
                DailyMission("m8", "Finish One Game Without Pause", "Finish one game without ever pausing", "NO_PAUSE", 1, 0, "Easy", 20)
            )
            val selected = pool.shuffled().take(3)
            prefs.edit()
                .putString("last_reset_day", todayStr)
                .putString("daily_missions", serializeMissions(selected))
                .apply()
            return selected
        }
        
        val stored = prefs.getString("daily_missions", "") ?: ""
        if (stored.isEmpty()) {
            // Initial boot
            val pool = listOf(
                DailyMission("m1", "Eat 20 Fruits", "Eat 20 fruits today", "EAT_FRUITS", 20, 0, "Easy", 20),
                DailyMission("m4", "Play 3 Games", "Play 3 games of Snake", "PLAY_3_GAMES", 3, 0, "Easy", 20),
                DailyMission("m8", "Finish One Game Without Pause", "Finish one game without ever pausing", "NO_PAUSE", 1, 0, "Easy", 20)
            )
            prefs.edit()
                .putString("last_reset_day", todayStr)
                .putString("daily_missions", serializeMissions(pool))
                .apply()
            return pool
        }
        
        return deserializeMissions(stored)
    }

    fun saveMissions(context: Context, missions: List<DailyMission>) {
        val prefs = context.getSharedPreferences("snake_progression_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("daily_missions", serializeMissions(missions)).apply()
    }

    // Load permanent achievements
    fun loadAchievements(context: Context): List<Achievement> {
        val prefs = context.getSharedPreferences("snake_progression_prefs", Context.MODE_PRIVATE)
        val stored = prefs.getString("achievements", "") ?: ""
        if (stored.isEmpty()) {
            val initial = listOf(
                Achievement("a1", "🐍", "First Snake", "Play your first game of Snake", 1, 0, 50),
                Achievement("a2", "🍎", "Fruit Collector", "Collect 100 Fruits in total", 100, 0, 100),
                Achievement("a3", "⚡", "Speed Master", "Reach Speed Level 10", 10, 0, 150),
                Achievement("a4", "🏆", "Score Hunter", "Reach Score 500 in a single game", 500, 0, 200),
                Achievement("a5", "👑", "Snake King", "Reach Score 1000 in a single game", 1000, 0, 500),
                Achievement("a6", "🔥", "Survivor", "Survive for 5 Minutes (300s) in a single game", 300, 0, 250),
                Achievement("a7", "💎", "Coin Collector", "Earn 1000 PlayWin Coins from Snake", 1000, 0, 300)
            )
            prefs.edit().putString("achievements", serializeAchievements(initial)).apply()
            return initial
        }
        return deserializeAchievements(stored)
    }

    fun saveAchievements(context: Context, achievements: List<Achievement>) {
        val prefs = context.getSharedPreferences("snake_progression_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("achievements", serializeAchievements(achievements)).apply()
    }

    // Load statistics
    fun loadStats(context: Context): SnakeStats {
        val prefs = context.getSharedPreferences("snake_progression_prefs", Context.MODE_PRIVATE)
        val gameHistoryPrefs = context.getSharedPreferences("snake_game_prefs", Context.MODE_PRIVATE)
        val highestScore = gameHistoryPrefs.getInt("high_score", 0)

        return SnakeStats(
            gamesPlayed = prefs.getInt("stats_games_played", 0),
            highestScore = highestScore,
            totalFruits = prefs.getInt("stats_total_fruits", 0),
            totalCoinsEarned = prefs.getInt("stats_total_coins_earned", 0),
            longestSurvivalTime = prefs.getInt("stats_longest_survival_time", 0),
            totalScore = prefs.getInt("stats_total_score", 0)
        )
    }

    fun saveStats(context: Context, stats: SnakeStats) {
        val prefs = context.getSharedPreferences("snake_progression_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putInt("stats_games_played", stats.gamesPlayed)
            .putInt("stats_total_fruits", stats.totalFruits)
            .putInt("stats_total_coins_earned", stats.totalCoinsEarned)
            .putInt("stats_longest_survival_time", stats.longestSurvivalTime)
            .putInt("stats_total_score", stats.totalScore)
            .apply()
    }

    // --- Level progression storage ---
    fun loadUnlockedLevel(context: Context): Int {
        val prefs = context.getSharedPreferences("snake_progression_prefs", Context.MODE_PRIVATE)
        return prefs.getInt("stats_unlocked_level", 1)
    }

    fun saveUnlockedLevel(context: Context, levelNum: Int) {
        val prefs = context.getSharedPreferences("snake_progression_prefs", Context.MODE_PRIVATE)
        val current = prefs.getInt("stats_unlocked_level", 1)
        if (levelNum > current) {
            prefs.edit().putInt("stats_unlocked_level", levelNum).apply()
        }
    }

    fun loadLevelStars(context: Context, levelNum: Int): Int {
        val prefs = context.getSharedPreferences("snake_progression_prefs", Context.MODE_PRIVATE)
        return prefs.getInt("stats_level_stars_$levelNum", 0)
    }

    fun saveLevelStars(context: Context, levelNum: Int, stars: Int) {
        val prefs = context.getSharedPreferences("snake_progression_prefs", Context.MODE_PRIVATE)
        val current = prefs.getInt("stats_level_stars_$levelNum", 0)
        if (stars > current) {
            prefs.edit().putInt("stats_level_stars_$levelNum", stars).apply()
        }
    }

    fun loadLevelHighScore(context: Context, levelNum: Int): Int {
        val prefs = context.getSharedPreferences("snake_progression_prefs", Context.MODE_PRIVATE)
        return prefs.getInt("stats_level_score_$levelNum", 0)
    }

    fun saveLevelHighScore(context: Context, levelNum: Int, score: Int) {
        val prefs = context.getSharedPreferences("snake_progression_prefs", Context.MODE_PRIVATE)
        val current = prefs.getInt("stats_level_score_$levelNum", 0)
        if (score > current) {
            prefs.edit().putInt("stats_level_score_$levelNum", score).apply()
        }
    }

    fun loadLevelBestTime(context: Context, levelNum: Int): Int {
        val prefs = context.getSharedPreferences("snake_progression_prefs", Context.MODE_PRIVATE)
        return prefs.getInt("stats_level_time_$levelNum", 99999)
    }

    fun saveLevelBestTime(context: Context, levelNum: Int, timeSeconds: Int) {
        val prefs = context.getSharedPreferences("snake_progression_prefs", Context.MODE_PRIVATE)
        val current = prefs.getInt("stats_level_time_$levelNum", 99999)
        if (timeSeconds < current) {
            prefs.edit().putInt("stats_level_time_$levelNum", timeSeconds).apply()
        }
    }

    fun resetProgress(context: Context) {
        val prefs = context.getSharedPreferences("snake_progression_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putInt("stats_unlocked_level", 1)
        for (i in 1..100) {
            editor.remove("stats_level_stars_$i")
            editor.remove("stats_level_score_$i")
            editor.remove("stats_level_time_$i")
        }
        editor.apply()
    }
}

// --- Theme and Level Models ---

enum class SnakeTheme(
    val id: String,
    val emoji: String,
    val title: String,
    val primaryColor: Color,
    val gridBgColor: Color,
    val wallColor: Color,
    val snakeHeadColor: Color,
    val snakeBodyColor: Color,
    val foodEmoji: String,
    val particleColors: List<Color>
) {
    NEON_CITY("NEON_CITY", "🌌", "Neon City", Color(0xFF00E5FF), Color(0xFF0D0A1B), Color(0xFF7C4DFF), Color(0xFF00FFCC), Color(0xFF00B3FF), "🍒", listOf(Color(0xFF00E5FF), Color(0xFF7C4DFF), Color(0xFFFF007F))),
    FOREST("FOREST", "🌲", "Forest", Color(0xFF4CAF50), Color(0xFF0F1E11), Color(0xFF8D6E63), Color(0xFF81C784), Color(0xFF388E3C), "🍄", listOf(Color(0xFF4CAF50), Color(0xFF8D6E63), Color(0xFFC8E6C9))),
    SNOW_WORLD("SNOW_WORLD", "❄️", "Snow World", Color(0xFF90CAF9), Color(0xFF0B192C), Color(0xFFB0BEC5), Color(0xFFE3F2FD), Color(0xFF42A5F5), "❄️", listOf(Color(0xFFE3F2FD), Color(0xFF90CAF9), Color(0xFFFFFFFF))),
    VOLCANO("VOLCANO", "🌋", "Volcano", Color(0xFFFF3D00), Color(0xFF1E0A0A), Color(0xFF5D4037), Color(0xFFFF9100), Color(0xFFDD2C00), "🌶️", listOf(Color(0xFFFF3D00), Color(0xFFFF9100), Color(0xFFFFEA00))),
    DESERT("DESERT", "🏜️", "Desert", Color(0xFFFFB74D), Color(0xFF1C130D), Color(0xFF8D6E63), Color(0xFFFFD54F), Color(0xFFF57C00), "🌵", listOf(Color(0xFFFFB74D), Color(0xFFFFD54F), Color(0xFFD7CCC8))),
    OCEAN("OCEAN", "🌊", "Ocean", Color(0xFF00E5FF), Color(0xFF0A192F), Color(0xFF00ACC1), Color(0xFF80DEEA), Color(0xFF00838F), "🐚", listOf(Color(0xFF00E5FF), Color(0xFF80DEEA), Color(0xFFE0F7FA))),
    SPACE("SPACE", "🚀", "Space", Color(0xFFE040FB), Color(0xFF050510), Color(0xFF651FFF), Color(0xFFF48FB1), Color(0xFF7C4DFF), "⭐", listOf(Color(0xFFE040FB), Color(0xFF651FFF), Color(0xFFF48FB1))),
    CYBER_GRID("CYBER_GRID", "⚡", "Cyber Grid", Color(0xFF00E676), Color(0xFF050F08), Color(0xFF00E676), Color(0xFFB9F6CA), Color(0xFF00C853), "💾", listOf(Color(0xFF00E676), Color(0xFF00C853), Color(0xFFCCFF90))),
    ANCIENT_TEMPLE("ANCIENT_TEMPLE", "🏛️", "Ancient Temple", Color(0xFFFFD54F), Color(0xFF14120F), Color(0xFFFFA726), Color(0xFFFFE082), Color(0xFFD84315), "🏺", listOf(Color(0xFFFFD54F), Color(0xFFFFA726), Color(0xFFBCAAA4))),
    DRAGON_KINGDOM("DRAGON_KINGDOM", "👑", "Dragon Kingdom", Color(0xFFFFD700), Color(0xFF1F0D0D), Color(0xFFFF5252), Color(0xFFFFD700), Color(0xFFB71C1C), "🐲", listOf(Color(0xFFFFD700), Color(0xFFFF5252), Color(0xFFFFD700)))
}

data class SnakeLevel(
    val number: Int,
    val name: String,
    val theme: SnakeTheme,
    val targetFruits: Int,
    val baseDelay: Long,
    val obstacles: List<SnakePoint>,
    val portals: List<Pair<SnakePoint, SnakePoint>>,
    val breakableWalls: List<SnakePoint>,
    val lavaTiles: List<SnakePoint>,
    val specialMechanic: String, // "NONE", "ICE", "LAVA", "WIND", "BOSS", "BONUS"
    val isBoss: Boolean = false,
    val isBonus: Boolean = false
)

fun generateLevelData(levelNum: Int): SnakeLevel {
    val themeIdx = ((levelNum - 1) / 10) % 10
    val theme = SnakeTheme.values()[themeIdx]
    
    val isBoss = levelNum % 10 == 0
    val isBonus = levelNum % 10 == 5
    
    val targetFruits = if (isBonus) {
        999 // Unlimited
    } else if (isBoss) {
        15
    } else {
        10 + (levelNum % 10) * 2
    }
    
    // Progressively faster snake speed (lower baseDelay)
    val baseDelay = (300 - (levelNum * 1.8)).coerceAtLeast(110.0).toLong()
    
    val obstacles = mutableListOf<SnakePoint>()
    val portals = mutableListOf<Pair<SnakePoint, SnakePoint>>()
    val breakableWalls = mutableListOf<SnakePoint>()
    val lavaTiles = mutableListOf<SnakePoint>()
    
    val specialMechanic = when {
        isBoss -> "BOSS"
        isBonus -> "BONUS"
        theme == SnakeTheme.SNOW_WORLD -> "ICE"
        theme == SnakeTheme.VOLCANO -> "LAVA"
        theme == SnakeTheme.DESERT -> "WIND"
        else -> "NONE"
    }
    
    val name = when (theme) {
        SnakeTheme.NEON_CITY -> if (isBoss) "Cyber Core" else if (isBonus) "Neon Rush" else "Neon Alley Lvl $levelNum"
        SnakeTheme.FOREST -> if (isBoss) "Wood Golem" else if (isBonus) "Forest Hunt" else "Deep Forest Lvl $levelNum"
        SnakeTheme.SNOW_WORLD -> if (isBoss) "Frost Titan" else if (isBonus) "Ice Slide" else "Frosty Pass Lvl $levelNum"
        SnakeTheme.VOLCANO -> if (isBoss) "Inferno Lord" else if (isBonus) "Magma Burst" else "Ash Valley Lvl $levelNum"
        SnakeTheme.DESERT -> if (isBoss) "Scorpion Den" else if (isBonus) "Dune Gold" else "Oasis Crossing Lvl $levelNum"
        SnakeTheme.OCEAN -> if (isBoss) "Kraken Abyss" else if (isBonus) "Pearl Dive" else "Reef Swim Lvl $levelNum"
        SnakeTheme.SPACE -> if (isBoss) "Cosmic Void" else if (isBonus) "Star Fall" else "Orbit Path Lvl $levelNum"
        SnakeTheme.CYBER_GRID -> if (isBoss) "A.I. Core" else if (isBonus) "Data Rush" else "Byte Stream Lvl $levelNum"
        SnakeTheme.ANCIENT_TEMPLE -> if (isBoss) "Stone Guard" else if (isBonus) "Relic Vault" else "Sacred Hall Lvl $levelNum"
        SnakeTheme.DRAGON_KINGDOM -> if (isBoss) "Wyvern Keep" else if (isBonus) "Royal Hoard" else "Dragon Den Lvl $levelNum"
    }

    // Generate obstacles symmetrically based on level number (avoid middle vertical line x=10 and horizontal line y=13 and snake start points)
    if (!isBonus) {
        val layoutPattern = levelNum % 6
        if (layoutPattern == 1 && !isBoss) {
            // Symmetric corner structures
            for (i in 2..4) {
                obstacles.add(SnakePoint(i, 3))
                obstacles.add(SnakePoint(i, 4))
                obstacles.add(SnakePoint(20 - 1 - i, 3))
                obstacles.add(SnakePoint(20 - 1 - i, 4))
                
                obstacles.add(SnakePoint(i, 22))
                obstacles.add(SnakePoint(i, 21))
                obstacles.add(SnakePoint(20 - 1 - i, 22))
                obstacles.add(SnakePoint(20 - 1 - i, 21))
            }
        } else if (layoutPattern == 2 && !isBoss) {
            // Symmetric inner pillars
            obstacles.add(SnakePoint(4, 6))
            obstacles.add(SnakePoint(4, 7))
            obstacles.add(SnakePoint(15, 6))
            obstacles.add(SnakePoint(15, 7))
            obstacles.add(SnakePoint(4, 19))
            obstacles.add(SnakePoint(4, 18))
            obstacles.add(SnakePoint(15, 19))
            obstacles.add(SnakePoint(15, 18))
        } else if (layoutPattern == 3 && !isBoss) {
            // Central side brackets
            for (y in 8..11) {
                obstacles.add(SnakePoint(3, y))
                obstacles.add(SnakePoint(16, y))
            }
            for (y in 15..18) {
                obstacles.add(SnakePoint(3, y))
                obstacles.add(SnakePoint(16, y))
            }
        } else if (layoutPattern == 4 && !isBoss) {
            // Horizontal bar blockades with safe passages
            for (x in 2..7) {
                obstacles.add(SnakePoint(x, 8))
                obstacles.add(SnakePoint(20 - 1 - x, 18))
            }
        } else if (layoutPattern == 5 && !isBoss) {
            // Center cross leaving the center empty
            for (i in 3..7) {
                obstacles.add(SnakePoint(i, 10))
                obstacles.add(SnakePoint(20 - 1 - i, 10))
                obstacles.add(SnakePoint(10, i))
                obstacles.add(SnakePoint(10, 26 - i))
            }
        }
    }
    
    // Lava Tiles for Volcano levels
    if (specialMechanic == "LAVA" && !isBonus) {
        val lavaRow = 10
        for (x in 0 until 20) {
            if (x != 3 && x != 4 && x != 9 && x != 10 && x != 15 && x != 16) {
                lavaTiles.add(SnakePoint(x, lavaRow))
            }
        }
        val lavaRow2 = 18
        for (x in 0 until 20) {
            if (x != 5 && x != 6 && x != 13 && x != 14) {
                lavaTiles.add(SnakePoint(x, lavaRow2))
            }
        }
    }
    
    // Teleport Gates / Portals
    if (theme == SnakeTheme.SPACE || levelNum % 10 == 3 || levelNum % 10 == 7) {
        portals.add(Pair(SnakePoint(2, 5), SnakePoint(17, 20)))
        if (levelNum > 30) {
            portals.add(Pair(SnakePoint(17, 5), SnakePoint(2, 20)))
        }
    }
    
    // Breakable Walls
    if (theme == SnakeTheme.ANCIENT_TEMPLE || levelNum % 10 == 4) {
        if (!isBonus) {
            val breakY = 16
            for (x in 3..16) {
                if (x != 9 && x != 10) {
                    breakableWalls.add(SnakePoint(x, breakY))
                }
            }
        }
    }
    
    return SnakeLevel(
        number = levelNum,
        name = name,
        theme = theme,
        targetFruits = targetFruits,
        baseDelay = baseDelay,
        obstacles = obstacles,
        portals = portals,
        breakableWalls = breakableWalls,
        lavaTiles = lavaTiles,
        specialMechanic = specialMechanic,
        isBoss = isBoss,
        isBonus = isBonus
    )
}
