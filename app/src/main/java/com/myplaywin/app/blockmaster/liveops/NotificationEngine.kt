package com.myplaywin.app.blockmaster.liveops

data class LiveNotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val iconEmoji: String,
    val timestamp: Long = System.currentTimeMillis()
)

object NotificationEngine {

    fun checkPendingNotifications(
        hasDailyMissionsToClaim: Boolean,
        hasWeeklyMissionsToClaim: Boolean,
        hasLoginRewardToClaim: Boolean,
        unclaimedAchievementsCount: Int,
        activeChestCount: Int,
        activeEventTitle: String
    ): List<LiveNotificationItem> {
        val list = mutableListOf<LiveNotificationItem>()

        if (hasLoginRewardToClaim) {
            list.add(
                LiveNotificationItem(
                    id = "notif_login_reward",
                    title = "Daily Calendar Reward Ready!",
                    message = "Claim today's 30-day calendar reward now!",
                    iconEmoji = "🎁"
                )
            )
        }

        if (hasDailyMissionsToClaim) {
            list.add(
                LiveNotificationItem(
                    id = "notif_daily_mission",
                    title = "Daily Mission Reward!",
                    message = "You have completed daily missions ready to claim!",
                    iconEmoji = "🎯"
                )
            )
        }

        if (hasWeeklyMissionsToClaim) {
            list.add(
                LiveNotificationItem(
                    id = "notif_weekly_mission",
                    title = "Weekly Mission Reward!",
                    message = "Claim your massive weekly mission reward!",
                    iconEmoji = "🏆"
                )
            )
        }

        if (unclaimedAchievementsCount > 0) {
            list.add(
                LiveNotificationItem(
                    id = "notif_achievements",
                    title = "$unclaimedAchievementsCount Achievements Unlocked!",
                    message = "Head to the Achievements tab to collect rewards!",
                    iconEmoji = "🎖️"
                )
            )
        }

        if (activeChestCount > 0) {
            list.add(
                LiveNotificationItem(
                    id = "notif_chest",
                    title = "Mystery Chest Ready!",
                    message = "You have $activeChestCount unopened mystery chests!",
                    iconEmoji = "📦"
                )
            )
        }

        list.add(
            LiveNotificationItem(
                id = "notif_event",
                title = "Live Event Active!",
                message = "$activeEventTitle is currently live with bonus rewards!",
                iconEmoji = "🔥"
            )
        )

        return list
    }
}
