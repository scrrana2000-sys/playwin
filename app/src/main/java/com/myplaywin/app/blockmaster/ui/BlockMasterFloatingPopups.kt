package com.myplaywin.app.blockmaster.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myplaywin.app.blockmaster.effects.FloatingPopupData

@Composable
fun BlockMasterFloatingPopups(
    popups: List<FloatingPopupData>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        popups.forEach { popup ->
            AnimatedPopupItem(popup = popup)
        }
    }
}

@Composable
private fun AnimatedPopupItem(popup: FloatingPopupData) {
    var animState by remember { mutableStateOf(false) }

    LaunchedEffect(popup.id) {
        animState = true
    }

    val scale by animateFloatAsState(
        targetValue = if (animState) 1.15f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "PopupScale"
    )

    val offsetY by animateDpAsState(
        targetValue = if (animState) (-40).dp else 20.dp,
        animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing),
        label = "PopupOffset"
    )

    val alpha by animateFloatAsState(
        targetValue = if (animState) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "PopupAlpha"
    )

    Box(
        modifier = Modifier
            .offset(y = offsetY)
            .scale(scale)
            .alpha(alpha)
            .background(Color(0xFF141022).copy(alpha = 0.9f), RoundedCornerShape(12.dp))
            .border(1.5.dp, popup.color, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = popup.text,
                color = popup.color,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp
            )
            popup.subText?.let { sub ->
                Text(
                    text = sub,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}
