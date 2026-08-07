package com.myplaywin.app.blockmaster.engine

import androidx.compose.ui.graphics.Color

enum class GameMode(
    val displayName: String,
    val description: String,
    val emojiIcon: String,
    val themeColor: Color,
    val timeLimitSec: Int = 0, // 0 = unlimited
    val baseSpeedMultiplier: Float = 1.0f,
    val allowsPowerUps: Boolean = true
) {
    CLASSIC(
        displayName = "Classic",
        description = "Standard level progression with goals, missions, and worlds.",
        emojiIcon = "🧩",
        themeColor = Color(0xFF00E5FF)
    ),
    ENDLESS(
        displayName = "Endless",
        description = "Infinite puzzle challenge! Speed scales smoothly with score.",
        emojiIcon = "♾️",
        themeColor = Color(0xFFA855F7)
    ),
    TIME_ATTACK_2M(
        displayName = "Time Attack (2M)",
        description = "Race against time! Score maximum points in 120 seconds.",
        emojiIcon = "⏱️",
        themeColor = Color(0xFFFFD700),
        timeLimitSec = 120
    ),
    TIME_ATTACK_5M(
        displayName = "Time Attack (5M)",
        description = "5-minute endurance speed run! High combo score multipliers.",
        emojiIcon = "⏳",
        themeColor = Color(0xFFFF9100),
        timeLimitSec = 300
    ),
    SURVIVAL(
        displayName = "Survival",
        description = "Hazardous block pressure! Test your line clear survival skills.",
        emojiIcon = "☣️",
        themeColor = Color(0xFFFF3D00),
        baseSpeedMultiplier = 1.3f
    ),
    ZEN(
        displayName = "Zen Mode",
        description = "Stress-free relaxed play with no drop speed acceleration.",
        emojiIcon = "🧘",
        themeColor = Color(0xFF00E676),
        baseSpeedMultiplier = 0.7f
    ),
    HARDCORE(
        displayName = "Hardcore",
        description = "Ultra high drop speed for true block master grandmasters!",
        emojiIcon = "🔥",
        themeColor = Color(0xFFE040FB),
        baseSpeedMultiplier = 2.0f
    ),
    DAILY_CHALLENGE(
        displayName = "Daily Challenge",
        description = "Unique objective updated every 24h with double coin rewards!",
        emojiIcon = "📅",
        themeColor = Color(0xFFFFD700)
    )
}
