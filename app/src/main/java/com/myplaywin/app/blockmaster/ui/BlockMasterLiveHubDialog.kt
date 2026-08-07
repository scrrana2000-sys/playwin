package com.myplaywin.app.blockmaster.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
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
import com.myplaywin.app.blockmaster.liveops.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockMasterLiveHubDialog(
    dailyMissions: List<LiveMission>,
    weeklyMissions: List<LiveMission>,
    claimedDailyIds: Set<String>,
    claimedWeeklyIds: Set<String>,
    claimedLoginDays: Set<Int>,
    activeEvent: SpecialLiveEvent,
    onClaimDailyMission: (LiveMission) -> Unit,
    onClaimWeeklyMission: (LiveMission) -> Unit,
    onClaimLoginDay: (LoginRewardDay) -> Unit,
    onOpenChest: (MysteryChestRarity) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("🎯 DAILY", "🏆 WEEKLY", "📅 30-DAY LOGINS", "🔥 SPECIAL EVENT")

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .fillMaxHeight(0.85f),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        content = {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF130F24),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    Brush.verticalGradient(
                        listOf(Color(0xFF00E5FF), Color(0xFFA855F7))
                    )
                ),
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    // HEADER
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "LIVE OPERATIONS HUB",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Fresh daily rewards & live events",
                                color = Color(0xFF00E5FF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFF221C38), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // TAB BAR
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color(0xFF1A1430),
                        contentColor = Color.White,
                        edgePadding = 0.dp
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        text = title,
                                        fontWeight = if (selectedTab == index) FontWeight.Black else FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (selectedTab == index) Color(0xFF00E5FF) else Color.Gray
                                    )
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // CONTENT CONTAINER
                    Box(modifier = Modifier.weight(1f)) {
                        when (selectedTab) {
                            0 -> DailyMissionsList(
                                missions = dailyMissions,
                                claimedIds = claimedDailyIds,
                                onClaim = onClaimDailyMission
                            )
                            1 -> WeeklyMissionsList(
                                missions = weeklyMissions,
                                claimedIds = claimedWeeklyIds,
                                onClaim = onClaimWeeklyMission
                            )
                            2 -> LoginCalendarGrid(
                                claimedDays = claimedLoginDays,
                                onClaim = onClaimLoginDay
                            )
                            3 -> SpecialEventBannerView(
                                event = activeEvent,
                                onOpenChest = onOpenChest
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun DailyMissionsList(
    missions: List<LiveMission>,
    claimedIds: Set<String>,
    onClaim: (LiveMission) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(missions) { mission ->
            val isClaimed = claimedIds.contains(mission.id)
            LiveMissionCard(
                mission = mission,
                isClaimed = isClaimed,
                onClaim = { onClaim(mission) }
            )
        }
    }
}

@Composable
private fun WeeklyMissionsList(
    missions: List<LiveMission>,
    claimedIds: Set<String>,
    onClaim: (LiveMission) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(missions) { mission ->
            val isClaimed = claimedIds.contains(mission.id)
            LiveMissionCard(
                mission = mission,
                isClaimed = isClaimed,
                accentColor = Color(0xFFFFD700),
                onClaim = { onClaim(mission) }
            )
        }
    }
}

@Composable
private fun LiveMissionCard(
    mission: LiveMission,
    isClaimed: Boolean,
    accentColor: Color = Color(0xFF00E5FF),
    onClaim: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B162F)),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = mission.iconEmoji, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mission.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = mission.description,
                        color = Color.LightGray,
                        fontSize = 10.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Progress Bar
                    LinearProgressIndicator(
                        progress = { mission.progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = accentColor,
                        trackColor = Color(0xFF2A2345)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+${mission.rewardCoins} 🪙 | +${mission.rewardXp} XP",
                    color = Color(0xFFFFD700),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                val isReadyToClaim = mission.currentAmount >= mission.targetAmount && !isClaimed

                Button(
                    onClick = onClaim,
                    enabled = isReadyToClaim,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isClaimed) Color.Gray else accentColor,
                        disabledContainerColor = Color(0xFF2B2544)
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text(
                        text = if (isClaimed) "CLAIMED" else if (isReadyToClaim) "CLAIM" else "${mission.currentAmount}/${mission.targetAmount}",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun LoginCalendarGrid(
    claimedDays: Set<Int>,
    onClaim: (LoginRewardDay) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(LoginCalendarEngine.DAYS_30) { dayReward ->
            val isClaimed = claimedDays.contains(dayReward.dayNumber)
            val currentDayNum = claimedDays.size + 1
            val isAvailable = !isClaimed && dayReward.dayNumber <= currentDayNum

            Card(
                modifier = Modifier
                    .aspectRatio(0.85f)
                    .clickable(enabled = isAvailable) { onClaim(dayReward) },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        isClaimed -> Color(0xFF191428)
                        dayReward.isMilestone -> Color(0xFF3B270C)
                        else -> Color(0xFF221A3B)
                    }
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    when {
                        isClaimed -> Color.Gray.copy(alpha = 0.3f)
                        dayReward.isMilestone -> Color(0xFFFFD700)
                        isAvailable -> Color(0xFF00E5FF)
                        else -> Color(0xFF332A52)
                    }
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Day ${dayReward.dayNumber}",
                        color = if (dayReward.isMilestone) Color(0xFFFFD700) else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    )

                    Text(text = dayReward.iconEmoji, fontSize = 20.sp)

                    Text(
                        text = if (isClaimed) "CLAIMED" else if (isAvailable) "CLAIM!" else "+${dayReward.rewardCoins}🪙",
                        color = if (isClaimed) Color.Gray else if (isAvailable) Color(0xFF00E5FF) else Color.LightGray,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun SpecialEventBannerView(
    event: SpecialLiveEvent,
    onOpenChest: (MysteryChestRarity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(event.primaryColor.copy(alpha = 0.2f), Color(0xFF140F26))
                ),
                RoundedCornerShape(14.dp)
            )
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = event.iconEmoji, fontSize = 48.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = event.title,
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp
        )
        Text(
            text = event.subtitle,
            color = event.accentColor,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1736)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "LIVE EVENT BONUSES",
                    color = event.accentColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = event.description,
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = event.accentColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "✨ ${event.specialBonusDescription}",
                        color = event.accentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "EVENT CHEST REWARDS",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { onOpenChest(MysteryChestRarity.GOLD) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Open Gold Chest 🥇", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
            }

            Button(
                onClick = { onOpenChest(MysteryChestRarity.LEGENDARY) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE040FB)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Open Crown Chest 👑", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
            }
        }
    }
}
