package com.myplaywin.app.blockmaster.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.myplaywin.app.blockmaster.world.BlockWorld
import com.myplaywin.app.blockmaster.world.WorldProgressionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockMasterWorldMapDialog(
    playerLevel: Int,
    activeWorldId: Int,
    claimedWorldRewards: Set<String>,
    onSelectWorld: (Int) -> Unit,
    onClaimReward: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var rewardCelebrationWorld by remember { mutableStateOf<BlockWorld?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
                .shadow(24.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
            color = Color(0xFF0F0C1B)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "WORLD ADVENTURE MAP",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            val unlockedCount = WorldProgressionManager.getUnlockedWorlds(playerLevel).size
                            Text(
                                text = "Unlocked $unlockedCount / ${WorldProgressionManager.WORLDS.size} Biomes (Level $playerLevel)",
                                fontSize = 12.sp,
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.1f), CircleShape)
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // World List
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(WorldProgressionManager.WORLDS, key = { it.id }) { world ->
                            val isUnlocked = playerLevel >= world.minLevel
                            val isActive = activeWorldId == world.id
                            val isClaimed = claimedWorldRewards.contains(world.id.toString())

                            WorldCardItem(
                                world = world,
                                playerLevel = playerLevel,
                                isUnlocked = isUnlocked,
                                isActive = isActive,
                                isClaimed = isClaimed,
                                onSelect = {
                                    if (isUnlocked) {
                                        onSelectWorld(world.id)
                                    }
                                },
                                onClaim = {
                                    if (isUnlocked && !isClaimed) {
                                        onClaimReward(world.id)
                                        rewardCelebrationWorld = world
                                    }
                                }
                            )
                        }
                    }
                }

                // Reward Celebration Dialog Overlay
                rewardCelebrationWorld?.let { world ->
                    WorldRewardCelebrationOverlay(
                        world = world,
                        onDismiss = { rewardCelebrationWorld = null }
                    )
                }
            }
        }
    }
}

@Composable
private fun WorldCardItem(
    world: BlockWorld,
    playerLevel: Int,
    isUnlocked: Boolean,
    isActive: Boolean,
    isClaimed: Boolean,
    onSelect: () -> Unit,
    onClaim: () -> Unit
) {
    val cardBg = if (isActive) {
        Brush.horizontalGradient(
            colors = listOf(world.bgGradientTop, world.bgGradientBottom)
        )
    } else if (isUnlocked) {
        Brush.horizontalGradient(
            colors = listOf(Color(0xFF1B162E), Color(0xFF131024))
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(Color(0xFF120E1C), Color(0xFF0A0712))
        )
    }

    val borderColor = if (isActive) {
        world.accentColor
    } else if (isUnlocked) {
        world.accentColor.copy(alpha = 0.4f)
    } else {
        Color.White.copy(alpha = 0.1f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = isUnlocked, onClick = onSelect),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBg)
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Biome Icon Badge
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isUnlocked) world.accentColor.copy(alpha = 0.2f)
                            else Color.White.copy(alpha = 0.05f)
                        )
                        .border(
                            1.dp,
                            if (isUnlocked) world.accentColor else Color.Gray.copy(alpha = 0.3f),
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isUnlocked) {
                        Text(text = world.iconEmoji, fontSize = 26.sp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Info Column
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = world.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isUnlocked) Color.White else Color.Gray
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        if (isActive) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = world.accentColor
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = world.description,
                        fontSize = 11.sp,
                        color = if (isUnlocked) Color.LightGray else Color.DarkGray,
                        lineHeight = 14.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Gameplay Modifier Tag
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isUnlocked) world.accentColor.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f)
                    ) {
                        Text(
                            text = world.gameplayModifier.description,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isUnlocked) world.accentColor else Color.Gray,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Level Requirement / Progress if Locked
                    if (!isUnlocked) {
                        Spacer(modifier = Modifier.height(6.dp))
                        val progress = (playerLevel.toFloat() / world.minLevel.toFloat()).coerceIn(0f, 1f)
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Unlocks at Level ${world.minLevel}",
                                    fontSize = 10.sp,
                                    color = Color(0xFFFF9100),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$playerLevel / ${world.minLevel}",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = Color(0xFFFF9100),
                                trackColor = Color.White.copy(alpha = 0.1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Action Button (Claim Reward or Select)
                if (isUnlocked) {
                    Column(horizontalAlignment = Alignment.End) {
                        if (!isClaimed) {
                            Button(
                                onClick = onClaim,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFD700)
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Claim",
                                    tint = Color.Black,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "CLAIM",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black
                                )
                            }
                        } else if (!isActive) {
                            OutlinedButton(
                                onClick = onSelect,
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = Brush.horizontalGradient(listOf(world.accentColor, world.accentColor))
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(
                                    text = "SELECT",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = world.accentColor
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Active World",
                                tint = world.accentColor,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorldRewardCelebrationOverlay(
    world: BlockWorld,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF161226),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .border(2.dp, Color(0xFFFFD700), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "🎉", fontSize = 48.sp)

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "BIOME UNLOCKED REWARD!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFD700),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = world.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Badge display
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF241C3B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = world.rewardBadge,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF00E5FF)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🪙 +${world.rewardCoins} Coins",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD700)
                            )
                            Text(
                                text = "⭐ +${world.rewardXp} XP",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFA855F7)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "COLLECT REWARD",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                }
            }
        }
    }
}
