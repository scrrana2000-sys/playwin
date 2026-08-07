package com.myplaywin.app.blockmaster.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.playwin.ads.RewardType

@Composable
fun BlockMasterAdRewardDialog(
    matchCoins: Int,
    canDoubleCoins: Boolean,
    onWatchAdForDouble: () -> Unit,
    onWatchAdForBonusCoins: () -> Unit,
    onWatchAdForPowerUp: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E1435), Color(0xFF100B22))
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFFFD700), Color(0xFFA855F7), Color(0xFF00E5FF))
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📺 AD REWARD CENTER",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Text(
                    text = "Watch a quick sponsored video to earn instant rewards for your PlayWin Wallet!",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                // Reward Option 1: Double Match Coins
                if (matchCoins > 0 && canDoubleCoins) {
                    RewardOfferCard(
                        title = "2X MATCH COINS",
                        subtitle = "Turn +$matchCoins coins into +${matchCoins * 2} 🪙",
                        badge = "DOUBLE REWARD",
                        accentColor = Color(0xFFFFD700),
                        onClick = onWatchAdForDouble
                    )
                }

                // Reward Option 2: Extra 100 Bonus Coins
                RewardOfferCard(
                    title = "+100 BONUS COINS",
                    subtitle = "Instant PlayWin Wallet coin credit",
                    badge = "FREE COINS",
                    accentColor = Color(0xFF00E5FF),
                    onClick = onWatchAdForBonusCoins
                )

                // Reward Option 3: Extra Power-up Pack
                RewardOfferCard(
                    title = "REPLENISH POWER-UPS",
                    subtitle = "Get +2 Clear Row & +2 Bomb Power-ups",
                    badge = "POWER BOOST",
                    accentColor = Color(0xFFA855F7),
                    onClick = onWatchAdForPowerUp
                )
            }
        }
    }
}

@Composable
private fun RewardOfferCard(
    title: String,
    subtitle: String,
    badge: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161129)),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = accentColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = badge,
                        color = accentColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 8.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
                Text(text = subtitle, color = Color.Gray, fontSize = 10.sp)
            }

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.OndemandVideo,
                    contentDescription = "Watch",
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "WATCH", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
            }
        }
    }
}
