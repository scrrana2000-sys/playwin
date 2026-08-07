package com.myplaywin.app.blockmaster.liveops

import androidx.compose.ui.graphics.Color
import java.util.Calendar

data class SpecialLiveEvent(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val iconEmoji: String,
    val primaryColor: Color,
    val accentColor: Color,
    val coinMultiplier: Float = 1.0f,
    val xpMultiplier: Float = 1.0f,
    val specialBonusDescription: String,
    val startMonth: Int, // 1-12
    val endMonth: Int
)

object SpecialEventsManager {

    val EVENTS = listOf(
        SpecialLiveEvent(
            id = "event_diwali",
            title = "Diwali Festival of Lights",
            subtitle = "Sparkling Gold Live Event",
            description = "Celebrate Diwali! Enjoy 2x Bonus Coins on all line clears and special sparkling board themes.",
            iconEmoji = "🪔",
            primaryColor = Color(0xFFFF9900),
            accentColor = Color(0xFFFFD700),
            coinMultiplier = 2.0f,
            specialBonusDescription = "2x Bonus Coins & Golden Fireworks Particles",
            startMonth = 10,
            endMonth = 11
        ),
        SpecialLiveEvent(
            id = "event_halloween",
            title = "Spooky Halloween Night",
            subtitle = "Ghostly Dark Live Event",
            description = "Haunted blocks roam the grid! Earn 1.5x XP on every mission and combo clear.",
            iconEmoji = "🎃",
            primaryColor = Color(0xFF9C27B0),
            accentColor = Color(0xFFFF5722),
            xpMultiplier = 1.5f,
            specialBonusDescription = "1.5x Bonus XP & Pumpkin Particle Effects",
            startMonth = 10,
            endMonth = 10
        ),
        SpecialLiveEvent(
            id = "event_christmas",
            title = "Winter Christmas Wonderland",
            subtitle = "Frost & Ice Festival",
            description = "Shatter frost blocks and earn festive mystery chests in matches!",
            iconEmoji = "🎄",
            primaryColor = Color(0xFF00E5FF),
            accentColor = Color(0xFFFF1744),
            coinMultiplier = 1.5f,
            xpMultiplier = 1.5f,
            specialBonusDescription = "1.5x Coins & XP + Festive Snowfall Particles",
            startMonth = 12,
            endMonth = 12
        ),
        SpecialLiveEvent(
            id = "event_newyear",
            title = "New Year Carnival 2026",
            subtitle = "Rainbow Celebration",
            description = "Kick off the New Year with double score multipliers and fireworks celebrations!",
            iconEmoji = "🎆",
            primaryColor = Color(0xFFE040FB),
            accentColor = Color(0xFF00E676),
            coinMultiplier = 1.8f,
            xpMultiplier = 1.8f,
            specialBonusDescription = "1.8x Coin & XP Boost + Confetti Particles",
            startMonth = 1,
            endMonth = 1
        ),
        SpecialLiveEvent(
            id = "event_anniversary",
            title = "PlayWin Anniversary Gala",
            subtitle = "Ultimate Block Master Event",
            description = "Celebrate the PlayWin Anniversary with maximum rewards, free chests, and ultimate challenges!",
            iconEmoji = "👑",
            primaryColor = Color(0xFFFFD700),
            accentColor = Color(0xFF00E5FF),
            coinMultiplier = 2.5f,
            xpMultiplier = 2.5f,
            specialBonusDescription = "2.5x Ultimate Boost & Gold Particle Shower",
            startMonth = 8,
            endMonth = 8
        )
    )

    fun getActiveEvent(): SpecialLiveEvent {
        val month = Calendar.getInstance().get(Calendar.MONTH) + 1
        return EVENTS.find { month >= it.startMonth && month <= it.endMonth }
            ?: EVENTS.last() // Default to Anniversary / Gala if no month match
    }
}
