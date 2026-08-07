package com.myplaywin.app.blockmaster.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.myplaywin.app.blockmaster.seasons.SeasonEngine
import com.myplaywin.app.blockmaster.seasons.SeasonPassLevel

@Composable
fun BlockMasterSeasonPassDialog(
    currentSeasonId: Int,
    seasonXp: Long,
    claimedFree: Set<String>,
    claimedPremium: Set<String>,
    onClaimFree: (Int) -> Unit,
    onClaimPremium: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedSeasonId by remember { mutableIntStateOf(currentSeasonId) }
    val activeSeasonData = remember(selectedSeasonId) { SeasonEngine.getSeasonById(selectedSeasonId) }
    val passLevels = remember(selectedSeasonId) { SeasonEngine.getSeasonPassLevels(selectedSeasonId) }
    val currentPassLvl = remember(seasonXp) { SeasonEngine.calculatePassLevel(seasonXp) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(activeSeasonData.bgGradient.first()),
                            Color(activeSeasonData.bgGradient.last())
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    color = Color(activeSeasonData.themeHex),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = activeSeasonData.iconEmoji, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = activeSeasonData.name.uppercase(),
                                color = Color(activeSeasonData.themeHex),
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                            Text(
                                text = activeSeasonData.subtitle,
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

                // Season Selection Selector
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(SeasonEngine.seasonsList) { s ->
                        val isSel = s.id == selectedSeasonId
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSel) Color(s.themeHex).copy(alpha = 0.3f) else Color(0xFF161129),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSel) Color(s.themeHex) else Color.DarkGray
                            ),
                            modifier = Modifier.clickable { selectedSeasonId = s.id }
                        ) {
                            Text(
                                text = "${s.iconEmoji} S${s.id}",
                                color = if (isSel) Color(s.themeHex) else Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Season Pass Level & XP Progress Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1635))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "PASS LEVEL $currentPassLvl / 100",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Season XP: $seasonXp",
                                color = Color(activeSeasonData.themeHex),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        LinearProgressIndicator(
                            progress = { ((seasonXp % 300L).toFloat() / 300f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .width(120.dp)
                                .height(8.dp)
                                .clip(CircleShape),
                            color = Color(activeSeasonData.themeHex),
                            trackColor = Color.DarkGray
                        )
                    }
                }

                // 100 Levels Scrollable Pass List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(passLevels) { passLvl ->
                        SeasonPassLevelItem(
                            item = passLvl,
                            userPassLvl = currentPassLvl,
                            isFreeClaimed = claimedFree.contains("s${selectedSeasonId}_free_${passLvl.level}"),
                            isPremiumClaimed = claimedPremium.contains("s${selectedSeasonId}_prem_${passLvl.level}"),
                            themeHex = activeSeasonData.themeHex,
                            onClaimFree = { onClaimFree(passLvl.level) },
                            onClaimPremium = { onClaimPremium(passLvl.level) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SeasonPassLevelItem(
    item: SeasonPassLevel,
    userPassLvl: Int,
    isFreeClaimed: Boolean,
    isPremiumClaimed: Boolean,
    themeHex: Long,
    onClaimFree: () -> Unit,
    onClaimPremium: () -> Unit
) {
    val isUnlocked = userPassLvl >= item.level

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) Color(0xFF1E1736) else Color(0xFF130E22)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isUnlocked) Color(themeHex).copy(alpha = 0.4f) else Color.DarkGray.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = CircleShape,
                color = if (isUnlocked) Color(themeHex) else Color.DarkGray,
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${item.level}",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = "FREE TRACK", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                val freeText = when {
                    item.freeRewardCoins > 0 -> "+${item.freeRewardCoins} 🪙"
                    item.freeRewardPowerUp != null -> "⚡ ${item.freeRewardPowerUp}"
                    item.freeRewardTitle != null -> "🏷️ ${item.freeRewardTitle}"
                    else -> "+50 🪙"
                }
                Text(text = freeText, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                if (isUnlocked) {
                    if (isFreeClaimed) {
                        Text(text = "CLAIMED ✓", color = Color.Green, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Button(
                            onClick = onClaimFree,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(themeHex)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Text(text = "CLAIM", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            Divider(
                modifier = Modifier
                    .height(30.dp)
                    .width(1.dp),
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = "PREMIUM 👑", color = Color(0xFFFFD700), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                val premText = when {
                    item.premiumRewardCosmeticId != null -> "🎁 ${item.premiumRewardCosmeticId}"
                    item.premiumRewardCoins > 0 -> "+${item.premiumRewardCoins} 🪙"
                    else -> "+200 🪙"
                }
                Text(text = premText, color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Bold)

                if (isUnlocked) {
                    if (isPremiumClaimed) {
                        Text(text = "CLAIMED ✓", color = Color.Green, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Button(
                            onClick = onClaimPremium,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Text(text = "CLAIM", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}
