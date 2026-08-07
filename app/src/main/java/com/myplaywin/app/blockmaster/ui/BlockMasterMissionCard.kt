package com.myplaywin.app.blockmaster.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myplaywin.app.blockmaster.missions.MissionObjective

@Composable
fun BlockMasterMissionCard(
    missions: List<MissionObjective>,
    accentColor: Color = Color(0xFF00E5FF),
    modifier: Modifier = Modifier
) {
    if (missions.isEmpty()) return

    // Pick the current active (uncompleted) mission, or the first one
    val activeMission = missions.firstOrNull { !it.isCompleted } ?: missions.first()

    val progressAnim by animateFloatAsState(
        targetValue = activeMission.progressFraction,
        label = "missionProgressAnim"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF140C26)),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                listOf(accentColor.copy(alpha = 0.4f), Color(0xFFA855F7).copy(alpha = 0.3f))
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "🎯 ${activeMission.description}",
                color = if (activeMission.isCompleted) Color(0xFF00E676) else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                maxLines = 1
            )

            // Progress Bar
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF22173D))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progressAnim)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (activeMission.isCompleted) Color(0xFF00E676) else accentColor
                        )
                )
            }

            Text(
                text = "${activeMission.currentAmount}/${activeMission.targetAmount}",
                color = if (activeMission.isCompleted) Color(0xFF00E676) else accentColor,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp
            )
        }
    }
}
