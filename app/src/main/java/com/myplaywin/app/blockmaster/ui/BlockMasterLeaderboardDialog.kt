package com.myplaywin.app.blockmaster.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.myplaywin.app.blockmaster.progression.PlayerProgressionManager
import com.myplaywin.app.blockmaster.storage.BlockMasterSaveData

data class LeaderboardEntry(
    val rank: Int,
    val playerName: String,
    val score: Long,
    val playerLevel: Int,
    val avatarEmoji: String,
    val title: String,
    val isCurrentUser: Boolean = false
)

object LeaderboardDataStore {
    fun getLeaderboard(typeIndex: Int, saveData: BlockMasterSaveData): List<LeaderboardEntry> {
        val userScore: Long = when (typeIndex) {
            0 -> saveData.highScore.toLong() // Daily
            1 -> saveData.highScore.toLong() * 2L // Weekly
            2 -> saveData.lifetimeScore // All-Time
            else -> saveData.highScore.toLong() // Friends
        }

        val dummyEntries = when (typeIndex) {
            0 -> listOf( // Daily
                LeaderboardEntry(1, "BlockNinja_99", 48500, 42, "🥷", "Infinite Legend"),
                LeaderboardEntry(2, "CyberStacker", 42100, 38, "🤖", "Cosmic Architect"),
                LeaderboardEntry(3, "NeonQueen", 39800, 35, "👑", "Realm Sovereign"),
                LeaderboardEntry(4, "PixelGod", 36200, 31, "⚡", "Block Legend"),
                LeaderboardEntry(5, "TetrisPro_IN", 31400, 27, "💎", "Combo Champion")
            )
            1 -> listOf( // Weekly
                LeaderboardEntry(1, "VortexMaster", 284000, 85, "🌀", "Infinite Legend"),
                LeaderboardEntry(2, "BlockNinja_99", 242000, 42, "🥷", "Infinite Legend"),
                LeaderboardEntry(3, "ZenStacker", 198000, 64, "🧘", "Infinity God"),
                LeaderboardEntry(4, "HyperCube", 175000, 50, "🧊", "Realm Sovereign"),
                LeaderboardEntry(5, "CyberStacker", 162000, 38, "🤖", "Cosmic Architect")
            )
            2 -> listOf( // All-Time
                LeaderboardEntry(1, "GenesisKing", 1250000, 150, "👑", "Infinity God"),
                LeaderboardEntry(2, "VortexMaster", 980000, 85, "🌀", "Infinite Legend"),
                LeaderboardEntry(3, "BlockNinja_99", 840000, 42, "🥷", "Infinite Legend"),
                LeaderboardEntry(4, "AlphaStacker", 720000, 95, "⚡", "Infinity God"),
                LeaderboardEntry(5, "ZenStacker", 690000, 64, "🧘", "Infinity God")
            )
            else -> listOf( // Friends
                LeaderboardEntry(1, "Rahul_PlayWin", 34200, 25, "🎮", "Tetris Expert"),
                LeaderboardEntry(2, "Priya_Blocks", 29800, 21, "⭐", "Tetris Expert"),
                LeaderboardEntry(3, "Amit_Gamer", 24500, 18, "🔥", "Line Master")
            )
        }

        val currentRank = PlayerProgressionManager.getRankForLevel(saveData.playerLevel)
        val userEntry = LeaderboardEntry(
            rank = 6,
            playerName = saveData.playerName.ifEmpty { "YOU" },
            score = userScore,
            playerLevel = saveData.playerLevel,
            avatarEmoji = "🚀",
            title = currentRank.title,
            isCurrentUser = true
        )

        val combined = (dummyEntries + userEntry).sortedByDescending { it.score }
        return combined.mapIndexed { index, entry -> entry.copy(rank = index + 1) }
    }
}

@Composable
fun BlockMasterLeaderboardDialog(
    saveData: BlockMasterSaveData,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("📅 DAILY", "🏆 WEEKLY", "🌐 ALL-TIME", "👥 FRIENDS")

    val leaderboardEntries = remember(selectedTab, saveData) {
        LeaderboardDataStore.getLeaderboard(selectedTab, saveData)
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E1430), Color(0xFF100B20))
                    )
                )
                .border(
                    width = 2.dp,
                    color = Color(0xFFFFD700),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🥇", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "GLOBAL LEADERBOARD",
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Compete with top players worldwide",
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                // Tabs Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tabs.forEachIndexed { idx, tabTitle ->
                        val isSel = selectedTab == idx
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) Color(0xFFFFD700).copy(alpha = 0.25f) else Color(0xFF161129),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSel) Color(0xFFFFD700) else Color.DarkGray
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = idx }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                                Text(
                                    text = tabTitle,
                                    color = if (isSel) Color(0xFFFFD700) else Color.Gray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                // Leaderboard List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    itemsIndexed(leaderboardEntries) { _, entry ->
                        LeaderboardItemCard(entry = entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardItemCard(entry: LeaderboardEntry) {
    val rankColor = when (entry.rank) {
        1 -> Color(0xFFFFD700) // Gold
        2 -> Color(0xFFC0C0C0) // Silver
        3 -> Color(0xFFCD7F32) // Bronze
        else -> Color.Gray
    }

    val rankIcon = when (entry.rank) {
        1 -> "🥇"
        2 -> "🥈"
        3 -> "🥉"
        else -> "#${entry.rank}"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (entry.isCurrentUser) Color(0xFF261D42) else Color(0xFF161026)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (entry.isCurrentUser) Color(0xFF00E5FF) else rankColor.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = rankColor.copy(alpha = 0.2f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = rankIcon,
                            color = rankColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(text = entry.avatarEmoji, fontSize = 20.sp)

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = entry.playerName,
                            color = if (entry.isCurrentUser) Color(0xFF00E5FF) else Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                        if (entry.isCurrentUser) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF00E5FF).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "YOU",
                                    color = Color(0xFF00E5FF),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "Lvl ${entry.playerLevel} • ${entry.title}",
                        color = Color.LightGray,
                        fontSize = 10.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${entry.score}",
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
                Text(
                    text = "PTS",
                    color = Color.Gray,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
