package com.myplaywin.app.blockmaster.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myplaywin.app.blockmaster.blocks.TetrominoBlock
import com.myplaywin.app.blockmaster.powerups.PowerUpRegistry
import com.myplaywin.app.blockmaster.powerups.PowerUpType
import com.myplaywin.app.blockmaster.storage.BlockMasterSaveData

@Composable
fun BlockMasterLeftPowerUpsColumn(
    saveData: BlockMasterSaveData,
    holdPiece: TetrominoBlock?,
    onUsePowerUp: (PowerUpType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // --- 3 Power-ups above HOLD box ---
        for (i in 0 until 3) {
            val pu = PowerUpRegistry.ALL_POWER_UPS[i]
            val count = when (pu.type) {
                PowerUpType.CLEAR_ROW -> saveData.powerUpClearRow
                PowerUpType.CLEAR_COLUMN -> saveData.powerUpClearCol
                PowerUpType.DESTROY_BLOCK -> saveData.powerUpDestroyBlock
                PowerUpType.FREEZE_TIME -> saveData.powerUpFreezeTime
                PowerUpType.SCORE_BOOSTER -> saveData.powerUpScoreBooster
                PowerUpType.COIN_BOOSTER -> saveData.powerUpCoinBooster
            }
            PowerUpVerticalButton(
                emoji = pu.iconEmoji,
                count = count,
                color = pu.color,
                onClick = { onUsePowerUp(pu.type) }
            )
        }

        // --- HOLD box in the center ---
        BlockMasterPreviewBox(
            title = "HOLD",
            piece = holdPiece,
            modifier = Modifier
                .width(52.dp)
                .height(72.dp)
        )

        // --- 3 Power-ups below HOLD box ---
        for (i in 3 until 6) {
            val pu = PowerUpRegistry.ALL_POWER_UPS[i]
            val count = when (pu.type) {
                PowerUpType.CLEAR_ROW -> saveData.powerUpClearRow
                PowerUpType.CLEAR_COLUMN -> saveData.powerUpClearCol
                PowerUpType.DESTROY_BLOCK -> saveData.powerUpDestroyBlock
                PowerUpType.FREEZE_TIME -> saveData.powerUpFreezeTime
                PowerUpType.SCORE_BOOSTER -> saveData.powerUpScoreBooster
                PowerUpType.COIN_BOOSTER -> saveData.powerUpCoinBooster
            }
            PowerUpVerticalButton(
                emoji = pu.iconEmoji,
                count = count,
                color = pu.color,
                onClick = { onUsePowerUp(pu.type) }
            )
        }
    }
}

@Composable
fun BlockMasterActiveBuffsRow(
    freezeTimeRemaining: Int,
    scoreBoosterRemaining: Int,
    coinBoosterRemaining: Int,
    modifier: Modifier = Modifier
) {
    if (freezeTimeRemaining > 0 || scoreBoosterRemaining > 0 || coinBoosterRemaining > 0) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (freezeTimeRemaining > 0) {
                ActiveBuffBadge(
                    label = "FREEZE ❄️",
                    timeSec = freezeTimeRemaining,
                    color = Color(0xFF80D8FF)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            if (scoreBoosterRemaining > 0) {
                ActiveBuffBadge(
                    label = "2x SCORE 🚀",
                    timeSec = scoreBoosterRemaining,
                    color = Color(0xFFFFD700)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            if (coinBoosterRemaining > 0) {
                ActiveBuffBadge(
                    label = "2x COINS 🪙",
                    timeSec = coinBoosterRemaining,
                    color = Color(0xFF00E676)
                )
            }
        }
    }
}

@Composable
private fun ActiveBuffBadge(
    label: String,
    timeSec: Int,
    color: Color
) {
    Row(
        modifier = Modifier
            .background(color.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
            .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 1.5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 8.5.sp
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = "${timeSec}s",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 8.5.sp
        )
    }
}

@Composable
private fun PowerUpVerticalButton(
    emoji: String,
    count: Int,
    color: Color,
    onClick: () -> Unit
) {
    val isAvailable = count > 0

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(30.dp) // Reduced by ~10% from 34.dp
            .clip(CircleShape)
            .background(
                if (isAvailable) color.copy(alpha = 0.25f) else Color(0xFF1E1730),
                CircleShape
            )
            .border(
                width = 1.dp,
                color = if (isAvailable) color else Color.Gray.copy(alpha = 0.25f),
                shape = CircleShape
            )
            .clickable(enabled = isAvailable) { onClick() }
    ) {
        Text(
            text = emoji,
            fontSize = 13.sp
        )

        // Badge Count
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 1.dp, y = (-1).dp)
                .background(
                    if (isAvailable) color else Color.Gray,
                    CircleShape
                )
                .padding(horizontal = 2.5.dp, vertical = 0.5.dp)
        ) {
            Text(
                text = "x$count",
                color = Color.Black,
                fontWeight = FontWeight.Black,
                fontSize = 7.sp
            )
        }
    }
}
